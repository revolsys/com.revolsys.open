package com.revolsys.swing.map.layer.record.table.model;

import java.util.Collection;
import java.util.List;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jeometry.common.awt.WebColors;

import com.revolsys.beans.ObjectPropertyException;
import com.revolsys.record.Record;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.table.SortableTableModel;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.highlighter.OddEvenColorHighlighter;
import com.revolsys.swing.table.record.RecordRowTable;
import com.revolsys.swing.table.record.model.RecordListTableModel;

public class RecordLayerErrorsTableModel extends RecordListTableModel
  implements SortableTableModel, ListSelectionListener {
  private static final long serialVersionUID = 1L;

  private final List<Throwable> exceptions;

  private final AbstractRecordLayer layer;

  private final List<String> messages;

  public RecordLayerErrorsTableModel(final AbstractRecordLayer layer, final List<Record> records,
    final List<String> messages, final List<Throwable> exceptions,
    final Collection<String> fieldNames) {
    super(layer.getRecordDefinition(), records, fieldNames, 1);
    this.layer = layer;
    this.messages = messages;
    this.exceptions = exceptions;
    setEditable(true);
    setReadOnlyFieldNames(layer.getUserReadOnlyFieldNames());
  }

  public void addRecord(final LayerRecord record, final String errorMessage) {
    super.add(record);
    this.messages.add(errorMessage);
    this.exceptions.add(null);
  }

  public void addRecord(final LayerRecord record, final Throwable exception) {
    super.add(record);
    String message;
    if (exception instanceof ObjectPropertyException) {
      final ObjectPropertyException objectPropertyException = (ObjectPropertyException)exception;
      message = objectPropertyException.getPropertyName() + ": "
        + objectPropertyException.getMessage();
    } else {
      message = exception.getMessage();
    }
    this.messages.add(message);
    this.exceptions.add(exception);
  }

  @Override
  public Class<?> getColumnClass(final int columnIndex) {
    if (columnIndex == 0) {
      return String.class;
    } else {
      return super.getColumnClass(columnIndex);
    }
  }

  @Override
  public String getColumnName(final int columnIndex) {
    if (columnIndex == 0) {
      return "Error";
    } else {
      return super.getColumnName(columnIndex);
    }
  }

  @Override
  public int getRowCount() {
    return super.getRowCount();
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    if (columnIndex == 0) {
      if (rowIndex < this.messages.size()) {
        return this.messages.get(rowIndex);
      } else {
        return "-";
      }
    } else {
      return super.getValueAt(rowIndex, columnIndex);
    }
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    return false;
  }

  public TablePanel newPanel() {
    final RecordRowTable table = new RecordRowTable(this);
    table.setVisibleRowCount(this.getRowCount() + 1);
    table.setSortable(true);
    table.getSelectionModel().addListSelectionListener(this);
    table.resizeColumnsToContent();

    final HighlightPredicate predicate = (renderer, adapter) -> {
      final int columnIndex = adapter.convertColumnIndexToModel(adapter.column);
      return columnIndex == 0;
    };
    table.addHighlighter(new OddEvenColorHighlighter(predicate,
      WebColors.newAlpha(WebColors.Pink, 127), WebColors.LightCoral));

    return new TablePanel(table);
  }

  @Override
  public void valueChanged(final ListSelectionEvent event) {
    final RecordRowTable table = getTable();
    final ListSelectionModel selectionModel = table.getSelectionModel();
    final int rowCount = super.getRowCount();
    final boolean mergedSelected = selectionModel.isSelectedIndex(rowCount);
    for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
      final Record record = getRecord(rowIndex);
      if (record != null) {
        if (mergedSelected || selectionModel.isSelectedIndex(rowIndex)) {
          this.layer.addHighlightedRecords((LayerRecord)record);
        } else {
          this.layer.unHighlightRecords((LayerRecord)record);
        }
      }
    }
    this.layer.zoomToHighlighted();
  }

}
