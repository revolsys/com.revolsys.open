package com.revolsys.swing.map.layer;

import java.awt.Component;
import java.awt.Font;
import java.awt.TextArea;
import java.awt.Window;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.ScrollableSizeHint;
import org.jdesktop.swingx.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.beans.KeyedPropertyChangeEvent;
import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.equals.EqualsInstance;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.esri.EsriCoordinateSystems;
import com.revolsys.gis.cs.esri.EsriCsWktWriter;
import com.revolsys.io.AbstractObjectWithProperties;
import com.revolsys.io.FileUtil;
import com.revolsys.io.map.MapObjectFactoryRegistry;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.border.TitledBorder;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.ButtonTabComponent;
import com.revolsys.swing.component.TabbedValuePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.listener.BeanPropertyListener;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.ProjectFrame;
import com.revolsys.swing.map.layer.record.style.panel.LayerStylePanel;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.ExceptionUtil;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Property;

public abstract class AbstractLayer extends AbstractObjectWithProperties
implements Layer, PropertyChangeListener, PropertyChangeSupportProxy {
  public static final ImageIcon ICON_LAYER = Icons.getIcon("map");

  private static final AtomicLong ID_GEN = new AtomicLong();

  static {
    MenuFactory.createMenu(AbstractLayer.class, "ZoomToLayer", "MinScale",
      "MaxScale", "Refresh", "DeleteLayer", "LayerProperties");
  }

  private PropertyChangeListener beanPropertyListener = new BeanPropertyListener(
    this);

  private boolean editable = false;

  private ThreadLocal<Boolean> eventsEnabled = new ThreadLocal<>();

  private boolean exists = true;

  private GeometryFactory geometryFactory;

  private long id = ID_GEN.incrementAndGet();

  private Icon icon = ICON_LAYER;

  private boolean initialized;

  private Reference<LayerGroup> layerGroup;

  private long maximumScale = 0;

  private long minimumScale = Long.MAX_VALUE;

  private String name;

  private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
    this);

  private boolean queryable = true;

  private boolean querySupported = true;

  private boolean readOnly = false;

  private LayerRenderer<AbstractLayer> renderer;

  private boolean selectable = true;

  private boolean selectSupported = true;

  private String type;

  private boolean visible = true;

  protected Object sync = new Object();

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

  public int addRenderer(final LayerRenderer<?> child) {
    return addRenderer(child, 0);
  }

  public int addRenderer(final LayerRenderer<?> child, final int index) {
    setRenderer(child);
    return 0;
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
  public AbstractLayer clone() {
    try {
      final AbstractLayer clone = (AbstractLayer)super.clone();
      clone.beanPropertyListener = new BeanPropertyListener(clone);
      clone.eventsEnabled = new ThreadLocal<>();
      clone.id = this.id = ID_GEN.incrementAndGet();
      clone.initialized = false;
      clone.layerGroup = null;
      clone.propertyChangeSupport = new PropertyChangeSupport(clone);
      if (clone.renderer != null) {
        clone.renderer = clone.renderer.clone();
      }
      return clone;
    } catch (final CloneNotSupportedException e) {
      return null;
    }
  }

  @Override
  public int compareTo(final Layer layer) {
    return getName().compareTo(layer.getName());
  }

  @Override
  public TabbedValuePanel createPropertiesPanel() {
    final TabbedValuePanel tabPanel = new TabbedValuePanel("Layer " + this
      + " Properties", this);
    createPropertiesTabGeneral(tabPanel);
    createPropertiesTabCoordinateSystem(tabPanel);
    return tabPanel;
  }

  protected JPanel createPropertiesTabCoordinateSystem(
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
        final JLabel extentLabel = new JLabel(
          "<html><table cellspacing=\"3\" style=\"margin:0px\">"
              + "<tr><td>&nbsp;</td><th style=\"text-align:left\">Top:</th><td style=\"text-align:right\">"
              + StringConverterRegistry.toString(boundingBox.getMaximum(1))
              + "</td><td>&nbsp;</td></tr><tr>"
              + "<td><b>Left</b>: "
              + StringConverterRegistry.toString(boundingBox.getMinimum(0))
              + "</td><td>&nbsp;</td><td>&nbsp;</td>"
              + "<td><b>Right</b>: "
              + StringConverterRegistry.toString(boundingBox.getMaximum(0))
              + "</td></tr>"
              + "<tr><td>&nbsp;</td><th>Bottom:</th><td style=\"text-align:right\">"
              + StringConverterRegistry.toString(boundingBox.getMinimum(1))
              + "</td><td>&nbsp;</td></tr><tr>" + "</tr></table></html>");
        extentLabel.setFont(SwingUtil.FONT);
        extentPanel.add(extentLabel);

      }
      GroupLayoutUtil.makeColumns(extentPanel, 1, true);
      panel.add(extentPanel);

      final JPanel coordinateSystemPanel = new JPanel();
      coordinateSystemPanel.setBorder(new TitledBorder("Coordinate System"));
      final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
      if (coordinateSystem == null) {
        coordinateSystemPanel.add(new JLabel("Unknown"));
      } else {
        final int axisCount = geometryFactory.getAxisCount();
        SwingUtil.addLabelledReadOnlyTextField(coordinateSystemPanel, "ID",
          coordinateSystem.getId(), 10);
        SwingUtil.addLabelledReadOnlyTextField(coordinateSystemPanel, "axisCount",
          axisCount, 10);

        final double scaleXY = geometryFactory.getScaleXY();
        if (scaleXY > 0) {
          SwingUtil.addLabelledReadOnlyTextField(coordinateSystemPanel, "scaleXy",
            scaleXY, 10);
        } else {
          SwingUtil.addLabelledReadOnlyTextField(coordinateSystemPanel, "scaleXy",
            "Floating", 10);
        }

        if (axisCount > 2) {
          final double scaleZ = geometryFactory.getScaleZ();
          if (scaleZ > 0) {
            SwingUtil.addLabelledReadOnlyTextField(coordinateSystemPanel, "scaleZ",
              scaleZ, 10);
          } else {
            SwingUtil.addLabelledReadOnlyTextField(coordinateSystemPanel, "scaleZ",
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

  protected BasePanel createPropertiesTabGeneral(final TabbedValuePanel tabPanel) {
    final BasePanel generalPanel = new BasePanel(new VerticalLayout(5));
    generalPanel.setScrollableHeightHint(ScrollableSizeHint.FIT);

    tabPanel.addTab("General", generalPanel);

    createPropertiesTabGeneralPanelGeneral(generalPanel);
    final ValueField sourcePanel = createPropertiesTabGeneralPanelSource(generalPanel);
    if (sourcePanel.getComponentCount() == 0) {
      generalPanel.remove(sourcePanel);
    }
    return generalPanel;
  }

  protected ValueField createPropertiesTabGeneralPanelGeneral(
    final BasePanel parent) {
    final ValueField panel = new ValueField(this);
    SwingUtil.setTitledBorder(panel, "General");
    final Field nameField = (Field)SwingUtil.addObjectField(panel, this, "name");
    Property.addListener(nameField, "name", this.beanPropertyListener);

    final String type = Property.get(this, "type");
    final String typeLabel = CaseConverter.toCapitalizedWords(type);
    SwingUtil.addLabelledReadOnlyTextField(panel, "Type", typeLabel);

    GroupLayoutUtil.makeColumns(panel, 2, true);

    parent.add(panel);
    return panel;
  }

  protected ValueField createPropertiesTabGeneralPanelSource(
    final BasePanel parent) {
    final ValueField panel = new ValueField(this);
    SwingUtil.setTitledBorder(panel, "Source");

    parent.add(panel);
    return panel;
  }

  protected Component createTableViewComponent() {
    return null;
  }

  @Override
  public void delete() {
    setExists(false);
    this.beanPropertyListener = null;
    final Component component = getProperty("TableView");
    if (component != null) {
      final ProjectFrame projectFrame = ProjectFrame.get(this);
      if (projectFrame != null) {
        final JTabbedPane bottomTabs = projectFrame.getBottomTabs();
        if (bottomTabs != null) {
          bottomTabs.remove(component);
        }
      }
      setProperty("TableView", null);
    }
    firePropertyChange("deleted", false, true);
    setEventsEnabled(false);
    final LayerGroup layerGroup = getLayerGroup();
    if (layerGroup != null) {
      layerGroup.removeLayer(this);
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

  protected void doRefresh() {
  }

  protected void doRefreshAll() {
    doRefresh();
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

  public void firePropertyChange(final String propertyName,
    final Object oldValue, final Object newValue) {
    if (this.propertyChangeSupport != null && isEventsEnabled()) {
      this.propertyChangeSupport.firePropertyChange(propertyName, oldValue,
        newValue);
    }
  }

  public PropertyChangeListener getBeanPropertyListener() {
    return this.beanPropertyListener;
  }

  @Override
  public BoundingBox getBoundingBox() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return new BoundingBoxDoubleGf(geometryFactory);
  }

  @Override
  public BoundingBox getBoundingBox(final boolean visibleLayersOnly) {
    if (this.visible || !visibleLayersOnly) {
      return getBoundingBox();
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      return new BoundingBoxDoubleGf(geometryFactory);
    }
  }

  @Override
  public Collection<Class<?>> getChildClasses() {
    return Collections.emptySet();
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
    if (geometryFactory == null) {
      return new BoundingBoxDoubleGf();
    } else {
      return geometryFactory.boundingBox();
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
  public boolean isClonable() {
    return false;
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
    if (this.eventsEnabled.get() != Boolean.FALSE) {
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
    return isInitialized() && this.exists;
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
    return this.initialized;
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
    if (isExists() && isVisible()
        && (parent == null || parent.isVisible(scale))) {
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
  public void propertyChange(final PropertyChangeEvent event) {
    if (this.propertyChangeSupport != null && isEventsEnabled()) {
      this.propertyChangeSupport.firePropertyChange(event);
    }
  }

  @Override
  public void refresh() {
    if (SwingUtil.isEventDispatchThread()) {
      Invoke.background("Refresh " + getName(), this, "refresh");
    } else {
      try {
        doRefresh();
      } catch (final Throwable e) {
        LoggerFactory.getLogger(getClass()).error(
          "Unable to refresh layer: " + getName(), e);
      }
      firePropertyChange("refresh", false, true);
    }
  }

  @Override
  public void refreshAll() {
    if (SwingUtil.isEventDispatchThread()) {
      Invoke.background("Refresh " + getName(), this, "refreshAll");
    } else {
      try {
        doRefreshAll();
      } catch (final Throwable e) {
        LoggerFactory.getLogger(getClass()).error(
          "Unable to refresh layer: " + getName(), e);
      }
      firePropertyChange("refresh", false, true);
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
    if (directory != null) {
      if (canSaveSettings(directory)) {
        return doSaveSettings(directory);
      }
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
      this.layerGroup = new WeakReference<LayerGroup>(layerGroup);
      Property.addListener(this, layerGroup);
      firePropertyChange("layerGroup", old, layerGroup);
    }
  }

  @Override
  public void setMaximumScale(long maximumScale) {
    final double oldValue = this.maximumScale;
    if (maximumScale < 0) {
      maximumScale = 0;
    }
    this.maximumScale = maximumScale;
    firePropertyChange("maximumScale", oldValue, this.minimumScale);
  }

  @Override
  public void setMinimumScale(long minimumScale) {
    final double oldValue = this.minimumScale;
    if (minimumScale <= 0) {
      minimumScale = Long.MAX_VALUE;
    }
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
      if (!EqualsInstance.INSTANCE.equals(oldValue, value)) {
        final KeyedPropertyChangeEvent event = new KeyedPropertyChangeEvent(
          this, "property", oldValue, value, name);
        if (this.propertyChangeSupport != null) {
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
    final MapPanel map = MapPanel.get(this);
    if (map != null) {
      if (this.exists) {
        if (checkShowProperties()) {
          try {
            final Window window = SwingUtilities.getWindowAncestor(map);
            final TabbedValuePanel panel = createPropertiesPanel();
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
    final MapPanel map = MapPanel.get(this);
    if (map != null) {
      if (this.exists) {
        if (checkShowProperties()) {
          try {
            final Window window = SwingUtilities.getWindowAncestor(map);
            final TabbedValuePanel panel = createPropertiesPanel();
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

  protected <C extends Component> C showTableView() {
    final JTabbedPane tabs = ProjectFrame.get(this).getBottomTabs();
    final Object tableView = getProperty("TableView");
    Component component = null;
    if (tableView instanceof Component) {
      component = (Component)tableView;
      if (component.getParent() != tabs) {
        component = null;
      }
    }
    if (component == null) {
      component = createTableViewComponent();

      if (component != null) {
        final int tabIndex = tabs.getTabCount();
        final String name = getName();
        tabs.addTab(name, getIcon(), component);
        tabs.setTabComponentAt(tabIndex, new ButtonTabComponent(tabs));
        if (component != null) {
          final Component tabComponent = component;
          setPropertyWeak("TableView", tabComponent);
          tabs.addContainerListener(new ContainerAdapter() {
            @Override
            public void componentRemoved(final ContainerEvent e) {
              final Component eventComponent = e.getChild();
              if (eventComponent == tabComponent) {
                setProperty("TableView", null);
              }
            }
          });
          tabs.setSelectedIndex(tabIndex);
        }
      }
    } else {
      tabs.setSelectedComponent(component);
    }
    return (C)component;
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
