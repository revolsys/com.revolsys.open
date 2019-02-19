package com.revolsys.swing.map.layer;

import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import com.revolsys.collection.Parent;
import com.revolsys.collection.list.Lists;
import com.revolsys.collection.map.MapEx;
import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.elevation.cloud.PointCloudReadFactory;
import com.revolsys.elevation.gridded.GriddedElevationModelReaderFactory;
import com.revolsys.elevation.tin.TriangulatedIrregularNetworkReaderFactory;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.editor.BoundingBoxEditor;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactory;
import com.revolsys.io.PathName;
import com.revolsys.io.PathUtil;
import com.revolsys.io.connection.Connection;
import com.revolsys.io.connection.ConnectionRegistry;
import com.revolsys.io.file.FileNameExtensionFilter;
import com.revolsys.io.file.Paths;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.logging.Logs;
import com.revolsys.raster.GeoreferencedImageReadFactory;
import com.revolsys.record.io.RecordReaderFactory;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.spring.resource.PathResource;
import com.revolsys.spring.resource.Resource;
import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.map.component.CoordinateSystemField;
import com.revolsys.swing.map.layer.elevation.gridded.GriddedElevationModelLayer;
import com.revolsys.swing.map.layer.elevation.tin.TriangulatedIrregularNetworkLayer;
import com.revolsys.swing.map.layer.pointcloud.PointCloudLayer;
import com.revolsys.swing.map.layer.raster.GeoreferencedImageLayer;
import com.revolsys.swing.map.layer.record.FileRecordLayer;
import com.revolsys.swing.map.layer.record.ScratchRecordLayer;
import com.revolsys.swing.map.layer.record.renderer.GeometryStyleRecordLayerRenderer;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.menu.Menus;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.tree.node.file.PathTreeNode;
import com.revolsys.swing.tree.node.layer.LayerGroupTreeNode;
import com.revolsys.util.OS;
import com.revolsys.util.PreferencesUtil;
import com.revolsys.util.Property;
import com.revolsys.util.UrlUtil;

public class LayerGroup extends AbstractLayer implements Parent<Layer>, Iterable<Layer> {

  static {
    MenuFactory.addMenuInitializer(LayerGroup.class, menu -> {
      menu.addGroup(0, "group");
      Menus.<LayerGroup> addMenuItem(menu, "group", "Add Group",
        Icons.getIconWithBadge(PathTreeNode.getIconFolder(), "add"),
        LayerGroup::actionAddLayerGroup, false);

      final MenuFactory scratchMenu = new MenuFactory("Add Scratch Layer");
      scratchMenu.setIconName("map:add");

      for (final DataType dataType : Arrays.asList(DataTypes.POINT, DataTypes.LINE_STRING,
        DataTypes.POLYGON, DataTypes.MULTI_POINT, DataTypes.MULTI_LINE_STRING,
        DataTypes.MULTI_POLYGON, DataTypes.GEOMETRY, DataTypes.GEOMETRY_COLLECTION)) {
        String iconName;
        if (dataType.equals(DataTypes.GEOMETRY_COLLECTION)) {
          iconName = "table_geometry";
        } else {
          iconName = "table_" + dataType.toString().toLowerCase();
        }
        Menus.<LayerGroup> addMenuItem(scratchMenu, "layer", "Add " + dataType + " Layer", iconName,
          layerGroup -> {
            CoordinateSystemField.selectHorizontalCoordinateSystem(
              "Select coordinate system for layer", layerGroup, coordinateSystem -> {
                final GeometryFactory geometryFactory = coordinateSystem.getGeometryFactory();
                final Layer layer = new ScratchRecordLayer(geometryFactory, dataType);
                layerGroup.addLayer(layer);
              });
          }, false);

      }
      menu.addComponentFactory("group", scratchMenu);

      Menus.<LayerGroup> addMenuItem(menu, "group", "Open File Layer...", "page:add",
        LayerGroup::actionOpenFileLayer, false);

      Menus.<LayerGroup> addMenuItem(menu, "group", "Import Project...", "map:import",
        LayerGroup::actionImportProject, false);

    });
  }

  private static Layer getLayer(LayerGroup group, final String name) {
    for (final String path : PathUtil.getPathElements(PathUtil.getPath(name))) {
      final Layer layer = getLayerByName(group, path);
      if (layer instanceof LayerGroup) {
        group = (LayerGroup)layer;
      } else {
        return null;
      }
    }

    if (group != null) {
      final String layerName = PathUtil.getName(name);

      return getLayerByName(group, layerName);
    }
    return null;
  }

  private static Layer getLayerByName(final LayerGroup group, final String layerName) {
    for (final Layer layer : group.getLayers()) {
      if (layer.getName().equals(layerName)) {

        return layer;
      }
    }
    return null;
  }

  public static LayerGroup newLayer(final Map<String, ? extends Object> config) {
    final LayerGroup layerGroup = new LayerGroup();
    final Project project = layerGroup.getProject();
    layerGroup.loadLayers(project, config);
    return layerGroup;
  }

  private boolean singleLayerVisible = false;

  private boolean deleted = false;

  private List<Layer> layers = new ArrayList<>();

  public LayerGroup() {
    this(null);
  }

  public LayerGroup(final String name) {
    super("layerGroup");
    setVisible(true);
    setName(name);
    setRenderer(new LayerGroupRenderer(this));
    setInitialized(true);
    setOpen(true);
  }

  private LayerGroup actionAddLayerGroup() {
    final String name = JOptionPane.showInputDialog(SwingUtil.getActiveWindow(),
      "Enter the name of the new Layer Group.", "Add Layer Group", JOptionPane.PLAIN_MESSAGE);
    if (Property.hasValue(name)) {
      final LayerGroup newGroup = new LayerGroup(name);
      addLayer(newGroup);
      return newGroup;
    } else {
      return null;
    }
  }

  private void actionImportProject() {
    actionImportProject("Import Project", false);
  }

  public void actionImportProject(final String title, final boolean resetProject) {
    final Project project = getProject();
    if (resetProject) {
      if (!project.saveWithPrompt()) {
        return;
      }
    }
    final JFileChooser fileChooser = SwingUtil.newFileChooser(title,
      "com.revolsys.swing.map.project", "templateDirectory");

    final FileNameExtensionFilter filter = new FileNameExtensionFilter("Project (*.rgmap)",
      "rgmap");
    fileChooser.setAcceptAllFileFilterUsed(true);
    fileChooser.addChoosableFileFilter(filter);
    fileChooser.setFileFilter(filter);
    if (OS.isMac()) {
      fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    } else {
      fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    }
    final Window window = SwingUtil.getActiveWindow();
    final int returnVal = fileChooser.showOpenDialog(window);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      final File projectDirectory = fileChooser.getSelectedFile();
      if (projectDirectory != null) {
        PreferencesUtil.setUserString("com.revolsys.swing.map.project", "templateDirectory",
          projectDirectory.getParent());
        final PathResource resource = new PathResource(projectDirectory);

        importProject(project, resource, resetProject);
      }
    }
  }

  private void actionOpenFileLayer() {
    final Window window = SwingUtil.getActiveWindow();

    final JFileChooser fileChooser = SwingUtil.newFileChooser(getClass(), "currentDirectory");
    fileChooser.setMultiSelectionEnabled(true);

    final Set<String> allImageExtensions = new TreeSet<>();
    final List<FileNameExtensionFilter> imageFileFilters = IoFactory
      .newFileFilters(allImageExtensions, GeoreferencedImageReadFactory.class);

    final Set<String> allRecordExtensions = new TreeSet<>();
    final List<FileNameExtensionFilter> recordFileFilters = IoFactory
      .newFileFilters(allRecordExtensions, RecordReaderFactory.class);

    final Set<String> allElevationModelExtensions = new TreeSet<>();
    final List<FileNameExtensionFilter> elevationModelFileFilters = IoFactory
      .newFileFilters(allElevationModelExtensions, GriddedElevationModelReaderFactory.class);

    final Set<String> allTinExtensions = new TreeSet<>();
    final List<FileNameExtensionFilter> tinFileFilters = IoFactory.newFileFilters(allTinExtensions,
      GriddedElevationModelReaderFactory.class);

    final Set<String> allExtensions = new TreeSet<>();
    allExtensions.addAll(allRecordExtensions);
    allExtensions.addAll(allImageExtensions);
    allExtensions.addAll(allElevationModelExtensions);
    allExtensions.addAll(allTinExtensions);
    final FileNameExtensionFilter allFilter = IoFactory.newFileFilter("All Supported Files",
      allExtensions);
    fileChooser.addChoosableFileFilter(allFilter);

    fileChooser.addChoosableFileFilter(
      IoFactory.newFileFilter("All Vector/Record Files", allRecordExtensions));

    fileChooser
      .addChoosableFileFilter(IoFactory.newFileFilter("All Image Files", allImageExtensions));

    fileChooser.addChoosableFileFilter(
      IoFactory.newFileFilter("All Gridded Elevation Model Files", allElevationModelExtensions));

    fileChooser.addChoosableFileFilter(
      IoFactory.newFileFilter("All Triangulated Irregular Network Files", allTinExtensions));

    for (final List<? extends FileFilter> filters : Arrays.asList(recordFileFilters,
      imageFileFilters, elevationModelFileFilters, tinFileFilters)) {
      for (final FileFilter fileFilter : filters) {
        fileChooser.addChoosableFileFilter(fileFilter);
      }
    }

    fileChooser.setAcceptAllFileFilterUsed(false);
    fileChooser.setFileFilter(allFilter);

    final int status = fileChooser.showDialog(window, "Open Files");
    if (status == JFileChooser.APPROVE_OPTION) {
      final Object menuSource = MenuFactory.getMenuSource();
      final LayerGroup layerGroup;
      if (menuSource instanceof LayerGroupTreeNode) {
        final LayerGroupTreeNode node = (LayerGroupTreeNode)menuSource;
        layerGroup = node.getGroup();
      } else if (menuSource instanceof LayerGroup) {
        layerGroup = (LayerGroup)menuSource;
      } else {
        layerGroup = Project.get();
      }
      for (final File file : fileChooser.getSelectedFiles()) {
        Invoke.background("Open file: " + FileUtil.getCanonicalPath(file),
          () -> layerGroup.openFile(file));
      }
    }
    SwingUtil.saveFileChooserDirectory(getClass(), "currentDirectory", fileChooser);
  }

  protected <V extends Layer> void addDescendants(final List<V> layers, final Class<V> layerClass) {
    addLayers(layers, layerClass);
    for (final LayerGroup layerGroup : getLayerGroups()) {
      layerGroup.addDescendants(layers, layerClass);
    }
  }

  public boolean addLayer(final int index, final Layer layer) {
    final int addIndex;
    synchronized (this.layers) {
      addIndex = addLayerDo(index, layer);
    }
    if (addIndex > -1) {
      fireIndexedPropertyChange("layers", addIndex, null, layer);
      return true;
    } else {
      return false;
    }
  }

  public boolean addLayer(final Layer layer) {
    return addLayer(-1, layer);
  }

  protected int addLayerDo(final int index, final Layer layer) {
    if (layer != null && !this.layers.contains(layer)) {
      final String name = layer.getName();
      String newName = name;
      int i = 1;
      while (hasLayerWithSameName(layer, newName)) {
        newName = name + i;
        i++;
      }
      layer.setName(newName);
      int addIndex = index;
      if (index >= 0 && index < this.layers.size()) {
        this.layers.add(index, layer);
      } else {
        addIndex = this.layers.size();
        this.layers.add(layer);
      }
      layer.setLayerGroup(this);
      initialize(layer);
      return addIndex;
    } else {
      return -1;
    }
  }

  public LayerGroup addLayerGroup(final int index, final String name) {
    if (Property.hasValue(name)) {
      synchronized (this.layers) {
        final Layer layer = getLayer(name);
        if (layer == null) {
          final LayerGroup group = new LayerGroup(name);
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

  public LayerGroup addLayerGroup(final String name) {
    return addLayerGroup(0, name);
  }

  public void addLayers(final Iterable<Layer> layers) {
    if (layers != null) {
      for (final Layer layer : layers) {
        addLayer(layer);
      }
    }
  }

  @SuppressWarnings("unchecked")
  protected <V extends Layer> void addLayers(final List<V> layers, final Class<V> layerClass) {
    for (final Layer layer : this.layers) {
      if (layerClass.isAssignableFrom(layer.getClass())) {
        layers.add((V)layer);
      }
    }
  }

  public void addPath(final List<Layer> path) {
    final LayerGroup layerGroup = getLayerGroup();
    if (layerGroup != null) {
      layerGroup.addPath(path);
    }
    path.add(this);
  }

  @Override
  public void addVisibleBbox(final BoundingBoxEditor boundingBox) {
    if (isExists() && isVisible()) {
      for (final Layer layer : this) {
        layer.addVisibleBbox(boundingBox);
      }
    }
  }

  protected <V extends Layer> void addVisibleDescendants(final List<V> layers,
    final Class<V> layerClass, final double scale) {
    if (isVisible(scale)) {
      addVisibleLayers(layers, layerClass, scale);
      for (final LayerGroup layerGroup : getLayerGroups()) {
        layerGroup.addVisibleDescendants(layers, layerClass, scale);
      }
    }
  }

  @SuppressWarnings("unchecked")
  protected <V extends Layer> void addVisibleLayers(final List<V> layers, final Class<V> layerClass,
    final double scale) {
    for (final Layer layer : this.layers) {
      if (layer.isVisible(scale)) {
        if (layerClass.isAssignableFrom(layer.getClass())) {
          layers.add((V)layer);
        }
      }
    }
  }

  public void clear() {
    for (final Layer layer : new ArrayList<>(this.layers)) {
      layer.delete();
    }
    this.layers = new ArrayList<>();

  }

  public boolean containsLayer(final Layer layer) {
    return this.layers.contains(layer);
  }

  @Override
  public void delete() {
    this.deleted = true;
    synchronized (this.layers) {
      for (final Iterator<Layer> iterator = this.layers.iterator(); iterator.hasNext();) {
        final Layer layer = iterator.next();
        iterator.remove();
        try {
          layer.delete();
        } catch (final Throwable e) {
        }
        layer.setLayerGroup(null);
        Property.removeListener(layer, this);
      }
      super.delete();
      this.layers.clear();
    }
  }

  @Override
  public BoundingBox getBoundingBox() {
    return BoundingBox.bboxNew(this, this.layers);
  }

  @Override
  public BoundingBox getBoundingBox(final boolean visibleLayersOnly) {
    if (visibleLayersOnly) {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final BoundingBoxEditor boundingBox = geometryFactory.bboxEditor();
      addVisibleBbox(boundingBox);
      return boundingBox.newBoundingBox();
    } else {
      return getBoundingBox();
    }
  }

  @Override
  public List<Layer> getChildren() {
    return getLayers();
  }

  public <V extends Layer> List<V> getDescenants(final Class<V> layerClass) {
    final List<V> layers = new ArrayList<>();
    addDescendants(layers, layerClass);
    return layers;
  }

  @Override
  public java.nio.file.Path getDirectory() {
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

  @Override
  public GeometryFactory getGeometryFactory() {
    final LayerGroup layerGroup = getLayerGroup();
    final GeometryFactory geometryFactory = super.getGeometryFactory();
    if (geometryFactory == null && layerGroup != null) {
      return layerGroup.getGeometryFactory();
    } else {
      return geometryFactory;
    }
  }

  protected java.nio.file.Path getGroupSettingsDirectory(final java.nio.file.Path directory) {
    final String name = getName();
    final String groupDirectoryName = FileUtil.getSafeFileName(name);
    final java.nio.file.Path groupDirectory = Paths.getDirectoryPath(directory, groupDirectoryName);
    return groupDirectory;
  }

  @Override
  public long getId() {
    return 0;
  }

  @SuppressWarnings("unchecked")
  public <V extends Layer> V getLayer(final int i) {
    if (i < this.layers.size()) {
      return (V)this.layers.get(i);
    } else {
      return null;
    }
  }

  public <V extends Layer> V getLayer(final PathName name) {
    return getLayer(name.getPath());
  }

  @SuppressWarnings("unchecked")
  public <V extends Layer> V getLayer(final String name) {
    return (V)getLayer(this, name);
  }

  public Layer getLayerByPath(final List<String> path) {
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

  public Layer getLayerByPath(final String layerPath) {
    final List<String> path = Lists.split(layerPath.replaceAll("^\\s*/+\\s*", ""), "(\\s*/+\\s*)+");
    return getLayerByPath(path);
  }

  public int getLayerCount() {
    return this.layers.size();
  }

  public List<LayerGroup> getLayerGroups() {
    final List<LayerGroup> layerGroups = new ArrayList<>();
    for (final Layer layer : this.layers) {
      if (layer instanceof LayerGroup) {
        final LayerGroup layerGroup = (LayerGroup)layer;
        layerGroups.add(layerGroup);
      }
    }
    return layerGroups;
  }

  public List<Layer> getLayers() {
    return new ArrayList<>(this.layers);
  }

  public <V extends Layer> List<V> getLayers(final Class<V> layerClass) {
    final List<V> layers = new ArrayList<>();
    addLayers(layers, layerClass);
    return layers;
  }

  public <V extends Layer> List<V> getLayers(final List<PathName> names) {
    final List<V> layers = new ArrayList<>();
    for (final PathName name : names) {
      final V layer = getLayer(name);
      if (layer != null) {
        layers.add(layer);
      }
    }
    return layers;
  }

  public <V extends Layer> List<V> getLayers(final PathName... names) {
    return getLayers(Arrays.asList(names));
  }

  @Override
  public BoundingBox getSelectedBoundingBox() {
    final BoundingBoxEditor boundingBox = getGeometryFactory().bboxEditor();
    if (isExists() && isVisible()) {
      for (final Layer layer : this) {
        final BoundingBox layerBoundingBox = layer.getSelectedBoundingBox();
        boundingBox.addBbox(layerBoundingBox);
      }
    }
    return boundingBox.newBoundingBox();
  }

  @Override
  protected String getSettingsFileName() {
    return "rgLayerGroup.rgobject";
  }

  public <V extends Layer> List<V> getVisibleDescendants(final Class<V> layerClass,
    final double scale) {
    final List<V> layers = new ArrayList<>();
    addVisibleDescendants(layers, layerClass, scale);
    return layers;
  }

  public boolean hasLayerWithSameName(final Layer layer, final String name) {
    for (final Layer otherLayer : this.layers) {
      if (layer != otherLayer) {
        final String layerName = otherLayer.getName();
        if (DataTypes.STRING.equals(name, layerName)) {
          return true;
        }
      }
    }
    return false;
  }

  protected <C extends Connection> void importConnections(final String label,
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

  protected void importProject(final Project importProject) {
    importProjectLayers(importProject, false);
  }

  protected boolean importProject(final Project rootProject, final Resource resource,
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

  protected void importProjectLayers(final Project importProject, final boolean createGroup) {

    final List<Layer> importLayers = importProject.getLayers();
    if (!importLayers.isEmpty()) {
      LayerGroup targetGroup;
      if (createGroup) {
        final String projectName = importProject.getName();
        targetGroup = new LayerGroup(projectName);
        addLayer(targetGroup);
      } else {
        targetGroup = this;
      }
      targetGroup.addLayers(importLayers);
    }
  }

  public int indexOf(final Layer layer) {
    return this.layers.indexOf(layer);
  }

  public void initialize(final Layer layer) {
    if (getProject() != null) {
      LayerInitializer.initialize(layer);
      if (layer instanceof LayerGroup) {
        final LayerGroup layerGroup = (LayerGroup)layer;
        for (final Layer child : layerGroup) {
          initialize(child);
        }
      }
    }
  }

  @Override
  public boolean isDeleted() {
    return this.deleted;
  }

  @Override
  public boolean isDescendant(final Layer layer) {
    if (layer == this) {
      return true;
    } else {
      for (final Layer childLayer : this.layers) {
        if (childLayer.isDescendant(layer)) {
          return true;
        }
      }
      return false;
    }
  }

  public boolean isEmpty() {
    return this.layers.isEmpty();
  }

  @Override
  public boolean isHasSelectedRecords() {
    for (final Layer layer : getLayers()) {
      if (layer.isHasSelectedRecords()) {
        return true;
      }
    }
    return false;
  }

  public boolean isHasVisibleLayer() {
    for (final Layer layer : getLayers()) {
      if (layer.isVisible()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isQueryable() {
    return false;
  }

  @Override
  public boolean isQuerySupported() {
    return false;
  }

  public boolean isSingleLayerVisible() {
    return this.singleLayerVisible;
  }

  @Override
  public boolean isZoomToLayerEnabled() {
    if (!getBoundingBox().isEmpty()) {
      return true;
    }
    return false;
  }

  @Override
  public Iterator<Layer> iterator() {
    return getLayers().iterator();
  }

  protected Layer loadLayer(final File file) {
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
  protected void loadLayers(final Project rootProject, final Map<String, ? extends Object> config) {
    final List<String> layerFiles = (List<String>)config.remove("layers");
    setProperties(config);
    if (layerFiles != null) {
      for (String fileName : layerFiles) {
        if (fileName.endsWith(".rgmap")) {
          final Resource childResource = Resource.getBaseResource(fileName);
          if (!importProject(rootProject, childResource, false)) {
            Logs.error(LayerGroup.class, "Project not found: " + childResource);
          }
        } else {
          if (!fileName.endsWith("rgobject")) {
            fileName += "/rgLayerGroup.rgobject";
          }
          final Resource childResource = Resource.getBaseResource(fileName);
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
      }
    }
  }

  public int openFile(final Class<? extends IoFactory> factoryClass, int index, final URL url) {
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
      layer.setProperty("showTableView", isShowNewLayerTableView());
      if (index == -1) {
        addLayer(layer);
      } else {
        addLayer(index++, layer);
      }
    }
    return index;
  }

  public int openFile(final Class<? extends IoFactory> factoryClass, final URL url) {
    return openFile(factoryClass, -1, url);
  }

  public void openFile(final File file) {
    final String extension = FileUtil.getFileNameExtension(file);
    if ("rgobject".equals(extension)) {
      loadLayer(file);
    } else {
      final URL url = FileUtil.toUrl(file);
      openFile(url);
    }
  }

  public int openFile(final int index, final File file) {
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

  public int openFile(int index, final URL url) {
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
      layer.setProperty("showTableView", isShowNewLayerTableView());
      if (index == -1) {
        addLayer(layer);
      } else {
        addLayer(index++, layer);
      }
    }
    return index;
  }

  public void openFile(final URL url) {
    openFile(-1, url);
  }

  public void openFiles(int index, final List<File> files) {
    for (final File file : files) {
      index = openFile(index, file);
    }
  }

  public void openFiles(final List<File> files) {
    openFiles(-1, files);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    super.propertyChange(event);
    final Object source = event.getSource();
    final String propertyName = event.getPropertyName();
    if ("visible".equals(propertyName)) {
      if (isSingleLayerVisible()) {
        final boolean visible = (Boolean)event.getNewValue();
        if (visible) {
          for (final Layer layer : getLayers()) {
            if (layer != source) {
              layer.setVisible(false);
            }
          }
        }
      }
    }
  }

  @Override
  protected void refreshAllDo() {
    for (final Layer layer : this.layers) {
      layer.refreshAll();
    }
  }

  @Override
  protected void refreshDo() {
  }

  public void refreshLayer(final String path) {
    final Layer layer = getLayerByPath(path);
    if (layer != null) {
      layer.refresh();
    }
  }

  public Layer removeLayer(final int index) {
    synchronized (this.layers) {
      final Layer layer = this.layers.remove(index);
      fireIndexedPropertyChange("layers", index, layer, null);
      Property.removeListener(layer, this);
      if (layer.getLayerGroup() == this) {
        layer.setLayerGroup(null);
      }
      return layer;
    }
  }

  public boolean removeLayer(final Object o) {
    synchronized (this.layers) {
      final int index = this.layers.indexOf(o);
      if (index < 0) {
        return false;
      } else {
        removeLayer(index);
        return true;
      }
    }
  }

  public Layer removeLayer(final String layerName) {
    final Layer layer = getLayer(layerName);
    if (layer == null) {
      return null;
    } else {
      removeLayer(layer);
      return layer;
    }
  }

  public boolean removeLayer(final String layerName, final boolean delete) {
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

  public void reset() {
    clear();
  }

  public boolean saveAllSettings(final java.nio.file.Path directory) {
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

  @Override
  public boolean saveChanges() {
    boolean saved = super.saveChanges();
    for (final Layer layer : this) {
      saved &= layer.saveChanges();
    }
    return saved;
  }

  @Override
  protected boolean saveSettingsDo(final java.nio.file.Path directory) {
    final java.nio.file.Path groupDirectory = getGroupSettingsDirectory(directory);
    return super.saveSettingsDo(groupDirectory);
  }

  public void setLayers(final List<Layer> layers) {
    final Object oldValue;
    synchronized (this.layers) {
      oldValue = new ArrayList<>(this.layers);
      this.layers.clear();
      int index = 0;
      for (final Object layer : layers) {
        if (layer instanceof Layer) {
          addLayerDo(index++, (Layer)layer);
        }
      }
    }
    firePropertyChange("layer", oldValue, layers);
  }

  public void setSingleLayerVisible(final boolean singleLayerVisible) {
    this.singleLayerVisible = singleLayerVisible;
  }

  public void sort() {
    synchronized (this.layers) {
      Collections.sort(this.layers);
    }
  }

  @Override
  public MapEx toMap() {
    final MapEx map = super.toMap();
    map.remove("querySupported");
    map.remove("queryable");
    map.remove("editable");
    map.remove("selectable");
    map.remove("selectSupported");
    map.put("singleLayerVisible", isSingleLayerVisible());

    final List<String> layerFiles = new ArrayList<>();
    final List<Layer> layers = getLayers();
    for (final Layer layer : layers) {
      final String layerName = layer.getName();
      String layerFileName = FileUtil.getSafeFileName(layerName);
      if (layer instanceof LayerGroup) {
      } else {
        layerFileName += ".rgobject";
      }
      layerFiles.add(layerFileName);

    }
    addToMap(map, "layers", layerFiles);
    return map;
  }
}
