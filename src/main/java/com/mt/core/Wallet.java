package com.mt.core;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;

import com.mt.crypto.BtcAddressGenerator;
import com.mt.crypto.CryptoAddressGenerator;

/**
 * This class represents a simple holder which provides convenient way to
 * generate and access Wallet information without forcing a client to use
 * generators by hand or handle checked exceptions.<br>
 * <p>
 * This class works as a factory for various types of wallets based on the
 * underlying type of the CryptoAddressGenerator.
 *
 * @author mkrajcovic
 */
public final class Wallet {

	private String privateKey;
	private String publicKey;
	private String address;

	/**
	 * The generation of an invalid key is handled by iterative recreation
	 * of the wallet.
	 *
	 * @return a valid Bitcoin wallet
	 */
	public static Wallet createBitcoinWallet() {
		return new Wallet(BtcAddressGenerator.getInstance());
	}

	private Wallet(CryptoAddressGenerator cryptoWalletGenerator) {
		handleWalletCreation(cryptoWalletGenerator);
	}

	private void handleWalletCreation(CryptoAddressGenerator walletGenerator) {
		boolean createNew = true;
		while (createNew)
			try {
				KeyPair keyPair = walletGenerator.generateAsymetricKeyPair();
				this.privateKey = walletGenerator.getPrivateKey(keyPair);
				this.publicKey = walletGenerator.getPublicKey(keyPair);
				this.address = walletGenerator.getAddress(publicKey);
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

	@Override
	public String toString() {
		return "Wallet [address=" + address 
			+ ", privateKey=" + privateKey + "]";
	}

}
