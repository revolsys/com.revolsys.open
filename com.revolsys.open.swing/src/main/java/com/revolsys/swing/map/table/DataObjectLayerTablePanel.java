package com.revolsys.swing.map.table;

import javax.swing.JTable;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.action.enablecheck.ObjectPropertyEnableCheck;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.util.LayerUtil;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.dataobject.row.DataObjectRowTableModel;
import com.revolsys.swing.toolbar.ToolBar;
import com.vividsolutions.jts.geom.Geometry;

@SuppressWarnings("serial")
public class DataObjectLayerTablePanel extends TablePanel {

  private DataObjectLayer layer;

  public DataObjectLayerTablePanel(DataObjectLayer layer, JTable table) {
    super(table);
    this.layer = layer;
    MenuFactory menu = getMenu();
    DataObjectMetaData metaData = layer.getMetaData();
    boolean hasGeometry = metaData.getGeometryAttributeIndex() != -1;
    if (hasGeometry) {
      menu.addMenuItemTitleIcon("zoom", "Zoom to Record",
        "magnifier_zoom_selected", this, "zoomToRecord");
    }

    menu.addMenuItemTitleIcon("record", "View/Edit Record", "table_edit", this,
      "editRecord");

    ObjectPropertyEnableCheck canDeleteObjectsEnableCheck = new ObjectPropertyEnableCheck(
      layer, "canDeleteObjects");
    menu.addMenuItemTitleIcon("record", "Delete Record", "table_delete",
      canDeleteObjectsEnableCheck, this, "deleteRecord");

    ToolBar toolBar = getToolBar();

    final InvokeMethodAction addNewRecord = new InvokeMethodAction(null,
      "Add New Record", SilkIconLoader.getIcon("table_row_insert"),
      LayerUtil.class, "addNewRecord", layer);
    ObjectPropertyEnableCheck canAddObjectsEnableCheck = new ObjectPropertyEnableCheck(
      layer, "canAddObjects");
    addNewRecord.setEnableCheck(canAddObjectsEnableCheck);

    toolBar.addButton("record", addNewRecord);
  }

  public DataObjectLayer getLayer() {
    return layer;
  }

  public void zoomToRecord() {
    DataObject object = getEventRowObject();
    Project project = layer.getProject();
    Geometry geometry = object.getGeometryValue();
    if (geometry != null) {
      GeometryFactory geometryFactory = project.getGeometryFactory();
      BoundingBox boundingBox = BoundingBox.getBoundingBox(geometry)
        .convert(geometryFactory)
        .expandPercent(0.1);
      project.setViewBoundingBox(boundingBox);
    }
  }

  public void editRecord() {
    DataObject object = getEventRowObject();
    LayerUtil.showForm(layer, object);
  }

  protected DataObject getEventRowObject() {
    DataObjectRowTableModel model = getTableModel();
    int row = getEventRow();
    DataObject object = model.getObject(row);
    return object;
  }

  public DataObjectRowTableModel getTableModel() {
    JTable table = getTable();
    return (DataObjectRowTableModel)table.getModel();
  }
}
