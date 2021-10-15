package io.fairyproject.module;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Modular {

    /**
     * @return the name of the module
     */
    String value();

    /**
     * @return the class path of the module
     */
    String classPath() default "io.fairyproject";

    /**
     * @return is this module abstract
     */
    boolean abstraction() default false;

    /**
     * @return the modules to depend on
     */
    Depend[] depends() default {};

}
