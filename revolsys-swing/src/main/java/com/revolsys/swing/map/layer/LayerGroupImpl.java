package com.revolsys.swing.map.layer;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.jeometry.common.data.type.DataType;

import com.revolsys.collection.list.Lists;
import com.revolsys.elevation.gridded.GriddedElevationModelReaderFactory;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.editor.BoundingBoxEditor;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactory;
import com.revolsys.io.filter.FileNameExtensionFilter;
import com.revolsys.raster.GeoreferencedImageReadFactory;
import com.revolsys.record.io.RecordReaderFactory;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.spring.resource.PathResource;
import com.revolsys.swing.Dialogs;
import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.map.component.GeometryFactoryField;
import com.revolsys.swing.map.layer.record.ScratchRecordLayer;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.tree.node.file.PathTreeNode;
import com.revolsys.swing.tree.node.layer.LayerGroupTreeNode;
import com.revolsys.util.Cancellable;
import com.revolsys.util.OS;
import com.revolsys.util.PreferencesUtil;
import com.revolsys.util.Property;
import com.revolsys.util.UrlUtil;

public class LayerGroupImpl extends AbstractLayer implements LayerGroup {
  static {
    MenuFactory.addMenuInitializer(LayerGroupImpl.class, menu -> {
      menu.addGroup(0, "group");
      menu.<LayerGroupImpl> addMenuItem("group", "Add Group",
        Icons.getIconWithBadge(PathTreeNode.getIconFolder(), "add"),
        LayerGroupImpl::actionAddLayerGroup, false);

      final MenuFactory scratchMenu = new MenuFactory("Add Scratch Layer");
      scratchMenu.setIconName("map:add");

      for (final DataType dataType : Arrays.asList(GeometryDataTypes.POINT,
        GeometryDataTypes.LINE_STRING, GeometryDataTypes.POLYGON, GeometryDataTypes.MULTI_POINT,
        GeometryDataTypes.MULTI_LINE_STRING, GeometryDataTypes.MULTI_POLYGON,
        GeometryDataTypes.GEOMETRY, GeometryDataTypes.GEOMETRY_COLLECTION)) {
        String iconName;
        if (dataType.equals(GeometryDataTypes.GEOMETRY_COLLECTION)) {
          iconName = "table_geometry";
        } else {
          iconName = "table_" + dataType.toString().toLowerCase();
        }
        scratchMenu.<LayerGroup> addMenuItem("layer", "Add " + dataType + " Layer", iconName,
          layerGroup -> {
            GeometryFactoryField.selectGeometryFactory("Select coordinate system for layer",
              layerGroup, geometryFactory -> {
                final Layer layer = new ScratchRecordLayer(geometryFactory, dataType);
                layerGroup.addLayer(layer);
              });
          }, false);

      }
      menu.addComponentFactory("group", scratchMenu);

      menu.<LayerGroupImpl> addMenuItem("group", "Open File Layer...", "page:add",
        LayerGroupImpl::actionOpenFileLayer, false);

      menu.<LayerGroupImpl> addMenuItem("group", "Open URL Layer...", "page:add",
        LayerGroupImpl::actionOpenUrlLayer, false);

      menu.<LayerGroupImpl> addMenuItem("group", "Import Project...", "map:import",
        LayerGroupImpl::actionImportProject, false);

    });
  }

  private static final Layer[] EMPTY_LAYER_ARRAY = new Layer[0];

  private static final List<Layer> EMPTY_LAYER_LIST = Collections.emptyList();

  public static LayerGroup newLayer(final Map<String, ? extends Object> config) {
    final LayerGroup layerGroup = new LayerGroupImpl();
    final Project project = layerGroup.getProject();
    layerGroup.loadLayers(project, config);
    return layerGroup;
  }

  private boolean singleLayerVisible = false;

  private boolean deleted = false;

  private Layer[] layers = EMPTY_LAYER_ARRAY;

  public LayerGroupImpl() {
    this(null);
  }

  public LayerGroupImpl(final String name) {
    super("layerGroup");
    setVisible(true);
    setName(name);
    setRenderer(new LayerGroupRenderer(this));
    setInitialized(true);
    setOpen(true);
  }

  private LayerGroup actionAddLayerGroup() {
    final String name = Dialogs.showInputDialog("Enter the name of the new Layer Group.",
      "Add Layer Group", JOptionPane.PLAIN_MESSAGE);
    if (Property.hasValue(name)) {
      final LayerGroup newGroup = new LayerGroupImpl(name);
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
    final int returnVal = Dialogs.showOpenDialog(fileChooser);
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

    final int status = Dialogs.showDialog(fileChooser, "Open Files");
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

  private void actionOpenUrlLayer() {
    final String urlString = Dialogs.showInputDialog("URL");

    if (Property.hasValue(urlString)) {
      final URL url = UrlUtil.getUrl(urlString);
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
      Invoke.background("Open: " + url, () -> layerGroup.openFile(url));
    }
  }

  @Override
  public int addLayerDo(final int index, final Layer layer) {
    if (layer != null && !containsLayer(layer)) {
      final String name = layer.getName();
      String newName = name;
      int i = 1;
      while (hasLayerWithSameName(layer, newName)) {
        newName = name + i;
        i++;
      }
      layer.setName(newName);
      int addIndex = index;
      final Layer[] oldLayers = this.layers;
      final Layer[] newLayers = new Layer[oldLayers.length + 1];
      if (addIndex == 0) {
        System.arraycopy(oldLayers, 0, newLayers, 1, oldLayers.length);
      } else if (addIndex > 0 && addIndex < oldLayers.length) {
        System.arraycopy(oldLayers, 0, newLayers, 0, index);
        System.arraycopy(oldLayers, addIndex, newLayers, index + 1, oldLayers.length - index);
      } else {
        addIndex = oldLayers.length;
        System.arraycopy(oldLayers, 0, newLayers, 0, oldLayers.length);
      }
      newLayers[addIndex] = layer;
      this.layers = newLayers;

      layer.setLayerGroup(this);
      initialize(layer);
      return addIndex;
    } else {
      return -1;
    }
  }

  @Override
  protected void addSelectedBoundingBoxDo(final BoundingBoxEditor boundingBox) {
    for (final Layer layer : this) {
      layer.addSelectedBoundingBox(boundingBox);
    }
  }

  @Override
  public void clear() {
    final Layer[] layers = this.layers;
    this.layers = EMPTY_LAYER_ARRAY;
    for (final Layer layer : layers) {
      try {
        layer.delete();
      } catch (final Throwable e) {
      }
      layer.setLayerGroup(null);
      Property.removeListener(layer, this);
    }
  }

  @Override
  public boolean containsLayer(final Layer layer) {
    for (final Layer layer1 : this.layers) {
      if (layer1 == layer) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void delete() {
    this.deleted = true;
    synchronized (getSync()) {
      clear();
      super.delete();
    }
  }

  @Override
  public void fireIndexedPropertyChange(final String propertyName, final int index,
    final Object oldValue, final Object newValue) {
    super.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
  }

  @Override
  public void forEach(final Cancellable cancellable, final Consumer<? super Layer> action) {
    if (cancellable.isCancelled()) {
      return;
    }
    for (final Layer layer : this.layers) {
      action.accept(layer);
    }
  }

  @Override
  public void forEach(final Consumer<? super Layer> action) {
    for (final Layer layer : this.layers) {
      action.accept(layer);
    }
  }

  @Override
  public void forEachReverse(final Cancellable cancellable, final Consumer<? super Layer> action) {
    final Layer[] layers = this.layers;
    for (int i = layers.length - 1; i >= 0; i--) {
      if (cancellable.isCancelled()) {
        return;
      }
      final Layer layer = layers[i];
      action.accept(layer);
    }
  }

  @Override
  public void forEachReverse(final Consumer<? super Layer> action) {
    final Layer[] layers = this.layers;
    for (int i = layers.length - 1; i >= 0; i--) {
      final Layer layer = layers[i];
      action.accept(layer);
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

  @Override
  public long getId() {
    return 0;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V extends Layer> V getLayer(final int i) {
    if (i < this.layers.length) {
      return (V)this.layers[i];
    } else {
      return null;
    }
  }

  @Override
  public Layer getLayerByName(final String layerName) {
    for (final Layer layer : this.layers) {
      if (layer.getName().equals(layerName)) {

        return layer;
      }
    }
    return null;
  }

  @Override
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

  @Override
  public Layer getLayerByPath(final String layerPath) {
    final List<String> path = Lists.split(layerPath.replaceAll("^\\s*/+\\s*", ""), "(\\s*/+\\s*)+");
    return getLayerByPath(path);
  }

  @Override
  public int getLayerCount() {
    return this.layers.length;
  }

  @Override
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

  @Override
  public List<Layer> getLayers() {
    if (this.layers.length == 0) {
      return EMPTY_LAYER_LIST;
    } else {
      return Arrays.asList(this.layers);
    }
  }

  @Override
  protected String getSettingsFileName() {
    return "rgLayerGroup.rgobject";
  }

  @Override
  public Object getSync() {
    return this.layers;
  }

  @Override
  public int indexOf(final Layer layer) {
    final Layer[] layers = this.layers;
    for (int i = 0; i < layers.length; i++) {
      if (layers[i] == layer) {
        return i;
      }
    }
    return -1;
  }

  public void initialize(final Layer layer) {
    if (getProject() != null) {
      LayerInitializer.initialize(layer);
      if (layer instanceof LayerGroup) {
        final LayerGroup layerGroup = (LayerGroup)layer;
        layerGroup.forEach(this::initialize);
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
    return this.layers.length == 0;
  }

  @Override
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

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    super.propertyChange(event);
    final Object source = event.getSource();
    final String propertyName = event.getPropertyName();
    if ("visible".equals(propertyName)) {
      if (isSingleLayerVisible()) {
        final boolean visible = (Boolean)event.getNewValue();
        if (visible) {
          forEach((layer) -> {
            if (layer != source) {
              layer.setVisible(false);
            }
          });
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

  @Override
  public Layer removeLayer(final int index) {
    final Layer[] layers = this.layers;
    synchronized (layers) {
      if (index >= 0 && index < layers.length) {
        final Layer layer = layers[index];
        final Layer[] newLayers = new Layer[layers.length - 1];
        System.arraycopy(layers, 0, newLayers, 0, index);
        System.arraycopy(layers, index + 1, newLayers, index, layers.length - index - 1);
        this.layers = newLayers;
        fireIndexedPropertyChange("layers", index, layer, null);
        Property.removeListener(layer, this);
        if (layer.getLayerGroup() == this) {
          layer.setLayerGroup(null);
        }
        return layer;
      }
    }
    return null;
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
    synchronized (getSync()) {
      oldValue = Arrays.asList(layers);
      this.layers = EMPTY_LAYER_ARRAY;
      int index = 0;
      for (final Object layer : layers) {
        if (layer instanceof Layer) {
          addLayerDo(index++, (Layer)layer);
        }
      }
    }
    final Object newValue = Arrays.asList(layers);
    firePropertyChange("layer", oldValue, newValue);
  }

  public void setSingleLayerVisible(final boolean singleLayerVisible) {
    this.singleLayerVisible = singleLayerVisible;
  }

  public void sort() {
    synchronized (getSync()) {
      Arrays.sort(this.layers);
    }
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = super.toMap();
    map.remove("querySupported");
    map.remove("queryable");
    map.remove("editable");
    map.remove("selectable");
    map.remove("selectSupported");
    map.put("singleLayerVisible", isSingleLayerVisible());

    final List<String> layerFiles = new ArrayList<>();
    forEach((layer) -> {
      final String layerName = layer.getName();
      String layerFileName = FileUtil.getSafeFileName(layerName);
      if (layer instanceof LayerGroup) {
      } else {
        layerFileName += ".rgobject";
      }
      layerFiles.add(layerFileName);
    });
    addToMap(map, "layers", layerFiles);
    return map;
  }

}
