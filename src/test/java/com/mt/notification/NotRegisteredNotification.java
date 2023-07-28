package com.mt.notification;

import java.io.IOException;

// no @Registered annotation here
public class NotRegisteredNotification implements Notification {

	@Override
	public void sendNotification(Message message, Recipient recipient) throws IOException {
		// intentionally empty
	}
}
