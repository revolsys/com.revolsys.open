package com.revolsys.swing.map.layer;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.io.FileUtil;
import com.revolsys.io.datastore.DataObjectStoreConnectionRegistry;
import com.revolsys.io.file.FolderConnectionRegistry;
import com.revolsys.io.json.JsonMapIoFactory;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.jts.util.BoundingBoxUtil;
import com.revolsys.spring.SpringUtil;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.ExceptionUtil;

public class Project extends LayerGroup {

  private static WeakReference<Project> project = new WeakReference<Project>(
    null);

  public static Project get() {
    return Project.project.get();
  }

  public static void set(final Project project) {
    Project.project = new WeakReference<Project>(project);
  }

  private LayerGroup baseMapLayers = new LayerGroup("Base Maps");

  private DataObjectStoreConnectionRegistry dataStores = new DataObjectStoreConnectionRegistry(
    "Project");

  private FolderConnectionRegistry folderConnections = new FolderConnectionRegistry(
    "Project");

  private BoundingBox initialBoundingBox;

  private Resource resource;

  private BoundingBox viewBoundingBox = new BoundingBoxDoubleGf();

  private Map<String, BoundingBox> zoomBookmarks = new LinkedHashMap<String, BoundingBox>();

  public Project() {
    this("Project");
  }

  public Project(final String name) {
    super(name);
    baseMapLayers.setLayerGroup(this);
    setGeometryFactory(GeometryFactory.worldMercator());
  }

  private void addChangedLayers(final LayerGroup group,
    final List<Layer> layersWithChanges) {
    for (final Layer layer : group) {
      if (layer instanceof LayerGroup) {
        final LayerGroup subGroup = (LayerGroup)layer;
        addChangedLayers(subGroup, layersWithChanges);
      } else if (layer.isHasChanges()) {
        layersWithChanges.add(layer);
      }
    }

  }

  public void addZoomBookmark(final String name, final BoundingBox boundingBox) {
    if (name != null && boundingBox != null) {
      zoomBookmarks.put(name, boundingBox);
    }
  }

  @Override
  public void delete() {
    super.delete();
    baseMapLayers = null;
    viewBoundingBox = null;
    zoomBookmarks = null;
  }

  @Override
  protected boolean doSaveSettings(final File directory) {
    boolean saved = true;
    FileUtil.deleteDirectory(directory, false);
    directory.mkdir();

    saved &= super.doSaveSettings(directory);

    final File projectDirectory = getProjectDirectory();
    final File baseMapsDirectory = new File(projectDirectory, "Base Maps");
    FileUtil.deleteDirectory(baseMapsDirectory, false);
    final LayerGroup baseMapLayers = getBaseMapLayers();
    if (baseMapLayers != null) {
      saved &= baseMapLayers.saveAllSettings(projectDirectory);
    }
    return saved;
  }

  public LayerGroup getBaseMapLayers() {
    return baseMapLayers;
  }

  public DataObjectStoreConnectionRegistry getDataStores() {
    return dataStores;
  }

  @Override
  public File getDirectory() {
    final File directory = getProjectDirectory();
    if (directory != null) {
      final File layersDirectory = new File(directory, "Layers");
      layersDirectory.mkdirs();
      if (layersDirectory.isDirectory()) {
        return layersDirectory;
      }
    }
    return null;
  }

  public FolderConnectionRegistry getFolderConnections() {
    return folderConnections;
  }

  @Override
  protected File getGroupSettingsDirectory(final File directory) {
    return directory;
  }

  public BoundingBox getInitialBoundingBox() {
    return initialBoundingBox;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V extends Layer> V getLayer(final String name) {
    if (name.equals("Base Maps")) {
      return (V)baseMapLayers;
    } else {
      return (V)super.getLayer(name);
    }
  }

  @Override
  public Project getProject() {
    return this;
  }

  public File getProjectDirectory() {
    if (resource instanceof FileSystemResource) {
      final FileSystemResource fileResource = (FileSystemResource)resource;
      final File directory = fileResource.getFile();
      if (!directory.exists()) {
        directory.mkdirs();
      }
      if (directory.isDirectory()) {
        return directory;
      }
    }
    return null;
  }

  public BoundingBox getViewBoundingBox() {
    return viewBoundingBox;
  }

  public Map<String, BoundingBox> getZoomBookmarks() {
    return zoomBookmarks;
  }

  protected void readBaseMapsLayers(final Resource resource) {
    final Resource baseMapsResource = SpringUtil.getResource(resource,
      "Base Maps");
    final Resource layerGroupResource = SpringUtil.getResource(
      baseMapsResource, "rgLayerGroup.rgobject");
    if (layerGroupResource.exists()) {
      final Resource oldResource = SpringUtil.setBaseResource(baseMapsResource);
      try {
        final Map<String, Object> properties = JsonMapIoFactory.toMap(layerGroupResource);
        baseMapLayers.loadLayers(properties);
        boolean hasVisible = false;
        if (baseMapLayers != null) {
          for (final Layer layer : baseMapLayers) {
            if (hasVisible) {
              layer.setVisible(false);
            } else {
              hasVisible = layer.isVisible();
            }
          }
        }
      } finally {
        SpringUtil.setBaseResource(oldResource);
      }
    }
  }

  protected void readLayers(final Resource resource) {
    final Resource layerGroupResource = SpringUtil.getResource(resource,
      "rgLayerGroup.rgobject");
    if (!layerGroupResource.exists()) {
      LoggerFactory.getLogger(getClass()).error(
        "File not found: " + layerGroupResource);
    } else {
      final Resource oldResource = SpringUtil.setBaseResource(resource);
      try {
        final Map<String, Object> properties = JsonMapIoFactory.toMap(layerGroupResource);
        loadLayers(properties);
      } catch (final Throwable e) {
        LoggerFactory.getLogger(getClass()).error(
          "Unable to read: " + layerGroupResource, e);
      } finally {
        SpringUtil.setBaseResource(oldResource);
      }
    }
  }

  public void readProject(final Resource resource) {
    this.resource = resource;
    if (resource.exists()) {
      setEventsEnabled(false);
      final Resource layersDir = SpringUtil.getResource(resource, "Layers");
      readProperties(layersDir);
      final DataObjectStoreConnectionRegistry oldDataStoreConnections = DataObjectStoreConnectionRegistry.getForThread();
      try {
        final Resource dataStoresDirectory = SpringUtil.getResource(resource,
          "Data Stores");

        final boolean readOnly = isReadOnly();
        final DataObjectStoreConnectionRegistry dataStores = new DataObjectStoreConnectionRegistry(
          "Project", dataStoresDirectory, readOnly);
        setDataStores(dataStores);
        DataObjectStoreConnectionRegistry.setForThread(dataStores);

        final Resource folderConnectionsDirectory = SpringUtil.getResource(
          resource, "Folder Connections");
        folderConnections = new FolderConnectionRegistry("Project",
          folderConnectionsDirectory, readOnly);

        readLayers(layersDir);

        readBaseMapsLayers(resource);
      } finally {
        setEventsEnabled(true);
        DataObjectStoreConnectionRegistry.setForThread(oldDataStoreConnections);
      }
    }
  }

  protected void readProperties(final Resource resource) {
    final Resource layerGroupResource = SpringUtil.getResource(resource,
      "rgLayerGroup.rgobject");
    if (!layerGroupResource.exists()) {
      LoggerFactory.getLogger(getClass()).error(
        "File not found: " + layerGroupResource);
    } else {
      final Resource oldResource = SpringUtil.setBaseResource(resource);
      try {
        final Map<String, Object> properties = JsonMapIoFactory.toMap(layerGroupResource);
        setProperties(properties);
      } catch (final Throwable e) {
        LoggerFactory.getLogger(getClass()).error(
          "Unable to read: " + layerGroupResource, e);
      } finally {
        SpringUtil.setBaseResource(oldResource);
      }
    }
  }

  public void removeZoomBookmark(final String name) {
    if (name != null) {
      zoomBookmarks.remove(name);
    }
  }

  public void save() {
  }

  public boolean saveAllSettings() {
    if (isReadOnly()) {
      return true;
    } else {
      final File directory = getDirectory();
      return super.saveAllSettings(directory);
    }
  }

  public boolean saveChangesWithPrompt() {
    if (isReadOnly()) {
      return true;
    } else {
      final List<Layer> layersWithChanges = new ArrayList<Layer>();
      addChangedLayers(this, layersWithChanges);

      if (layersWithChanges.isEmpty()) {
        return true;
      } else {
        final MapPanel mapPanel = MapPanel.get(this);
        final JLabel message = new JLabel(
          "<html><body><p><b>The following layers have un-saved changes.</b></p>"
            + "<p><b>Do you want to save the changes before continuing?</b></p><ul><li>"
            + CollectionUtil.toString("</li>\n<li>", layersWithChanges)
            + "</li></ul></body></html>");

        final int option = JOptionPane.showConfirmDialog(mapPanel, message,
          "Save Changes", JOptionPane.YES_NO_CANCEL_OPTION,
          JOptionPane.WARNING_MESSAGE);
        if (option == JOptionPane.CANCEL_OPTION) {
          return false;
        } else if (option == JOptionPane.NO_OPTION) {
          return true;
        } else {
          for (final Iterator<Layer> iterator = layersWithChanges.iterator(); iterator.hasNext();) {
            final Layer layer = iterator.next();
            if (layer.saveChanges()) {
              iterator.remove();
            }
          }
          if (layersWithChanges.isEmpty()) {
            return true;
          } else {
            final JLabel message2 = new JLabel(
              "<html><body><p><b>The following layers could not be saved.</b></p>"
                + "<p><b>Do you want to ignore these changes and continue?</b></p><ul><li>"
                + CollectionUtil.toString("</li>\n<li>", layersWithChanges)
                + "</li></ul></body></html>");

            final int option2 = JOptionPane.showConfirmDialog(mapPanel,
              message2, "Ignore Changes", JOptionPane.OK_CANCEL_OPTION,
              JOptionPane.WARNING_MESSAGE);
            if (option2 == JOptionPane.CANCEL_OPTION) {
              return false;
            } else {
              return true;
            }
          }
        }
      }
    }
  }

  public boolean saveSettingsWithPrompt() {
    if (isReadOnly()) {
      return true;
    } else {
      final MapPanel mapPanel = MapPanel.get(this);
      final JLabel message = new JLabel(
        "<html><body><p><b>Save changes to project?</b></p></body></html>");

      final int option = JOptionPane.showConfirmDialog(mapPanel, message,
        "Save Changes", JOptionPane.YES_NO_CANCEL_OPTION,
        JOptionPane.WARNING_MESSAGE);
      if (option == JOptionPane.CANCEL_OPTION) {
        return false;
      } else if (option == JOptionPane.NO_OPTION) {
        return true;
      } else {
        if (saveAllSettings()) {
          return true;
        } else {
          final JLabel message2 = new JLabel(
            "<html><body><p>Saving project failed.</b></p>"
              + "<p><b>Do you want to ignore any changes and continue?</b></p></body></html>");

          final int option2 = JOptionPane.showConfirmDialog(mapPanel, message2,
            "Ignore Changes", JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE);
          if (option2 == JOptionPane.CANCEL_OPTION) {
            return false;
          } else {
            return true;
          }
        }
      }
    }
  }

  public boolean saveWithPrompt() {
    boolean result = saveChangesWithPrompt();
    if (result) {
      result = saveSettingsWithPrompt();
    }
    return result;
  }

  public void setDataStores(final DataObjectStoreConnectionRegistry dataStores) {
    this.dataStores = dataStores;
  }

  public void setFolderConnections(
    final FolderConnectionRegistry folderConnections) {
    this.folderConnections = folderConnections;
  }

  @Override
  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    if (geometryFactory != null) {
      super.setGeometryFactory(geometryFactory);
      firePropertyChange("srid", -2, geometryFactory.getSrid());
    }
  }

  @Override
  public void setProperty(final String name, final Object value) {
    if ("srid".equals(name)) {
      try {
        final Integer srid = StringConverterRegistry.toObject(Integer.class,
          value);
        setGeometryFactory(GeometryFactory.floating3(srid));
      } catch (final Throwable t) {
      }
    } else if ("viewBoundingBox".equals(name)) {
      if (value != null) {
        final BoundingBox viewBoundingBox = BoundingBoxDoubleGf.create(value.toString());
        if (!BoundingBoxUtil.isEmpty(viewBoundingBox)) {
          initialBoundingBox = viewBoundingBox;
          setGeometryFactory(viewBoundingBox.getGeometryFactory());
          setViewBoundingBox(viewBoundingBox);
        }
      }
    } else {
      super.setProperty(name, value);
    }
  }

  public void setSrid(final Number srid) {
    if (srid != null) {
      setGeometryFactory(GeometryFactory.floating3(srid.intValue()));
    }
  }

  public void setViewBoundingBox(BoundingBox viewBoundingBox) {
    if (!BoundingBoxUtil.isEmpty(viewBoundingBox)) {
      // TODO really should be min scale
      double minDimension;
      if (viewBoundingBox.getCoordinateSystem() instanceof GeographicCoordinateSystem) {
        minDimension = 0.000005;
      } else {
        minDimension = 0.5;
      }
      final BoundingBox oldValue = this.viewBoundingBox;
      final double width = viewBoundingBox.getWidth();
      if (width < minDimension) {
        viewBoundingBox = viewBoundingBox.expand((minDimension - width) / 2, 0);
      }
      final double height = viewBoundingBox.getHeight();
      if (height < minDimension) {
        viewBoundingBox = viewBoundingBox.expand(0, (minDimension - height) / 2);
      }
      this.viewBoundingBox = viewBoundingBox;
      getPropertyChangeSupport().firePropertyChange("viewBoundingBox",
        oldValue, viewBoundingBox);
    }
  }

  public void setZoomBookmarks(final Map<String, ?> zoomBookmarks) {
    final Map<String, BoundingBox> bookmarks = new LinkedHashMap<String, BoundingBox>();
    if (zoomBookmarks != null) {
      for (final Entry<String, ?> entry : zoomBookmarks.entrySet()) {
        final String name = entry.getKey();
        final Object object = entry.getValue();
        if (object != null && name != null) {
          try {
            BoundingBox boundingBox = null;
            if (object instanceof BoundingBoxDoubleGf) {
              boundingBox = (BoundingBox)object;
            } else if (object instanceof Geometry) {
              final Geometry geometry = (Geometry)object;
              boundingBox = geometry.getBoundingBox();
            } else if (object != null) {
              final String wkt = object.toString();
              boundingBox = BoundingBoxDoubleGf.create(wkt);
            }
            if (boundingBox != null) {
              bookmarks.put(name, boundingBox);
            }
          } catch (final Throwable e) {
            ExceptionUtil.log(getClass(), "Not a valid bounding box " + name
              + "=" + object, e);
          }
        }
      }
      this.zoomBookmarks = bookmarks;
    }
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = super.toMap();

    BoundingBox boundingBox = getViewBoundingBox();
    if (!BoundingBoxUtil.isEmpty(boundingBox)) {
      BoundingBox defaultBoundingBox = null;
      final GeometryFactory geometryFactory = getGeometryFactory();
      if (geometryFactory != null) {
        final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
        if (coordinateSystem != null) {
          defaultBoundingBox = coordinateSystem.getAreaBoundingBox();
        }
        boundingBox = boundingBox.convert(geometryFactory);
      }
      MapSerializerUtil.add(map, "viewBoundingBox", boundingBox,
        defaultBoundingBox);
      final Map<String, BoundingBox> zoomBookmarks = getZoomBookmarks();
      MapSerializerUtil.add(map, "zoomBookmarks", zoomBookmarks);
    }

    return map;
  }

}
