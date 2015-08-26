package com.revolsys.format.esri.map.rest;

import java.util.Collections;
import java.util.Map;

import com.revolsys.format.json.Json;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.UrlUtil;

public class ArcGisResponse extends AbstractMapWrapper {
  public static final Map<String, ? extends Object> FORMAT_PARAMETER = Collections.singletonMap("f",
    "json");

  private Catalog catalog;

  private String path;

  private String serviceUrl;

  public ArcGisResponse() {
  }

  protected ArcGisResponse(final Catalog catalog, final String name) {
    init(catalog, name);
  }

  protected ArcGisResponse(final String serviceUrl) {
    setServiceUrl(serviceUrl);
  }

  public Double getCurrentVersion() {
    final Number version = getValue("currentVersion");
    if (version == null) {
      return null;
    } else {
      return version.doubleValue();
    }
  }

  public String getPath() {
    return this.path;
  }

  public String getServiceUrl() {
    return this.serviceUrl;
  }

  @Override
  public synchronized Map<String, Object> getValues() {
    Map<String, Object> values = super.getValues();
    if (values == null) {
      final Resource resource = Resource
        .getResource(UrlUtil.getUrl(this.serviceUrl + this.path, FORMAT_PARAMETER));
      values = Json.toMap(resource);
      setValues(values);
    }
    return values;
  }

  protected void init(final Catalog catalog, final String name) {
    this.catalog = catalog;
    setName(name);
  }

  protected void setCatalog(final Catalog catalog) {
    this.catalog = catalog;
  }

  protected void setName(final String name) {
    this.serviceUrl = this.catalog.getServiceUrl();
    this.path = "/" + name;
  }

  protected void setServiceUrl(final String serviceUrl) {
    this.serviceUrl = serviceUrl;
    this.path = "";
  }
}
