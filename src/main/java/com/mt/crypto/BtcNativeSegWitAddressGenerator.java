package com.mt.crypto;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/**
 * 
 * @author mkrajcovic
 */
public class BtcNativeSegWitAddressGenerator implements CryptoAddressGenerator {

	private static CryptoAddressGenerator legacyGenerator = BtcLegacyAddressGenerator.getInstance();

	private static final BtcNativeSegWitAddressGenerator INSTANCE = new BtcNativeSegWitAddressGenerator();

	private BtcNativeSegWitAddressGenerator() {
		// intentionally empty
	}

	/**
	 * @return the singleton instance of this class
	 */
	public static final BtcNativeSegWitAddressGenerator getInstance() {
		return INSTANCE;
	}

	@Override
	public KeyPair generateAsymetricKeyPair() throws GeneralSecurityException {
		return legacyGenerator.generateAsymetricKeyPair();
	}

	@Override
	public String getPublicKey(KeyPair keyPair) throws InvalidKeyException {
		return legacyGenerator.getPublicKey(keyPair);
	}

	@Override
	public String getPrivateKey(KeyPair keyPair) throws InvalidKeyException {
		return legacyGenerator.getPrivateKey(keyPair);
	}

	@Override
	public String getAddress(String publicKey) throws NoSuchProviderException, NoSuchAlgorithmException {
		throw new UnsupportedOperationException("not implemented");
	}
}
