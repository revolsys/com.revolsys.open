package com.revolsys.swing.map.layer.record.table.model;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.jdesktop.swingx.VerticalLayout;
import org.jeometry.common.awt.WebColors;

import com.revolsys.collection.list.Lists;
import com.revolsys.collection.map.Maps;
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
import com.revolsys.util.Pair;

public class BlockDeleteRecords {
  public static <LR extends LayerRecord> Map<AbstractRecordLayer, List<LR>> confirmDeleteRecords(
    final String suffix, final List<LR> records, final boolean confirmDeleteRecords) {
    final Pair<Map<AbstractRecordLayer, List<LR>>, Map<AbstractRecordLayer, List<LR>>> deletedBlocked = getDeletedBlocked(
      suffix, records);
    final Map<AbstractRecordLayer, List<LR>> blockedRecordsByLayer = deletedBlocked.getValue1();
    final Map<AbstractRecordLayer, List<LR>> otherRecordsByLayer = deletedBlocked.getValue2();
    final boolean delete = false;
    if (blockedRecordsByLayer.isEmpty()) {
      if (AbstractRecordLayer.isGlobalConfirmDeleteRecords() || confirmDeleteRecords) {
        final int recordCount = records.size();
        final String message = "Delete " + recordCount + " records" + suffix
          + "? This action cannot be undone.";
        final String title = "Delete Records" + suffix;
        final int confirm = Dialogs.showConfirmDialog(message, title, JOptionPane.YES_NO_OPTION,
          JOptionPane.ERROR_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
          return otherRecordsByLayer;
        }
      } else {
        return otherRecordsByLayer;
      }
    } else {
      if (BlockDeleteRecords.showErrorDialog(blockedRecordsByLayer, otherRecordsByLayer)) {
        return otherRecordsByLayer;
      }
    }
    if (delete) {
      for (final Iterator<List<LR>> iterator = otherRecordsByLayer.values().iterator(); iterator
        .hasNext();) {
        final List<LR> otherRecords = iterator.next();
        if (otherRecords.isEmpty()) {
          iterator.remove();
        }
      }
      return otherRecordsByLayer;
    } else {
      return Collections.emptyMap();
    }
  }

  public static <LR extends LayerRecord> Pair<Map<AbstractRecordLayer, List<LR>>, Map<AbstractRecordLayer, List<LR>>> getDeletedBlocked(
    final String suffix, final List<LR> records) {
    final Map<AbstractRecordLayer, List<LR>> blockedRecords = new LinkedHashMap<>();
    final Map<AbstractRecordLayer, List<LR>> otherRecords = new LinkedHashMap<>();
    for (final LR record : records) {
      final AbstractRecordLayer layer = record.getLayer();
      if (layer.isDeleteBlocked(suffix, record)) {
        Maps.getList(otherRecords, layer);
        Maps.addToList(blockedRecords, layer, record);
      } else if (otherRecords != null) {
        Maps.addToList(otherRecords, layer, record);
      }
    }
    return new Pair<>(blockedRecords, otherRecords);
  }

  public static <LR extends LayerRecord> boolean showErrorDialog(
    final Map<AbstractRecordLayer, List<LR>> blockedRecordsByLayer,
    final Map<AbstractRecordLayer, List<LR>> otherRecordsByLayer) {
    return Invoke.andWait(() -> {
      final BasePanel panel = new BasePanel(new VerticalLayout(5));
      int totalOtherCount = otherRecordsByLayer.size();
      for (final List<LR> otherRecords : otherRecordsByLayer.values()) {
        totalOtherCount += otherRecords.size();
      }
      int preferredHeight = 75;
      String message = "<p><b style=\"color:red\">The following records cannot be deleted. Blocked field values are shown in red.</b></p>"
        + "<p>Edit those records to change those fields (e.g. clear value, or set to No) if you are sure you want to delete them.</p>";
      if (totalOtherCount > 0) {
        message = message + "<p style=\"color:blue\">There are " + totalOtherCount
          + " other record(s) to  be deleted, click the Yes to delete those records, or No to not delete any records.</p>";
      }

      panel.add(new JLabel("<html>" + message + "</html>"));
      for (final AbstractRecordLayer layer : otherRecordsByLayer.keySet()) {
        preferredHeight += 30;
        final String layerPath = layer.getPath();
        final List<LR> blockedRecords = blockedRecordsByLayer.getOrDefault(layer,
          Collections.emptyList());
        final int blockedCount = blockedRecords.size();
        final List<LR> otherRecords = otherRecordsByLayer.get(layer);
        final int otherCount = otherRecords.size();

        final BasePanel layerPanel = new BasePanel(new VerticalLayout(5));
        layerPanel.setBorder(BorderFactory.createTitledBorder(layerPath));
        panel.add(layerPanel);
        if (!otherRecords.isEmpty()) {
          preferredHeight += 22;
          layerPanel.add(new JLabel("<html><p style=\"color:blue\">There are " + otherCount
            + " other record(s) to be deleted</p></html>"));
        }
        if (blockedCount > 0) {
          preferredHeight += 70;
          preferredHeight += blockedCount + 22;
          layerPanel.add(new JLabel("<html><p style=\"color:red\">There are " + blockedCount
            + " record(s) that can't be deleted</p></html>"));
          final List<String> deleteRecordsBlockFieldNames = Lists
            .toArray(layer.getDeleteRecordsBlockFieldNames());
          final List<String> fieldNames = Lists.toArray(deleteRecordsBlockFieldNames);
          for (final String fieldName : layer.getFieldNames()) {
            if (!deleteRecordsBlockFieldNames.contains(fieldName)) {
              fieldNames.add(fieldName);
            }

          }
          final RecordRowTableModel tableModel = new RecordListTableModel(
            layer.getRecordDefinition(), blockedRecords, fieldNames);

          final List<Integer> blockedFieldIndexes = new ArrayList<>();
          for (final String fieldName : deleteRecordsBlockFieldNames) {
            final int fieldIndex = fieldNames.indexOf(fieldName);
            blockedFieldIndexes.add(fieldIndex);
          }

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
          layerPanel.add(tablePanel);
        }
      }
      final Rectangle screenBounds = SwingUtil.getScreenBounds();
      panel.setPreferredSize(new Dimension(screenBounds.width - 300,
        Math.min(preferredHeight, screenBounds.height - 150)));

      final Window window = Dialogs.getWindow();
      int option = JOptionPane.YES_NO_OPTION;
      if (totalOtherCount == 0) {
        option = JOptionPane.DEFAULT_OPTION;
      }
      final JOptionPane pane = new JOptionPane(panel, JOptionPane.ERROR_MESSAGE, option, null, null,
        null);

      pane.setComponentOrientation(window.getComponentOrientation());

      final JDialog dialog = pane.createDialog(window, "Confirm Delete Records");

      dialog.pack();
      SwingUtil.setLocationCentre(screenBounds, dialog);
      dialog.setVisible(true);
      SwingUtil.dispose(dialog);
      final Integer result = (Integer)pane.getValue();
      if (result != null && result == JOptionPane.YES_OPTION) {
        return totalOtherCount > 0;
      }
      return false;
    });
  }
}
