package org.zouarioss.skinnedratorm.engine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.zouarioss.skinnedratorm.metadata.EntityMetadata;
import org.zouarioss.skinnedratorm.metadata.FieldMetadata;
import org.zouarioss.skinnedratorm.metadata.MetadataRegistry;

public class QueryBuilder<T> {

  private final Class<T> entityClass;
  private final Connection connection;
  private final EntityMetadata metadata;

  private final List<String> whereClauses = new ArrayList<>();
  private final List<Object> parameters = new ArrayList<>();
  private String orderBy;
  private Integer limit;
  private Integer offset;

  public QueryBuilder(final Class<T> entityClass, final Connection connection) {
    this.entityClass = entityClass;
    this.connection = connection;
    this.metadata = MetadataRegistry.getMetadata(entityClass);
  }

  public QueryBuilder<T> where(final String column, final Object value) {
    whereClauses.add(column + " = ?");
    parameters.add(value);
    return this;
  }

  public QueryBuilder<T> where(final String column, final String operator, final Object value) {
    whereClauses.add(column + " " + operator + " ?");
    parameters.add(value);
    return this;
  }

  public QueryBuilder<T> whereIn(final String column, final List<?> values) {
    if (values == null || values.isEmpty()) {
      return this;
    }
    final String placeholders = String.join(",", Collections.nCopies(values.size(), "?"));
    whereClauses.add(column + " IN (" + placeholders + ")");
    parameters.addAll(values);
    return this;
  }

  public QueryBuilder<T> orderBy(final String column) {
    this.orderBy = column + " ASC";
    return this;
  }

  public QueryBuilder<T> orderBy(final String column, final String direction) {
    this.orderBy = column + " " + direction;
    return this;
  }

  public QueryBuilder<T> limit(final int limit) {
    this.limit = limit;
    return this;
  }

  public QueryBuilder<T> offset(final int offset) {
    this.offset = offset;
    return this;
  }

  public List<T> getResultList() throws Exception {
    final String sql = buildSelectQuery();
    final PreparedStatement stmt = connection.prepareStatement(sql);

    int index = 1;
    for (final Object param : parameters) {
      stmt.setObject(index++, param);
    }

    final ResultSet rs = stmt.executeQuery();
    final List<T> results = new ArrayList<>();

    while (rs.next()) {
      final T instance = mapResultToEntity(rs);
      results.add(instance);
    }

    return results;
  }

  public T getSingleResult() throws Exception {
    final List<T> results = limit(1).getResultList();
    if (results.isEmpty()) {
      return null;
    }
    return results.get(0);
  }

  public long count() throws Exception {
    final String sql = buildCountQuery();
    final PreparedStatement stmt = connection.prepareStatement(sql);

    int index = 1;
    for (final Object param : parameters) {
      stmt.setObject(index++, param);
    }

    final ResultSet rs = stmt.executeQuery();
    if (rs.next()) {
      return rs.getLong(1);
    }
    return 0;
  }

  private String buildSelectQuery() {
    final StringBuilder sql = new StringBuilder("SELECT * FROM ")
        .append(metadata.getTableName());

    if (!whereClauses.isEmpty()) {
      sql.append(" WHERE ").append(String.join(" AND ", whereClauses));
    }

    if (orderBy != null) {
      sql.append(" ORDER BY ").append(orderBy);
    }

    if (limit != null) {
      sql.append(" LIMIT ").append(limit);
    }

    if (offset != null) {
      sql.append(" OFFSET ").append(offset);
    }

    return sql.toString();
  }

  private String buildCountQuery() {
    final StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM ")
        .append(metadata.getTableName());

    if (!whereClauses.isEmpty()) {
      sql.append(" WHERE ").append(String.join(" AND ", whereClauses));
    }

    return sql.toString();
  }

  private T mapResultToEntity(final ResultSet rs) throws Exception {
    final T instance = entityClass.getDeclaredConstructor().newInstance();

    for (final FieldMetadata fieldMeta : metadata.getFields()) {
      Object value = rs.getObject(fieldMeta.getColumnName());

      if (value != null) {
        // Handle UUID conversion
        if (fieldMeta.getField().getType() == java.util.UUID.class && value instanceof String) {
          value = java.util.UUID.fromString((String) value);
        }
        // Handle Timestamp to Instant conversion
        else if (fieldMeta.getField().getType() == java.time.Instant.class && value instanceof java.sql.Timestamp) {
          value = ((java.sql.Timestamp) value).toInstant();
        }
        // Handle enum conversion
        else if (fieldMeta.getEnumType() != null) {
          value = convertToEnum(fieldMeta, value);
        }
      }

      fieldMeta.getField().set(instance, value);
    }

    // Load OneToOne relationships (only owning side has join column)
    for (final FieldMetadata relationshipMeta : metadata.getRelationshipFields()) {
      if (relationshipMeta.isOwningSide() && relationshipMeta.getJoinColumnName() != null) {
        // Get the foreign key value from the result set
        Object foreignKeyValue = rs.getObject(relationshipMeta.getJoinColumnName());
        
        if (foreignKeyValue != null) {
          // Convert to UUID if needed
          if (foreignKeyValue instanceof String) {
            foreignKeyValue = java.util.UUID.fromString((String) foreignKeyValue);
          }
          
          // Load the related entity using EntityManager
          final Class<?> relatedClass = relationshipMeta.getField().getType();
          final org.zouarioss.skinnedratorm.core.EntityManager em = 
              new org.zouarioss.skinnedratorm.core.EntityManager(connection);
          final Object relatedEntity = em.findById(relatedClass, foreignKeyValue);
          
          // Set the related entity
          relationshipMeta.getField().set(instance, relatedEntity);
        }
      }
    }

    return instance;
  }

  @SuppressWarnings("unchecked")
  private Object convertToEnum(final FieldMetadata fieldMeta, final Object value) {
    final Class<?> enumType = fieldMeta.getField().getType();

    if (!enumType.isEnum()) {
      return value;
    }

    if (fieldMeta.getEnumType() == org.zouarioss.skinnedratorm.annotations.EnumType.STRING) {
      return Enum.valueOf((Class<? extends Enum>) enumType, value.toString());
    } else {
      final Object[] constants = enumType.getEnumConstants();
      return constants[(Integer) value];
    }
  }
}
