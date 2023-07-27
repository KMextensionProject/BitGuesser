package com.mt.core;

import static com.mt.core.AddressType.BECH32;
import static com.mt.core.AddressType.P2PKH;
import static java.util.Collections.unmodifiableSet;

import java.security.GeneralSecurityException;
import java.util.EnumSet;
import java.util.Set;

public class BitcoinWallet extends Wallet {

	private static final Set<AddressType> SUPPORTED_ADDRESS_TYPES = unmodifiableSet(EnumSet.of(P2PKH, /*P2SH,*/ BECH32));

	private String bech32Address;

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
		case BECH32:
			try {
				if (bech32Address == null) {
					bech32Address = addressType.getGenerator().getAddress(publicKey);
				}
				return bech32Address;
			} catch (GeneralSecurityException gex) {
				throw new ApplicationFailure("Error during address creation: " + gex);
			}
		default:
			throw new IllegalArgumentException("Unsupported address type: " + addressType);
		}
	}

	@Override
	public Set<AddressType> getSupportedAddressTypes() {
		return SUPPORTED_ADDRESS_TYPES;
	}

	@Override
	public boolean equals(Object other) {
		if (!super.equals(other)) {
			return false;
		}
		if (other instanceof BitcoinWallet) {
			return bech32Address.equals(((BitcoinWallet) other).getAddress(BECH32));
		}
		return false;
	}

	@Override
	public String toString() {
		return "BitcoinWallet [bech32Address=" + bech32Address 
			+ ", privateKey=" + privateKey
//			+ ", publicKey=" + publicKey
			+ ", address=" + address + "]";
	}

}
