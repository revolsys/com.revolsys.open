package com.revolsys.jump.ui.swing.table;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.revolsys.jump.ui.swing.FeatureTypeUiBuilderRegistry;
import com.vividsolutions.jump.feature.Feature;

public final class FeatureTableFactory {
  private FeatureTableFactory() {
  }

  public static JTable createTable(final Feature feature,
    final FeatureTypeUiBuilderRegistry uiBuilderRegistry) {
    FeatureTableModel tableModel = new FeatureTableModel(feature);
    return createTable(tableModel, uiBuilderRegistry);
  }

  public static JTable createTable(final FeatureTableModel tableModel,
    final FeatureTypeUiBuilderRegistry uiBuilderRegistry) {
    FeatureTableCellRenderer renderer = new FeatureTableCellRenderer(
      uiBuilderRegistry);
    FeatureTableCellEditor editor = new FeatureTableCellEditor(
      uiBuilderRegistry);

    JTable table = new JTable(tableModel);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

    TableColumnModel columnModel = table.getColumnModel();

    TableColumn attributeColumn = columnModel.getColumn(0);
    attributeColumn.setCellRenderer(renderer);
    attributeColumn.setWidth(120);

    TableColumn valueColumn = columnModel.getColumn(1);
    valueColumn.setCellRenderer(renderer);
    valueColumn.setCellEditor(editor);
    valueColumn.setWidth(120);

    return table;
  }

  public static JTable createTable(final FeatureListTableModel tableModel,
    final FeatureTypeUiBuilderRegistry uiBuilderRegistry) {
    final JTable table = new JTable(tableModel);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    tableModel.addTableModelListener(new TableModelListener() {
      public void tableChanged(final TableModelEvent e) {
        if (e.getType() == TableModelEvent.HEADER_ROW) {
          addRenderersAndEditors(table, tableModel, uiBuilderRegistry);
        }
      }
    });
    addRenderersAndEditors(table, tableModel, uiBuilderRegistry);

    return table;
  }

  private static void addRenderersAndEditors(final JTable table,
    final FeatureListTableModel tableModel,
    final FeatureTypeUiBuilderRegistry uiBuilderRegistry) {
    FeatureCollectionTableCellRenderer renderer = new FeatureCollectionTableCellRenderer(
      uiBuilderRegistry);
    FeatureCollectionTableCellEditor editor = new FeatureCollectionTableCellEditor(
      uiBuilderRegistry);

    TableColumnModel columnModel = table.getColumnModel();

    TableColumn idColumn = columnModel.getColumn(0);
    idColumn.setCellRenderer(renderer);

    for (int i = 1; i < tableModel.getColumnCount(); i++) {
      TableColumn column = columnModel.getColumn(i);
      column.setCellRenderer(renderer);
      column.setCellEditor(editor);
      column.sizeWidthToFit();
    }
  }
}
