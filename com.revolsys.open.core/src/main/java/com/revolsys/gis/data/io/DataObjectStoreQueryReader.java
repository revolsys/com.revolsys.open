package com.revolsys.gis.data.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.util.StringUtils;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.query.Query;
import com.revolsys.gis.data.query.SqlCondition;
import com.revolsys.gis.io.Statistics;

public class DataObjectStoreQueryReader extends IteratorReader<DataObject>
  implements DataObjectReader {

  private AbstractDataObjectStore dataStore;

  private List<Query> queries = new ArrayList<Query>();

  private BoundingBox boundingBox;

  private List<String> typePaths;

  private String whereClause;

  private Statistics statistics;

  public DataObjectStoreQueryReader() {
    setIterator(new DataStoreMultipleQueryIterator(this));
  }

  public DataObjectStoreQueryReader(final AbstractDataObjectStore dataStore) {
    this();
    setDataStore(dataStore);
  }

  public void addQuery(final Query query) {
    queries.add(query);
  }

  @Override
  @PreDestroy
  public void close() {
    super.close();
    boundingBox = null;
    dataStore = null;
    queries = null;
    typePaths = null;
    whereClause = null;
    if (statistics != null) {
      statistics.disconnect();
    }
    statistics = null;
  }

  protected AbstractIterator<DataObject> createQueryIterator(final int i) {
    if (i < queries.size()) {
      final Query query = queries.get(i);
      if (StringUtils.hasText(whereClause)) {
        query.and(new SqlCondition(whereClause));
      }
      if (boundingBox != null) {
        query.setBoundingBox(boundingBox);
      }

      final AbstractIterator<DataObject> iterator = dataStore.createIterator(
        query, getProperties());
      return iterator;
    }
    throw new NoSuchElementException();
  }

  public BoundingBox getBoundingBox() {
    return boundingBox;
  }

  public AbstractDataObjectStore getDataStore() {
    return dataStore;
  }

  @Override
  public DataObjectMetaData getMetaData() {
    return ((DataObjectIterator)iterator()).getMetaData();
  }

  public List<Query> getQueries() {
    return queries;
  }

  public String getWhereClause() {
    return whereClause;
  }

  @Override
  @PostConstruct
  public void open() {
    if (typePaths != null) {
      for (final String tableName : typePaths) {
        final DataObjectMetaData metaData = dataStore.getMetaData(tableName);
        if (metaData != null) {
          Query query;
          if (boundingBox == null) {
            query = new Query(metaData);
            query.setWhereCondition(new SqlCondition(whereClause));
          } else {
            query = new Query(metaData);
            query.setBoundingBox(boundingBox);
          }
          addQuery(query);
        }
      }
    }
    super.open();
  }

  public void setBoundingBox(final BoundingBox boundingBox) {
    this.boundingBox = boundingBox;
  }

  public void setDataStore(final AbstractDataObjectStore dataStore) {
    this.dataStore = dataStore;
  }

  /**
   * @param queries the queries to set
   */
  public void setQueries(final Collection<Query> queries) {
    this.queries.clear();
    for (final Query query : queries) {
      addQuery(query);
    }
  }

  public void setQueries(final List<Query> queries) {
    this.queries.clear();
    for (final Query query : queries) {
      addQuery(query);
    }
  }

  public void setStatistics(final Statistics statistics) {
    this.statistics = statistics;
    if (statistics != null) {
      statistics.connect();
    }
    setProperty(Statistics.class.getName(), statistics);
  }

  /**
   * @param typePaths the typePaths to set
   */
  public void setTypeNames(final List<String> typePaths) {
    this.typePaths = typePaths;

  }

  public void setWhereClause(final String whereClause) {
    this.whereClause = whereClause;
  }
}
