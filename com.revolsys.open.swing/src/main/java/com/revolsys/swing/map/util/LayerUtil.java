package com.revolsys.swing.map.util;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerFactory;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.arcgisrest.ArcGisServerRestLayerFactory;
import com.revolsys.swing.map.layer.bing.BingLayerFactory;
import com.revolsys.swing.map.layer.dataobject.DataObjectFileLayer;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.DataObjectStoreLayerFactory;
import com.revolsys.swing.map.layer.geonames.GeoNamesBoundingBoxLayerWorker;
import com.revolsys.swing.map.layer.grid.GridLayer;
import com.revolsys.swing.map.layer.openstreetmap.OpenStreetMapLayerFactory;
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
    addLayerFactory(DataObjectFileLayer.FACTORY);
    addLayerFactory(GridLayer.FACTORY);
    addLayerFactory(WikipediaBoundingBoxLayerWorker.FACTORY);
    addLayerFactory(GeoNamesBoundingBoxLayerWorker.FACTORY);
    addLayerFactory(GeoReferencedImageLayer.FACTORY);
  }

  public static void addLayerFactory(final LayerFactory<?> factory) {
    final String typeName = factory.getTypeName();
    LAYER_FACTORIES.put(typeName, factory);
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

  public static void showProperties() {
    showProperties(null);
  }

  public static void showProperties(final String tabName) {
    final Layer layer = ObjectTree.getMouseClickItem();
    if (layer != null) {
      layer.showProperties(tabName);
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
