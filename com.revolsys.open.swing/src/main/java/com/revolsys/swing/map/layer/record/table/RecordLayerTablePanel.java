package com.revolsys.swing.map.layer.record.table;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableCellEditor;

import com.revolsys.collection.map.Maps;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactory;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.record.io.RecordWriterFactory;
import com.revolsys.record.query.Condition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.ConsumerAction;
import com.revolsys.swing.action.RunnableAction;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.action.enablecheck.ObjectPropertyEnableCheck;
import com.revolsys.swing.map.action.AddFileLayerAction;
import com.revolsys.swing.map.form.FieldNamesSetPanel;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.LayerRecordMenu;
import com.revolsys.swing.map.layer.record.SqlLayerFilter;
import com.revolsys.swing.map.layer.record.component.FieldFilterPanel;
import com.revolsys.swing.map.layer.record.table.model.RecordLayerTableModel;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.menu.PopupMenu;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.TableRowCount;
import com.revolsys.swing.table.record.RecordRowTable;
import com.revolsys.swing.table.record.editor.RecordTableCellEditor;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.util.PreferencesUtil;
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

  public RecordLayerTablePanel(final AbstractRecordLayer layer, final RecordLayerTable table,
    final Map<String, Object> config) {
    super(table);
    this.layer = layer;
    this.tableModel = getTableModel();
    final Map<String, Object> pluginConfig = layer.getPluginConfig(AbstractLayer.PLUGIN_TABLE_VIEW);

    table.getTableCellEditor().addMouseListener(this);
    table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

    final LayerRecordMenu menu = this.layer.getRecordMenu();

    final RecordTableCellEditor tableCellEditor = table.getTableCellEditor();
    tableCellEditor.setPopupMenu(menu);

    initToolBar(pluginConfig);

    setPluginConfig(pluginConfig);
    layer.setPluginConfig(AbstractLayer.PLUGIN_TABLE_VIEW, this);
    Property.addListener(layer, this);
    this.tableModel.refresh();
  }

  private void actionExportRecords() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final JFileChooser fileChooser = SwingUtil.newFileChooser("Export Records",
      "com.revolsys.swing.map.table.export", "directory");
    final String defaultFileExtension = PreferencesUtil
      .getUserString("com.revolsys.swing.map.table.export", "fileExtension", "tsv");

    final List<FileNameExtensionFilter> recordFileFilters = new ArrayList<>();
    for (final RecordWriterFactory factory : IoFactory.factories(RecordWriterFactory.class)) {
      if (recordDefinition.hasGeometryField() || factory.isCustomFieldsSupported()) {
        recordFileFilters.add(AddFileLayerAction.newFilter(factory));
      }
    }
    AddFileLayerAction.sortFilters(recordFileFilters);

    fileChooser.setAcceptAllFileFilterUsed(false);
    fileChooser.setSelectedFile(new File(fileChooser.getCurrentDirectory(), this.layer.getName()));
    for (final FileNameExtensionFilter fileFilter : recordFileFilters) {
      fileChooser.addChoosableFileFilter(fileFilter);
      if (Arrays.asList(fileFilter.getExtensions()).contains(defaultFileExtension)) {
        fileChooser.setFileFilter(fileFilter);
      }
    }

    fileChooser.setMultiSelectionEnabled(false);
    final int returnVal = fileChooser.showSaveDialog(SwingUtil.getActiveWindow());
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      final FileNameExtensionFilter fileFilter = (FileNameExtensionFilter)fileChooser
        .getFileFilter();
      File file = fileChooser.getSelectedFile();
      if (file != null) {
        final String fileExtension = FileUtil.getFileNameExtension(file);
        final String expectedExtension = fileFilter.getExtensions()[0];
        if (!fileExtension.equals(expectedExtension)) {
          file = FileUtil.getFileWithExtension(file, expectedExtension);
        }
        final File targetFile = file;
        PreferencesUtil.setUserString("com.revolsys.swing.map.table.export", "fileExtension",
          expectedExtension);
        PreferencesUtil.setUserString("com.revolsys.swing.map.table.export", "directory",
          file.getParent());
        final String description = "Export " + this.layer.getPath() + " to "
          + targetFile.getAbsolutePath();
        Invoke.background(description, () -> {
          this.tableModel.exportRecords(targetFile);
        });
      }
    }
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

  protected JToggleButton addFieldFilterToggleButton(final ToolBar toolBar, final int index,
    final String title, final String icon, final String mode, final EnableCheck enableCheck) {
    final JToggleButton button = toolBar.addToggleButtonTitleIcon(FILTER_FIELD, index, title, icon,
      () -> this.tableModel.setFieldFilterMode(mode));
    this.buttonByMode.put(FILTER_FIELD + "_" + mode, button);
    return button;
  }

  protected JToggleButton addGeometryFilterToggleButton(final ToolBar toolBar, final int index,
    final String title, final String icon, final String mode, final EnableCheck enableCheck) {
    final JToggleButton button = toolBar.addToggleButtonTitleIcon(FILTER_GEOMETRY, index, title,
      icon, () -> this.tableModel.setGeometryFilterMode(mode));
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
      Property.removeListener(this.layer, this);
      this.layer.setPluginConfig(AbstractLayer.PLUGIN_TABLE_VIEW, toMap());
    }
    this.tableModel = null;
    this.layer = null;
    if (this.fieldFilterPanel != null) {
      this.fieldFilterPanel.close();
      this.fieldFilterPanel = null;
    }
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

  protected void initToolBar(final Map<String, Object> pluginConfig) {
    final ToolBar toolBar = getToolBar();

    final RecordDefinition recordDefinition = getRecordDefinition();
    final boolean hasGeometry = recordDefinition.hasGeometryField();

    final MenuFactory layerMenuFactory = MenuFactory.findMenu(this.layer);
    if (layerMenuFactory != null) {
      toolBar.addButtonTitleIcon("menu", "Layer Menu", "menu",
        () -> layerMenuFactory.show(this.layer, this, 10, 10));
    }

    if (hasGeometry) {
      final EnableCheck hasSelectedRecords = new ObjectPropertyEnableCheck(this.layer,
        "hasSelectedRecords");
      toolBar.addButton("layer", "Zoom to Selected", "magnifier_zoom_selected", hasSelectedRecords,
        this.layer::zoomToSelected);
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
    toolBar.addComponent("search", this.fieldFilterPanel);

    toolBar.addButtonTitleIcon("search", "Advanced Search", "filter_edits",
      this.fieldFilterPanel::showAdvancedFilter);

    final EnableCheck hasFilter = new ObjectPropertyEnableCheck(this.tableModel, "hasFilter");

    toolBar.addButton("search", "Clear Search", "filter_delete", hasFilter,
      this.fieldFilterPanel::clear);

    toolBar.addButton("search",
      ConsumerAction.action(Icons.getIconWithBadge("book", "filter"), "Query History", (event) -> {
        final Object source = event.getSource();
        Component component = null;
        if (source instanceof Component) {
          component = (Component)source;
        }
        final PopupMenu queryMenu = new PopupMenu("Query History");
        final MenuFactory factory = queryMenu.getMenu();
        // factory.addMenuItemTitleIcon("default", "Add Bookmark", "add", this,
        // "addZoomBookmark");

        for (final Condition filter : this.tableModel.getFilterHistory()) {
          factory.addMenuItemTitleIcon("bookmark", filter.toString(), null,
            () -> this.fieldFilterPanel.setFilter(filter));
        }
        queryMenu.show(component, 0, 20);
      }));

    // Filter buttons

    final JToggleButton clearFilter = addFieldFilterToggleButton(toolBar, -1, "Show All Records",
      "table_filter", RecordLayerTableModel.MODE_RECORDS_ALL, null);
    clearFilter.doClick();

    final EnableCheck editableEnableCheck = new ObjectPropertyEnableCheck(this.layer, "editable");
    addFieldFilterToggleButton(toolBar, -1, "Show Only Changed Records", "change_table_filter",
      RecordLayerTableModel.MODE_RECORDS_CHANGED, editableEnableCheck);

    addFieldFilterToggleButton(toolBar, -1, "Show Only Selected Records", "filter_selected",
      RecordLayerTableModel.MODE_RECORDS_SELECTED, null);

    if (hasGeometry) {
      final JToggleButton showAllGeometries = addGeometryFilterToggleButton(toolBar, -1,
        "Show All Records ", "world_filter", "all", null);
      showAllGeometries.doClick();

      addGeometryFilterToggleButton(toolBar, -1, "Show Records on Map", "map_filter", "boundingBox",
        null);
    }
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

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    if (source instanceof LayerRecord) {
      repaint();
    } else if (source == this.layer) {
      final String propertyName = event.getPropertyName();
      if (propertyName.endsWith("Changed")) {
        this.tableModel.refresh();
      }
      repaint();
    }
  }

  public void setFieldFilterMode(String mode) {
    if (!Property.hasValue(mode)) {
      mode = this.tableModel.getFieldFilterMode();
    }
    final JToggleButton button = this.buttonByMode.get(FILTER_FIELD + "_" + mode);
    if (button != null) {
      if (!button.isSelected()) {
        button.doClick();
      }
      this.tableModel.setFieldFilterMode(mode);
    }
  }

  public void setGeometryFilterMode(String mode) {
    if (!Property.hasValue(mode)) {
      mode = this.tableModel.getGeometryFilterMode();
    }
    final JToggleButton button = this.buttonByMode.get(FILTER_GEOMETRY + "_" + mode);
    if (button != null) {
      this.tableModel.setGeometryFilterMode(mode);
      if (!button.isSelected()) {
        button.doClick();
      }
    }
  }

  protected void setPluginConfig(final Map<String, Object> config) {
    final Object orderBy = config.get("orderBy");
    if (orderBy instanceof Map) {
      @SuppressWarnings("unchecked")
      final Map<String, Boolean> order = (Map<String, Boolean>)orderBy;
      this.tableModel.setOrderBy(order);
    }
    final String fieldFilterMode = Maps.getString(config, "fieldFilterMode");
    setFieldFilterMode(fieldFilterMode);

    final String geometryFilterMode = Maps.getString(config, "geometryFilterMode");
    setGeometryFilterMode(geometryFilterMode);
  }

  @Override
  protected void showMenu(final MouseEvent e) {
    final LayerRecord record = RecordRowTable.getEventRecord();
    LayerRecordMenu.setEventRecord(record);
    super.showMenu(e);
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = new LinkedHashMap<>();

    final String fieldFilterMode = this.tableModel.getFieldFilterMode();
    MapSerializerUtil.add(map, "fieldFilterMode", fieldFilterMode);

    final String geometryFilterMode = this.tableModel.getGeometryFilterMode();
    MapSerializerUtil.add(map, "geometryFilterMode", geometryFilterMode);

    final Condition condition = this.tableModel.getFilter();
    if (this.fieldFilterPanel != null) {
      MapSerializerUtil.add(map, "searchField", this.fieldFilterPanel.getSearchFieldName());
    }
    if (condition != null) {
      final String sql = condition.toFormattedString();
      final SqlLayerFilter filter = new SqlLayerFilter(this.layer, sql);
      MapSerializerUtil.add(map, "filter", filter);
    }
    MapSerializerUtil.add(map, "orderBy", this.tableModel.getOrderBy());
    return map;
  }
}
