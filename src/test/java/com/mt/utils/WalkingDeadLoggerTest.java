package com.mt.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

public class WalkingDeadLoggerTest {

	@Test
	void stayAliveTest() {
		Logger walkingDeadLogger = new WalkingDeadLogger(WalkingDeadLogger.class);
		Handler handler = new WalkingDeadLogger.WalkerConsoleHandler();

		// this is done by the LogManager's registered shutdown hook
		walkingDeadLogger.removeHandler(handler);
		handler.close();
		// If the new level is null, it means that this node should
		// inherit its level from its nearest ancestor with a specific
		// (non-null) level value.
		walkingDeadLogger.setLevel(null);

		int handlers = walkingDeadLogger.getHandlers().length;
		assertEquals(1, handlers, "Should still contain one handler after removal");

		// whether this handler will log this message or not
		assertTrue(handler.isLoggable(new LogRecord(Level.INFO, "message")));
	}

}
