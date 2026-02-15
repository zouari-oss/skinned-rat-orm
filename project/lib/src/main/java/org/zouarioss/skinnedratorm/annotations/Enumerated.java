// `Enumerated` package name
package org.zouarioss.skinnedratorm.annotations;

// `java` import(s)
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// `zouarioss` import(s)
import org.zouarioss.skinnedratorm.flag.EnumType;

/**
 * Specifies how an {@code enum} field should be persisted in the database.
 *
 * <p>
 * The {@code @Enumerated} annotation defines the strategy used to map
 * a Java {@code enum} type to a database column. It determines whether
 * the enum is stored using its ordinal value (position) or its string name.
 * </p>
 *
 * <p>
 * This annotation is retained at runtime, allowing the ORM engine to
 * inspect the selected {@link org.zouarioss.skinnedratorm.flag.EnumType}
 * and apply the appropriate conversion logic during persistence and retrieval.
 * </p>
 *
 * <h2>Mapping Strategies:</h2>
 * <ul>
 * <li><b>ORDINAL</b> – Stores the enum constant's numeric position (default
 * behavior).</li>
 * <li><b>STRING</b> – Stores the enum constant's name as a string.</li>
 * </ul>
 *
 * <h2>Usage Example:</h2>
 *
 * <pre>{@code
 * public enum Role {
 *   USER,
 *   ADMIN
 * }
 *
 * @Enumerated(EnumType.STRING)
 * private Role role;
 * }</pre>
 *
 * <p>
 * <b>Recommendation:</b> Using {@code EnumType.STRING} is generally safer,
 * as it prevents issues caused by reordering enum constants, which would
 * affect ordinal values.
 * </p>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-15
 *
 * @see org.zouarioss.skinnedratorm.flag.EnumType
 * @see org.zouarioss.skinnedratorm.annotations.Column
 *
 *      <a href=
 *      "https://github.com/zouari-oss/skinned-rat-orm/tree/main/project/lib/src/main/java/org/zouarioss/skinnedratorm/annotations/Enumerated.java"
 *      target="_blank">
 *      Enumerated.java
 *      </a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Enumerated {
  EnumType value() default EnumType.ORDINAL;
} // `Enumerated` @interface
