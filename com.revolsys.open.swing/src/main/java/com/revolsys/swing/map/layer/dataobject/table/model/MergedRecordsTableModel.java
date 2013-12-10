package com.revolsys.swing.map.layer.dataobject.table.model;

import java.util.Collection;
import java.util.Map;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.map.layer.dataobject.AbstractDataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.map.layer.dataobject.table.predicate.MergedNullValuePredicate;
import com.revolsys.swing.map.layer.dataobject.table.predicate.MergedObjectPredicate;
import com.revolsys.swing.map.layer.dataobject.table.predicate.MergedValuePredicate;
import com.revolsys.swing.table.SortableTableModel;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.dataobject.model.DataObjectListTableModel;
import com.revolsys.swing.table.dataobject.row.DataObjectRowTable;

public class MergedRecordsTableModel extends DataObjectListTableModel implements
  SortableTableModel {
  private static final long serialVersionUID = 1L;

  public static TablePanel createPanel(final AbstractDataObjectLayer layer,
    final DataObject mergedObject, final Collection<LayerDataObject> objects) {
    final MergedRecordsTableModel model = new MergedRecordsTableModel(layer,
      mergedObject, objects);
    final DataObjectRowTable table = new DataObjectRowTable(model);
    table.setVisibleRowCount(objects.size() + 2);
    MergedValuePredicate.add(table);
    MergedObjectPredicate.add(table);
    MergedNullValuePredicate.add(table);
    table.setSortable(false);

    return new TablePanel(table);
  }

  private final DataObject mergedObject;

  public MergedRecordsTableModel(final AbstractDataObjectLayer layer) {
    this(layer, null, null);
  }

  public MergedRecordsTableModel(final AbstractDataObjectLayer layer,
    final DataObject mergedObject, final Collection<LayerDataObject> objects) {
    super(layer.getMetaData(), objects, layer.getColumnNames());
    setAttributesOffset(1);
    this.mergedObject = mergedObject;
    setEditable(true);
    setReadOnlyFieldNames(layer.getUserReadOnlyFieldNames());
  }

  @Override
  public String getColumnName(final int columnIndex) {
    if (columnIndex == 0) {
      return "#";
    } else {
      return super.getColumnName(columnIndex);
    }
  }

  public DataObject getMergedObject() {
    return mergedObject;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends DataObject> V getObject(final int index) {
    if (index == super.getRowCount()) {
      return (V)mergedObject;
    } else {
      return (V)super.getObject(index);
    }
  }

  @Override
  public int getRowCount() {
    return super.getRowCount() + 1;
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    if (columnIndex == 0) {
      if (rowIndex == getRowCount() - 1) {
        return "Merge";
      } else {
        return rowIndex + 1;
      }
    } else {
      return super.getValueAt(rowIndex, columnIndex);
    }
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    if (columnIndex == 0) {
      return false;
    } else if (rowIndex == getRowCount() - 1) {
      return super.isCellEditable(rowIndex, columnIndex);
    } else {
      return false;
    }
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex,
    final int columnIndex) {
    final Map<String, Object> object = getObject(rowIndex);
    if (object != null) {
      final String name = getColumnName(columnIndex);
      object.put(name, value);
    }
  }

}
