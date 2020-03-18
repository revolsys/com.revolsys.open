package com.revolsys.swing.map.layer.record.table;

import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.table.BaseJTable;
import com.revolsys.swing.table.record.editor.RecordTableCellEditor;

public class RecordLayerTableCellEditor extends RecordTableCellEditor {
  private static final long serialVersionUID = 1L;

  private final AbstractRecordLayer layer;

  public RecordLayerTableCellEditor(final BaseJTable table, final AbstractRecordLayer layer) {
    super(table);
    this.layer = layer;
  }

  public RecordLayerTableCellEditor(final RecordLayerTable table) {
    this(table, table.getLayer());
  }

  @Override
  protected RecordDefinition getRecordDefinition() {
    return this.layer.getRecordDefinition();
  }

  @Override
  protected Field newField(final String fieldName) {
    return this.layer.newCompactField(fieldName, true);
  }
}
