package com.mt.config;

import static java.nio.file.Files.newBufferedReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Configuration loader class for standard java properties file located anywhere
 * on the file system.
 *
 * @author mkrajcovic
 */
public class PropertiesFileConfiguration implements ApplicationConfiguration {

	private final Properties config;

	public PropertiesFileConfiguration(String path) {
        config = new Properties();
		try (BufferedReader fileReader = newBufferedReader(Paths.get(path).toAbsolutePath())) {
			config.load(fileReader);
            // null empty values example:
            // db.url=
            List<Object> toRemove = new ArrayList<>();
            for (Map.Entry<Object, Object> entry : config.entrySet()) {
            	if (String.valueOf(entry.getValue()).trim().isEmpty()) {
            		toRemove.add(entry.getKey());
            	}
            }
            toRemove.forEach(config::remove);
        } catch (NullPointerException e) {
            throw new ExceptionInInitializerError("File " + path + " could not be found");
        } catch (IOException ioex) {
        	throw new ExceptionInInitializerError(ioex);
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
