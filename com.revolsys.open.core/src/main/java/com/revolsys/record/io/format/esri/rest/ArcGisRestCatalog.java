package com.revolsys.record.io.format.esri.rest;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.io.PathName;
import com.revolsys.io.map.MapObjectFactoryRegistry;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.logging.Logs;
import com.revolsys.record.io.format.esri.rest.feature.ArcGisRestFeatureService;
import com.revolsys.record.io.format.esri.rest.map.ArcGisRestMapService;
import com.revolsys.util.Property;
import com.revolsys.webservice.WebService;

public class ArcGisRestCatalog extends ArcGisResponse
  implements WebService<CatalogElement>, CatalogElement, MapSerializer {

  private static final Map<String, Function<ArcGisRestServiceContainer, ArcGisRestService>> SERVICE_FACTORY_BY_TYPE = Maps
    .<String, Function<ArcGisRestServiceContainer, ArcGisRestService>> buildHash()//
    .add("MapServer", ArcGisRestMapService::new) //
    .add("FeatureServer", ArcGisRestFeatureService::new) //
    .getMap();

  public static void mapObjectFactoryInit() {
    MapObjectFactoryRegistry.newFactory("arcGisRestServer", "Arc GIS REST Server",
      ArcGisRestCatalog::newArcGisRestCatalog);
  }

  public static ArcGisRestCatalog newArcGisRestCatalog(
    final Map<String, ? extends Object> properties) {
    final String serviceUrl = (String)properties.get("serviceUrl");
    if (Property.hasValue(serviceUrl)) {
      final ArcGisRestCatalog service = newArcGisRestCatalog(serviceUrl);
      service.setProperties(properties);
      return service;
    } else {
      throw new IllegalArgumentException("Missing serviceUrl");
    }
  }

  // http://vmgis.vernon.ca/proxy.ashx?http%3a%2f%2f10.200.0.3%2farcgis%2frest%2fservices%2fMaps%2fLocalWildfireInfo%2fMapServer%3ff%3djson
  public static ArcGisRestCatalog newArcGisRestCatalog(String rootUrl) {
    rootUrl = rootUrl.replaceAll("/+$", "");
    if (rootUrl.endsWith("/services") || rootUrl.endsWith("%2fservices")) {
      return new ArcGisRestCatalog(rootUrl);
    } else if (rootUrl.endsWith("/rest")) {
      return new ArcGisRestCatalog(rootUrl + "/services");
    } else {
      final int index = rootUrl.indexOf("/rest/services");
      if (index == -1) {
        throw new IllegalArgumentException(
          "ArgGIS rest services must include the path /rest/services");
      } else {
        final String url = rootUrl.substring(0, index + 14);
        return new ArcGisRestCatalog(url);
      }
    }
  }

  private Map<String, CatalogElement> childByName = new HashMap<>();

  private List<CatalogElement> children;

  private ArcGisRestCatalog(final ArcGisRestCatalog parent, final String path) {
    super(parent, path);
  }

  private ArcGisRestCatalog(final String rootUrl) {
    setResourceUrl(rootUrl);
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
    refreshIfNeeded();
    if (name == null) {
      return null;
    } else {
      final String childKey = name.toLowerCase();
      return (C)this.childByName.get(childKey);
    }
  }

  @Override
  public List<CatalogElement> getChildren() {
    refreshIfNeeded();
    return this.children;
  }

  @Override
  public String getIconName() {
    return "folder:world";
  }

  @Override
  public String getName() {
    String name = super.getName();
    if (name == null) {
      final PathName pathName = getPathName();
      if (pathName == null || pathName.equals("/")) {
        final String resourceUrl = getResourceUrl();
        try {
          final URI uri = new URI(resourceUrl);
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

  @Override
  protected void initialize(final MapEx properties) {
    final List<CatalogElement> children = new ArrayList<>();
    final Map<String, CatalogElement> childByName = new HashMap<>();
    final List<String> folderNames = properties.getValue("folders", Collections.emptyList());
    for (final String folderPath : folderNames) {
      final String folderName = PathName.newPathName(folderPath).getName();
      final ArcGisRestCatalog folder = new ArcGisRestCatalog(this, folderName);
      children.add(folder);
      final String childKey = folderName.toLowerCase();
      childByName.put(childKey, folder);
    }
    final List<MapEx> serviceDescriptions = properties.getValue("services",
      Collections.emptyList());
    for (final MapEx serviceDescription : serviceDescriptions) {
      final String serviceContainerPath = serviceDescription.getString("name");
      final String serviceType = serviceDescription.getString("type");
      final String serviceContainerName = PathName.newPathName(serviceContainerPath).getName();
      try {
        final String childKey = serviceContainerName.toLowerCase();
        ArcGisRestServiceContainer container = (ArcGisRestServiceContainer)childByName
          .get(childKey);
        if (container == null) {
          container = new ArcGisRestServiceContainer(this, serviceContainerName);
          childByName.put(childKey, container);
          children.add(container);
        }
        ArcGisRestService service;
        final Function<ArcGisRestServiceContainer, ArcGisRestService> serviceFactory = SERVICE_FACTORY_BY_TYPE
          .get(serviceType);
        if (serviceFactory == null) {
          service = new ArcGisRestService(container, serviceType);
        } else {
          service = serviceFactory.apply(container);
        }
        container.addService(service);
      } catch (final Throwable e) {
        Logs.error(this, "Unable to get service: " + getResourceUrl() + "/" + serviceContainerPath,
          e);
      }
    }
    this.childByName = Collections.unmodifiableMap(childByName);
    this.children = Collections.unmodifiableList(children);
  }

  @Override
  public MapEx toMap() {
    final MapEx map = newTypeMap("arcGisRestServer");
    final String serviceUrl = getResourceUrl();
    map.put("serviceUrl", serviceUrl);
    final String name = getName();
    addToMap(map, "name", name, "");
    return map;
  }
}
