package com.revolsys.gis.data.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.util.StringUtils;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.util.CollectionUtil;
import com.vividsolutions.jts.geom.Geometry;

public class Query implements Cloneable {
  private List<String> attributeNames = Collections.emptyList();

  private BoundingBox boundingBox;

  private final Map<String, Object> filter = new LinkedHashMap<String, Object>();

  private String fromClause;

  private Geometry geometry;

  private boolean lockResults = false;

  private DataObjectMetaData metaData;

  private Map<String, Boolean> orderBy = new HashMap<String, Boolean>();

  private List<Attribute> parameterAttributes = new ArrayList<Attribute>();

  private List<Object> parameters = new ArrayList<Object>();

  private String sql;

  private String typeName;

  private String typePathAlias;

  private String whereClause;

  public Query() {
  }

  public Query(final DataObjectMetaData metaData) {
    this(metaData.getPath());
    this.metaData = metaData;
  }

  public Query(final DataObjectMetaData metaData, final String sql) {
    this(metaData.getPath(), sql);
    this.metaData = metaData;
  }

  public Query(final DataObjectMetaData metaData, final String sql,
    final List<Object> parameters) {
    this(metaData.getPath(), sql, parameters);
    this.metaData = metaData;
  }

  public Query(final DataObjectMetaData metaData, final String sql,
    final Object... parameters) {
    this(metaData.getPath(), sql, Arrays.asList(parameters));
    this.metaData = metaData;
  }

  public Query(final DataObjectStore dataStore, final String path) {
    this(dataStore.getMetaData(path));
  }

  public Query(final String typePath) {
    this(typePath, null, Collections.emptyList());
  }

  public Query(final String typePath, final String query) {
    this(typePath, query, Collections.emptyList());
  }

  public Query(final String typePath, final String query,
    final List<Object> parameters) {
    this.typeName = typePath;
    this.sql = query;
    if (parameters != null) {
      this.parameters.addAll(parameters);
    }
  }

  public Query(final String typePath, final String query,
    final Object... parameters) {
    this(typePath, query, Arrays.asList(parameters));
  }

  public void addFilter(final String name, final Object value) {
    filter.put(name, value);
  }

  public void addOrderBy(final String column, final boolean ascending) {
    orderBy.put(column, ascending);
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

  public void addParameters(final List<Object> parameters) {
    for (final Object parameter : parameters) {
      addParameter(parameter);
    }
  }

  public void addParameters(final Object... parameters) {
    addParameters(Arrays.asList(parameters));
  }

  @Override
  public Query clone() {
    try {
      final Query clone = (Query)super.clone();
      clone.parameterAttributes = new ArrayList<Attribute>(parameterAttributes);
      clone.attributeNames = new ArrayList<String>(clone.attributeNames);
      clone.parameters = new ArrayList<Object>(parameters);
      clone.orderBy = new HashMap<String, Boolean>(orderBy);
      return clone;
    } catch (final CloneNotSupportedException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  public List<String> getAttributeNames() {
    return attributeNames;
  }

  public BoundingBox getBoundingBox() {
    return boundingBox;
  }

  public Map<String, Object> getFilter() {
    return filter;
  }

  public String getFromClause() {
    return fromClause;
  }

  public Geometry getGeometry() {
    return geometry;
  }

  public DataObjectMetaData getMetaData() {
    return metaData;
  }

  public Map<String, Boolean> getOrderBy() {
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

  public String getTypeName() {
    return typeName;
  }

  public String getTypeNameAlias() {
    return typePathAlias;
  }

  public String getWhereClause() {
    return whereClause;
  }

  public boolean isLockResults() {
    return lockResults;
  }

  public void setAttributeNames(final List<String> attributeNames) {
    this.attributeNames = attributeNames;
  }

  public void setAttributeNames(final String... attributeNames) {
    setAttributeNames(Arrays.asList(attributeNames));
  }

  public void setBoundingBox(final BoundingBox boundingBox) {
    this.boundingBox = boundingBox;
  }

  public void setFilter(final Map<String, ? extends Object> filter) {
    this.filter.clear();
    if (filter != null) {
      this.filter.putAll(filter);
    }
  }

  public void setFromClause(final String fromClause) {
    this.fromClause = fromClause;
  }

  public void setGeometry(final Geometry geometry) {
    this.geometry = geometry;
  }

  public void setLockResults(final boolean lockResults) {
    this.lockResults = lockResults;
  }

  public void setMetaData(final DataObjectMetaData metaData) {
    this.metaData = metaData;
  }

  public void setOrderBy(final Map<String, Boolean> orderBy) {
    this.orderBy = orderBy;
  }

  public void setOrderByColumns(final List<String> orderBy) {
    this.orderBy.clear();
    for (final String column : orderBy) {
      this.orderBy.put(column, Boolean.TRUE);
    }
  }

  public void setOrderByColumns(final String... orderBy) {
    setOrderByColumns(Arrays.asList(orderBy));
  }

  public void setParameters(final List<Object> parameters) {
    this.parameters.clear();
    this.parameterAttributes.clear();
    addParameters(parameters);
  }

  public void setParameters(final Object... parameters) {
    setParameters(Arrays.asList(parameters));
  }

  public void setSql(final String sql) {
    this.sql = sql;
  }

  public void setTypeName(final String typeName) {
    this.typeName = typeName;
  }

  public void setTypeNameAlias(final String typePathAlias) {
    this.typePathAlias = typePathAlias;
  }

  public void setWhereClause(final String whereClause) {
    this.whereClause = whereClause;
  }

  @Override
  public String toString() {
    final StringBuffer string = new StringBuffer();
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
          string.append(JdbcUtils.getQualifiedTableName(typeName));
        } else if (metaData != null) {
          string.append(JdbcUtils.getQualifiedTableName(metaData.getPath()));
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
        for (final Iterator<Entry<String, Boolean>> iterator = orderBy.entrySet()
          .iterator(); iterator.hasNext();) {
          final Entry<String, Boolean> entry = iterator.next();
          final String column = entry.getKey();
          string.append(column);
          final Boolean ascending = entry.getValue();
          if (!ascending) {
            string.append(" DESC");
          }
          if (iterator.hasNext()) {
            string.append(", ");
          }
        }
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
}
