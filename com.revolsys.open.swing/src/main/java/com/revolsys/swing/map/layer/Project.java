package com.revolsys.swing.map.layer;

import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
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

import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.geometry.util.BoundingBoxUtil;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.FileUtil;
import com.revolsys.io.file.FileConnectionManager;
import com.revolsys.io.file.FolderConnectionRegistry;
import com.revolsys.io.file.Paths;
import com.revolsys.logging.Logs;
import com.revolsys.record.io.RecordStoreConnectionManager;
import com.revolsys.record.io.RecordStoreConnectionRegistry;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.spring.resource.FileSystemResource;
import com.revolsys.spring.resource.PathResource;
import com.revolsys.spring.resource.Resource;
import com.revolsys.spring.resource.SpringUtil;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.ProjectFrame;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.util.PreferencesUtil;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;
import com.revolsys.util.WrappedException;
import com.revolsys.util.number.Integers;

public class Project extends LayerGroup {
  private static WeakReference<Project> projectReference = new WeakReference<>(null);

  static {
    final MenuFactory menu = MenuFactory.getMenu(Project.class);
    menu.deleteMenuItem("layer", "Delete");
  }

  public static synchronized void clearProject(final Project project) {
    final WeakReference<Project> projectReference = Project.projectReference;
    if (Project.projectReference != null) {
      final Project currentProject = projectReference.get();
      if (currentProject == project) {
        Project.projectReference = new WeakReference<>(null);
      }
    }
  }

  public static synchronized Project get() {
    return Project.projectReference.get();
  }

  public static synchronized void set(final Project project) {
    Project.projectReference = new WeakReference<>(project);
  }

  private BaseMapLayerGroup baseMapLayers = new BaseMapLayerGroup();

  private FolderConnectionRegistry folderConnections = new FolderConnectionRegistry("Project");

  private BoundingBox initialBoundingBox;

  private RecordStoreConnectionRegistry recordStores = new RecordStoreConnectionRegistry("Project");

  private Resource resource;

  private BoundingBox viewBoundingBox = BoundingBox.EMPTY;

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
  public void delete() {
    super.delete();
    this.baseMapLayers = null;
    this.viewBoundingBox = null;
    this.zoomBookmarks = null;
  }

  public BaseMapLayerGroup getBaseMapLayers() {
    return this.baseMapLayers;
  }

  @Override
  public Path getDirectory() {
    final Path directory = getProjectDirectory();
    if (directory != null) {
      final Path layersDirectory = directory.resolve("Layers");
      Paths.createDirectories(layersDirectory);
      if (Files.isDirectory(layersDirectory)) {
        return layersDirectory;
      }
    }
    return null;
  }

  public FolderConnectionRegistry getFolderConnections() {
    return this.folderConnections;
  }

  @Override
  protected Path getGroupSettingsDirectory(final Path directory) {
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

  public Path getProjectDirectory() {
    if (this.resource == null) {
      final Path directory = getSaveAsDirectory();
      return directory;
    }
    if (this.resource instanceof FileSystemResource) {
      final FileSystemResource fileResource = (FileSystemResource)this.resource;
      final File directory = fileResource.getFile();
      if (!directory.exists()) {
        directory.mkdirs();
      }
      if (directory.isDirectory()) {
        return directory.toPath();
      }
    } else if (this.resource instanceof PathResource) {
      final PathResource pathResource = (PathResource)this.resource;
      final Path directory = pathResource.getPath();
      if (!Files.exists(directory)) {
        try {
          Files.createDirectories(directory);
        } catch (final IOException e) {
          throw new WrappedException(e);
        }
      }
      if (Files.isDirectory(directory)) {
        return directory;
      }
    }
    return null;
  }

  public RecordStoreConnectionRegistry getRecordStores() {
    return this.recordStores;
  }

  public Path getSaveAsDirectory() {
    File directory = null;
    final JFileChooser fileChooser = SwingUtil.newFileChooser("Save Project",
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
      return directory.toPath();
    } else {
      return null;
    }
  }

  public BoundingBox getViewBoundingBox() {
    return this.viewBoundingBox;
  }

  public Map<String, BoundingBox> getZoomBookmarks() {
    return this.zoomBookmarks;
  }

  @Override
  protected ValueField newPropertiesTabGeneralPanelSource(final BasePanel parent) {
    final ValueField panel = super.newPropertiesTabGeneralPanelSource(parent);
    if (this.resource != null) {
      SwingUtil.addLabelledReadOnlyTextField(panel, "URL", this.resource.getURL());
      GroupLayouts.makeColumns(panel, 2, true);
    }
    return panel;
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
    final Resource baseMapsResource = resource.newChildResource("Base Maps");
    final Resource layerGroupResource = baseMapsResource.newChildResource("rgLayerGroup.rgobject");
    if (layerGroupResource.exists()) {
      final Resource oldResource = SpringUtil.setBaseResource(baseMapsResource);
      try {
        final Map<String, Object> properties = Json.toMap(layerGroupResource);
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
    final Resource layerGroupResource = resource.newChildResource("rgLayerGroup.rgobject");
    if (!layerGroupResource.exists()) {
      LoggerFactory.getLogger(getClass()).error("File not found: " + layerGroupResource);
    } else {
      final Resource oldResource = SpringUtil.setBaseResource(resource);
      try {
        final Map<String, Object> properties = Json.toMap(layerGroupResource);
        loadLayers(properties);
      } catch (final Throwable e) {
        LoggerFactory.getLogger(getClass()).error("Unable to read: " + layerGroupResource, e);
      } finally {
        SpringUtil.setBaseResource(oldResource);
      }
    }
  }

  public void readProject(final Path path) {
    final Resource resource = new PathResource(path);
    readProject(resource);
  }

  public void readProject(final Resource resource) {
    this.resource = resource;
    if (resource.exists()) {
      String name;
      try (
        final BaseCloseable booleanValueCloseable = eventsDisabled()) {
        final Resource layersDir = resource.newChildResource("Layers");
        readProperties(layersDir);

        final RecordStoreConnectionRegistry oldRecordStoreConnections = RecordStoreConnectionRegistry
          .getForThread();
        try {
          final Resource recordStoresDirectory = resource.newChildResource("Record Stores");
          if (!recordStoresDirectory.exists()) {
            final Resource dataStoresDirectory = resource.newChildResource("Data Stores");
            if (dataStoresDirectory.exists()) {
              final File file = dataStoresDirectory.getFile();
              file.renameTo(new File(file.getParentFile(), "Record Stores"));
            }
          }

          final boolean readOnly = isReadOnly();
          final RecordStoreConnectionRegistry recordStores = new RecordStoreConnectionRegistry(
            "Project", recordStoresDirectory, readOnly);
          setRecordStores(recordStores);
          RecordStoreConnectionRegistry.setForThread(recordStores);

          final Resource folderConnectionsDirectory = resource
            .newChildResource("Folder Connections");
          this.folderConnections = new FolderConnectionRegistry("Project",
            folderConnectionsDirectory, readOnly);

          readLayers(layersDir);

          readBaseMapsLayers(resource);
        } finally {
          RecordStoreConnectionRegistry.setForThread(oldRecordStoreConnections);
        }
        name = getName();
        setName(null);
      }
      setName(name);
    }
  }

  protected void readProperties(final Resource resource) {
    final Resource layerGroupResource = resource.newChildResource("rgLayerGroup.rgobject");
    if (!layerGroupResource.exists()) {
      LoggerFactory.getLogger(getClass()).error("File not found: " + layerGroupResource);
    } else {
      final Resource oldResource = SpringUtil.setBaseResource(resource);
      try {
        final Map<String, Object> properties = Json.toMap(layerGroupResource);
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
    setName("Project");
    this.baseMapLayers.clear();
    this.recordStores = new RecordStoreConnectionRegistry("Project");
    this.folderConnections = new FolderConnectionRegistry("Project");
    this.initialBoundingBox = null;
    this.resource = null;
    this.viewBoundingBox = BoundingBox.EMPTY;
    this.zoomBookmarks.clear();
    firePropertyChange("reset", false, true);
  }

  public void save() {
  }

  public boolean saveAllSettings() {
    if (isReadOnly()) {
      return true;
    } else {
      final Path directory = getDirectory();
      final boolean saveAllSettings = super.saveAllSettings(directory);
      if (saveAllSettings) {
        final RecordStoreConnectionManager recordStoreConnectionManager = RecordStoreConnectionManager
          .get();
        final RecordStoreConnectionRegistry recordStoreConnections = recordStoreConnectionManager
          .getConnectionRegistry("Project");
        recordStoreConnections.saveAs(this.resource, "Record Stores");

        final FileConnectionManager fileConnectionManager = FileConnectionManager.get();
        final FolderConnectionRegistry folderConnections = fileConnectionManager
          .getConnectionRegistry("Project");
        folderConnections.saveAs(this.resource, "Folder Connections");
      }
      return saveAllSettings;
    }
  }

  public Path saveAllSettingsAs() {
    final Resource resource = this.resource;
    try {
      this.resource = null;
      final Path projectPath = getSaveAsDirectory();
      if (projectPath != null) {
        setName(Paths.getBaseName(projectPath));
        saveAllSettings();
      }
      return projectPath;
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
        final MapPanel mapPanel = getMapPanel();
        final JLabel message = new JLabel(
          "<html><body><p><b>The following layers have un-saved changes.</b></p>"
            + "<p><b>Do you want to save the changes before continuing?</b></p><ul><li>"
            + Strings.toString("</li>\n<li>", layersWithChanges) + "</li></ul></body></html>");

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
                + Strings.toString("</li>\n<li>", layersWithChanges) + "</li></ul></body></html>");

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

  @Override
  protected boolean saveSettingsDo(final Path directory) {
    boolean saved = true;
    FileUtil.deleteDirectory(directory.toFile(), false);
    Paths.createDirectories(directory);

    saved &= super.saveSettingsDo(directory);

    final Path projectPath = getProjectDirectory();
    if (projectPath == null) {
      return false;
    } else {
      final Path baseMapsPath = projectPath.resolve("Base Maps");
      FileUtil.deleteDirectory(baseMapsPath.toFile(), false);
      final LayerGroup baseMapLayers = getBaseMapLayers();
      if (baseMapLayers != null) {
        saved &= baseMapLayers.saveAllSettings(projectPath);
      }
      return saved;
    }
  }

  public boolean saveSettingsWithPrompt() {
    if (isReadOnly()) {
      return true;
    } else {
      final MapPanel mapPanel = getMapPanel();
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
    super.setGeometryFactory(geometryFactory);
  }

  @Override
  public void setProperty(final String name, final Object value) {
    if ("srid".equals(name)) {
      try {
        final Integer srid = Integers.toValid(value);
        if (srid != null) {
          setGeometryFactory(GeometryFactory.floating3(srid));
        }
      } catch (final Throwable t) {
      }
    } else if ("viewBoundingBox".equals(name)) {
      if (value != null) {
        final BoundingBox viewBoundingBox = BoundingBox.newBoundingBox(value.toString());
        if (!BoundingBoxUtil.isEmpty(viewBoundingBox)) {
          this.initialBoundingBox = viewBoundingBox;
          setViewBoundingBoxAndGeometryFactory(viewBoundingBox);
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
      final GeometryFactory geometryFactory = GeometryFactory.floating3(srid.intValue());
      setGeometryFactory(geometryFactory);
    }
  }

  public void setViewBoundingBox(final BoundingBox viewBoundingBox) {
    final BoundingBox oldBoundingBox = this.viewBoundingBox;
    final boolean bboxUpdated = setViewBoundingBoxDo(viewBoundingBox);
    if (bboxUpdated) {
      firePropertyChange("viewBoundingBox", oldBoundingBox, this.viewBoundingBox);
    }
  }

  public void setViewBoundingBoxAndGeometryFactory(final BoundingBox viewBoundingBox) {
    if (!Property.isEmpty(viewBoundingBox)) {
      final BoundingBox oldBoundingBox = this.viewBoundingBox;
      final boolean bboxUpdated = setViewBoundingBoxDo(viewBoundingBox);

      final GeometryFactory oldGeometryFactory = getGeometryFactory();
      final GeometryFactory geometryFactory = viewBoundingBox.getGeometryFactory();
      final boolean geometryFactoryUpdated = setGeometryFactoryDo(geometryFactory);

      if (geometryFactoryUpdated) {
        fireGeometryFactoryChanged(oldGeometryFactory, geometryFactory);
      }
      if (bboxUpdated) {
        firePropertyChange("viewBoundingBox", oldBoundingBox, this.viewBoundingBox);
      }
    }
  }

  protected boolean setViewBoundingBoxDo(BoundingBox viewBoundingBox) {
    if (Property.isEmpty(viewBoundingBox)) {
      return false;
    } else {
      // TODO really should be min scale
      double minDimension;
      if (viewBoundingBox.getCoordinateSystem() instanceof GeographicCoordinateSystem) {
        minDimension = 0.000005;
      } else {
        minDimension = 0.5;
      }
      final double width = viewBoundingBox.getWidth();
      if (width < minDimension) {
        viewBoundingBox = viewBoundingBox.expand((minDimension - width) / 2, 0);
      }
      final double height = viewBoundingBox.getHeight();
      if (height < minDimension) {
        viewBoundingBox = viewBoundingBox.expand(0, (minDimension - height) / 2);
      }
      final BoundingBox oldBoundingBox = this.viewBoundingBox;
      this.viewBoundingBox = viewBoundingBox;
      return !viewBoundingBox.equals(oldBoundingBox);
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
              boundingBox = BoundingBox.newBoundingBox(wkt);
            }
            if (boundingBox != null) {
              bookmarks.put(name, boundingBox);
            }
          } catch (final Throwable e) {
            Logs.error(getClass(), "Not a valid bounding box " + name + "=" + object, e);
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
      addToMap(map, "viewBoundingBox", boundingBox, defaultBoundingBox);
      final Map<String, BoundingBox> zoomBookmarks = getZoomBookmarks();
      addToMap(map, "zoomBookmarks", zoomBookmarks);
    }
    final ProjectFrame projectFrame = ProjectFrame.get(this);
    if (projectFrame != null) {
      final Rectangle frameBounds = projectFrame.getBounds();
      if (frameBounds != null) {
        map.put("frameBounds",
          Arrays.asList(frameBounds.x, frameBounds.y, frameBounds.width, frameBounds.height));
      }
    }

    return map;
  }

}
