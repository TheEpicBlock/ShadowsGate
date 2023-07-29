package nl.theepicblock.shadowsgate.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a method will take care of marking things as dirty
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface MarksAsDirty {
    /**
     * Indicated that this function hands things off to another function, which will in turn mark it as dirty
     */
    String becauseOf() default "";
}
