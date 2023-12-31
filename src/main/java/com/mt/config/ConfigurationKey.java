package com.mt.config;

public enum ConfigurationKey implements ApplicationConfiguration.Key {

	DATABASE_URL("db.url"),
	DATABASE_USER("db.usr"),
	DATABASE_PASSWORD("db.pwd"),

	DATABASE_SCHEMA("db.schema"),
	DATABASE_TABLE_ADDRESS("db.table.address"),
	DATABASE_TABLE_ADDRESS_FIELD("db.table.address.addressField"),
	DATABASE_TABLE_ADDRESS_PRIVATE_KEY_FIELD("db.table.address.privateKeyField"),

	DATABASE_ENABLE_WALLET_SAVING("db.enableWalletSaving"),
	DATABASE_TABLE_SAVE_WALLET("db.table.save_wallet"),
	DATABASE_TABLE_SAVE_WALLET_ADDRESS_FIELD("db.table.save_wallet.addressField"),
	DATABASE_TABLE_SAVE_WALLET_ADDRESS_PRIVATE_KEY_FIELD("db.table.save_wallet.privateKeyField"),

	NOTIFICATION_RECIPIENT_EMAIL("notification.recipient.email"),
	NOTIFICATION_RECIPIENT_PHONE("notification.recipient.phone"),
	NOTIFICATION_RECIPIENT_OTHER_CONTACT("notification.recipient.otherContact");

	private final String name;

	private ConfigurationKey(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
