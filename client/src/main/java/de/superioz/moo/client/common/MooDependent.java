package de.superioz.moo.client.common;

import de.superioz.moo.client.Moo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Shows the need of the connection to the cloud by {@link Moo}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MooDependent {

}
