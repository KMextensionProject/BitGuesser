package com.mt.crypto;

import java.security.GeneralSecurityException;
import java.security.KeyPair;

/**
 *
 * @author mkrajcovic
 *
 */
public interface AsymetricKeyGenerator {

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	public KeyPair generateAsymetricKeyPair() throws GeneralSecurityException;

	/**
	 *
	 * @param keyPair
	 * @return
	 * @throws Exception
	 */
	public String extractPublicKey(KeyPair keyPair) throws GeneralSecurityException;

	/**
	 *
	 * @param keyPair
	 * @return
	 * @throws Exception
	 */
	public String extractPrivateKey(KeyPair keyPair) throws GeneralSecurityException;

}
