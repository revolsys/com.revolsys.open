package com.revolsys.jdbc.data.model.property;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.codes.CodeTableProperty;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.io.JdbcDataObjectStore;

public class JdbcCodeTableProperty extends CodeTableProperty {
  private static final Logger LOG = Logger.getLogger(JdbcCodeTableProperty.class);

  private DataSource dataSource;

  private JdbcDataObjectStore dataStore;

  private String insertSql;

  private String tableName;

  private boolean useAuditColumns;

  @Override
  public JdbcCodeTableProperty clone() {
    return this;
  }

  @Override
  protected synchronized Object createId(final List<Object> values) {
    try {
      final Connection connection = JdbcUtils.getConnection(dataSource);
      try {
        Object id = loadId(values, false);
        boolean retry = true;
        while (id == null) {
          final PreparedStatement statement = connection.prepareStatement(insertSql);
          try {
            id = dataStore.getNextPrimaryKey(getMetaData());
            int index = 1;
            index = JdbcUtils.setValue(statement, index, id);
            for (int i = 0; i < getValueAttributeNames().size(); i++) {
              final Object value = values.get(i);
              index = JdbcUtils.setValue(statement, index, value);
            }
            if (statement.executeUpdate() > 0) {
              return id;
            }
          } catch (final SQLException e) {
            if (retry) {
              retry = false;
              id = loadId(values, false);
            } else {
              throw new RuntimeException(tableName
                + ": Unable to create ID for  " + values, e);
            }
          } finally {
            JdbcUtils.close(statement);
          }
        }
        return id;

      } finally {
        JdbcUtils.release(connection, dataSource);
      }

    } catch (final SQLException e) {
      throw new RuntimeException(tableName + ": Unable to create ID for  "
        + values, e);
    }

  }

  public DataSource getDataSource() {
    return dataSource;
  }

  @Override
  public JdbcDataObjectStore getDataStore() {
    return dataStore;
  }

  @Override
  public void setMetaData(final DataObjectMetaData metaData) {
    super.setMetaData(metaData);
    dataStore = (JdbcDataObjectStore)metaData.getDataObjectStore();
    dataSource = dataStore.getDataSource();
    if (metaData != null) {
      this.tableName = JdbcUtils.getQualifiedTableName(metaData.getPath());

      final List<String> valueAttributeNames = getValueAttributeNames();
      String idColumn = metaData.getIdAttributeName();
      if (!StringUtils.hasText(idColumn)) {
        idColumn = metaData.getAttributeName(0);
      }
      this.insertSql = "INSERT INTO " + tableName + " (" + idColumn;
      for (int i = 0; i < valueAttributeNames.size(); i++) {
        final String columnName = valueAttributeNames.get(i);
        this.insertSql += ", " + columnName;
      }
      if (useAuditColumns) {
        insertSql += ", WHO_CREATED, WHEN_CREATED, WHO_UPDATED, WHEN_UPDATED";
      }
      insertSql += ") VALUES (?";
      for (int i = 0; i < valueAttributeNames.size(); i++) {
        insertSql += ", ?";
      }
      if (useAuditColumns) {
        if (dataStore.getClass()
          .getName()
          .equals("com.revolsys.gis.oracle.io.OracleDataObjectStore")) {
          insertSql += ", USER, SYSDATE, USER, SYSDATE";
        } else {
          insertSql += ", current_user, current_timestamp, current_user, current_timestamp";
        }
      }
      insertSql += ")";
    }
  }

  public void setUseAuditColumns(final boolean useAuditColumns) {
    this.useAuditColumns = useAuditColumns;
  }

  @Override
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
