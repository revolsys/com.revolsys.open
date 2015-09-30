package com.revolsys.swing.table.record.row;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.record.Record;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.map.layer.record.table.model.RecordLayerTableModel;
import com.revolsys.swing.map.layer.record.table.predicate.ErrorPredicate;
import com.revolsys.swing.map.layer.record.table.predicate.ModifiedAttributePredicate;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.BaseJTable;
import com.revolsys.swing.table.record.editor.RecordTableCellEditor;
import com.revolsys.swing.table.record.model.RecordRowTableModel;
import com.revolsys.swing.table.record.renderer.RecordRowTableCellRenderer;

public class RecordRowTable extends BaseJTable implements MouseListener {
  private static final long serialVersionUID = 1L;

  private TableCellRenderer cellRenderer;

  private RecordTableCellEditor tableCellEditor;

  public RecordRowTable(final RecordRowTableModel model) {
    this(model, new RecordRowTableCellRenderer());
  }

  public RecordRowTable(final RecordRowTableModel model, final TableCellRenderer cellRenderer) {
    super(model);
    this.cellRenderer = cellRenderer;
    setSortable(false);

    final JTableHeader tableHeader = getTableHeader();

    final TableColumnModel columnModel = getColumnModel();
    this.tableCellEditor = createTableCellEditor();
    this.tableCellEditor.addCellEditorListener(model);
    for (int columnIndex = 0; columnIndex < model.getColumnCount(); columnIndex++) {
      final TableColumn column = columnModel.getColumn(columnIndex);
      if (columnIndex >= model.getFieldsOffset()) {
        column.setCellEditor(this.tableCellEditor);
      }
      column.setCellRenderer(cellRenderer);
    }
    tableHeader.addMouseListener(this);
    model.setTable(this);

    ModifiedAttributePredicate.add(this);
    ErrorPredicate.add(this);
  }

  protected RecordTableCellEditor createTableCellEditor() {
    return new RecordTableCellEditor(this);
  }

  @Override
  public void dispose() {
    super.dispose();
    this.tableCellEditor = null;
    this.cellRenderer = null;
  }

  public RecordDefinition getRecordDefinition() {
    final RecordRowTableModel model = (RecordRowTableModel)getModel();
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
  public ListSelectionModel getSelectionModel() {
    if (getTableModel() instanceof RecordLayerTableModel) {
      final RecordLayerTableModel layerTableModel = (RecordLayerTableModel)getTableModel();
      if (layerTableModel.getFieldFilterMode().equals(RecordLayerTableModel.MODE_SELECTED)) {
        return layerTableModel.getHighlightedModel();
      }
    }
    return super.getSelectionModel();
  }

  public RecordTableCellEditor getTableCellEditor() {
    return this.tableCellEditor;
  }

  @Override
  protected void initializeColumnPreferredWidth(final TableColumn column) {
    super.initializeColumnPreferredWidth(column);
    final RecordRowTableModel model = getTableModel();
    final RecordDefinition recordDefinition = model.getRecordDefinition();
    final int viewIndex = column.getModelIndex();
    final int attributesOffset = model.getFieldsOffset();
    if (viewIndex < attributesOffset) {
      final String fieldName = model.getFieldName(viewIndex - attributesOffset);
      final FieldDefinition attribute = recordDefinition.getField(fieldName);
      if (attribute != null) {
        Integer columnWidth = attribute.getProperty("tableColumnWidth");
        final String columnName = attribute.getTitle();
        if (columnWidth == null) {
          columnWidth = attribute.getMaxStringLength() * 7;
          columnWidth = Math.min(columnWidth, 200);
          attribute.setProperty("tableColumnWidth", columnWidth);
        }
        final int nameWidth = columnName.length() * 8 + 15;
        column.setMinWidth(nameWidth);
        column.setPreferredWidth(Math.max(nameWidth, columnWidth));
      }
    }
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    if (e.getSource() == getTableHeader()) {
      final RecordRowTableModel model = (RecordRowTableModel)getModel();
      final RecordDefinition recordDefinition = model.getRecordDefinition();
      final int column = columnAtPoint(e.getPoint());
      if (column > -1 && SwingUtilities.isLeftMouseButton(e)) {
        final int index = convertColumnIndexToModel(column);
        final Class<?> attributeClass = recordDefinition.getFieldClass(index);
        if (!Geometry.class.isAssignableFrom(attributeClass)) {
          model.setSortOrder(index);
        }
      }
    }
  }

  @Override
  public void mouseEntered(final MouseEvent e) {
  }

  @Override
  public void mouseExited(final MouseEvent e) {
  }

  @Override
  public void mousePressed(final MouseEvent e) {
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
  }

  @Override
  public void tableChanged(final TableModelEvent event) {
    Invoke.later(() -> {
      final TableModel model = getModel();
      int fieldsOffset = 0;
      if (model instanceof RecordLayerTableModel) {
        final RecordLayerTableModel layerModel = (RecordLayerTableModel)model;
        final String mode = layerModel.getFieldFilterMode();
        final List<String> sortableModes = layerModel.getSortableModes();
        if (sortableModes.contains(mode)) {
          setSortable(true);
        } else {
          setSortable(false);
        }
        fieldsOffset = layerModel.getFieldsOffset();
      }

      try {
        super.tableChanged(event);
      } catch (final Throwable t) {
      }
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

  @Override
  public String toString() {
    return getRecordDefinition().getPath();
  }
}
