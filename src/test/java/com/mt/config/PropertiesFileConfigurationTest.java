package com.mt.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class PropertiesFileConfigurationTest {

	@Test
	void loadingValuesTest() {
		ApplicationConfiguration config = new PropertiesFileConfiguration("testConfiguration.properties");

		assertNotNull(config.get(ConfigurationKey.NOTIFICATION_RECIPIENT_EMAIL));
		// but this should be commented in the config file if not used at all
		assertEquals(true, config.get(ConfigurationKey.NOTIFICATION_RECIPIENT_EMAIL).isEmpty());
		assertEquals("mytelegrambotid:chatId", config.get(ConfigurationKey.NOTIFICATION_RECIPIENT_OTHER_CONTACT));

		assertNull(config.get(ConfigurationKey.DATABASE_TABLE_AUTOSAVE));
		assertEquals("false", config.get(ConfigurationKey.DATABASE_AUTOSAVE_GENERATED_KEYS, "true"));
		assertEquals("s_address", config.get(ConfigurationKey.DATABASE_TABLE_ADDRESS_FIELD));
		assertEquals("t_address", config.get(ConfigurationKey.DATABASE_TABLE_ADDRESS));
	}

}
