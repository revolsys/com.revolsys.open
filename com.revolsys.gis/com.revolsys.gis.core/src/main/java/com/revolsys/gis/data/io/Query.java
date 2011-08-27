package com.revolsys.gis.data.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.jdbc.JdbcUtils;

public class Query {
  private final List<Attribute> parameterAttributes = new ArrayList<Attribute>();

  private List<String> attributeNames = Collections.emptyList();

  private DataObjectMetaData metaData;

  private final List<Object> parameters = new ArrayList<Object>();

  private String sql;

  private QName tableName;

  private QName typeName;

  private String whereClause;

  private String fromClause;

  private List<String> orderBy = new ArrayList<String>();

  public Query() {
  }

  public Query(final DataObjectMetaData metaData) {
    this(metaData.getName());
    this.metaData = metaData;
  }

  public Query(final DataObjectMetaData metaData, final String sql) {
    this(metaData.getName(), sql);
    this.metaData = metaData;
  }

  public Query(final DataObjectMetaData metaData, final String sql,
    final List<Object> parameters) {
    this(metaData.getName(), sql, parameters);
    this.metaData = metaData;
  }

  public Query(final DataObjectMetaData metaData, final String sql,
    final Object... parameters) {
    this(metaData.getName(), sql, Arrays.asList(parameters));
    this.metaData = metaData;
  }

  public Query(final DataObjectStore dataStore, final QName typeName) {
    this(dataStore.getMetaData(typeName));
  }

  public Query(final QName tableName) {
    this(tableName, null, Collections.emptyList());
  }

  public Query(final QName tableName, final String query) {
    this(tableName, query, Collections.emptyList());
  }

  public Query(final QName tableName, final String query,
    final List<Object> parameters) {
    this.tableName = tableName;
    this.sql = query;
    if (parameters != null) {
      this.parameters.addAll(parameters);
    }
  }

  public Query(final QName typeName, final String query,
    final Object... parameters) {
    this(typeName, query, Arrays.asList(parameters));
  }

  protected void addAttributeName(final StringBuffer sql,
    final String tablePrefix, final Attribute attribute) {
    sql.append(attribute.getName());
  }

  private void addColumnNames(final StringBuffer sql,
    final DataObjectMetaData metaData, final String tablePrefix) {
    for (int i = 0; i < metaData.getAttributeCount(); i++) {
      if (i > 0) {
        sql.append(", ");
      }
      final Attribute attribute = metaData.getAttribute(i);
      addAttributeName(sql, tablePrefix, attribute);
    }
  }

  protected void addColumnNames(final StringBuffer sql,
    final DataObjectMetaData metaData, final String tablePrefix,
    final List<String> attributeNames) {
    for (int i = 0; i < attributeNames.size(); i++) {
      if (i > 0) {
        sql.append(", ");
      }
      final String attributeName = attributeNames.get(i);
      final Attribute attribute = metaData.getAttribute(attributeName);
      addAttributeName(sql, tablePrefix, attribute);
    }
  }

  public void addColumnsAndTableName(final StringBuffer sql,
    final DataObjectMetaData metaData, final String tablePrefix,
    final String where) {
    final QName typeName = metaData.getName();
    sql.append("SELECT ");
    addColumnNames(sql, metaData, tablePrefix);
    sql.append(" FROM ");
    final String tableName = JdbcUtils.getTableName(typeName);
    sql.append(tableName);
    sql.append(" ");
    sql.append(tablePrefix);
    if (where != null) {
      sql.append(" WHERE ");
      sql.append(where);
    }
  }

  public void addParameter(final Object value) {
    parameters.add(value);
    parameterAttributes.add(null);
  }

  public void addParameter(final Object value, final Attribute attribute) {
    addParameter(value);
    parameterAttributes.set(parameterAttributes.size() - 1, attribute);
  }

  public void addParameter(final String name, final Object value) {
    final Attribute attribute = metaData.getAttribute(name);
    addParameter(value, attribute);
  }

  public List<String> getAttributeNames() {
    return attributeNames;
  }

  public String getFromClause() {
    return fromClause;
  }

  public DataObjectMetaData getMetaData() {
    return metaData;
  }

  public List<String> getOrderBy() {
    return orderBy;
  }

  public List<Attribute> getParameterAttributes() {
    return parameterAttributes;
  }

  public int getParameterCount() {
    return parameters.size();
  }

  public List<Object> getParameters() {
    return parameters;
  }

  public String getSql() {
    return sql;
  }

  public QName getTableName() {
    return tableName;
  }

  public QName getTypeName() {
    return typeName;
  }

  public String getWhereClause() {
    return whereClause;
  }

  public void setAttributeNames(final List<String> attributeNames) {
    this.attributeNames = attributeNames;
  }

  public void setFromClause(final String fromClause) {
    this.fromClause = fromClause;
  }

  public void setMetaData(final DataObjectMetaData metaData) {
    this.metaData = metaData;
  }

  public void setOrderBy(final List<String> orderBy) {
    this.orderBy = orderBy;
  }

  public void setParameters(final List<Object> parameters) {
    this.parameters.clear();
    this.parameterAttributes.clear();
    for (final Object parameter : parameters) {
      addParameter(parameter);
    }
  }

  public void setParameters(final Object... parameters) {
    setParameters(Arrays.asList(parameters));
  }

  public void setOrderBy(final String... orderBy) {
    setOrderBy(Arrays.asList(orderBy));
  }

  public void setSql(final String sql) {
    this.sql = sql;
  }

  public void setTableName(final QName tableName) {
    this.tableName = tableName;
  }

  public void setTypeName(final QName typeName) {
    this.typeName = typeName;
  }

  public void setWhereClause(final String whereClause) {
    this.whereClause = whereClause;
  }

  @Override
  public String toString() {
    return getSql() + "\n" + getParameters();
  }
}
