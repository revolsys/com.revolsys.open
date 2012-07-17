package com.revolsys.io.esri.map.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.revolsys.util.UrlUtil;

public class Catalog extends ArcGisResponse {

  private String name;

  public Catalog(String serviceUrl) {
    serviceUrl = serviceUrl.replaceAll("/+$", "");
    if (serviceUrl.endsWith("services")) {
      setServiceUrl(serviceUrl);
      name = "";
    } else {
      String parentUrl = UrlUtil.getParent(serviceUrl);
      Catalog parent = new Catalog(parentUrl);
      String name = UrlUtil.getFileName(serviceUrl);
      init(parent, name);
    }
  }

  private Catalog(Catalog catalog, String name) {
    super(catalog, name);
    this.name = name;
  }

  public String getName() {
    return name;
  }

  private List<Catalog> folders;

  public synchronized List<Catalog> getFolders() {
    if (folders == null) {
      folders = new ArrayList<Catalog>();
      List<String> folderNames = getValue("folders");
      if (folderNames != null) {
        for (String name : folderNames) {
          Catalog folder = new Catalog(this, name);
          folders.add(folder);
        }
      }
    }
    return folders;
  }

  private List<Service> services;

  public synchronized List<Service> getServices() {
    if (services == null) {
      services = new ArrayList<Service>();
      List<Map<String, Object>> serviceDescriptions = getValue("services");
      if (serviceDescriptions != null) {
        for (Map<String, Object> serviceDescription : serviceDescriptions) {
          String name = (String)serviceDescription.get("name");
          String type = (String)serviceDescription.get("type");
          Service service;
          try {
            Class<Service> serviceClass = (Class<Service>)getClass().forName(
              "com.revolsys.io.esri.map.rest." + type);
            service = serviceClass.newInstance();
            service.setCatalog(this);
            service.setServiceName(name);
          } catch (Throwable t) {
            service = new Service(this, name, type);
          }
          services.add(service);
        }
      }
    }
    return services;
  }

}
