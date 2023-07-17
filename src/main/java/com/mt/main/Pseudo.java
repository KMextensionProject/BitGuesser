package com.mt.main;

import static java.lang.Runtime.getRuntime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mt.core.BitcoinWallet;
import com.mt.core.Wallet;

/**
 * 
 * Takto nejako by sa to mohlo pouzivat
 *
 */
public class Pseudo {

	private static final int MAX_BATCH_INSERT_SIZE = 2000;

	public static void main(String[] args) {
		getRuntime().addShutdownHook(new Thread(Pseudo::notifyOnShutdown));

		// Wallet list for batch check against database
		List<Wallet> wallets = new ArrayList<>(MAX_BATCH_INSERT_SIZE);

		// create wallets endlessly
		while (true) {

			while (wallets.size() != MAX_BATCH_INSERT_SIZE) {
				wallets.add(new BitcoinWallet());
			}

			// if we have enough for batch verification
			List<Wallet> walletsFoundInDb = getWalletsFoundInDB(wallets);

			// if we generated some existing wallets
			if (!walletsFoundInDb.isEmpty()) {
				insertPrivateKeysToDB(wallets);
				sendTelegramNotification();
			}

			wallets.clear();
		}

	}

	private static void notifyOnShutdown() {
		System.out.println("Telegram notification for unexpected application shutdown");
	}

	private static List<Wallet> getWalletsFoundInDB(List<Wallet> wallets) {
		// will check all types of addresses
		return Collections.emptyList();
	}

	private static void insertPrivateKeysToDB(List<Wallet> wallets) {
		wallets.forEach(e -> System.out.println(e.getPrivateKey()));
	}

	private static void sendTelegramNotification() {
		// without keys, so they don't go through the internet
		System.out.println("Sending telegram notification");
	}

}
