package com.revolsys.data.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.util.StringUtils;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.data.query.Query;
import com.revolsys.data.query.SqlCondition;
import com.revolsys.data.query.functions.F;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.jts.geom.BoundingBox;

public class RecordStoreQueryReader extends IteratorReader<Record>
  implements RecordReader {

  private AbstractRecordStore recordStore;

  private List<Query> queries = new ArrayList<Query>();

  private BoundingBox boundingBox;

  private List<String> typePaths;

  private String whereClause;

  public RecordStoreQueryReader() {
    setIterator(new DataStoreMultipleQueryIterator(this));
  }

  public RecordStoreQueryReader(final AbstractRecordStore recordStore) {
    this();
    setDataStore(recordStore);
  }

  public void addQuery(final Query query) {
    queries.add(query);
  }

  @Override
  @PreDestroy
  public void close() {
    super.close();
    boundingBox = null;
    recordStore = null;
    queries = null;
    typePaths = null;
    whereClause = null;
  }

  protected AbstractIterator<Record> createQueryIterator(final int i) {
    if (i < queries.size()) {
      final Query query = queries.get(i);
      if (StringUtils.hasText(whereClause)) {
        query.and(new SqlCondition(whereClause));
      }
      if (boundingBox != null) {
        final Attribute geometryAttribute = query.getRecordDefinition()
          .getGeometryAttribute();
        query.and(F.envelopeIntersects(geometryAttribute, boundingBox));
      }

      final AbstractIterator<Record> iterator = recordStore.createIterator(
        query, getProperties());
      return iterator;
    }
    throw new NoSuchElementException();
  }

  public BoundingBox getBoundingBox() {
    return boundingBox;
  }

  public AbstractRecordStore getDataStore() {
    return recordStore;
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return ((RecordIterator)iterator()).getRecordDefinition();
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
        final RecordDefinition recordDefinition = recordStore.getRecordDefinition(tableName);
        if (recordDefinition != null) {
          Query query;
          if (boundingBox == null) {
            query = new Query(recordDefinition);
            query.setWhereCondition(new SqlCondition(whereClause));
          } else {
            query = new Query(recordDefinition);
            query.setWhereCondition(F.envelopeIntersects(
              recordDefinition.getGeometryAttribute(), boundingBox));
            ;
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

  public void setDataStore(final AbstractRecordStore recordStore) {
    this.recordStore = recordStore;
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
