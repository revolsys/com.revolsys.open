package com.revolsys.io.esri.map.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.measure.unit.BaseUnit;

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
      name = "";
    } else {
      final String parentUrl = UrlUtil.getParent(serviceUrl);
      final Catalog parent = new Catalog(parentUrl);
      final String name = UrlUtil.getFileName(serviceUrl);
      init(parent, name);
    }
  }

  public synchronized List<Catalog> getFolders() {
    if (folders == null) {
      folders = new ArrayList<Catalog>();
      final List<String> folderNames = getValue("folders");
      if (folderNames != null) {
        for (final String name : folderNames) {
          final Catalog folder = new Catalog(this, name);
          folders.add(folder);
        }
      }
    }
    return folders;
  }

  public String getName() {
    return name;
  }

  public synchronized List<Service> getServices() {
    if (services == null) {
      services = new ArrayList<Service>();
      final List<Map<String, Object>> serviceDescriptions = getValue("services");
      if (serviceDescriptions != null) {
        for (final Map<String, Object> serviceDescription : serviceDescriptions) {
          final String name = (String)serviceDescription.get("name");
          final String type = (String)serviceDescription.get("type");
          Service service;
          try {
            getClass();
            final Class<Service> serviceClass = (Class<Service>)Class.forName("com.revolsys.io.esri.map.rest."
              + type);
            service = serviceClass.newInstance();
            service.setCatalog(this);
            service.setServiceName(name);
          } catch (final Throwable t) {
            service = new Service(this, name, type);
          }
          services.add(service);
        }
      }
    }
    return services;
  }

  public Service getService(String serviceName) {
    for (Service service : getServices()) {
      if (service.getServiceName().equals(serviceName)) {
        return service;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public <T extends Service> T getService(String name,
    Class<T> serviceClass) {
    for (Service service : getServices()) {
      String serviceName = service.getServiceName();
      if (serviceName.equals(name)) {
        if (serviceClass.isAssignableFrom(service.getClass())) {
          return (T)service;
        } else {
          throw new IllegalArgumentException("ArcGIS REST service is not a "
            + serviceClass.getName() + ": " + name);
        }
      }
    }
    for (Service service : getServices()) {
      String serviceName = service.getServiceName();
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

}
