package com.revolsys.odata.model;

import java.net.URI;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.http.ApacheHttpRequestBuilder;
import com.revolsys.http.ApacheHttpRequestBuilderFactory;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.RecordState;
import com.revolsys.record.io.RecordIterator;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.RecordDefinition;

public class ODataQueryIterator extends AbstractIterator<Record>
  implements RecordReader, RecordIterator {

  private final RecordDefinition recordDefinition;

  private final ODataRecordStore recordStore;

  private final Query query;

  private Iterator<JsonObject> results;

  private URI nextURI;

  private final ApacheHttpRequestBuilderFactory requestFactory;

  private RecordFactory<Record> recordFactory;

  private int readCount;

  public ODataQueryIterator(final ODataRecordStore recordStore,
    final ApacheHttpRequestBuilderFactory requestFactory, final Query query,
    final Map<String, Object> properties) {
    this.recordStore = recordStore;
    this.recordFactory = query.getRecordFactory();
    if (this.recordFactory == null) {
      this.recordFactory = recordStore.getRecordFactory();
    }
    this.requestFactory = requestFactory;
    this.query = query;
    this.recordDefinition = query.getRecordDefinition();
    setProperties(properties);
  }

  void executeRequest(final ApacheHttpRequestBuilder request) {
    final JsonObject json = request.getJson();
    if (json == null) {
      this.nextURI = null;
      this.results = Collections.emptyIterator();
    } else {
      this.nextURI = json.getValue("@odata.nextLink", DataTypes.ANY_URI);
      this.results = json.getJsonList("value").jsonObjects().iterator();
    }
  }

  @Override
  protected Record getNext() throws NoSuchElementException {
    if (this.readCount >= this.query.getLimit()) {
      throw new NoSuchElementException();
    }
    do {
      if (this.results != null && this.results.hasNext()) {
        final JsonObject recordJson = this.results.next();
        this.readCount++;

        final Record record = this.recordFactory.newRecord(this.recordDefinition);
        if (record != null) {
          record.setState(RecordState.INITIALIZING);
          record.setValues(recordJson);
          record.setState(RecordState.PERSISTED);

          this.recordStore.addStatistic("query", record);
        }
        return record;
      }
      if (this.nextURI == null) {
        throw new NoSuchElementException();
      } else {
        final ApacheHttpRequestBuilder request = this.requestFactory.get(this.nextURI);

        executeRequest(request);
      }
    } while (this.results != null);
    throw new NoSuchElementException();
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  @Override
  protected void initDo() {
    super.initDo();
    final ApacheHttpRequestBuilder request = this.recordStore.newRequest(this.query);
    executeRequest(request);
  }

}
