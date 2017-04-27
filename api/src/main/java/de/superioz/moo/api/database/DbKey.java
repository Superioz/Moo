package de.superioz.moo.api.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DbKey {

    /**
     * This value specifies the key of one field inside the database<br>
     * Example: If you have playerData and the field is named 'name' but you want
     * to name it 'lastName', then just add a @DbKey(name = 'lastName') to the field and done.
     *
     * @return The key
     */
    String key() default "";

}
