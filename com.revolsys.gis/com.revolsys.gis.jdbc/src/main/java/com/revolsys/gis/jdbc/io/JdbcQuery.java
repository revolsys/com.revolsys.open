package com.revolsys.gis.jdbc.io;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.QName;

import com.revolsys.gis.jdbc.attribute.JdbcAttribute;

public class JdbcQuery {
  private List<JdbcAttribute> parameterAttributes = new ArrayList<JdbcAttribute>();

  private List<Object> parameters = new ArrayList<Object>();

  private String sql;

  private QName tableName;

  private QName typeName;

  public JdbcQuery() {
  }

  public JdbcQuery(
    final QName tableName) {
    this(tableName, null, null, null);
  }

  public JdbcQuery(
    final QName tableName,
    final String query) {
    this(tableName, query, null, null);
  }

  public JdbcQuery(
    final QName tableName,
    final String query,
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

  public JdbcQuery(
    final QName typeName,
    final String query,
    final List<Object> parameters) {
    this(typeName, query, parameters, null);
  }

  public JdbcQuery(
    final QName typeName,
    final String query,
    final Object... parameters) {
    this(typeName, query, Arrays.asList(parameters), null);
  }

  public void addParameter(
    final Object value) {
    parameters.add(value);
    parameterAttributes.add(new JdbcAttribute());
  }

  public void addParameter(
    final Object value,
    final JdbcAttribute attribute) {
    parameters.add(value);
    parameterAttributes.add(attribute);
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

  public void setParameterAttributes(
    final List<JdbcAttribute> parameterAttributes) {
    this.parameterAttributes = parameterAttributes;
    initParameterAttributes();
  }

  public void setParameters(
    final List<Object> parameters) {
    this.parameters = parameters;
    initParameterAttributes();
  }

  public void setPreparedStatementParameters(
    final PreparedStatement statement)
    throws SQLException {
    int statementParameterIndex = 1;
    for (int i = 0; i < parameters.size(); i++) {
      final JdbcAttribute attribute = parameterAttributes.get(i);
      final Object value = parameters.get(i);
      statementParameterIndex = attribute.setPreparedStatementValue(statement,
        statementParameterIndex, value);
    }
  }

  public void setSql(
    final String sql) {
    this.sql = sql;
  }

  public void setTableName(
    final QName tableName) {
    this.tableName = tableName;
  }

  public void setTableNameString(
    final String tableNameString) {
    this.tableName = QName.valueOf(tableNameString);
  }

  public void setTypeName(
    final QName typeName) {
    this.typeName = typeName;
  }

  public void setTypeNameString(
    final String typeNameString) {
    this.typeName = QName.valueOf(typeNameString);
  }
}
