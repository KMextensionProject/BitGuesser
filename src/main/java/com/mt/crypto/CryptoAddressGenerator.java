package com.mt.crypto;

import java.security.GeneralSecurityException;

/**
 *
 * @author mkrajcovic
 */
public interface CryptoAddressGenerator extends AsymetricKeyGenerator {

	/**
	 *
	 * @param publicKey - which to compute crypto wallet address from
	 * @return wallet address
	 */
	public String getAddress(String publicKey) throws GeneralSecurityException;

}
