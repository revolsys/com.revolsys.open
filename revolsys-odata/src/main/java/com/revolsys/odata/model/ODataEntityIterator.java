package com.revolsys.odata.model;

import java.net.URI;
import java.util.Iterator;
import java.util.List;

import org.apache.olingo.commons.api.data.AbstractEntityCollection;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Operation;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.ex.ODataNotSupportedException;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.queryoption.CountOption;

import com.revolsys.io.BaseCloseable;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.record.schema.TableRecordStoreConnection;
import com.revolsys.transaction.Transaction;
import com.revolsys.util.UriBuilder;

public class ODataEntityIterator extends AbstractEntityCollection
  implements BaseCloseable, Iterator<Entity> {

  private Transaction transaction;

  private RecordReader reader;

  private Iterator<Record> iterator;

  private final ODataEntityType entityType;

  private final int skip;

  private int readCount;

  private final int limit;

  private final ODataRequest request;

  private final UriInfo uriInfo;

  private final Query query;

  private Query countQuery;

  private final TableRecordStoreConnection connection;

  private boolean countLoaded;

  private Integer count;

  public ODataEntityIterator(final ODataRequest request, final UriInfo uriInfo,
    final EdmEntitySet edmEntitySet, final ODataEntityType entityType,
    final TableRecordStoreConnection connection) throws ODataApplicationException {
    this.request = request;
    this.uriInfo = uriInfo;
    this.entityType = entityType;
    this.connection = connection;
    final Query query = entityType.newQuery(uriInfo);

    final CountOption countOption = this.uriInfo.getCountOption();
    if (countOption == null) {
      this.countLoaded = true;
    } else {
      this.countQuery = query.clone();
      this.countLoaded = !countOption.getValue();
    }
    this.query = query;
    this.entityType.addLimits(this.query, this.uriInfo);
    this.skip = query.getOffset();
    this.limit = query.getLimit();
  }

  @Override
  public void close() {
    if (this.reader != null) {
      try {
        this.reader.close();
      } finally {
        this.transaction.close();
      }
    }
  }

  @Override
  public Integer getCount() {
    if (!this.countLoaded) {
      this.countLoaded = true;
      try (
        Transaction transaction = this.connection.newTransaction()) {
        final RecordStore recordStore = this.entityType.getRecordStore();
        final Integer count = recordStore.getRecordCount(this.countQuery);
        this.count = count;
      }
    }
    return this.count;
  }

  /**
   * {@inheritDoc}
   * <p/>
   * <b>ATTENTION:</b> <code>getDeltaLink</code> is not supported by default.
   */
  @Override
  public URI getDeltaLink() {
    throw new ODataNotSupportedException("Entity Iterator does not support getDeltaLink()");
  }

  private Iterator<Record> getIterator() {
    if (this.reader == null) {
      this.transaction = this.connection.newTransaction();
      final RecordStore recordStore = this.entityType.getRecordStore();
      this.reader = recordStore.getRecords(this.query);
      this.iterator = this.reader.iterator();
    }
    return this.iterator;
  }

  @Override
  public URI getNext() {
    final Integer count = getCount();
    final int totalRead = this.skip + this.readCount;
    if (count == null) {
      if (this.readCount < this.limit) {
        return null;
      }
    } else {
      if (totalRead >= count) {
        return null;
      }
    }

    final String uri = this.request.getRawRequestUri();
    return new UriBuilder(uri).setParameter("$skip", totalRead).build();
  }

  /**
   * {@inheritDoc}
   * <p/>
   * <b>ATTENTION:</b> <code>getOperations</code> is not supported by default.
   */
  @Override
  public List<Operation> getOperations() {
    // "Remove is not supported for iteration over Entities."
    throw new ODataNotSupportedException(
      "Entity Iterator does not support getOperations() by default");
  }

  @Override
  public boolean hasNext() {
    final Iterator<Record> iterator = getIterator();
    final boolean hasNext = iterator.hasNext();
    if (!hasNext) {
      close();
    }
    return hasNext;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterator<Entity> iterator() {
    return this;
  }

  @Override
  public Entity next() {
    try {
      final Iterator<Record> iterator = getIterator();
      final Record record = iterator.next();
      this.readCount++;
      return this.entityType.newEntity(record);
    } catch (final RuntimeException e) {
      throw e;
    }
  }

  /**
   * {@inheritDoc}
   * <p/>
   * <b>ATTENTION:</b> <code>remove</code> is not supported by default.
   */
  @Override
  public void remove() {
    // "Remove is not supported for iteration over Entities."
    throw new ODataNotSupportedException("Entity Iterator does not support remove()");
  }

  @Override
  public String toString() {
    return this.query.toString();
  }
}
