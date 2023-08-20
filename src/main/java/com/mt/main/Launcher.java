package com.mt.main;

import java.util.List;

import com.mt.config.ApplicationConfiguration;
import com.mt.config.PropertiesFileConfiguration;
import com.mt.core.Wallet;

public class Launcher {

	static {
		System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] %4$s [%2$s] - %5$s%n");
	}

	public static void main(String[] args) {

		final WalletService service = new WalletService(loadConfig(args));
		List<Wallet> wallets;

		while (true) { // NOSONAR this infinity loop is intented
			wallets = service.generateWallets(1000);
			service.processWalletsAsync(wallets);
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
