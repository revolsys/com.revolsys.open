package com.revolsys.swing.map.util;

import java.awt.Component;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.revolsys.io.FileUtil;
import com.revolsys.io.json.JsonMapIoFactory;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerFactory;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.arcgisrest.ArcGisServerRestLayer;
import com.revolsys.swing.map.layer.bing.BingLayer;
import com.revolsys.swing.map.layer.geonames.GeoNamesBoundingBoxLayerWorker;
import com.revolsys.swing.map.layer.grid.GridLayer;
import com.revolsys.swing.map.layer.wikipedia.WikipediaBoundingBoxLayerWorker;
import com.revolsys.swing.map.table.DataObjectLayerTableModel;
import com.revolsys.swing.map.table.LayerTablePanelFactory;
import com.revolsys.swing.map.table.ListDataObjectLayerTableModel;

public class LayerUtil {

  private static final Map<String, LayerFactory<?>> LAYER_FACTORIES = new HashMap<String, LayerFactory<?>>();

  private static final Map<Class<? extends Layer>, LayerTablePanelFactory> LAYER_TABLE_FACTORIES = new HashMap<Class<? extends Layer>, LayerTablePanelFactory>();

  static {
    addLayerFactory(ArcGisServerRestLayer.FACTORY);
    addLayerFactory(BingLayer.FACTORY);
    addLayerFactory(GridLayer.FACTORY);
    addLayerFactory(WikipediaBoundingBoxLayerWorker.FACTORY);
    addLayerFactory(GeoNamesBoundingBoxLayerWorker.FACTORY);

    addLayerTablePanelFactory(DataObjectLayerTableModel.FACTORY);
    addLayerTablePanelFactory(ListDataObjectLayerTableModel.FACTORY);
  }

  public static void addLayerFactory(LayerFactory<?> factory) {
    String typeName = factory.getTypeName();
    LAYER_FACTORIES.put(typeName, factory);
  }

  public static void addLayerTablePanelFactory(LayerTablePanelFactory factory) {
    Class<? extends Layer> layerClass = factory.getLayerClass();
    LAYER_TABLE_FACTORIES.put(layerClass, factory);
  }

  @SuppressWarnings("unchecked")
  public static <T extends Layer> T getLayer(
    final Map<String, Object> properties) {
    if (properties != null) {
      final String typeName = (String)properties.get("type");
      LayerFactory<?> layerFactory = getLayerFactory(typeName);
      if (layerFactory == null) {
        LoggerFactory.getLogger(LayerUtil.class).error(
          "No layer factory for " + typeName);
      } else {
        return (T)layerFactory.createLayer(properties);
      }
    }
    return null;
  }

  public static LayerFactory<?> getLayerFactory(final String typeName) {
    return LAYER_FACTORIES.get(typeName);
  }

  public static LayerTablePanelFactory getLayerTablePanelFactory(
    final Class<?> layerClass) {
    if (layerClass == null) {
      return null;
    } else {
      LayerTablePanelFactory factory = LAYER_TABLE_FACTORIES.get(layerClass);
      if (factory == null) {
        Class<?> superclass = layerClass.getSuperclass();
        factory = getLayerTablePanelFactory(superclass);
        if (factory == null) {
          Class<?>[] interfaces = layerClass.getInterfaces();
          for (Class<?> interfaceClass : interfaces) {
            factory = getLayerTablePanelFactory(interfaceClass);
            if (factory != null) {
              return factory;
            }
          }
        }
      }
      return factory;
    }
  }

  public static void loadLayer(final LayerGroup group, final File file) {
    final Map<String, Object> properties = JsonMapIoFactory.toMap(file);
    final Layer layer = getLayer(properties);
    if (layer != null) {
      group.add(layer);
    }
  }

  public static void loadLayerGroup(final File directory,
    final LayerGroup parent) {
    for (final File file : directory.listFiles()) {
      final String name = file.getName();
      if (file.isDirectory()) {
        final LayerGroup group = parent.addLayerGroup(name);
        loadLayerGroup(file, group);
      } else {
        final String fileExtension = FileUtil.getFileNameExtension(file);
        if (fileExtension.equals("rglayer")) {
          loadLayer(parent, file);
        }
      }
    }
  }

  public static Component getLayerTablePanel(Layer layer) {
    Class<? extends Layer> layerClass = layer.getClass();
    LayerTablePanelFactory factory = getLayerTablePanelFactory(layerClass);
    if (factory != null) {
      return factory.createPanel(layer);
    }
    return null;
  }
}
