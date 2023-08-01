package com.mt.utils;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * This is a console logger meant for use within application shutdown hooks.<br>
 * It survives the LogManager's global shutdown hook meant to destroy all logger
 * instances.
 *
 * @author mkrajcovic
 */
public class WalkingDeadLogger extends Logger {

	/**
	 * Creates a new WalkingDead logger with no demands for existing loggers
	 * created for the same class.
	 */
	public WalkingDeadLogger(Class<?> forClass) {
		super(forClass.getName(), null);
		addHandler(new WalkerConsoleHandler());
	}

	@Override
	public void removeHandler(Handler handler) {
		// just walk
	}

	static class WalkerConsoleHandler extends ConsoleHandler {
		@Override
		public void close() {
			// just walk, baby
		}
	}
}
