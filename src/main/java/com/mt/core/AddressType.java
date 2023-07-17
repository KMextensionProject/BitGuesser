package com.mt.core;

import com.mt.crypto.BtcLegacyAddressGenerator;
import com.mt.crypto.BtcNativeSegWitAddressGenerator;
import com.mt.crypto.CryptoAddressGenerator;

public enum AddressType {

	/**
	 * Describes the Bitcoin Legacy address format starting with '1'
	 */
	P2PKH(new BtcLegacyAddressGenerator()),
	
	/**
	 * Describes the Bitcoin SegWit address format starting with '3'
	 */
//	P2SH(new BtcSegWitAddressGenerator()),

	/**
	 * Describes the Bitcoin Native SegWit address format starting with 'bc1'
	 */
	Bech32(new BtcNativeSegWitAddressGenerator());

	private CryptoAddressGenerator generator;

	private AddressType(CryptoAddressGenerator generator) {
		this.generator = generator;
	}

	public CryptoAddressGenerator getGenerator() {
		return this.generator;
	}
}
