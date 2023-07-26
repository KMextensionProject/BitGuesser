package com.mt.config;

import static com.mt.config.ConfigurationKey.DATABASE_ENABLE_WALLET_SAVING;
import static com.mt.config.ConfigurationKey.DATABASE_TABLE_ADDRESS;
import static com.mt.config.ConfigurationKey.DATABASE_TABLE_ADDRESS_FIELD;
import static com.mt.config.ConfigurationKey.DATABASE_TABLE_SAVE_WALLET;
import static com.mt.config.ConfigurationKey.NOTIFICATION_RECIPIENT_EMAIL;
import static com.mt.config.ConfigurationKey.NOTIFICATION_RECIPIENT_OTHER_CONTACT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class PropertiesFileConfigurationTest {

	@Test
	void loadingValuesTest() {
		ApplicationConfiguration config = new PropertiesFileConfiguration("src/test/resources/testConfiguration.properties");

		assertNull(config.get(NOTIFICATION_RECIPIENT_EMAIL));
		assertEquals("mytelegrambotid:chatId", config.get(NOTIFICATION_RECIPIENT_OTHER_CONTACT));

		assertNull(config.get(DATABASE_TABLE_SAVE_WALLET));
		assertEquals("false", config.get(DATABASE_ENABLE_WALLET_SAVING, "true"));
		assertEquals("s_address", config.get(DATABASE_TABLE_ADDRESS_FIELD));
		assertEquals("t_address", config.get(DATABASE_TABLE_ADDRESS));
	}

}
