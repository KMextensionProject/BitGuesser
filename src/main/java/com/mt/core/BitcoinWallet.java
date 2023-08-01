package com.mt.core;

import static com.mt.core.AddressType.BECH32;
import static com.mt.core.AddressType.P2PKH;
import static com.mt.core.AddressType.P2SH;
import static java.util.Collections.unmodifiableSet;

import java.security.GeneralSecurityException;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public class BitcoinWallet extends Wallet {

	private static final Set<AddressType> SUPPORTED_ADDRESS_TYPES = unmodifiableSet(EnumSet.of(P2PKH, P2SH, BECH32));

	private String p2shAddress;
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
		case P2SH:
			if (p2shAddress == null) {
				p2shAddress = initAddress(P2SH);
			}
			return p2shAddress;
		case BECH32:
			if (bech32Address == null) {
				bech32Address = initAddress(BECH32);
			}
			return bech32Address;
		default:
			throw new IllegalArgumentException("Unsupported address type: " + addressType);
		}
	}

	private String initAddress(AddressType addressType) {
		try {
			return addressType.getGenerator().getAddress(publicKey);
		} catch (GeneralSecurityException gex) {
			throw new ApplicationFailure("Error during " + addressType + " address creation: " + gex);
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
			BitcoinWallet wallet = (BitcoinWallet) other;
			return Objects.equals(bech32Address, wallet.getAddress(BECH32))
				&& Objects.equals(p2shAddress, wallet.getAddress(P2SH));
		}
		return false;
	}

	@Override
	public String toString() {
		return "BitcoinWallet [bech32Address=" + getAddress(BECH32)
			+ ", p2shAddress=" + getAddress(P2SH)
			+ ", p2pkhAddress=" + getAddress(P2PKH)
			+ ", privateKey=" + privateKey
			+ ", publicKey=" + publicKey + "]";
	}

}
