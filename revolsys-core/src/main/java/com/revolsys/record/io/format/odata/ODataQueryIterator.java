package com.revolsys.record.io.format.odata;

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
import com.revolsys.record.schema.FieldDefinition;
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

  private ApacheHttpRequestBuilder request;

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
    this.request = request;
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
    final Iterator<JsonObject> results = this.results;
    do {
      if (results != null && results.hasNext()) {
        final JsonObject recordJson = results.next();
        this.readCount++;

        final RecordDefinition recordDefinition = this.recordDefinition;
        final Record record = this.recordFactory.newRecord(recordDefinition);
        if (record != null) {
          record.setState(RecordState.INITIALIZING);
          for (final FieldDefinition field : recordDefinition.getFields()) {
            final String name = field.getName();
            final Object value = recordJson.getValue(name);
            record.setValue(field, value);
          }
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
    } while (results != null);
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

  @Override
  public String toString() {
    return this.query.toString();
  }

}
