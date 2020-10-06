package com.revolsys.swing.map.layer;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;

import com.revolsys.collection.Parent;
import com.revolsys.collection.list.Lists;
import com.revolsys.collection.map.MapEx;
import com.revolsys.connection.Connection;
import com.revolsys.connection.ConnectionRegistry;
import com.revolsys.elevation.cloud.PointCloudReadFactory;
import com.revolsys.elevation.gridded.GriddedElevationModelReaderFactory;
import com.revolsys.elevation.tin.TriangulatedIrregularNetworkReaderFactory;
import com.revolsys.geometry.model.editor.BoundingBoxEditor;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactory;
import com.revolsys.io.PathUtil;
import com.revolsys.io.file.Paths;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.raster.GeoreferencedImageReadFactory;
import com.revolsys.record.io.RecordReaderFactory;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.spring.resource.PathResource;
import com.revolsys.spring.resource.Resource;
import com.revolsys.spring.resource.UrlResource;
import com.revolsys.swing.map.layer.elevation.gridded.GriddedElevationModelLayer;
import com.revolsys.swing.map.layer.elevation.tin.TriangulatedIrregularNetworkLayer;
import com.revolsys.swing.map.layer.pointcloud.PointCloudLayer;
import com.revolsys.swing.map.layer.raster.GeoreferencedImageLayer;
import com.revolsys.swing.map.layer.record.FileRecordLayer;
import com.revolsys.swing.map.layer.record.renderer.GeometryStyleRecordLayerRenderer;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.util.Cancellable;
import com.revolsys.util.Property;
import com.revolsys.util.UrlUtil;

public interface LayerGroup extends Layer, Parent<Layer>, Iterable<Layer> {

  static Layer getLayer(LayerGroup group, final String name) {
    for (final String path : PathUtil.getPathElements(PathUtil.getPath(name))) {
      final Layer layer = group.getLayerByName(path);
      if (layer instanceof LayerGroup) {
        group = (LayerGroup)layer;
      } else {
        return null;
      }
    }

    if (group != null) {
      final String layerName = PathUtil.getName(name);

      return group.getLayerByName(layerName);
    }
    return null;
  }

  default <V extends Layer> void addDescendants(final List<V> layers, final Class<V> layerClass) {
    addLayers(layers, layerClass);
    for (final LayerGroup layerGroup : getLayerGroups()) {
      layerGroup.addDescendants(layers, layerClass);
    }
  }

  default boolean addLayer(final int index, final Layer layer) {
    final int addIndex;
    synchronized (getSync()) {
      addIndex = addLayerDo(index, layer);
    }
    if (addIndex > -1) {
      fireIndexedPropertyChange("layers", addIndex, null, layer);
      return true;
    } else {
      return false;
    }
  }

  default boolean addLayer(final Layer layer) {
    return addLayer(-1, layer);
  }

  int addLayerDo(int index, Layer layer);

  default LayerGroup addLayerGroup(final int index, final String name) {
    if (Property.hasValue(name)) {
      synchronized (getSync()) {
        final Layer layer = getLayer(name);
        if (layer == null) {
          final LayerGroup group = new LayerGroupImpl(name);
          if (index < 0) {
            addLayer(group);
          } else {
            addLayer(index, group);
          }
          return group;
        }
        if (layer instanceof LayerGroup) {
          return (LayerGroup)layer;
        } else {
          throw new IllegalArgumentException("Layer exists with name " + name);
        }
      }
    } else {
      return this;
    }
  }

  default LayerGroup addLayerGroup(final String name) {
    return addLayerGroup(0, name);
  }

  default void addLayers(final Iterable<Layer> layers) {
    if (layers != null) {
      for (final Layer layer : layers) {
        addLayer(layer);
      }
    }
  }

  @SuppressWarnings("unchecked")
  default <V extends Layer> void addLayers(final List<V> layers, final Class<V> layerClass) {
    for (final Layer layer : this) {
      if (layerClass.isAssignableFrom(layer.getClass())) {
        layers.add((V)layer);
      }
    }
  }

  default void addPath(final List<Layer> path) {
    final LayerGroup layerGroup = getLayerGroup();
    if (layerGroup != null) {
      layerGroup.addPath(path);
    }
    path.add(this);
  }

  @Override
  default void addVisibleBbox(final BoundingBoxEditor boundingBox) {
    if (isExists() && isVisible()) {
      for (final Layer layer : this) {
        layer.addVisibleBbox(boundingBox);
      }
    }
  }

  default <V extends Layer> void addVisibleDescendants(final List<V> layers,
    final Class<V> layerClass, final double scale) {
    if (isVisible(scale)) {
      addVisibleLayers(layers, layerClass, scale);
      for (final LayerGroup layerGroup : getLayerGroups()) {
        layerGroup.addVisibleDescendants(layers, layerClass, scale);
      }
    }
  }

  @SuppressWarnings("unchecked")
  default <V extends Layer> void addVisibleLayers(final List<V> layers, final Class<V> layerClass,
    final double scale) {
    for (final Layer layer : getLayers()) {
      if (layer.isVisible(scale)) {
        if (layerClass.isAssignableFrom(layer.getClass())) {
          layers.add((V)layer);
        }
      }
    }
  }

  boolean canSaveSettings(Path directory);

  void clear();

  default boolean containsLayer(final Layer layer) {
    for (final Layer layer1 : getLayers()) {
      if (layer1 == layer) {
        return true;
      }
    }
    return false;
  }

  @Override
  void delete();

  void fireIndexedPropertyChange(String name, int index, Object oldValue, Object newValue);

  void forEach(final Cancellable cancellable, final Consumer<? super Layer> action);

  @Override
  void forEach(final Consumer<? super Layer> action);

  void forEachReverse(final Cancellable cancellable, final Consumer<? super Layer> action);

  void forEachReverse(final Consumer<? super Layer> action);

  default java.nio.file.Path getDirectory() {
    final LayerGroup layerGroup = getLayerGroup();
    if (layerGroup != null) {
      final java.nio.file.Path parentDirectory = layerGroup.getDirectory();
      if (parentDirectory != null) {
        final java.nio.file.Path layerDirectory = getGroupSettingsDirectory(parentDirectory);
        return layerDirectory;
      }
    }
    return null;
  }

  default java.nio.file.Path getGroupSettingsDirectory(final java.nio.file.Path directory) {
    final String name = getName();
    final String groupDirectoryName = FileUtil.getSafeFileName(name);
    final java.nio.file.Path groupDirectory = Paths.getDirectoryPath(directory, groupDirectoryName);
    return groupDirectory;
  }

  @SuppressWarnings("unchecked")
  default <V extends Layer> V getLayer(final int i) {
    if (i < getLayerCount()) {
      return (V)getLayers().get(i);
    } else {
      return null;
    }
  }

  default <V extends Layer> V getLayer(final PathName name) {
    return getLayer(name.getPath());
  }

  @SuppressWarnings("unchecked")
  default <V extends Layer> V getLayer(final String name) {
    return (V)getLayer(this, name);
  }

  default Layer getLayerByName(final String layerName) {
    for (final Layer layer : getLayers()) {
      if (layer.getName().equals(layerName)) {

        return layer;
      }
    }
    return null;
  }

  default Layer getLayerByPath(final List<String> path) {
    if (path.isEmpty()) {
      return this;
    } else {
      final Layer layer = getLayer(path.get(0));
      if (path.size() == 1) {
        return layer;
      } else {
        if (layer instanceof LayerGroup) {
          final LayerGroup layerGroup = (LayerGroup)layer;
          return layerGroup.getLayerByPath(path.subList(1, path.size()));
        }
        return null;
      }
    }
  }

  default Layer getLayerByPath(final String layerPath) {
    final List<String> path = Lists.split(layerPath.replaceAll("^\\s*/+\\s*", ""), "(\\s*/+\\s*)+");
    return getLayerByPath(path);
  }

  default int getLayerCount() {
    return getLayers().size();
  }

  default List<LayerGroup> getLayerGroups() {
    final List<LayerGroup> layerGroups = new ArrayList<>();
    for (final Layer layer : getLayers()) {
      if (layer instanceof LayerGroup) {
        final LayerGroup layerGroup = (LayerGroup)layer;
        layerGroups.add(layerGroup);
      }
    }
    return layerGroups;
  }

  List<Layer> getLayers();

  default <V extends Layer> List<V> getLayers(final Class<V> layerClass) {
    final List<V> layers = new ArrayList<>();
    addLayers(layers, layerClass);
    return layers;
  }

  default <V extends Layer> List<V> getLayers(final List<PathName> names) {
    final List<V> layers = new ArrayList<>();
    for (final PathName name : names) {
      final V layer = getLayer(name);
      if (layer != null) {
        layers.add(layer);
      }
    }
    return layers;
  }

  default <V extends Layer> List<V> getLayers(final PathName... names) {
    return getLayers(Arrays.asList(names));
  }

  Object getSync();

  default <V extends Layer> List<V> getVisibleDescendants(final Class<V> layerClass,
    final double scale) {
    final List<V> layers = new ArrayList<>();
    addVisibleDescendants(layers, layerClass, scale);
    return layers;
  }

  default boolean hasLayerWithSameName(final Layer layer, final String name) {
    for (final Layer otherLayer : this) {
      if (layer != otherLayer) {
        final String layerName = otherLayer.getName();
        if (DataTypes.STRING.equals(name, layerName)) {
          return true;
        }
      }
    }
    return false;
  }

  default <C extends Connection> void importConnections(final String label,
    final Project importProject, final ConnectionRegistry<C> importConnections,
    final ConnectionRegistry<C> connections) {
    for (final C importConnection : importConnections.getConnections()) {
      final String connectionName = importConnection.getName();
      final C connection = connections.getConnection(connectionName);
      if (connection == null) {
        final MapEx importConfig = importConnection.getConfig();
        connections.addConnection(importConfig);
      } else if (!importConnection.equalsConfig(connection)) {
        Logs.error(this, "Ignore Duplicate " + label + ": " + connectionName + " from: "
          + importProject.getResource());
      }
    }
  }

  default void importProject(final Project importProject) {
    importProjectLayers(importProject, false);
  }

  default boolean importProject(final Project rootProject, final Resource resource,
    final boolean resetProject) {
    if (resetProject) {
      reset();
    }
    if (resource.exists()) {
      final Project importProject = new Project("ImportProject_" + resource.getBaseName());
      importProject.readProject(rootProject, resource);

      importProject(importProject);
      if (resetProject) {
        setName(importProject.getName());
      }
      return true;
    } else {
      return false;
    }
  }

  default void importProjectLayers(final Project importProject, final boolean createGroup) {
    if (!importProject.isEmpty()) {
      LayerGroup targetGroup;
      if (createGroup) {
        final String projectName = importProject.getName();
        targetGroup = new LayerGroupImpl(projectName);
        addLayer(targetGroup);
      } else {
        targetGroup = this;
      }
      importProject.forEach(targetGroup::addLayer);
    }
  }

  int indexOf(final Layer layer);

  @Override
  default boolean isHasSelectedRecords() {
    for (final Layer layer : this) {
      if (layer.isHasSelectedRecords()) {
        return true;
      }
    }
    return false;
  }

  default

      boolean isHasVisibleLayer() {
    for (final Layer layer : this) {
      if (layer.isVisible()) {
        return true;
      }
    }
    return false;
  }

  @Override
  default boolean isQueryable() {
    return false;
  }

  @Override
  default boolean isQuerySupported() {
    return false;
  }

  boolean isSingleLayerVisible();

  default boolean isZoomToLayerEnabled() {
    if (!getBoundingBox().isEmpty()) {
      return true;
    }
    return false;
  }

  default Layer loadLayer(final File file) {
    final Resource oldResource = Resource.setBaseResource(new PathResource(file.getParentFile()));

    try {
      final Map<String, Object> properties = Json.toMap(file);
      final Layer layer = MapObjectFactory.toObject(properties);
      if (layer != null) {
        addLayer(layer);
      }
      return layer;
    } catch (final Throwable t) {
      Logs.error(this, "Cannot load layer from " + file, t);
      return null;
    } finally {
      Resource.setBaseResource(oldResource);
    }
  }

  @SuppressWarnings("unchecked")
  default void loadLayers(final Project rootProject, final Map<String, ? extends Object> config) {
    final List<Object> childLayerReferences = (List<Object>)config.remove("layers");
    setProperties(config);
    if (childLayerReferences != null) {
      for (final Object childLayerReference : childLayerReferences) {
        if (childLayerReference instanceof String) {
          String layerFileName = (String)childLayerReference;
          if (layerFileName.endsWith(".rgmap")) {
            final Resource childResource;
            if (layerFileName.startsWith("folderconnection://")) {
              childResource = new PathResource(new UrlResource(layerFileName).getFile());
            } else {
              childResource = Resource.getBaseResource(layerFileName);
            }
            if (!importProject(rootProject, childResource, false)) {
              Logs.error(LayerGroup.class, "Project not found: " + childResource);
            }
          } else {
            if (!layerFileName.endsWith("rgobject")) {
              layerFileName += "/rgLayerGroup.rgobject";
            }
            final Resource childResource = Resource.getBaseResource(layerFileName);
            if (childResource.exists()) {
              final Object object = MapObjectFactory.toObject(childResource);
              if (object instanceof Layer) {
                final Layer layer = (Layer)object;
                addLayer(layer);
              } else if (object != null) {
                Logs.error(this,
                  "Unexpected object type " + object.getClass() + " in " + childResource);
              }
            } else {
              Logs.error(LayerGroup.class, "Layer not found: " + childResource);
            }
          }
        } else if (childLayerReference instanceof JsonObject) {
          final JsonObject childLayerConfig = (JsonObject)childLayerReference;
          final String name = childLayerConfig.getString("name");

        } else {
          Logs.error(this, "Invalid layer config\n" + childLayerReference);
        }

      }
    }
  }

  default int openFile(final Class<? extends IoFactory> factoryClass, int index, final URL url) {
    final String urlString = url.toString();
    final Map<String, Object> properties = new HashMap<>();
    properties.put("url", urlString);
    String name = UrlUtil.getFileBaseName(url);
    name = FileUtil.fromSafeName(name);
    properties.put("name", name);
    Layer layer;
    if (factoryClass == TriangulatedIrregularNetworkReaderFactory.class) {
      layer = new TriangulatedIrregularNetworkLayer(properties);
    } else if (factoryClass == GriddedElevationModelReaderFactory.class) {
      layer = new GriddedElevationModelLayer(properties);
    } else if (factoryClass == GeoreferencedImageReadFactory.class) {
      layer = new GeoreferencedImageLayer(properties);
    } else if (factoryClass == PointCloudReadFactory.class) {
      layer = new PointCloudLayer(properties);
    } else if (factoryClass == RecordReaderFactory.class) {
      final FileRecordLayer recordLayer = new FileRecordLayer(properties);
      final GeometryStyleRecordLayerRenderer renderer = recordLayer.getRenderer();
      renderer.setStyle(GeometryStyle.newStyle());
      layer = recordLayer;
    } else {
      layer = null;
    }
    if (layer != null) {
      layer.setProperty("showTableView", Layer.isShowNewLayerTableView());
      if (index == -1) {
        addLayer(layer);
      } else {
        addLayer(index++, layer);
      }
    }
    return index;
  }

  default int openFile(final Class<? extends IoFactory> factoryClass, final URL url) {
    return openFile(factoryClass, -1, url);
  }

  default void openFile(final File file) {
    final String extension = FileUtil.getFileNameExtension(file);
    if ("rgobject".equals(extension)) {
      loadLayer(file);
    } else {
      final URL url = FileUtil.toUrl(file);
      openFile(url);
    }
  }

  default int openFile(final int index, final File file) {
    final String extension = FileUtil.getFileNameExtension(file);
    if ("rgobject".equals(extension)) {
      loadLayer(file);
      // TODO index
      return index;
    } else {
      final URL url = FileUtil.toUrl(file);
      return openFile(index, url);
    }
  }

  default int openFile(int index, final URL url) {
    final String urlString = url.toString();
    final Map<String, Object> properties = new HashMap<>();
    properties.put("url", urlString);
    String name = UrlUtil.getFileBaseName(url);
    name = FileUtil.fromSafeName(name);
    properties.put("name", name);
    Layer layer;
    if (IoFactory.hasFactory(TriangulatedIrregularNetworkReaderFactory.class, url)) {
      layer = new TriangulatedIrregularNetworkLayer(properties);
    } else if (IoFactory.hasFactory(GriddedElevationModelReaderFactory.class, url)) {
      layer = new GriddedElevationModelLayer(properties);
    } else if (IoFactory.hasFactory(GeoreferencedImageReadFactory.class, url)) {
      layer = new GeoreferencedImageLayer(properties);
    } else if (IoFactory.hasFactory(PointCloudReadFactory.class, url)) {
      layer = new PointCloudLayer(properties);
    } else if (IoFactory.hasFactory(RecordReaderFactory.class, url)) {
      final FileRecordLayer recordLayer = new FileRecordLayer(properties);
      final GeometryStyleRecordLayerRenderer renderer = recordLayer.getRenderer();
      renderer.setStyle(GeometryStyle.newStyle());
      layer = recordLayer;
    } else {
      layer = null;
    }
    if (layer != null) {
      layer.setProperty("showTableView", Layer.isShowNewLayerTableView());
      if (index == -1) {
        addLayer(layer);
      } else {
        addLayer(index++, layer);
      }
    }
    return index;
  }

  default void openFile(final URL url) {
    openFile(-1, url);
  }

  default void openFiles(int index, final List<File> files) {
    for (final File file : files) {
      index = openFile(index, file);
    }
  }

  default void openFiles(final List<File> files) {
    openFiles(-1, files);
  }

  @Override
  default void refresh() {
  }

  Layer removeLayer(final int index);

  default boolean removeLayer(final Object o) {
    if (o instanceof Layer) {
      final Layer layer = (Layer)o;
      synchronized (getSync()) {
        final int index = indexOf(layer);
        if (index >= 0) {
          removeLayer(index);
          return true;
        }
      }
    }
    return false;
  }

  default Layer removeLayer(final String layerName) {
    final Layer layer = getLayer(layerName);
    if (layer == null) {
      return null;
    } else {
      removeLayer(layer);
      return layer;
    }
  }

  default boolean removeLayer(final String layerName, final boolean delete) {
    final Layer layer = getLayer(layerName);
    if (layer == null) {
      return false;
    } else {
      if (delete) {
        layer.delete();
      } else {
        removeLayer(layer);
      }
      return true;
    }
  }

  default void reset() {
    clear();
  }

  default boolean saveAllSettings(final java.nio.file.Path directory) {
    if (directory == null) {
      return false;
    } else {
      boolean saved = true;
      final java.nio.file.Path groupDirectory = getGroupSettingsDirectory(directory);
      if (canSaveSettings(directory)) {
        saved &= saveSettings(directory);
        for (final Layer layer : this) {
          if (layer instanceof LayerGroup) {
            final LayerGroup layerGroup = (LayerGroup)layer;
            saved &= layerGroup.saveAllSettings(groupDirectory);
          } else {
            saved &= layer.saveSettings(groupDirectory);
          }
        }
      } else {
        saved = false;
      }
      return saved;
    }
  }

  @SuppressWarnings("unchecked")
  default <L extends Layer> void walkLayers(final Cancellable cancellable,
    final Class<L> layerClass, final Consumer<L> action) {
    for (int i = 0; i < getLayerCount() && !cancellable.isCancelled(); i++) {
      Layer layer;
      try {
        layer = getLayer(i);
      } catch (final ArrayIndexOutOfBoundsException e) {
        return;
      }
      if (layer instanceof LayerGroup) {
        final LayerGroup layerGroup = (LayerGroup)layer;
        layerGroup.walkLayers(cancellable, layerClass, action);
      } else if (layerClass.isAssignableFrom(layer.getClass())) {
        action.accept((L)layer);
      }
    }
  }

  default void walkLayers(final Cancellable cancellable, final Consumer<Layer> action) {
    for (int i = 0; i < getLayerCount() && !cancellable.isCancelled(); i++) {
      Layer layer;
      try {
        layer = getLayer(i);
      } catch (final ArrayIndexOutOfBoundsException e) {
        return;
      }
      if (layer instanceof LayerGroup) {
        final LayerGroup layerGroup = (LayerGroup)layer;
        layerGroup.walkLayers(cancellable, action);
      } else {
        action.accept(layer);
      }
    }
  }
}
