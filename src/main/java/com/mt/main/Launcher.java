package com.mt.main;

import java.util.List;

import com.mt.config.ApplicationConfiguration;
import com.mt.config.PropertiesFileConfiguration;
import com.mt.core.Wallet;

public class Launcher {

	public static void main(String[] args) {

		// if the predefined logic wrapped into this classe is not sufficient enough,
		// one can easily use Wallet, Database and Notification classes to implemnt
		// custom processing flow and logic
		final BitGuesserService service = new BitGuesserService(loadConfig(args));

		while (true) { // NO-SONAR this infinity loop is intented
			List<Wallet> wallets = service.generateWallets(1000);
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
