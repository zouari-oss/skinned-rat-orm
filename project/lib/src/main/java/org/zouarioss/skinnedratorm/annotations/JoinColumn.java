package org.zouarioss.skinnedratorm.annotations;

import org.zouarioss.skinnedratorm.flag.OnDeleteType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JoinColumn {
  String name();

  boolean nullable() default true;

  boolean unique() default false;
  
  OnDeleteType onDelete() default OnDeleteType.CASCADE;
  
  OnDeleteType onUpdate() default OnDeleteType.CASCADE;
}
