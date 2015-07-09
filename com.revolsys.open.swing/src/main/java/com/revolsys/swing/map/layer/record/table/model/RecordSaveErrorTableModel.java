package com.revolsys.swing.map.layer.record.table.model;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.VerticalLayout;

import com.revolsys.beans.ObjectPropertyException;
import com.revolsys.data.record.Record;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.table.SortableTableModel;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.record.model.RecordListTableModel;
import com.revolsys.swing.table.record.row.RecordRowTable;

public class RecordSaveErrorTableModel extends RecordListTableModel
  implements SortableTableModel, ListSelectionListener {
  private static final long serialVersionUID = 1L;

  public static TablePanel createPanel(final RecordSaveErrorTableModel model) {
    final RecordRowTable table = new RecordRowTable(model);
    table.setVisibleRowCount(model.getRowCount() + 1);
    table.setSortable(true);
    table.getSelectionModel().addListSelectionListener(model);
    table.resizeColumnsToContent();
    return new TablePanel(table);
  }

  private final AbstractRecordLayer layer;

  private final List<String> messages = new ArrayList<>();

  private final List<Throwable> exceptions = new ArrayList<>();

  public RecordSaveErrorTableModel(final AbstractRecordLayer layer) {
    super(layer.getRecordDefinition(), Collections.<LayerRecord> emptyList(),
      layer.getFieldNames());
    this.layer = layer;
    setFieldsOffset(1);
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

  public boolean showErrorDialog() {
    if (isEmpty()) {
      return true;
    } else {
      final String layerPath = this.layer.getPath();
      final BasePanel panel = new BasePanel(new VerticalLayout(),
        new JLabel("<html><p><b style=\"color:red\">Error saving changes for layer:</b></p><p>"
          + layerPath + "</p>"),
        RecordSaveErrorTableModel.createPanel(this));
      final Rectangle screenBounds = SwingUtil.getScreenBounds();
      panel.setPreferredSize(new Dimension(screenBounds.width - 300, getRowCount() * 22 + 75));

      final Window window = SwingUtil.getActiveWindow();
      final JOptionPane pane = new JOptionPane(panel, JOptionPane.ERROR_MESSAGE,
        JOptionPane.DEFAULT_OPTION, null, null, null);

      pane.setComponentOrientation(window.getComponentOrientation());

      final JDialog dialog = pane.createDialog(window, "Error Saving Changes: " + layerPath);

      dialog.pack();
      SwingUtil.setLocationCentre(screenBounds, dialog);
      dialog.setVisible(true);
      dialog.dispose();
      return false;
    }
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
