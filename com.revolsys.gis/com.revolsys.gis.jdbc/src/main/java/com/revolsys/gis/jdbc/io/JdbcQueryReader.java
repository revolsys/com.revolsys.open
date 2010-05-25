package com.revolsys.gis.jdbc.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.xml.namespace.QName;

import com.revolsys.gis.data.io.AbstractReader;
import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;

public class JdbcQueryReader extends AbstractReader<DataObject> implements
  DataObjectReader {

  public static QName getQName(
    final String tableName) {
    final String[] parts = tableName.split("\\.");
    if (parts.length == 1) {
      return new QName(parts[0]);
    } else {
      return new QName(parts[0], parts[1]);
    }
  }

  public static String getTableName(
    final QName typeName) {
    String tableName;
    final String namespaceURI = typeName.getNamespaceURI();
    if (namespaceURI != "") {
      tableName = namespaceURI + "." + typeName.getLocalPart();
    } else {
      tableName = typeName.getLocalPart();
    }
    return tableName;
  }

  private boolean autoCommit = false;

  private final JdbcDataObjectStore dataStore;

  private int fetchSize = 10;

  private final List<JdbcQuery> queries = new ArrayList<JdbcQuery>();

  private JdbcMultipleQueryIterator iterator;

  public JdbcQueryReader(
    final JdbcDataObjectStore dataStore) {
    this.dataStore = dataStore;
  }

  public void addQuery(
    final JdbcQuery query) {
    queries.add(query);
  }

  public void addQuery(
    final QName typeName,
    final String query) {
    addQuery(new JdbcQuery(typeName, query));
  }

  public void addQuery(
    final QName typeName,
    final String query,
    final List<Object> parameters) {
    addQuery(new JdbcQuery(typeName, query, parameters));
  }

  public void addQuery(
    final QName typeName,
    final String query,
    final Object... parameters) {
    addQuery(typeName, query, Arrays.asList(parameters));
  }

  @PreDestroy
  public void close() {
    if (iterator != null) {
      iterator.close();
    }
  }

  public JdbcDataObjectStore getDataObjectStore() {
    return dataStore;
  }

  public int getFetchSize() {
    return fetchSize;
  }

  protected void initialize() {
  }

  public boolean isAutoCommit() {
    return autoCommit;
  }

  public Iterator<DataObject> iterator() {
    initialize();
    if (iterator == null) {
      iterator = new JdbcMultipleQueryIterator(dataStore, queries, autoCommit,
        fetchSize);
    }
    return iterator;
  }

  public DataObjectMetaData getMetaData() {
    return ((JdbcMultipleQueryIterator)iterator()).getMetaData();
  }

  public void setAutoCommit(
    final boolean autoCommit) {
    this.autoCommit = autoCommit;
  }

  public void setFetchSize(
    final int fetchSize) {
    this.fetchSize = fetchSize;
  }

  /**
   * @param queries the queries to set
   */
  public void setQueries(
    final List<JdbcQuery> queries) {
    for (final JdbcQuery query : queries) {
      addQuery(query);
    }
  }

  /**
   * @param tableNames the tableNames to set
   */
  public void setTableNames(
    final List<QName> tableNames) {
    for (final QName tableName : tableNames) {
      addQuery(tableName, "SELECT * FROM " + getTableName(tableName));
    }
  }
}
