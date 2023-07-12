package com.mt.main;

import static java.lang.Runtime.getRuntime;
import static java.lang.System.getenv;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;

import com.mt.crypto.BitWalletGenerator;
import com.mt.crypto.CryptoAddressGenerator;
import com.mt.notification.Message;
import com.mt.notification.Notification;
import com.mt.notification.Recipient;
import com.mt.notification.TelegramNotification;

public class AppRunner {

	public static void main(String[] args) throws Exception {
		getRuntime().addShutdownHook(new Thread(AppRunner::notifyOnShutdown));

		testBitGeneration();
		testTelegramNotification();
	}

	private static void testBitGeneration() throws GeneralSecurityException {
		CryptoAddressGenerator btcGen = new BitWalletGenerator();
		KeyPair keyPair = btcGen.generateAsymetricKeyPair();
		String publicKey = btcGen.getPublicKey(keyPair);
		String privateKey = btcGen.getPrivateKey(keyPair);

		System.out.println("address: " + btcGen.getAddress(publicKey));
//		System.out.println("public key: " + publicKey);
		System.out.println("private key: " + privateKey);
	}

	private static void testTelegramNotification() throws IOException {
		Message message = new Message("BitGuesser", "This is a test notification");
		Recipient recipient = new Recipient().withOtherAddress(getenv(("telegram_recipient")));

		Notification telegram = new TelegramNotification();
		telegram.sendNotification(message, recipient);
	}

	private static final void notifyOnShutdown() {
		System.out.println("Notification before shutdown");
	}

}
