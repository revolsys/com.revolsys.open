package com.revolsys.io.esri.map.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Catalog extends ArcGisResponse {

  private String name;

  public Catalog(String serviceUrl) {
    super(serviceUrl);
    name = "";
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
          Service folder = new Service(this, name, type);
          services.add(folder);
        }
      }
    }
    return services;
  }

}
