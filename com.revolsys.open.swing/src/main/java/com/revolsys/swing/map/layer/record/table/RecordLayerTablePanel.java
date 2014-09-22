package com.revolsys.swing.map.layer.record.table;

import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;

import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.property.DirectionalAttributes;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.swing.action.enablecheck.AndEnableCheck;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.action.enablecheck.InvokeMethodEnableCheck;
import com.revolsys.swing.action.enablecheck.ObjectPropertyEnableCheck;
import com.revolsys.swing.action.enablecheck.OrEnableCheck;
import com.revolsys.swing.map.form.LayerRecordForm;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.component.AttributeFilterPanel;
import com.revolsys.swing.map.layer.record.table.model.RecordLayerTableModel;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.menu.WrappedMenuFactory;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.TableRowCount;
import com.revolsys.swing.table.record.editor.RecordTableCellEditor;
import com.revolsys.swing.table.record.model.RecordRowTableModel;
import com.revolsys.swing.table.record.row.RecordRowPropertyEnableCheck;
import com.revolsys.swing.table.record.row.RecordRowRunnable;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.util.Property;

public class RecordLayerTablePanel extends TablePanel implements
  PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  public static final String FILTER_GEOMETRY = "filter_geometry";

  public static final String FILTER_ATTRIBUTE = "filter_attribute";

  private final AbstractRecordLayer layer;

  private final RecordLayerTableModel tableModel;

  private final JToggleButton selectedButton;

  public RecordLayerTablePanel(final AbstractRecordLayer layer,
    final RecordLayerTable table) {
    super(table);
    this.layer = layer;
    this.tableModel = getTableModel();

    // Right click Menu
    final MenuFactory menu = this.tableModel.getMenu();

    final RecordTableCellEditor tableCellEditor = table.getTableCellEditor();
    tableCellEditor.setPopupMenu(menu);

    table.getTableCellEditor().addMouseListener(this);
    table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    final RecordDefinition recordDefinition = layer.getRecordDefinition();
    final boolean hasGeometry = recordDefinition.getGeometryAttributeIndex() != -1;
    final EnableCheck deletableEnableCheck = new RecordRowPropertyEnableCheck(
      "deletable");

    final EnableCheck modifiedEnableCheck = new RecordRowPropertyEnableCheck(
      "modified");
    final EnableCheck deletedEnableCheck = new RecordRowPropertyEnableCheck(
      "deleted");
    final EnableCheck notEnableCheck = new RecordRowPropertyEnableCheck(
      "deleted", false);
    final OrEnableCheck modifiedOrDeleted = new OrEnableCheck(
      modifiedEnableCheck, deletedEnableCheck);

    final EnableCheck editableEnableCheck = new ObjectPropertyEnableCheck(
      layer, "editable");

    menu.addGroup(0, "default");
    menu.addGroup(1, "record");
    menu.addGroup(2, "dnd");

    final MenuFactory layerMenuFactory = MenuFactory.findMenu(layer);
    if (layerMenuFactory != null) {
      menu.addComponentFactory("default", 0, new WrappedMenuFactory("Layer",
        layerMenuFactory));
    }

    menu.addMenuItemTitleIcon("record", "View/Edit Record", "table_edit",
      notEnableCheck, this, "editRecord");

    if (hasGeometry) {
      menu.addMenuItemTitleIcon("record", "Zoom to Record",
        "magnifier_zoom_selected", this, "zoomToRecord");
    }
    menu.addMenuItemTitleIcon("record", "Delete Record", "table_row_delete",
      deletableEnableCheck, this, "deleteRecord");

    menu.addMenuItem("record", RecordRowRunnable.createAction("Revert Record",
      "arrow_revert", modifiedOrDeleted, "revertChanges"));

    menu.addMenuItem("record", RecordRowRunnable.createAction(
      "Revert Empty Fields", "field_empty_revert", modifiedEnableCheck,
      "revertEmptyFields"));

    menu.addMenuItemTitleIcon("dnd", "Copy Record", "page_copy", this,
      "copyRecord");

    if (hasGeometry) {
      menu.addMenuItemTitleIcon("dnd", "Paste Geometry", "geometry_paste",
        new AndEnableCheck(editableEnableCheck, new InvokeMethodEnableCheck(
          this, "canPasteRecordGeometry")), this, "pasteGeometry");

      final MenuFactory editMenu = new MenuFactory("Edit Record Operations");
      final DataType geometryDataType = recordDefinition.getGeometryAttribute()
        .getType();
      if (geometryDataType == DataTypes.LINE_STRING
        || geometryDataType == DataTypes.MULTI_LINE_STRING) {
        if (DirectionalAttributes.getProperty(recordDefinition)
          .hasDirectionalAttributes()) {
          editMenu.addMenuItemTitleIcon("geometry",
            LayerRecordForm.FLIP_RECORD_NAME, LayerRecordForm.FLIP_RECORD_ICON,
            editableEnableCheck, this, "flipRecordOrientation");
          editMenu.addMenuItemTitleIcon("geometry",
            LayerRecordForm.FLIP_LINE_ORIENTATION_NAME,
            LayerRecordForm.FLIP_LINE_ORIENTATION_ICON, editableEnableCheck,
            this, "flipLineOrientation");
          editMenu.addMenuItemTitleIcon("geometry",
            LayerRecordForm.FLIP_FIELDS_NAME, LayerRecordForm.FLIP_FIELDS_ICON,
            editableEnableCheck, this, "flipFields");
        } else {
          editMenu.addMenuItemTitleIcon("geometry", "Flip Line Orientation",
            "flip_line", editableEnableCheck, this, "flipLineOrientation");
        }
      }
      menu.addComponentFactory("record", 2, editMenu);
    }

    // Toolbar
    final ToolBar toolBar = getToolBar();

    if (layerMenuFactory != null) {
      toolBar.addButtonTitleIcon("menu", "Layer Menu", "menu",
        layerMenuFactory, "show", layer, this, 10, 10);
    }
    toolBar.addComponent("count", new TableRowCount(this.tableModel));

    toolBar.addButtonTitleIcon("table", "Refresh", "table_refresh", this,
      "refresh");

    final AttributeFilterPanel attributeFilterPanel = new AttributeFilterPanel(
      this, this.tableModel);
    toolBar.addComponent("search", attributeFilterPanel);

    toolBar.addButtonTitleIcon("search", "Advanced Search", "filter_edits",
      attributeFilterPanel, "showAdvancedFilter");

    final EnableCheck hasFilter = new ObjectPropertyEnableCheck(
      this.tableModel, "hasFilter");

    toolBar.addButton("search", "Clear Search", "filter_delete", hasFilter,
      attributeFilterPanel, "clear");

    // Filter buttons

    final JToggleButton clearFilter = toolBar.addToggleButtonTitleIcon(
      FILTER_ATTRIBUTE, -1, "Show All Records", "table_filter",
      this.tableModel, "setAttributeFilterMode", RecordLayerTableModel.MODE_ALL);
    clearFilter.doClick();

    toolBar.addToggleButton(FILTER_ATTRIBUTE, -1, "Show Only Changed Records",
      "change_table_filter", editableEnableCheck, this.tableModel,
      "setAttributeFilterMode", RecordLayerTableModel.MODE_EDITS);

    this.selectedButton = toolBar.addToggleButton(FILTER_ATTRIBUTE, -1,
      "Show Only Selected Records", "filter_selected", null, this.tableModel,
      "setAttributeFilterMode", RecordLayerTableModel.MODE_SELECTED);

    if (hasGeometry) {
      final JToggleButton showAllGeometries = toolBar.addToggleButtonTitleIcon(
        FILTER_GEOMETRY, -1, "Show All Records ", "world_filter",
        this.tableModel, "setFilterByBoundingBox", false);
      showAllGeometries.doClick();

      toolBar.addToggleButtonTitleIcon(FILTER_GEOMETRY, -1,
        "Show Records on Map", "map_filter", this.tableModel,
        "setFilterByBoundingBox", true);
    }
    Property.addListener(layer, this);
  }

  public boolean canPasteRecordGeometry() {
    final LayerRecord record = getEventRowObject();
    return this.layer.canPasteRecordGeometry(record);
  }

  public void copyRecord() {
    final LayerRecord record = getEventRowObject();
    this.layer.copyRecordsToClipboard(Collections.singletonList(record));
  }

  public void deleteRecord() {
    final LayerRecord object = getEventRowObject();
    this.layer.deleteRecords(object);
  }

  public void editRecord() {
    final LayerRecord object = getEventRowObject();
    if (object != null && !object.isDeleted()) {
      this.layer.showForm(object);
    }
  }

  public void flipFields() {
    final LayerRecord record = getEventRowObject();
    final DirectionalAttributes property = DirectionalAttributes.getProperty(record);
    property.reverseAttributes(record);
  }

  public void flipLineOrientation() {
    final LayerRecord record = getEventRowObject();
    final DirectionalAttributes property = DirectionalAttributes.getProperty(record);
    property.reverseGeometry(record);
  }

  public void flipRecordOrientation() {
    final LayerRecord record = getEventRowObject();
    DirectionalAttributes.reverse(record);
  }

  public Collection<? extends String> getColumnNames() {
    return this.layer.getFieldNamesSet();
  }

  protected LayerRecord getEventRowObject() {
    final RecordRowTableModel model = getTableModel();
    final int row = getEventRow();
    if (row > -1) {
      final LayerRecord object = model.getRecord(row);
      return object;
    } else {
      return null;
    }
  }

  public AbstractRecordLayer getLayer() {
    return this.layer;
  }

  @Override
  protected Object getMenuSource() {
    return this.layer;
  }

  @SuppressWarnings("unchecked")
  @Override
  public RecordLayerTableModel getTableModel() {
    final JTable table = getTable();
    return (RecordLayerTableModel)table.getModel();
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
      editRecord();
    }
  }

  public void pasteGeometry() {
    final LayerRecord record = getEventRowObject();
    this.layer.pasteRecordGeometry(record);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    if (source instanceof LayerRecord) {
      repaint();
    } else if (source == this.layer) {
      final String propertyName = event.getPropertyName();
      if ("recordsChanged".equals(propertyName)) {
        this.tableModel.refresh();
      }
      repaint();
    }
  }

  public void refresh() {
    this.tableModel.refresh();
  }

  @Override
  public void removeNotify() {
    super.removeNotify();
    Property.removeListener(this.layer, this);
  }

  public void setAttributeFilterMode(final String mode) {
    if (RecordLayerTableModel.MODE_SELECTED.equals(mode)) {
      this.selectedButton.setSelected(true);
      this.selectedButton.doClick();
    }
  }

  public void zoomToRecord() {
    final Record object = getEventRowObject();
    final Project project = this.layer.getProject();
    final Geometry geometry = object.getGeometryValue();
    if (geometry != null) {
      final GeometryFactory geometryFactory = project.getGeometryFactory();
      final BoundingBox boundingBox = geometry.getBoundingBox()
        .convert(geometryFactory)
        .expandPercent(0.1);
      project.setViewBoundingBox(boundingBox);
    }
  }
}
