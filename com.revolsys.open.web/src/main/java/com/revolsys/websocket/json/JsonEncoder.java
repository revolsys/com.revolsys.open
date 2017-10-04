package com.revolsys.websocket.json;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

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
