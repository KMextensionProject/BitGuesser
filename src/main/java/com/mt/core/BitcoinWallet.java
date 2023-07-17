package com.mt.core;

import static com.mt.core.AddressType.Bech32;
import static com.mt.core.AddressType.P2PKH;

import java.security.GeneralSecurityException;
import static java.util.Arrays.asList;
import java.util.List;

public class BitcoinWallet extends Wallet {

	/**
	 * Creates new Bitcoin Wallet with P2PKH (Legacy) address.
	 */
	public BitcoinWallet() {
		super(P2PKH.getGenerator());
	}

	@Override
	public String getAddress(AddressType addressType) {
		switch(addressType) {
		case P2PKH:
			return address;
//		case P2SH:
		case Bech32:
			try {
				return addressType.getGenerator().getAddress(publicKey);
			} catch (GeneralSecurityException gex) {
				throw new RuntimeException(gex);
			}
		default:
			throw new IllegalArgumentException("Unsupported address type: " + addressType);
		}
	}

	@Override
	public List<AddressType> getSupportedAddressTypes() {
		return asList(
			P2PKH,
//			P2SH,
			Bech32);
	}

}
