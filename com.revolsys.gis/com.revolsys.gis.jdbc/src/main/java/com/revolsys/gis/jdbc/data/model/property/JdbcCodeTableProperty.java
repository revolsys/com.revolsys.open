package com.revolsys.gis.jdbc.data.model.property;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.codes.CodeTableProperty;
import com.revolsys.gis.jdbc.io.JdbcDataObjectStore;
import com.revolsys.jdbc.JdbcUtils;

public class JdbcCodeTableProperty extends CodeTableProperty {
  private static final Logger LOG = Logger.getLogger(JdbcCodeTableProperty.class);

  private Map<String, String> auditColumns = Collections.emptyMap();

  private DataSource dataSource;

  private JdbcDataObjectStore dataStore;

  private boolean initialized;

  private String insertSql;

  private String tableName;

  @Override
  public JdbcCodeTableProperty clone() {
    return this;
  }

  @Override
  public void setMetaData(DataObjectMetaData metaData) {
    super.setMetaData(metaData);
    dataStore = (JdbcDataObjectStore)metaData.getDataObjectStore();
    dataSource = dataStore.getDataSource();
  }

  protected synchronized Object createId(final List<Object> values) {
    try {
      init();
      final Connection connection = JdbcUtils.getConnection(dataSource);
      try {
        JdbcUtils.lockTable(connection, tableName);
        Object id = loadId(values, false);
        if (id == null) {
          final PreparedStatement statement = connection.prepareStatement(insertSql);
          try {
            id = dataStore.getNextPrimaryKey(getMetaData());
            statement.setObject(1, id);
            for (int i = 0; i < getValueAttributeNames().size(); i++) {
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

  public DataSource getDataSource() {
    return dataSource;
  }

  public JdbcDataObjectStore getDataStore() {
    return dataStore;
  }

  public String getTableName() {
    return tableName;
  }

  @PostConstruct
  protected synchronized void init() {
    if (!initialized) {
      DataObjectMetaData metaData = getMetaData();
      if (metaData != null) {
        this.tableName = JdbcUtils.getTableName(metaData.getName());

        List<String> valueAttributeNames = getValueAttributeNames();
        final String idColumn = metaData.getIdAttributeName();
        this.insertSql = "INSERT INTO " + tableName + " (" + idColumn;
        for (int i = 0; i < valueAttributeNames.size(); i++) {
          final String columnName = valueAttributeNames.get(i);
          this.insertSql += ", " + columnName;
        }
        for (final Entry<String, String> auditColumn : auditColumns.entrySet()) {
          insertSql += ", " + auditColumn.getKey();
        }
        insertSql += ") VALUES (?";
        for (int i = 0; i < valueAttributeNames.size(); i++) {
          insertSql += ", ?";
        }
        for (final Entry<String, String> auditColumn : auditColumns.entrySet()) {
          insertSql += ", " + auditColumn.getValue();
        }
        insertSql += ")";
      }
    }
    initialized = true;
  }

  public void setAuditColumns(final boolean useAuditColumns) {
    auditColumns = new HashMap<String, String>();
    auditColumns.put("WHO_CREATED", "USER");
    auditColumns.put("WHEN_CREATED", "SYSDATE");
    auditColumns.put("WHO_UPDATED", "USER");
    auditColumns.put("WHEN_UPDATED", "SYSDATE");
  }

  public void setAuditColumns(final Map<String, String> auditColumns) {
    this.auditColumns = auditColumns;
  }

  public String toString(final List<String> values) {
    final StringBuffer string = new StringBuffer(values.get(0));
    for (int i = 1; i < values.size(); i++) {
      final String value = values.get(i);
      string.append(",");
      string.append(value);
    }
    return string.toString();
  }
}
