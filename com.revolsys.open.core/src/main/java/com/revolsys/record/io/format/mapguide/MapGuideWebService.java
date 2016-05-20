package com.revolsys.record.io.format.mapguide;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.io.PathName;
import com.revolsys.io.map.MapObjectFactoryRegistry;
import com.revolsys.logging.Logs;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.spring.resource.Resource;
import com.revolsys.spring.resource.UrlResource;
import com.revolsys.util.Property;
import com.revolsys.util.UrlUtil;
import com.revolsys.webservice.WebService;
import com.revolsys.webservice.WebServiceResource;

public class MapGuideWebService implements WebService<MapGuideResource> {
  public static final String J_TYPE = "mapGuideWebServer";

  private static final Map<String, Function<MapEx, ResourceDocument>> RESOURCE_DOCUMENT_FACTORIES = Maps
    .<String, Function<MapEx, ResourceDocument>> buildHash() //
    .add("ApplicationDefinition", ApplicationDefinition::new)
    .add("DrawingSource", DrawingSource::new)
    .add("FeatureSource", FeatureSource::new)
    .add("LayerDefinition", LayerDefinition::new)
    .add("LoadProcedure", LoadProcedure::new)
    .add("MapDefinition", MapDefinition::new)
    .add("SymbolDefinition", SymbolDefinition::new)
    .add("SymbolLibrary", SymbolLibrary::new)
    .add("WatermarkDefinition", WatermarkDefinition::new)
    .getMap();

  public static void mapObjectFactoryInit() {
    MapObjectFactoryRegistry.newFactory(J_TYPE, "Map Guide Web Server",
      MapGuideWebService::newMapGuideWebService);
  }

  public static MapGuideWebService newMapGuideWebService(
    final Map<String, ? extends Object> properties) {
    final String serviceUrl = (String)properties.get("serviceUrl");
    if (Property.hasValue(serviceUrl)) {
      final MapGuideWebService service = new MapGuideWebService(serviceUrl);
      // service.setProperties(properties);
      return service;
    } else {
      throw new IllegalArgumentException("Missing serviceUrl");
    }
  }

  private final String serviceUrl;

  private final String username = "Anonymous";

  private final Object resfreshSync = new Object();

  private boolean initialized = false;

  private final String password = null;

  private final String mapAgentUrl;

  private String name;

  private Folder root;

  public MapGuideWebService(final String serverUrl) {
    this.serviceUrl = serverUrl;
    this.mapAgentUrl = serverUrl + "/mapagent/mapagent.fcgi?VERSION=1.0.0&OPERATION=";
  }

  @Override
  public <R extends WebServiceResource> R getChild(final String name) {
    refreshIfNeeded();
    if (this.root == null) {
      return null;
    } else {
      return this.root.getChild(name);
    }
  }

  @Override
  public List<MapGuideResource> getChildren() {
    refreshIfNeeded();
    if (this.root == null) {
      return Collections.emptyList();
    } else {
      return this.root.getChildren();
    }
  }

  @Override
  public String getIconName() {
    return "folder:world";
  }

  public InputStream getInputStream(final String operation, final String format,
    final Map<String, ? extends Object> parameters) {
    final Resource resource = getResource(operation, format, parameters);
    return resource.getInputStream();
  }

  public MapEx getJsonResponse(final String operation,
    final Map<String, ? extends Object> parameters) {
    final StringBuilder url = new StringBuilder(this.mapAgentUrl);
    url.append(operation);
    url.append("&format=application%2Fjson&");
    UrlUtil.appendQuery(url, parameters);
    final Resource resource = new UrlResource(url, this.username, this.password);
    return Json.toMap(resource);
  }

  @Override
  public String getName() {
    return this.name;
  }

  public Resource getResource(final String operation, final String format,
    final Map<String, ? extends Object> parameters) throws Error {
    final StringBuilder url = new StringBuilder(this.mapAgentUrl);
    url.append(operation);
    if (format != null) {
      url.append("&format=");
      url.append(UrlUtil.percentEncode(format));
    }
    url.append('&');
    UrlUtil.appendQuery(url, parameters);
    final Resource resource = new UrlResource(url, this.username, this.password);
    return resource;
  }

  public void getResources(final String path) {
    final MapEx parameters = new LinkedHashMapEx();
    parameters.put("RESOURCEID", "Library:/" + path);
    parameters.put("COMPUTECHILDREN", "1");
    parameters.put("DEPTH", "-1");
    final MapEx response = getJsonResponse("ENUMERATERESOURCES", parameters);
    final MapEx resourceList = response.getValue("ResourceList");
    final List<MapEx> resourceFolders = resourceList.getValue("ResourceFolder");
    final Map<PathName, Folder> folderByPath = new HashMap<>();
    for (final MapEx resourceDefinition : resourceFolders) {
      final Folder folder = new Folder(resourceDefinition);
      folder.setWebService(this);
      final PathName resourcePath = folder.getPath();
      folderByPath.put(resourcePath, folder);
      final PathName parentPath = resourcePath.getParent();
      if (parentPath != null) {
        final Folder parent = folderByPath.get(parentPath);
        parent.addResource(folder);
      }
    }
    final List<MapEx> resourceDocuments = resourceList.getValue("ResourceDocument");
    for (final MapEx resourceDefinition : resourceDocuments) {
      final List<String> resourceIdList = resourceDefinition.getValue("ResourceId");
      final String resourceId = resourceIdList.get(0);
      final String resourceType = resourceId.substring(resourceId.lastIndexOf(".") + 1);
      final Function<MapEx, ResourceDocument> factory = RESOURCE_DOCUMENT_FACTORIES
        .get(resourceType);
      if (factory != null) {
        final ResourceDocument resource = factory.apply(resourceDefinition);
        resource.setWebService(this);
        final PathName resourcePath = resource.getPath();
        final PathName parentPath = resourcePath.getParent();
        if (parentPath != null) {
          final Folder parent = folderByPath.get(parentPath);
          parent.addResource(resource);
        }
      } else {
        Logs.debug(this, "Unsupported resource type: " + resourceType);
      }
    }
    this.root = folderByPath.get(PathName.ROOT);
  }

  public String getServiceUrl() {
    return this.serviceUrl;
  }

  @Override
  public final void refresh() {
    synchronized (this.resfreshSync) {
      refreshDo();
    }
  }

  protected void refreshDo() {
    getResources("/");
  }

  public final void refreshIfNeeded() {
    synchronized (this.resfreshSync) {
      if (!this.initialized) {
        this.initialized = true;
        refresh();
      }
    }
  }

  @Override
  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public MapEx toMap() {
    final MapEx map = newTypeMap(J_TYPE);
    map.put("serviceUrl", this.serviceUrl);
    final String name = getName();
    addToMap(map, "name", name, "");
    return map;
  }

  @Override
  public String toString() {
    return getName() + " " + this.serviceUrl;
  }
}
