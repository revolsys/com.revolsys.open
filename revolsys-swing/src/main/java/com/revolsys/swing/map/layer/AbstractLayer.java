package com.revolsys.swing.map.layer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.TextArea;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
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
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.ScrollableSizeHint;
import org.jdesktop.swingx.VerticalLayout;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.logging.Logs;
import org.jeometry.common.number.Doubles;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.revolsys.beans.KeyedPropertyChangeEvent;
import com.revolsys.beans.PropertyChangeSupport;
import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.collection.EmptyReference;
import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.MapSerializerMap;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactory;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.swing.Borders;
import com.revolsys.swing.Dialogs;
import com.revolsys.swing.Icons;
import com.revolsys.swing.Panels;
import com.revolsys.swing.RsSwingServiceInitializer;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.TabbedValuePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.listener.BeanPropertyListener;
import com.revolsys.swing.map.ProjectFrame;
import com.revolsys.swing.map.component.GeometryFactoryField;
import com.revolsys.swing.map.layer.menu.TreeItemScaleMenu;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.preferences.PreferenceFields;
import com.revolsys.swing.table.NumberTableCellRenderer;
import com.revolsys.swing.tree.TreeNodes;
import com.revolsys.swing.tree.node.file.PathTreeNode;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.PreferenceKey;
import com.revolsys.util.Preferences;
import com.revolsys.util.Property;
import com.revolsys.util.ToolTipProxy;
import com.revolsys.value.ThreadBooleanValue;

public abstract class AbstractLayer extends BaseObjectWithProperties
  implements Layer, PropertyChangeListener, PropertyChangeSupportProxy, ToolTipProxy {
  private class LayerSync {
  }

  public class PanelComponentHolder extends BasePanel {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public PanelComponentHolder() {
      super(new BorderLayout());
    }

    public Component getPanel() {
      if (getComponentCount() == 0) {
        return this;
      } else {
        return getComponent(0);
      }
    }

    private void setPanel(final Component component) {
      add(component, BorderLayout.CENTER);
    }

  }

  private static final AtomicLong ID_GEN = new AtomicLong();

  public static final String PLUGIN_TABLE_VIEW = "tableView";

  public static final String PREFERENCE_PATH = "/com/revolsys/gis/layer";

  public static final PreferenceKey PREFERENCE_NEW_LAYERS_SHOW_TABLE_VIEW = new PreferenceKey(
    PREFERENCE_PATH, "newLayersShowTableView", DataTypes.BOOLEAN, false)//
      .setCategoryTitle("Layers");

  public static final PreferenceKey PREFERENCE_NEW_LAYERS_VISIBLE = new PreferenceKey(
    PREFERENCE_PATH, "newLayersVisible", DataTypes.BOOLEAN, false)//
      .setCategoryTitle("Layers");

  static {
    MenuFactory.addMenuInitializer(AbstractLayer.class, menu -> {
      menu.addMenuItem("zoom", -1, "Zoom to Layer", "magnifier",
        AbstractLayer::isZoomToLayerEnabled, AbstractLayer::zoomToLayer, true);

      final Predicate<AbstractLayer> hasGeometry = AbstractLayer::isHasGeometry;
      menu.addComponentFactory("scale", new TreeItemScaleMenu<>(true, hasGeometry,
        AbstractLayer::getMinimumScale, AbstractLayer::setMinimumScale));
      menu.addComponentFactory("scale", new TreeItemScaleMenu<>(false, hasGeometry,
        AbstractLayer::getMaximumScale, AbstractLayer::setMaximumScale));

      final Predicate<AbstractLayer> exists = AbstractLayer::isExists;

      menu.<AbstractLayer> addMenuItem("refresh", "Refresh", "arrow_refresh",
        AbstractLayer::refreshAll, true);

      menu.<AbstractLayer> addMenuItem("layer", "Delete", "delete",
        AbstractLayer::deleteWithConfirm, false);

      menu.<AbstractLayer> addMenuItem("layer", -1, "Layer Properties", "information", exists,
        AbstractLayer::showProperties, false);

      PreferenceFields.addField("com.revolsys.gis", PREFERENCE_NEW_LAYERS_VISIBLE);
      PreferenceFields.addField("com.revolsys.gis", PREFERENCE_NEW_LAYERS_SHOW_TABLE_VIEW);
    });
  }

  public static boolean isShowNewLayerTableView() {
    return Preferences.getValue("com.revolsys.gis", PREFERENCE_NEW_LAYERS_SHOW_TABLE_VIEW);
  }

  public static void menuItemPathAddLayer(final String menuGroup, final String menuName,
    final String iconName, final Class<? extends IoFactory> factoryClass) {
    final EnableCheck enableCheck = RsSwingServiceInitializer.enableCheck(factoryClass);
    TreeNodes.addMenuItem(PathTreeNode.MENU, menuGroup, menuName, (final PathTreeNode node) -> {
      final URL url = node.getUrl();
      final Project project = Project.get();
      project.openFile(url);
    })
      .setVisibleCheck(enableCheck) //
      .setIconName(iconName, "add");
  }

  private String errorMessage;

  private boolean deleted = false;

  private boolean open = false;

  protected PropertyChangeListener beanPropertyListener = new BeanPropertyListener(this);

  private BoundingBox boundingBox = BoundingBox.empty();

  private boolean editable = false;

  private final ThreadBooleanValue eventsEnabled = new ThreadBooleanValue(true);

  private boolean exists = true;

  private GeometryFactory geometryFactory = GeometryFactory.DEFAULT_2D;

  private Icon icon = Icons.getIcon("map");

  private final long id = ID_GEN.incrementAndGet();

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

  private final LayerSync sync;

  private String type;

  private MenuFactory menu;

  private boolean visible = Preferences.getValue("com.revolsys.gis", PREFERENCE_NEW_LAYERS_VISIBLE);

  private GeometryFactory selectedGeometryFactory;

  protected AbstractLayer(final String type) {
    this.sync = new LayerSync();
    this.type = type;
  }

  @Override
  public void activatePanelComponent(final Component component, final Map<String, Object> config) {
    setProperty("bottomTabOpen", config);
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

  public void clearPluginConfig(final String pluginName) {
    this.pluginConfigByName.remove(pluginName);
  }

  @Override
  public AbstractLayer clone() {
    final MapEx config = toMap();
    return MapObjectFactory.toObject(config);
  }

  @Override
  public int compareTo(final Layer layer) {
    return getName().compareTo(layer.getName());
  }

  @Override
  public void delete() {
    this.deleted = true;
    final ProjectFrame projectFrame = ProjectFrame.get(this);
    if (projectFrame != null) {
      projectFrame.removeBottomTab(this);
    }
    setExists(false);
    this.beanPropertyListener = null;

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
    final int confirm = Dialogs.showConfirmDialog(
      "Delete the layer and any child layers? This action cannot be undone.", "Delete Layer",
      JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
    if (confirm == JOptionPane.YES_OPTION) {
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
    final int coordinateSystemId = newGeometryFactory.getHorizontalCoordinateSystemId();
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
      return geometryFactory.bboxEmpty();
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
  public synchronized MenuFactory getMenu() {
    if (this.menu == null) {
      final Class<? extends AbstractLayer> clazz = getClass();
      final MenuFactory parentMenu = MenuFactory.getMenu(clazz);
      final String name = getName();
      this.menu = new MenuFactory(name, parentMenu);
    }
    return this.menu;
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
      return geometryFactory.bboxEmpty();
    }
  }

  public GeometryFactory getSelectedGeometryFactory() {
    return this.selectedGeometryFactory;
  }

  protected String getSettingsFileName() {
    final String name = getName();
    return FileUtil.getSafeFileName(name) + ".rgobject";
  }

  public Object getSync() {
    return this.sync;
  }

  @Override
  public String getToolTip() {
    return this.errorMessage;
  }

  @Override
  public String getType() {
    return this.type;
  }

  @Override
  public final synchronized void initialize() {
    if (!isInitialized()) {
      initializeForce();
    }
  }

  protected boolean initializeDo() {
    initializeMenus();
    return true;
  }

  protected synchronized void initializeForce() {
    try {
      final boolean exists;
      try (
        BaseCloseable eventsDisabled = eventsDisabled()) {
        exists = initializeDo();
      }
      setExists(exists);
    } catch (final RuntimeException e) {
      Logs.error(this, getPath() + ": Unable to initialize layer", e);
      setExists(false);
    } finally {
      setInitialized(true);
    }
    if (isExists()) {
      initializePost();
      if (Property.getBoolean(this, "showTableView")) {
        Invoke.later(this::showTableView);
      }
    }
  }

  protected void initializeMenuExpressions(final List<String> menuInitializerExpressions) {
    for (final String menuInitializerExpression : getProperty("menuInitializerExpressions",
      Collections.<String> emptyList())) {
      if (Property.hasValue(menuInitializerExpression)) {
        if (!menuInitializerExpressions.contains(menuInitializerExpression)) {
          menuInitializerExpressions.add(menuInitializerExpression);
        }
      }
    }
  }

  protected void initializeMenus() {
    final List<String> menuInitializerExpressions = new ArrayList<>();
    initializeMenuExpressions(menuInitializerExpressions);

    final MenuFactory layerMenu = getMenu();
    final EvaluationContext context = initializeMenusContext(layerMenu);

    for (final String menuFactoryExpression : menuInitializerExpressions) {
      try {
        final SpelExpressionParser parser = new SpelExpressionParser();
        final Expression expression = parser.parseExpression(menuFactoryExpression);

        expression.getValue(context, Void.class);
      } catch (final Throwable e) {
        Logs.error(this, "Unable to create menu for " + this, e);
      }
    }
  }

  protected EvaluationContext initializeMenusContext(final MenuFactory layerMenu) {
    final EvaluationContext context = new StandardEvaluationContext(this);
    context.setVariable("layerMenu", layerMenu);
    return context;
  }

  protected void initializePost() {
  }

  @Override
  public boolean isClonable() {
    return false;
  }

  @Override
  public boolean isDeleted() {
    if (this.deleted) {
      return true;
    } else {
      final LayerGroup parent = getLayerGroup();
      if (parent == null) {
        return false;
      } else {
        return parent.isDeleted();
      }
    }
  }

  @Override
  public boolean isEditable() {
    return this.editable;
  }

  @Override
  public boolean isEditable(final double scale) {
    return isVisible(scale) && isEditable() && !isReadOnly();
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
    if (isInitialized()) {
      if (isExists()) {
        return newTableViewComponent(config);
      } else {
        return new PanelComponentHolder();
      }
    } else {
      final PanelComponentHolder basePanel = new PanelComponentHolder();
      addPropertyChangeListener("initialized", (event) -> {
        if (isInitialized() && isExists()) {
          Invoke.later(() -> {
            final Component tableViewComponent = newTableViewComponent(config);
            if (tableViewComponent != null) {
              basePanel.setPanel(tableViewComponent);
              removePropertyChangeListener("initialized", this);
            }
          });
        }
      });
      if (isInitialized()) {
        firePropertyChange("initialized", false, true);
      }
      return basePanel;
    }
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
      extentPanel.setLayout(new BorderLayout());
      final BoundingBox boundingBox = getBoundingBox();
      if (boundingBox == null || boundingBox.isEmpty()) {
        extentPanel.add(new JLabel("Unknown"), BorderLayout.CENTER);

      } else {
        final double minX = boundingBox.getMinX();
        final double minY = boundingBox.getMinY();
        final double maxX = boundingBox.getMaxX();
        final double maxY = boundingBox.getMaxY();
        final String units = " " + boundingBox.getUnit().toString();
        final JLabel extentLabel = new JLabel("<html><table cellspacing=\"3\" style=\"margin:0px\">"
          + "<tr><td>&nbsp;</td><th style=\"text-align:left\">Top:</th><td style=\"text-align:right\">"
          + Doubles.toString(maxY) + units + "</td><td>&nbsp;</td></tr><tr>" + "<td><b>Left</b>: "
          + Doubles.toString(minX) + units + "</td><td>&nbsp;</td><td>&nbsp;</td>"
          + "<td><b>Right</b>: " + Doubles.toString(maxX) + units + "</td></tr>"
          + "<tr><td>&nbsp;</td><th>Bottom:</th><td style=\"text-align:right\">"
          + Doubles.toString(minY) + units + "</td><td>&nbsp;</td></tr><tr>"
          + "</tr></table></html>");
        extentLabel.setFont(SwingUtil.FONT);
        extentPanel.add(extentLabel, BorderLayout.CENTER);

        final int boundingBoxAxisCount = boundingBox.getAxisCount();
        final DefaultTableModel boundingBoxTableModel = new DefaultTableModel(new Object[] {
          "AXIS", "MIN", "MAX"
        }, 0);
        boundingBoxTableModel.addRow(new Object[] {
          "X", minX, maxX
        });
        boundingBoxTableModel.addRow(new Object[] {
          "Y", minY, maxY
        });
        if (boundingBoxAxisCount > 2) {
          boundingBoxTableModel.addRow(new Object[] {
            "Z", boundingBox.getMinZ(), boundingBox.getMaxZ()
          });
        }
        final JXTable boundingBoxTable = new JXTable(boundingBoxTableModel);
        boundingBoxTable.setVisibleRowCount(3);
        boundingBoxTable.setDefaultEditor(Object.class, null);
        boundingBoxTable.setDefaultRenderer(Object.class, new NumberTableCellRenderer());
        final JScrollPane boundingBoxScroll = new JScrollPane(boundingBoxTable);
        extentPanel.add(boundingBoxScroll, BorderLayout.EAST);
        boundingBoxTable.getColumnExt(0).setMaxWidth(31);
      }

      panel.add(extentPanel);

      final JPanel coordinateSystemPanel = Panels.titledTransparent("Coordinate System");
      if (!geometryFactory.isHasHorizontalCoordinateSystem()) {
        coordinateSystemPanel.add(new JLabel("Unknown"));
      } else {
        final int axisCount = geometryFactory.getAxisCount();
        SwingUtil.addLabelledReadOnlyTextField(coordinateSystemPanel, "ID",
          geometryFactory.getHorizontalCoordinateSystemId(), 10);
        SwingUtil.addLabelledReadOnlyTextField(coordinateSystemPanel, "axisCount", axisCount, 10);

        final double scaleX = geometryFactory.getScaleX();
        if (scaleX > 0) {
          SwingUtil.addLabelledReadOnlyTextField(coordinateSystemPanel, "scaleX", scaleX, 10);
        } else {
          SwingUtil.addLabelledReadOnlyTextField(coordinateSystemPanel, "scaleX", "Floating", 10);
        }
        final double scaleY = geometryFactory.getScaleXY();
        if (scaleY > 0) {
          SwingUtil.addLabelledReadOnlyTextField(coordinateSystemPanel, "scaleY", scaleY, 10);
        } else {
          SwingUtil.addLabelledReadOnlyTextField(coordinateSystemPanel, "scaleY", "Floating", 10);
        }

        if (axisCount > 2) {
          final double scaleZ = geometryFactory.getScaleZ();
          if (scaleZ > 0) {
            SwingUtil.addLabelledReadOnlyTextField(coordinateSystemPanel, "scaleZ", scaleZ, 10);
          } else {
            SwingUtil.addLabelledReadOnlyTextField(coordinateSystemPanel, "scaleZ", "Floating", 10);
          }
        }

        SwingUtil.addLabel(coordinateSystemPanel, "ESRI WKT");
        final String wktFormatted = geometryFactory.toWktCsFormatted();
        final TextArea wktTextArea = new TextArea(wktFormatted, 10, 80);
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

  public void redraw() {
    firePropertyChange("redraw", false, true);
  }

  @Override
  public final void refresh() {
    Invoke.background("Refresh Layer " + getName(), () -> {
      refreshBackground();
    });
  }

  @Override
  public final void refreshAll() {
    if (isInitialized() && isExists()) {
      try {
        refreshAllDo();
      } catch (final Throwable e) {
        Logs.error(this, "Unable to refresh layer: " + getName(), e);
      } finally {
        refreshPostDo();
      }
    } else {
      initializeForce();
    }
  }

  protected void refreshAllDo() {
    refreshDo();
  }

  protected final void refreshBackground() {
    try {
      refreshDo();
    } catch (final Throwable e) {
      Logs.error(this, "Unable to refresh layer: " + getName(), e);
    } finally {
      refreshPostDo();
    }
  }

  protected void refreshDo() {
  }

  protected void refreshPostDo() {
    firePropertyChange("refresh", false, true);
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
    writeToFile(settingsFile);
    return true;
  }

  protected void setBoundingBox(final BoundingBox boundingBox) {
    if (boundingBox.isEmpty()) {
      this.boundingBox = this.geometryFactory.getAreaBoundingBox();
    } else if (boundingBox.isHasHorizontalCoordinateSystem()
      || !isHasHorizontalCoordinateSystem()) {
      this.boundingBox = boundingBox;
    } else {
      this.boundingBox = getGeometryFactory().newBoundingBox(boundingBox);
    }
  }

  @Override
  public void setEditable(final boolean editable) {
    final boolean old = isEditable();
    this.editable = editable;
    firePropertyChange("editable", old, isEditable());
  }

  public void setExists(final boolean exists) {
    if (exists) {
      this.errorMessage = null;
    }
    this.exists = exists;
    firePropertyChange("exists", !this.exists, this.exists);
  }

  public final void setGeometryFactory(final GeometryFactory geometryFactory) {
    final GeometryFactory oldGeometryFactory = this.geometryFactory;
    final GeometryFactory newGeometryFactory = setGeometryFactoryDo(geometryFactory);
    if (newGeometryFactory != null) {
      fireGeometryFactoryChanged(oldGeometryFactory, newGeometryFactory);
    }
  }

  protected GeometryFactory setGeometryFactoryDo(final GeometryFactory geometryFactory) {
    if (geometryFactory == null) {
      return null;
    } else if (geometryFactory.equals(this.geometryFactory)) {
      return null;
    } else {
      this.geometryFactory = geometryFactory;
      final BoundingBox boundingBox = getBoundingBox();
      if (Property.isEmpty(boundingBox)) {
        setBoundingBox(geometryFactory.getAreaBoundingBox());
      } else if (!boundingBox.getGeometryFactory().isHasHorizontalCoordinateSystem()
        && geometryFactory.isHasHorizontalCoordinateSystem()) {
        setBoundingBox(boundingBox.bboxToCs(geometryFactory));
      }
      return geometryFactory;
    }
  }

  protected final GeometryFactory setGeometryFactoryPrompt(final GeometryFactory geometryFactory) {
    // Set the geometry factory even if it has no coordinate system
    setGeometryFactory(geometryFactory);
    // Then request the user to select the coordinate system
    if (geometryFactory == null || !geometryFactory.isHasHorizontalCoordinateSystem()) {
      GeometryFactory referenceGeometryFactory;
      if (this.selectedGeometryFactory == null) {
        referenceGeometryFactory = geometryFactory;
      } else {
        referenceGeometryFactory = geometryFactory
          .convertCoordinateSystem(this.selectedGeometryFactory);
      }
      final String title = "Layer: " + getPath();
      GeometryFactoryField.promptGeometryFactory(title, referenceGeometryFactory, factory -> {
        final GeometryFactory selectedGeometryFactory = referenceGeometryFactory
          .convertCoordinateSystem(factory);
        this.selectedGeometryFactory = selectedGeometryFactory;
        setGeometryFactory(selectedGeometryFactory);
      });
    }
    return getGeometryFactory();
  }

  public void setIcon(final Icon icon) {
    final Object oldValue = this.icon;
    this.icon = icon;
    try (
      final BaseCloseable eventsEnabled = eventsEnabled()) {
      firePropertyChange("icon", oldValue, icon);
    }
  }

  public void setIcon(final String iconName) {
    final Icon icon = Icons.getIcon(iconName);
    setIcon(icon);
  }

  protected void setInitialized(final boolean initialized) {
    this.initialized = initialized;
    firePropertyChange("initialized", !initialized, this.initialized);
  }

  @Override
  public void setLayerGroup(final LayerGroup layerGroup) {
    final LayerGroup old = getLayerGroup();
    if (old != layerGroup) {
      final String oldPath = getPath();
      if (old != null) {
        Property.removeListener(this, old);
      }
      this.layerGroup = new WeakReference<>(layerGroup);
      Property.addListener(this, layerGroup);
      try (
        final BaseCloseable eventsEnabled = eventsEnabled()) {
        firePropertyChange("layerGroup", old, layerGroup);
        firePropertyChange("path", oldPath, getPath());
      }
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
    final String oldPath = getPath();
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
    try (
      final BaseCloseable eventsEnabled = eventsEnabled()) {
      firePropertyChange("name", oldValue, this.name);
      firePropertyChange("path", oldPath, getPath());
    }
  }

  public boolean setNotExists(final String errorMessage) {
    this.errorMessage = errorMessage;
    setExists(false);
    return false;
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
    if (properties == null || !getProperties().equals(properties)) {
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

  public void setSelectedGeometryFactory(final GeometryFactory selectedGeometryFactory) {
    this.selectedGeometryFactory = selectedGeometryFactory;
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
    if (this.exists) {
      if (checkShowProperties()) {
        try {
          final TabbedValuePanel panel = newPropertiesPanel();
          panel.setSelectdTab(tabName);
          panel.showDialog();
          refresh();
        } finally {
          removeProperty("INTERNAL_PROPERTIES_VISIBLE");
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
  public JsonObject toMap() {
    final JsonObject map = newMapTree(this.type);
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
    addToMap(map, "selectedGeometryFactory", this.selectedGeometryFactory);
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
      final BoundingBox boundingBox = layerBoundingBox.bboxEditor() //
        .setGeometryFactory(geometryFactory) //
        .clipToCoordinateSystem() //
        .expandPercent(0.1) //
        .newBoundingBox();
      project.setViewBoundingBox(boundingBox);
    }
  }
}
