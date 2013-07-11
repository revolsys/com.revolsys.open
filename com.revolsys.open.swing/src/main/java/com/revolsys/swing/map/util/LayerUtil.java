package com.revolsys.swing.map.util;

import java.awt.Component;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.io.AbstractDataObjectReaderFactory;
import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.FileUtil;
import com.revolsys.io.json.JsonMapIoFactory;
import com.revolsys.spring.SpringUtil;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerFactory;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.arcgisrest.ArcGisServerRestLayerFactory;
import com.revolsys.swing.map.layer.bing.BingLayerFactory;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.DataObjectListLayer;
import com.revolsys.swing.map.layer.dataobject.DataObjectStoreLayerFactory;
import com.revolsys.swing.map.layer.dataobject.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.layer.dataobject.style.GeometryStyle;
import com.revolsys.swing.map.layer.geonames.GeoNamesBoundingBoxLayerWorker;
import com.revolsys.swing.map.layer.grid.GridLayer;
import com.revolsys.swing.map.layer.openstreetmap.OpenStreetMapLayerFactory;
import com.revolsys.swing.map.layer.raster.AbstractGeoReferencedImageFactory;
import com.revolsys.swing.map.layer.raster.GeoReferencedImage;
import com.revolsys.swing.map.layer.raster.GeoReferencedImageLayer;
import com.revolsys.swing.map.layer.wikipedia.WikipediaBoundingBoxLayerWorker;
import com.revolsys.swing.tree.ObjectTree;
import com.vividsolutions.jts.geom.Geometry;

public class LayerUtil {

  private static final Map<String, LayerFactory<?>> LAYER_FACTORIES = new HashMap<String, LayerFactory<?>>();

  static {
    addLayerFactory(new DataObjectStoreLayerFactory());
    addLayerFactory(new ArcGisServerRestLayerFactory());
    addLayerFactory(new BingLayerFactory());
    addLayerFactory(new OpenStreetMapLayerFactory());
    addLayerFactory(GridLayer.FACTORY);
    addLayerFactory(WikipediaBoundingBoxLayerWorker.FACTORY);
    addLayerFactory(GeoNamesBoundingBoxLayerWorker.FACTORY);
    addLayerFactory(GeoReferencedImageLayer.FACTORY);
  }

  public static void addLayer(final Layer layer) {
    if (layer != null) {
      final LayerGroup layerGroup = getCurrentLayerGroup();
      if (layerGroup == null) {
        LoggerFactory.getLogger(LayerUtil.class).error(
          "Cannot find project to open file:" + layer);
      } else {
        layerGroup.add(layer);
      }
    }
  }

  public static void addLayerFactory(final LayerFactory<?> factory) {
    final String typeName = factory.getTypeName();
    LAYER_FACTORIES.put(typeName, factory);
  }

  public static void addNewObject() {
    final Layer layer = ObjectTree.getMouseClickItem();
    if (layer instanceof DataObjectLayer) {
      final DataObjectLayer dataObjectLayer = (DataObjectLayer)layer;
      dataObjectLayer.addNewObject();
    }
  }

  public static LayerGroup getCurrentLayerGroup() {
    final Project project = Project.get();
    if (project != null) {
      final List<LayerGroup> groups = project.getLayerGroups();
      if (groups.isEmpty()) {
        return project;
      } else {
        return groups.get(0);
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public static <T extends Layer> T getLayer(
    final Map<String, Object> properties) {
    if (properties != null) {
      final String typeName = (String)properties.get("type");
      final LayerFactory<?> layerFactory = getLayerFactory(typeName);
      if (layerFactory == null) {
        LoggerFactory.getLogger(LayerUtil.class).error(
          "No layer factory for " + typeName);
      } else {
        final Layer layer = layerFactory.createLayer(properties);
        return (T)layer;
      }
    }
    return null;
  }

  public static LayerFactory<?> getLayerFactory(final String typeName) {
    return LAYER_FACTORIES.get(typeName);
  }

  public static Component getLayerTablePanel(final DataObjectLayer layer) {
    if (layer == null) {
      return null;
    } else {
      return layer.createTablePanel();
    }
  }

  public static void loadLayer(final LayerGroup group, final File file) {
    final Resource oldResource = SpringUtil.setBaseResource(new FileSystemResource(
      file.getParentFile()));

    try {
      final Map<String, Object> properties = JsonMapIoFactory.toMap(file);
      final Layer layer = getLayer(properties);
      if (layer != null) {
        group.add(layer);
      }
    } catch (final Throwable t) {
      LoggerFactory.getLogger(LayerUtil.class).error(
        "Cannot load layer from " + file, t);
    } finally {
      SpringUtil.setBaseResource(oldResource);
    }
  }

  public static void loadLayerGroup(final LayerGroup parent,
    final File directory) {
    for (final File file : directory.listFiles()) {
      final String name = file.getName();
      if (file.isDirectory()) {
        final LayerGroup group = parent.addLayerGroup(name);
        loadLayerGroup(group, file);
      } else {
        final String fileExtension = FileUtil.getFileNameExtension(file);
        if (fileExtension.equals("rglayer")) {
          loadLayer(parent, file);
        }
      }
    }
  }

  public static void openFile(final File file) {
    final LayerGroup layerGroup = getCurrentLayerGroup();
    openFile(layerGroup, file);

  }

  public static void openFile(final LayerGroup layerGroup, final File file) {
    if (layerGroup == null) {
      LoggerFactory.getLogger(LayerUtil.class).error(
        "Cannot find project to open file:" + file);
    } else {
      final String extension = FileUtil.getFileNameExtension(file);
      if ("rgmap".equals(extension)) {
        loadLayerGroup(layerGroup, file);
      } else if ("rglayer".equals(extension)) {
        loadLayer(layerGroup, file);
      } else {
        final FileSystemResource resource = new FileSystemResource(file);

        final GeoReferencedImage image = AbstractGeoReferencedImageFactory.loadGeoReferencedImage(resource);
        if (image != null) {
          final GeoReferencedImageLayer layer = new GeoReferencedImageLayer(
            FileUtil.getBaseName(file), image);
          layerGroup.add(layer);
          layer.setEditable(true);
        } else {
          final DataObjectReader reader = AbstractDataObjectReaderFactory.dataObjectReader(resource);
          if (reader != null) {
            try {
              final DataObjectMetaData metaData = reader.getMetaData();
              final GeometryFactory geometryFactory = metaData.getGeometryFactory();
              BoundingBox boundingBox = new BoundingBox(geometryFactory);
              final DataObjectListLayer layer = new DataObjectListLayer(
                metaData);
              final GeometryStyleRenderer renderer = layer.getRenderer();
              renderer.setStyle(GeometryStyle.createStyle());
              for (final DataObject object : reader) {
                final Geometry geometry = object.getGeometryValue();
                boundingBox = boundingBox.expandToInclude(geometry);
                layer.add(object);
              }
              layer.setBoundingBox(boundingBox);
              layerGroup.add(layer);
            } finally {
              reader.close();
            }
          }
        }
      }
    }
  }

  public static void openFiles(final List<File> files) {
    for (final File file : files) {
      openFile(file);
    }
  }

  public static void showProperties() {
    showProperties(null);
  }

  public static void showProperties(final String tabName) {
    final Layer layer = ObjectTree.getMouseClickItem();
    if (layer != null) {
      layer.showProperties(tabName);
    }
  }

  public static void showViewAttributes() {
    final DataObjectLayer layer = ObjectTree.getMouseClickItem();
    if (layer != null) {
      layer.showViewAttributes();
    }
  }

  public static void zoomTo(final Geometry geometry) {
    final Project project = Project.get();
    if (project != null) {
      final GeometryFactory geometryFactory = project.getGeometryFactory();
      final BoundingBox boundingBox = BoundingBox.getBoundingBox(
        geometryFactory, geometry)
        .expandPercent(0.1)
        .clipToCoordinateSystem();

      project.setViewBoundingBox(boundingBox);
    }
  }

  public static void zoomToObject(final DataObject object) {
    final Layer layer = ObjectTree.getMouseClickItem();
    if (layer != null) {
      final Project project = layer.getProject();
      final GeometryFactory geometryFactory = project.getGeometryFactory();
      final BoundingBox boundingBox = BoundingBox.getBoundingBox(
        geometryFactory, object)
        .expandPercent(0.1)
        .clipToCoordinateSystem();

      project.setViewBoundingBox(boundingBox);
    }
  }

}
