package com.revolsys.http;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import com.revolsys.record.io.format.json.JsonType;

public class JsonEntity extends StringEntity {

  public JsonEntity(final JsonType json) {
    super(json.toJsonString(false), ContentType.APPLICATION_JSON);
  }

}
