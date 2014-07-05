package com.revolsys.jdbc.io;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.revolsys.data.codes.AbstractCodeTable;
import com.revolsys.data.identifier.Identifier;
import com.revolsys.data.identifier.SingleIdentifier;
import com.revolsys.jdbc.JdbcUtils;

@Deprecated
public class JdbcCodeTable extends AbstractCodeTable {
  private static final Logger LOG = Logger.getLogger(JdbcCodeTable.class);

  private String allSql;

  private Map<String, String> auditColumns = Collections.emptyMap();

  private List<String> columnAliases = new ArrayList<String>();

  private DataSource dataSource;

  private JdbcRecordStore dataStore;

  private String idByValueSql;

  private String idColumn;

  private boolean initialized;

  private String insertSql;

  private boolean loadAll;

  private String sequenceName;

  private String tableName;

  private String valueByIdSql;

  private final List<String> valueColumns = new ArrayList<String>();

  public JdbcCodeTable() {
  }

  public JdbcCodeTable(final String tableName, final String sequenceName,
    final String idColumn, final boolean capitalizeWords,
    final boolean loadAll, final String... valueColumns) {
    super(capitalizeWords);
    this.tableName = tableName;
    this.sequenceName = sequenceName;
    this.idColumn = idColumn;
    setValueColumns(valueColumns);
    this.loadAll = loadAll;
  }

  public JdbcCodeTable(final String tableName, final String sequenceName,
    final String idColumn, final List<String> columnAliases,
    final boolean capitalizeWords, final boolean loadAll,
    final String... valueColumns) {
    super(capitalizeWords);
    this.tableName = tableName;
    this.sequenceName = sequenceName;
    this.idColumn = idColumn;
    this.columnAliases = columnAliases;
    setValueColumns(valueColumns);
    this.loadAll = loadAll;
  }

  public JdbcCodeTable(final String tableName, final String sequenceName,
    final String idColumn, final Map<String, String> auditColumns,
    final boolean capitalizeWords, final boolean loadAll,
    final List<String> valueColumns) {
    super(capitalizeWords);
    this.tableName = tableName;
    this.sequenceName = sequenceName;
    this.idColumn = idColumn;
    setValueColumns(valueColumns);
    this.auditColumns = auditColumns;
    this.loadAll = loadAll;
  }

  public JdbcCodeTable(final String tableName, final String sequenceName,
    final String idColumn, final String... valueColumns) {
    this.tableName = tableName;
    this.idColumn = idColumn;
    this.sequenceName = sequenceName;
    setValueColumns(valueColumns);
  }

  @Override
  public AbstractCodeTable clone() {
    final JdbcCodeTable codeTable = new JdbcCodeTable(this.tableName,
      this.sequenceName, this.idColumn, this.auditColumns, isCapitalizeWords(),
      this.loadAll, this.valueColumns);
    codeTable.setColumnAliases(this.columnAliases);
    return codeTable;
  }

  protected Identifier createId(final List<Object> values) {
    try {
      init();
      final Connection connection = JdbcUtils.getConnection(this.dataSource);
      try {
        JdbcUtils.lockTable(connection, this.tableName);
        Object id = loadId(values, false);
        if (id == null) {
          final PreparedStatement statement = connection.prepareStatement(this.insertSql);
          try {
            id = this.dataStore.getNextPrimaryKey(this.sequenceName);
            int index = 1;
            index = JdbcUtils.setValue(statement, index, id);
            for (int i = 0; i < this.valueColumns.size(); i++) {
              final Object value = values.get(i);
              index = JdbcUtils.setValue(statement, index, value);
            }
            if (statement.executeUpdate() > 0) {
              return SingleIdentifier.create(id);
            } else {
              return null;
            }
          } finally {
            JdbcUtils.close(statement);
          }
        } else {
          return SingleIdentifier.create(id);
        }

      } finally {
        try {
          connection.commit();
        } catch (final SQLException e) {
          LOG.error(e.getMessage(), e);
        }
        JdbcUtils.release(connection, this.dataSource);
      }

    } catch (final SQLException e) {
      throw new RuntimeException(this.tableName + ": Unable to create ID for  "
        + values, e);
    }

  }

  public Map<String, String> getAuditColumns() {
    return this.auditColumns;
  }

  public List<String> getColumnAliases() {
    return this.columnAliases;
  }

  public DataSource getDataSource() {
    return this.dataSource;
  }

  public JdbcRecordStore getDataStore() {
    return this.dataStore;
  }

  @Override
  public String getIdAttributeName() {
    return this.idColumn;
  }

  public String getSequenceName() {
    return this.sequenceName;
  }

  public String getTableName() {
    return this.tableName;
  }

  public List<String> getValueColumns() {
    return this.valueColumns;
  }

  protected synchronized void init() {
    if (!this.initialized) {

      this.allSql = "SELECT " + this.idColumn + ", "
          + toString(this.valueColumns) + " FROM " + this.tableName;
      this.valueByIdSql = "SELECT " + toString(this.valueColumns) + " FROM "
        + this.tableName + " WHERE " + this.idColumn + " = ?";
      this.idByValueSql = "SELECT " + this.idColumn + " FROM " + this.tableName
        + " WHERE ";
      this.insertSql = "INSERT INTO " + this.tableName + " (" + this.idColumn;
      for (int i = 0; i < this.valueColumns.size(); i++) {
        final String columnName = this.valueColumns.get(i);
        if (i > 0) {
          this.idByValueSql += " AND ";
        }
        this.idByValueSql += columnName + " = ?";
        this.insertSql += ", " + columnName;
      }
      for (final Entry<String, String> auditColumn : this.auditColumns.entrySet()) {
        this.insertSql += ", " + auditColumn.getKey();
      }
      this.insertSql += ") VALUES (?";
      for (int i = 0; i < this.valueColumns.size(); i++) {
        this.insertSql += ", ?";
      }
      for (final Entry<String, String> auditColumn : this.auditColumns.entrySet()) {
        this.insertSql += ", " + auditColumn.getValue();
      }
      this.insertSql += ")";
    }

    this.initialized = true;
  }

  public boolean isLoadAll() {
    return this.loadAll;
  }

  private void loadAll() {
    try {
      final Connection connection = JdbcUtils.getConnection(this.dataSource);

      try {
        final PreparedStatement statement = connection.prepareStatement(this.allSql);
        try {
          final ResultSet rs = statement.executeQuery();
          try {
            while (rs.next()) {
              final Identifier id = SingleIdentifier.create(
                rs.getLong(1));
              final List<Object> values = new ArrayList<Object>();
              for (int i = 0; i < this.valueColumns.size(); i++) {
                values.add(rs.getObject(2 + i));
              }
              addValue(id, values);
            }
          } finally {
            rs.close();
          }
        } finally {
          JdbcUtils.close(statement);
        }
      } finally {
        JdbcUtils.release(connection, this.dataSource);
      }
    } catch (final SQLException e) {
      throw new RuntimeException("Unable to load all values for: "
          + this.tableName, e);
    }
  }

  @Override
  protected Identifier loadId(final List<Object> values,
    final boolean createId) {
    init();
    Identifier id = null;
    if (createId && this.loadAll) {
      loadAll();
      id = getIdByValue(values);
    } else {
      try {
        final Connection connection = JdbcUtils.getConnection(this.dataSource);
        try {
          final PreparedStatement statement = connection.prepareStatement(this.idByValueSql);
          try {
            int index = 1;
            for (int i = 0; i < this.valueColumns.size(); i++) {
              final Object value = values.get(i);
              index = JdbcUtils.setValue(statement, index, value);
            }
            try (
              final ResultSet resultSet = statement.executeQuery()) {
              if (resultSet.next()) {
                id = SingleIdentifier.create(resultSet.getLong(1));
              }
            }
          } finally {
            JdbcUtils.close(statement);
          }
        } finally {
          JdbcUtils.release(connection, this.dataSource);
        }
      } catch (final SQLException e) {
        throw new RuntimeException(this.tableName + ": Unable to load ID: ", e);
      }
    }
    if (createId && id == null) {
      return createId(values);
    } else {
      return id;
    }
  }

  @Override
  protected List<Object> loadValues(final Object id) {
    init();
    List<Object> values = null;
    if (this.loadAll) {
      loadAll();
      values = getValueById(id);
    } else {
      try {
        final Connection connection = JdbcUtils.getConnection(this.dataSource);
        try {
          final PreparedStatement statement = connection.prepareStatement(this.valueByIdSql);
          try {
            JdbcUtils.setValue(statement, 1, id);
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
          JdbcUtils.release(connection, this.dataSource);
        }
      } catch (final SQLException e) {
        throw new IllegalArgumentException(this.tableName + " " + id
          + " does not exist", e);
      }
    }
    return values;
  }

  public void setAuditColumns(final boolean useAuditColumns) {
    this.auditColumns = new HashMap<String, String>();
    this.auditColumns.put("WHO_CREATED", "USER");
    this.auditColumns.put("WHEN_CREATED", "SYSDATE");
    this.auditColumns.put("WHO_UPDATED", "USER");
    this.auditColumns.put("WHEN_UPDATED", "SYSDATE");
  }

  public void setAuditColumns(final Map<String, String> auditColumns) {
    this.auditColumns = auditColumns;
  }

  public void setColumnAliases(final List<String> columnAliases) {
    this.columnAliases = columnAliases;
  }

  public void setDataSource(final DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void setDataStore(final JdbcRecordStore dataStore) {
    this.dataStore = dataStore;
    if (dataStore != null) {
      this.dataSource = dataStore.getDataSource();
    }
  }

  public void setIdColumn(final String idColumn) {
    this.idColumn = idColumn;
  }

  public void setLoadAll(final boolean loadAll) {
    this.loadAll = loadAll;
  }

  public void setSequenceName(final String sequenceName) {
    this.sequenceName = sequenceName;
  }

  public void setTableName(final String tableName) {
    this.tableName = tableName;
  }

  public void setValueColumn(final String valueColumn) {
    this.valueColumns.clear();
    this.valueColumns.add(valueColumn);
  }

  public void setValueColumns(final List<String> valueColumns) {
    this.valueColumns.clear();
    for (final String column : valueColumns) {
      this.valueColumns.add(column);
    }
  }

  public void setValueColumns(final String... valueColumns) {
    this.valueColumns.clear();
    for (final String column : valueColumns) {
      this.valueColumns.add(column);
    }
  }

  @Override
  public String toString() {
    return this.tableName + " " + this.idColumn + " " + this.valueColumns;

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
