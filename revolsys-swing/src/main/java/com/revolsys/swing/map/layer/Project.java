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

import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.logging.Logs;
import org.jeometry.common.number.Integers;

import com.revolsys.connection.file.FolderConnectionRegistry;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.editor.BoundingBoxEditor;
import com.revolsys.geometry.util.RectangleUtil;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.FileUtil;
import com.revolsys.io.file.Paths;
import com.revolsys.io.filter.FileNameExtensionFilter;
import com.revolsys.record.io.RecordStoreConnectionRegistry;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.spring.resource.PathResource;
import com.revolsys.spring.resource.Resource;
import com.revolsys.swing.Dialogs;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.map.ProjectFrame;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.util.PreferencesUtil;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;
import com.revolsys.webservice.WebServiceConnectionRegistry;

public class Project extends LayerGroup {
  private static WeakReference<Project> projectReference = new WeakReference<>(null);

  static {
    MenuFactory.addMenuInitializer(Project.class, (menu) -> {
      menu.deleteMenuItem("layer", "Delete");
    });
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

  private final FolderConnectionRegistry folderConnections;

  private BoundingBox initialBoundingBox;

  private final RecordStoreConnectionRegistry recordStores;

  private Resource resource;

  private BoundingBox viewBoundingBox = BoundingBox.empty();

  private final WebServiceConnectionRegistry webServices;

  private Map<String, BoundingBox> zoomBookmarks = new LinkedHashMap<>();

  private final String connectionRegistryName;

  public Project() {
    this("Project");
  }

  public Project(final String connectionRegistryName) {
    super("Project");
    setType("Project");
    this.connectionRegistryName = connectionRegistryName;
    this.baseMapLayers.setLayerGroup(this);
    setGeometryFactory(GeometryFactory.worldMercator());

    this.folderConnections = new FolderConnectionRegistry(connectionRegistryName);
    this.recordStores = new RecordStoreConnectionRegistry(connectionRegistryName);
    this.webServices = new WebServiceConnectionRegistry(connectionRegistryName);
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
  public BoundingBox getBoundingBox() {
    final BoundingBox boundingBox = super.getBoundingBox();
    if (boundingBox.isEmpty()) {
      return getAreaBoundingBox();
    } else {
      return boundingBox;
    }
  }

  public String getConnectionRegistryName() {
    return this.connectionRegistryName;
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
    if (this.resource instanceof PathResource) {
      final PathResource pathResource = (PathResource)this.resource;
      final Path directory = pathResource.getPath();
      if (!Files.exists(directory)) {
        try {
          Files.createDirectories(directory);
        } catch (final IOException e) {
          throw Exceptions.wrap(e);
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

  public Resource getResource() {
    return this.resource;
  }

  public Path getSaveAsDirectory() {
    File directory = null;
    final JFileChooser fileChooser = SwingUtil.newFileChooser("Save Project",
      "com.revolsys.swing.map.project", "directory");
    fileChooser.setFileFilter(new FileNameExtensionFilter("Project", "rgmap"));
    fileChooser.setMultiSelectionEnabled(false);
    final int returnVal = Dialogs.showSaveDialog(fileChooser);
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
      this.resource = new PathResource(directory);
      return directory.toPath();
    } else {
      return null;
    }
  }

  public BoundingBox getViewBoundingBox() {
    return this.viewBoundingBox;
  }

  public WebServiceConnectionRegistry getWebServices() {
    return this.webServices;
  }

  public Map<String, BoundingBox> getZoomBookmarks() {
    return this.zoomBookmarks;
  }

  @Override
  protected void importProject(final Project importProject) {
    super.importProject(importProject);

    final BaseMapLayerGroup baseMaps = getBaseMapLayers();
    baseMaps.importProjectBaseMaps(importProject);
  }

  public boolean isBaseMapLayer(final Layer layer) {
    return this.baseMapLayers.isDescendant(layer);
  }

  public boolean isSaved() {
    return this.resource != null;
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

  public BaseMapLayerGroup readBaseMapsLayers(final Project rootProject, final Resource resource) {
    final Resource baseMapsResource = resource.newChildResource("Base Maps");
    final Resource layerGroupResource = baseMapsResource.newChildResource("rgLayerGroup.rgobject");
    if (layerGroupResource.exists()) {
      final Resource oldResource = Resource.setBaseResource(baseMapsResource);
      try {
        final Map<String, Object> properties = Json.toMap(layerGroupResource);
        this.baseMapLayers.loadLayers(rootProject, properties);
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
        Resource.setBaseResource(oldResource);
      }
    }
    return this.baseMapLayers;
  }

  protected void readLayers(final Project rootProject, final Resource resource) {
    final Resource layerGroupResource = resource.newChildResource("rgLayerGroup.rgobject");
    if (!layerGroupResource.exists()) {
      Logs.error(this, "File not found: " + layerGroupResource);
    } else {
      final Resource oldResource = Resource.setBaseResource(resource);
      try {
        final Map<String, Object> properties = Json.toMap(layerGroupResource);
        loadLayers(rootProject, properties);
      } catch (final RuntimeException e) {
        Logs.error(this, "Unable to read: " + layerGroupResource, e);
      } finally {
        Resource.setBaseResource(oldResource);
      }
    }
  }

  public void readProject(final Project rootProject, final Resource resource) {
    this.resource = resource;
    if (this.resource.exists()) {
      String name;
      try (
        final BaseCloseable booleanValueCloseable = eventsDisabled()) {

        final RecordStoreConnectionRegistry oldRecordStoreConnections = RecordStoreConnectionRegistry
          .getForThread();
        try {
          final boolean readOnly = isReadOnly();

          final Resource folderConnectionsDirectory = this.resource
            .newChildResource("Folder Connections");
          this.folderConnections.clear(folderConnectionsDirectory, readOnly);

          final Resource recordStoresDirectory = this.resource.newChildResource("Record Stores");
          this.recordStores.clear(recordStoresDirectory, readOnly);

          final Resource webServicesDirectory = this.resource.newChildResource("Web Services");
          this.webServices.clear(webServicesDirectory, readOnly);

          if (rootProject == null) {
            RecordStoreConnectionRegistry.setForThread(this.recordStores);
          } else {
            final WebServiceConnectionRegistry rootWebServices = rootProject.getWebServices();
            importConnections("Web Service", this, this.webServices, rootWebServices);

            final RecordStoreConnectionRegistry rootRecordStores = rootProject.getRecordStores();
            rootProject.importConnections("Record Store", this, this.recordStores,
              rootRecordStores);

            final FolderConnectionRegistry rootFolderConnections = rootProject
              .getFolderConnections();
            rootProject.importConnections("Folder Connection", this, this.folderConnections,
              rootFolderConnections);
          }
          final Resource layersDir = this.resource.newChildResource("Layers");
          final boolean hasLayers = layersDir.exists();
          if (hasLayers) {
            readProperties(layersDir);
          }
          if (hasLayers) {
            readLayers(rootProject, layersDir);
          }

          readBaseMapsLayers(rootProject, this.resource);
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
      Logs.error(this, "File not found: " + layerGroupResource);
    } else {
      final Resource oldResource = Resource.setBaseResource(resource);
      try {
        final Map<String, Object> properties = Json.toMap(layerGroupResource);
        setProperties(properties);
      } catch (final Throwable e) {
        Logs.error(this, "Unable to read: " + layerGroupResource, e);
      } finally {
        Resource.setBaseResource(oldResource);
      }
    }
  }

  public void removeZoomBookmark(final String name) {
    if (name != null) {
      this.zoomBookmarks.remove(name);
    }
  }

  @Override
  public void reset() {
    super.reset();
    setName("Project");
    this.baseMapLayers.clear();

    this.recordStores.clear();
    this.folderConnections.clear();
    this.webServices.clear();

    this.initialBoundingBox = null;
    this.resource = null;
    this.viewBoundingBox = BoundingBox.empty();
    this.zoomBookmarks.clear();
    firePropertyChange("reset", false, true);
  }

  public void save() {
  }

  public boolean saveAllSettings() {
    saveChanges();
    if (isReadOnly()) {
      return true;
    } else {
      final Path directory = getDirectory();
      final boolean saveAllSettings = super.saveAllSettings(directory);
      if (saveAllSettings) {
        this.recordStores.saveAs(this.resource, "Record Stores");
        this.folderConnections.saveAs(this.resource, "Folder Connections");
        this.webServices.saveAs(this.resource, "Web Services");
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
      final List<Layer> layersWithChanges = new ArrayList<>();
      addChangedLayers(this, layersWithChanges);

      if (layersWithChanges.isEmpty()) {
        return true;
      } else {
        final JLabel message = new JLabel(
          "<html><body><p><b>The following layers have un-saved changes.</b></p>"
            + "<p><b>Do you want to save the changes before continuing?</b></p><ul><li>"
            + Strings.toString("</li>\n<li>", layersWithChanges) + "</li></ul></body></html>");

        final int option = Dialogs.showConfirmDialog(message, "Save Changes", optionType,
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
                + Strings.toString("</li>\n<li>", layersWithChanges) + "</li></ul></body></html>");

            final int option2 = Dialogs.showConfirmDialog(message2, "Ignore Changes",
              JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (option2 == JOptionPane.YES_OPTION) {
              return true;
            } else {
              return false;
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
      final JLabel message = new JLabel(
        "<html><body><p><b>Save changes to project?</b></p></body></html>");

      final int option = Dialogs.showConfirmDialog(message, "Save Changes",
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

          final int option2 = Dialogs.showConfirmDialog(message2, "Ignore Changes",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
          if (option2 == JOptionPane.YES_OPTION) {
            return true;
          } else {
            return false;
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

  @Override
  public void setProperty(final String name, final Object value) {
    if ("srid".equals(name)) {
      try {
        final Integer srid = Integers.toValid(value);
        if (srid != null) {
          setGeometryFactory(GeometryFactory.floating3d(srid));
        }
      } catch (final RuntimeException t) {
      }
    } else if ("viewBoundingBox".equals(name)) {
      if (value != null) {
        final BoundingBox viewBoundingBox = BoundingBox.bboxNew(value.toString());
        if (!RectangleUtil.isEmpty(viewBoundingBox)) {
          this.initialBoundingBox = viewBoundingBox;
          setViewBoundingBoxAndGeometryFactory(viewBoundingBox);
        }
      }
    } else {
      super.setProperty(name, value);
    }
  }

  public void setSrid(final Number srid) {
    if (srid != null) {
      final GeometryFactory geometryFactory = GeometryFactory.floating3d(srid.intValue());
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

      final GeometryFactory geometryFactory = viewBoundingBox.getGeometryFactory();
      setGeometryFactory(geometryFactory);

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
      if (viewBoundingBox.getGeometryFactory().isGeographic()) {
        minDimension = 0.000005;
      } else {
        minDimension = 0.5;
      }
      final BoundingBoxEditor bboxEditor = viewBoundingBox.bboxEditor();
      final double width = viewBoundingBox.getWidth();
      if (width < minDimension) {
        bboxEditor.expandDeltaX((minDimension - width) / 2);
      }
      final double height = viewBoundingBox.getHeight();
      if (height < minDimension) {
        bboxEditor.expandDeltaY((minDimension - height) / 2);
      }
      viewBoundingBox = bboxEditor.newBoundingBox();
      final BoundingBox oldBoundingBox = this.viewBoundingBox;
      this.viewBoundingBox = viewBoundingBox;
      return !viewBoundingBox.equals(oldBoundingBox);
    }
  }

  public void setZoomBookmarks(final Map<String, ?> zoomBookmarks) {
    final Map<String, BoundingBox> bookmarks = new LinkedHashMap<>();
    if (zoomBookmarks != null) {
      for (final Entry<String, ?> entry : zoomBookmarks.entrySet()) {
        final String name = entry.getKey();
        final Object object = entry.getValue();
        if (object != null && name != null) {
          try {
            BoundingBox boundingBox = null;
            if (object instanceof BoundingBox) {
              boundingBox = (BoundingBox)object;
            } else if (object instanceof Geometry) {
              final Geometry geometry = (Geometry)object;
              boundingBox = geometry.getBoundingBox();
            } else if (object != null) {
              final String wkt = object.toString();
              boundingBox = BoundingBox.bboxNew(wkt);
            }
            if (boundingBox != null) {
              bookmarks.put(name, boundingBox);
            }
          } catch (final Throwable e) {
            Logs.error(this, "Not a valid bounding box " + name + "=" + object, e);
          }
        }
      }
      this.zoomBookmarks = bookmarks;
    }
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = super.toMap();

    BoundingBox boundingBox = getViewBoundingBox();
    if (!RectangleUtil.isEmpty(boundingBox)) {
      final BoundingBox defaultBoundingBox = getAreaBoundingBox();
      final GeometryFactory geometryFactory = getGeometryFactory();
      if (geometryFactory != null) {
        boundingBox = boundingBox.bboxToCs(geometryFactory);
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
