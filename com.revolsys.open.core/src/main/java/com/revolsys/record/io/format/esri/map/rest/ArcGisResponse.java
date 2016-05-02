package com.revolsys.record.io.format.esri.map.rest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.revolsys.properties.ObjectWithProperties;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Property;
import com.revolsys.util.UrlUtil;

public class ArcGisResponse extends AbstractMapWrapper implements ObjectWithProperties {
  public static final Map<String, ? extends Object> FORMAT_PARAMETER = Collections.singletonMap("f",
    "json");

  private final Map<String, Object> properties = new HashMap<>();

  private Catalog catalog;

  private String path = "";

  private String serviceUrl;

  private String name;

  public ArcGisResponse() {
  }

  protected ArcGisResponse(final Catalog catalog, final String path) {
    init(catalog, path);
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

  public String getName() {
    return this.name;
  }

  public String getPath() {
    return this.path;
  }

  @Override
  public Map<String, Object> getProperties() {
    return this.properties;
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

  protected void init(final Catalog catalog, final String path) {
    setCatalog(catalog);
    setPath(path);
  }

  protected void setCatalog(final Catalog catalog) {
    this.catalog = catalog;
    if (catalog != null) {
      this.serviceUrl = this.catalog.getServiceUrl();
    }
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setPath(final String path) {
    if (Property.hasValue(path)) {
      if (path.startsWith("/")) {
        this.path = path;
      } else {
        this.path = "/" + path;
      }
    } else {
      this.path = "";
    }
  }

  protected void setServiceUrl(final String serviceUrl) {
    this.serviceUrl = serviceUrl;
    this.path = "";
  }

  @Override
  public String toString() {
    return getName();
  }
}
