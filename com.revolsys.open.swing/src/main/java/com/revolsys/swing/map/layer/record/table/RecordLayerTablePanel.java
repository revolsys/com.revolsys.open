package com.revolsys.swing.map.layer.record.table;

import java.awt.Component;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

import org.jdesktop.swingx.VerticalLayout;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.record.query.Condition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.Icons;
import com.revolsys.swing.action.ConsumerAction;
import com.revolsys.swing.action.RunnableAction;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.action.enablecheck.ObjectPropertyEnableCheck;
import com.revolsys.swing.component.ProgressMonitor;
import com.revolsys.swing.dnd.ClipboardUtil;
import com.revolsys.swing.dnd.transferable.StringTransferable;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.form.FieldNamesSetPanel;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.LayerRecordMenu;
import com.revolsys.swing.map.layer.record.component.FieldCalculator;
import com.revolsys.swing.map.layer.record.component.FieldFilterPanel;
import com.revolsys.swing.map.layer.record.component.SetRecordsFieldValue;
import com.revolsys.swing.map.layer.record.table.model.RecordLayerTableModel;
import com.revolsys.swing.map.layer.record.table.model.TableRecordsMode;
import com.revolsys.swing.menu.BaseJPopupMenu;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.TableRowCount;
import com.revolsys.swing.table.record.RecordRowTable;
import com.revolsys.swing.table.record.editor.RecordTableCellEditor;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.util.Property;

public class RecordLayerTablePanel extends TablePanel
  implements PropertyChangeListener, MapSerializer {
  public static final String FILTER_FIELD = "filter_field";

  public static final String FILTER_GEOMETRY = "filter_geometry";

  private static final long serialVersionUID = 1L;

  private final Map<String, JToggleButton> buttonByMode = new HashMap<>();

  private FieldFilterPanel fieldFilterPanel;

  private JButton fieldSetsButton;

  private AbstractRecordLayer layer;

  private RecordLayerTableModel tableModel;

  private final PropertyChangeListener viewportListener;

  public RecordLayerTablePanel(final AbstractRecordLayer layer, final RecordLayerTable table,
    final Map<String, Object> config) {
    super(table);
    this.layer = layer;
    this.tableModel = getTableModel();
    Property.addListenerNewValueSource(this.tableModel, "tableRecordsMode",
      this::setTableRecordsMode);
    Property.addListenerNewValueSource(this.tableModel, "geometryFilterMode",
      this::setGeometryFilterMode);
    final Map<String, Object> pluginConfig = layer.getPluginConfig(AbstractLayer.PLUGIN_TABLE_VIEW);

    table.getTableCellEditor().addMouseListener(this);
    table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

    final MenuFactory headerMenu = getHeaderMenu();
    SetRecordsFieldValue.addMenuItem(headerMenu);
    FieldCalculator.addMenuItem(headerMenu);
    headerMenu.addMenuItem("field", "Copy Raw Values", "page_white_copy",
      () -> actionCopyColumnValues(false));
    headerMenu.addMenuItem("field", "Copy Display Values", "page_white_copy",
      () -> actionCopyColumnValues(true));

    final LayerRecordMenu menu = this.layer.getRecordMenu();

    final RecordTableCellEditor tableCellEditor = table.getTableCellEditor();
    tableCellEditor.setPopupMenu(menu::newJPopupMenu);

    newToolBar(pluginConfig);

    setPluginConfig(pluginConfig);
    layer.setPluginConfig(AbstractLayer.PLUGIN_TABLE_VIEW, this);
    Property.addListener(layer, this);
    this.viewportListener = (e) -> {
      if (this.tableModel != null && this.tableModel.isFilterByBoundingBox()) {
        this.tableModel.refresh();
      }
    };
    MapPanel.getMapPanel(layer)
      .getViewport()
      .addPropertyChangeListener("boundingBox", this.viewportListener);
    this.tableModel.refresh();
  }

  private void actionCopyColumnValues(final boolean showDisplayValues) {
    final Consumer<ProgressMonitor> action = monitor -> {
      final int columnIndex = TablePanel.getEventColumn();
      final StringBuilder result = new StringBuilder();
      final Consumer<String> valueAction = value -> {
        result.append(value);
        result.append('\n');
        monitor.addProgress();
      };
      if (showDisplayValues) {
        this.tableModel.forEachColumnDisplayValue(monitor, columnIndex, valueAction);
      } else {
        this.tableModel.forEachColumnValue(monitor, columnIndex, value -> {
          if (value == null) {
            valueAction.accept("");
          } else {
            final String string = DataTypes.toString(value);
            valueAction.accept(string);
          }
        });
      }

      if (!monitor.isCancelled()) {
        ClipboardUtil
          .setContents(new StringTransferable(DataFlavor.stringFlavor, result.toString()));
      }
    };
    final int rowCount = this.tableModel.getRowCount();
    ProgressMonitor.background(this, "Copy Values", "", action, rowCount);
  }

  private void actionExportRecords() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final String title = this.layer.getName();
    final boolean hasGeometryField = recordDefinition.hasGeometryField();
    AbstractRecordLayer.exportRecords(title, hasGeometryField, this.tableModel::exportRecords);
  }

  private void actionShowFieldSetsMenu() {
    final JPopupMenu menu = new JPopupMenu();

    final JMenuItem editMenuItem = RunnableAction.newMenuItem("Edit Field Sets",
      "fields_filter_edit", () -> {
        final String fieldNamesSetName = FieldNamesSetPanel.showDialog(this.layer);
        if (Property.hasValue(fieldNamesSetName)) {
          this.tableModel.setFieldNamesSetName(fieldNamesSetName);
        }
      });
    menu.add(editMenuItem);

    menu.addSeparator();

    final AbstractRecordLayer layer = getLayer();
    final String selectedFieldSetName = layer.getFieldNamesSetName();
    for (final String fieldSetName : layer.getFieldNamesSetNames()) {
      final JCheckBoxMenuItem menuItem = RunnableAction.newCheckBoxMenuItem(fieldSetName,
        () -> this.tableModel.setFieldNamesSetName(fieldSetName));
      if (fieldSetName.equalsIgnoreCase(selectedFieldSetName)) {
        menuItem.setSelected(true);
      }
      menu.add(menuItem);
    }
    MenuFactory.showMenu(menu, this.fieldSetsButton, 10, 10);
  }

  protected JToggleButton addGeometryFilterToggleButton(final ToolBar toolBar, final int index,
    final String title, final String icon, final String mode, final EnableCheck enableCheck) {
    final JToggleButton button = toolBar.addToggleButtonTitleIcon(FILTER_GEOMETRY, index, title,
      icon, enableCheck, () -> setGeometryFilterMode(mode));
    this.buttonByMode.put(FILTER_GEOMETRY + "_" + mode, button);
    return button;
  }

  @Override
  public void close() {
    final RecordLayerTable table = getTable();
    if (table != null) {
      final RecordTableCellEditor tableCellEditor = table.getTableCellEditor();
      tableCellEditor.close();
      table.dispose();
    }
    if (this.layer != null) {
      MapPanel.getMapPanel(this.layer)
        .removePropertyChangeListener("boundingBox", this.viewportListener);
      Property.removeListener(this.layer, this);
      this.layer.setPluginConfig(AbstractLayer.PLUGIN_TABLE_VIEW, toMap());
      this.layer = null;
    }
    this.tableModel = null;
    if (this.fieldFilterPanel != null) {
      this.fieldFilterPanel.close();
      this.fieldFilterPanel = null;
    }
  }

  @Override
  public JPopupMenu getHeaderMenu(final int columnIndex) {
    final JPopupMenu headerMenu = super.getHeaderMenu(columnIndex);
    final String columnName = this.tableModel.getColumnName(columnIndex);
    final JMenuItem menuItem = new JMenuItem();
    final JLabel title = new JLabel(columnName);
    title.setFont(menuItem.getFont().deriveFont(Font.BOLD));
    title.setBackground(menuItem.getBackground());
    title.setHorizontalAlignment(SwingConstants.CENTER);
    title.setHorizontalTextPosition(SwingConstants.CENTER);
    final JPanel labelPanel = new JPanel(new VerticalLayout());
    labelPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    labelPanel.setOpaque(false);
    labelPanel.add(title);
    headerMenu.add(labelPanel, 0);
    headerMenu.add(new JPopupMenu.Separator(), 1);
    return headerMenu;
  }

  @Override
  protected RecordLayerTable getHeaderMenuSource() {
    return getTable();
  }

  public AbstractRecordLayer getLayer() {
    return this.layer;
  }

  @Override
  protected Object getMenuSource() {
    return this.layer;
  }

  public RecordDefinition getRecordDefinition() {
    return this.layer.getRecordDefinition();
  }

  @SuppressWarnings("unchecked")
  @Override
  public RecordLayerTable getTable() {
    return (RecordLayerTable)super.getTable();
  }

  @SuppressWarnings("unchecked")
  @Override
  public RecordLayerTableModel getTableModel() {
    final JTable table = getTable();
    return (RecordLayerTableModel)table.getModel();
  }

  @Override
  public boolean isCurrentCellEditable() {
    return super.isCurrentCellEditable() && this.layer.isCanEditRecords();
  }

  protected boolean isRecordDeleted(final LayerRecord record) {
    return getLayer().isDeleted(record);
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    super.mouseClicked(e);
    if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
      if (isEditing()) {
        final JTable table = getTable();
        final TableCellEditor cellEditor = table.getCellEditor();
        cellEditor.stopCellEditing();
      }
      final LayerRecord record = RecordRowTable.getEventRecord();
      this.layer.showForm(record);
    }
  }

  protected void newToolBar(final Map<String, Object> pluginConfig) {
    final ToolBar toolBar = getToolBar();

    final RecordDefinition recordDefinition = getRecordDefinition();
    final boolean hasGeometry = recordDefinition.hasGeometryField();

    final MenuFactory layerMenuFactory = MenuFactory.findMenu(this.layer);
    if (layerMenuFactory != null) {
      toolBar.addButtonTitleIcon("menu", "Layer Menu", "menu",
        () -> layerMenuFactory.showMenu(this.layer, this, 10, 10));
    }

    if (hasGeometry) {
      final EnableCheck hasSelectedRecords = new ObjectPropertyEnableCheck(this.layer,
        "hasSelectedRecords");
      toolBar.addButton("layer", "Zoom to Selected", "magnifier_zoom_selected", hasSelectedRecords,
        this.layer::zoomToSelected);
      toolBar.addButton("layer", "Pan to Selected", "pan_selected", hasSelectedRecords,
        this.layer::panToSelected);
    }
    final RecordLayerTable table = getTable();
    final TableRowCount tableRowCount = new TableRowCount(table);
    toolBar.addComponent("count", tableRowCount);

    toolBar.addButtonTitleIcon("table", "Refresh", "table_refresh", this.tableModel::refresh);

    toolBar.addButtonTitleIcon("table", "Export Records", "table_save",
      new ObjectPropertyEnableCheck(tableRowCount, "rowCount", 0, true),
      () -> actionExportRecords());

    this.fieldSetsButton = toolBar.addButtonTitleIcon("table", "Field Sets", "fields_filter",
      () -> actionShowFieldSetsMenu());

    this.fieldFilterPanel = new FieldFilterPanel(this, this.tableModel, pluginConfig);
    if (this.fieldFilterPanel.isVisible()) {
      toolBar.addComponent("search", this.fieldFilterPanel);

      toolBar.addButtonTitleIcon("search", "Advanced Search", "filter_edits",
        this.fieldFilterPanel::showAdvancedFilter);

      final EnableCheck hasFilter = new ObjectPropertyEnableCheck(this.tableModel, "hasFilter");

      toolBar.addButton("search", "Clear Search", "filter_delete", hasFilter,
        this.fieldFilterPanel::clear);

      final EnableCheck hasFilterHistory = new ObjectPropertyEnableCheck(this.tableModel,
        "hasFilterHistory");
      toolBar.addButton("search", ConsumerAction.action("Search History",
        Icons.getIconWithBadge("book", "filter"), hasFilterHistory, (event) -> {
          final Object source = event.getSource();
          Component component = null;
          if (source instanceof Component) {
            component = (Component)source;
          }
          final BaseJPopupMenu menu = new BaseJPopupMenu();

          for (final Condition filter : this.tableModel.getFilterHistory()) {
            menu.addMenuItem(filter.toString(), () -> this.fieldFilterPanel.setFilter(filter));
          }
          menu.showMenu(component, 0, 20);
        }));
    }
    // Filter buttons

    boolean first = true;
    for (final TableRecordsMode fieldFilterMode : this.tableModel.getFieldFilterModes()) {
      final String key = fieldFilterMode.getKey();
      final String title = fieldFilterMode.getTitle();
      final Icon icon = fieldFilterMode.getIcon();
      final EnableCheck enableCheck = fieldFilterMode.getEnableCheck();
      final JToggleButton button = toolBar.addToggleButton(FILTER_FIELD, -1, null, title, icon,
        enableCheck, () -> {
          if (this.tableModel != null) {
            this.tableModel.setTableRecordsMode(fieldFilterMode);
          }
        });
      this.buttonByMode.put(FILTER_FIELD + "_" + key, button);
      if (first) {
        button.doClick();
        first = false;
      }
    }

    if (hasGeometry) {
      final JToggleButton showAllGeometries = addGeometryFilterToggleButton(toolBar, -1,
        "Show All Records ", "world_filter", "all", null);
      showAllGeometries.doClick();

      addGeometryFilterToggleButton(toolBar, -1, "Show Records on Map", "map_filter", "boundingBox",
        new ObjectPropertyEnableCheck(this.tableModel, "filterByBoundingBoxSupported"));
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final String propertyName = event.getPropertyName();
    final Object source = event.getSource();
    if (source instanceof LayerRecord) {
      repaint();
    } else if (source == this.layer) {
      if (propertyName.endsWith("Changed")) {
        this.tableModel.refresh();
      } else if ("selectedRecordsByBoundingBox".equals(propertyName)) {
        this.fieldFilterPanel.clear();
      } else {
        repaint();
      }
    }
  }

  public void setFieldFilterMode(final String key) {
    final TableRecordsMode tableRecordsMode = this.tableModel.getTableRecordsMode(key);
    setTableRecordsMode(tableRecordsMode);
  }

  public void setGeometryFilterMode(final String geometryFilterMode) {
    final String mode = this.tableModel.setGeometryFilterMode(geometryFilterMode);
    final JToggleButton button = this.buttonByMode.get(FILTER_GEOMETRY + "_" + mode);
    if (button != null) {
      if (!button.isSelected()) {
        button.doClick();
      }
    }
  }

  protected void setPluginConfig(final Map<String, Object> config) {
    final Object orderBy = config.get("orderBy");
    if (orderBy instanceof Map) {
      @SuppressWarnings("unchecked")
      final Map<CharSequence, Boolean> order = (Map<CharSequence, Boolean>)orderBy;
      this.tableModel.setOrderBy(order);
    }
    final String tableRecordsMode = Maps.getString(config, "fieldFilterMode");
    setFieldFilterMode(tableRecordsMode);

    final String geometryFilterMode = Maps.getString(config, "geometryFilterMode");
    setGeometryFilterMode(geometryFilterMode);
  }

  private void setTableRecordsMode(TableRecordsMode tableRecordsMode) {
    if (!Property.hasValue(tableRecordsMode)) {
      tableRecordsMode = this.tableModel.getTableRecordsMode();
    }
    final JToggleButton button = this.buttonByMode
      .get(FILTER_FIELD + "_" + tableRecordsMode.getKey());
    if (button != null) {
      if (!button.isSelected()) {
        button.doClick();
      }
      if (this.tableModel != null) {
        this.tableModel.setTableRecordsMode(tableRecordsMode);
      }
    }
  }

  @Override
  public MapEx toMap() {
    final MapEx map = new LinkedHashMapEx();

    final String tableRecordsMode = this.tableModel.getTableRecordsMode().getKey();
    addToMap(map, "fieldFilterMode", tableRecordsMode);

    final String geometryFilterMode = this.tableModel.getGeometryFilterMode();
    addToMap(map, "geometryFilterMode", geometryFilterMode);

    if (this.fieldFilterPanel != null) {
      addToMap(map, "searchField", this.fieldFilterPanel.getSearchFieldName());
    }
    addToMap(map, "orderBy", this.tableModel.getOrderBy());
    return map;
  }
}
