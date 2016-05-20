package com.revolsys.record.io.format.esri.rest;

import java.util.Collections;
import java.util.Map;

import com.revolsys.collection.map.MapEx;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Property;
import com.revolsys.util.UrlUtil;
import com.revolsys.webservice.WebServiceResource;

public abstract class ArcGisResponse extends AbstractMapWrapper implements CatalogElement {
  public static final Map<String, ? extends Object> FORMAT_PARAMETER = Collections.singletonMap("f",
    "json");

  private CatalogElement parent;

  private String resourceUrl;

  private String name;

  private double currentVersion;

  private boolean useProxy;

  public ArcGisResponse() {
  }

  protected ArcGisResponse(final CatalogElement parent) {
    this.parent = parent;
    this.resourceUrl = this.parent.getResourceUrl();
    this.useProxy = parent.isUseProxy();
  }

  protected ArcGisResponse(final CatalogElement parent, final String name) {
    this.name = name;
    this.parent = parent;
    this.resourceUrl = this.parent.getResourceUrl(getPathElement());
    this.useProxy = parent.isUseProxy();
  }

  public double getCurrentVersion() {
    return this.currentVersion;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <R extends WebServiceResource> R getParent() {
    return (R)this.parent;
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

  public Resource getResource(final String child, final Map<String, ? extends Object> parameters) {
    final String resourceUrl = getResourceUrl(child);

    final StringBuilder queryUrl = new StringBuilder(resourceUrl);
    if (isUseProxy()) {
      final String query = '?' + UrlUtil.getQueryString(parameters);
      queryUrl.append(UrlUtil.percentEncode(query));
    } else {
      queryUrl.append('?');
      UrlUtil.appendQuery(queryUrl, parameters);
    }

    final Resource resource = Resource.getResource(queryUrl);
    return resource;
  }

  @Override
  public String getResourceUrl() {
    return this.resourceUrl;
  }

  protected void initialize(final MapEx properties) {
    setProperties(properties);
  }

  @Override
  public boolean isUseProxy() {
    return this.useProxy;
  }

  @Override
  protected void refreshDo() {
    final String resourceUrl = getResourceUrl();
    final String url;
    if (isUseProxy()) {
      url = resourceUrl + "%3ff%3djson";
    } else {
      url = resourceUrl + "?f=json";
    }
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

  public void setParent(final CatalogElement parent) {
    this.parent = parent;
  }

  protected void setResourceUrl(final String resourceUrl) {
    this.resourceUrl = resourceUrl;
    this.useProxy = resourceUrl.matches(".+\\?.+rest%2fservices.*");
  }

  @Override
  public String toString() {
    return getName() + "\t" + getResourceUrl();
  }
}
