package com.revolsys.swing.map.layer.record.table.model;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.jeometry.common.awt.WebColors;

import com.revolsys.collection.list.Lists;
import com.revolsys.swing.Dialogs;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.record.RecordRowTable;
import com.revolsys.swing.table.record.model.RecordListTableModel;
import com.revolsys.swing.table.record.model.RecordRowTableModel;

public class BlockDeleteRecords {
  public static <LR extends LayerRecord> boolean showErrorDialog(final AbstractRecordLayer layer,
    final List<LR> blockedRecords, final List<LR> otherRecords,
    final Consumer<Collection<LR>> deleteAction) {
    Invoke.later(() -> {
      final List<String> deleteRecordsBlockFieldNames = Lists
        .toArray(layer.getDeleteRecordsBlockFieldNames());

      final List<String> fieldNames = Lists.toArray(deleteRecordsBlockFieldNames);

      for (final String fieldName : layer.getFieldNames()) {
        if (!deleteRecordsBlockFieldNames.contains(fieldName)) {
          fieldNames.add(fieldName);
        }

      }
      final RecordRowTableModel tableModel = new RecordListTableModel(layer.getRecordDefinition(),
        blockedRecords, fieldNames);

      final List<Integer> blockedFieldIndexes = new ArrayList<>();
      for (final String fieldName : deleteRecordsBlockFieldNames) {
        final int fieldIndex = fieldNames.indexOf(fieldName);
        blockedFieldIndexes.add(fieldIndex);
      }
      final String layerPath = layer.getPath();

      final RecordRowTable table = new RecordRowTable(tableModel);
      table.setVisibleRowCount(tableModel.getRowCount() + 1);
      table.setSortable(true);
      table.resizeColumnsToContent();

      table.addColorHighlighter((row, col) -> {
        if (col < deleteRecordsBlockFieldNames.size()) {
          final String fieldName = deleteRecordsBlockFieldNames.get(col);
          final LayerRecord record = blockedRecords.get(row);
          return layer.isDeletedRecordFieldBlocked(record, fieldName);

        }
        return false;
      }, WebColors.Pink, WebColors.Red);

      final TablePanel tablePanel = new TablePanel(table);

      String message = "<p><b style=\"color:red\">The following records cannot be deleted. Blocked field values are shown in red.</b></p>"
        + "<p>Edit those records to change those fields (e.g. clear value, or set to No) if you are sure you want to delete them.</p>";
      final int otherCount = otherRecords.size();
      if (otherCount > 0) {
        message = message + "<p style=\"color:red\">There are " + otherCount
          + " other record(s) to  be deleted, click the Yes to delete those records, or No to not delete any records.</p>";
      }
      final BasePanel panel = new BasePanel(new BorderLayout());
      panel.add(new JLabel("<html>" + message + "</html>"), BorderLayout.NORTH);
      panel.add(tablePanel, BorderLayout.CENTER);
      final Rectangle screenBounds = SwingUtil.getScreenBounds();
      panel.setPreferredSize(new Dimension(screenBounds.width - 300,
        Math.min(tableModel.getRowCount() * 22 + 75, screenBounds.height - 150)));

      final Window window = Dialogs.getWindow();
      int option = JOptionPane.YES_NO_OPTION;
      if (otherRecords.isEmpty()) {
        option = JOptionPane.DEFAULT_OPTION;
      }
      final JOptionPane pane = new JOptionPane(panel, JOptionPane.ERROR_MESSAGE, option, null, null,
        null);

      pane.setComponentOrientation(window.getComponentOrientation());

      final JDialog dialog = pane.createDialog(window, "Cannot delete records: " + layerPath);

      dialog.pack();
      SwingUtil.setLocationCentre(screenBounds, dialog);
      dialog.setVisible(true);
      SwingUtil.dispose(dialog);
      final Integer result = (Integer)pane.getValue();
      if (result != null && result == JOptionPane.YES_OPTION) {
        deleteAction.accept(otherRecords);
      }
    });
    return false;
  }
}
