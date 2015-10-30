package com.revolsys.swing.map.layer.record.table;

import com.revolsys.swing.field.Field;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.component.RecordLayerFields;
import com.revolsys.swing.table.record.editor.RecordTableCellEditor;

public class RecordLayerTableCellEditor extends RecordTableCellEditor {
  private static final long serialVersionUID = 1L;

  private final AbstractRecordLayer layer;

  public RecordLayerTableCellEditor(final RecordLayerTable table) {
    super(table);
    this.layer = table.getLayer();
  }

  @Override
  protected Field newField(final String fieldName) {
    return RecordLayerFields.newCompactField(this.layer, fieldName, true);
  }
}
