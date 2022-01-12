package com.revolsys.odata.model;

import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.http.ApacheHttpRequestBuilder;
import com.revolsys.http.ApacheHttpRequestBuilderFactory;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordIterator;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.UriBuilder;

public class ODataQueryIterator extends AbstractIterator<Record>
  implements RecordReader, RecordIterator {

  private final RecordDefinition recordDefinition;

  private final ODataRecordStore recordStore;

  private final Query query;

  private Iterator<JsonObject> results;

  private URI nextURI;

  private final ApacheHttpRequestBuilderFactory requestFactory;

  public ODataQueryIterator(final ODataRecordStore recordStore,
    final ApacheHttpRequestBuilderFactory requestFactory, final Query query,
    final Map<String, Object> properties) {
    this.recordStore = recordStore;
    this.requestFactory = requestFactory;
    this.query = query;
    this.recordDefinition = query.getRecordDefinition();
    setProperties(properties);
  }

  void executeRequest(final ApacheHttpRequestBuilder request) {
    final JsonObject json = request.getJson();
    this.nextURI = json.getValue("@odata.nextLink", DataTypes.ANY_URI);
    this.results = json.getJsonList("value").jsonObjects().iterator();
  }

  @Override
  protected Record getNext() throws NoSuchElementException {
    do {
      if (this.results != null && this.results.hasNext()) {
        final JsonObject recordJson = this.results.next();
        return this.recordDefinition.newRecord(recordJson);
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
    final String name = this.query.getTablePath().getName();
    final URI baseUri = this.recordStore.getUri();
    final URI uri = new UriBuilder(baseUri).appendPathSegments(name).build();
    final ApacheHttpRequestBuilder request = this.requestFactory.get(uri)
      .addParameter(ODataRecordStore.FORMAT_JSON);
    final int offset = this.query.getOffset();
    if (offset > 0) {
      request.addParameter("$top", offset);
    }
    final int limit = this.query.getLimit();
    if (limit > 0 && limit < Integer.MAX_VALUE) {
      request.addParameter("$top", limit);
    }
    executeRequest(request);
  }

}
