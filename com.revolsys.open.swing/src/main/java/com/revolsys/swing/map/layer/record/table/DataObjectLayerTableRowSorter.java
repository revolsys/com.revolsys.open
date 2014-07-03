package com.revolsys.swing.map.layer.record.table;

import java.text.Collator;
import java.util.Comparator;

import org.jdesktop.swingx.sort.TableSortController;

import com.revolsys.comparator.NumericComparator;
import com.revolsys.data.codes.CodeTable;
import com.revolsys.data.codes.CodeTableCoparator;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.swing.map.layer.record.AbstractDataObjectLayer;
import com.revolsys.swing.map.layer.record.table.model.DataObjectLayerTableModel;

public class DataObjectLayerTableRowSorter extends
  TableSortController<DataObjectLayerTableModel> {

  private final AbstractDataObjectLayer layer;

  public DataObjectLayerTableRowSorter(final AbstractDataObjectLayer layer,
    final DataObjectLayerTableModel model) {
    super(model);
    this.layer = layer;
  }

  @Override
  public Comparator<?> getComparator(final int columnIndex) {
    final DataObjectLayerTableModel model = getModel();
    final String attributeName = model.getFieldName(columnIndex);
    final RecordDefinition metaData = layer.getMetaData();
    final CodeTable codeTable = metaData.getCodeTableByColumn(attributeName);
    if (codeTable == null) {
      final Class<?> columnClass = model.getColumnClass(columnIndex);
      final Comparator<?> comparator = super.getComparator(columnIndex);
      if (comparator != null) {
        return comparator;
      }
      if (columnClass == String.class) {
        return Collator.getInstance();
      } else if (Number.class.isAssignableFrom(columnClass)) {
        return new NumericComparator<Object>();
      } else if (Comparable.class.isAssignableFrom(columnClass)) {
        return COMPARABLE_COMPARATOR;
      } else {
        return Collator.getInstance();
      }
    } else {
      return new CodeTableCoparator(codeTable);
    }
  }
}
