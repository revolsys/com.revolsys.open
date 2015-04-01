package com.revolsys.websocket.json;

import java.util.Map;

import com.revolsys.websocket.AsyncResult;

public class JsonPropertyAsyncResult implements AsyncResult<Map<String, Object>> {

  private final String propertyName;

  public JsonPropertyAsyncResult(final String propertyName) {
    this.propertyName = propertyName;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getResult(final Map<String, Object> result) {
    return (V)result.get(this.propertyName);
  }
}
