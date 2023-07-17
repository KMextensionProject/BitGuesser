package com.mt.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class BtcNativeSegWitAddressGenerator extends BtcLegacyAddressGenerator {

	@Override
	public String getAddress(String publicKey) throws NoSuchProviderException, NoSuchAlgorithmException {
		throw new UnsupportedOperationException("not implemented");
	}

}
