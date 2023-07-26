package com.mt.main;

import static com.mt.config.ConfigurationKey.NOTIFICATION_RECIPIENT_EMAIL;
import static com.mt.config.ConfigurationKey.NOTIFICATION_RECIPIENT_OTHER_CONTACT;
import static com.mt.config.ConfigurationKey.NOTIFICATION_RECIPIENT_PHONE;
import static java.lang.Runtime.getRuntime;
import static java.util.Objects.isNull;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.Executors.newFixedThreadPool;

import java.io.IOException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
 * This class represents the main operational interface of this application.<br>
 * It provides an easy way to generate amounts of Bitcoin wallets and perform
 * their processing via single method that composes the necessary operations in
 * accordance with the business case of this application. Internal units of the
 * processing are configured by the
 * {@link com.mt.config.ApplicationConfiguration} object.
 *
 * <p>
 * By itself, this class manages all the necessary resources needed during the
 * whole life of the application. These resources are released automatically
 * before termination as part of the JVM shutdown hook.
 * </p>
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
		try {
			if (!isNull(taskProcessor)) {
				taskProcessor.shutdown(); // do not accept any future tasks
				while (true) {
					if (taskProcessor.awaitTermination(5, TimeUnit.SECONDS)) {
						System.out.println("All running tasks have been executed successfully");
						break;
					}
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
	 * Creates the required amount of Bitcoin wallets.<br>
	 * <b><i>Note:</i></b> The generation is CPU intensive
	 * and time complexity is O(n).
	 *
	 * @param howMany - Bitcoin wallets to generate.
	 * @return list of generated Bitcoin wallets
	 */
	public List<Wallet> generateWallets(int howMany) {
		final List<Wallet> wallets = new ArrayList<>(howMany);
		while (wallets.size() != howMany) {
			wallets.add(new BitcoinWallet());
		}
		return wallets;
	}

	/**
	 * Delegates the synchronous version of wallet processing to the underlying
	 * single-threaded task executor, so the {@link #generateWallets(int)} can
	 * be used to generate continuously more wallets without blocking while
	 * waiting on database operations to complete. <br>
	 * See {@link #processWallets(List)} for more behavior details.
	 *
	 * @param wallets - to process
	 */
	public CompletableFuture<Void> processWalletsAsync(List<Wallet> wallets) {
		if (taskProcessor == null) {
			taskProcessor = newFixedThreadPool(1);
		}
		return runAsync(() -> processWallets(wallets), taskProcessor);
	}

	/**
	 * Queries the database for a match on provided wallet addresses. If there
	 * is a match, then the private keys will be saved to corresponding
	 * addresses along with notifying the client to all defined
	 * {@link com.mt.notification.Notification} implementations.
	 * <p>
	 * All the wallets passed into this method will be saved to database if
	 * configured via {@code db.table.save_wallet} properties.
	 * </p>
	 *
	 * @param wallets - to find a match for
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
	}
}
