package com.revolsys.record.io.format.esri.rest;

import java.util.Collections;
import java.util.Map;

import com.revolsys.collection.map.MapEx;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Property;
import com.revolsys.util.UrlUtil;

public class ArcGisResponse extends AbstractMapWrapper {
  public static final Map<String, ? extends Object> FORMAT_PARAMETER = Collections.singletonMap("f",
    "json");

  private ArcGisRestCatalog catalog;

  private String path = "";

  private String serviceUrl;

  private String name;

  private double currentVersion;

  public ArcGisResponse() {
  }

  protected ArcGisResponse(final ArcGisRestCatalog catalog, final String path) {
    init(catalog, path);
  }

  protected ArcGisResponse(final String serviceUrl) {
    setServiceUrl(serviceUrl);
  }

  public ArcGisRestCatalog getCatalog() {
    return this.catalog;
  }

  public double getCurrentVersion() {
    return this.currentVersion;
  }

  public String getName() {
    return this.name;
  }

  public String getPath() {
    return this.path;
  }

  @Override
  public synchronized MapEx getProperties() {
    final MapEx properties = super.getProperties();
    if (Property.isEmpty(properties)) {
      properties.put("initializing", true);

      properties.put("initializing", false);
    }
    return properties;
  }

  public String getResourceUrl() {
    return this.serviceUrl + this.path;
  }

  public String getServiceUrl() {
    return this.serviceUrl;
  }

  protected void init(final ArcGisRestCatalog catalog, final String path) {
    this.catalog = catalog;
    if (catalog != null) {
      this.serviceUrl = this.catalog.getServiceUrl();
    }
    setPath(path);
  }

  protected void initialize(final MapEx properties) {
    setProperties(properties);
  }

  @Override
  protected void refreshDo() {
    final String resourceUrl = getResourceUrl();
    final String url = UrlUtil.getUrl(resourceUrl, FORMAT_PARAMETER);
    final Resource resource = Resource.getResource(url);
    final MapEx newProperties = Json.toMap(resource);
    initialize(newProperties);
  }

  public void setCurrentVersion(final double currentVersion) {
    this.currentVersion = currentVersion;
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
    return getName() + "\t" + getResourceUrl();
  }
}