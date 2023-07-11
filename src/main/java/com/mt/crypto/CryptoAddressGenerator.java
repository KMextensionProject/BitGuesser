package com.mt.crypto;

/**
 *
 * @author mkrajcovic
 */
public interface CryptoAddressGenerator extends AsymetricKeyGenerator {

	/**
	 *
	 * @param publicKey
	 * @return
	 */
	public String getAddress(String publicKey);

}
