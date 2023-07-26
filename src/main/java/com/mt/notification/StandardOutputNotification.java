package com.mt.notification;

import java.io.IOException;

/**
 * Default Notification implementation.
 *
 * @author mkrajcovic
 *
 */
public class StandardOutputNotification implements Notification {

	/**
	 * Prints the message body to the standard output.
	 */
	@Override
	public void sendNotification(Message message, Recipient recipient) throws IOException {
		System.out.println("Console Notification: " + message.getSubject() + " - " + message.getBody()); // NOSONAR
	}

}
