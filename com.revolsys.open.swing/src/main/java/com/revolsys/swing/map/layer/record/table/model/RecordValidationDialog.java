package com.revolsys.swing.map.layer.record.table.model;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Rectangle;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import org.jdesktop.swingx.VerticalLayout;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlightPredicate.AndHighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;

import com.revolsys.awt.WebColors;
import com.revolsys.beans.ObjectPropertyException;
import com.revolsys.collection.list.Lists;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.highlighter.ColorHighlighter;
import com.revolsys.swing.table.record.RecordRowTable;
import com.revolsys.swing.table.record.model.RecordListTableModel;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.util.Property;

public class RecordValidationDialog implements PropertyChangeListener, Closeable {
  public static void validateRecords(final String title, final AbstractRecordLayer layer,
    final Iterable<? extends LayerRecord> records,
    final Consumer<RecordValidationDialog> successAction,
    final Consumer<RecordValidationDialog> cancelAction) {
    try (
      final RecordValidationDialog validator = new RecordValidationDialog(layer)) {
      validator.validateRecords(records);
      if (validator.hasInvalidRecords()) {
        validator.showErrorDialog(title, successAction, cancelAction);
      } else {
        successAction.accept(validator);
      }
    }
  }

  public static void validateRecords(final String title, final AbstractRecordLayer layer,
    final LayerRecord record, final Consumer<RecordValidationDialog> successAction,
    final Consumer<RecordValidationDialog> cancelAction) {
    final List<LayerRecord> records = Collections.singletonList(record);
    validateRecords(title, layer, records, successAction, cancelAction);
  }

  private final AbstractRecordLayer layer;

  private final List<Map<String, String>> invalidRecordErrors = new ArrayList<>();

  private final List<LayerRecord> validRecords = new ArrayList<>();

  private final List<LayerRecord> invalidRecords = new ArrayList<>();

  private RecordValidationDialog(final AbstractRecordLayer layer) {
    this.layer = layer;
    Property.addListener(layer, this);
  }

  private synchronized void addRecordFieldError(final LayerRecord record, final String fieldName,
    final String errorMessage) {
    int recordIndex = getInvalidRecordIndex(record);
    Map<String, String> fieldErrors;
    if (recordIndex == -1) {
      recordIndex = this.invalidRecords.size();
      this.invalidRecords.add(record);
      fieldErrors = new HashMap<>();
      this.invalidRecordErrors.add(fieldErrors);
    } else {
      fieldErrors = this.invalidRecordErrors.get(recordIndex);
    }
    final String oldErrors = fieldErrors.get(fieldName);
    if (Property.hasValue(oldErrors)) {
      final String mergedErrorMessage = oldErrors + "\n" + errorMessage;
      fieldErrors.put(fieldName, mergedErrorMessage);
    } else {
      fieldErrors.put(fieldName, errorMessage);
    }
  }

  @Override
  public void close() {
    Property.removeListener(this.layer, this);
  }

  private int getInvalidRecordIndex(final LayerRecord record) {
    for (int i = this.invalidRecords.size() - 1; i >= 0; i--) {
      final LayerRecord invalidRecord = this.invalidRecords.get(i);
      if (record.isSame(invalidRecord)) {
        return i;
      }
    }
    return -1;
  }

  public List<LayerRecord> getInvalidRecords() {
    return this.invalidRecords;
  }

  public List<LayerRecord> getValidRecords() {
    return this.validRecords;
  }

  public boolean hasInvalidRecords() {
    return !this.invalidRecords.isEmpty();
  }

  private TablePanel newInvalidRecordsTablePanel() {
    final RecordDefinition recordDefinition = this.layer.getRecordDefinition();
    final List<String> fieldNames = this.layer.getFieldNames();

    final RecordListTableModel model = new RecordListTableModel(recordDefinition,
      this.invalidRecords, fieldNames);
    model.setReadOnlyFieldNames(this.layer.getUserReadOnlyFieldNames());

    final RecordRowTable table = new RecordRowTable(model);
    table.setVisibleRowCount(Math.min(10, model.getRowCount() + 1));
    table.setSortable(true);

    // table.getSelectionModel().addListSelectionListener((event) -> {
    // final ListSelectionModel selectionModel = table.getSelectionModel();
    // final int rowCount = this.invalidRecords.size();
    // final boolean mergedSelected = selectionModel.isSelectedIndex(rowCount);
    // for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
    // final Record record = this.invalidRecords.get(rowIndex);
    // if (record != null) {
    // if (mergedSelected || selectionModel.isSelectedIndex(rowIndex)) {
    // this.layer.addHighlightedRecords((LayerRecord)record);
    // } else {
    // this.layer.unHighlightRecords((LayerRecord)record);
    // }
    // }
    // }
    // this.layer.zoomToHighlighted();
    // });

    table.resizeColumnsToContent();

    final HighlightPredicate invalidFieldPredicate = (final Component renderer,
      final ComponentAdapter adapter) -> {
      try {
        final int rowIndex = adapter.convertRowIndexToModel(adapter.row);
        final int columnIndex = adapter.convertColumnIndexToModel(adapter.column);
        final Map<String, String> fieldErrors = this.invalidRecordErrors.get(rowIndex);
        if (!fieldErrors.isEmpty()) {
          final String fieldName = this.layer.getFieldName(columnIndex);
          final String errorMessage = fieldErrors.get(fieldName);
          if (Property.hasValue(errorMessage)) {
            final JComponent jcomponent = (JComponent)renderer;
            jcomponent.setToolTipText(errorMessage);
            return true;
          }
        }
      } catch (final Throwable e) {
      }
      return false;
    };
    final Highlighter invalidFieldHighlighter = new ColorHighlighter(invalidFieldPredicate,
      WebColors.setAlpha(Color.RED, 64), Color.RED, Color.RED, Color.YELLOW);
    table.addHighlighter(invalidFieldHighlighter);

    final HighlightPredicate validRecordPredicate = (final Component renderer,
      final ComponentAdapter adapter) -> {
      try {
        final int rowIndex = adapter.convertRowIndexToModel(adapter.row);
        final Map<String, String> fieldErrors = this.invalidRecordErrors.get(rowIndex);
        if (fieldErrors.isEmpty()) {
          return true;
        }
      } catch (final Throwable e) {
      }
      return false;
    };
    table.addHighlighter(
      new ColorHighlighter(new AndHighlightPredicate(validRecordPredicate, HighlightPredicate.EVEN),
        WebColors.setAlpha(WebColors.LimeGreen, 127), WebColors.Black,
        WebColors.setAlpha(WebColors.DarkGreen, 191), Color.WHITE));

    table.addHighlighter(
      new ColorHighlighter(new AndHighlightPredicate(validRecordPredicate, HighlightPredicate.ODD),
        WebColors.LimeGreen, WebColors.Black, WebColors.DarkGreen, Color.WHITE));

    final TablePanel tablePanel = new TablePanel(table);
    tablePanel
      .setBorder(BorderFactory.createTitledBorder(table.getRowCount() + " invalid records"));
    return tablePanel;
  }

  private TablePanel newValidRecordsTablePanel() {
    final RecordDefinition recordDefinition = this.layer.getRecordDefinition();
    final List<String> fieldNames = this.layer.getFieldNames();
    final RecordListTableModel model = new RecordListTableModel(recordDefinition, this.validRecords,
      fieldNames);
    final RecordRowTable table = new RecordRowTable(model);
    table.setVisibleRowCount(Math.min(10, model.getRowCount() + 1));
    table.setSortable(true);
    table.setEditable(false);
    table.resizeColumnsToContent();

    final TablePanel tablePanel = new TablePanel(table);
    tablePanel.setBorder(BorderFactory.createTitledBorder(table.getRowCount() + " valid records"));
    return tablePanel;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent e) {
    final Object source = e.getSource();
    if (source instanceof LayerRecord) {
      final LayerRecord record = (LayerRecord)source;
      if (this.layer.isLayerRecord(record)) {
        final String fieldName = e.getPropertyName();
        final int fieldIndex = record.getFieldIndex(fieldName);
        validateField(record, fieldIndex);
      }
    }
  }

  private void showErrorDialog(final String title,
    final Consumer<RecordValidationDialog> successAction,
    final Consumer<RecordValidationDialog> cancelAction) {
    Invoke.andWait(() -> {
      final String layerPath = this.layer.getPath();

      final Window window = SwingUtil.getActiveWindow();

      final JDialog dialog = new JDialog(window, "Error " + title + " for " + layerPath,
        ModalityType.APPLICATION_MODAL);
      dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

      dialog.setLayout(new BorderLayout());

      final ToolBar toolBar = new ToolBar();
      toolBar.addButtonTitleIcon("default", "Cancel " + title, "table_cancel", () -> {
        dialog.setVisible(false);
        cancelAction.accept(this);
      });
      toolBar.addButtonTitleIcon("default", "Save valid records", "table_save", () -> {
        dialog.setVisible(false);
        validateRecords(Lists.array(this.invalidRecords));
        successAction.accept(this);
      });

      final TablePanel invalidRecordsTablePanel = newInvalidRecordsTablePanel();

      final JLabel message = new JLabel(
        "<html><b style=\"color:red\">Edit the invalid fields (red background) for the invalid records.</b><br />"
          + " Valid records will have a green background when edited.</html>");
      message.setBorder(BorderFactory.createTitledBorder(layerPath));
      final BasePanel panel = new BasePanel(new VerticalLayout(), //
        toolBar, //
        message, //
        invalidRecordsTablePanel //
      );

      panel.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
      if (!this.validRecords.isEmpty()) {
        final TablePanel validRecordsTablePanel = newValidRecordsTablePanel();
        panel.add(validRecordsTablePanel);
      }
      dialog.add(panel, BorderLayout.NORTH);

      final Rectangle screenBounds = SwingUtil.getScreenBounds();

      dialog.pack();
      dialog.setSize(screenBounds.width - 50, dialog.getPreferredSize().height);
      SwingUtil.setLocationCentre(screenBounds, dialog);
      dialog.setVisible(true);
      dialog.dispose();
    });
  }

  protected boolean validateField(final LayerRecord record, final int fieldIndex) {
    final String fieldName = record.getFieldName(fieldIndex);
    try {
      record.validateField(fieldIndex);
      final int recordIndex = getInvalidRecordIndex(record);
      if (recordIndex > -1) {
        final Map<String, String> fieldErrors = this.invalidRecordErrors.get(recordIndex);
        fieldErrors.remove(fieldName);
      }
    } catch (final ObjectPropertyException e) {
      final String errorMessage = e.getLocalizedMessage();
      addRecordFieldError(record, fieldName, errorMessage);
      return false;
    } catch (final Throwable e) {
      final String errorMessage = e.getLocalizedMessage();
      addRecordFieldError(record, fieldName, errorMessage);
      return false;
    }
    return true;
  }

  private void validateRecord(final LayerRecord record) {
    if (this.layer.isLayerRecord(record)) {
      boolean valid = true;
      if (!this.layer.isDeleted(record)) {
        final int fieldCount = record.getFieldCount();
        for (int fieldIndex = 0; fieldIndex < fieldCount; fieldIndex++) {
          valid &= validateField(record, fieldIndex);
        }
      }
      if (valid) {
        this.validRecords.add(record);
      }
    } else {
      throw new IllegalArgumentException(
        "Record must be in layer: " + this.layer.getPath() + "\n" + record);
    }
  }

  protected void validateRecords(final Iterable<? extends LayerRecord> records) {
    for (final LayerRecord record : records) {
      validateRecord(record);
    }
  }
}
