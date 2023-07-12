package com.mt.notification;

import java.io.IOException;

public interface Notification {

	/**
	 *
	 * @param message
	 * @param recipient
	 * @throws IOException
	 */
	public void sendNotification(Message message, Recipient recipient) throws IOException;

}
