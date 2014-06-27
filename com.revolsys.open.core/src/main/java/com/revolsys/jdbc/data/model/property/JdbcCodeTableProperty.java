package com.revolsys.jdbc.data.model.property;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.RecordIdentifier;
import com.revolsys.gis.data.model.SingleRecordIdentifier;
import com.revolsys.gis.data.model.codes.CodeTableProperty;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.io.JdbcDataObjectStore;

public class JdbcCodeTableProperty extends CodeTableProperty {

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
  protected synchronized RecordIdentifier createId(final List<Object> values) {
    try {
      final Connection connection = JdbcUtils.getConnection(this.dataSource);
      try {
        RecordIdentifier id = loadId(values, false);
        boolean retry = true;
        while (id == null) {
          final PreparedStatement statement = connection.prepareStatement(this.insertSql);
          try {
            id = SingleRecordIdentifier.create(
              this.dataStore.getNextPrimaryKey(getMetaData()));
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
              throw new RuntimeException(this.tableName
                + ": Unable to create ID for  " + values, e);
            }
          } finally {
            JdbcUtils.close(statement);
          }
        }
        return id;

      } finally {
        JdbcUtils.release(connection, this.dataSource);
      }

    } catch (final SQLException e) {
      throw new RuntimeException(this.tableName + ": Unable to create ID for  "
          + values, e);
    }

  }

  public DataSource getDataSource() {
    return this.dataSource;
  }

  @Override
  public JdbcDataObjectStore getDataStore() {
    return this.dataStore;
  }

  @Override
  public void setMetaData(final DataObjectMetaData metaData) {
    super.setMetaData(metaData);
    this.dataStore = (JdbcDataObjectStore)metaData.getDataStore();
    this.dataSource = this.dataStore.getDataSource();
    if (metaData != null) {
      this.tableName = JdbcUtils.getQualifiedTableName(metaData.getPath());

      final List<String> valueAttributeNames = getValueAttributeNames();
      String idColumn = metaData.getIdAttributeName();
      if (!StringUtils.hasText(idColumn)) {
        idColumn = metaData.getAttributeName(0);
      }
      this.insertSql = "INSERT INTO " + this.tableName + " (" + idColumn;
      for (int i = 0; i < valueAttributeNames.size(); i++) {
        final String columnName = valueAttributeNames.get(i);
        this.insertSql += ", " + columnName;
      }
      if (this.useAuditColumns) {
        this.insertSql += ", WHO_CREATED, WHEN_CREATED, WHO_UPDATED, WHEN_UPDATED";
      }
      this.insertSql += ") VALUES (?";
      for (int i = 0; i < valueAttributeNames.size(); i++) {
        this.insertSql += ", ?";
      }
      if (this.useAuditColumns) {
        if (this.dataStore.getClass()
            .getName()
            .equals("com.revolsys.gis.oracle.io.OracleDataObjectStore")) {
          this.insertSql += ", USER, SYSDATE, USER, SYSDATE";
        } else {
          this.insertSql += ", current_user, current_timestamp, current_user, current_timestamp";
        }
      }
      this.insertSql += ")";
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
