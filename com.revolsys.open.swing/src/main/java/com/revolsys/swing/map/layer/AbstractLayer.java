package com.revolsys.swing.map.layer;

import java.awt.Component;
import java.awt.Font;
import java.awt.TextArea;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.ScrollableSizeHint;
import org.jdesktop.swingx.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bibliothek.gui.dock.common.DefaultSingleCDockable;

import com.revolsys.beans.KeyedPropertyChangeEvent;
import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.esri.EsriCoordinateSystems;
import com.revolsys.gis.cs.esri.EsriCsWktWriter;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.io.AbstractObjectWithProperties;
import com.revolsys.io.FileUtil;
import com.revolsys.io.map.MapObjectFactoryRegistry;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.enablecheck.AndEnableCheck;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.border.TitledBorder;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.TabbedValuePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.listener.BeanPropertyListener;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.layer.dataobject.style.panel.DataObjectLayerStylePanel;
import com.revolsys.swing.map.layer.menu.TreeItemScaleMenu;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.TreeItemPropertyEnableCheck;
import com.revolsys.swing.tree.TreeItemRunnable;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.util.ExceptionUtil;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Property;

public abstract class AbstractLayer extends AbstractObjectWithProperties
  implements Layer, PropertyChangeListener, PropertyChangeSupportProxy {
  private static final AtomicLong ID_GEN = new AtomicLong();

  static {
    final MenuFactory menu = ObjectTreeModel.getMenu(AbstractLayer.class);

    final EnableCheck exists = new TreeItemPropertyEnableCheck("exists");

    final EnableCheck hasGeometry = new TreeItemPropertyEnableCheck(
      "hasGeometry");
    menu.addMenuItem("zoom", TreeItemRunnable.createAction("Zoom to Layer",
      "magnifier", new AndEnableCheck(exists, hasGeometry), "zoomToLayer"));

    menu.addComponentFactory("scale", new TreeItemScaleMenu(true));
    menu.addComponentFactory("scale", new TreeItemScaleMenu(false));

    menu.addMenuItem(TreeItemRunnable.createAction("Refresh", "arrow_refresh",
      exists, "refresh"));

    menu.addMenuItem("layer", TreeItemRunnable.createAction("Delete Layer",
      "delete", "deleteWithConfirm"));

    menu.addMenuItem("layer", TreeItemRunnable.createAction("Layer Properties",
      "information", exists, "showProperties"));
  }

  private boolean exists = true;

  private PropertyChangeListener beanPropertyListener = new BeanPropertyListener(
    this);

  private boolean editable = false;

  private Reference<LayerGroup> layerGroup;

  private String name;

  private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
    this);

  private GeometryFactory geometryFactory;

  private boolean readOnly = false;

  private boolean selectable = true;

  private boolean selectSupported = true;

  private long maximumScale = 0;

  private long minimumScale = Long.MAX_VALUE;

  private boolean visible = true;

  private boolean queryable = true;

  private final ThreadLocal<Boolean> eventsEnabled = new ThreadLocal<Boolean>();

  private boolean querySupported = true;

  private final long id = ID_GEN.incrementAndGet();

  private LayerRenderer<?> renderer;

  private String type;

  private boolean initialized;

  public AbstractLayer() {
  }

  public AbstractLayer(final Map<String, ? extends Object> properties) {
    // Don't use super constructor as fields will not have been populated
    setProperties(properties);
  }

  public AbstractLayer(final String name) {
    this.name = name;
  }

  public AbstractLayer(final String name,
    final Map<String, ? extends Object> properties) {
    this.name = name;
    setProperties(properties);
  }

  protected void addParent(final List<Layer> path) {
    final LayerGroup parent = getLayerGroup();
    if (parent != null) {
      path.add(0, parent);
      parent.addParent(path);
    }
  }

  protected JPanel addPropertiesTabCoordinateSystem(
    final TabbedValuePanel tabPanel) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (geometryFactory != null) {
      final JPanel panel = new JPanel(new VerticalLayout(5));
      tabPanel.addTab("Spatial", panel);

      final JPanel extentPanel = new JPanel();
      SwingUtil.setTitledBorder(extentPanel, "Extent");
      final BoundingBox boundingBox = getBoundingBox();
      if (boundingBox == null || boundingBox.isEmpty()) {
        extentPanel.add(new JLabel("Unknown"));

      } else {
        extentPanel.add(new JLabel(
          "<html><table cellspacing=\"3\" style=\"margin:0px\">"
            + "<tr><td>&nbsp;</td><th style=\"text-align:left\">Top:</th><td style=\"text-align:right\">"
            + boundingBox.getMaximumY()
            + "</td><td>&nbsp;</td></tr><tr>"
            + "<td><b>Left</b>: "
            + boundingBox.getMinimumX()
            + "</td><td>&nbsp;</td><td>&nbsp;</td>"
            + "<td><b>Right</b>: "
            + boundingBox.getMaximumX()
            + "</td></tr>"
            + "<tr><td>&nbsp;</td><th>Bottom:</th><td style=\"text-align:right\">"
            + boundingBox.getMinimumY() + "</td><td>&nbsp;</td></tr><tr>"
            + "</tr></table></html>"));

      }
      GroupLayoutUtil.makeColumns(extentPanel, 1, true);
      panel.add(extentPanel);

      final JPanel coordinateSystemPanel = new JPanel();
      coordinateSystemPanel.setBorder(new TitledBorder("Coordinate System"));
      final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
      if (coordinateSystem == null) {
        coordinateSystemPanel.add(new JLabel("Unknown"));
      } else {
        final int numAxis = geometryFactory.getNumAxis();
        SwingUtil.addReadOnlyTextField(coordinateSystemPanel, "ID",
          coordinateSystem.getId(), 10);
        SwingUtil.addReadOnlyTextField(coordinateSystemPanel, "numAxis",
          numAxis, 10);

        final double scaleXY = geometryFactory.getScaleXY();
        if (scaleXY > 0) {
          SwingUtil.addReadOnlyTextField(coordinateSystemPanel, "scaleXy",
            scaleXY, 10);
        } else {
          SwingUtil.addReadOnlyTextField(coordinateSystemPanel, "scaleXy",
            "Floating", 10);
        }

        if (numAxis > 2) {
          final double scaleZ = geometryFactory.getScaleZ();
          if (scaleZ > 0) {
            SwingUtil.addReadOnlyTextField(coordinateSystemPanel, "scaleZ",
              scaleZ, 10);
          } else {
            SwingUtil.addReadOnlyTextField(coordinateSystemPanel, "scaleZ",
              "Floating", 10);
          }
        }

        final CoordinateSystem esriCoordinateSystem = EsriCoordinateSystems.getCoordinateSystem(coordinateSystem);
        SwingUtil.addLabel(coordinateSystemPanel, "ESRI WKT");
        final TextArea wktTextArea = new TextArea(
          EsriCsWktWriter.toString(esriCoordinateSystem), 10, 80);
        wktTextArea.setEditable(false);
        wktTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        coordinateSystemPanel.add(wktTextArea);

        GroupLayoutUtil.makeColumns(coordinateSystemPanel, 2, true);
      }
      panel.add(new JScrollPane(coordinateSystemPanel));

      return panel;
    }
    return null;
  }

  protected JPanel addPropertiesTabGeneral(final TabbedValuePanel tabPanel) {
    final BasePanel generalPanel = new BasePanel(new VerticalLayout(5));
    generalPanel.setScrollableHeightHint(ScrollableSizeHint.FIT);

    tabPanel.addTab("General", generalPanel);

    addPropertiesTabGeneralPanelGeneral(generalPanel);
    final ValueField sourcePanel = addPropertiesTabGeneralPanelSource(generalPanel);
    if (sourcePanel.getComponentCount() == 0) {
      generalPanel.remove(sourcePanel);
    }
    return generalPanel;
  }

  protected ValueField addPropertiesTabGeneralPanelGeneral(
    final BasePanel parent) {
    final ValueField panel = new ValueField(this);
    SwingUtil.setTitledBorder(panel, "General");
    final Field nameField = (Field)SwingUtil.addObjectField(panel, this, "name");
    nameField.addPropertyChangeListener("name", this.beanPropertyListener);

    final Field typeField = (Field)SwingUtil.addObjectField(panel, this, "type");
    typeField.setEnabled(false);

    GroupLayoutUtil.makeColumns(panel, 2, true);

    parent.add(panel);
    return panel;
  }

  protected ValueField addPropertiesTabGeneralPanelSource(final BasePanel parent) {
    final ValueField panel = new ValueField(this);
    SwingUtil.setTitledBorder(panel, "Source");

    parent.add(panel);
    return panel;
  }

  @Override
  public void addPropertyChangeListener(final PropertyChangeListener listener) {
    this.propertyChangeSupport.addPropertyChangeListener(listener);
  }

  @Override
  public void addPropertyChangeListener(final String propertyName,
    final PropertyChangeListener listener) {
    this.propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
  }

  public boolean canSaveSettings(final File directory) {
    if (directory != null) {
      final Logger log = LoggerFactory.getLogger(getClass());
      if (!directory.exists()) {
        log.error("Unable to save layer " + getPath()
          + " directory does not exist " + directory);
      } else if (!directory.isDirectory()) {
        log.error("Unable to save layer " + getPath()
          + " file is not a directory " + directory);
      } else if (!directory.canWrite()) {
        log.error("Unable to save layer " + getPath()
          + " directory is not writable " + directory);
      } else {
        return true;
      }
    }
    return false;
  }

  protected boolean checkShowProperties() {
    boolean show = true;
    synchronized (this) {
      if (BooleanStringConverter.getBoolean(getProperty("INTERNAL_PROPERTIES_VISIBLE"))) {
        show = false;
      } else {
        setProperty("INTERNAL_PROPERTIES_VISIBLE", true);
      }
    }
    return show;
  }

  @Override
  public int compareTo(final Layer layer) {
    return getName().compareTo(layer.getName());
  }

  @Override
  public TabbedValuePanel createPropertiesPanel() {
    final TabbedValuePanel tabPanel = new TabbedValuePanel("Layer " + this
      + " Properties", this);
    addPropertiesTabGeneral(tabPanel);
    addPropertiesTabCoordinateSystem(tabPanel);
    return tabPanel;
  }

  @Override
  public void delete() {
    setExists(false);
    this.beanPropertyListener = null;
    final DefaultSingleCDockable dockable = getProperty("TableView");
    if (dockable != null) {
      // TODO all this should be done by listeners
      dockable.setVisible(false);
    }
    firePropertyChange("deleted", false, true);
    setEventsEnabled(false);
    final LayerGroup layerGroup = getLayerGroup();
    if (layerGroup != null) {
      layerGroup.remove(this);
    }
    final PropertyChangeSupport propertyChangeSupport = this.propertyChangeSupport;
    if (propertyChangeSupport != null) {
      Property.removeAllListeners(propertyChangeSupport);
      this.propertyChangeSupport = null;
    }
  }

  public void deleteWithConfirm() {
    final int confirm = JOptionPane.showConfirmDialog(MapPanel.get(this),
      "Delete the layer and any child layers? This action cannot be undone.",
      "Delete Layer", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
    if (confirm == JOptionPane.OK_OPTION) {
      delete();
    }
  }

  protected boolean doInitialize() {
    return true;
  }

  protected boolean doSaveChanges() {
    return true;
  }

  protected boolean doSaveSettings(final File directory) {
    final String settingsFileName = getSettingsFileName();
    final File settingsFile = new File(directory, settingsFileName);
    MapObjectFactoryRegistry.write(settingsFile, this);
    return true;
  }

  protected void fireIndexedPropertyChange(final String propertyName,
    final int index, final Object oldValue, final Object newValue) {
    if (this.propertyChangeSupport != null) {
      this.propertyChangeSupport.fireIndexedPropertyChange(propertyName, index,
        oldValue, newValue);
    }
  }

  protected void firePropertyChange(final String propertyName,
    final Object oldValue, final Object newValue) {
    if (this.propertyChangeSupport != null && isEventsEnabled()) {
      this.propertyChangeSupport.firePropertyChange(propertyName, oldValue,
        newValue);
    }
  }

  @Override
  public BoundingBox getBoundingBox() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return new BoundingBox(geometryFactory);
  }

  @Override
  public BoundingBox getBoundingBox(final boolean visibleLayersOnly) {
    if (this.visible || !visibleLayersOnly) {
      return getBoundingBox();
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      return new BoundingBox(geometryFactory);
    }
  }

  public File getDirectory() {
    final LayerGroup layerGroup = getLayerGroup();
    if (layerGroup == null) {
      return null;
    } else {
      return layerGroup.getDirectory();
    }
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    final LayerGroup layerGroup = getLayerGroup();
    if (this.geometryFactory == null && layerGroup != null) {
      return layerGroup.getGeometryFactory();
    } else {
      return this.geometryFactory;
    }
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
    final List<Layer> path = new ArrayList<Layer>();
    path.add(this);
    addParent(path);
    return path;
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
    return new BoundingBox(geometryFactory);
  }

  protected String getSettingsFileName() {
    final String name = getName();
    return FileUtil.getSafeFileName(name) + ".rgobject";
  }

  @Override
  public String getType() {
    return this.type;
  }

  @Override
  public final synchronized void initialize() {
    if (!isInitialized()) {
      try {
        final boolean exists = doInitialize();
        setExists(exists);
      } catch (final Throwable e) {
        ExceptionUtil.log(getClass(), "Unable to initialize layer: "
          + getPath(), e);
        setExists(false);
      } finally {
        setInitialized(true);
      }
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
    if (eventsEnabled.get() != Boolean.FALSE) {
      final LayerGroup layerGroup = getLayerGroup();
      if (layerGroup == null || layerGroup == this) {
        return true;
      } else {
        return layerGroup.isEventsEnabled();
      }
    } else {
      return false;
    }
  }

  @Override
  public boolean isExists() {
    return isInitialized() && exists;
  }

  @Override
  public boolean isHasChanges() {
    return false;
  }

  @Override
  public boolean isHasGeometry() {
    return true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
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
    return isExists() && isVisible()
      && (isSelectSupported() && this.selectable || isEditable());
  }

  @Override
  public boolean isSelectable(final double scale) {
    return isVisible(scale) && isSelectable();
  }

  @Override
  public boolean isSelectSupported() {
    return this.selectSupported;
  }

  @Override
  public boolean isVisible() {
    return this.visible;
  }

  @Override
  public boolean isVisible(final double scale) {
    if (isExists() && isVisible()) {
      final long longScale = (long)scale;
      if (getMinimumScale() >= longScale && longScale >= getMaximumScale()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    if (propertyChangeSupport != null && isEventsEnabled()) {
      this.propertyChangeSupport.firePropertyChange(event);
    }
  }

  @Override
  public void refresh() {
    firePropertyChange("refresh", false, true);
  }

  @Override
  public void removePropertyChangeListener(final PropertyChangeListener listener) {
    if (this.propertyChangeSupport != null) {
      this.propertyChangeSupport.removePropertyChangeListener(listener);
    }
  }

  @Override
  public void removePropertyChangeListener(final String propertyName,
    final PropertyChangeListener listener) {
    if (this.propertyChangeSupport != null) {
      this.propertyChangeSupport.removePropertyChangeListener(propertyName,
        listener);
    }
  }

  @Override
  public boolean saveChanges() {
    boolean saved = true;
    if (isHasChanges()) {
      saved &= doSaveChanges();
    }
    return saved;
  }

  public boolean saveSettings() {
    final File directory = getDirectory();
    return saveSettings(directory);
  }

  @Override
  public boolean saveSettings(final File directory) {
    if (canSaveSettings(directory)) {
      return doSaveSettings(directory);
    }
    return false;
  }

  @Override
  public void setEditable(final boolean editable) {
    final boolean old = isEditable();
    this.editable = editable;
    firePropertyChange("editable", old, isEditable());
  }

  public boolean setEventsEnabled(final boolean eventsEnabled) {
    final boolean oldValue = this.eventsEnabled.get() != Boolean.FALSE;
    if (eventsEnabled) {
      this.eventsEnabled.set(null);
    } else {
      this.eventsEnabled.set(Boolean.FALSE);
    }
    return oldValue;
  }

  public void setExists(final boolean exists) {
    final boolean old = this.exists;
    this.exists = exists;
    firePropertyChange("exists", old, this.exists);
  }

  protected void setGeometryFactory(final GeometryFactory geometryFactory) {
    if (geometryFactory != this.geometryFactory && geometryFactory != null) {
      final GeometryFactory old = this.geometryFactory;
      this.geometryFactory = geometryFactory;
      firePropertyChange("geometryFactory", old, this.geometryFactory);
    }
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
        removePropertyChangeListener(old);
      }
      this.layerGroup = new WeakReference<LayerGroup>(layerGroup);
      addPropertyChangeListener(layerGroup);
      firePropertyChange("layerGroup", old, layerGroup);
    }
  }

  @Override
  public void setMaximumScale(final long maximumScale) {
    this.maximumScale = maximumScale;
  }

  @Override
  public void setMinimumScale(final long minimumScale) {
    this.minimumScale = minimumScale;
  }

  @Override
  public void setName(final String name) {
    final Object oldValue = this.name;
    this.name = name;
    firePropertyChange("name", this.name, oldValue);
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
    } else if (name.equals("maximumScale")) {
      setMaximumScale(((Number)value).longValue());
    } else {
      final Object oldValue = getProperty(name);
      if (!EqualsRegistry.INSTANCE.equals(oldValue, value)) {
        final KeyedPropertyChangeEvent event = new KeyedPropertyChangeEvent(
          this, "property", oldValue, value, name);
        if (propertyChangeSupport != null) {
          this.propertyChangeSupport.firePropertyChange(event);
        }
        try {
          JavaBeanUtil.setProperty(this, name, value);
          super.setProperty(name, value);
        } catch (final Throwable e) {
          LoggerFactory.getLogger(getClass()).error(
            "Unable to set property:" + name, e);
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

  @Override
  public void setRenderer(final LayerRenderer<? extends Layer> renderer) {
    final Object oldValue = this.renderer;
    Property.removeListener(this.renderer, this);
    this.renderer = renderer;
    Property.addListener(this.renderer, this);
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
    final MapPanel map = MapPanel.get(this);
    if (map != null) {
      if (exists) {
        if (checkShowProperties()) {
          try {
            final Window window = SwingUtilities.getWindowAncestor(map);
            final TabbedValuePanel panel = createPropertiesPanel();
            panel.setSelectdTab(tabName);
            panel.showDialog(window);
          } finally {
            removeProperty("INTERNAL_PROPERTIES_VISIBLE");
          }
        }
      }
    }
  }

  @Override
  public void showRendererProperties(final LayerRenderer<?> renderer) {
    final MapPanel map = MapPanel.get(this);
    if (map != null) {
      if (exists) {
        if (checkShowProperties()) {
          try {
            final Window window = SwingUtilities.getWindowAncestor(map);
            final TabbedValuePanel panel = createPropertiesPanel();
            panel.setSelectdTab("Style");
            final DataObjectLayerStylePanel stylePanel = panel.getTab("Style");
            stylePanel.setSelectedRenderer(renderer);
            panel.showDialog(window);
          } finally {
            removeProperty("INTERNAL_PROPERTIES_VISIBLE");
          }
        }
      }
    }
  }

  public void toggleEditable() {
    final boolean editable = isEditable();
    setEditable(!editable);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = new LinkedHashMap<String, Object>();
    MapSerializerUtil.add(map, "type", this.type);
    MapSerializerUtil.add(map, "name", this.name);
    MapSerializerUtil.add(map, "visible", this.visible);
    MapSerializerUtil.add(map, "querySupported", this.querySupported);
    if (this.querySupported) {
      MapSerializerUtil.add(map, "queryable", this.queryable);
    }
    MapSerializerUtil.add(map, "readOnly", this.readOnly);
    if (!this.readOnly) {
      MapSerializerUtil.add(map, "editable", this.editable);
    }
    if (this.selectSupported) {
      MapSerializerUtil.add(map, "selectable", this.selectable);
    }
    MapSerializerUtil.add(map, "selectSupported", this.selectSupported);
    MapSerializerUtil.add(map, "maximumScale", this.maximumScale);
    MapSerializerUtil.add(map, "minimumScale", this.minimumScale);
    MapSerializerUtil.add(map, "style", this.renderer);
    final Map<String, Object> properties = (Map<String, Object>)MapSerializerUtil.getValue(getProperties());
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
    return map;
  }

  @Override
  public String toString() {
    return getName();
  }

  public void zoomToLayer() {
    final Project project = getProject();
    final GeometryFactory geometryFactory = project.getGeometryFactory();
    final BoundingBox layerBoundingBox = getBoundingBox();
    final BoundingBox boundingBox = layerBoundingBox.convert(geometryFactory)
      .expandPercent(0.1)
      .clipToCoordinateSystem();

    project.setViewBoundingBox(boundingBox);
  }
}
