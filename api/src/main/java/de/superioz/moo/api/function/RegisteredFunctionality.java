package de.superioz.moo.api.function;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define a method, which works as a predefined function.
 * This functions can be called from {@link Functionalities} class, where all these
 * functions have to be registered as well.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RegisteredFunctionality {

    /**
     * Name of the function. This name is important because it is needed to call
     * the function from inside {@link Functionalities}
     *
     * @return The name as string
     */
    String name() default "";

}
