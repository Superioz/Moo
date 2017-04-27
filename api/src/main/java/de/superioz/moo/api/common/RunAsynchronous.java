package de.superioz.moo.api.common;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation shows that the method or whatever wants to be
 * running asynchronous
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RunAsynchronous {

}
