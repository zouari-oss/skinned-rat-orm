package org.zouarioss.skinnedratorm.annotations;

import org.zouarioss.skinnedratorm.flag.EnumType;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Enumerated {
  EnumType value() default EnumType.ORDINAL;
}
