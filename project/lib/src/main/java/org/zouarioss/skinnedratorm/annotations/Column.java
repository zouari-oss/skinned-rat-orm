// `Column` Package name
package org.zouarioss.skinnedratorm.annotations;

// `java` import(s)
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
  String name() default "";

  boolean nullable() default true;

  boolean updatable() default true;

  boolean unique() default false;

  int length() default 255;

  String columnDefinition() default "";

  String defaultValue() default "";
} // Column @interface
