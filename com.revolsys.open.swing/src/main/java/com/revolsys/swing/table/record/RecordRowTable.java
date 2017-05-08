package com.revolsys.swing.table.record;

import java.awt.event.MouseEvent;
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
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.table.model.RecordLayerTableModel;
import com.revolsys.swing.map.layer.record.table.predicate.ErrorPredicate;
import com.revolsys.swing.map.layer.record.table.predicate.ModifiedFieldPredicate;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.BaseColumnFactory;
import com.revolsys.swing.table.BaseJTable;
import com.revolsys.swing.table.TablePanel;
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
        if (eventRow != -1) {
          final RecordRowTableModel model = table.getTableModel();
          final V record = model.getRecord(eventRow);
          return record;
        }
      }
    }
    return null;
  }

  private TableCellRenderer cellRenderer;

  private RecordTableCellEditor tableCellEditor;

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

    final TableColumnModel columnModel = getColumnModel();
    this.tableCellEditor = newTableCellEditor();
    this.tableCellEditor.addCellEditorListener(model);
    for (int columnIndex = 0; columnIndex < columnModel.getColumnCount(); columnIndex++) {
      try {
        final TableColumn column = columnModel.getColumn(columnIndex);
        if (columnIndex >= model.getColumnFieldsOffset()) {
          column.setCellEditor(this.tableCellEditor);
        }
        column.setCellRenderer(cellRenderer);
      } catch (final ArrayIndexOutOfBoundsException e) {
      }
    }
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
    this.tableCellEditor = null;
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

  public LayerRecord getRecord(final int row) {
    final RecordLayerTableModel model = getTableModel();
    if (model == null) {
      return null;
    } else {
      return model.getRecord(row);
    }
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

  public RecordTableCellEditor getTableCellEditor() {
    return this.tableCellEditor;
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

  protected RecordTableCellEditor newTableCellEditor() {
    return new RecordTableCellEditor(this);
  }

  @Override
  public void setRowSorter(final RowSorter<? extends TableModel> sorter) {
    super.setRowSorter(sorter);
    if (sorter != null) {
      final SortController<?> sortController = getSortController();
      if (sortController != null) {
        final RecordRowTableModel model = getTableModel();
        sortController.resetSortOrders();
        for (final Entry<Integer, SortOrder> entry : model.getSortedColumns().entrySet()) {
          final int index = entry.getKey();
          final SortOrder sortOrder = entry.getValue();
          sortController.setSortOrder(index, sortOrder);
        }
      }
    }
  }

  @Override
  public void tableChanged(final TableModelEvent event) {
    Invoke.later(() -> {
      final RecordRowTableModel model = getModel();
      final int fieldsOffset = model.getColumnFieldsOffset();

      tableChangedDo(event);
      final int type = event.getType();
      final int eventColumn = event.getColumn();
      final int row = event.getFirstRow();
      if (type == TableModelEvent.UPDATE && eventColumn == TableModelEvent.ALL_COLUMNS
        && row == TableModelEvent.HEADER_ROW) {
        createDefaultColumnsFromModel();
        final TableColumnModel columnModel = getColumnModel();
        for (int columnIndex = 0; columnIndex < model.getColumnCount(); columnIndex++) {
          final TableColumn column = columnModel.getColumn(columnIndex);
          if (columnIndex >= fieldsOffset) {
            column.setCellEditor(this.tableCellEditor);
          }
          column.setCellRenderer(this.cellRenderer);
        }
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
