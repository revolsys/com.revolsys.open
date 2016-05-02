package com.revolsys.record.io.format.esri.map.rest;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.revolsys.io.PathName;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.util.UrlUtil;
import com.revolsys.webservice.WebService;

public class Catalog extends ArcGisResponse
  implements WebService<CatalogElement>, CatalogElement, MapSerializer {
  private List<Catalog> folders;

  private List<Service> services;

  private Catalog(final Catalog catalog, final String path) {
    super(catalog, path);
  }

  public Catalog(final Map<String, Object> properties) {
    setProperties(properties);
  }

  public Catalog(final String serviceUrl) {
    setServiceUrl(serviceUrl);
  }

  @Override
  public List<CatalogElement> getChildren() {
    final List<CatalogElement> children = new ArrayList<>();
    final List<Catalog> folders = getFolders();
    children.addAll(folders);
    final List<Service> services = getServices();
    children.addAll(services);
    return children;
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

  @Override
  public String getName() {
    String name = super.getName();
    if (name == null) {
      final String path = getPath();
      final PathName pathName = PathName.newPathName(path);
      if (pathName == null || pathName.equals("/")) {
        final String serviceUrl = getServiceUrl();
        try {
          final URI uri = new URI(serviceUrl);
          return uri.getHost();
        } catch (final Throwable e) {
          return "???";
        }
      } else {
        name = pathName.getName();
        setName(name);
      }
    }
    return name;
  }

  public Service getService(final String serviceName) {
    for (final Service service : getServices()) {
      if (service.getName().equals(serviceName)) {
        return service;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public <T extends Service> T getService(final String name, final Class<T> serviceClass) {
    for (final Service service : getServices()) {
      final String serviceName = service.getName();
      if (serviceName.equals(name)) {
        if (serviceClass.isAssignableFrom(service.getClass())) {
          return (T)service;
        } else {
          throw new IllegalArgumentException(
            "ArcGIS REST service is not a " + serviceClass.getName() + ": " + name);
        }
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public synchronized List<Service> getServices() {
    if (this.services == null) {
      this.services = new ArrayList<Service>();
      final List<Map<String, Object>> serviceDescriptions = getValue("services");
      if (serviceDescriptions != null) {
        for (final Map<String, Object> serviceDescription : serviceDescriptions) {
          final String servicePath = (String)serviceDescription.get("name");
          final String serviceType = (String)serviceDescription.get("type");
          Service service;
          try {
            final Class<Service> serviceClass = (Class<Service>)Class
              .forName("com.revolsys.record.io.format.esri.map.rest." + serviceType);
            service = serviceClass.newInstance();
            service.setService(this, servicePath, serviceType);
          } catch (final Throwable t) {
            service = new Service(this, servicePath, serviceType);
          }
          this.services.add(service);
        }
      }
    }
    return this.services;
  }

  @Override
  public void setServiceUrl(String serviceUrl) {
    serviceUrl = serviceUrl.replaceAll("/+$", "");
    if (serviceUrl.endsWith("services")) {
      super.setServiceUrl(serviceUrl);
      setPath("");
    } else {
      final String parentUrl = UrlUtil.getParent(serviceUrl);
      final Catalog parent = new Catalog(parentUrl);
      final String path = UrlUtil.getFileName(serviceUrl);
      init(parent, path);
    }
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = newTypeMap("arcgisRestServer");
    final String serviceUrl = getServiceUrl();
    map.put("serviceUrl", serviceUrl);
    final String path = getPath();
    map.put("path", path);
    addToMap(map, "path", path, "");
    final String name = getName();
    addToMap(map, "name", name, "");
    return map;
  }

}
