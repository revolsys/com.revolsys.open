package com.revolsys.record.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jeometry.common.io.PathName;

import com.revolsys.collection.list.Lists;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.predicate.Predicates;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.Records;
import com.revolsys.record.query.functions.EnvelopeIntersects;
import com.revolsys.record.query.functions.F;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.LockMode;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.util.Cancellable;
import com.revolsys.util.CancellableProxy;
import com.revolsys.util.Property;
import com.revolsys.util.count.LabelCounters;

public class Query extends BaseObjectWithProperties
  implements Cloneable, CancellableProxy, RecordDefinitionProxy {
  private static void addFilter(final Query query, final RecordDefinition recordDefinition,
    final Map<String, ?> filter, final AbstractMultiCondition multipleCondition) {
    if (filter != null && !filter.isEmpty()) {
      for (final Entry<String, ?> entry : filter.entrySet()) {
        final String name = entry.getKey();
        final FieldDefinition fieldDefinition = recordDefinition.getField(name);
        if (fieldDefinition == null) {
          final Object value = entry.getValue();
          if (value == null) {
            multipleCondition.addCondition(Q.isNull(name));
          } else if (value instanceof Collection) {
            final Collection<?> values = (Collection<?>)value;
            multipleCondition.addCondition(new In(name, values));
          } else {
            multipleCondition.addCondition(Q.equal(name, value));
          }
        } else {
          final Object value = entry.getValue();
          if (value == null) {
            multipleCondition.addCondition(Q.isNull(name));
          } else if (value instanceof Collection) {
            final Collection<?> values = (Collection<?>)value;
            multipleCondition.addCondition(new In(fieldDefinition, values));
          } else {
            multipleCondition.addCondition(Q.equal(fieldDefinition, value));
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

  public static Query equal(final FieldDefinition field, final Object value) {
    final RecordDefinition recordDefinition = field.getRecordDefinition();
    final Query query = new Query(recordDefinition);
    final Value valueCondition = new Value(field, value);
    final BinaryCondition equal = Q.equal(field, valueCondition);
    query.setWhereCondition(equal);
    return query;
  }

  public static Query equal(final RecordDefinitionProxy recordDefinition, final String name,
    final Object value) {
    final FieldDefinition fieldDefinition = recordDefinition.getFieldDefinition(name);
    if (fieldDefinition == null) {
      return null;
    } else {
      final Query query = new Query(recordDefinition);
      final Value valueCondition = new Value(fieldDefinition, value);
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

  public static Query intersects(final RecordStore recordStore, final PathName path,
    final BoundingBox boundingBox) {
    final RecordDefinition recordDefinition = recordStore.getRecordDefinition(path);
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

  public static Query where(
    final BiFunction<FieldDefinition, Object, BinaryCondition> whereFunction,
    final FieldDefinition field, final Object value) {
    final RecordDefinition recordDefinition = field.getRecordDefinition();
    final Query query = new Query(recordDefinition);
    final Value valueCondition = new Value(field, value);
    final BinaryCondition equal = whereFunction.apply(field, valueCondition);
    query.setWhereCondition(equal);
    return query;
  }

  private boolean distinct = false;

  private Cancellable cancellable;

  private RecordFactory<Record> recordFactory;

  private List<String> fieldNames = new ArrayList<>();

  private String fromClause;

  private int limit = Integer.MAX_VALUE;

  private LockMode lockMode = LockMode.NONE;

  private int offset = 0;

  private Map<CharSequence, Boolean> orderBy = new LinkedHashMap<>();

  private List<Object> parameters = new ArrayList<>();

  private RecordDefinition recordDefinition;

  private String sql;

  private LabelCounters labelCountMap;

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

  public Query(final RecordDefinitionProxy recordDefinition) {
    this(recordDefinition, null);
  }

  public Query(final RecordDefinitionProxy recordDefinition, final Condition whereCondition) {
    this(recordDefinition == null ? null : recordDefinition.getPathName());
    this.recordDefinition = recordDefinition.getRecordDefinition();
    setWhereCondition(whereCondition);
  }

  public Query(final String typePath) {
    this(PathName.newPathName(typePath));
  }

  public Query(final String typePath, final Condition whereCondition) {
    this(PathName.newPathName(typePath), whereCondition);
  }

  public Query addFieldName(final String fieldName) {
    if (this.fieldNames == null) {
      this.fieldNames = new ArrayList<>();
    }
    if (!this.fieldNames.contains(fieldName)) {
      this.fieldNames.add(fieldName);
    }
    return this;
  }

  public Query addOrderBy(final CharSequence field) {
    return addOrderBy(field, true);
  }

  public Query addOrderBy(final CharSequence field, final boolean ascending) {
    this.orderBy.put(field, ascending);
    return this;
  }

  public Query addOrderBy(final String fieldName) {
    return addOrderBy(fieldName, true);
  }

  public Query addOrderBy(final String fieldName, final boolean ascending) {
    if (this.recordDefinition != null) {
      final FieldDefinition fieldDefinition = this.recordDefinition.getField(fieldName);
      if (fieldDefinition != null) {
        return this.addOrderBy(fieldDefinition, ascending);
      }
    }
    this.orderBy.put(fieldName, ascending);
    return this;
  }

  public Query addOrderById() {
    if (this.recordDefinition != null) {
      for (final FieldDefinition idField : this.recordDefinition.getIdFields()) {
        addOrderBy(idField);
      }
    }
    return this;
  }

  @Deprecated
  public void addParameter(final Object value) {
    this.parameters.add(value);
  }

  public Query and(final Condition condition) {
    if (!Property.isEmpty(condition)) {
      Condition whereCondition = getWhereCondition();
      whereCondition = whereCondition.and(condition);
      setWhereCondition(whereCondition);
    }
    return this;
  }

  public Query and(final Condition... conditions) {
    if (conditions != null) {
      Condition whereCondition = getWhereCondition();
      for (final Condition condition : conditions) {
        if (Property.hasValue(condition)) {
          whereCondition = whereCondition.and(condition);
        }
      }
      setWhereCondition(whereCondition);
    }
    return this;
  }

  public Query and(final Iterable<? extends Condition> conditions) {
    if (conditions != null) {
      Condition whereCondition = getWhereCondition();
      for (final Condition condition : conditions) {
        if (Property.hasValue(condition)) {
          whereCondition = whereCondition.and(condition);
        }
      }
      setWhereCondition(whereCondition);
    }
    return this;
  }

  public void clearOrderBy() {
    this.orderBy.clear();
  }

  @Override
  public Query clone() {
    final Query clone = (Query)super.clone();
    clone.fieldNames = new ArrayList<>(clone.fieldNames);
    clone.parameters = new ArrayList<>(this.parameters);
    clone.orderBy = new HashMap<>(this.orderBy);
    if (this.whereCondition != null) {
      clone.whereCondition = this.whereCondition.clone();
    }
    if (!clone.getFieldNames().isEmpty() || clone.whereCondition != null) {
      clone.sql = null;
    }
    return clone;
  }

  @SuppressWarnings("unchecked")
  public <R extends Record> void forEachRecord(final Iterable<R> records,
    final Consumer<? super R> consumer) {
    final Map<? extends CharSequence, Boolean> orderBy = getOrderBy();
    final Predicate<R> filter = (Predicate<R>)getWhereCondition();
    if (orderBy.isEmpty()) {
      if (filter == null) {
        records.forEach(consumer);
      } else {
        records.forEach((record) -> {
          if (filter.test(record)) {
            consumer.accept(record);
          }
        });
      }
    } else {
      final Comparator<R> comparator = Records.newComparatorOrderBy(orderBy);
      final List<R> results = Predicates.filter(records, filter);
      results.sort(comparator);
      results.forEach(consumer);
    }
  }

  @Override
  public Cancellable getCancellable() {
    return this.cancellable;
  }

  @Override
  public List<String> getFieldNames() {
    return this.fieldNames;
  }

  public List<FieldDefinition> getFields(final RecordDefinition recordDefinition) {
    if (this.fieldNames.isEmpty()) {
      if (recordDefinition == null) {
        return Collections.emptyList();
      } else {
        return recordDefinition.getFields();
      }
    } else {
      final List<FieldDefinition> fields = new ArrayList<>();
      for (String fieldName : this.fieldNames) {
        if (fieldName.equals("*")) {
          fields.addAll(recordDefinition.getFields());
        } else {
          if (fieldName.endsWith("\"")) {
            final int index = fieldName.indexOf('"');
            if (index > 0 && fieldName.charAt(index - 1) == ' ') {
              fieldName = fieldName.substring(index + 1, fieldName.length() - 1);
            }
          }
          final FieldDefinition field = recordDefinition.getField(fieldName);
          if (field != null) {
            fields.add(field);
          }
        }
      }
      return fields;
    }
  }

  public String getFromClause() {
    return this.fromClause;
  }

  @Override
  public FieldDefinition getGeometryField() {
    return getRecordDefinition().getGeometryField();
  }

  public int getLimit() {
    return this.limit;
  }

  public LockMode getLockMode() {
    return this.lockMode;
  }

  public int getOffset() {
    return this.offset;
  }

  public Map<CharSequence, Boolean> getOrderBy() {
    return this.orderBy;
  }

  public List<Object> getParameters() {
    return this.parameters;
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V extends Record> RecordFactory<V> getRecordFactory() {
    return (RecordFactory<V>)this.recordFactory;
  }

  public String getSql() {
    return this.sql;
  }

  public LabelCounters getStatistics() {
    return this.labelCountMap;
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

  public boolean isDistinct() {
    return this.distinct;
  }

  public Query newQuery(final RecordDefinition recordDefinition) {
    final Query query = clone();
    query.setRecordDefinition(recordDefinition);
    return query;
  }

  public void or(final Condition condition) {
    final Condition whereCondition = getWhereCondition();
    if (whereCondition.isEmpty()) {
      setWhereCondition(condition);
    } else if (whereCondition instanceof Or) {
      final Or or = (Or)whereCondition;
      or.or(condition);
    } else {
      setWhereCondition(new Or(whereCondition, condition));
    }
  }

  public void setCancellable(final Cancellable cancellable) {
    this.cancellable = cancellable;
  }

  public Query setDistinct(final boolean distinct) {
    this.distinct = distinct;
    return this;
  }

  public Query setFieldNames(final List<String> fieldNames) {
    this.fieldNames = Lists.toArray(fieldNames);
    return this;
  }

  public Query setFieldNames(final String... fieldNames) {
    setFieldNames(Arrays.asList(fieldNames));
    return this;
  }

  public Query setFromClause(final String fromClause) {
    this.fromClause = fromClause;
    return this;
  }

  public Query setLimit(final int limit) {
    if (limit < 0) {
      this.limit = Integer.MAX_VALUE;
    } else {
      this.limit = limit;
    }
    return this;
  }

  public Query setLockMode(final LockMode lockMode) {
    this.lockMode = lockMode;
    return this;
  }

  public Query setOffset(final int offset) {
    this.offset = offset;
    return this;
  }

  public Query setOrderBy(final CharSequence field) {
    this.orderBy.clear();
    return addOrderBy(field);
  }

  public Query setOrderBy(final Map<? extends CharSequence, Boolean> orderBy) {
    if (orderBy != this.orderBy) {
      this.orderBy.clear();
      if (orderBy != null) {
        for (final Entry<? extends CharSequence, Boolean> entry : orderBy.entrySet()) {
          final CharSequence field = entry.getKey();
          final Boolean ascending = entry.getValue();
          addOrderBy(field, ascending);
        }
        this.orderBy.putAll(orderBy);
      }
    }
    return this;
  }

  public Query setOrderByFieldNames(final List<? extends CharSequence> orderBy) {
    this.orderBy.clear();
    for (final CharSequence field : orderBy) {
      addOrderBy(field);
    }
    return this;
  }

  public Query setOrderByFieldNames(final String... orderBy) {
    return setOrderByFieldNames(Arrays.asList(orderBy));
  }

  public void setRecordDefinition(final RecordDefinition recordDefinition) {
    this.recordDefinition = recordDefinition;
    if (this.whereCondition != null) {
      this.whereCondition.setRecordDefinition(recordDefinition);
    }
  }

  @SuppressWarnings("unchecked")
  public void setRecordFactory(final RecordFactory<?> recordFactory) {
    this.recordFactory = (RecordFactory<Record>)recordFactory;
  }

  public Query setSql(final String sql) {
    this.sql = sql;
    return this;
  }

  public Query setStatistics(final LabelCounters labelCountMap) {
    this.labelCountMap = labelCountMap;
    return this;
  }

  public void setTypeName(final String typeName) {
    this.typeName = PathName.newPathName(typeName);
  }

  public void setTypeNameAlias(final String typePathAlias) {
    this.typePathAlias = typePathAlias;
  }

  public Query setWhere(final String where) {
    final Condition whereCondition = QueryValue.parseWhere(this.recordDefinition, where);
    return setWhereCondition(whereCondition);
  }

  public Query setWhereCondition(final Condition whereCondition) {
    if (whereCondition == null) {
      this.whereCondition = Condition.ALL;
    } else {
      this.whereCondition = whereCondition;
      final RecordDefinition recordDefinition = getRecordDefinition();
      if (recordDefinition != null) {
        whereCondition.setRecordDefinition(recordDefinition);
      }
    }
    return this;
  }

  public <V extends Record> void sort(final List<V> records) {
    final Map<? extends CharSequence, Boolean> orderBy = getOrderBy();
    if (Property.hasValue(orderBy)) {
      final Comparator<Record> comparator = Records.newComparatorOrderBy(orderBy);
      records.sort(comparator);
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
