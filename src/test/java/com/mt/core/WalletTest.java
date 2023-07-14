package com.mt.core;

import java.io.IOException;
import java.security.Security;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class WalletTest {

	private Wallet wallet;

	@BeforeAll
	void createWallet() {
		Security.addProvider(new BouncyCastleProvider());
		wallet = Wallet.createBitcoinLegacyWallet();
	}

	@Test
	void creationTest() {
		assertNotNull(wallet, "wallet cannot be null");
		assertNotNull(wallet.getAddress(), "address cannot be null");
		assertNotNull(wallet.getPrivateKey(), "private key cannot be null");
		assertNotNull(wallet.getPublicKey(), "public key cannot be null");
	}

	@Test
	void integrityTest() {
		int addressLength = wallet.getAddress().length();
		int privateLength = wallet.getPrivateKey().length();
		int publicLength = wallet.getPublicKey().length();

		assertTrue(addressLength >= 25 && addressLength <= 35);
		assertTrue(privateLength == 64);
		assertTrue(publicLength >= 68 && publicLength <= 130);
	}

	@Test
	void walletActivenessTest() {
		boolean isValid;
		try {
			isValid = WalletValidator.isValid(wallet);
		} catch (IOException ioex) {
			isValid = false;
		}
		assertTrue(isValid);
	}

	@AfterAll
	void logValues() {
		// neberem nic na lahku vahu :D
		System.out.println("tested address: " + wallet.getAddress());
		System.out.println("tested private key: " + wallet.getPrivateKey());
	}
}
