package com.mt.notification;

import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
		Set<Class<?>> registeredClasses = findRegisteredImplementors(packageName, classNames);

		LOG.info(() -> "Found active notifications for: "
			+ registeredClasses.stream()
							   .map(Class::getSimpleName)
							   .collect(toList()));

		List<Notification> notifications = instantiate(registeredClasses);
		if (notifications.isEmpty()) {
			LOG.info("Registering default notifications for standard output");
			notifications.add(new StandardOutputNotification());
		}
		return notifications;
	}

	private static List<String> loadClassNames(String packageName) {
		String packagePath = packageName.replace('.', '/');
		String filePath = locatePackageFullPath(packagePath);
		if (filePath.contains("!")) {
			return readClassNamesFromJarFile(filePath, packagePath);
		}
		return readClassNamesFromClassPath(filePath);
	}

	private static String locatePackageFullPath(String packagePath) {
		URL resourceUrl = NotificationLoader.class
				.getClassLoader()
				.getResource(packagePath);

		if (nonNull(resourceUrl)) {
			return resourceUrl.getFile();
		}
		throw new ApplicationFailure("Missing resource for " + packagePath);
	}

	private static List<String> readClassNamesFromJarFile(String file, String packagePath) {
		List<String> classNames = new ArrayList<>();
		String jarLocation = file.split("!")[0].substring(5); // URL = file:
		try (ZipFile jarFile = new ZipFile(jarLocation)) {
			Enumeration<? extends ZipEntry> jarEntries = jarFile.entries();
			while (jarEntries.hasMoreElements()) {
				ZipEntry jarEntry = jarEntries.nextElement();
				String entryName = jarEntry.getName();
				if (entryName.startsWith(packagePath) && entryName.endsWith(".class")) {
					String classFileName = entryName.substring(entryName.lastIndexOf('/') + 1); // safe
					classNames.add(classFileName);
				}
			}
		} catch (IOException ioex) {
			throw new ApplicationFailure("Unable to read class files for notifications");
		}
		return classNames;
	}

	private static List<String> readClassNamesFromClassPath(String file) {
		return Arrays.stream(new File(file).list())
			.filter(line -> line.endsWith(".class"))
			.collect(toList());
	}

	private static Set<Class<?>> findRegisteredImplementors(String packageName, List<String> classNames) {
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
		}//open cv
		return stream(interfaces).anyMatch(Notification.class::equals);
	}

	private static List<Notification> instantiate(Collection<Class<?>> registeredClasses) {
		return registeredClasses.stream()
			.map(c -> newInstance(c))
			.collect(toList());
	}

	private static Notification newInstance(Class<?> classObj) {
		try {
			return (Notification) classObj.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			return null;
		}
	}
}
