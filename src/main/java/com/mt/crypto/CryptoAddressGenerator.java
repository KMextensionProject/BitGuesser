package com.mt.crypto;

import java.security.GeneralSecurityException;

/**
 *
 * @author mkrajcovic
 */
public interface CryptoAddressGenerator extends AsymetricKeyGenerator {

	/**
	 *
	 * @param publicKey - which to compute crypto address from
	 * @return address as string
	 */
	public String getAddress(String publicKey) throws GeneralSecurityException;

}
