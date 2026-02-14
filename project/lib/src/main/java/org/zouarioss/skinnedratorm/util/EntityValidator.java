package org.zouarioss.skinnedratorm.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.zouarioss.skinnedratorm.annotations.NotNull;
import org.zouarioss.skinnedratorm.annotations.Pattern;
import org.zouarioss.skinnedratorm.annotations.Size;
import org.zouarioss.skinnedratorm.exception.ValidationException;

public class EntityValidator {

  public static void validate(final Object entity) throws Exception {
    final List<String> errors = new ArrayList<>();
    final Class<?> clazz = entity.getClass();

    for (final Field field : getAllFields(clazz)) {
      field.setAccessible(true);
      final Object value = field.get(entity);

      if (field.isAnnotationPresent(NotNull.class)) {
        if (value == null) {
          final NotNull annotation = field.getAnnotation(NotNull.class);
          errors.add(field.getName() + ": " + annotation.message());
        }
      }

      if (field.isAnnotationPresent(Size.class) && value != null) {
        final Size annotation = field.getAnnotation(Size.class);
        int size = 0;

        if (value instanceof String) {
          size = ((String) value).length();
        } else if (value instanceof java.util.Collection) {
          size = ((java.util.Collection<?>) value).size();
        }

        if (size < annotation.min() || size > annotation.max()) {
          errors.add(field.getName() + ": " + annotation.message());
        }
      }

      if (field.isAnnotationPresent(Pattern.class) && value != null) {
        final Pattern annotation = field.getAnnotation(Pattern.class);
        if (value instanceof String) {
          if (!((String) value).matches(annotation.regexp())) {
            errors.add(field.getName() + ": " + annotation.message());
          }
        }
      }
    }

    if (!errors.isEmpty()) {
      throw new ValidationException("Validation failed: " + String.join(", ", errors));
    }
  }

  private static List<Field> getAllFields(Class<?> clazz) {
    final List<Field> fields = new ArrayList<>();
    while (clazz != null && clazz != Object.class) {
      for (final Field field : clazz.getDeclaredFields()) {
        fields.add(field);
      }
      clazz = clazz.getSuperclass();
    }
    return fields;
  }
}
