package com.revolsys.jdbc.io;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.data.io.RecordIterator;
import com.revolsys.data.query.Query;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.RecordState;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.gis.io.Statistics;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.attribute.JdbcAttribute;

public class JdbcQueryIterator extends AbstractIterator<Record> implements
RecordIterator {

  public static Record getNextObject(final JdbcRecordStore dataStore,
    final RecordDefinition recordDefinition, final List<Attribute> attributes,
    final RecordFactory recordFactory, final ResultSet resultSet) {
    final Record object = recordFactory.createRecord(recordDefinition);
    if (object != null) {
      object.setState(RecordState.Initalizing);
      int columnIndex = 1;

      for (final Attribute attribute : attributes) {
        final JdbcAttribute jdbcAttribute = (JdbcAttribute)attribute;
        try {
          columnIndex = jdbcAttribute.setAttributeValueFromResultSet(resultSet,
            columnIndex, object);
        } catch (final SQLException e) {
          throw new RuntimeException("Unable to get value " + (columnIndex + 1)
            + " from result set", e);
        }
      }
      object.setState(RecordState.Persisted);
      dataStore.addStatistic("query", object);
    }
    return object;
  }

  public static ResultSet getResultSet(final RecordDefinition recordDefinition,
    final PreparedStatement statement, final Query query) throws SQLException {
    JdbcUtils.setPreparedStatementParameters(statement, query);

    return statement.executeQuery();
  }

  private Connection connection;

  private final int currentQueryIndex = -1;

  private RecordFactory recordFactory;

  private DataSource dataSource;

  private JdbcRecordStore dataStore;

  private final int fetchSize = 10;

  private RecordDefinition recordDefinition;

  private List<Query> queries;

  private ResultSet resultSet;

  private PreparedStatement statement;

  private List<Attribute> attributes = new ArrayList<Attribute>();

  private Query query;

  private Statistics statistics;

  public JdbcQueryIterator(final JdbcRecordStore dataStore, final Query query,
    final Map<String, Object> properties) {
    super();
    this.connection = dataStore.getConnection();
    this.dataSource = dataStore.getDataSource();

    if (this.dataSource != null) {
      try {
        this.connection = JdbcUtils.getConnection(this.dataSource);
        boolean autoCommit = false;
        if (BooleanStringConverter.getBoolean(properties.get("autoCommit"))) {
          autoCommit = true;
        }
        this.connection.setAutoCommit(autoCommit);
      } catch (final SQLException e) {
        throw new IllegalArgumentException("Unable to create connection", e);
      }
    }
    this.recordFactory = query.getProperty("recordFactory");
    if (this.recordFactory == null) {
      this.recordFactory = dataStore.getRecordFactory();
    }
    this.dataStore = dataStore;
    this.query = query;
    this.statistics = (Statistics)properties.get(Statistics.class.getName());
  }

  @Override
  @PreDestroy
  public void doClose() {
    JdbcUtils.close(this.statement, this.resultSet);
    JdbcUtils.release(this.connection, this.dataSource);
    this.attributes = null;
    this.connection = null;
    this.recordFactory = null;
    this.dataSource = null;
    this.dataStore = null;
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

  public JdbcRecordStore getDataStore() {
    return this.dataStore;
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
        final Record object = getNextObject(this.dataStore,
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

  protected ResultSet getResultSet() {
    final String tableName = this.query.getTypeName();
    this.recordDefinition = this.query.getRecordDefinition();
    if (this.recordDefinition == null) {
      if (tableName != null) {
        this.recordDefinition = this.dataStore.getRecordDefinition(tableName);
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
        this.recordDefinition = this.dataStore.getRecordDefinition(tableName,
          resultSetMetaData);
      }
      final List<String> attributeNames = new ArrayList<String>(
          this.query.getAttributeNames());
      if (attributeNames.isEmpty()) {
        this.attributes.addAll(this.recordDefinition.getAttributes());
      } else {
        for (final String attributeName : attributeNames) {
          if (attributeName.equals("*")) {
            this.attributes.addAll(this.recordDefinition.getAttributes());
          } else {
            final Attribute attribute = this.recordDefinition.getAttribute(attributeName);
            if (attribute != null) {
              this.attributes.add(attribute);
            }
          }
        }
      }

      final String typePath = this.query.getTypeNameAlias();
      if (typePath != null) {
        final RecordDefinitionImpl newMetaData = ((RecordDefinitionImpl)this.recordDefinition).clone();
        newMetaData.setName(typePath);
        this.recordDefinition = newMetaData;
      }
    } catch (final SQLException e) {
      JdbcUtils.close(this.statement, this.resultSet);
      throw JdbcUtils.getException(this.dataSource, this.connection,
        "Execute Query", sql, e);
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
