package com.mt.main;

import static java.lang.Runtime.getRuntime;
import static java.lang.System.getenv;

import java.io.IOException;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.mt.core.AddressType;
import com.mt.core.BitcoinWallet;
import com.mt.core.Wallet;
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

	private static void testBitWalletGeneration() throws Exception {
		Wallet wallet = new BitcoinWallet();

		System.out.println(wallet.getAddress()); // default legacy address
		System.out.println(wallet.getAddress(AddressType.P2PKH)); // default legacy address
//		System.out.println(wallet.getAddress(AddressType.Bech32)); // native segwit address
		System.out.println(wallet.getPublicKey());
		System.out.println(wallet.getPrivateKey());

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
}
