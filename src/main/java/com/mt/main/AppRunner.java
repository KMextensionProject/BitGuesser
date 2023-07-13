package com.mt.main;

import static java.lang.Runtime.getRuntime;
import static java.lang.System.getenv;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.mt.crypto.BitWalletGenerator;
import com.mt.crypto.CryptoAddressGenerator;
import com.mt.notification.Message;
import com.mt.notification.Notification;
import com.mt.notification.Recipient;
import com.mt.notification.TelegramNotification;

public class AppRunner {

	private static final String NOTIFICATION_ADDRESS = getenv("telegram_recipient");

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	public static void main(String[] args) throws Exception {
		getRuntime().addShutdownHook(new Thread(AppRunner::testTelegramNotification));
		testBitWalletGeneration();

		// validate with:
//		https://blockchain.info/balance?active=
	}

	private static void testBitWalletGeneration() throws GeneralSecurityException {
		CryptoAddressGenerator btcGen = new BitWalletGenerator();
		KeyPair keyPair = btcGen.generateAsymetricKeyPair();
		String publicKey = btcGen.getPublicKey(keyPair);
		String privateKey = btcGen.getPrivateKey(keyPair);

		System.out.println("address: " + btcGen.getAddress(publicKey));
//		System.out.println("public key: " + publicKey);
		System.out.println("private key: " + privateKey);
	}

	private static void testTelegramNotification() {
		Message message = new Message("BitGuesser", "This is a test notification");
		Recipient recipient = new Recipient().withOtherAddress(NOTIFICATION_ADDRESS);

		Notification telegram = new TelegramNotification();
		try {
			telegram.sendNotification(message, recipient);
			System.out.println("Telegram notification has been sent");
		} catch (IOException ioex) {
			ioex.printStackTrace();
		}
	}

//	[0, 46, 77, 17, -102, 63, 44, 38, 103, -23, 38, -126, -82, -109, 88, -108, 72, 7, 110, -127, -113]
//	[0, 46, 77, 17, -102, 63, 44, 38, 103, -23, 38, -126, -82, -109, 88, -108, 72, 7, 110, -127, -113]

//	[0, 64, 60, -27, 124, 24, -52, 62, 4, -79, -94, -124, -66, -29, 124, 4, 19, -8, 65, -124, -63, 0, 0, 0, 0]
//	[0, 64, 60, -27, 124, 24, -52, 62, 4, -79, -94, -124, -66, -29, 124, 4, 19, -8, 65, -124, -63, 0, 0, 0, 0]
//	[0, 64, 60, -27, 124, 24, -52, 62, 4, -79, -94, -124, -66, -29, 124, 4, 19, -8, 65, -124, -63, -51, -39, 78, -127]
//	[0, 64, 60, -27, 124, 24, -52, 62, 4, -79, -94, -124, -66, -29, 124, 4, 19, -8, 65, -124, -63, -51, -39, 78, -127]	
}
