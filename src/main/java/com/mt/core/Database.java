package com.mt.core;

import static com.mt.config.ConfigurationKey.DATABASE_AUTOSAVE_GENERATED;
import static com.mt.config.ConfigurationKey.DATABASE_PASSWORD;
import static com.mt.config.ConfigurationKey.DATABASE_SCHEMA;
import static com.mt.config.ConfigurationKey.DATABASE_TABLE_ADDRESS;
import static com.mt.config.ConfigurationKey.DATABASE_TABLE_ADDRESS_FIELD;
import static com.mt.config.ConfigurationKey.DATABASE_TABLE_ADDRESS_PRIVATE_KEY_FIELD;
import static com.mt.config.ConfigurationKey.DATABASE_TABLE_AUTOSAVE;
import static com.mt.config.ConfigurationKey.DATABASE_TABLE_AUTOSAVE_ADDRESS_FIELD;
import static com.mt.config.ConfigurationKey.DATABASE_TABLE_AUTOSAVE_ADDRESS_PRIVATE_KEY_FIELD;
import static com.mt.config.ConfigurationKey.DATABASE_URL;
import static com.mt.config.ConfigurationKey.DATABASE_USER;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mt.config.ApplicationConfiguration;

/**
 * This class represents a database communication interface for Wallet
 * objects.<p>
 * Its main purpose is to support the following goals:
 * <ul>
 * <li>finding the wallets matched on addresses in the configured table</li>
 * <li>updating the configured table by assigning the private key to the found addresses</li>
 * <li>saving the generated (usually searched) wallets into a separate table if configured</li>
 * </ul>
 *
 * @author mkrajcovic
 */
public class Database implements AutoCloseable {

	private Connection connection;
	private boolean recover;

	private final String url;
	private final String usr;
	private final String pwd;

	private final String schema;
	private final String table;
	private final String addressField;
	private final String privateKeyField;

	// TODO: rename these and provide public method to do the insert?
	// it would then do nothing if it was not set
	// Pros of this solution is to clearly read it from bussiness code
	private final boolean isAutosaveGeneratedAllowed;
	private String autosaveTable;
	private String autosaveAddressField;
	private String autosavePrivateKeyField;

	public Database(ApplicationConfiguration config) {
		requireNonNull(config);
		recover = true;

		url = requireNonNull(config.get(DATABASE_URL), "jdbc database URL configuration cannot be null");
		usr = requireNonNull(config.get(DATABASE_USER), "database username configuration cannot be null");
		pwd = requireNonNull(config.get(DATABASE_PASSWORD), "database password configuration cannot be null");

		// this comes from the provided DDL script
		schema = config.get(DATABASE_SCHEMA, "bitcoin");
		table = config.get(DATABASE_TABLE_ADDRESS, "t_address");
		addressField = config.get(DATABASE_TABLE_ADDRESS_FIELD, "s_address");
		privateKeyField = config.get(DATABASE_TABLE_ADDRESS_PRIVATE_KEY_FIELD, "s_private_key");

		isAutosaveGeneratedAllowed = Boolean.valueOf(config.get(DATABASE_AUTOSAVE_GENERATED));
		if (isAutosaveGeneratedAllowed) {
			autosaveTable = requireNonNull(config.get(DATABASE_TABLE_AUTOSAVE));
			autosaveAddressField = requireNonNull(config.get(DATABASE_TABLE_AUTOSAVE_ADDRESS_FIELD));
			autosavePrivateKeyField = requireNonNull(config.get(DATABASE_TABLE_AUTOSAVE_ADDRESS_PRIVATE_KEY_FIELD));
		}
	}

	private Connection getConnection() {
		try {
			if (recover && (connection == null || connection.isClosed())) {
				connection = DriverManager.getConnection(url, usr, pwd);
				connection.setAutoCommit(true);
			}
		} catch (SQLException sqle) {
			throw new IllegalStateException("Could not re-/connect to the database", sqle);
		}
		return connection;
	}

	/**
	 * Queries the database for a match on provided wallet addresses.
	 * <p>
	 * If the automatic save of generated (searched) wallets is configured, this
	 * method will perform this additional operation.
	 * </p>
	 *
	 * @param wallets - to find a match for
	 * @return list of wallets which the match was found for
	 */
	public List<Wallet> findAddresses(List<Wallet> wallets) {
		List<String> addresses = extractAllAddresses(wallets);
		String query = createParameterizedQuery(addresses.size());
		System.out.println("Calling " + query);

		try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
			List<String> foundAddresses = queryForAddresses(pstmt, addresses);
			return retainMatchedWallets(wallets, foundAddresses);
		} catch (SQLException sqle) {
			throw new RuntimeException("Error calling " + query + " with params " + addresses, sqle);
		} finally {
			if (isAutosaveGeneratedAllowed) {
				saveGenerated(wallets);
			}
		}
	}

	// works only for compatible addresses which share the same set of address types
	// otherwise the variable length of placeholders would break prepared batches
	private List<String> extractAllAddresses(List<Wallet> wallets) {
		List<String> addresses = new ArrayList<>();
		for (Wallet wallet : wallets) {
			for (AddressType addressType : wallet.getSupportedAddressTypes()) {
				addresses.add(wallet.getAddress(addressType));
			}
		}
		return addresses;
	}

	private String createParameterizedQuery(int placeholders) {
		return "SELECT " + addressField
			+ " FROM " + schema + "." + table
			+ " WHERE " + privateKeyField + " IS NULL"
			+ " AND " + addressField + " IN ("
			+ repeat("?", placeholders, ",") + ");";
	}

	private List<String> queryForAddresses(PreparedStatement pstmt, List<String> lookup) throws SQLException {
		for (int i = 0; i < lookup.size(); i++) {
			pstmt.setString(i + 1, lookup.get(i));
		}
		ResultSet rs = pstmt.executeQuery();
		List<String> foundAddresses = new ArrayList<>();
		while (rs.next()) {
			foundAddresses.add(rs.getString(addressField));
		}
		return foundAddresses;
	}

	private List<Wallet> retainMatchedWallets(List<Wallet> wallets, List<String> addresses) {
		return wallets.stream()
		.filter(wallet -> wallet.getSupportedAddressTypes()
			.stream()
			.anyMatch(addType -> addresses.contains(wallet.getAddress(addType))))
		.collect(toList());
	}

	private void saveGenerated(List<Wallet> wallets) {
		String insert = createParameterizedInsert(2);
		System.out.println("Calling " + insert + " for " + wallets.size() + " wallets");

		try (PreparedStatement pstmt = getConnection().prepareStatement(insert)) {
			// do this for all the address types?
			// if so, make it configurable to choose between all address types
			// or just the base one
			for (Wallet wallet : wallets) {
				pstmt.setString(1, wallet.getAddress());
				pstmt.setString(2, wallet.getPrivateKey());
				pstmt.addBatch();
			}
			pstmt.executeBatch();
		} catch (SQLException sqle) {
			throw new RuntimeException(insert, sqle);
		}
	}

	private String createParameterizedInsert(int placeholders) {
		// stick to the currently used schema
		return "INSERT INTO " + schema + "." + autosaveTable 
			+ "(" + autosaveAddressField + "," + autosavePrivateKeyField 
			+ ") VALUES (" + repeat("?", placeholders, ",") + ");";
	}

	/**
	 * Performs insert of private keys to corresponding addresses stored already in
	 * the database.
	 *
	 * @param wallets - of which the private keys should be inserted
	 */
	// TODO: return list of more readable errors for those records that fail to update ?
	public void savePrivateKeys(List<Wallet> wallets) {
		String update = createParameterizedUpdate(wallets.get(0).getSupportedAddressTypes().size());
		System.out.println("Calling " + update);

		try (PreparedStatement pstmt = getConnection().prepareStatement(update)) {
			insertPrivateKeysForAddresses(pstmt, wallets);
		} catch (SQLException sqle) {
			throw new RuntimeException("Error calling " + update + " for wallets: " + wallets);
		}
	}

	private String createParameterizedUpdate(int placeholders) {
		return "UPDATE " + schema + "." + table 
			+ " SET " + privateKeyField + " = ? WHERE " 
			+ addressField + " IN (" 
			+ repeat("?", placeholders, ",") + ");";
	}

	// this method does not belong here
	private String repeat(String what, int howMany, String delimiter) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < howMany; i++) {
			sb.append(what).append(delimiter);
		}
		return sb.deleteCharAt(sb.length() - 1).toString();
	}

	private void insertPrivateKeysForAddresses(PreparedStatement pstmt, List<Wallet> wallets) throws SQLException {
		for (Wallet wallet : wallets) {
			pstmt.setString(1, wallet.getPrivateKey());

			int position = 2;
			for (AddressType addressType : wallet.getSupportedAddressTypes()) {
				pstmt.setString(position, wallet.getAddress(addressType));
				position++;
			}
			pstmt.addBatch();
		}
		pstmt.executeBatch();
	}

	/**
	 * Definitely terminates the underlying connection making it unrecoverable for
	 * further operations.
	 */
	@Override
	public void close() {
		recover = false;
		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
			}
		} catch (SQLException sqle) {
			// do nothing
		}
	}

	/**
	 * @return whether the close() on the underlying connection
	 * 		   has been called or not.
	 */
	public boolean isConnectionActive() {
		return recover;
	}

	/**
	 * @return whether the automatic saving of generated wallets is set
	 */
	public boolean isAutosaveGeneratedAllowed() {
		return isAutosaveGeneratedAllowed;
	}
}
