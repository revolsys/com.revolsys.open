package com.revolsys.swing.map.table;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.swing.RowFilter;
import javax.swing.RowFilter.ComparisonType;
import javax.swing.table.TableCellRenderer;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.query.BinaryCondition;
import com.revolsys.gis.data.query.Cast;
import com.revolsys.gis.data.query.Column;
import com.revolsys.gis.data.query.Condition;
import com.revolsys.gis.data.query.Function;
import com.revolsys.gis.data.query.Value;
import com.revolsys.swing.listener.InvokeMethodPropertyChangeListener;
import com.revolsys.swing.map.layer.dataobject.DataObjectListLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.table.dataobject.row.DataObjectRowTable;
import com.revolsys.swing.table.filter.ContainsFilter;
import com.revolsys.swing.table.filter.EqualFilter;
import com.vividsolutions.jts.geom.Geometry;

public class DataObjectListLayerTableModel extends DataObjectLayerTableModel
  implements PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  public static DataObjectRowTable createTable(final DataObjectListLayer layer) {
    final DataObjectListLayerTableModel model = new DataObjectListLayerTableModel(
      layer);
    final TableCellRenderer cellRenderer = new DataObjectLayerTableCellRenderer(
      model);
    final DataObjectRowTable table = new DataObjectRowTable(model, cellRenderer);

    table.setSelectionModel(new DataObjectLayerListSelectionModel(model));
    layer.addPropertyChangeListener("selected",
      new InvokeMethodPropertyChangeListener(table, "repaint"));

    return table;
  }

  private DataObjectListLayer layer;

  private final Set<PropertyChangeListener> propertyChangeListeners = new LinkedHashSet<PropertyChangeListener>();

  public DataObjectListLayerTableModel(final DataObjectListLayer layer) {
    this(layer, layer.getMetaData().getAttributeNames());
  }

  public DataObjectListLayerTableModel(final DataObjectListLayer layer,
    final List<String> columnNames) {
    super(layer, columnNames);
    this.layer = layer;
    layer.addPropertyChangeListener("records", this);
    setEditable(false);
    setSortableModes(MODE_SELECTED, MODE_ALL);
  }

  public void addPropertyChangeListener(
    final PropertyChangeListener propertyChangeListener) {
    this.propertyChangeListeners.add(propertyChangeListener);
  }

  @Override
  @PreDestroy
  public void dispose() {
    super.dispose();
    layer = null;
  }

  private void firePropertyChange(final DataObject object, final String name,
    final Object oldValue, final Object newValue) {
    final PropertyChangeEvent event = new PropertyChangeEvent(object, name,
      oldValue, newValue);
    for (final PropertyChangeListener listener : propertyChangeListeners) {
      listener.propertyChange(event);
    }
  }

  @Override
  public LayerDataObject getObject(final int row) {
    final String attributeFilterMode = getAttributeFilterMode();
    if (attributeFilterMode.equals(MODE_SELECTED)) {
      return super.getObject(row);
    } else {
      return layer.getRecord(row);
    }
  }

  public List<LayerDataObject> getObjects() {
    return layer.getRecords();
  }

  public Set<PropertyChangeListener> getPropertyChangeListeners() {
    return Collections.unmodifiableSet(propertyChangeListeners);
  }

  @Override
  public int getRowCount() {
    final String attributeFilterMode = getAttributeFilterMode();
    if (attributeFilterMode.equals(MODE_SELECTED)) {
      return super.getRowCount();
    } else {
      return layer.getRowCount();
    }
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    if (isEditable()) {
      final String columnName = getColumnName(columnIndex);
      final DataObjectMetaData metaData = getMetaData();
      final DataType dataType = metaData.getAttributeType(columnName);
      if (Geometry.class.isAssignableFrom(dataType.getJavaClass())) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent evt) {
    fireTableDataChanged();
  }

  public void removePropertyChangeListener(
    final PropertyChangeListener propertyChangeListener) {
    this.propertyChangeListeners.remove(propertyChangeListener);
  }

  @Override
  public void setSearchCondition(final Condition searchCondition) {
    super.setSearchCondition(searchCondition);
    final DataObjectRowTable table = getTable();
    if (searchCondition == null) {
      table.setRowFilter(null);
    } else {
      if (searchCondition instanceof BinaryCondition) {
        final BinaryCondition binaryCondition = (BinaryCondition)searchCondition;
        final String operator = binaryCondition.getOperator();
        Condition left = binaryCondition.getLeft();
        final Condition right = binaryCondition.getRight();
        int columnIndex = -1;
        while (columnIndex == -1) {
          if (left instanceof Column) {
            final Column column = (Column)left;
            final String columnName = column.getName();
            columnIndex = getMetaData().getAttributeIndex(columnName);
          } else if (left instanceof Function) {
            final Function function = (Function)left;
            left = function.getConditions().get(0);
          } else if (left instanceof Cast) {
            final Cast cast = (Cast)left;
            left = cast.getCondition();
          } else {
            return;
          }
        }

        if (columnIndex > -1) {
          Object value = null;
          if (right instanceof Value) {
            final Value valueObject = (Value)right;
            value = valueObject.getValue();

          }
          if (operator.equals("=")) {
            RowFilter<Object, Object> filter;
            if (value instanceof Number) {
              final Number number = (Number)value;
              filter = RowFilter.numberFilter(ComparisonType.EQUAL, number,
                columnIndex);
            } else {
              filter = new EqualFilter(StringConverterRegistry.toString(value),
                columnIndex);
            }
            table.setRowFilter(filter);
          } else if (operator.equals("LIKE")) {
            final RowFilter<Object, Object> filter = new ContainsFilter(
              StringConverterRegistry.toString(value), columnIndex);
            table.setRowFilter(filter);
          }
        }
      }
    }
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex,
    final int columnIndex) {
    final DataObject object = getObject(rowIndex);
    if (object != null) {
      final String name = getColumnName(columnIndex);
      final Object oldValue = object.getValueByPath(name);
      object.setValue(name, value);
      firePropertyChange(object, name, oldValue, value);
    }
  }

  /*
   * TODO @Override public SortOrder setSortOrder(int column) { SortOrder
   * sortOrder = super.setSortOrder(column); final String attributeName =
   * getAttributeName(column); final Comparator<DataObject> comparitor = new
   * DataObjectAttributeComparator( sortOrder == SortOrder.ASCENDING,
   * attributeName); Collections.sort(objects, comparitor);
   * fireTableDataChanged(); return sortOrder; }
   */

}
