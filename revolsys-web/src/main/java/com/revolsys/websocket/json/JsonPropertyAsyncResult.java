package com.revolsys.websocket.json;

import com.revolsys.collection.map.MapEx;
import com.revolsys.websocket.AsyncResult;

public class JsonPropertyAsyncResult implements AsyncResult<MapEx> {

  private final String propertyName;

  public JsonPropertyAsyncResult(final String propertyName) {
    this.propertyName = propertyName;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getResult(final MapEx result) {
    return (V)result.get(this.propertyName);
  }
}
