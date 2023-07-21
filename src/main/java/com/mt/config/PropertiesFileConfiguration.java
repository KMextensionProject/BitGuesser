package com.mt.config;

import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author mkrajcovic
 */
public class PropertiesFileConfiguration implements ApplicationConfiguration {

	private final Properties config;

	public PropertiesFileConfiguration(String path) {
        config = new Properties();
        try {
            config.load(this.getClass().getClassLoader().getResourceAsStream(path));
        } catch (IOException | NullPointerException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

	@Override
	public String get(ApplicationConfiguration.Key key) {
		return config.getProperty(key.toString());
	}

	@Override
	public String get(ApplicationConfiguration.Key key, String defaultValue) {
		return config.getProperty(key.toString(), defaultValue);
	}
}
