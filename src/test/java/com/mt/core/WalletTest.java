package com.mt.core;

import static com.mt.core.AddressType.BECH32;
import static com.mt.core.AddressType.P2PKH;
import static com.mt.core.AddressType.P2SH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
class WalletTest {

	private Wallet wallet;

	@BeforeAll
	void createWallet() {
		Security.addProvider(new BouncyCastleProvider());
		wallet = new BitcoinWallet();
	}

	@Test
	void creationTest() {
		assertNotNull(wallet, "wallet cannot be null");
		assertNotNull(wallet.getAddress(P2PKH), "legacy address cannot be null");
		assertNotNull(wallet.getAddress(P2SH), "SegWit address cannot be null");
		assertNotNull(wallet.getAddress(BECH32), "native SegWit address cannot be null");
		assertNotNull(wallet.getPrivateKey(), "private key cannot be null");
		assertNotNull(wallet.getPublicKey(), "public key cannot be null");
	}

	@Test
	void integrityTest() {
		int nativeSegWitAddressLength = wallet.getAddress(BECH32).length();
		int segWitAddressLength = wallet.getAddress(P2SH).length();
		int legacyAddressLength = wallet.getAddress().length();
		int privateLength = wallet.getPrivateKey().length();
		int publicLength = wallet.getPublicKey().length();

		assertTrue(nativeSegWitAddressLength == 42 || nativeSegWitAddressLength == 62);
		assertEquals(34, segWitAddressLength);
		assertTrue(legacyAddressLength >= 25 && legacyAddressLength <= 35);
		assertEquals(64, privateLength);
		assertTrue(publicLength >= 68 && publicLength <= 130);
	}

	@Test
	void walletActivenessTest() {
		assertTrue(isAddressValid(wallet.getAddress(P2PKH)));
		assertTrue(isAddressValid(wallet.getAddress(P2SH)));
		assertTrue(isAddressValid(wallet.getAddress(BECH32)));
	}

	private static boolean isAddressValid(String address) {
		boolean isValid;
		try {
			isValid = WalletValidator.isValid(address);
		} catch (IOException ioex) {
			System.out.println("invalidating test result: " + ioex.getMessage());
			isValid = false;
		}
		return isValid;
	}

	@AfterAll
	void logValues() {
		// neberem nic na lahku vahu :D
		System.out.println("[DEBUG] p2pkh address: " + wallet.getAddress());
		System.out.println("[DEBUG] p2sh address: " + wallet.getAddress(P2SH));
		System.out.println("[DEBUG] bech32 address: " + wallet.getAddress(BECH32));
		System.out.println("[DEBUG] public key: " + wallet.getPublicKey());
		System.out.println("[DEBUG] private key: " + wallet.getPrivateKey());
	}
}
