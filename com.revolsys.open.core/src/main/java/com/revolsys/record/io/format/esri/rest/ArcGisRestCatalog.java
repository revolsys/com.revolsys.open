package com.revolsys.record.io.format.esri.rest;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.io.PathName;
import com.revolsys.io.map.MapObjectFactoryRegistry;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.logging.Logs;
import com.revolsys.record.io.format.esri.rest.map.ArcGisRestMapServer;
import com.revolsys.util.UrlUtil;
import com.revolsys.util.function.Function2;
import com.revolsys.webservice.WebService;

public class ArcGisRestCatalog extends ArcGisResponse
  implements WebService<CatalogElement>, CatalogElement, MapSerializer {

  private static final Map<String, Function2<ArcGisRestCatalog, String, ArcGisRestService>> SERVICE_FACTORY_BY_TYPE = Maps
    .<String, Function2<ArcGisRestCatalog, String, ArcGisRestService>> buildHash()//
    .add("MapServer", ArcGisRestMapServer::new) //
    .getMap();

  public static void mapObjectFactoryInit() {
    MapObjectFactoryRegistry.newFactory("arcGisRestServer", "Arc GIS REST Server",
      ArcGisRestCatalog::new);
  }

  private final List<ArcGisRestCatalog> folders = new ArrayList<>();

  private final List<ArcGisRestService> services = new ArrayList<>();

  private final Map<String, CatalogElement> childByName = new HashMap<>();

  private ArcGisRestCatalog parent;

  private List<CatalogElement> children;

  protected ArcGisRestCatalog() {
  }

  private ArcGisRestCatalog(final ArcGisRestCatalog arcGisRestCatalog, final String path) {
    super(arcGisRestCatalog, path);
  }

  public ArcGisRestCatalog(final Map<String, ? extends Object> properties) {
    setProperties(properties);
  }

  public ArcGisRestCatalog(final String serviceUrl) {
    setServiceUrl(serviceUrl);
  }

  @SuppressWarnings("unchecked")
  public <T extends CatalogElement> T getCatalogElement(final PathName pathName) {
    final List<String> elements = pathName.getElements();
    if (!elements.isEmpty()) {
      CatalogElement catalogElement = getChild(elements.get(0));
      for (int i = 1; catalogElement != null && i < elements.size(); i++) {
        final String childLayerName = elements.get(i);
        catalogElement = catalogElement.getChild(childLayerName);
      }
      return (T)catalogElement;
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public <T extends CatalogElement> T getCatalogElement(final PathName pathName,
    final Class<T> elementClass) {
    if (pathName != null) {
      final List<String> elements = pathName.getElements();
      if (!elements.isEmpty()) {
        CatalogElement catalogElement = getChild(elements.get(0));
        for (int i = 1; catalogElement != null && i < elements.size(); i++) {
          final String childLayerName = elements.get(i);
          catalogElement = catalogElement.getChild(childLayerName);
        }
        if (catalogElement == null) {
          return null;
        } else if (elementClass.isAssignableFrom(catalogElement.getClass())) {
          return (T)catalogElement;
        } else {
          throw new IllegalArgumentException(
            "ArcGIS REST resource " + pathName + " is not a " + elementClass.getName());
        }
      }
    }
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <C extends CatalogElement> C getChild(final String name) {
    initChildren();
    return (C)this.childByName.get(name);
  }

  @Override
  public List<CatalogElement> getChildren() {
    initChildren();
    return this.children;
  }

  public synchronized List<ArcGisRestCatalog> getFolders() {
    initChildren();
    return this.folders;
  }

  @Override
  public String getIconName() {
    return "world";
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

  @Override
  public ArcGisRestCatalog getParent() {
    return this.parent;
  }

  public ArcGisRestService getService(final String serviceName) {
    for (final ArcGisRestService arcGisRestService : getServices()) {
      if (arcGisRestService.getName().equals(serviceName)) {
        return arcGisRestService;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public <T extends ArcGisRestService> T getService(final String name,
    final Class<T> serviceClass) {
    final CatalogElement child = getChild(name);
    if (child != null) {
      if (serviceClass.isAssignableFrom(child.getClass())) {
        return (T)child;
      } else {
        throw new IllegalArgumentException(
          "ArcGIS REST service is not a " + serviceClass.getName() + ": " + name);
      }
    }
    return null;
  }

  public synchronized List<ArcGisRestService> getServices() {
    initChildren();
    return this.services;
  }

  private void initChildren() {
    synchronized (this.childByName) {
      if (this.children == null) {
        this.children = new ArrayList<>();
        final List<String> folderNames = getValue("folders");
        if (folderNames != null) {
          for (final String name : folderNames) {
            final ArcGisRestCatalog folder = new ArcGisRestCatalog(this, name);
            this.folders.add(folder);
            this.children.add(folder);
            final String serviceName = PathName.newPathName(name).getName();
            this.childByName.put(serviceName, folder);
          }
        }
        final List<Map<String, Object>> serviceDescriptions = getValue("services");
        if (serviceDescriptions != null) {
          for (final Map<String, Object> serviceDescription : serviceDescriptions) {
            final String servicePath = (String)serviceDescription.get("name");
            final String serviceType = (String)serviceDescription.get("type");
            ArcGisRestService service;
            try {
              final Function2<ArcGisRestCatalog, String, ArcGisRestService> serviceFactory = SERVICE_FACTORY_BY_TYPE
                .get(serviceType);
              if (serviceFactory == null) {
                service = new ArcGisRestService(this, servicePath, serviceType);
              } else {
                service = serviceFactory.apply(this, servicePath);
              }
              this.services.add(service);
              this.children.add(service);
              final String serviceName = PathName.newPathName(servicePath).getName();
              this.childByName.put(serviceName, service);

            } catch (final Throwable e) {
              Logs.error(this, "Unable to get service: " + getResourceUrl() + "/" + servicePath, e);
            }
          }
        }
      }
    }
  }

  public void setParent(final ArcGisRestCatalog parent) {
    this.parent = parent;
  }

  @Override
  public void setServiceUrl(String serviceUrl) {
    serviceUrl = serviceUrl.replaceAll("/+$", "");
    if (serviceUrl.endsWith("services")) {
      super.setServiceUrl(serviceUrl);
      setPath("");
    } else {
      final String parentUrl = UrlUtil.getParent(serviceUrl);
      final ArcGisRestCatalog parent = new ArcGisRestCatalog(parentUrl);
      final String path = UrlUtil.getFileName(serviceUrl);
      init(parent, path);
    }
  }

  @Override
  public MapEx toMap() {
    final MapEx map = newTypeMap("arcGisRestServer");
    final String serviceUrl = getServiceUrl();
    map.put("serviceUrl", serviceUrl);
    final String name = getName();
    addToMap(map, "name", name, "");
    return map;
  }
}
