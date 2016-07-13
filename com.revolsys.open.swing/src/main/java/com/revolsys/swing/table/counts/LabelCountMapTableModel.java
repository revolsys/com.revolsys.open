package com.revolsys.swing.table.counts;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.table.TableColumnExt;

import com.revolsys.awt.WebColors;
import com.revolsys.io.PathNameProxy;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.AbstractTableModel;
import com.revolsys.swing.table.BaseJTable;
import com.revolsys.util.Counter;
import com.revolsys.util.count.CategoryLabelCountMap;
import com.revolsys.util.count.LabelCountMap;

public class LabelCountMapTableModel extends AbstractTableModel {
  private static final long serialVersionUID = 1L;

  private String selectedCountName;

  private String selectedLabel;

  private CategoryLabelCountMap categoryLabelCountMap = new CategoryLabelCountMap();

  private final List<String> countNames = new ArrayList<>();

  private String labelTitle = "Type";

  private final List<String> labels = new ArrayList<>();

  private int columnCount = 1;

  public LabelCountMapTableModel() {
  }

  public void addCount(final CharSequence label, final CharSequence countName) {
    addCount(label, countName, 1);
  }

  public void addCount(final CharSequence label, final CharSequence countName, final long count) {
    if (label != null && countName != null) {
      final LabelCountMap labelCountMap = getStatistics(label, countName);
      labelCountMap.addCount(label, count);
    }
  }

  public void addCount(final PathNameProxy pathNameProxy, final CharSequence countName) {
    addCount(pathNameProxy, countName, 1);
  }

  public void addCount(final PathNameProxy pathNameProxy, final CharSequence countName,
    final long count) {
    if (pathNameProxy != null) {
      final CharSequence label = pathNameProxy.getPathName();
      addCount(label, countName, count);
    }
  }

  private void addCountColumn(final CharSequence name) {
    Invoke.later(() -> {
      this.columnCount++;
      final int columnIndex = 1 + this.countNames.indexOf(name);
      fireTableStructureChanged();
      final TableColumn column = new TableColumnExt(columnIndex);
      setColumnWidth(columnIndex, column);
      final BaseJTable table = getTable();
      table.addColumn(column);
    });
  }

  public void addCountNameColumn(final CharSequence name) {
    getStatistics(name);
  }

  public boolean addCountNameColumn(final int index, final CharSequence countName) {
    boolean added = false;
    synchronized (this.countNames) {
      if (!this.countNames.contains(countName)) {
        added = true;
        this.countNames.add(index, countName.toString());
      }
    }
    if (added) {
      addCountColumn(countName);
    }
    return added;
  }

  public void clearCounts() {
    this.categoryLabelCountMap.clear();
  }

  public void clearCounts(final CharSequence countName) {
    final LabelCountMap labelCountMap = getStatistics(countName);
    labelCountMap.clearCounts();
  }

  @Override
  public Class<?> getColumnClass(final int columnIndex) {
    if (columnIndex == 0) {
      return String.class;
    } else {
      return Long.class;
    }
  }

  @Override
  public int getColumnCount() {
    return this.columnCount;
  }

  @Override
  public String getColumnName(final int columnIndex) {
    if (columnIndex < 1) {
      return this.labelTitle;
    } else {
      final int index = columnIndex - 1;
      if (index < this.countNames.size()) {
        return this.countNames.get(index);
      }
      return null;
    }
  }

  public Long getCount(final CharSequence label, final CharSequence countName) {
    return this.categoryLabelCountMap.getCount(countName, label);
  }

  public Counter getCounter(final CharSequence label, final CharSequence countName) {
    final LabelCountMap labelCountMap = getStatistics(label, countName);
    return labelCountMap.getCounter(label);
  }

  @Override
  public int getRowCount() {
    return this.labels.size();
  }

  public CategoryLabelCountMap getStatistics() {
    return this.categoryLabelCountMap;
  }

  public LabelCountMap getStatistics(final CharSequence name) {
    boolean added = false;
    try {
      synchronized (this.countNames) {
        if (!this.countNames.contains(name)) {
          added = true;
          this.countNames.add(name.toString());
        }
        return this.categoryLabelCountMap.getLabelCountMap(name);
      }
    } finally {
      if (added) {
        addCountColumn(name);
      }
    }
  }

  public LabelCountMap getStatistics(final CharSequence label, final CharSequence countName) {
    final LabelCountMap labelCountMap = getStatistics(countName);
    newTypePathRow(label);
    return labelCountMap;
  }

  public String getTypeNameTitle() {
    return this.labelTitle;
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    final String label = this.labels.get(rowIndex);
    switch (columnIndex) {
      case 0:
        return label;
      default:
        final String countName = this.countNames.get(columnIndex - 1);
        final Long value = getCount(label, countName);
        return value;
    }
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    return false;
  }

  public void newStatistics(final CharSequence countName, final CharSequence label) {
    addCountNameColumn(countName);
    newTypePathRow(label);
  }

  public BaseJTable newTable() {
    final BaseJTable table = new BaseJTable(this);
    setTable(table);
    final TableColumnModel columnModel = table.getColumnModel();
    for (int columnIndex = 0; columnIndex < getColumnCount(); columnIndex++) {
      final int columnIndex1 = columnIndex;
      final TableColumn column = columnModel.getColumn(columnIndex1);
      setColumnWidth(columnIndex1, column);
    }
    table.setAutoCreateColumnsFromModel(false);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    final RowSorter<? extends TableModel> rowSorter = table.getRowSorter();
    final SortKey sortKey = new SortKey(0, SortOrder.ASCENDING);
    rowSorter.setSortKeys(Arrays.asList(sortKey));

    table.addHighlighter(
      new ColorHighlighter((final Component renderer, final ComponentAdapter adapter) -> {
        final int row = adapter.convertRowIndexToModel(adapter.row);
        if (getValueAt(row, 0).equals(LabelCountMapTableModel.this.selectedLabel)) {
          return true;
        }
        return false;
      }, WebColors.ForestGreen, WebColors.Yellow, WebColors.DarkGreen, WebColors.Yellow));

    table.addHighlighter(
      new ColorHighlighter((final Component renderer, final ComponentAdapter adapter) -> {
        final int column = adapter.convertColumnIndexToModel(adapter.column);
        final int row = adapter.convertRowIndexToModel(adapter.row);
        if (getValueAt(row, 0).equals(LabelCountMapTableModel.this.selectedLabel)) {
          if (getColumnName(column).equals(LabelCountMapTableModel.this.selectedCountName)) {
            return true;
          }
        }
        return false;
      }, WebColors.Yellow, WebColors.DarkGreen, WebColors.Gold, WebColors.DarkGreen));
    return table;
  }

  public void newTypePathRow(final CharSequence label) {
    int index = -1;
    try {
      synchronized (this.labels) {
        final String labelString = label.toString();
        if (!this.labels.contains(labelString)) {
          index = this.labels.size();
          Invoke.andWait(() -> {
            this.labels.add(labelString);
          });
        }
      }
    } finally {
      if (index != -1) {
        fireTableRowsInserted(index, index);
      }
    }
  }

  public void refresh() {

    final int rowCount = getRowCount();
    if (rowCount > 0) {
      try {
        fireTableRowsUpdated(0, rowCount - 1);
        if (this.selectedCountName != null && this.selectedLabel != null) {
          selectLabelCountCell(this.selectedLabel, this.selectedCountName);
        }
      } catch (final Throwable t) {
      }
    }
  }

  @Override
  public void removeTableModelListener(final TableModelListener l) {
  }

  public void selectLabelCountCell(final CharSequence label, final CharSequence countName) {
    this.selectedLabel = label.toString();
    this.selectedCountName = countName.toString();
    final BaseJTable table = getTable();
    table.repaint();
  }

  public void selectLabelCountCell(final PathNameProxy pathNameProxy, final String countName) {
    final CharSequence label = pathNameProxy.getPathName();
    selectLabelCountCell(label, countName);
  }

  private void setColumnWidth(final int columnIndex, final TableColumn column) {
    int minWidth = 80;
    if (columnIndex == 0) {
      minWidth = 270;
    }
    final String columnName = getColumnName(columnIndex);
    final int width = Math.max(minWidth, columnName.length() * 8);
    column.setMinWidth(width);
    column.setPreferredWidth(width);
  }

  public void setLabelTitle(final String labelTitle) {
    this.labelTitle = labelTitle;
    fireTableStructureChanged();
  }

  public void setStatistics(final Map<String, LabelCountMap> statisticsMap) {
    try {
      this.categoryLabelCountMap = new CategoryLabelCountMap(statisticsMap);
      for (final Entry<String, LabelCountMap> entry : statisticsMap.entrySet()) {
        final String countName = entry.getKey();
        addCountNameColumn(countName);
        final LabelCountMap labelCountMap = entry.getValue();
        for (final String label : labelCountMap.getLabels()) {
          newStatistics(countName, label);
        }

      }
    } catch (final ConcurrentModificationException e) {

    }
    refresh();
  }

  public void setStatistics(final String statisticName, final LabelCountMap labelCountMap) {
    addCountNameColumn(statisticName);
    this.categoryLabelCountMap.setStatistics(statisticName, labelCountMap);
  }

  @Override
  public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
  }
}
