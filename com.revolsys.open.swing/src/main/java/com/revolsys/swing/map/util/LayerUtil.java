package com.revolsys.swing.map.util;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

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
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.io.FileUtil;
import com.revolsys.io.json.JsonMapIoFactory;
import com.revolsys.spring.SpringUtil;
import com.revolsys.swing.DockingFramesUtil;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.form.DataObjectForm;
import com.revolsys.swing.map.form.DataObjectLayerFormFactory;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerFactory;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.arcgisrest.ArcGisServerRestLayer;
import com.revolsys.swing.map.layer.bing.BingLayer;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.DataObjectListLayer;
import com.revolsys.swing.map.layer.dataobject.DataObjectStoreLayer;
import com.revolsys.swing.map.layer.dataobject.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.layer.dataobject.style.GeometryStyle;
import com.revolsys.swing.map.layer.geonames.GeoNamesBoundingBoxLayerWorker;
import com.revolsys.swing.map.layer.grid.GridLayer;
import com.revolsys.swing.map.layer.raster.GeoJpegImage;
import com.revolsys.swing.map.layer.raster.GeoReferencedImage;
import com.revolsys.swing.map.layer.raster.GeoReferencedImageLayer;
import com.revolsys.swing.map.layer.raster.GeoTiffImage;
import com.revolsys.swing.map.layer.wikipedia.WikipediaBoundingBoxLayerWorker;
import com.revolsys.swing.map.table.DataObjectLayerTableModel;
import com.revolsys.swing.map.table.DataObjectListLayerTableModel;
import com.revolsys.swing.map.table.LayerTablePanelFactory;
import com.revolsys.swing.tree.ObjectTree;
import com.vividsolutions.jts.geom.Geometry;

public class LayerUtil {

  private static Map<DataObject, Window> forms = new HashMap<DataObject, Window>();

  private static final Map<String, LayerFactory<?>> LAYER_FACTORIES = new HashMap<String, LayerFactory<?>>();

  private static final Map<Class<? extends Layer>, LayerTablePanelFactory> LAYER_TABLE_FACTORIES = new HashMap<Class<? extends Layer>, LayerTablePanelFactory>();

  static {
    addLayerFactory(DataObjectStoreLayer.FACTORY);
    addLayerFactory(ArcGisServerRestLayer.FACTORY);
    addLayerFactory(BingLayer.FACTORY);
    addLayerFactory(GridLayer.FACTORY);
    addLayerFactory(WikipediaBoundingBoxLayerWorker.FACTORY);
    addLayerFactory(GeoNamesBoundingBoxLayerWorker.FACTORY);
    addLayerFactory(GeoReferencedImageLayer.FACTORY);

    addLayerTablePanelFactory(DataObjectLayerTableModel.FACTORY);
    addLayerTablePanelFactory(DataObjectListLayerTableModel.FACTORY);
  }

  public static void addLayerFactory(final LayerFactory<?> factory) {
    final String typeName = factory.getTypeName();
    LAYER_FACTORIES.put(typeName, factory);
  }

  public static void addLayerTablePanelFactory(
    final LayerTablePanelFactory factory) {
    final Class<? extends Layer> layerClass = factory.getLayerClass();
    LAYER_TABLE_FACTORIES.put(layerClass, factory);
  }

  public static void addNewRecord() {
    final Layer layer = ObjectTree.getMouseClickItem();
    if (layer instanceof DataObjectLayer) {
      final DataObjectLayer dataObjectLayer = (DataObjectLayer)layer;
      dataObjectLayer.addNewRecord();
    }
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
        return (T)layerFactory.createLayer(properties);
      }
    }
    return null;
  }

  public static LayerFactory<?> getLayerFactory(final String typeName) {
    return LAYER_FACTORIES.get(typeName);
  }

  public static Component getLayerTablePanel(final Layer layer) {
    final Class<? extends Layer> layerClass = layer.getClass();
    final LayerTablePanelFactory factory = getLayerTablePanelFactory(layerClass);
    if (factory != null) {
      return factory.createPanel(layer);
    }
    return null;
  }

  public static LayerTablePanelFactory getLayerTablePanelFactory(
    final Class<?> layerClass) {
    if (layerClass == null) {
      return null;
    } else {
      LayerTablePanelFactory factory = LAYER_TABLE_FACTORIES.get(layerClass);
      if (factory == null) {
        final Class<?> superclass = layerClass.getSuperclass();
        factory = getLayerTablePanelFactory(superclass);
        if (factory == null) {
          final Class<?>[] interfaces = layerClass.getInterfaces();
          for (final Class<?> interfaceClass : interfaces) {
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

  public static LayerGroup getCurrentLayerGroup() {
    final Project project = Project.get();
    if (project != null) {
      List<LayerGroup> groups = project.getLayerGroups();
      if (groups.isEmpty()) {
        return project;
      } else {
        return groups.get(0);
      }
    }
    return null;
  }

  public static void addLayer(Layer layer) {
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
        final List<String> readerFileSuffixes = new ArrayList<String>(
          Arrays.asList(ImageIO.getReaderFileSuffixes()));
        readerFileSuffixes.add("tif");
        readerFileSuffixes.add("tiff");
        if (readerFileSuffixes.contains(extension)) {

          GeoReferencedImage image;
          // TODO factories
          if (Arrays.asList("jpg", "jpeg").contains(extension)) {
            image = new GeoJpegImage(resource);
          } else if (Arrays.asList("tif", "tiff").contains(extension)) {
            image = new GeoTiffImage(resource);
          } else {
            image = new GeoReferencedImage(resource);
          }

          final GeoReferencedImageLayer layer = new GeoReferencedImageLayer(
            FileUtil.getBaseName(file), image);
          layerGroup.add(layer);
        } else {
          final DataObjectReader reader = AbstractDataObjectReaderFactory.dataObjectReader(resource);
          if (reader != null) {
            try {
              final DataObjectMetaData metaData = reader.getMetaData();
              final GeometryFactory geometryFactory = metaData.getGeometryFactory();
              BoundingBox boundingBox = new BoundingBox(geometryFactory);
              final DataObjectListLayer layer = new DataObjectListLayer(
                metaData);
              GeometryStyleRenderer renderer = layer.getRenderer();
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

  public static void showForm(final DataObjectLayer layer,
    final DataObject object) {
    synchronized (forms) {
      Window window = forms.get(object);
      if (window == null) {
        final Project project = layer.getProject();
        if (project == null || object == null) {
          return;
        } else {
          final Object id = object.getIdValue();
          final Component form = DataObjectLayerFormFactory.createFormComponent(
            layer, object);
          String title;
          if (object.getState() == DataObjectState.New) {
            title = "Add NEW " + layer.getName();
          } else if (layer.isCanEditObjects()) {
            title = "Edit " + layer.getName() + " #" + id;
          } else {
            title = "View " + layer.getName() + " #" + id;
            if (form instanceof DataObjectForm) {
              final DataObjectForm dataObjectForm = (DataObjectForm)form;
              dataObjectForm.setEditable(false);
            }
          }
          window = new JFrame(title);
          window.add(new JScrollPane(form));
          window.pack();
          window.setLocation(50, 50);
          // TODO smart location
          window.setVisible(true);
          forms.put(object, window);
          window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
              forms.remove(object);
            }
          });
        }
      }
      window.requestFocus();
    }

  }

  public static void showViewAttributes() {
    final Layer layer = ObjectTree.getMouseClickItem();
    if (layer != null) {
      DefaultSingleCDockable dockable;
      synchronized (layer) {
        dockable = layer.getProperty("TableView");
      }
      if (dockable == null) {
        final Project project = layer.getProject();

        final Component component = LayerUtil.getLayerTablePanel(layer);
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
      }

      dockable.toFront();
    }
  }

  public static void toggleEditable() {
    final Layer layer = ObjectTree.getMouseClickItem();
    final boolean editable = layer.isEditable();
    layer.setEditable(!editable);
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

  public static void deleteLayer() {
    final Layer layer = ObjectTree.getMouseClickItem();
    if (layer != null) {
      int confirm = JOptionPane.showConfirmDialog(MapPanel.get(layer),
        "Delete the layer and any child layers? This action cannot be undone.",
        "Delete Layer", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
      if (confirm == JOptionPane.OK_OPTION) {
        deleteLayer(layer);
      }
    }
  }

  public static void deleteLayer(final Layer layer) {
    if (layer instanceof LayerGroup) {
      LayerGroup layerGroup = (LayerGroup)layer;
      for (Layer childLayer : new ArrayList<Layer>(layerGroup)) {
        deleteLayer(childLayer);
      }
    }
    // TODO all this should be done by listeners
    Window window = forms.remove(layer);
    if (window != null) {
      window.setVisible(false);
    }
    DefaultSingleCDockable dockable = layer.getProperty("TableView");
    if (dockable != null) {
      dockable.setVisible(false);
    }
    layer.delete();
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
}
