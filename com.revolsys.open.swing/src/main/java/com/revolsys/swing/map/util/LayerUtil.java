package com.revolsys.swing.map.util;

import java.awt.Component;
import java.awt.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.SingleCDockable;
import bibliothek.gui.dock.common.event.CDockableStateListener;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.mode.ExtendedMode;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.io.AbstractDataObjectReaderFactory;
import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.FileUtil;
import com.revolsys.io.json.JsonMapIoFactory;
import com.revolsys.spring.SpringUtil;
import com.revolsys.swing.DockingFramesUtil;
import com.revolsys.swing.component.TabbedValuePanel;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerFactory;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.arcgisrest.ArcGisServerRestLayerFactory;
import com.revolsys.swing.map.layer.bing.BingLayerFactory;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.DataObjectListLayer;
import com.revolsys.swing.map.layer.dataobject.DataObjectStoreLayerFactory;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
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

  public static void addNewRecord() {
    final Layer layer = ObjectTree.getMouseClickItem();
    if (layer instanceof DataObjectLayer) {
      final DataObjectLayer dataObjectLayer = (DataObjectLayer)layer;
      dataObjectLayer.addNewRecord();
    }
  }

  public static void deleteLayer() {
    final Layer layer = ObjectTree.getMouseClickItem();
    if (layer != null) {
      final int confirm = JOptionPane.showConfirmDialog(MapPanel.get(layer),
        "Delete the layer and any child layers? This action cannot be undone.",
        "Delete Layer", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
      if (confirm == JOptionPane.OK_OPTION) {
        deleteLayer(layer);
      }
    }
  }

  public static void deleteLayer(final Layer layer) {
    if (layer instanceof LayerGroup) {
      final LayerGroup layerGroup = (LayerGroup)layer;
      for (final Layer childLayer : new ArrayList<Layer>(layerGroup)) {
        deleteLayer(childLayer);
      }
    }
    // TODO all this should be done by listeners

    final DefaultSingleCDockable dockable = layer.getProperty("TableView");
    if (dockable != null) {
      dockable.setVisible(false);
    }
    layer.delete();
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
    final Layer layer = ObjectTree.getMouseClickItem();
    showProperties(layer);
  }

  public static void showProperties(final Layer layer) {
    showProperties(layer, null);
  }

  public static void showProperties(final Layer layer, final String tabName) {
    if (layer != null) {
      final Window window = SwingUtilities.getWindowAncestor(MapPanel.get(layer));
      final TabbedValuePanel panel = layer.createPropertiesPanel();
      panel.setSelectdTab(tabName);
      panel.showDialog(window);
    }
  }

  public static void showProperties(final String tabName) {
    final Layer layer = ObjectTree.getMouseClickItem();
    showProperties(layer, tabName);
  }

  public static void showViewAttributes() {
    final DataObjectLayer layer = ObjectTree.getMouseClickItem();
    showViewAttributes(layer);
  }

  public static void showViewAttributes(final DataObjectLayer layer) {
    if (layer != null) {
      DefaultSingleCDockable dockable;
      synchronized (layer) {
        dockable = layer.getProperty("TableView");
      }
      if (dockable == null) {
        final Project project = layer.getProject();

        final Component component = layer.createTablePanel();
        if (component != null) {
          final String id = layer.getClass().getName() + "." + layer.getId();
          dockable = DockingFramesUtil.addDockable(project,
            MapPanel.MAP_TABLE_WORKING_AREA, id, layer.getName(), component);

          dockable.setCloseable(true);
          layer.setProperty("TableView", dockable);
          dockable.addCDockableStateListener(new CDockableStateListener() {
            @Override
            public void extendedModeChanged(final CDockable dockable,
              final ExtendedMode mode) {
            }

            @Override
            public void visibilityChanged(final CDockable dockable) {
              final boolean visible = dockable.isVisible();
              if (!visible) {
                dockable.getControl()
                  .getOwner()
                  .remove((SingleCDockable)dockable);
                synchronized (layer) {
                  layer.setProperty("TableView", null);
                }
              }
            }
          });
          dockable.toFront();
        }
      }
    }
  }

  public static void toggleEditable() {
    final Layer layer = ObjectTree.getMouseClickItem();
    final boolean editable = layer.isEditable();
    layer.setEditable(!editable);
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

  public static void zoomToLayer() {
    final Layer layer = ObjectTree.getMouseClickItem();
    if (layer != null) {
      final Project project = layer.getProject();
      final GeometryFactory geometryFactory = project.getGeometryFactory();
      final BoundingBox boundingBox = layer.getBoundingBox()
        .convert(geometryFactory)
        .expandPercent(0.1)
        .clipToCoordinateSystem();

      project.setViewBoundingBox(boundingBox);
    }
  }

  public static void zoomToLayerSelected() {
    final Layer layer = ObjectTree.getMouseClickItem();
    if (layer != null) {
      final Project project = layer.getProject();
      final GeometryFactory geometryFactory = project.getGeometryFactory();
      final BoundingBox boundingBox = layer.getSelectedBoundingBox()
        .convert(geometryFactory)
        .expandPercent(0.1);
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

  public void showForm(final DataObjectLayer layer, final LayerDataObject object) {
    layer.showForm(object);
  }
}
