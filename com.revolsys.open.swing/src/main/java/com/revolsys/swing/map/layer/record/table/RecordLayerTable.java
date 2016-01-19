package com.revolsys.swing.map.layer.record.table;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.EventObject;

import javax.swing.JComponent;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
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
    JTableHeader tableHeader = getTableHeader();
    tableHeader.setReorderingAllowed(false);
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

  @Override
  public RecordLayerTableModel getModel() {
    return (RecordLayerTableModel)super.getModel();
  }

  @Override
  public ListSelectionModel getSelectionModel() {
    final RecordLayerTableModel model = getModel();
    final ListSelectionModel selectionModel = model.getSelectionModel();
    if (selectionModel == null) {
      return super.getSelectionModel();
    } else {
      return selectionModel;
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

  @Override
  public void setSelectionModel(ListSelectionModel newModel) {
    if (newModel == null) {
      newModel = createDefaultSelectionModel();
    }
    super.setSelectionModel(newModel);
  }

  @Override
  protected void tableChangedDo(final TableModelEvent event) {
    final RecordLayerTableModel model = getModel();
    if (model.isSortable()) {
      setSortable(true);
    } else {
      setSortable(false);
    }
    final RowFilter<RecordRowTableModel, Integer> rowFilter = model.getRowFilter();
    final boolean filterChanged = getRowFilter() != rowFilter;
    if (filterChanged) {
      setRowFilter(null);
    }
    super.tableChangedDo(event);
    if (filterChanged) {
      setRowFilter(rowFilter);
    }
  }
}
