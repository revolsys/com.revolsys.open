package com.revolsys.gis.jdbc.io;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.jdbc.attribute.JdbcAttribute;
import com.revolsys.jdbc.JdbcUtils;

public class JdbcQuery {
  public static void addColumnNames(final StringBuffer sql,
    final DataObjectMetaData metaData, final String tablePrefix) {
    for (int i = 0; i < metaData.getAttributeCount(); i++) {
      if (i > 0) {
        sql.append(", ");
      }
      final Attribute attribute = metaData.getAttribute(i);
      if (attribute instanceof JdbcAttribute) {
        final JdbcAttribute jdbcAttribute = (JdbcAttribute)attribute;
        jdbcAttribute.addColumnName(sql, tablePrefix);
      }
    }
  }

  public static void addColumnNames(final StringBuffer sql,
    final DataObjectMetaData metaData, final String tablePrefix,
    final List<String> attributeNames) {
    for (int i = 0; i < attributeNames.size(); i++) {
      if (i > 0) {
        sql.append(", ");
      }
      final String attributeName = attributeNames.get(i);
      final Attribute attribute = metaData.getAttribute(attributeName);
      if (attribute instanceof JdbcAttribute) {
        final JdbcAttribute jdbcAttribute = (JdbcAttribute)attribute;
        jdbcAttribute.addColumnName(sql, tablePrefix);
      }
    }
  }

  public static void addColumnsAndTableName(final StringBuffer sql,
    final DataObjectMetaData metaData, final String tablePrefix,
    final List<String> attributeNames, final String where) {
    final QName typeName = metaData.getName();
    sql.append("SELECT ");
    addColumnNames(sql, metaData, tablePrefix, attributeNames);
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

  public static void addColumnsAndTableName(final StringBuffer sql,
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

  public static JdbcQuery createQuery(final DataObjectMetaData metaData) {
    final String tablePrefix = "T";
    return createQuery(metaData, tablePrefix);
  }

  public static JdbcQuery createQuery(final DataObjectMetaData metaData,
    final List<String> attributeNames) {
    final String tablePrefix = "T";
    return createQuery(metaData, tablePrefix, attributeNames, null);
  }

  public static JdbcQuery createQuery(final DataObjectMetaData metaData,
    final String tablePrefix) {
    final StringBuffer sql = new StringBuffer();
    addColumnsAndTableName(sql, metaData, tablePrefix, null);
    return new JdbcQuery(metaData, sql.toString());
  }

  public static JdbcQuery createQuery(final DataObjectMetaData metaData,
    final String tablePrefix, final List<String> attributeNames,
    final String where) {
    final StringBuffer sql = new StringBuffer();
    addColumnsAndTableName(sql, metaData, tablePrefix, attributeNames, where);
    JdbcQuery jdbcQuery = new JdbcQuery(metaData, sql.toString());
    jdbcQuery.setAttributeNames(attributeNames);
    return jdbcQuery;
  }

  private List<String> attributeNames = Collections.emptyList();

  public List<String> getAttributeNames() {
    return attributeNames;
  }

  public void setAttributeNames(List<String> attributeNames) {
    this.attributeNames = attributeNames;
  }

  public static JdbcQuery createQuery(final DataObjectMetaData metaData,
    final List<String> attributeNames, final String where) {
    return createQuery(metaData, "T", attributeNames, where);
  }

  public static JdbcQuery createQuery(final DataObjectStore dataStore,
    final QName typeName) {
    final DataObjectMetaData metaData = dataStore.getMetaData(typeName);
    return createQuery(metaData);
  }

  public static JdbcQuery createQuery(final DataObjectStore dataStore,
    final QName typeName, final List<String> attributeNames) {
    final DataObjectMetaData metaData = dataStore.getMetaData(typeName);
    return createQuery(metaData, attributeNames);
  }

  public static JdbcQuery createQuery(final DataObjectStore dataStore,
    final QName typeName, final List<String> attributeNames, String where) {
    final DataObjectMetaData metaData = dataStore.getMetaData(typeName);
    return createQuery(metaData, attributeNames, where);
  }

  public static String getTableName(final QName typeName) {
    String tableName;
    final String namespaceURI = typeName.getNamespaceURI();
    if (namespaceURI != "") {
      tableName = namespaceURI + "." + typeName.getLocalPart();
    } else {
      tableName = typeName.getLocalPart();
    }
    return tableName;
  }

  private DataObjectMetaData metaData;

  private List<JdbcAttribute> parameterAttributes = new ArrayList<JdbcAttribute>();

  private List<Object> parameters = new ArrayList<Object>();

  private String sql;

  private QName tableName;

  private QName typeName;

  public JdbcQuery() {
  }

  public JdbcQuery(final DataObjectMetaData metaData) {
    this(metaData.getName());
    this.metaData = metaData;
  }

  public JdbcQuery(final DataObjectMetaData metaData, final String sql) {
    this(metaData.getName(), sql);
    this.metaData = metaData;
  }

  public JdbcQuery(final DataObjectMetaData metaData, final String sql,
    final List<Object> parameters) {
    this(metaData.getName(), sql, parameters);
    this.metaData = metaData;
  }

  public JdbcQuery(final DataObjectMetaData metaData, final String sql,
    final Object... parameters) {
    this(metaData.getName(), sql, parameters);
    this.metaData = metaData;
  }

  public JdbcQuery(final QName tableName) {
    this(tableName, null, null, null);
  }

  public JdbcQuery(final QName tableName, final String query) {
    this(tableName, query, null, null);
  }

  public JdbcQuery(final QName tableName, final String query,
    final List<? extends Object> parameters,
    final List<? extends JdbcAttribute> parameterAttributes) {
    this.tableName = tableName;
    this.sql = query;
    if (parameters != null) {
      this.parameters.addAll(parameters);
    }
    if (parameterAttributes != null) {
      this.parameterAttributes.addAll(parameterAttributes);
    }
    initParameterAttributes();
  }

  public JdbcQuery(final QName typeName, final String query,
    final List<Object> parameters) {
    this(typeName, query, parameters, null);
  }

  public JdbcQuery(final QName typeName, final String query,
    final Object... parameters) {
    this(typeName, query, Arrays.asList(parameters), null);
  }

  public void addParameter(final Object value) {
    parameters.add(value);
    parameterAttributes.add(new JdbcAttribute());
  }

  public void addParameter(final Object value, final JdbcAttribute attribute) {
    parameters.add(value);
    parameterAttributes.add(attribute);
  }

  public DataObjectMetaData getMetaData() {
    return metaData;
  }

  public List<JdbcAttribute> getParameterAttributes() {
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

  private void initParameterAttributes() {
    for (int i = 0; i < this.parameters.size(); i++) {
      if (i < this.parameterAttributes.size()) {
        if (this.parameterAttributes.get(i) == null) {
          this.parameterAttributes.set(i, new JdbcAttribute());
        }
      } else {
        this.parameterAttributes.add(new JdbcAttribute());
      }

    }
  }

  public void setMetaData(final DataObjectMetaData metaData) {
    this.metaData = metaData;
  }

  public void setParameterAttributes(
    final List<JdbcAttribute> parameterAttributes) {
    this.parameterAttributes = parameterAttributes;
    initParameterAttributes();
  }

  public void setParameters(final List<Object> parameters) {
    this.parameters = parameters;
    initParameterAttributes();
  }

  public void setPreparedStatementParameters(final PreparedStatement statement)
    throws SQLException {
    int statementParameterIndex = 1;
    for (int i = 0; i < parameters.size(); i++) {
      final JdbcAttribute attribute = parameterAttributes.get(i);
      final Object value = parameters.get(i);
      statementParameterIndex = attribute.setPreparedStatementValue(statement,
        statementParameterIndex, value);
    }
  }

  public void setSql(final String sql) {
    this.sql = sql;
  }

  public void setTableName(final QName tableName) {
    this.tableName = tableName;
  }

  public void setTableNameString(final String tableNameString) {
    this.tableName = QName.valueOf(tableNameString);
  }

  public void setTypeName(final QName typeName) {
    this.typeName = typeName;
  }

  public void setTypeNameString(final String typeNameString) {
    this.typeName = QName.valueOf(typeNameString);
  }

  @Override
  public String toString() {
    return getSql() + "\n" + getParameters();
  }
}
