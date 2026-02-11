
package org.zouarioss.skinnedratorm.metadata;

import java.lang.reflect.Field;

import org.zouarioss.skinnedratorm.annotations.Column;
import org.zouarioss.skinnedratorm.annotations.CreationTimestamp;
import org.zouarioss.skinnedratorm.annotations.EnumType;
import org.zouarioss.skinnedratorm.annotations.Enumerated;
import org.zouarioss.skinnedratorm.annotations.GeneratedValue;
import org.zouarioss.skinnedratorm.annotations.GenerationType;
import org.zouarioss.skinnedratorm.annotations.Id;
import org.zouarioss.skinnedratorm.annotations.UpdateTimestamp;

public class FieldMetadata {

  private final Field field;
  private final String columnName;
  private final boolean id;
  private final EnumType enumType;
  private final boolean creationTimestamp;
  private final boolean updateTimestamp;
  private final boolean generatedValue;
  private final GenerationType generationType;
  private final boolean updatable;
  private final boolean unique;
  private final int length;
  private final boolean nullable;

  public FieldMetadata(final Field field) {
    this.field = field;
    this.field.setAccessible(true);

    this.id = field.isAnnotationPresent(Id.class);

    final Column column = field.getAnnotation(Column.class);
    this.columnName = (column != null && !column.name().isBlank())
        ? column.name()
        : field.getName();

    this.updatable = (column != null) ? column.updatable() : true;
    this.unique = (column != null) && column.unique();
    this.length = (column != null) ? column.length() : 255;
    this.nullable = (column != null) ? column.nullable() : true;

    final Enumerated enumerated = field.getAnnotation(Enumerated.class);
    this.enumType = (enumerated != null)
        ? enumerated.value()
        : null;

    this.creationTimestamp = field.isAnnotationPresent(CreationTimestamp.class);
    this.updateTimestamp = field.isAnnotationPresent(UpdateTimestamp.class);

    final GeneratedValue gv = field.getAnnotation(GeneratedValue.class);
    this.generatedValue = (gv != null);
    this.generationType = (gv != null) ? gv.strategy() : null;
  }

  public Field getField() {
    return field;
  }

  public String getColumnName() {
    return columnName;
  }

  public boolean isId() {
    return id;
  }

  public EnumType getEnumType() {
    return enumType;
  }

  public boolean isCreationTimestamp() {
    return creationTimestamp;
  }

  public boolean isUpdateTimestamp() {
    return updateTimestamp;
  }

  public boolean isGeneratedValue() {
    return generatedValue;
  }

  public GenerationType getGenerationType() {
    return generationType;
  }

  public boolean isUpdatable() {
    return updatable;
  }

  public boolean isUnique() {
    return unique;
  }

  public int getLength() {
    return length;
  }

  public boolean isNullable() {
    return nullable;
  }
}
