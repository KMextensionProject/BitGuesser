package com.mt.core;

import static com.mt.config.ConfigurationKey.DATABASE_ENABLE_WALLET_SAVING;
import static com.mt.config.ConfigurationKey.DATABASE_PASSWORD;
import static com.mt.config.ConfigurationKey.DATABASE_SCHEMA;
import static com.mt.config.ConfigurationKey.DATABASE_TABLE_ADDRESS;
import static com.mt.config.ConfigurationKey.DATABASE_TABLE_ADDRESS_FIELD;
import static com.mt.config.ConfigurationKey.DATABASE_TABLE_ADDRESS_PRIVATE_KEY_FIELD;
import static com.mt.config.ConfigurationKey.DATABASE_TABLE_SAVE_WALLET;
import static com.mt.config.ConfigurationKey.DATABASE_TABLE_SAVE_WALLET_ADDRESS_FIELD;
import static com.mt.config.ConfigurationKey.DATABASE_TABLE_SAVE_WALLET_ADDRESS_PRIVATE_KEY_FIELD;
import static com.mt.config.ConfigurationKey.DATABASE_URL;
import static com.mt.config.ConfigurationKey.DATABASE_USER;
import static com.mt.utils.StringHelper.keepFirst;
import static com.mt.utils.StringHelper.repeat;
import static java.util.Objects.requireNonNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.mt.config.ApplicationConfiguration;
import com.mt.utils.WalkingDeadLogger;

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
public final class Database implements AutoCloseable {

	private static final Logger LOG = new WalkingDeadLogger(Database.class);

	// database connection settings
	private Connection connection;
	private boolean recover;
	private final String url;
	private final String usr;
	private final String pwd;

	// main lookup table
	private final String schema;
	private final String table;
	private final String addressField;
	private final String privateKeyField;

	// side table for saving generated (searched) wallets
	private final boolean isWalletSavingAllowed;
	private String walletSaveTable;
	private String walletSaveAddressField;
	private String walletSavePrivateKeyField;

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

		isWalletSavingAllowed = Boolean.valueOf(config.get(DATABASE_ENABLE_WALLET_SAVING));
		if (isWalletSavingAllowed) {
			walletSaveTable = config.get(DATABASE_TABLE_SAVE_WALLET, "t_generated_address");
			walletSaveAddressField = config.get(DATABASE_TABLE_SAVE_WALLET_ADDRESS_FIELD, addressField);
			walletSavePrivateKeyField = config.get(DATABASE_TABLE_SAVE_WALLET_ADDRESS_PRIVATE_KEY_FIELD, privateKeyField);
		}

		// initialize and fail immediately if we cannot connect
		getConnection();
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
	 *
	 * @param wallets - to find a match for
	 * @return list of wallets which the match was found for
	 */
	public List<String> findAddresses(List<String> searchedAddresses) {
		String query = createParameterizedQuery(searchedAddresses.size());
		LOG.fine(() -> keepFirst(100, query));

		try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
			return queryForAddresses(pstmt, searchedAddresses);
		} catch (SQLException | IllegalStateException error) {
			throw new ApplicationFailure("Error calling select: " + query + " with params " + searchedAddresses, error);
		}
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

	/**
	 * If the wallet saving is not configured, this method behaves as noop,
	 * otherwise it will save all the addresses and corresponding private keys
	 * to the configured side table which is supposed to reside within the same
	 * schema as the main lookup table used for searching.
	 *
	 * @param wallets
	 *            that should be saved to database
	 */
	public void saveWallets(List<Wallet> wallets) {
		if (!isWalletSavingAllowed) {
			return;
		}
		String insert = createParameterizedInsert(2);
		LOG.fine(() -> insert + " (" + wallets.size() + " wallets)");

		try (PreparedStatement pstmt = getConnection().prepareStatement(insert)) {
			for (Wallet wallet : wallets) {
				for (AddressType type : wallet.getSupportedAddressTypes()) {
					pstmt.setString(1, wallet.getAddress(type));
					pstmt.setString(2, wallet.getPrivateKey());
					pstmt.addBatch();
				}
			}
			pstmt.executeBatch();
		} catch (SQLException | IllegalStateException error) {
			throw new ApplicationFailure("Error calling insert: " + insert, error);
		}
	}

	private String createParameterizedInsert(int placeholders) {
		// stick to the currently used schema
		return "INSERT INTO " + schema + "." + walletSaveTable 
			+ "(" + walletSaveAddressField + "," + walletSavePrivateKeyField 
			+ ") VALUES (" + repeat("?", placeholders, ",") + ");";
	}

	/**
	 * Performs insert of private keys to corresponding addresses stored already in
	 * the database.
	 *
	 * @param wallets - of which the private keys should be inserted
	 */
	public void savePrivateKeys(List<Wallet> wallets) {
		String update = createParameterizedUpdate(wallets.get(0).getSupportedAddressTypes().size());
		LOG.fine(update);

		try (PreparedStatement pstmt = getConnection().prepareStatement(update)) {
			insertPrivateKeysForAddresses(pstmt, wallets);
		} catch (SQLException | IllegalStateException error) {
			throw new ApplicationFailure("Error calling update: " + update + " for wallets: " + wallets, error);
		}
	}

	private String createParameterizedUpdate(int placeholders) {
		return "UPDATE " + schema + "." + table 
			+ " SET " + privateKeyField + " = ? WHERE " 
			+ addressField + " IN (" 
			+ repeat("?", placeholders, ",") + ");";
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
	 * @return the total count of generated addresses if the wallet saving is
	 *         enabled, otherwise -1 is returned.
	 */
	public long countGeneratedAddresses() {
		if (!isWalletSavingAllowed) {
			return -1L;
		}
		String countStmt = "SELECT COUNT(*) FROM " + schema + "." + walletSaveTable;
		LOG.fine(countStmt);

		try (PreparedStatement pstmt = getConnection().prepareStatement(countStmt)) {
			return pstmt.executeQuery().getLong(1);
		} catch (SQLException sqlex) {
			throw new ApplicationFailure("Error calling: " + countStmt, sqlex);
		}
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
			LOG.warning("Error closing database connection: " + sqle);
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
		return isWalletSavingAllowed;
	}
}
