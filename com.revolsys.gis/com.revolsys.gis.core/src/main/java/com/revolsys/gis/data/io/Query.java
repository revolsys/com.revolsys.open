package com.revolsys.gis.data.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.util.CollectionUtil;

public class Query {
  private final List<Attribute> parameterAttributes = new ArrayList<Attribute>();

  private List<String> attributeNames = Collections.emptyList();

  private DataObjectMetaData metaData;

  private final List<Object> parameters = new ArrayList<Object>();

  private String sql;

  private QName typeName;

  private QName typeNameAlias;

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

  public Query(final QName typeName) {
    this(typeName, null, Collections.emptyList());
  }

  public Query(final QName typeName, final String query) {
    this(typeName, query, Collections.emptyList());
  }

  public Query(final QName typeName, final String query,
    final List<Object> parameters) {
    this.typeName = typeName;
    this.sql = query;
    if (parameters != null) {
      this.parameters.addAll(parameters);
    }
  }

  public Query(final QName typeName, final String query,
    final Object... parameters) {
    this(typeName, query, Arrays.asList(parameters));
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

  public QName getTypeName() {
    return typeName;
  }

  public QName getTypeNameAlias() {
    return typeNameAlias;
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

  public void setTypeName(final QName typeName) {
    this.typeName = typeName;
  }

  public void setTypeNameAlias(final QName typeNameAlias) {
    this.typeNameAlias = typeNameAlias;
  }

  public void setWhereClause(final String whereClause) {
    this.whereClause = whereClause;
  }

  @Override
  public String toString() {
    StringBuffer string = new StringBuffer();
    if (sql == null) {
      string.append("SELECT ");
      if (attributeNames.isEmpty()) {
        string.append("*");
      } else {
        CollectionUtil.append(string, attributeNames, ", ");
      }
      string.append(" FROM ");
      if (fromClause == null) {
        if (typeName != null) {
          string.append(JdbcUtils.getTableName(typeName));
        } else if (metaData != null) {
          string.append(JdbcUtils.getTableName(metaData.getName()));
        }
      } else {
        string.append(fromClause);
      }
      if (StringUtils.hasText(whereClause)) {
        string.append(" WHERE ");
        string.append(whereClause);
      }
      if (!orderBy.isEmpty()) {
        string.append(" ORDER BY ");
        CollectionUtil.append(string, orderBy, ", ");
      }
    } else {
      string.append(sql);
    }
    if (!parameters.isEmpty()) {
      string.append(" ");
      string.append(parameters);
    }
    return string.toString();
  }

  public void setAttributeNames(String... attributeNames) {
    setAttributeNames(Arrays.asList(attributeNames));
  }
}
