package com.mt.notification;

import java.io.IOException;

@Registered
public class TestNotification implements Notification {

	@Override
	public void sendNotification(Message message, Recipient recipient) throws IOException {
		// intentionally empty
	}

}
