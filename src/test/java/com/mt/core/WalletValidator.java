package com.mt.core;

import static java.util.Objects.isNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.ServerException;

/**
 *
 * @author mkrajcovic
 */
class WalletValidator {

	private static final String BLOCKCHAIN_WALLET_INFO_URL = "https://blockchain.info/balance?active=";

	public static boolean isValid(Wallet wallet) throws IOException {
		String url = BLOCKCHAIN_WALLET_INFO_URL + wallet.getAddress();
		HttpURLConnection httpCon = null;
		try {
			httpCon = (HttpURLConnection) new URL(url).openConnection();
			int responseCode = httpCon.getResponseCode();
			// either 200 for valid address
			// or 400 for error_address
			if (responseCode == 200) {
				return true;
			} else if (responseCode >= 500) {
				throw new ServerException("Server returned status code: " + responseCode + " for requested URL: " + url);
			}
		} finally {
			if (!isNull(httpCon)) {
//				 if not disconnected, it may be reused
				httpCon.disconnect();
			}
		}
		return false;
	}
}
