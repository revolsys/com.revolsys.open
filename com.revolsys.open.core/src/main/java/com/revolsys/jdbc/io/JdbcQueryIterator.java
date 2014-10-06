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
import com.revolsys.jdbc.attribute.JdbcFieldDefinition;

public class JdbcQueryIterator extends AbstractIterator<Record> implements
RecordIterator {

  public static Record getNextObject(final JdbcRecordStore recordStore,
    final RecordDefinition recordDefinition, final List<FieldDefinition> attributes,
    final RecordFactory recordFactory, final ResultSet resultSet) {
    final Record object = recordFactory.createRecord(recordDefinition);
    if (object != null) {
      object.setState(RecordState.Initalizing);
      int columnIndex = 1;

      for (final FieldDefinition attribute : attributes) {
        final JdbcFieldDefinition jdbcAttribute = (JdbcFieldDefinition)attribute;
        try {
          columnIndex = jdbcAttribute.setFieldValueFromResultSet(resultSet,
            columnIndex, object);
        } catch (final SQLException e) {
          throw new RuntimeException("Unable to get value " + (columnIndex + 1)
            + " from result set", e);
        }
      }
      object.setState(RecordState.Persisted);
      recordStore.addStatistic("query", object);
    }
    return object;
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

  private List<FieldDefinition> attributes = new ArrayList<FieldDefinition>();

  private Query query;

  private Statistics statistics;

  public JdbcQueryIterator(final JdbcRecordStore recordStore,
    final Query query, final Map<String, Object> properties) {
    super();

    final boolean autoCommit = BooleanStringConverter.getBoolean(properties.get("autoCommit"));
    this.connection = recordStore.getJdbcConnection(autoCommit);
    this.recordFactory = query.getProperty("recordFactory");
    if (this.recordFactory == null) {
      this.recordFactory = recordStore.getRecordFactory();
    }
    this.recordStore = recordStore;
    this.query = query;
    this.statistics = query.getStatistics();
    if (this.statistics == null) {
      this.statistics = (Statistics)properties.get(Statistics.class.getName());
    }
  }

  @Override
  @PreDestroy
  public void doClose() {
    JdbcUtils.close(this.statement, this.resultSet);
    FileUtil.closeSilent(this.connection);
    this.attributes = null;
    this.connection = null;
    this.recordFactory = null;
    this.recordStore = null;
    this.recordDefinition = null;
    this.queries = null;
    this.query = null;
    this.resultSet = null;
    this.statement = null;
    this.statistics = null;
  }

  @Override
  protected void doInit() {
    this.resultSet = getResultSet();
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
      if (this.resultSet != null && this.resultSet.next()) {
        final Record object = getNextObject(this.recordStore,
          this.recordDefinition, this.attributes, this.recordFactory,
          this.resultSet);
        if (this.statistics != null) {
          this.statistics.add(object);
        }
        return object;
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
    if (this.recordDefinition == null) {
      hasNext();
    }
    return this.recordDefinition;
  }

  public JdbcRecordStore getRecordStore() {
    return this.recordStore;
  }

  protected ResultSet getResultSet() {
    final String tableName = this.query.getTypeName();
    this.recordDefinition = this.query.getRecordDefinition();
    if (this.recordDefinition == null) {
      if (tableName != null) {
        this.recordDefinition = this.recordStore.getRecordDefinition(tableName);
        this.query.setRecordDefinition(this.recordDefinition);
      }
    }
    final String sql = getSql(this.query);
    try {
      this.statement = this.connection.prepareStatement(sql);
      this.statement.setFetchSize(this.fetchSize);

      this.resultSet = getResultSet(this.recordDefinition, this.statement,
        this.query);
      final ResultSetMetaData resultSetMetaData = this.resultSet.getMetaData();

      if (this.recordDefinition == null) {
        this.recordDefinition = this.recordStore.getRecordDefinition(tableName,
          resultSetMetaData);
      }
      final List<String> attributeNames = new ArrayList<String>(
          this.query.getFieldNames());
      if (attributeNames.isEmpty()) {
        this.attributes.addAll(this.recordDefinition.getFields());
      } else {
        for (final String attributeName : attributeNames) {
          if (attributeName.equals("*")) {
            this.attributes.addAll(this.recordDefinition.getFields());
          } else {
            final FieldDefinition attribute = this.recordDefinition.getField(attributeName);
            if (attribute != null) {
              this.attributes.add(attribute);
            }
          }
        }
      }

      final String typePath = this.query.getTypeNameAlias();
      if (typePath != null) {
        final RecordDefinitionImpl newRecordDefinition = ((RecordDefinitionImpl)this.recordDefinition).rename(typePath);
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

  protected void setQuery(final Query query) {
    this.query = query;
  }

}
