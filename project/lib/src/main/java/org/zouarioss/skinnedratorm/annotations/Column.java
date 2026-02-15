// `Column` Package name
package org.zouarioss.skinnedratorm.annotations;

// `java` import(s)
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a database column mapping annotation used by the Skinned Rat ORM
 * framework.
 *
 * <p>
 * The {@code @Column} annotation is applied to entity fields to configure
 * how they are mapped to database table columns. It allows customization of
 * column properties such as name, nullability, uniqueness, length, default
 * value, update behavior, and custom SQL definitions.
 * </p>
 *
 * <p>
 * This annotation is retained at runtime, enabling reflection-based
 * processing by the ORM engine to generate schema definitions and manage
 * persistence operations dynamically.
 * </p>
 *
 * <h2>Usage Example:</h2>
 *
 * <pre>{@code
 * @Column(name = "email", nullable = false, unique = true, length = 150)
 * private String email;
 * }</pre>
 *
 * <h2>Attributes:</h2>
 * <ul>
 * <li><b>name</b> – Specifies the column name in the database table.
 * If left empty, the field name is used.</li>
 * <li><b>nullable</b> – Defines whether the column can contain NULL
 * values.</li>
 * <li><b>updatable</b> – Indicates whether the column is included in SQL UPDATE
 * statements.</li>
 * <li><b>unique</b> – Specifies whether the column must contain unique
 * values.</li>
 * <li><b>length</b> – Defines the column length (primarily for VARCHAR
 * types).</li>
 * <li><b>columnDefinition</b> – Allows specifying a custom SQL column
 * definition.</li>
 * <li><b>defaultValue</b> – Defines a default value for the column at the
 * database level.</li>
 * </ul>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-15
 *
 * @see org.zouarioss.skinnedratorm.annotations.Table
 * @see org.zouarioss.skinnedratorm.annotations.Id
 *
 *      <a href=
 *      "https://github.com/zouari-oss/skinned-rat-orm/tree/main/project/lib/src/main/java/org/zouarioss/skinnedratorm/annotations/Column.java"
 *      target="_blank">Column.java</a>
 */
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
