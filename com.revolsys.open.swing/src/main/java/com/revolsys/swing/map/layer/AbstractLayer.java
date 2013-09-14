package com.revolsys.swing.map.layer;

import java.awt.Component;
import java.awt.Font;
import java.awt.TextArea;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.VerticalLayout;
import org.slf4j.LoggerFactory;

import bibliothek.gui.dock.common.DefaultSingleCDockable;

import com.revolsys.beans.KeyedPropertyChangeEvent;
import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.esri.EsriCoordinateSystems;
import com.revolsys.gis.cs.esri.EsriCsWktWriter;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.io.AbstractObjectWithProperties;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.component.TabbedValuePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.listener.BeanPropertyListener;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.layer.dataobject.style.panel.DataObjectLayerStylePanel;
import com.revolsys.swing.map.layer.menu.SetLayerScaleMenu;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.TreeItemPropertyEnableCheck;
import com.revolsys.swing.tree.TreeItemRunnable;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Property;

public abstract class AbstractLayer extends AbstractObjectWithProperties
  implements Layer, PropertyChangeListener, PropertyChangeSupportProxy {
  private static final AtomicLong ID_GEN = new AtomicLong();

  static {
    final MenuFactory menu = ObjectTreeModel.getMenu(AbstractLayer.class);

    final EnableCheck hasGeometry = new TreeItemPropertyEnableCheck(
      "hasGeometry");
    menu.addMenuItem("zoom", TreeItemRunnable.createAction("Zoom to Layer",
      "magnifier", hasGeometry, "zoomToLayer"));

    menu.addComponentFactory("scale", new SetLayerScaleMenu(true));
    menu.addComponentFactory("scale", new SetLayerScaleMenu(false));

    menu.addMenuItem(TreeItemRunnable.createAction("Refresh", "arrow_refresh",
      "refresh"));

    menu.addMenuItem("layer", TreeItemRunnable.createAction("Delete Layer",
      "delete", "deleteWithConfirm"));

    menu.addMenuItem("layer", TreeItemRunnable.createAction("Layer Properties",
      "information", "showProperties"));
  }

  protected PropertyChangeListener beanPropertyListener = new BeanPropertyListener(
    this);

  private boolean editable = false;

  private LayerGroup layerGroup;

  private String name;

  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
    this);

  private GeometryFactory geometryFactory;

  private boolean readOnly = false;

  private boolean selectable = true;

  private boolean selectSupported = true;

  private long maximumScale = 0;

  private long minimumScale = Long.MAX_VALUE;

  private boolean visible = true;

  private boolean queryable = true;

  private boolean eventsEnabled = true;

  private boolean querySupported = true;

  private final long id = ID_GEN.incrementAndGet();

  private LayerRenderer<?> renderer;

  private String type;

  public AbstractLayer() {
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
      final JPanel panel = new JPanel(new VerticalLayout());
      tabPanel.addTab("Spatial", panel);

      final JPanel extentPanel = new JPanel();
      extentPanel.setBorder(BorderFactory.createTitledBorder("Extent"));
      final BoundingBox boundingBox = getBoundingBox();
      if (boundingBox == null || boundingBox.isNull()) {
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
      GroupLayoutUtil.makeColumns(extentPanel, 1);
      panel.add(extentPanel);

      final JPanel coordinateSystemPanel = new JPanel();
      coordinateSystemPanel.setBorder(BorderFactory.createTitledBorder("Coordinate System"));
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
          EsriCsWktWriter.toString(esriCoordinateSystem), 30, 80);
        wktTextArea.setEditable(false);
        wktTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        coordinateSystemPanel.add(wktTextArea);

        GroupLayoutUtil.makeColumns(coordinateSystemPanel, 2);
      }
      panel.add(coordinateSystemPanel);

      return panel;
    }
    return null;
  }

  protected ValueField addPropertiesTabGeneral(final TabbedValuePanel tabPanel) {
    final ValueField panel = new ValueField(this);
    tabPanel.addTab("General", panel);

    final JComponent nameField = SwingUtil.addField(panel, this, "name");
    if (nameField instanceof Field) {
      nameField.addPropertyChangeListener("name", this.beanPropertyListener);
    }

    GroupLayoutUtil.makeColumns(panel, 2);
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
    final DefaultSingleCDockable dockable = getProperty("TableView");
    if (dockable != null) {
      // TODO all this should be done by listeners
      dockable.setVisible(false);
    }
    firePropertyChange("deleted", false, true);
    this.eventsEnabled = false;
    if (this.layerGroup != null) {
      this.layerGroup.remove(this);
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

  protected boolean doSaveChanges() {
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

  @Override
  public GeometryFactory getGeometryFactory() {
    if (this.geometryFactory == null && this.layerGroup != null) {
      return this.layerGroup.getGeometryFactory();
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
    return this.layerGroup;
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
    if (this.layerGroup == null) {
      return null;
    } else {
      return this.layerGroup.getProject();
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

  @Override
  public String getType() {
    return this.type;
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
    return this.eventsEnabled;
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
  public boolean isQueryable() {
    return this.querySupported && this.queryable;
  }

  @Override
  public boolean isQuerySupported() {
    return this.querySupported;
  }

  @Override
  public boolean isReadOnly() {
    return this.readOnly;
  }

  @Override
  public boolean isSelectable() {
    return isVisible()
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
    if (isVisible()) {
      final long longScale = (long)scale;
      if (getMinimumScale() >= longScale && longScale >= getMaximumScale()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    if (isEventsEnabled()) {
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
    if (isHasChanges()) {
      return doSaveChanges();
    }
    return true;
  }

  @Override
  public void setEditable(final boolean editable) {
    final boolean old = isEditable();
    this.editable = editable;
    firePropertyChange("editable", old, isEditable());
  }

  public boolean setEventsEnabled(final boolean eventsEnabled) {
    final boolean oldValue = this.eventsEnabled;
    this.eventsEnabled = eventsEnabled;
    return oldValue;
  }

  protected void setGeometryFactory(final GeometryFactory geometryFactory) {
    if (geometryFactory != this.geometryFactory) {
      final GeometryFactory old = this.geometryFactory;
      this.geometryFactory = geometryFactory;
      firePropertyChange("geometryFactory", old, this.geometryFactory);
    }
  }

  @Override
  public void setLayerGroup(final LayerGroup layerGroup) {
    if (this.layerGroup != layerGroup) {
      final LayerGroup old = this.layerGroup;
      if (old != null) {
        removePropertyChangeListener(old);
      }
      this.layerGroup = layerGroup;
      addPropertyChangeListener(layerGroup);
      firePropertyChange("layerGroup", old, layerGroup);
    }
  }

  @Override
  public void setMaximumScale(final long maxScale) {
    this.maximumScale = maxScale;
  }

  @Override
  public void setMinimumScale(final long minScale) {
    this.minimumScale = minScale;
  }

  @Override
  public void setName(final String name) {
    if (!EqualsRegistry.equal(name, this.name)) {
      this.propertyChangeSupport.firePropertyChange("name", this.name, name);
      this.name = name;
    }
  }

  @Override
  public void setProperties(final Map<String, ? extends Object> properties) {
    if (properties == null || !this.getProperties().equals(properties)) {
      this.propertyChangeSupport.firePropertyChange("properties",
        this.getProperties(), properties);
      super.setProperties(properties);
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
        this.propertyChangeSupport.firePropertyChange(event);

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
    Property.removeListener(this.renderer, this);
    this.renderer = renderer;
    Property.addListener(this.renderer, this);
  }

  @Override
  public void setSelectable(final boolean selectable) {
    final boolean oldValue = this.selectable;
    this.selectable = selectable;
    this.propertyChangeSupport.firePropertyChange("selectable", oldValue,
      selectable);
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
    this.propertyChangeSupport.firePropertyChange("visible", oldVisible,
      visible);
  }

  @Override
  public void showProperties() {
    showProperties(null);
  }

  @Override
  public void showProperties(final String tabName) {
    final MapPanel map = MapPanel.get(this);
    if (map != null) {
      final Window window = SwingUtilities.getWindowAncestor(map);
      final TabbedValuePanel panel = createPropertiesPanel();
      panel.setSelectdTab(tabName);
      panel.showDialog(window);
    }
  }

  @Override
  public void showRendererProperties(final LayerRenderer<?> renderer) {
    final MapPanel map = MapPanel.get(this);
    if (map != null) {
      final Window window = SwingUtilities.getWindowAncestor(map);
      final TabbedValuePanel panel = createPropertiesPanel();
      panel.setSelectdTab("Style");
      final DataObjectLayerStylePanel stylePanel = panel.getTab("Style");
      stylePanel.setSelectedRenderer(renderer);
      panel.showDialog(window);
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
    MapSerializerUtil.add(map, "visible", this.visible, true);
    MapSerializerUtil.add(map, "querySupported", this.querySupported, true);
    if (this.querySupported) {
      MapSerializerUtil.add(map, "queryable", this.queryable, true);
    }
    MapSerializerUtil.add(map, "readOnly", this.readOnly, false);
    if (!this.readOnly) {
      MapSerializerUtil.add(map, "editable", this.editable, false);
    }
    if (this.selectSupported) {
      MapSerializerUtil.add(map, "selectable", this.selectable, true);
    }
    MapSerializerUtil.add(map, "selectSupported", this.selectSupported, true);
    MapSerializerUtil.add(map, "maximumScale", this.maximumScale, 0L);
    MapSerializerUtil.add(map, "minimumScale", this.minimumScale,
      Long.MAX_VALUE);
    MapSerializerUtil.add(map, "style", this.renderer);
    final Map<String, Object> properties = (Map<String, Object>)MapSerializerUtil.getValue(getProperties());
    if (properties != null) {
      for (final Entry<String, Object> entry : properties.entrySet()) {
        final String name = entry.getKey();
        if (!map.containsKey(name)) {
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
    final BoundingBox boundingBox = getBoundingBox().convert(geometryFactory)
      .expandPercent(0.1)
      .clipToCoordinateSystem();

    project.setViewBoundingBox(boundingBox);
  }
}
