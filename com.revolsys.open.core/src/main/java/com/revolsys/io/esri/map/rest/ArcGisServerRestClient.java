package com.revolsys.io.esri.map.rest;

import java.util.Collections;
import java.util.Map;

import org.springframework.core.io.Resource;

import com.revolsys.io.json.JsonMapIoFactory;
import com.revolsys.spring.SpringUtil;
import com.revolsys.util.UrlUtil;

public class ArcGisServerRestClient {
  private static final Map<String, ? extends Object> FORMAT_PARAMETER = Collections.singletonMap(
    "f", "json");

  private String baseUrl;

  public ArcGisServerRestClient(String baseUrl) {
    super();
    this.baseUrl = baseUrl;
  }

  public Map<String, Object> getMapServer(String serviceName) {
    Resource resource = SpringUtil.getResource(UrlUtil.getUrl(baseUrl + "/"
      + serviceName + "/MapServer", FORMAT_PARAMETER));
    return JsonMapIoFactory.toMap(resource);
  }
}
