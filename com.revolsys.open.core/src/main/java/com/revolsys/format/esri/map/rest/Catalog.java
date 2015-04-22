package com.revolsys.format.esri.map.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.revolsys.util.UrlUtil;

public class Catalog extends ArcGisResponse {

  private String name;

  private List<Catalog> folders;

  private List<Service> services;

  private Catalog(final Catalog catalog, final String name) {
    super(catalog, name);
    this.name = name;
  }

  public Catalog(String serviceUrl) {
    serviceUrl = serviceUrl.replaceAll("/+$", "");
    if (serviceUrl.endsWith("services")) {
      setServiceUrl(serviceUrl);
      this.name = "";
    } else {
      final String parentUrl = UrlUtil.getParent(serviceUrl);
      final Catalog parent = new Catalog(parentUrl);
      final String name = UrlUtil.getFileName(serviceUrl);
      init(parent, name);
    }
  }

  public synchronized List<Catalog> getFolders() {
    if (this.folders == null) {
      this.folders = new ArrayList<Catalog>();
      final List<String> folderNames = getValue("folders");
      if (folderNames != null) {
        for (final String name : folderNames) {
          final Catalog folder = new Catalog(this, name);
          this.folders.add(folder);
        }
      }
    }
    return this.folders;
  }

  public String getName() {
    return this.name;
  }

  public Service getService(final String serviceName) {
    for (final Service service : getServices()) {
      if (service.getServiceName().equals(serviceName)) {
        return service;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public <T extends Service> T getService(final String name, final Class<T> serviceClass) {
    for (final Service service : getServices()) {
      final String serviceName = service.getServiceName();
      if (serviceName.equals(name)) {
        if (serviceClass.isAssignableFrom(service.getClass())) {
          return (T)service;
        } else {
          throw new IllegalArgumentException("ArcGIS REST service is not a "
            + serviceClass.getName() + ": " + name);
        }
      }
    }
    for (final Service service : getServices()) {
      final String serviceName = service.getServiceName();
      if (serviceName.endsWith(name)) {
        if (serviceClass.isAssignableFrom(service.getClass())) {
          return (T)service;
        } else {
          throw new IllegalArgumentException("ArcGIS REST service is not a "
            + serviceClass.getName() + ": " + name);
        }
      }
    }
    return null;
  }

  public synchronized List<Service> getServices() {
    if (this.services == null) {
      this.services = new ArrayList<Service>();
      final List<Map<String, Object>> serviceDescriptions = getValue("services");
      if (serviceDescriptions != null) {
        for (final Map<String, Object> serviceDescription : serviceDescriptions) {
          final String name = (String)serviceDescription.get("name");
          final String type = (String)serviceDescription.get("type");
          Service service;
          try {
            getClass();
            final Class<Service> serviceClass = (Class<Service>)Class.forName("com.revolsys.format.esri.map.rest."
              + type);
            service = serviceClass.newInstance();
            service.setCatalog(this);
            service.setServiceName(name);
          } catch (final Throwable t) {
            service = new Service(this, name, type);
          }
          this.services.add(service);
        }
      }
    }
    return this.services;
  }

}
