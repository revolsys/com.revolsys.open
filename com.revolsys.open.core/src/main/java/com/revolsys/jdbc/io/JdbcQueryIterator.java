package com.revolsys.jdbc.io;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.annotation.PreDestroy;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.data.io.RecordIterator;
import com.revolsys.data.query.Query;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.RecordState;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.gis.io.Statistics;
import com.revolsys.io.FileUtil;
import com.revolsys.jdbc.JdbcConnection;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.field.JdbcFieldDefinition;

public class JdbcQueryIterator extends AbstractIterator<Record> implements
  RecordIterator {

  public static Record getNextRecord(final JdbcRecordStore recordStore,
    final RecordDefinition recordDefinition,
    final List<FieldDefinition> fields, final RecordFactory recordFactory,
    final ResultSet resultSet) {
    final Record record = recordFactory.createRecord(recordDefinition);
    if (record != null) {
      record.setState(RecordState.Initalizing);
      int columnIndex = 1;
      for (final FieldDefinition field : fields) {
        final JdbcFieldDefinition jdbcField = (JdbcFieldDefinition)field;
        try {
          columnIndex = jdbcField.setFieldValueFromResultSet(resultSet,
            columnIndex, record);
        } catch (final SQLException e) {
          throw new RuntimeException("Unable to get value " + (columnIndex + 1)
            + " from result set", e);
        }
      }
      record.setState(RecordState.Persisted);
      recordStore.addStatistic("query", record);
    }
    return record;
  }

  public static ResultSet getResultSet(final RecordDefinition recordDefinition,
    final PreparedStatement statement, final Query query) throws SQLException {
    JdbcUtils.setPreparedStatementParameters(statement, query);

    return statement.executeQuery();
  }

  private JdbcConnection connection;

  private final int currentQueryIndex = -1;

  private RecordFactory recordFactory;

  private JdbcRecordStore recordStore;

  private final int fetchSize = 10;

  private RecordDefinition recordDefinition;

  private List<Query> queries;

  private ResultSet resultSet;

  private PreparedStatement statement;

  private List<FieldDefinition> fields = new ArrayList<FieldDefinition>();

  private Query query;

  private Statistics statistics;

  public JdbcQueryIterator(final JdbcRecordStore recordStore,
    final Query query, final Map<String, Object> properties) {
    super();

    final boolean autoCommit = BooleanStringConverter.getBoolean(properties.get("autoCommit"));
    connection = recordStore.getJdbcConnection(autoCommit);
    recordFactory = query.getProperty("recordFactory");
    if (recordFactory == null) {
      recordFactory = recordStore.getRecordFactory();
    }
    this.recordStore = recordStore;
    this.query = query;
    statistics = query.getStatistics();
    if (statistics == null) {
      statistics = (Statistics)properties.get(Statistics.class.getName());
    }
  }

  @Override
  @PreDestroy
  public void doClose() {
    JdbcUtils.close(statement, resultSet);
    FileUtil.closeSilent(connection);
    fields = null;
    connection = null;
    recordFactory = null;
    recordStore = null;
    recordDefinition = null;
    queries = null;
    query = null;
    resultSet = null;
    statement = null;
    statistics = null;
  }

  @Override
  protected void doInit() {
    resultSet = getResultSet();
  }

  protected String getErrorMessage() {
    if (queries == null) {
      return null;
    } else {
      return queries.get(currentQueryIndex).getSql();
    }
  }

  @Override
  protected Record getNext() throws NoSuchElementException {
    try {
      if (resultSet != null && resultSet.next()) {
        final Record record = getNextRecord(recordStore, recordDefinition,
          fields, recordFactory, resultSet);
        if (statistics != null) {
          statistics.add(record);
        }
        return record;
      } else {
        close();
        throw new NoSuchElementException();
      }
    } catch (final SQLException e) {
      close();
      throw new RuntimeException(getErrorMessage(), e);
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
    if (recordDefinition == null) {
      hasNext();
    }
    return recordDefinition;
  }

  public JdbcRecordStore getRecordStore() {
    return recordStore;
  }

  protected ResultSet getResultSet() {
    final String tableName = query.getTypeName();
    recordDefinition = query.getRecordDefinition();
    if (recordDefinition == null) {
      if (tableName != null) {
        recordDefinition = recordStore.getRecordDefinition(tableName);
        query.setRecordDefinition(recordDefinition);
      }
    }
    final String sql = getSql(query);
    try {
      statement = connection.prepareStatement(sql);
      statement.setFetchSize(fetchSize);

      resultSet = getResultSet(recordDefinition, statement, query);
      final ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

      if (recordDefinition == null) {
        recordDefinition = recordStore.getRecordDefinition(tableName,
          resultSetMetaData);
      }
      final List<String> fieldNames = new ArrayList<String>(
        query.getFieldNames());
      if (fieldNames.isEmpty()) {
        fields.addAll(recordDefinition.getFields());
      } else {
        for (final String fieldName : fieldNames) {
          if (fieldName.equals("*")) {
            fields.addAll(recordDefinition.getFields());
          } else {
            final FieldDefinition field = recordDefinition.getField(fieldName);
            if (field != null) {
              fields.add(field);
            }
          }
        }
      }

      final String typePath = query.getTypeNameAlias();
      if (typePath != null) {
        final RecordDefinitionImpl newRecordDefinition = ((RecordDefinitionImpl)recordDefinition).rename(typePath);
        recordDefinition = newRecordDefinition;
      }
    } catch (final SQLException e) {
      JdbcUtils.close(statement, resultSet);
      throw connection.getException("Execute Query", sql, e);
    }
    return resultSet;
  }

  protected String getSql(final Query query) {
    return JdbcUtils.getSelectSql(query);
  }

  protected void setQuery(final Query query) {
    this.query = query;
  }

}
