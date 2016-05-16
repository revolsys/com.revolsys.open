package com.revolsys.record.io.format.esri.rest;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.collection.Parent;
import com.revolsys.collection.list.Lists;

public class ArcGisRestServiceContainer implements CatalogElement, Parent<ArcGisRestService> {
  private List<ArcGisRestService> services = Collections.emptyList();

  private final Map<String, ArcGisRestService> serviceByType = new TreeMap<>();

  private final ArcGisRestCatalog parent;

  private final String name;

  private final String resourceUrl;

  private final boolean useProxy;

  public ArcGisRestServiceContainer(final ArcGisRestCatalog parent, final String name) {
    this.parent = parent;
    this.name = name;
    this.resourceUrl = parent.getResourceUrl(name);
    this.useProxy = parent.isUseProxy();
  }

  public void addService(final ArcGisRestService service) {
    final String serviceType = service.getServiceType();
    this.serviceByType.put(serviceType, service);
    this.services = Collections.unmodifiableList(Lists.toArray(this.serviceByType.values()));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <C extends CatalogElement> C getChild(final String name) {
    final ArcGisRestService service = this.serviceByType.get(name);
    if (service == null) {
      if (this.services.size() == 1) {
        return this.services.get(0).getChild(name);
      } else {
        return null;
      }
    } else {
      return (C)service;
    }
  }

  @Override
  public List<ArcGisRestService> getChildren() {
    return this.services;
  }

  @Override
  public String getIconName() {
    return "folder:world";
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public ArcGisRestCatalog getParent() {
    return this.parent;
  }

  @Override
  public String getResourceUrl() {
    return this.resourceUrl;
  }

  @Override
  public boolean isUseProxy() {
    return this.useProxy;
  }
}
