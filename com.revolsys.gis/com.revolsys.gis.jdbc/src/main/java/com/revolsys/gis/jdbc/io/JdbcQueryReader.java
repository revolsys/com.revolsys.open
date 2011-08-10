package com.revolsys.gis.jdbc.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.xml.namespace.QName;

import org.springframework.util.StringUtils;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.AbstractReader;

public class JdbcQueryReader extends AbstractReader<DataObject> implements
  DataObjectReader {

  public static QName getQName(final String tableName) {
    final String[] parts = tableName.split("\\.");
    if (parts.length == 1) {
      return new QName(parts[0]);
    } else {
      return new QName(parts[0], parts[1]);
    }
  }

  private boolean autoCommit = false;

  private JdbcDataObjectStore dataStore;

  private int fetchSize = 10;

  private final List<JdbcQuery> queries = new ArrayList<JdbcQuery>();

  private JdbcMultipleQueryIterator iterator;

  private BoundingBox boundingBox;

  private List<QName> typeNames;

  private String whereClause;

  public JdbcQueryReader() {
  }

  public JdbcQueryReader(final JdbcDataObjectStore dataStore) {
    this.dataStore = dataStore;
  }

  public void addQuery(final JdbcQuery query) {
    queries.add(query);
  }

  public void addQuery(final QName typeName, final String query) {
    addQuery(new JdbcQuery(typeName, query));
  }

  public void addQuery(final QName typeName, final String query,
    final List<Object> parameters) {
    addQuery(new JdbcQuery(typeName, query, parameters));
  }

  public void addQuery(final QName typeName, final String query,
    final Object... parameters) {
    addQuery(typeName, query, Arrays.asList(parameters));
  }

  @PreDestroy
  public void close() {
    if (iterator != null) {
      iterator.close();
    }
  }

  public JdbcDataObjectStore getDataStore() {
    return dataStore;
  }

  public int getFetchSize() {
    return fetchSize;
  }

  public DataObjectMetaData getMetaData() {
    return ((JdbcMultipleQueryIterator)iterator()).getMetaData();
  }

  protected void initialize() {
    if (typeNames != null) {
      for (final QName tableName : typeNames) {
        final DataObjectMetaData metaData = dataStore.getMetaData(tableName);
        if (metaData != null) {
          final JdbcQuery query;
          if (boundingBox == null) {
            final StringBuffer sql = new StringBuffer();
            JdbcQuery.addColumnsAndTableName(sql, metaData, "T", null);
            if (StringUtils.hasText(whereClause)) {
              sql.append(" WHERE ");
              sql.append(whereClause);
            }
            query = new JdbcQuery(metaData, sql.toString());
          } else {
            QName typeName = metaData.getName();
            query = dataStore.createQuery(typeName, null, boundingBox);
          }
          addQuery(query);
        }
      }
    }
  }

  public String getWhereClause() {
    return whereClause;
  }

  public void setWhereClause(String whereClause) {
    this.whereClause = whereClause;
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

  public void open() {
    iterator().hasNext();
  }

  public void setAutoCommit(final boolean autoCommit) {
    this.autoCommit = autoCommit;
  }

  public void setDataStore(final JdbcDataObjectStore dataStore) {
    this.dataStore = dataStore;
  }

  public void setFetchSize(final int fetchSize) {
    this.fetchSize = fetchSize;
  }

  /**
   * @param queries the queries to set
   */
  public void setQueries(final List<JdbcQuery> queries) {
    for (final JdbcQuery query : queries) {
      addQuery(query);
    }
  }

  /**
   * @param typeNames the typeNames to set
   */
  public void setTypeNames(final List<QName> typeNames) {
    this.typeNames = typeNames;

  }

  public void setBoundingBox(BoundingBox boundingBox) {
    this.boundingBox = boundingBox;
  }
}
