package com.mt.main;

import java.security.KeyPair;

import com.mt.crypto.BitWalletGenerator;
import com.mt.crypto.CryptoAddressGenerator;

public class AppRunner {

	public static void main(String[] args) throws Exception {

		Runtime.getRuntime().addShutdownHook(new Thread(() -> System.out.println("notifikacia")));

		CryptoAddressGenerator btcGenerator = new BitWalletGenerator();
		KeyPair keyPair = btcGenerator.generateAsymetricKeyPair();
		String publicKey = btcGenerator.getPublicKey(keyPair);
		String privateKey = btcGenerator.getPrivateKey(keyPair);

		System.out.println("address: " + btcGenerator.getAddress(publicKey));
		System.out.println("public key: " + publicKey);
		System.out.println("private key: " + privateKey);
	}

}
