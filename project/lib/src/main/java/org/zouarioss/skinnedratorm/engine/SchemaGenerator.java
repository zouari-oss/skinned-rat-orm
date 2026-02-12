package org.zouarioss.skinnedratorm.engine;

import java.sql.Connection;
import java.sql.Statement;

import org.zouarioss.skinnedratorm.annotations.EnumType;
import org.zouarioss.skinnedratorm.metadata.EntityMetadata;
import org.zouarioss.skinnedratorm.metadata.FieldMetadata;
import org.zouarioss.skinnedratorm.metadata.MetadataRegistry;

public class SchemaGenerator {

  private final Connection connection;
  private final SQLDialect dialect;

  public SchemaGenerator(final Connection connection, final SQLDialect dialect) {
    this.connection = connection;
    this.dialect = dialect;
  }

  public void createTable(final Class<?> entityClass) throws Exception {
    final EntityMetadata metadata = MetadataRegistry.getMetadata(entityClass);
    final String sql = generateCreateTableSQL(metadata, entityClass);

    try (Statement stmt = connection.createStatement()) {
      stmt.execute(sql);
    }

    createIndexes(entityClass, metadata);
  }

  public void dropTable(final Class<?> entityClass) throws Exception {
    final EntityMetadata metadata = MetadataRegistry.getMetadata(entityClass);
    final String sql = "DROP TABLE IF EXISTS " + metadata.getTableName();

    try (Statement stmt = connection.createStatement()) {
      stmt.execute(sql);
    }
  }

  private String generateCreateTableSQL(final EntityMetadata metadata, final Class<?> entityClass) {
    final StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
        .append(metadata.getTableName())
        .append(" (");

    for (final FieldMetadata field : metadata.getFields()) {
      sql.append(field.getColumnName())
          .append(" ")
          .append(mapJavaTypeToSQL(field));

      if (!field.isNullable()) {
        sql.append(" NOT NULL");
      }

      if (field.isUnique()) {
        sql.append(" UNIQUE");
      }

      sql.append(",");
    }

    // Add foreign key columns for OneToOne relationships (only owning side)
    for (final FieldMetadata relationshipField : metadata.getRelationshipFields()) {
      if (relationshipField.isOwningSide() && relationshipField.getJoinColumnName() != null) {
        sql.append(relationshipField.getJoinColumnName())
            .append(" CHAR(36)"); // UUID foreign key
        
        // Assuming nullable is same as the @JoinColumn nullable attribute
        // For now, we'll make it nullable
        sql.append(" NULL");
        
        if (relationshipField.isUnique()) {
          sql.append(" UNIQUE");
        }
        
        sql.append(",");
      }
    }

    if (metadata.getIdField() != null) {
      sql.append("PRIMARY KEY (")
          .append(metadata.getIdField().getColumnName())
          .append(")");
    } else {
      sql.setLength(sql.length() - 1);
    }

    sql.append(")");

    return sql.toString();
  }

  private String mapJavaTypeToSQL(final FieldMetadata field) {
    final Class<?> type = field.getField().getType();

    if (field.isId()) {
      if (type == String.class) {
        return "VARCHAR(" + field.getLength() + ")";
      } else if (type == java.util.UUID.class) {
        return "CHAR(36)"; // UUID string representation
      } else if (type == Long.class || type == long.class) {
        return dialect == SQLDialect.MYSQL ? "BIGINT AUTO_INCREMENT" : "BIGSERIAL";
      } else if (type == Integer.class || type == int.class) {
        return dialect == SQLDialect.MYSQL ? "INT AUTO_INCREMENT" : "SERIAL";
      }
    }

    if (type == String.class) {
      return "VARCHAR(" + field.getLength() + ")";
    } else if (type == java.util.UUID.class) {
      return "CHAR(36)";
    } else if (type == Integer.class || type == int.class) {
      return "INT";
    } else if (type == Long.class || type == long.class) {
      return "BIGINT";
    } else if (type == Double.class || type == double.class) {
      return "DOUBLE PRECISION";
    } else if (type == Float.class || type == float.class) {
      return "REAL";
    } else if (type == Boolean.class || type == boolean.class) {
      return "BOOLEAN";
    } else if (type.isEnum()) {
      if (field.getEnumType() == EnumType.STRING) {
        return "VARCHAR(255)";
      } else {
        return "INT";
      }
    } else if (type == java.time.Instant.class) {
      return "TIMESTAMP";
    } else if (type == java.time.LocalDateTime.class) {
      return "TIMESTAMP";
    } else if (type == java.time.LocalDate.class) {
      return "DATE";
    } else if (type == java.util.Date.class) {
      return "TIMESTAMP";
    }

    return "VARCHAR(" + field.getLength() + ")";
  }

  private void createIndexes(final Class<?> entityClass, final EntityMetadata metadata) throws Exception {
    final org.zouarioss.skinnedratorm.annotations.Index[] indexes = entityClass.getAnnotationsByType(
        org.zouarioss.skinnedratorm.annotations.Index.class);

    for (final org.zouarioss.skinnedratorm.annotations.Index index : indexes) {
      final String columns = String.join(", ", index.columnList());
      final String sql = "CREATE INDEX IF NOT EXISTS " + index.name() +
          " ON " + metadata.getTableName() + " (" + columns + ")";

      try (Statement stmt = connection.createStatement()) {
        stmt.execute(sql);
      }
    }
  }
}
