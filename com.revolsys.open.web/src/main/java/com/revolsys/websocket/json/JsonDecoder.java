package com.revolsys.websocket.json;

import java.util.Map;

import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import com.revolsys.record.io.format.json.Json;

public class JsonDecoder implements Decoder.Text<Map<String, Object>> {

  @Override
  public Map<String, Object> decode(final String string) {
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
