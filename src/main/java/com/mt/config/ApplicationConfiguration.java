package com.mt.config;

public interface ApplicationConfiguration {

	interface Key {}
    String get(Key key);
    String get(Key key, String defaultValue);

}
