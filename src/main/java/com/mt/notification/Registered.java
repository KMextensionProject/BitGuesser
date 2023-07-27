package com.mt.notification;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation used for runtime registering of {@link Notification}
 * implementations which will be used to send notifications.
 *
 * @author mkrajcovic
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Registered {

}
