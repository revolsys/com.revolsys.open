package com.revolsys.gis.jdbc.data.model.property;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataProperty;
import com.revolsys.gis.data.model.codes.AbstractCodeTable;
import com.revolsys.gis.jdbc.io.JdbcDataObjectStore;
import com.revolsys.gis.jdbc.io.JdbcQuery;
import com.revolsys.jdbc.JdbcUtils;

public class JdbcCodeTableProperty extends AbstractCodeTable implements
  DataObjectMetaDataProperty {
  private static final Logger LOG = Logger.getLogger(JdbcCodeTableProperty.class);

  public static final String PROPERTY_NAME = JdbcCodeTableProperty.class.getName();

  public static final JdbcCodeTableProperty getProperty(
    final DataObjectMetaData metaData) {
    final JdbcCodeTableProperty property = metaData.getProperty(PROPERTY_NAME);
    return property;
  }

  private String allSql;

  private Map<String, String> auditColumns = Collections.emptyMap();

  private List<String> columnAliases = new ArrayList<String>();

  private DataSource dataSource;

  private JdbcDataObjectStore dataStore;

  private String idByValueSql;

  private boolean initialized;

  private String insertSql;

  private boolean loadAll;

  private DataObjectMetaData metaData;

  private String tableName;

  private String valueByIdSql;

  private final List<String> valueColumns = new ArrayList<String>();

  public JdbcCodeTableProperty() {
  }

  @Override
  public JdbcCodeTableProperty clone() {
    return this;
  }

  protected synchronized Number createId(
    final List<Object> values) {
    try {
      init();
      final Connection connection = JdbcUtils.getConnection(dataSource);
      try {
        JdbcUtils.lockTable(connection, tableName);
        Number id = loadId(values, false);
        if (id == null) {
          final PreparedStatement statement = connection.prepareStatement(insertSql);
          try {
            id = dataStore.getNextPrimaryKey(metaData);
            statement.setObject(1, id);
            for (int i = 0; i < valueColumns.size(); i++) {
              final Object value = values.get(i);
              statement.setObject(i + 2, value);
            }
            if (statement.executeUpdate() > 0) {
              return id;
            } else {
              return null;
            }
          } finally {
            JdbcUtils.close(statement);
          }
        } else {
          return id;
        }

      } finally {
        try {
          connection.commit();
        } catch (final SQLException e) {
          LOG.error(e.getMessage(), e);
        }
        JdbcUtils.close(connection);
      }

    } catch (final SQLException e) {
      throw new RuntimeException(tableName + ": Unable to create ID for  "
        + values, e);
    }

  }

  public Map<String, String> getAuditColumns() {
    return auditColumns;
  }

  public List<String> getColumnAliases() {
    return columnAliases;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public JdbcDataObjectStore getDataStore() {
    return dataStore;
  }

  @Override
  public Number getId(
    final Map<String, ? extends Object> valueMap) {
    final Object[] values = new Object[valueColumns.size()];
    for (int i = 0; i < values.length; i++) {
      final String name = valueColumns.get(i);
      final Object value = valueMap.get(name);
      values[i] = value;
    }
    return getId(values);
  }

  public String getIdColumn() {
    return metaData.getIdAttributeName();
  }

  @Override
  public Map<String, ? extends Object> getMap(
    final Number id) {
    final List<Object> values = getValues(id);
    if (values == null) {
      return Collections.emptyMap();
    } else {
      final Map<String, Object> map = new HashMap<String, Object>();
      for (int i = 0; i < values.size(); i++) {
        final String name = valueColumns.get(i);
        final Object value = values.get(i);
        map.put(name, value);
      }
      return map;
    }
  }

  public DataObjectMetaData getMetaData() {
    return metaData;
  }

  public String getPropertyName() {
    return PROPERTY_NAME;
  }

  public String getTableName() {
    return tableName;
  }

  public List<String> getValueColumns() {
    return valueColumns;
  }

  protected synchronized void init() {
    if (!initialized) {
      final String idColumn = getMetaData().getIdAttributeName();
      this.allSql = "SELECT " + idColumn + ", " + toString(valueColumns)
        + " FROM " + tableName;
      this.valueByIdSql = "SELECT " + toString(valueColumns) + " FROM "
        + tableName + " WHERE " + idColumn + " = ?";
      this.idByValueSql = "SELECT " + idColumn + " FROM " + tableName
        + " WHERE ";
      this.insertSql = "INSERT INTO " + tableName + " (" + idColumn;
      for (int i = 0; i < valueColumns.size(); i++) {
        final String columnName = valueColumns.get(i);
        if (i > 0) {
          this.idByValueSql += " AND ";
        }
        this.idByValueSql += columnName + " = ?";
        this.insertSql += ", " + columnName;
      }
      for (final Entry<String, String> auditColumn : auditColumns.entrySet()) {
        insertSql += ", " + auditColumn.getKey();
      }
      insertSql += ") VALUES (?";
      for (int i = 0; i < valueColumns.size(); i++) {
        insertSql += ", ?";
      }
      for (final Entry<String, String> auditColumn : auditColumns.entrySet()) {
        insertSql += ", " + auditColumn.getValue();
      }
      insertSql += ")";
    }

    initialized = true;
  }

  public boolean isLoadAll() {
    return loadAll;
  }

  private void loadAll() {
    try {
      final Connection connection = JdbcUtils.getConnection(dataSource);

      try {
        final PreparedStatement statement = connection.prepareStatement(allSql);
        try {
          final ResultSet rs = statement.executeQuery();
          try {
            final Map<Number, List<Object>> valueMap = new LinkedHashMap<Number, List<Object>>();
            while (rs.next()) {
              final Number id = rs.getLong(1);
              final List<Object> values = new ArrayList<Object>();
              for (int i = 0; i < valueColumns.size(); i++) {
                values.add(rs.getObject(2 + i));
              }
              valueMap.put(id, values);
            }
            addValues(valueMap);
          } finally {
            rs.close();
          }
        } finally {
          JdbcUtils.close(statement);
        }
      } finally {
        JdbcUtils.close(connection);
      }
    } catch (final SQLException e) {
      throw new RuntimeException("Unable to load all values for: " + tableName,
        e);
    }
  }

  @Override
  protected synchronized Number loadId(
    final List<Object> values,
    final boolean createId) {
    init();
    Number id = null;
    if (createId && loadAll) {
      loadAll();
      id = getIdByValue(values);
    } else {
      try {
        final Connection connection = JdbcUtils.getConnection(dataSource);
        try {
          final PreparedStatement statement = connection.prepareStatement(idByValueSql);
          try {
            for (int i = 0; i < valueColumns.size(); i++) {
              final Object value = values.get(i);
              statement.setObject(i + 1, value);
            }
            final ResultSet resultSet = statement.executeQuery();
            try {
              if (resultSet.next()) {
                id = resultSet.getLong(1);
              }
            } finally {
              JdbcUtils.close(resultSet);
            }
          } finally {
            JdbcUtils.close(statement);
          }
        } finally {
          JdbcUtils.close(connection);
        }
      } catch (final SQLException e) {
        throw new RuntimeException(tableName + ": Unable to load ID: ", e);
      }
    }
    if (createId && id == null) {
      return createId(values);
    } else {
      return id;
    }
  }

  @Override
  protected List<Object> loadValues(
    final Number id) {
    init();
    List<Object> values = null;
    if (loadAll) {
      loadAll();
      values = getValueById(id);
    } else {
      try {
        final Connection connection = JdbcUtils.getConnection(dataSource);
        try {
          final PreparedStatement statement = connection.prepareStatement(valueByIdSql);
          try {
            statement.setLong(1, id.longValue());
            final ResultSet rs = statement.executeQuery();
            try {
              if (rs.next()) {
                final int numColumns = rs.getMetaData().getColumnCount();
                values = new ArrayList<Object>();
                for (int i = 0; i < numColumns; i++) {
                  values.add(rs.getObject(i + 1));
                }
              }
            } finally {
              JdbcUtils.close(rs);
            }
          } finally {
            JdbcUtils.close(statement);
          }
        } finally {
          JdbcUtils.close(connection);
        }
      } catch (final SQLException e) {
        throw new IllegalArgumentException(tableName + " " + id
          + " does not exist", e);
      }
    }
    return values;
  }

  public void setAuditColumns(
    final boolean useAuditColumns) {
    auditColumns = new HashMap<String, String>();
    auditColumns.put("WHO_CREATED", "USER");
    auditColumns.put("WHEN_CREATED", "SYSDATE");
    auditColumns.put("WHO_UPDATED", "USER");
    auditColumns.put("WHEN_UPDATED", "SYSDATE");
  }

  public void setAuditColumns(
    final Map<String, String> auditColumns) {
    this.auditColumns = auditColumns;
  }

  public void setColumnAliases(
    final List<String> columnAliases) {
    this.columnAliases = columnAliases;
  }

  public void setLoadAll(
    final boolean loadAll) {
    this.loadAll = loadAll;
  }

  public void setMetaData(
    final DataObjectMetaData metaData) {
    if (this.metaData != null) {
      this.metaData.setProperty(getPropertyName(), null);
      this.dataStore = null;
      this.dataSource = null;
    }
    this.metaData = metaData;
    if (metaData != null) {
      metaData.setProperty(getPropertyName(), this);
      this.dataStore = (JdbcDataObjectStore)this.metaData.getDataObjectStore();
      dataStore.addCodeTable(this);
      this.dataSource = dataStore.getDataSource();
      this.tableName = JdbcQuery.getTableName(metaData.getName());
    }
  }

  public void setValueColumn(
    final String valueColumn) {
    this.valueColumns.clear();
    this.valueColumns.add(valueColumn);
  }

  public void setValueColumns(
    final List<String> valueColumns) {
    this.valueColumns.clear();
    for (final String column : valueColumns) {
      this.valueColumns.add(column);
    }
  }

  public void setValueColumns(
    final String... valueColumns) {
    this.valueColumns.clear();
    for (final String column : valueColumns) {
      this.valueColumns.add(column);
    }
  }

  @Override
  public String toString() {
    return tableName + " " + getIdColumn() + " " + valueColumns;

  }

  public String toString(
    final List<String> values) {
    final StringBuffer string = new StringBuffer(values.get(0));
    for (int i = 1; i < values.size(); i++) {
      final String value = values.get(i);
      string.append(",");
      string.append(value);
    }
    return string.toString();
  }
}
