package org.zouarioss.skinnedratorm.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Enumerated {
  EnumType value() default EnumType.ORDINAL;
}
