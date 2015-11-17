package com.revolsys.swing.map.layer.record.table;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.EventObject;

import javax.swing.JComponent;
import javax.swing.RowSorter;
import javax.swing.table.TableModel;

import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.dnd.ClipboardUtil;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.LayerRecordMenu;
import com.revolsys.swing.map.layer.record.table.model.RecordLayerTableModel;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.record.RecordRowTable;
import com.revolsys.swing.table.record.editor.RecordTableCellEditor;
import com.revolsys.swing.table.record.model.RecordRowTableModel;

public class RecordLayerTable extends RecordRowTable {
  private static final long serialVersionUID = 1L;

  public RecordLayerTable(final RecordLayerTableModel model) {
    super(model);
    getTableHeader().setReorderingAllowed(false);
  }

  public void copyFieldValue() {
    if (isEditingCurrentCell()) {
      final RecordTableCellEditor tableCellEditor = getTableCellEditor();
      final JComponent editorComponent = tableCellEditor.getEditorComponent();
      SwingUtil.dndCopy(editorComponent);
    } else {
      final RecordRowTableModel model = getTableModel();
      final int row = TablePanel.getEventRow();
      final int column = TablePanel.getEventColumn();
      final Object value = model.getValueAt(row, column);

      final String displayValue = model.toDisplayValue(row, column, value);
      final StringSelection transferable = new StringSelection(displayValue);
      ClipboardUtil.setContents(transferable);
    }
  }

  @Override
  protected RowSorter<? extends TableModel> createDefaultRowSorter() {
    final AbstractRecordLayer layer = getLayer();
    final RecordLayerTableModel model = (RecordLayerTableModel)getTableModel();
    return new RecordLayerTableRowSorter(layer, model);
  }

  public void cutFieldValue() {
    if (isEditingCurrentCell()) {
      final RecordTableCellEditor tableCellEditor = getTableCellEditor();
      final JComponent editorComponent = tableCellEditor.getEditorComponent();
      SwingUtil.dndCut(editorComponent);
    } else {
      copyFieldValue();
      final RecordRowTableModel model = getTableModel();
      final int row = TablePanel.getEventRow();
      final int column = TablePanel.getEventColumn();
      model.setValueAt(null, row, column);
    }
  }

  @Override
  public boolean editCellAt(final int row, final int column, final EventObject e) {
    final LayerRecord record = getRecord(row);
    LayerRecordMenu.setEventRecord(record);
    return super.editCellAt(row, column, e);
  }

  @SuppressWarnings("unchecked")
  public <V extends AbstractRecordLayer> V getLayer() {
    final RecordLayerTableModel model = (RecordLayerTableModel)getTableModel();
    return (V)model.getLayer();
  }

  public LayerRecord getRecord(final int row) {
    final RecordLayerTableModel model = getTableModel();
    if (model == null) {
      return null;
    } else {
      return model.getRecord(row);
    }
  }

  @Override
  protected RecordTableCellEditor newTableCellEditor() {
    return new RecordLayerTableCellEditor(this);
  }

  public void pasteFieldValue() {
    if (isEditingCurrentCell()) {
      final RecordTableCellEditor tableCellEditor = getTableCellEditor();
      final JComponent editorComponent = tableCellEditor.getEditorComponent();
      SwingUtil.dndPaste(editorComponent);
    } else {
      try {
        final Transferable clipboard = ClipboardUtil.getContents();
        final Object value = clipboard.getTransferData(DataFlavor.stringFlavor);
        final RecordRowTableModel model = getTableModel();
        final int row = TablePanel.getEventRow();
        final int column = TablePanel.getEventColumn();
        model.setValueAt(value, row, column);
      } catch (final Throwable e) {
      }
    }
  }
}
