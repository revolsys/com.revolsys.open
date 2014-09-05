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

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.map.layer.record.table.model.RecordLayerTableModel;
import com.revolsys.swing.map.layer.record.table.predicate.ErrorPredicate;
import com.revolsys.swing.map.layer.record.table.predicate.ModifiedAttributePredicate;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.BaseJxTable;
import com.revolsys.swing.table.record.editor.RecordTableCellEditor;
import com.revolsys.swing.table.record.model.RecordRowTableModel;
import com.revolsys.swing.table.record.renderer.RecordRowTableCellRenderer;

public class RecordRowTable extends BaseJxTable implements MouseListener {
  private static final long serialVersionUID = 1L;

  private final RecordTableCellEditor tableCellEditor;

  public RecordRowTable(final RecordRowTableModel model) {
    this(model, new RecordRowTableCellRenderer());
  }

  public RecordRowTable(final RecordRowTableModel model,
    final TableCellRenderer cellRenderer) {
    super(model);
    setSortable(false);

    final JTableHeader tableHeader = getTableHeader();

    final TableColumnModel columnModel = getColumnModel();
    this.tableCellEditor = new RecordTableCellEditor(this);
    this.tableCellEditor.addCellEditorListener(model);
    for (int columnIndex = 0; columnIndex < model.getColumnCount(); columnIndex++) {
      final TableColumn column = columnModel.getColumn(columnIndex);
      if (columnIndex >= model.getAttributesOffset()) {
        column.setCellEditor(this.tableCellEditor);
      }
      column.setCellRenderer(cellRenderer);
    }
    tableHeader.addMouseListener(this);
    model.setTable(this);

    ModifiedAttributePredicate.add(this);
    ErrorPredicate.add(this);

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
      if (layerTableModel.getAttributeFilterMode().equals(
        RecordLayerTableModel.MODE_SELECTED)) {
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
    final int attributesOffset = model.getAttributesOffset();
    if (viewIndex < attributesOffset) {
      final String attributeName = model.getFieldName(viewIndex
        - attributesOffset);
      final Attribute attribute = recordDefinition.getAttribute(attributeName);
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
        final Class<?> attributeClass = recordDefinition.getAttributeClass(index);
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
    if (SwingUtil.isEventDispatchThread()) {
      final TableModel model = getModel();
      if (model instanceof RecordLayerTableModel) {
        final RecordLayerTableModel layerModel = (RecordLayerTableModel)model;
        final String mode = layerModel.getAttributeFilterMode();
        final List<String> sortableModes = layerModel.getSortableModes();
        if (sortableModes.contains(mode)) {
          setSortable(true);
        } else {
          setSortable(false);
        }
      }
      try {
        super.tableChanged(event);
      } catch (final Throwable t) {
      }
      if (this.tableHeader != null) {
        this.tableHeader.resizeAndRepaint();
      }
    } else {
      Invoke.later(this, "tableChanged", event);
    }
  }
}
