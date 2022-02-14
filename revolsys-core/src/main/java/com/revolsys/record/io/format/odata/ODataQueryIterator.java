package com.revolsys.record.io.format.odata;

import java.net.URI;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;

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

  private static Function<JsonObject, Record> recordFactoryConverter(
    final RecordDefinition recordDefinition, final RecordFactory<Record> recordFactory) {
    return (recordJson) -> {
      final Record record = recordFactory.newRecord(recordDefinition);
      if (record != null) {
        record.setState(RecordState.INITIALIZING);
        for (final FieldDefinition field : record.getRecordDefinition().getFields()) {
          final String name = field.getName();
          final Object value = recordJson.getValue(name);
          record.setValue(field, value);
        }
        record.setState(RecordState.PERSISTED);
      }
      return record;
    };
  }

  private final String queryLabel;

  private Iterator<JsonObject> results;

  private URI nextURI;

  private final ApacheHttpRequestBuilderFactory requestFactory;

  private RecordFactory<Record> recordFactory;

  private final Function<JsonObject, Record> recordConverter;

  private int readCount;

  private ApacheHttpRequestBuilder request;

  private final RecordDefinition recordDefinition;

  private int limit = Integer.MAX_VALUE;

  public ODataQueryIterator(final ApacheHttpRequestBuilderFactory requestFactory,
    final ApacheHttpRequestBuilder request, final Function<JsonObject, Record> recordConverter,
    final RecordDefinition recordDefinition) {
    this.requestFactory = requestFactory;
    this.recordConverter = recordConverter;
    this.request = request;
    this.recordDefinition = recordDefinition;
    this.queryLabel = request.getUri().toString();
  }

  public ODataQueryIterator(final ApacheHttpRequestBuilderFactory requestFactory,
    final ApacheHttpRequestBuilder request, final RecordFactory<Record> recordFactory,
    final RecordDefinition recordDefinition) {
    this.requestFactory = requestFactory;
    this.recordConverter = recordFactoryConverter(recordDefinition, recordFactory);
    this.request = request;
    this.recordDefinition = recordDefinition;
    this.queryLabel = request.getUri().toString();
  }

  public ODataQueryIterator(final ODataRecordStore recordStore,
    final ApacheHttpRequestBuilderFactory requestFactory, final Query query,
    final Map<String, Object> properties) {
    RecordFactory<Record> recordFactory = query.getRecordFactory();
    if (recordFactory == null) {
      recordFactory = recordStore.getRecordFactory();
    }
    this.recordDefinition = query.getRecordDefinition();
    this.recordConverter = recordFactoryConverter(this.recordDefinition, recordFactory);
    this.requestFactory = requestFactory;
    this.queryLabel = query.toString();
    setProperties(properties);
    this.request = recordStore.newRequest(query);
    this.limit = query.getLimit();
  }

  void executeRequest() {
    final JsonObject json = this.request.getJson();
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
    if (this.readCount >= this.limit) {
      throw new NoSuchElementException();
    }
    final Iterator<JsonObject> results = this.results;
    do {
      if (results != null && results.hasNext()) {
        final JsonObject recordJson = results.next();
        this.readCount++;
        return this.recordConverter.apply(recordJson);
      }
      if (this.nextURI == null) {
        throw new NoSuchElementException();
      } else {
        this.request = this.requestFactory.get(this.nextURI);

        executeRequest();
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
    executeRequest();
  }

  @Override
  public String toString() {
    return this.queryLabel;
  }

}
