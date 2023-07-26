package com.mt.main;

import static com.mt.config.ConfigurationKey.NOTIFICATION_RECIPIENT_EMAIL;
import static com.mt.config.ConfigurationKey.NOTIFICATION_RECIPIENT_OTHER_CONTACT;
import static com.mt.config.ConfigurationKey.NOTIFICATION_RECIPIENT_PHONE;
import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.Executors.newFixedThreadPool;

import java.io.IOException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.mt.config.ApplicationConfiguration;
import com.mt.core.BitcoinWallet;
import com.mt.core.Database;
import com.mt.core.Wallet;
import com.mt.notification.Message;
import com.mt.notification.Notification;
import com.mt.notification.Recipient;

/**
 *
 * @author mkrajcovic
 */
public class BitGuesserService {

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	private final Database db;
	private ExecutorService taskProcessor; // prefer lazy init
	private List<Notification> notifications;
	private Recipient recipient;

	public BitGuesserService(ApplicationConfiguration config) {
		db = new Database(config);
		recipient = buildRecipient(config);
		notifications = loadNotifications();
		registerShutdownHook();
	}

	private Recipient buildRecipient(ApplicationConfiguration config) {
		return new Recipient()
			.withEmail(config.get(NOTIFICATION_RECIPIENT_EMAIL))
			.withPhoneNumber(config.get(NOTIFICATION_RECIPIENT_PHONE))
			.withOtherAddress(config.get(NOTIFICATION_RECIPIENT_OTHER_CONTACT));
	}

	private List<Notification> loadNotifications() {
		return Collections.emptyList();
	}

	private void registerShutdownHook() {
		getRuntime().addShutdownHook(new Thread(this::releaseResources));
	}

	private void releaseResources() {
		System.out.println("Shutting down the task executor");
		taskProcessor.shutdown(); // do not accept any future tasks
		try {
			while (true) {
				if (taskProcessor.awaitTermination(5, TimeUnit.SECONDS)) {
					System.out.println("All running tasks have been executed successfully");
					break;
				}
			}
			System.out.println("Closing database connection");
			db.close();
		} catch (InterruptedException iex) {
			iex.printStackTrace();
		} finally {
			sendNotification(new Message("BitGuesser has been terminated"));
		}
	}

	/**
	 *
	 * @param howMany
	 * @return
	 */
	public List<Wallet> generateWallets(int howMany) {
		final List<Wallet> wallets = new ArrayList<>(howMany);
		while (wallets.size() != howMany) {
			wallets.add(new BitcoinWallet());
		}
		return wallets;
	}

	/**
	 *
	 * @param wallets
	 */
	public void processWalletsAsync(List<Wallet> wallets) {
		if (taskProcessor == null) {
			taskProcessor = newFixedThreadPool(1);
		}
		runAsync(() -> processWallets(wallets), taskProcessor);
	}

	/**
	 *
	 * @param wallets
	 */
	public void processWallets(List<Wallet> wallets) {
		db.saveWallets(wallets);

		List<Wallet> foundWallets = db.findAddresses(wallets);
		if (!foundWallets.isEmpty()) {
			db.savePrivateKeys(foundWallets);
			sendNotification(buildFoundNotificationMessage(foundWallets));
		}
	}

	private Message buildFoundNotificationMessage(List<Wallet> foundWallets) {
		// TODO: create proper message body
		return new Message("BitGuesser - new wallets have been found", foundWallets.toString());
	}

	private void sendNotification(Message message) {
		if (recipient.isDefined() && !notifications.isEmpty()) {
			try {
				for (Notification notification : notifications) {
					notification.sendNotification(message, recipient);
				}
			} catch (IOException ioex) {
				// do not exit the program by throwing an error
				ioex.printStackTrace();
			}
		}
		System.out.println("notifications have been sent");
	}
}
