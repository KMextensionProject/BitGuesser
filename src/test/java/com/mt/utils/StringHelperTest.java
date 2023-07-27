package com.mt.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static com.mt.utils.StringHelper.repeat;

class StringHelperTest {

	@Test
	void testStringRepetition() {
		assertEquals("aaa", repeat("a", 3, ""));
		assertEquals("aaa", repeat("a", 3, null));
		assertEquals(null, repeat(null, 3, null));
		assertThrows(IllegalArgumentException.class, () -> repeat("das", -5, null));
	}

}
