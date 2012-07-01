package com.revolsys.io.esri.map.rest;

import java.util.Collections;
import java.util.Map;

import org.springframework.core.io.Resource;

import com.revolsys.io.json.JsonMapIoFactory;
import com.revolsys.spring.SpringUtil;
import com.revolsys.util.UrlUtil;

public class ArcGisResponse {
  public static final Map<String, ? extends Object> FORMAT_PARAMETER = Collections.singletonMap(
    "f", "json");

  private Map<String, Object> response;

  private String serviceUrl;

  private String path;

  protected ArcGisResponse(String serviceUrl) {
    this.serviceUrl = serviceUrl;
    this.path = "";
  }

  public String getServiceUrl() {
    return serviceUrl;
  }

  public String getPath() {
    return path;
  }

  protected ArcGisResponse(Catalog catalog, String name) {
    this.serviceUrl = catalog.getServiceUrl();
    this.path = "/" + name;
  }

  public synchronized Map<String, Object> getResponse() {
    if (response == null) {
      Resource resource = SpringUtil.getResource(UrlUtil.getUrl(serviceUrl
        + path, FORMAT_PARAMETER));
      response = JsonMapIoFactory.toMap(resource);
    }
    return response;
  }

  @SuppressWarnings("unchecked")
  public <T> T getValue(String name) {
    Map<String, Object> response = getResponse();
    return (T)response.get(name);
  }

  public Double getCurrentVersion() {
    Number version = getValue("currentVersion");
    if (version == null) {
      return null;
    } else {
      return version.doubleValue();
    }
  }
}
