package com.revolsys.swing.map.layer;

import java.awt.Component;
import java.awt.Font;
import java.awt.TextArea;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.ScrollableSizeHint;
import org.jdesktop.swingx.VerticalLayout;

import com.revolsys.beans.KeyedPropertyChangeEvent;
import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.collection.EmptyReference;
import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.MapSerializerMap;
import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.esri.EsriCoordinateSystems;
import com.revolsys.geometry.cs.esri.EsriCsWktWriter;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.FileUtil;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.logging.Logs;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.swing.Borders;
import com.revolsys.swing.Icons;
import com.revolsys.swing.Panels;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.TabbedValuePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.listener.BeanPropertyListener;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.ProjectFrame;
import com.revolsys.swing.map.ProjectFramePanel;
import com.revolsys.swing.map.layer.menu.TreeItemScaleMenu;
import com.revolsys.swing.map.layer.record.style.panel.LayerStylePanel;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.menu.Menus;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.preferences.PreferencesDialog;
import com.revolsys.util.Booleans;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.OS;
import com.revolsys.util.Property;
import com.revolsys.value.ThreadBooleanValue;

public abstract class AbstractLayer extends BaseObjectWithProperties
  implements Layer, PropertyChangeListener, PropertyChangeSupportProxy, ProjectFramePanel {
  public static final Icon ICON_LAYER = Icons.getIcon("map");

  private static final AtomicLong ID_GEN = new AtomicLong();

  public static final String PLUGIN_TABLE_VIEW = "tableView";

  public static final String PREFERENCE_NEW_LAYERS_SHOW_TABLE_VIEW = "newLayersShowTableView";

  public static final String PREFERENCE_NEW_LAYERS_VISIBLE = "newLayersVisible";

  public static final String PREFERENCE_PATH = "/com/revolsys/gis/layer";

  static {
    final MenuFactory menu = MenuFactory.getMenu(AbstractLayer.class);

    Menus.addMenuItem(menu, "zoom", "Zoom to Layer", "magnifier",
      AbstractLayer::isZoomToLayerEnabled, AbstractLayer::zoomToLayer, true);

    final Predicate<AbstractLayer> hasGeometry = AbstractLayer::isHasGeometry;
    menu.addComponentFactory("scale", new TreeItemScaleMenu<>(true, hasGeometry,
      AbstractLayer::getMinimumScale, AbstractLayer::setMinimumScale));
    menu.addComponentFactory("scale", new TreeItemScaleMenu<>(false, hasGeometry,
      AbstractLayer::getMaximumScale, AbstractLayer::setMaximumScale));

    final Predicate<AbstractLayer> exists = AbstractLayer::isExists;

    Menus.<AbstractLayer> addMenuItem(menu, "refresh", "Refresh", "arrow_refresh", exists,
      AbstractLayer::refreshAll, true);

    Menus.<AbstractLayer> addMenuItem(menu, "layer", "Delete", "delete",
      AbstractLayer::deleteWithConfirm, false);

    Menus.<AbstractLayer> addMenuItem(menu, "layer", "Layer Properties", "information", exists,
      AbstractLayer::showProperties, false);

    final PreferencesDialog preferencesDialog = PreferencesDialog.get();
    preferencesDialog.addPreference("Layers", "com.revolsys.gis", PREFERENCE_PATH,
      PREFERENCE_NEW_LAYERS_VISIBLE, DataTypes.BOOLEAN, false);
    preferencesDialog.addPreference("Layers", "com.revolsys.gis", PREFERENCE_PATH,
      PREFERENCE_NEW_LAYERS_SHOW_TABLE_VIEW, DataTypes.BOOLEAN, false);
  }

  private boolean open = false;

  private PropertyChangeListener beanPropertyListener = new BeanPropertyListener(this);

  private BoundingBox boundingBox = BoundingBox.empty();

  private boolean editable = false;

  private ThreadBooleanValue eventsEnabled = new ThreadBooleanValue(true);

  private boolean exists = true;

  private GeometryFactory geometryFactory;

  private Icon icon = ICON_LAYER;

  private long id = ID_GEN.incrementAndGet();

  private boolean initialized;

  private Reference<LayerGroup> layerGroup;

  private long maximumScale = 0;

  private long minimumScale = Long.MAX_VALUE;

  private String name;

  private Map<String, Map<String, Object>> pluginConfigByName = new TreeMap<>();

  private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

  private boolean queryable = true;

  private boolean querySupported = true;

  private boolean readOnly = false;

  private LayerRenderer<AbstractLayer> renderer;

  private boolean selectable = true;

  private boolean selectSupported = true;

  private Object sync = new Object();

  private String type;

  private boolean visible = OS.getPreferenceBoolean("com.revolsys.gis", PREFERENCE_PATH,
    PREFERENCE_NEW_LAYERS_VISIBLE, false);

  protected AbstractLayer(final String type) {
    this.type = type;
  }

  @Override
  public void activatePanelComponent(final Component component, final Map<String, Object> config) {

  }

  protected void addParent(final List<Layer> path) {
    final LayerGroup parent = getLayerGroup();
    if (parent != null) {
      path.add(0, parent);
      parent.addParent(path);
    }
  }

  public int addRenderer(final LayerRenderer<?> child) {
    return addRenderer(child, 0);
  }

  public int addRenderer(final LayerRenderer<?> child, final int index) {
    setRenderer(child);
    return 0;
  }

  public boolean canSaveSettings(final Path directory) {
    if (directory != null) {
      if (!Files.exists(directory)) {
        Logs.error(this,
          "Unable to save layer " + getPath() + " directory does not exist " + directory);
      } else if (!Files.isDirectory(directory)) {
        Logs.error(this,
          "Unable to save layer " + getPath() + " file is not a directory " + directory);
      } else if (!Files.isWritable(directory)) {
        Logs.error(this,
          "Unable to save layer " + getPath() + " directory is not writable " + directory);
      } else {
        return true;
      }
    }
    return false;
  }

  protected boolean checkShowProperties() {
    boolean show = true;
    synchronized (this) {
      if (Booleans.getBoolean(getProperty("INTERNAL_PROPERTIES_VISIBLE"))) {
        show = false;
      } else {
        setProperty("INTERNAL_PROPERTIES_VISIBLE", true);
      }
    }
    return show;
  }

  public void clearPluginConfig(final String pluginName) {
    this.pluginConfigByName.remove(pluginName);
  }

  @Override
  public AbstractLayer clone() {
    final AbstractLayer clone = (AbstractLayer)super.clone();
    clone.beanPropertyListener = new BeanPropertyListener(clone);
    clone.eventsEnabled = new ThreadBooleanValue(true);
    clone.id = this.id = ID_GEN.incrementAndGet();
    clone.initialized = false;
    clone.layerGroup = null;
    clone.propertyChangeSupport = new PropertyChangeSupport(clone);
    if (clone.renderer != null) {
      clone.renderer = clone.renderer.clone();
    }
    clone.sync = new Object();
    return clone;
  }

  @Override
  public int compareTo(final Layer layer) {
    return getName().compareTo(layer.getName());
  }

  @Override
  public void delete() {
    setExists(false);
    this.beanPropertyListener = null;
    final ProjectFrame projectFrame = ProjectFrame.get(this);
    if (projectFrame != null) {
      projectFrame.removeBottomTab(this);
    }
    firePropertyChange("deleted", false, true);
    final LayerGroup layerGroup = getLayerGroup();
    if (layerGroup != null) {
      layerGroup.removeLayer(this);
      this.layerGroup = new EmptyReference<>();
    }
    this.eventsEnabled.closeable(false);
    final PropertyChangeSupport propertyChangeSupport = this.propertyChangeSupport;
    if (propertyChangeSupport != null) {
      Property.removeAllListeners(propertyChangeSupport);
      this.propertyChangeSupport = null;
    }
    if (this.renderer != null) {
      this.renderer.setLayer(null);
    }
  }

  @Override
  public void deletePanelComponent(final Component component) {
    clearPluginConfig(AbstractLayer.PLUGIN_TABLE_VIEW);
  }

  public void deleteWithConfirm() {
    final int confirm = JOptionPane.showConfirmDialog(getMapPanel(),
      "Delete the layer and any child layers? This action cannot be undone.", "Delete Layer",
      JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
    if (confirm == JOptionPane.OK_OPTION) {
      delete();
    }
  }

  public BaseCloseable eventsDisabled() {
    return this.eventsEnabled.closeable(false);
  }

  public BaseCloseable eventsEnabled() {
    return this.eventsEnabled.closeable(true);
  }

  protected void fireGeometryFactoryChanged(final GeometryFactory oldGeometryFactory,
    final GeometryFactory newGeometryFactory) {
    firePropertyChange("geometryFactory", oldGeometryFactory, this.geometryFactory);
    final int coordinateSystemId = newGeometryFactory.getCoordinateSystemId();
    firePropertyChange("srid", -2, coordinateSystemId);
  }

  protected void fireIndexedPropertyChange(final String propertyName, final int index,
    final Object oldValue, final Object newValue) {
    if (this.propertyChangeSupport != null) {
      this.propertyChangeSupport.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
    }
  }

  @Override
  public void firePropertyChange(final String propertyName, final Object oldValue,
    final Object newValue) {
    if (this.propertyChangeSupport != null && this.eventsEnabled.isTrue()) {
      this.propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
  }

  public PropertyChangeListener getBeanPropertyListener() {
    return this.beanPropertyListener;
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  @Override
  public BoundingBox getBoundingBox(final boolean visibleLayersOnly) {
    if (this.visible || !visibleLayersOnly) {
      return getBoundingBox();
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      return geometryFactory.newBoundingBoxEmpty();
    }
  }

  @Override
  public Collection<Class<?>> getChildClasses() {
    return Collections.emptySet();
  }

  public Path getDirectory() {
    final LayerGroup layerGroup = getLayerGroup();
    if (layerGroup == null) {
      return null;
    } else {
      return layerGroup.getDirectory();
    }
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  public Icon getIcon() {
    return this.icon;
  }

  @Override
  public long getId() {
    return this.id;
  }

  @Override
  public LayerGroup getLayerGroup() {
    if (this.layerGroup == null) {
      return null;
    } else {
      return this.layerGroup.get();
    }
  }

  @Override
  public long getMaximumScale() {
    return this.maximumScale;
  }

  @Override
  public long getMinimumScale() {
    return this.minimumScale;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends LayerGroup> V getParent() {
    return (V)getLayerGroup();
  }

  @Override
  public String getPath() {
    final LayerGroup layerGroup = getLayerGroup();
    if (layerGroup == null) {
      return "/";
    } else {
      final String path = layerGroup.getPath();
      if ("/".equals(path)) {
        return "/" + getName();
      } else {
        return path + "/" + getName();
      }
    }
  }

  @Override
  public List<Layer> getPathList() {
    final List<Layer> path = new ArrayList<>();
    path.add(this);
    addParent(path);
    return path;
  }

  public Map<String, Object> getPluginConfig(final String pluginName) {
    final Map<String, Object> pluginConfig = this.pluginConfigByName.get(pluginName);
    if (pluginConfig == null) {
      return Collections.emptyMap();
    } else {
      return new LinkedHashMap<>(pluginConfig);
    }
  }

  @Override
  public Project getProject() {
    final LayerGroup layerGroup = getLayerGroup();
    if (layerGroup == null) {
      return null;
    } else {
      return layerGroup.getProject();
    }
  }

  @Override
  public PropertyChangeSupport getPropertyChangeSupport() {
    return this.propertyChangeSupport;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <L extends LayerRenderer<? extends Layer>> L getRenderer() {
    return (L)this.renderer;
  }

  @Override
  public BoundingBox getSelectedBoundingBox() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (geometryFactory == null) {
      return BoundingBox.empty();
    } else {
      return geometryFactory.newBoundingBoxEmpty();
    }
  }

  protected String getSettingsFileName() {
    final String name = getName();
    return FileUtil.getSafeFileName(name) + ".rgobject";
  }

  public Object getSync() {
    return this.sync;
  }

  @Override
  public String getType() {
    return this.type;
  }

  @Override
  public final synchronized void initialize() {
    if (!isInitialized()) {
      try {
        final boolean exists;
        try (
          BaseCloseable eventsDisabled = eventsDisabled()) {
          exists = initializeDo();
        }
        setExists(exists);
        if (exists && Property.getBoolean(this, "showTableView")) {
          Invoke.later(this::showTableView);
        }
      } catch (final Throwable e) {
        Logs.error(this, "Unable to initialize layer: " + getPath(), e);
        setExists(false);
      } finally {
        setInitialized(true);
      }
    }
  }

  protected boolean initializeDo() {
    return true;
  }

  @Override
  public boolean isClonable() {
    return false;
  }

  @Override
  public boolean isDeleted() {
    final Project project = getProject();
    if (project == null) {
      return false;
    } else {
      return project.isDeleted();
    }
  }

  @Override
  public boolean isEditable() {
    return this.editable;
  }

  @Override
  public boolean isEditable(final double scale) {
    return isVisible(scale) && isEditable();
  }

  public boolean isEventsEnabled() {
    return this.eventsEnabled.isTrue();
  }

  @Override
  public boolean isExists() {
    return isInitialized() && this.exists;
  }

  @Override
  public boolean isHasSelectedRecords() {
    return false;
  }

  @Override
  public boolean isInitialized() {
    return this.initialized;
  }

  @Override
  public boolean isOpen() {
    return this.open;
  }

  @Override
  public boolean isQueryable() {
    return this.querySupported && this.queryable;
  }

  @Override
  public boolean isQuerySupported() {
    return isExists() && this.querySupported;
  }

  @Override
  public boolean isReadOnly() {
    return !isExists() || this.readOnly;
  }

  @Override
  public boolean isSelectable() {
    return isExists() && isVisible() && (isSelectSupported() && this.selectable || isEditable());
  }

  @Override
  public boolean isSelectable(final double scale) {
    return isSelectable() && isVisible(scale);
  }

  @Override
  public boolean isSelectSupported() {
    return this.selectSupported;
  }

  @Override
  public boolean isVisible() {
    final LayerGroup parent = getParent();
    return this.visible && (parent == null || parent.isVisible());
  }

  @Override
  public boolean isVisible(final double scale) {
    final LayerGroup parent = getParent();
    if (isExists() && isVisible() && (parent == null || parent.isVisible(scale))) {
      final long longScale = (long)scale;
      final long minimumScale = getMinimumScale();
      final long maximumScale = getMaximumScale();
      if (minimumScale >= longScale && longScale >= maximumScale) {
        return true;
      }
    }
    return false;
  }

  public boolean isZoomToLayerEnabled() {
    if (isHasGeometry()) {
      if (!getBoundingBox().isEmpty()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Component newPanelComponent(final Map<String, Object> config) {
    return newTableViewComponent(config);
  }

  @Override
  public TabbedValuePanel newPropertiesPanel() {
    final TabbedValuePanel tabPanel = new TabbedValuePanel("Layer " + this + " Properties", this);
    newPropertiesTabGeneral(tabPanel);
    newPropertiesTabCoordinateSystem(tabPanel);
    return tabPanel;
  }

  protected JPanel newPropertiesTabCoordinateSystem(final TabbedValuePanel tabPanel) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (geometryFactory != null) {
      final JPanel panel = new JPanel(new VerticalLayout(5));
      tabPanel.addTab("Spatial", "world", panel);

      final JPanel extentPanel = Panels.titledTransparent("Extent");
      final BoundingBox boundingBox = getBoundingBox();
      if (boundingBox == null || boundingBox.isEmpty()) {
        extentPanel.add(new JLabel("Unknown"));

      } else {
        final JLabel extentLabel = new JLabel("<html><table cellspacing=\"3\" style=\"margin:0px\">"
          + "<tr><td>&nbsp;</td><th style=\"text-align:left\">Top:</th><td style=\"text-align:right\">"
          + DataTypes.toString(boundingBox.getMaximum(1)) + "</td><td>&nbsp;</td></tr><tr>"
          + "<td><b>Left</b>: " + DataTypes.toString(boundingBox.getMinimum(0))
          + "</td><td>&nbsp;</td><td>&nbsp;</td>" + "<td><b>Right</b>: "
          + DataTypes.toString(boundingBox.getMaximum(0)) + "</td></tr>"
          + "<tr><td>&nbsp;</td><th>Bottom:</th><td style=\"text-align:right\">"
          + DataTypes.toString(boundingBox.getMinimum(1)) + "</td><td>&nbsp;</td></tr><tr>"
          + "</tr></table></html>");
        extentLabel.setFont(SwingUtil.FONT);
        extentPanel.add(extentLabel);

      }
      GroupLayouts.makeColumns(extentPanel, 1, true);
      panel.add(extentPanel);

      final JPanel coordinateSystemPanel = Panels.titledTransparent("Coordinate System");
      final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
      if (coordinateSystem == null) {
        coordinateSystemPanel.add(new JLabel("Unknown"));
      } else {
        final int axisCount = geometryFactory.getAxisCount();
        SwingUtil.addLabelledReadOnlyTextField(coordinateSystemPanel, "ID",
          coordinateSystem.getCoordinateSystemId(), 10);
        SwingUtil.addLabelledReadOnlyTextField(coordinateSystemPanel, "axisCount", axisCount, 10);

        final double scaleXY = geometryFactory.getScaleXy();
        if (scaleXY > 0) {
          SwingUtil.addLabelledReadOnlyTextField(coordinateSystemPanel, "scaleXy", scaleXY, 10);
        } else {
          SwingUtil.addLabelledReadOnlyTextField(coordinateSystemPanel, "scaleXy", "Floating", 10);
        }

        if (axisCount > 2) {
          final double scaleZ = geometryFactory.getScaleZ();
          if (scaleZ > 0) {
            SwingUtil.addLabelledReadOnlyTextField(coordinateSystemPanel, "scaleZ", scaleZ, 10);
          } else {
            SwingUtil.addLabelledReadOnlyTextField(coordinateSystemPanel, "scaleZ", "Floating", 10);
          }
        }

        final CoordinateSystem esriCoordinateSystem = EsriCoordinateSystems
          .getCoordinateSystem(coordinateSystem);
        SwingUtil.addLabel(coordinateSystemPanel, "ESRI WKT");
        final TextArea wktTextArea = new TextArea(EsriCsWktWriter.toString(esriCoordinateSystem),
          10, 80);
        wktTextArea.setEditable(false);
        wktTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        coordinateSystemPanel.add(wktTextArea);

        GroupLayouts.makeColumns(coordinateSystemPanel, 2, true);
      }
      panel.add(coordinateSystemPanel);

      return panel;
    }
    return null;
  }

  protected BasePanel newPropertiesTabGeneral(final TabbedValuePanel tabPanel) {
    final BasePanel generalPanel = new BasePanel(new VerticalLayout(5));
    generalPanel.setScrollableHeightHint(ScrollableSizeHint.FIT);

    tabPanel.addTab("General", generalPanel);

    newPropertiesTabGeneralPanelGeneral(generalPanel);
    final ValueField sourcePanel = newPropertiesTabGeneralPanelSource(generalPanel);
    if (sourcePanel.getComponentCount() == 0) {
      generalPanel.remove(sourcePanel);
    }
    return generalPanel;
  }

  protected ValueField newPropertiesTabGeneralPanelGeneral(final BasePanel parent) {
    final ValueField panel = new ValueField(this);
    Borders.titled(panel, "General");
    final Field nameField = (Field)SwingUtil.addObjectField(panel, this, "name");
    Property.addListener(nameField, "name", this.beanPropertyListener);

    final String type = Property.get(this, "type");
    final String typeLabel = CaseConverter.toCapitalizedWords(type);
    SwingUtil.addLabelledReadOnlyTextField(panel, "Type", typeLabel);

    GroupLayouts.makeColumns(panel, 2, true);

    parent.add(panel);
    return panel;
  }

  protected ValueField newPropertiesTabGeneralPanelSource(final BasePanel parent) {
    final ValueField panel = new ValueField(this);
    Borders.titled(panel, "Source");

    parent.add(panel);
    return panel;
  }

  protected Component newTableViewComponent(final Map<String, Object> config) {
    return null;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    if (this.propertyChangeSupport != null && this.eventsEnabled.isTrue()) {
      this.propertyChangeSupport.firePropertyChange(event);
    }
  }

  @Override
  public final void refresh() {
    Invoke.background("Refresh Layer " + getName(), () -> {
      try {
        refreshDo();
      } catch (final Throwable e) {
        Logs.error(this, "Unable to refresh layer: " + getName(), e);
      }
      firePropertyChange("refresh", false, true);
    });
  }

  @Override
  public final void refreshAll() {
    try {
      refreshAllDo();
    } catch (final Throwable e) {
      Logs.error(this, "Unable to refresh layer: " + getName(), e);
    }
    firePropertyChange("refresh", false, true);
  }

  protected void refreshAllDo() {
    refreshDo();
  }

  protected void refreshDo() {
  }

  @Override
  public boolean saveChanges() {
    boolean saved = true;
    if (isHasChanges()) {
      saved &= saveChangesDo();
    }
    return saved;
  }

  protected boolean saveChangesDo() {
    return true;
  }

  public boolean saveSettings() {
    final Path directory = getDirectory();
    return saveSettings(directory);
  }

  @Override
  public boolean saveSettings(final Path directory) {
    if (directory != null) {
      if (canSaveSettings(directory)) {
        return saveSettingsDo(directory);
      }
    }
    return false;
  }

  protected boolean saveSettingsDo(final java.nio.file.Path directory) {
    final String settingsFileName = getSettingsFileName();
    final java.nio.file.Path settingsFile = directory.resolve(settingsFileName);
    MapObjectFactory.write(settingsFile, this);
    return true;
  }

  protected void setBoundingBox(final BoundingBox boundingBox) {
    this.boundingBox = boundingBox;
  }

  @Override
  public void setEditable(final boolean editable) {
    final boolean old = isEditable();
    this.editable = editable;
    firePropertyChange("editable", old, isEditable());
  }

  public void setExists(final boolean exists) {
    final boolean old = this.exists;
    this.exists = exists;
    firePropertyChange("exists", old, this.exists);
  }

  protected void setGeometryFactory(final GeometryFactory geometryFactory) {
    final GeometryFactory oldGeometryFactory = this.geometryFactory;
    if (setGeometryFactoryDo(geometryFactory)) {
      fireGeometryFactoryChanged(oldGeometryFactory, geometryFactory);
    }
  }

  protected boolean setGeometryFactoryDo(final GeometryFactory geometryFactory) {
    if (geometryFactory == null) {
      return false;
    } else if (geometryFactory.equals(this.geometryFactory)) {
      return false;
    } else {
      this.geometryFactory = geometryFactory;
      if (Property.isEmpty(this.boundingBox)) {
        final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
        if (coordinateSystem != null) {
          this.boundingBox = coordinateSystem.getAreaBoundingBox();
        }
      } else if (this.boundingBox != null
        && !this.boundingBox.getGeometryFactory().isHasCoordinateSystem()
        && geometryFactory.isHasCoordinateSystem()) {
        this.boundingBox = this.boundingBox.convert(geometryFactory);
      }
      return true;
    }
  }

  public void setIcon(final Icon icon) {
    this.icon = icon;
  }

  protected void setInitialized(final boolean initialized) {
    this.initialized = initialized;
    firePropertyChange("initialized", !initialized, this.initialized);
  }

  @Override
  public void setLayerGroup(final LayerGroup layerGroup) {
    final LayerGroup old = getLayerGroup();
    if (old != layerGroup) {
      if (old != null) {
        Property.removeListener(this, old);
      }
      this.layerGroup = new WeakReference<>(layerGroup);
      Property.addListener(this, layerGroup);
      firePropertyChange("layerGroup", old, layerGroup);
    }
  }

  @Override
  public void setMaximumScale(long maximumScale) {
    if (maximumScale < 0) {
      maximumScale = 0;
    }
    final long oldValue = this.maximumScale;
    this.maximumScale = maximumScale;
    firePropertyChange("maximumScale", oldValue, this.minimumScale);
  }

  @Override
  public void setMinimumScale(long minimumScale) {
    if (minimumScale <= 0) {
      minimumScale = Long.MAX_VALUE;
    }
    final long oldValue = this.minimumScale;
    this.minimumScale = minimumScale;
    firePropertyChange("minimumScale", oldValue, this.minimumScale);
  }

  @Override
  public void setName(final String name) {
    final Object oldValue = this.name;
    final LayerGroup layerGroup = getLayerGroup();
    String newName = name;
    if (layerGroup != null) {
      int i = 1;
      while (layerGroup.hasLayerWithSameName(this, newName)) {
        newName = name + i;
        i++;
      }
    }
    this.name = newName;
    firePropertyChange("name", oldValue, this.name);
  }

  @Override
  public void setOpen(final boolean open) {
    final boolean oldValue = this.open;
    this.open = open;
    firePropertyChange("open", oldValue, this.open);
  }

  public void setPluginConfig(final Map<String, Map<String, Object>> pluginConfig) {
    this.pluginConfigByName = pluginConfig;
  }

  public void setPluginConfig(final String pluginName, final Map<String, Object> config) {
    this.pluginConfigByName.put(pluginName, config);
  }

  public void setPluginConfig(final String pluginName, final MapSerializer serializer) {
    setPluginConfig(pluginName, new MapSerializerMap(serializer));
  }

  @Override
  public void setProperties(final Map<String, ? extends Object> properties) {
    if (properties == null || !this.getProperties().equals(properties)) {
      super.setProperties(properties);
      firePropertyChange("properties", null, properties);
    }
  }

  @Override
  public void setProperty(final String name, final Object value) {
    // TODO see if we can get the JavaBeanUtil set property to work with
    // conversions
    if (name.equals("type")) {
    } else if (name.equals("minimumScale")) {
      setMinimumScale(((Number)value).longValue());
    } else if (name.equals("open")) {
      setOpen((Boolean)value);
    } else if (name.equals("maximumScale")) {
      setMaximumScale(((Number)value).longValue());
    } else {
      final Object oldValue = getProperty(name);

      try {
        super.setProperty(name, value);
      } catch (final Throwable e) {
        Logs.error(this, "Unable to set property:" + name, e);
      }
      if (!DataType.equal(oldValue, value)) {
        final KeyedPropertyChangeEvent event = new KeyedPropertyChangeEvent(this, "property",
          oldValue, value, name);
        if (this.propertyChangeSupport != null) {
          this.propertyChangeSupport.firePropertyChange(event);
        }
      }
    }
  }

  @Override
  public void setQueryable(final boolean queryable) {
    this.queryable = queryable;
  }

  protected void setQuerySupported(final boolean querySupported) {
    this.querySupported = querySupported;
  }

  @Override
  public void setReadOnly(final boolean readOnly) {
    this.readOnly = readOnly;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setRenderer(final LayerRenderer<? extends Layer> renderer) {
    final LayerRenderer<?> oldValue = this.renderer;
    if (oldValue != null) {
      oldValue.setLayer(null);
      Property.removeListener(renderer, this);
    }
    this.renderer = (LayerRenderer<AbstractLayer>)renderer;
    if (renderer != null) {
      ((AbstractLayerRenderer<?>)this.renderer).setEditing(false);
      this.renderer.setLayer(this);
      Property.addListener(renderer, this);
    }
    firePropertyChange("renderer", oldValue, this.renderer);
    fireIndexedPropertyChange("renderer", 0, oldValue, this.renderer);
  }

  @Override
  public void setSelectable(final boolean selectable) {
    final boolean oldValue = this.selectable;
    this.selectable = selectable;
    firePropertyChange("selectable", oldValue, selectable);
  }

  public void setSelectSupported(final boolean selectSupported) {
    this.selectSupported = selectSupported;
  }

  protected void setType(final String type) {
    this.type = type;
  }

  @Override
  public void setVisible(final boolean visible) {
    final boolean oldVisible = this.visible;
    this.visible = visible;
    firePropertyChange("visible", oldVisible, visible);
  }

  @Override
  public void showProperties() {
    showProperties(null);
  }

  @Override
  public void showProperties(final String tabName) {
    final MapPanel map = getMapPanel();
    if (map != null) {
      if (this.exists) {
        if (checkShowProperties()) {
          try {
            final Window window = SwingUtilities.getWindowAncestor(map);
            final TabbedValuePanel panel = newPropertiesPanel();
            panel.setSelectdTab(tabName);
            panel.showDialog(window);
            refresh();
          } finally {
            removeProperty("INTERNAL_PROPERTIES_VISIBLE");
          }
        }
      }
    }
  }

  @Override
  public void showRendererProperties(final LayerRenderer<?> renderer) {
    final MapPanel map = getMapPanel();
    if (map != null) {
      if (this.exists) {
        if (checkShowProperties()) {
          try {
            final Window window = SwingUtilities.getWindowAncestor(map);
            final TabbedValuePanel panel = newPropertiesPanel();
            panel.setSelectdTab("Style");
            final LayerStylePanel stylePanel = panel.getTab("Style");
            stylePanel.setSelectedRenderer(renderer);
            panel.showDialog(window);
            refresh();
          } finally {
            removeProperty("INTERNAL_PROPERTIES_VISIBLE");
          }
        }
      }
    }
  }

  @Override
  public void showTableView() {
    showTableView(Collections.emptyMap());
  }

  @Override
  public <C extends Component> C showTableView(final Map<String, Object> config) {
    final ProjectFrame projectFrame = ProjectFrame.get(this);
    if (projectFrame == null) {
      return null;
    } else {
      return projectFrame.addBottomTab(this, config);
    }
  }

  public void toggleEditable() {
    final boolean editable = isEditable();
    setEditable(!editable);
  }

  @SuppressWarnings("unchecked")
  @Override
  public MapEx toMap() {
    final MapEx map = new LinkedHashMapEx();
    addTypeToMap(map, this.type);
    addToMap(map, "name", this.name);
    addToMap(map, "visible", this.visible);
    addToMap(map, "open", this.open);
    addToMap(map, "querySupported", this.querySupported);
    if (this.querySupported) {
      addToMap(map, "queryable", this.queryable);
    }
    addToMap(map, "readOnly", this.readOnly);
    if (!this.readOnly) {
      addToMap(map, "editable", this.editable);
    }
    if (this.selectSupported) {
      addToMap(map, "selectable", this.selectable);
    }
    addToMap(map, "selectSupported", this.selectSupported);
    addToMap(map, "maximumScale", this.maximumScale);
    addToMap(map, "minimumScale", this.minimumScale);
    addToMap(map, "style", this.renderer);
    addToMap(map, "pluginConfig", this.pluginConfigByName);
    final Map<String, Object> properties = (Map<String, Object>)toMapValue(getProperties());
    if (properties != null) {
      for (final Entry<String, Object> entry : properties.entrySet()) {
        final String name = entry.getKey();
        if (!map.containsKey(name) && !name.startsWith("INTERNAL")) {
          final Object value = entry.getValue();
          if (!(value instanceof Component)) {
            map.put(name, value);
          }
        }
      }
    }
    map.remove("showTableView");
    return map;
  }

  @Override
  public String toString() {
    return getName();
  }

  public void zoomToLayer() {
    final Project project = getProject();
    if (project != null) {
      final GeometryFactory geometryFactory = project.getGeometryFactory();
      final BoundingBox layerBoundingBox = getBoundingBox();
      final BoundingBox boundingBox = layerBoundingBox.convert(geometryFactory)
        .clipToCoordinateSystem()
        .expandPercent(0.1);
      project.setViewBoundingBox(boundingBox);
    }
  }
}
