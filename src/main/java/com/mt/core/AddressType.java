package com.mt.core;

import com.mt.crypto.BtcLegacyAddressGenerator;
import com.mt.crypto.BtcNativeSegWitAddressGenerator;
import com.mt.crypto.CryptoAddressGenerator;

public enum AddressType {

	P2PKH("Bitcoin Legacy", new BtcLegacyAddressGenerator()),
//	P2SH("Bitcoin SegWit", new BtcSegWitAddressGenerator()),
	Bech32("Bitcoin native SegWit", new BtcNativeSegWitAddressGenerator());

	private String commonName;
	private CryptoAddressGenerator generator;

	private AddressType(String commonName, CryptoAddressGenerator generator) {
		this.commonName = commonName;
		this.generator = generator;
	}

	public CryptoAddressGenerator getGenerator() {
		return this.generator;
	}
	
	public String getCommonName() {
		return this.commonName;
	}
}
