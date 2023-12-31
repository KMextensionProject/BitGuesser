package com.mt.notification;

import static java.lang.ClassLoader.getSystemClassLoader;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

import com.mt.core.ApplicationFailure;

/**
 * 
 * @author mkrajcovic
 */
public class NotificationLoader {

	private static final Logger LOG = Logger.getLogger(NotificationLoader.class.getName());

	private NotificationLoader() {
		throw new IllegalStateException("NotificationLoader was not designed to be instantiated");
	}

	/**
	 * @return
	 */
	public static List<Notification> loadRegisteredNotifications() {
		return loadRegisteredNotifications("com.mt.notification");
	}

	/**
	 *
	 * @param packageName
	 * @return
	 */
	public static List<Notification> loadRegisteredNotifications(String packageName) {
		requireNonNull(packageName, "Package name cannot be null");

		List<String> classNames = loadClassNames(packageName);
		Set<Class<?>> registeredClasses = findRegisteredNotificationImplementors(packageName, classNames);
		LOG.info(() -> "Found active notifications for: " + registeredClasses.stream().map(Class::getSimpleName).collect(toList()));

		List<Notification> notifications = registeredClasses.stream()
			.map(c -> newInstance(c))
			.collect(toList());

		if (notifications.isEmpty()) {
			LOG.info("Registering default notifications for standard output");
			notifications.add(newInstance(StandardOutputNotification.class));
		}

		return notifications;
	}

	// TODO: test / optionally refactor this to support packages from any dependent or wrapper projects
	private static List<String> loadClassNames(String packageName) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(loadPackageResource(packageName)))) {
			return reader.lines()
				.filter(line -> line.endsWith(".class"))
				.collect(toList());
		} catch (IOException ioex) {
			throw new ApplicationFailure("Unable to read class files for notifications");
		}
	}

	private static InputStream loadPackageResource(String packageName) {
		InputStream resourceStream = getSystemClassLoader().getResourceAsStream(packageName.replace('.', '/'));
		// if not null it will be closed automatically by the BufferedReader wrapper
		requireNonNull(resourceStream, "No such package - " + packageName);
		return resourceStream;
	}

	private static Set<Class<?>> findRegisteredNotificationImplementors(String packageName, List<String> classNames) {
		return classNames.stream()
			.map(cn -> getClass(cn, packageName))
			.filter(Objects::nonNull)
			.filter(c -> c.isAnnotationPresent(Registered.class))
			.filter(c -> isNotificationSubclass(c))
			.collect(toSet());
	}

	private static Class<?> getClass(String className, String packageName) {
		try {
			return Class.forName(packageName + "." + className.substring(0, className.lastIndexOf('.')));
		} catch (ClassNotFoundException e) {
			// fall to returning null
		}
		return null;
	}

	private static boolean isNotificationSubclass(Class<?> classObj) {
		Class<?>[] interfaces = classObj.getInterfaces();
		if (interfaces.length == 0) {
			return false;
		}
		return stream(interfaces).anyMatch(e -> Notification.class.equals(e));
	}

	private static Notification newInstance(Class<?> classObj) {
		try {
			return (Notification) classObj.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			return null;
		}
	}

}
