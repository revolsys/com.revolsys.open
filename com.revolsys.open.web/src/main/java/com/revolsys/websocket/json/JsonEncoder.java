package com.revolsys.websocket.json;

import java.util.Map;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.revolsys.format.json.Json;

public class JsonEncoder implements Encoder.Text<Map<String, Object>> {

  @Override
  public void destroy() {
  }

  @Override
  public String encode(final Map<String, Object> map) throws EncodeException {
    return Json.toString(map);
  }

  @Override
  public void init(final EndpointConfig config) {
  }
}
