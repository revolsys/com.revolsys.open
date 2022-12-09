package com.revolsys.websocket.json;

import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import jakarta.websocket.EndpointConfig;

import com.revolsys.collection.map.MapEx;
import com.revolsys.record.io.format.json.Json;

public class JsonEncoder implements Encoder.Text<MapEx> {

  @Override
  public void destroy() {
  }

  @Override
  public String encode(final MapEx map) throws EncodeException {
    return Json.toString(map);
  }

  @Override
  public void init(final EndpointConfig config) {
  }

}
