package com.mt.main;

import java.security.Security;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.mt.config.ApplicationConfiguration;
import com.mt.config.PropertiesFileConfiguration;
import com.mt.core.BitcoinWallet;
import com.mt.core.Database;
import com.mt.core.Wallet;

public class Launcher {

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	public static void main(String[] args) {
		ApplicationConfiguration config = loadConfig(args);

		try (Database db = new Database(config)) {

			List<Wallet> wallets = new ArrayList<>(10);
			while (wallets.size() != 10) {
				wallets.add(new BitcoinWallet());
			}

			List<Wallet> foundWallets = db.findAddresses(wallets);
			wallets.forEach(System.out::println);
			if (!foundWallets.isEmpty()) {
				db.savePrivateKeys(foundWallets);
			}
		}
	}

	private static ApplicationConfiguration loadConfig(String[] programArgs) {
		String path = "src/main/resources/configuration.properties";
		if (programArgs.length != 0) {
			path = programArgs[0];
		}
		return new PropertiesFileConfiguration(path);
	}
}
