package com.revolsys.swing.map.layer;

import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.format.json.JsonMapIoFactory;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.io.FileUtil;
import com.revolsys.io.datastore.RecordStoreConnectionManager;
import com.revolsys.io.datastore.RecordStoreConnectionRegistry;
import com.revolsys.io.file.FolderConnectionManager;
import com.revolsys.io.file.FolderConnectionRegistry;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.jts.util.BoundingBoxUtil;
import com.revolsys.spring.SpringUtil;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.ProjectFrame;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.ExceptionUtil;
import com.revolsys.util.PreferencesUtil;

public class Project extends LayerGroup {

  static {
    final MenuFactory menu = MenuFactory.getMenu(Project.class);
    menu.deleteMenuItem("layer", "Delete Layer");
  }

  private static WeakReference<Project> project = new WeakReference<Project>(null);

  public static Project get() {
    return Project.project.get();
  }

  public static void set(final Project project) {
    Project.project = new WeakReference<Project>(project);
  }

  private BaseMapLayerGroup baseMapLayers = new BaseMapLayerGroup();

  private RecordStoreConnectionRegistry recordStores = new RecordStoreConnectionRegistry("Project");

  private FolderConnectionRegistry folderConnections = new FolderConnectionRegistry("Project");

  private BoundingBox initialBoundingBox;

  private Resource resource;

  private BoundingBox viewBoundingBox = new BoundingBoxDoubleGf();

  private Map<String, BoundingBox> zoomBookmarks = new LinkedHashMap<String, BoundingBox>();

  public Project() {
    this("Project");
  }

  public Project(final String name) {
    super(name);
    setType("Project");
    this.baseMapLayers.setLayerGroup(this);
    setGeometryFactory(GeometryFactory.worldMercator());
  }

  private void addChangedLayers(final LayerGroup group, final List<Layer> layersWithChanges) {
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
      this.zoomBookmarks.put(name, boundingBox);
    }
  }

  @Override
  protected ValueField createPropertiesTabGeneralPanelSource(final BasePanel parent) {
    final ValueField panel = super.createPropertiesTabGeneralPanelSource(parent);
    if (this.resource != null) {
      try {
        SwingUtil.addLabelledReadOnlyTextField(panel, "URL", this.resource.getURL());
        GroupLayoutUtil.makeColumns(panel, 2, true);
      } catch (final IOException e) {
      }

    }
    return panel;
  }

  @Override
  public void delete() {
    super.delete();
    this.baseMapLayers = null;
    this.viewBoundingBox = null;
    this.zoomBookmarks = null;
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
    return this.baseMapLayers;
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
    return this.folderConnections;
  }

  @Override
  protected File getGroupSettingsDirectory(final File directory) {
    return directory;
  }

  public BoundingBox getInitialBoundingBox() {
    return this.initialBoundingBox;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V extends Layer> V getLayer(final String name) {
    if (name.equals("Base Maps")) {
      return (V)this.baseMapLayers;
    } else {
      return (V)super.getLayer(name);
    }
  }

  @Override
  public Project getProject() {
    return this;
  }

  public File getProjectDirectory() {
    if (this.resource == null) {
      final File directory = getSaveAsDirectory();
      return directory;
    }
    if (this.resource instanceof FileSystemResource) {
      final FileSystemResource fileResource = (FileSystemResource)this.resource;
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

  public RecordStoreConnectionRegistry getRecordStores() {
    return this.recordStores;
  }

  public File getSaveAsDirectory() {
    File directory = null;
    final JFileChooser fileChooser = SwingUtil.createFileChooser("Save Project",
      "com.revolsys.swing.map.project", "directory");
    fileChooser.setFileFilter(new FileNameExtensionFilter("Project", "rgmap"));
    fileChooser.setMultiSelectionEnabled(false);
    final int returnVal = fileChooser.showSaveDialog(SwingUtil.getActiveWindow());
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      directory = fileChooser.getSelectedFile();
      final String fileExtension = FileUtil.getFileNameExtension(directory);
      if (!"rgmap".equals(fileExtension)) {
        directory = new File(directory.getParentFile(), directory.getName() + ".rgmap");
      }
      PreferencesUtil.setUserString("com.revolsys.swing.map.project", "directory",
        directory.getParent());

      if (directory.exists()) {
        FileUtil.deleteDirectory(directory);
      }
      directory.mkdirs();
      this.resource = new FileSystemResource(directory);
    }
    return directory;
  }

  public BoundingBox getViewBoundingBox() {
    return this.viewBoundingBox;
  }

  public Map<String, BoundingBox> getZoomBookmarks() {
    return this.zoomBookmarks;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    super.propertyChange(event);
    if (event.getPropertyName().equals("hasSelectedRecords")) {
      final boolean selected = isHasSelectedRecords();
      firePropertyChange("hasSelectedRecords", !selected, selected);
    }
  }

  protected void readBaseMapsLayers(final Resource resource) {
    final Resource baseMapsResource = SpringUtil.getResource(resource, "Base Maps");
    final Resource layerGroupResource = SpringUtil.getResource(baseMapsResource,
      "rgLayerGroup.rgobject");
    if (layerGroupResource.exists()) {
      final Resource oldResource = SpringUtil.setBaseResource(baseMapsResource);
      try {
        final Map<String, Object> properties = JsonMapIoFactory.toMap(layerGroupResource);
        this.baseMapLayers.loadLayers(properties);
        boolean hasVisible = false;
        if (this.baseMapLayers != null) {
          for (final Layer layer : this.baseMapLayers) {
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
    final Resource layerGroupResource = SpringUtil.getResource(resource, "rgLayerGroup.rgobject");
    if (!layerGroupResource.exists()) {
      LoggerFactory.getLogger(getClass()).error("File not found: " + layerGroupResource);
    } else {
      final Resource oldResource = SpringUtil.setBaseResource(resource);
      try {
        final Map<String, Object> properties = JsonMapIoFactory.toMap(layerGroupResource);
        loadLayers(properties);
      } catch (final Throwable e) {
        LoggerFactory.getLogger(getClass()).error("Unable to read: " + layerGroupResource, e);
      } finally {
        SpringUtil.setBaseResource(oldResource);
      }
    }
  }

  public void readProject(final Resource resource) {
    this.resource = resource;
    if (resource.exists()) {
      setEventsEnabled(false);
      String name;
      try {
        final Resource layersDir = SpringUtil.getResource(resource, "Layers");
        readProperties(layersDir);

        final RecordStoreConnectionRegistry oldRecordStoreConnections = RecordStoreConnectionRegistry.getForThread();
        try {
          final Resource recordStoresDirectory = SpringUtil.getResource(resource, "Record Stores");
          if (!recordStoresDirectory.exists()) {
            final Resource dataStoresDirectory = SpringUtil.getResource(resource, "Data Stores");
            if (dataStoresDirectory.exists()) {
              try {
                final File file = dataStoresDirectory.getFile();
                file.renameTo(new File(file.getParentFile(), "Record Stores"));
              } catch (final IOException e) {
              }
            }
          }

          final boolean readOnly = isReadOnly();
          final RecordStoreConnectionRegistry recordStores = new RecordStoreConnectionRegistry(
            "Project", recordStoresDirectory, readOnly);
          setRecordStores(recordStores);
          RecordStoreConnectionRegistry.setForThread(recordStores);

          final Resource folderConnectionsDirectory = SpringUtil.getResource(resource,
            "Folder Connections");
          this.folderConnections = new FolderConnectionRegistry("Project",
            folderConnectionsDirectory, readOnly);

          readLayers(layersDir);

          readBaseMapsLayers(resource);
        } finally {
          RecordStoreConnectionRegistry.setForThread(oldRecordStoreConnections);
        }
        name = getName();
        setName(null);
      } finally {
        setEventsEnabled(true);
      }
      setName(name);
    }
  }

  protected void readProperties(final Resource resource) {
    final Resource layerGroupResource = SpringUtil.getResource(resource, "rgLayerGroup.rgobject");
    if (!layerGroupResource.exists()) {
      LoggerFactory.getLogger(getClass()).error("File not found: " + layerGroupResource);
    } else {
      final Resource oldResource = SpringUtil.setBaseResource(resource);
      try {
        final Map<String, Object> properties = JsonMapIoFactory.toMap(layerGroupResource);
        setProperties(properties);
      } catch (final Throwable e) {
        LoggerFactory.getLogger(getClass()).error("Unable to read: " + layerGroupResource, e);
      } finally {
        SpringUtil.setBaseResource(oldResource);
      }
    }
  }

  public void removeZoomBookmark(final String name) {
    if (name != null) {
      this.zoomBookmarks.remove(name);
    }
  }

  public void reset() {
    clear();
    this.baseMapLayers.clear();
    this.recordStores = new RecordStoreConnectionRegistry("Project");
    this.folderConnections = new FolderConnectionRegistry("Project");
    this.initialBoundingBox = null;
    this.resource = null;
    this.viewBoundingBox = new BoundingBoxDoubleGf();
    this.zoomBookmarks.clear();
    firePropertyChange("reset", false, true);
  }

  public void save() {
  }

  public boolean saveAllSettings() {
    if (isReadOnly()) {
      return true;
    } else {
      final File directory = getDirectory();
      final boolean saveAllSettings = super.saveAllSettings(directory);
      if (saveAllSettings) {
        final RecordStoreConnectionManager recordStoreConnectionManager = RecordStoreConnectionManager.get();
        final RecordStoreConnectionRegistry recordStoreConnections = recordStoreConnectionManager.getConnectionRegistry("Project");
        recordStoreConnections.saveAs(this.resource, "Record Stores");

        final FolderConnectionManager folderConnectionManager = FolderConnectionManager.get();
        final FolderConnectionRegistry folderConnections = folderConnectionManager.getConnectionRegistry("Project");
        folderConnections.saveAs(this.resource, "Folder Connections");
      }
      return saveAllSettings;
    }
  }

  public File saveAllSettingsAs() {
    final Resource resource = this.resource;
    try {
      this.resource = null;
      final File directory = getSaveAsDirectory();
      if (directory != null) {
        setName(FileUtil.getBaseName(directory));
        saveAllSettings();
      }
      return directory;
    } finally {
      if (this.resource == null) {
        this.resource = resource;
      }
    }
  }

  public boolean saveChangesWithPrompt() {
    return saveChangesWithPrompt(JOptionPane.YES_NO_CANCEL_OPTION);
  }

  public boolean saveChangesWithPrompt(final int optionType) {
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

        final int option = JOptionPane.showConfirmDialog(mapPanel, message, "Save Changes",
          optionType, JOptionPane.WARNING_MESSAGE);
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

            final int option2 = JOptionPane.showConfirmDialog(mapPanel, message2, "Ignore Changes",
              JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
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

      final int option = JOptionPane.showConfirmDialog(mapPanel, message, "Save Changes",
        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
      if (option == JOptionPane.CANCEL_OPTION) {
        return false;
      } else if (option == JOptionPane.NO_OPTION) {
        return true;
      } else {
        if (saveAllSettings()) {
          return true;
        } else {
          final JLabel message2 = new JLabel("<html><body><p>Saving project failed.</b></p>"
            + "<p><b>Do you want to ignore any changes and continue?</b></p></body></html>");

          final int option2 = JOptionPane.showConfirmDialog(mapPanel, message2, "Ignore Changes",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
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

  public void setFolderConnections(final FolderConnectionRegistry folderConnections) {
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
        final Integer srid = StringConverterRegistry.toObject(Integer.class, value);
        setGeometryFactory(GeometryFactory.floating3(srid));
      } catch (final Throwable t) {
      }
    } else if ("viewBoundingBox".equals(name)) {
      if (value != null) {
        final BoundingBox viewBoundingBox = BoundingBoxDoubleGf.create(value.toString());
        if (!BoundingBoxUtil.isEmpty(viewBoundingBox)) {
          this.initialBoundingBox = viewBoundingBox;
          setGeometryFactory(viewBoundingBox.getGeometryFactory());
          setViewBoundingBox(viewBoundingBox);
        }
      }
    } else {
      super.setProperty(name, value);
    }
  }

  public void setRecordStores(final RecordStoreConnectionRegistry recordStores) {
    this.recordStores = recordStores;
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
      firePropertyChange("viewBoundingBox", oldValue, viewBoundingBox);
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
            ExceptionUtil.log(getClass(), "Not a valid bounding box " + name + "=" + object, e);
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
      MapSerializerUtil.add(map, "viewBoundingBox", boundingBox, defaultBoundingBox);
      final Map<String, BoundingBox> zoomBookmarks = getZoomBookmarks();
      MapSerializerUtil.add(map, "zoomBookmarks", zoomBookmarks);
    }
    final Rectangle frameBounds = ProjectFrame.get(this).getBounds();
    if (frameBounds != null) {
      map.put("frameBounds",
        Arrays.asList(frameBounds.x, frameBounds.y, frameBounds.width, frameBounds.height));
    }

    return map;
  }

}
