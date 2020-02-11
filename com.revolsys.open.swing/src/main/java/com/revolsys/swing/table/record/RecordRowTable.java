package com.revolsys.swing.table.record;

import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.sort.SortController;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.record.Record;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.listener.BaseMouseListener;
import com.revolsys.swing.map.layer.record.table.model.RecordLayerTableModel;
import com.revolsys.swing.map.layer.record.table.predicate.ErrorPredicate;
import com.revolsys.swing.map.layer.record.table.predicate.ModifiedFieldPredicate;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.BaseColumnFactory;
import com.revolsys.swing.table.BaseJTable;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.editor.BaseTableCellEditor;
import com.revolsys.swing.table.record.editor.RecordTableCellEditor;
import com.revolsys.swing.table.record.model.RecordRowTableModel;
import com.revolsys.swing.table.record.renderer.RecordRowTableCellRenderer;

public class RecordRowTable extends BaseJTable implements BaseMouseListener {
  private static final long serialVersionUID = 1L;

  public static <V extends Record> V getEventRecord() {
    final BaseJTable eventTable = TablePanel.getEventTable();
    if (eventTable instanceof RecordRowTable) {
      final RecordRowTable table = (RecordRowTable)eventTable;
      if (table != null) {
        final int eventRow = TablePanel.getEventRow();
        return table.getRecord(eventRow);
      }
    }
    return null;
  }

  private TableCellRenderer cellRenderer;

  private boolean showDisplayValues = true;

  public RecordRowTable(final RecordRowTableModel model) {
    this(model, new RecordRowTableCellRenderer());
  }

  public RecordRowTable(final RecordRowTableModel model, final TableCellRenderer cellRenderer) {
    super(model);
    setColumnFactory(new BaseColumnFactory());
    this.cellRenderer = cellRenderer;
    setSortable(false);
    setShowHorizontalLines(false);
    setRowMargin(0);

    final JTableHeader tableHeader = getTableHeader();

    refreshColumnModel();
    tableHeader.addMouseListener(this);

    addNewRecordHighlighter();
    addModifiedRecordHighlighter();
    addDeletedRecordHighlighter();
    ModifiedFieldPredicate.add(this);

    ErrorPredicate.add(this);
  }

  protected void addDeletedRecordHighlighter() {
  }

  protected void addModifiedRecordHighlighter() {
  }

  protected void addNewRecordHighlighter() {
  }

  @Override
  public void dispose() {
    super.dispose();
    this.cellRenderer = null;
  }

  public FieldDefinition getColumnFieldDefinition(final int columnIndex) {
    final RecordRowTableModel model = getModel();
    return model.getColumnFieldDefinition(columnIndex);
  }

  public String getColumnFieldName(final int columnIndex) {
    final RecordRowTableModel model = getModel();
    return model.getColumnFieldName(columnIndex);
  }

  @Override
  public RecordRowTableModel getModel() {
    return (RecordRowTableModel)super.getModel();
  }

  public <V extends Record> V getRecord(final int row) {
    if (row >= 0) {
      final RecordLayerTableModel model = getTableModel();
      if (model != null) {
        return model.getRecord(row);
      }
    }
    return null;
  }

  public RecordDefinition getRecordDefinition() {
    final RecordRowTableModel model = getModel();
    return model.getRecordDefinition();
  }

  public Record getSelectedRecord() {
    final int row = getSelectedRow();
    if (row == -1) {
      return null;
    } else {
      final RecordRowTableModel tableModel = getTableModel();
      return tableModel.getRecord(row);
    }
  }

  @Override
  protected void initializeColumnPreferredWidth(final TableColumn column) {
    super.initializeColumnPreferredWidth(column);
    final RecordRowTableModel model = getTableModel();
    final int columnIndex = column.getModelIndex();
    final FieldDefinition fieldDefinition = model.getColumnFieldDefinition(columnIndex);
    if (fieldDefinition != null) {
      Integer columnWidth = fieldDefinition.getProperty("tableColumnWidth");
      final String columnName = fieldDefinition.getTitle();
      if (columnWidth == null) {
        columnWidth = fieldDefinition.getMaxStringLength() * 7;
        columnWidth = Math.min(columnWidth, 200);
        fieldDefinition.setProperty("tableColumnWidth", columnWidth);
      }
      final int nameWidth = columnName.length() * 8 + 15;
      column.setPreferredWidth(Math.max(nameWidth, columnWidth));
    }
  }

  public boolean isShowDisplayValues() {
    return this.showDisplayValues;
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    final Object source = e.getSource();
    if (source == getTableHeader()) {
      final RecordRowTableModel model = getModel();
      final int columnIndex = columnAtPoint(e.getPoint());
      if (columnIndex > -1 && SwingUtilities.isLeftMouseButton(e)) {
        final Class<?> fieldClass = model.getColumnClass(columnIndex);
        if (!Geometry.class.isAssignableFrom(fieldClass)) {
          model.setSortOrder(columnIndex);
        }
      }
    }
  }

  @Override
  protected BaseTableCellEditor newTableCellEditor() {
    return new RecordTableCellEditor(this);
  }

  protected void refreshColumnModel() {
    final RecordRowTableModel model = getModel();
    final int columnFieldsOffset = model.getColumnFieldsOffset();
    final TableColumnModel columnModel = getColumnModel();
    for (int columnIndex = 0; columnIndex < columnModel.getColumnCount(); columnIndex++) {
      try {
        final TableColumn column = columnModel.getColumn(columnIndex);
        if (columnIndex >= columnFieldsOffset) {
          final BaseTableCellEditor cellEditor = getCellEditor(columnIndex);
          column.setCellEditor(cellEditor);
        }
        column.setCellRenderer(this.cellRenderer);
      } catch (final ArrayIndexOutOfBoundsException e) {
      }
    }
  }

  @Override
  public void setRowSorter(final RowSorter<? extends TableModel> sorter) {
    super.setRowSorter(sorter);
    if (sorter != null) {
      final SortController<?> sortController = getSortController();
      if (sortController != null) {
        final RecordRowTableModel model = getTableModel();
        sortController.resetSortOrders();
        final Map<Integer, SortOrder> sortedColumns = model.getSortedColumns();
        for (final Entry<Integer, SortOrder> entry : sortedColumns.entrySet()) {
          final int index = entry.getKey();
          if (index < model.getColumnCount()) {
            final SortOrder sortOrder = entry.getValue();
            sortController.setSortOrder(index, sortOrder);
          }
        }
      }
    }
  }

  public void setShowDisplayValues(final boolean showDisplayValues) {
    final boolean oldValue = showDisplayValues;
    this.showDisplayValues = showDisplayValues;
    firePropertyChange("showDisplayValues", oldValue, showDisplayValues);
    repaint();
  }

  @Override
  public void tableChanged(final TableModelEvent event) {
    Invoke.later(() -> {
      tableChangedDo(event);
      final int type = event.getType();
      final int eventColumn = event.getColumn();
      final int row = event.getFirstRow();
      if (type == TableModelEvent.UPDATE && eventColumn == TableModelEvent.ALL_COLUMNS
        && row == TableModelEvent.HEADER_ROW) {
        createDefaultColumnsFromModel();
        refreshColumnModel();
        initializeColumnWidths();
      }
      if (this.tableHeader != null) {
        this.tableHeader.resizeAndRepaint();
      }
    });
  }

  protected void tableChangedDo(final TableModelEvent event) {
    try {
      super.tableChanged(event);
    } catch (final Throwable t) {
    }
  }

  @Override
  public String toString() {
    return getRecordDefinition().getPath();
  }
}
