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
import org.zouarioss.skinnedratorm.util.ResultSetMapper;

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
    validateColumn(column);
    whereClauses.add(column + " = ?");
    parameters.add(value);
    return this;
  }

  public QueryBuilder<T> where(final String column, final String operator, final Object value) {
    validateColumn(column);
    whereClauses.add(column + " " + operator + " ?");
    parameters.add(value);
    return this;
  }

  public QueryBuilder<T> whereIn(final String column, final List<?> values) {
    if (values == null || values.isEmpty()) {
      return this;
    }
    validateColumn(column);
    final String placeholders = String.join(",", Collections.nCopies(values.size(), "?"));
    whereClauses.add(column + " IN (" + placeholders + ")");
    parameters.addAll(values);
    return this;
  }

  public QueryBuilder<T> orderBy(final String column) {
    validateColumn(column);
    this.orderBy = column + " ASC";
    return this;
  }

  public QueryBuilder<T> orderBy(final String column, final String direction) {
    validateColumn(column);
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

    try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
      int index = 1;
      for (final Object param : parameters) {
        stmt.setObject(index++, param);
      }

      try (final ResultSet rs = stmt.executeQuery()) {
        final List<T> results = new ArrayList<>();

        while (rs.next()) {
          final T instance = ResultSetMapper.mapResultToEntity(entityClass, metadata, rs, connection);
          results.add(instance);
        }

        return results;
      }
    }
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

    try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
      int index = 1;
      for (final Object param : parameters) {
        stmt.setObject(index++, param);
      }

      try (final ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return rs.getLong(1);
        }
        return 0;
      }
    }
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

  private void validateColumn(final String column) {
    final boolean valid = metadata.getFields().stream()
        .anyMatch(f -> f.getColumnName().equals(column))
        || metadata.getRelationshipFields().stream()
            .anyMatch(f -> column.equals(f.getJoinColumnName()));

    if (!valid) {
      throw new org.zouarioss.skinnedratorm.exception.QueryException(
          "Unknown column '" + column + "' for entity " + entityClass.getSimpleName());
    }
  }
}
