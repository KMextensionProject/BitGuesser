package com.mt.core;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.util.List;

import com.mt.crypto.CryptoAddressGenerator;

/**
 * This class represents a simple holder which provides convenient way to
 * generate and access Wallet information without forcing a client to use
 * generators by hand or handle checked exceptions.
 *
 * @author mkrajcovic
 */
public abstract class Wallet {

	protected String privateKey;
	protected String publicKey;
	protected String address;

	/**
	 * The generation of an invalid key is handled by iterative recreation
	 * of the wallet.
	 *
	 * @return a valid Bitcoin legacy wallet (from before Segwit update)
	 */
	protected Wallet(CryptoAddressGenerator cryptoWalletGenerator) {
		handleWalletCreation(cryptoWalletGenerator);
	}

	private void handleWalletCreation(CryptoAddressGenerator walletGenerator) {
		boolean createNew = true;
		while (createNew)
			try {
				KeyPair keyPair = walletGenerator.generateAsymetricKeyPair();
				this.privateKey = walletGenerator.getPrivateKey(keyPair);
				this.publicKey = walletGenerator.getPublicKey(keyPair);
				this.address = walletGenerator.getAddress(publicKey); // bech32
				createNew = false;
			} catch (InvalidKeyException invalidKeyError) {
				// recreate on invalid key generation
			} catch (GeneralSecurityException generalError) {
				throw new RuntimeException(generalError);
			}
	}

	public String getPrivateKey() {
		return this.privateKey;
	}

	public String getPublicKey() {
		return this.publicKey;
	}

	public String getAddress() {
		return this.address;
	}

	/**
	 * @param addressType
	 *            - one of supported address types
	 * @return the address computed by the underlying address generator of the
	 *         given type
	 */
	public abstract String getAddress(AddressType addressType);

	/**
	 * @return the list of supported address types
	 */
	public abstract List<AddressType> getSupportedAddressTypes();

	@Override
	public String toString() {
		return "Wallet [address=" + address 
			+ ", privateKey=" + privateKey + "]";
	}

}
