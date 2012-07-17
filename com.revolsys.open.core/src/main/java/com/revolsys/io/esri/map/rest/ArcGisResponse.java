package com.revolsys.io.esri.map.rest;

import java.util.Collections;
import java.util.Map;

import org.springframework.core.io.Resource;

import com.revolsys.io.json.JsonMapIoFactory;
import com.revolsys.spring.SpringUtil;
import com.revolsys.util.UrlUtil;

public class ArcGisResponse extends AbstractMapWrapper {
  public static final Map<String, ? extends Object> FORMAT_PARAMETER = Collections.singletonMap(
    "f", "json");

  private String serviceUrl;

  private String path;

  private Catalog catalog;

  public ArcGisResponse() {
  }

  protected ArcGisResponse(final Catalog catalog, final String name) {
    init(catalog, name);
  }

  protected void init(final Catalog catalog, final String name) {
    this.catalog = catalog;
    setName(name);
  }

  protected ArcGisResponse(final String serviceUrl) {
    setServiceUrl(serviceUrl);
  }

  protected void setServiceUrl(final String serviceUrl) {
    this.serviceUrl = serviceUrl;
    this.path = "";
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
    return path;
  }

  public synchronized Map<String, Object> getValues() {
    Map<String, Object> values = super.getValues();
    if (values == null) {
      final Resource resource = SpringUtil.getResource(UrlUtil.getUrl(
        serviceUrl + path, FORMAT_PARAMETER));
      values = JsonMapIoFactory.toMap(resource);
      setValues(values);
    }
    return values;
  }

  public String getServiceUrl() {
    return serviceUrl;
  }

  protected void setCatalog(final Catalog catalog) {
    this.catalog = catalog;
  }

  protected void setName(final String name) {
    this.serviceUrl = catalog.getServiceUrl();
    this.path = "/" + name;
  }
}
