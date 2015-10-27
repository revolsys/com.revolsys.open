package com.revolsys.record.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.gis.io.Statistics;
import com.revolsys.io.PathName;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.record.query.functions.EnvelopeIntersects;
import com.revolsys.record.query.functions.F;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;

public class Query extends BaseObjectWithProperties implements Cloneable {
  private static void addFilter(final Query query, final RecordDefinition recordDefinition,
    final Map<String, ?> filter, final AbstractMultiCondition multipleCondition) {
    if (filter != null && !filter.isEmpty()) {
      for (final Entry<String, ?> entry : filter.entrySet()) {
        final String name = entry.getKey();
        final FieldDefinition attribute = recordDefinition.getField(name);
        if (attribute == null) {
          final Object value = entry.getValue();
          if (value == null) {
            multipleCondition.add(Q.isNull(name));
          } else if (value instanceof Collection) {
            final Collection<?> values = (Collection<?>)value;
            multipleCondition.add(new In(name, values));
          } else {
            multipleCondition.add(Q.equal(name, value));
          }
        } else {
          final Object value = entry.getValue();
          if (value == null) {
            multipleCondition.add(Q.isNull(name));
          } else if (value instanceof Collection) {
            final Collection<?> values = (Collection<?>)value;
            multipleCondition.add(new In(attribute, values));
          } else {
            multipleCondition.add(Q.equal(attribute, value));
          }
        }
      }
      query.setWhereCondition(multipleCondition);
    }
  }

  public static Query and(final RecordDefinition recordDefinition, final Map<String, ?> filter) {
    final Query query = new Query(recordDefinition);
    final And and = new And();
    addFilter(query, recordDefinition, filter, and);
    return query;
  }

  public static Query equal(final RecordDefinition recordDefinition, final String name,
    final Object value) {
    final FieldDefinition attribute = recordDefinition.getField(name);
    if (attribute == null) {
      return null;
    } else {
      final Query query = new Query(recordDefinition);
      final Value valueCondition = new Value(attribute, value);
      final BinaryCondition equal = Q.equal(name, valueCondition);
      query.setWhereCondition(equal);
      return query;
    }
  }

  public static Query intersects(final RecordDefinition recordDefinition,
    final BoundingBox boundingBox) {
    final FieldDefinition geometryField = recordDefinition.getGeometryField();
    if (geometryField == null) {
      return null;
    } else {
      final EnvelopeIntersects intersects = F.envelopeIntersects(geometryField, boundingBox);
      final Query query = new Query(recordDefinition, intersects);
      return query;
    }

  }

  public static Query or(final RecordDefinition recordDefinition, final Map<String, ?> filter) {
    final Query query = new Query(recordDefinition);
    final Or or = new Or();
    addFilter(query, recordDefinition, filter, or);
    return query;
  }

  public static Query orderBy(final PathName pathName, final String... orderBy) {
    final Query query = new Query(pathName);
    query.setOrderByFieldNames(orderBy);
    return query;
  }

  private List<String> fieldNames = Collections.emptyList();

  private String fromClause;

  private int limit = Integer.MAX_VALUE;

  private boolean lockResults = false;

  private int offset = 0;

  private Map<String, Boolean> orderBy = new HashMap<String, Boolean>();

  private List<Object> parameters = new ArrayList<Object>();

  private RecordDefinition recordDefinition;

  private String sql;

  private Statistics statistics;

  private PathName typeName;

  private String typePathAlias;

  private Condition whereCondition = Condition.ALL;

  public Query() {
  }

  public Query(final PathName typePath) {
    this(typePath, null);
  }

  public Query(final PathName typePath, final Condition whereCondition) {
    this.typeName = typePath;
    setWhereCondition(whereCondition);
  }

  public Query(final RecordDefinition recordDefinition) {
    this(recordDefinition, null);
  }

  public Query(final RecordDefinition recordDefinition, final Condition whereCondition) {
    this(recordDefinition.getPath());
    this.recordDefinition = recordDefinition;
    setWhereCondition(whereCondition);
  }

  public Query(final String typePath) {
    this(PathName.newPathName(typePath));
  }

  public Query(final String typePath, final Condition whereCondition) {
    this(PathName.newPathName(typePath), whereCondition);
  }

  public Query addOrderBy(final String column) {
    return addOrderBy(column, true);
  }

  public Query addOrderBy(final String column, final boolean ascending) {
    this.orderBy.put(column, ascending);
    return this;
  }

  @Deprecated
  public void addParameter(final Object value) {
    this.parameters.add(value);
  }

  public void and(final Condition condition) {
    if (condition != null) {
      final Condition whereCondition = getWhereCondition();
      if (whereCondition.isEmpty()) {
        setWhereCondition(condition);
      } else if (whereCondition instanceof And) {
        final And and = (And)whereCondition;
        and.add(condition);
      } else {
        setWhereCondition(new And(whereCondition, condition));
      }
    }
  }

  @Override
  public Query clone() {
    try {
      final Query clone = (Query)super.clone();
      clone.fieldNames = new ArrayList<String>(clone.fieldNames);
      clone.parameters = new ArrayList<Object>(this.parameters);
      clone.orderBy = new HashMap<String, Boolean>(this.orderBy);
      if (this.whereCondition != null) {
        clone.whereCondition = this.whereCondition.clone();
      }
      if (!clone.getFieldNames().isEmpty() || clone.whereCondition != null) {
        clone.sql = null;
      }
      return clone;
    } catch (final CloneNotSupportedException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  public List<String> getFieldNames() {
    return this.fieldNames;
  }

  public String getFromClause() {
    return this.fromClause;
  }

  public FieldDefinition getGeometryField() {
    return getRecordDefinition().getGeometryField();
  }

  public int getLimit() {
    return this.limit;
  }

  public int getOffset() {
    return this.offset;
  }

  public Map<String, Boolean> getOrderBy() {
    return this.orderBy;
  }

  public List<Object> getParameters() {
    return this.parameters;
  }

  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  public String getSql() {
    return this.sql;
  }

  public Statistics getStatistics() {
    return this.statistics;
  }

  public String getTypeName() {
    if (this.typeName == null) {
      return null;
    } else {
      return this.typeName.getPath();
    }
  }

  public String getTypeNameAlias() {
    return this.typePathAlias;
  }

  public PathName getTypePath() {
    return this.typeName;
  }

  public String getWhere() {
    return this.whereCondition.toFormattedString();
  }

  public Condition getWhereCondition() {
    return this.whereCondition;
  }

  public boolean isLockResults() {
    return this.lockResults;
  }

  public void or(final Condition condition) {
    final Condition whereCondition = getWhereCondition();
    if (whereCondition.isEmpty()) {
      setWhereCondition(condition);
    } else if (whereCondition instanceof Or) {
      final Or or = (Or)whereCondition;
      or.add(condition);
    } else {
      setWhereCondition(new Or(whereCondition, condition));
    }
  }

  public void setFieldNames(final List<String> fieldNames) {
    this.fieldNames = fieldNames;
  }

  public void setFieldNames(final String... fieldNames) {
    setFieldNames(Arrays.asList(fieldNames));
  }

  public void setFromClause(final String fromClause) {
    this.fromClause = fromClause;
  }

  public void setLimit(final int limit) {
    if (limit < 0) {
      this.limit = Integer.MAX_VALUE;
    } else {
      this.limit = limit;
    }
  }

  public void setLockResults(final boolean lockResults) {
    this.lockResults = lockResults;
  }

  public void setOffset(final int offset) {
    this.offset = offset;
  }

  public void setOrderBy(final Map<String, Boolean> orderBy) {
    if (orderBy != this.orderBy) {
      this.orderBy.clear();
      if (orderBy != null) {
        this.orderBy.putAll(orderBy);
      }
    }
  }

  public void setOrderByFieldNames(final List<String> orderBy) {
    this.orderBy.clear();
    for (final String column : orderBy) {
      this.orderBy.put(column, Boolean.TRUE);
    }
  }

  public void setOrderByFieldNames(final String... orderBy) {
    setOrderByFieldNames(Arrays.asList(orderBy));
  }

  public void setRecordDefinition(final RecordDefinition recordDefinition) {
    this.recordDefinition = recordDefinition;
    if (this.whereCondition != null) {
      this.whereCondition.setRecordDefinition(recordDefinition);
    }
  }

  public void setSql(final String sql) {
    this.sql = sql;
  }

  public void setStatistics(final Statistics statistics) {
    this.statistics = statistics;
  }

  public void setTypeName(final String typeName) {
    this.typeName = PathName.newPathName(typeName);
  }

  public void setTypeNameAlias(final String typePathAlias) {
    this.typePathAlias = typePathAlias;
  }

  public void setWhere(final String where) {
    final Condition whereCondition = QueryValue.parseWhere(this.recordDefinition, where);
    setWhereCondition(whereCondition);
  }

  public void setWhereCondition(final Condition whereCondition) {
    if (whereCondition == null) {
      this.whereCondition = Condition.ALL;
    } else {
      this.whereCondition = whereCondition;
      final RecordDefinition recordDefinition = getRecordDefinition();
      if (recordDefinition != null) {
        whereCondition.setRecordDefinition(recordDefinition);
      }
    }
  }

  @Override
  public String toString() {
    try {
      final StringBuilder string = new StringBuilder();
      if (this.sql == null) {
        string.append(JdbcUtils.getSelectSql(this));
      } else {
        string.append(this.sql);
      }
      if (!this.parameters.isEmpty()) {
        string.append(" ");
        string.append(this.parameters);
      }
      return string.toString();
    } catch (final Throwable t) {
      t.printStackTrace();
      return "";
    }
  }
}
