package com.revolsys.jdbc.io;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.jeometry.common.io.PathName;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.UncategorizedSQLException;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.io.FileUtil;
import com.revolsys.jdbc.JdbcConnection;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.RecordState;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Booleans;
import com.revolsys.util.count.LabelCountMap;
import com.revolsys.util.count.LabelCounters;

public class JdbcQueryIterator extends AbstractIterator<Record> implements RecordReader {
  public static Record getNextRecord(final JdbcRecordStore recordStore,
    final RecordDefinition recordDefinition, final List<FieldDefinition> fields,
    final RecordFactory<Record> recordFactory, final ResultSet resultSet,
    final boolean internStrings) {
    final Record record = recordFactory.newRecord(recordDefinition);
    if (record != null) {
      record.setState(RecordState.INITIALIZING);
      int columnIndex = 1;
      for (final FieldDefinition field : fields) {
        final JdbcFieldDefinition jdbcField = (JdbcFieldDefinition)field;
        try {
          columnIndex = jdbcField.setFieldValueFromResultSet(resultSet, columnIndex, record,
            internStrings);
        } catch (final SQLException e) {
          throw new RuntimeException(
            "Unable to get value " + (columnIndex + 1) + " from result set", e);
        }
      }
      record.setState(RecordState.PERSISTED);
      recordStore.addStatistic("query", record);
    }
    return record;
  }

  private boolean internStrings;

  private JdbcConnection connection;

  private final int currentQueryIndex = -1;

  private final int fetchSize = 10;

  private List<FieldDefinition> fields = new ArrayList<>();

  private List<Query> queries;

  private Query query;

  private JdbcRecordDefinition recordDefinition;

  private RecordFactory<Record> recordFactory;

  private JdbcRecordStore recordStore;

  private ResultSet resultSet;

  private PreparedStatement statement;

  private LabelCounters labelCountMap;

  public JdbcQueryIterator(final JdbcRecordStore recordStore, final Query query,
    final Map<String, Object> properties) {
    super();

    final boolean autoCommit = Booleans.getBoolean(properties.get("autoCommit"));
    this.internStrings = Booleans.getBoolean(properties.get("internStrings"));
    this.connection = recordStore.getJdbcConnection(autoCommit);
    this.recordFactory = query.getRecordFactory();
    if (this.recordFactory == null) {
      this.recordFactory = recordStore.getRecordFactory();
    }
    this.recordStore = recordStore;
    this.query = query;
    this.labelCountMap = query.getStatistics();
    if (this.labelCountMap == null) {
      this.labelCountMap = (LabelCounters)properties.get(LabelCountMap.class.getName());
    }
  }

  @Override
  public synchronized void closeDo() {
    JdbcUtils.close(this.statement, this.resultSet);
    FileUtil.closeSilent(this.connection);
    this.fields = null;
    this.connection = null;
    this.recordFactory = null;
    this.recordStore = null;
    this.recordDefinition = null;
    this.queries = null;
    this.query = null;
    this.resultSet = null;
    this.statement = null;
    this.labelCountMap = null;
  }

  protected String getErrorMessage() {
    if (this.queries == null) {
      return null;
    } else {
      return this.queries.get(this.currentQueryIndex).getSql();
    }
  }

  @Override
  protected Record getNext() throws NoSuchElementException {
    try {
      if (this.resultSet != null && !this.query.isCancelled() && this.resultSet.next()) {
        final Record record = getNextRecord(this.recordStore, this.recordDefinition, this.fields,
          this.recordFactory, this.resultSet, this.internStrings);
        if (this.labelCountMap != null) {
          this.labelCountMap.addCount(record);
        }
        return record;
      } else {
        close();
        throw new NoSuchElementException();
      }
    } catch (final SQLException e) {
      final boolean cancelled = this.query.isCancelled();
      DataAccessException e2;
      if (cancelled) {
        e2 = null;
      } else {
        final JdbcConnection connection = this.connection;
        final String sql = getErrorMessage();
        if (connection == null) {
          e2 = new UncategorizedSQLException("Get Next", sql, e);
        } else {
          e2 = connection.getException("Get Next", sql, e);
        }
      }
      close();
      if (cancelled) {
        throw new NoSuchElementException();
      } else {
        throw e2;
      }
    } catch (final RuntimeException e) {
      close();
      throw e;
    } catch (final Error e) {
      close();
      throw e;
    }
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    if (this.recordDefinition == null) {
      hasNext();
    }
    return this.recordDefinition;
  }

  @Override
  public JdbcRecordStore getRecordStore() {
    return this.recordStore;
  }

  protected ResultSet getResultSet() {
    final String tableName = this.query.getTypeName();
    final RecordDefinition queryRecordDefinition = this.query.getRecordDefinition();
    if (queryRecordDefinition != null) {
      this.recordDefinition = this.recordStore.getRecordDefinition(queryRecordDefinition);
      if (this.recordDefinition != null) {
        this.query.setRecordDefinition(this.recordDefinition);
      }
    }
    if (this.recordDefinition == null) {
      if (tableName != null) {
        this.recordDefinition = this.recordStore.getRecordDefinition(tableName);
        this.query.setRecordDefinition(this.recordDefinition);
      }
    }
    String dbTableName;
    if (this.recordDefinition == null) {
      final PathName pathName = PathName.newPathName(tableName);
      if (pathName == null) {
        dbTableName = null;
      } else {
        dbTableName = pathName.getName();
      }
    } else {
      dbTableName = this.recordDefinition.getDbTableName();
    }

    final String sql = getSql(this.query);
    try {
      this.statement = this.connection.prepareStatement(sql);
      this.statement.setFetchSize(this.fetchSize);

      this.resultSet = this.recordStore.getResultSet(this.statement, this.query);
      final ResultSetMetaData resultSetMetaData = this.resultSet.getMetaData();

      if (this.recordDefinition == null) {
        this.recordDefinition = this.recordStore.getRecordDefinition(tableName, resultSetMetaData,
          dbTableName);
      }
      this.fields = this.query.getFields(this.recordDefinition);

      final String typePath = this.query.getTypeNameAlias();
      if (typePath != null) {
        final JdbcRecordDefinition newRecordDefinition = this.recordDefinition.rename(typePath);
        this.recordDefinition = newRecordDefinition;
      }
    } catch (final SQLException e) {
      JdbcUtils.close(this.statement, this.resultSet);
      throw this.connection.getException("Execute Query", sql, e);
    }
    return this.resultSet;
  }

  protected String getSql(final Query query) {
    return JdbcUtils.getSelectSql(query);
  }

  @Override
  protected void initDo() {
    this.resultSet = getResultSet();
  }

  public boolean isInternStrings() {
    return this.internStrings;
  }

  public void setInternStrings(final boolean internStrings) {
    this.internStrings = internStrings;
  }

  protected void setQuery(final Query query) {
    this.query = query;
  }

}
