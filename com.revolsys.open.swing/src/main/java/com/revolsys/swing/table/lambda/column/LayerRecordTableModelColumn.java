package com.revolsys.swing.table.lambda.column;

import com.revolsys.record.Record;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;

public class LayerRecordTableModelColumn extends RecordTableModelColumn {

  private final AbstractRecordLayer layer;

  public LayerRecordTableModelColumn(final String columnName, final AbstractRecordLayer layer,
    final Record record, final boolean editable) {
    super(columnName, record, editable);
    this.layer = layer;
  }

  public LayerRecordTableModelColumn(final String columnName, final LayerRecord record,
    final boolean editable) {
    super(columnName, record, editable);
    this.layer = record.getLayer();
  }

  @Override
  public boolean isCellEditable(final int rowIndex) {
    if (!this.layer.isReadOnly()) {
      final String fieldName = this.record.getFieldName(rowIndex);
      if (!this.layer.isFieldUserReadOnly(fieldName)) {
        return true;
      }
    }
    return false;
  }
}
