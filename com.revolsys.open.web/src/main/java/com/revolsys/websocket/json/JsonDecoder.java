package com.revolsys.websocket.json;

import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import com.revolsys.collection.map.MapEx;
import com.revolsys.record.io.format.json.Json;

public class JsonDecoder implements Decoder.Text<MapEx> {

  @Override
  public MapEx decode(final String string) {
    return Json.toObjectMap(string);
  }

  @Override
  public void destroy() {
  }

  @Override
  public void init(final EndpointConfig config) {
  }

  @Override
  public boolean willDecode(final String string) {
    return string != null && string.charAt(0) == '{';
  }
}
