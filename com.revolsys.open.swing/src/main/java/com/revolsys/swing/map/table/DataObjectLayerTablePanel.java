package com.revolsys.swing.map.table;

import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JTable;
import javax.swing.JToggleButton;

import org.springframework.util.StringUtils;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.query.Conditions;
import com.revolsys.gis.data.query.Query;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.enablecheck.ObjectPropertyEnableCheck;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.TableRowCount;
import com.revolsys.swing.table.dataobject.row.DataObjectRowTableModel;
import com.revolsys.swing.toolbar.ToolBar;
import com.vividsolutions.jts.geom.Geometry;

@SuppressWarnings("serial")
public class DataObjectLayerTablePanel extends TablePanel implements
  PropertyChangeListener {

  public static final String FILTER_GEOMETRY = "filter_geometry";

  public static final String FILTER_ATTRIBUTE = "filter_attribute";

  private final DataObjectLayer layer;

  public DataObjectLayerTablePanel(final DataObjectLayer layer,
    final JTable table) {
    super(table);
    this.layer = layer;
    final DataObjectLayerTableModel tableModel = getTableModel();
    final MenuFactory menu = getMenu();
    final DataObjectMetaData metaData = layer.getMetaData();
    final boolean hasGeometry = metaData.getGeometryAttributeIndex() != -1;
    if (hasGeometry) {
      menu.addMenuItemTitleIcon("zoom", "Zoom to Record",
        "magnifier_zoom_selected", this, "zoomToRecord");
    }

    menu.addMenuItemTitleIcon("record", "View/Edit Record", "table_edit", this,
      "editRecord");

    final ObjectPropertyEnableCheck canDeleteObjectsEnableCheck = new ObjectPropertyEnableCheck(
      layer, "canDeleteObjects");
    menu.addMenuItemTitleIcon("record", "Delete Record", "table_delete",
      canDeleteObjectsEnableCheck, this, "deleteRecord");

    final ToolBar toolBar = getToolBar();

    toolBar.addComponent("count", new TableRowCount(tableModel));

    final ObjectPropertyEnableCheck canAddObjectsEnableCheck = new ObjectPropertyEnableCheck(
      layer, "canAddObjects");
    toolBar.addButton("record", "Add New Record", "table_row_insert",
      canAddObjectsEnableCheck, layer, "addNewRecord");

    final AttributeFilterPanel attributeFilterPanel = new AttributeFilterPanel(
      metaData);
    attributeFilterPanel.addPropertyChangeListener(this);
    toolBar.addComponent("search", attributeFilterPanel);
    // Filter buttons

    final JToggleButton clearFilter = toolBar.addToggleButtonTitleIcon(
      FILTER_ATTRIBUTE, -1, "Show All Records", "table_filter", tableModel,
      "setAttributeFilterMode", DataObjectLayerTableModel.MODE_ALL);
    clearFilter.doClick();

    final ObjectPropertyEnableCheck editableEnableCheck = new ObjectPropertyEnableCheck(
      layer, "editable");
    toolBar.addToggleButton(FILTER_ATTRIBUTE, -1, "Show Only Changed Records",
      "change_table_filter", editableEnableCheck, tableModel,
      "setAttributeFilterMode", DataObjectLayerTableModel.MODE_EDITS);

    final ObjectPropertyEnableCheck selectableEnableCheck = new ObjectPropertyEnableCheck(
      layer, "selectionCount", 0, true);
    toolBar.addToggleButton(FILTER_ATTRIBUTE, -1, "Show Only Selected Records",
      "filter_selected", selectableEnableCheck, tableModel,
      "setAttributeFilterMode", DataObjectLayerTableModel.MODE_SELECTED);

    if (hasGeometry) {

      final JToggleButton showAllGeometries = toolBar.addToggleButtonTitleIcon(
        FILTER_GEOMETRY, -1, "Show All Records ", "world_filter", tableModel,
        "setFilterByBoundingBox", false);
      showAllGeometries.doClick();

      toolBar.addToggleButtonTitleIcon(FILTER_GEOMETRY, -1,
        "Show Records on Map", "map_filter", tableModel,
        "setFilterByBoundingBox", true);
    }
  }

  public void deleteRecord() {
    final DataObject object = getEventRowObject();
    layer.deleteObjects(object);
  }

  public void editRecord() {
    final DataObject object = getEventRowObject();
    layer.showForm(object);
  }

  protected DataObject getEventRowObject() {
    final DataObjectRowTableModel model = getTableModel();
    final int row = getEventRow();
    final DataObject object = model.getObject(row);
    return object;
  }

  public DataObjectLayer getLayer() {
    return layer;
  }

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
    if (source instanceof AttributeFilterPanel) {
      final AttributeFilterPanel filterPanel = (AttributeFilterPanel)source;
      // TODO allow original query
      // TODO only update if it changed
      final Query query = layer.getQuery();
      final String searchAttribute = filterPanel.getSearchAttribute();
      final String searchText = filterPanel.getSearchText();
      if (StringUtils.hasText(searchAttribute)
        && StringUtils.hasText(searchText)) {
        final String likeString = "%" + searchText + "%";
        query.setWhereCondition(Conditions.like(searchAttribute, likeString));
      } else {
        query.setWhereCondition(null);
      }
      layer.setQuery(query);
    }
  }

  public void zoomToRecord() {
    final DataObject object = getEventRowObject();
    final Project project = layer.getProject();
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
