package com.revolsys.swing.map.table;

import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JTable;
import javax.swing.JToggleButton;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.action.enablecheck.ObjectPropertyEnableCheck;
import com.revolsys.swing.action.enablecheck.OrEnableCheck;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.TableRowCount;
import com.revolsys.swing.table.dataobject.row.DataObjectRowPropertyEnableCheck;
import com.revolsys.swing.table.dataobject.row.DataObjectRowRunnable;
import com.revolsys.swing.table.dataobject.row.DataObjectRowTableModel;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.swing.tree.ObjectTree;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.vividsolutions.jts.geom.Geometry;

public class DataObjectLayerTablePanel extends TablePanel implements
  PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  public static final String FILTER_GEOMETRY = "filter_geometry";

  public static final String FILTER_ATTRIBUTE = "filter_attribute";

  private final DataObjectLayer layer;

  private final DataObjectLayerTableModel tableModel;

  private final JToggleButton selectedButton;

  public DataObjectLayerTablePanel(final DataObjectLayer layer,
    final JTable table) {
    super(table);
    this.layer = layer;
    this.tableModel = getTableModel();
    final MenuFactory menu = getMenu();
    final DataObjectMetaData metaData = layer.getMetaData();
    final boolean hasGeometry = metaData.getGeometryAttributeIndex() != -1;
    if (hasGeometry) {
      menu.addMenuItemTitleIcon("zoom", "Zoom to Record",
        "magnifier_zoom_selected", this, "zoomToRecord");
    }
    final EnableCheck deletableEnableCheck = new DataObjectRowPropertyEnableCheck(
      "deletable");

    final EnableCheck modifiedEnableCheck = new DataObjectRowPropertyEnableCheck(
      "modified");
    final EnableCheck deletedEnableCheck = new DataObjectRowPropertyEnableCheck(
      "deleted");
    final EnableCheck notEnableCheck = new DataObjectRowPropertyEnableCheck(
      "deleted", false);
    final OrEnableCheck modifiedOrDeleted = new OrEnableCheck(
      modifiedEnableCheck, deletedEnableCheck);

    menu.addMenuItemTitleIcon("record", "View/Edit Record", "table_edit",
      notEnableCheck, this, "editRecord");

    menu.addMenuItemTitleIcon("record", "Delete Record", "table_row_delete",
      deletableEnableCheck, this, "deleteRecord");

    menu.addMenuItem("record", DataObjectRowRunnable.createAction(
      "Revert Record", "arrow_revert", modifiedOrDeleted, "revertChanges"));

    final ToolBar toolBar = getToolBar();

    final MenuFactory menuFactory = ObjectTreeModel.findMenu(layer);
    if (menuFactory != null) {
      toolBar.addButtonTitleIcon("menu", "Layer Menu", "menu",
        ObjectTree.class, "showMenu", menuFactory, layer, this, 10, 10);
    }
    toolBar.addComponent("count", new TableRowCount(this.tableModel));

    final AttributeFilterPanel attributeFilterPanel = new AttributeFilterPanel(
      this);
    toolBar.addComponent("search", attributeFilterPanel);

    toolBar.addButtonTitleIcon("search", "Advanced Filter", "filter_edits",
      attributeFilterPanel, "showAdvancedFilter");

    toolBar.addButtonTitleIcon("search", "Clear Search", "filter_delete",
      attributeFilterPanel, "clear");

    // Filter buttons

    final JToggleButton clearFilter = toolBar.addToggleButtonTitleIcon(
      FILTER_ATTRIBUTE, -1, "Show All Records", "table_filter",
      this.tableModel, "setAttributeFilterMode",
      DataObjectLayerTableModel.MODE_ALL);
    clearFilter.doClick();

    final EnableCheck editableEnableCheck = new ObjectPropertyEnableCheck(
      layer, "editable");
    toolBar.addToggleButton(FILTER_ATTRIBUTE, -1, "Show Only Changed Records",
      "change_table_filter", editableEnableCheck, this.tableModel,
      "setAttributeFilterMode", DataObjectLayerTableModel.MODE_EDITS);

    this.selectedButton = toolBar.addToggleButton(FILTER_ATTRIBUTE, -1,
      "Show Only Selected Records", "filter_selected", null, this.tableModel,
      "setAttributeFilterMode", DataObjectLayerTableModel.MODE_SELECTED);

    if (hasGeometry) {

      final JToggleButton showAllGeometries = toolBar.addToggleButtonTitleIcon(
        FILTER_GEOMETRY, -1, "Show All Records ", "world_filter",
        this.tableModel, "setFilterByBoundingBox", false);
      showAllGeometries.doClick();

      toolBar.addToggleButtonTitleIcon(FILTER_GEOMETRY, -1,
        "Show Records on Map", "map_filter", this.tableModel,
        "setFilterByBoundingBox", true);
    }
    layer.addPropertyChangeListener(this);
  }

  public void deleteRecord() {
    final LayerDataObject object = getEventRowObject();
    this.layer.deleteRecords(object);
  }

  public void editRecord() {
    final LayerDataObject object = getEventRowObject();
    if (object != null && !object.isDeleted()) {
      this.layer.showForm(object);
    }
  }

  protected LayerDataObject getEventRowObject() {
    final DataObjectRowTableModel model = getTableModel();
    final int row = getEventRow();
    final LayerDataObject object = model.getObject(row);
    return object;
  }

  public DataObjectLayer getLayer() {
    return this.layer;
  }

  @SuppressWarnings("unchecked")
  @Override
  public DataObjectLayerTableModel getTableModel() {
    final JTable table = getTable();
    return (DataObjectLayerTableModel)table.getModel();
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    super.mouseClicked(e);
    if (SwingUtil.isLeftButtonAndNoModifiers(e) && e.getClickCount() == 2) {
      editRecord();
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    if (source instanceof LayerDataObject) {
      repaint();
    } else if (source == this.layer) {
      final String propertyName = event.getPropertyName();
      if ("recordsChanged".equals(propertyName)) {
        this.tableModel.refresh();
      }
      repaint();
    }
  }

  @Override
  public void removeNotify() {
    super.removeNotify();
    this.layer.removePropertyChangeListener(this);
  }

  public void setAttributeFilterMode(final String mode) {
    if (DataObjectLayerTableModel.MODE_SELECTED.equals(mode)) {
      this.selectedButton.doClick();
    }
  }

  public void zoomToRecord() {
    final DataObject object = getEventRowObject();
    final Project project = this.layer.getProject();
    final Geometry geometry = object.getGeometryValue();
    if (geometry != null) {
      final GeometryFactory geometryFactory = project.getGeometryFactory();
      final BoundingBox boundingBox = BoundingBox.getBoundingBox(geometry)
        .convert(geometryFactory)
        .expandPercent(0.1);
      project.setViewBoundingBox(boundingBox);
    }
  }
}
