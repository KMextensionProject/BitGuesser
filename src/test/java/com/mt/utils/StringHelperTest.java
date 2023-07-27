package com.mt.utils;

import static com.mt.utils.StringHelper.keepFirst;
import static com.mt.utils.StringHelper.repeat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class StringHelperTest {

	@Test
	void testStringRepetition() {
		assertEquals("aaa", repeat("a", 3, ""));
		assertEquals("aaa", repeat("a", 3, null));
		assertEquals(null, repeat(null, 3, null));
		assertEquals("a,a", repeat("a", 2, ","));
		assertThrows(IllegalArgumentException.class, () -> repeat("das", -5, null));
	}

	@Test
	void leftTrimTest() {
		assertEquals(13, keepFirst(10, "123456789101112131415").length()); // "..."
		assertEquals("123", keepFirst(10, "123")); // ""
		assertNull(keepFirst(1, null));
		assertThrows(IllegalArgumentException.class, () -> keepFirst(-1, "123"));
	}

}
