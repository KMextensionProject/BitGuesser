package com.mt.notification;

import static com.mt.notification.NotificationLoader.loadRegisteredNotifications;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

public class NotificationLoaderTest {

	@Test
	public void findRegisteredImplementators() {
		List<Notification> notifications = loadRegisteredNotifications("com.mt.notification");

		/*
		 * TestNotification is a Registered Notification implementor while the
		 * NotRegisteredNotification is missing registration annotation
		 */
		assertEquals(1, notifications.size());
		assertEquals(TestNotification.class, notifications.get(0).getClass());

		// provide invalid packages
		assertThrows(NullPointerException.class, () -> loadRegisteredNotifications(null));
		assertThrows(NullPointerException.class, () -> loadRegisteredNotifications("unknown"));

		/*
		 * Let's use a valid package with no Notification class, which should
		 * result in loading the default StandardOutputNotification
		 */
		notifications = loadRegisteredNotifications("com.mt.config");
		assertEquals(1, notifications.size());
		assertEquals(StandardOutputNotification.class, notifications.get(0).getClass());
	}

}
