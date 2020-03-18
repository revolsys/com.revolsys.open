package com.revolsys.swing.map.layer.record.table.model;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
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
import org.jeometry.common.awt.WebColors;

import com.revolsys.beans.ObjectPropertyException;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.Dialogs;
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
      validator.validateRecords(records, false);
      if (validator.invalidRecords.isEmpty()) {
        successAction.accept(validator);
      } else {
        validator.showErrorDialog(title, successAction, cancelAction);
      }
    }
  }

  public static void validateRecords(final String title, final AbstractRecordLayer layer,
    final LayerRecord record, final Consumer<RecordValidationDialog> successAction,
    final Consumer<RecordValidationDialog> cancelAction) {
    final List<LayerRecord> records = Collections.singletonList(record);
    validateRecords(title, layer, records, successAction, cancelAction);
  }

  private final List<Map<String, String>> invalidRecordErrors = new ArrayList<>();

  /** Records that were initially invalid. Does not change when records are made valid. */
  private final List<LayerRecord> invalidRecords = new ArrayList<>();

  private final AbstractRecordLayer layer;

  /** Records that were initially valid. Does not change when records are made valid. */
  private final List<LayerRecord> validRecords = new ArrayList<>();

  private RecordValidationDialog(final AbstractRecordLayer layer) {
    this.layer = layer;
    Property.addListener(layer, this);
  }

  protected void addInvalidRecords(final List<LayerRecord> records, final boolean withErrors) {
    for (int i = 0; i < this.invalidRecords.size(); i++) {
      final Map<String, String> errors = this.invalidRecordErrors.get(i);
      final boolean hasErrors = !errors.isEmpty();
      if (hasErrors == withErrors) {
        final LayerRecord record = this.invalidRecords.get(i);
        records.add(record);
      }
    }
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

  /**
   * Get the list of records that were still invalid after editing.
   *
   * @return The list of records.
   */
  public List<LayerRecord> getInvalidRecords() {
    final List<LayerRecord> records = new ArrayList<>();
    addInvalidRecords(records, true);
    return records;
  }

  /**
   * Get the list of records that were still valid after editing.
   *
   * @return The list of records.
   */
  public List<LayerRecord> getValidRecords() {
    final List<LayerRecord> records = new ArrayList<>(this.validRecords);
    addInvalidRecords(records, false);
    return records;
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
      WebColors.newAlpha(Color.RED, 64), Color.RED, Color.RED, Color.YELLOW);
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
        WebColors.newAlpha(WebColors.LimeGreen, 127), WebColors.Black,
        WebColors.newAlpha(WebColors.DarkGreen, 191), Color.WHITE));

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
        final FieldDefinition fieldDefinition = record.getFieldDefinition(fieldName);
        validateField(record, fieldDefinition);
      }
    }
  }

  private void showErrorDialog(final String title,
    final Consumer<RecordValidationDialog> successAction,
    final Consumer<RecordValidationDialog> cancelAction) {
    Invoke.later(() -> {
      final String layerPath = this.layer.getPath();

      final JDialog dialog = Dialogs.newDocumentModal("Error " + title + " for " + layerPath);
      dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

      dialog.setLayout(new BorderLayout());

      final ToolBar toolBar = new ToolBar();
      toolBar.addButtonTitleIcon("default", "Cancel " + title, "table_cancel", () -> {
        dialog.setVisible(false);
        cancelAction.accept(this);
      });
      toolBar.addButtonTitleIcon("default", "Save valid records", "table:save", () -> {
        dialog.setVisible(false);
        validateRecords(this.invalidRecords, true);
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
      SwingUtil.dispose(dialog);
    });
  }

  private boolean validateField(final LayerRecord record, final FieldDefinition fieldDefinition) {
    if (fieldDefinition != null) {
      final String fieldName = fieldDefinition.getName();
      try {
        record.validateField(fieldDefinition);
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
    }
    return true;
  }

  private void validateRecord(final LayerRecord record, final boolean wasInvalid) {
    if (this.layer.isLayerRecord(record)) {
      boolean valid = true;
      if (!this.layer.isDeleted(record)) {
        for (final FieldDefinition fieldDefinition : record.getFieldDefinitions()) {
          valid &= validateField(record, fieldDefinition);
        }
      }
      if (valid && !wasInvalid) {
        this.validRecords.add(record);
      }
    } else {
      throw new IllegalArgumentException(
        "Record must be in layer: " + this.layer.getPath() + "\n" + record);
    }
  }

  private void validateRecords(final Iterable<? extends LayerRecord> records,
    final boolean wasInvalid) {
    for (final LayerRecord record : records) {
      validateRecord(record, wasInvalid);
    }
  }
}
