package com.revolsys.swing.table.dataobject.row;

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

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.map.layer.dataobject.table.model.DataObjectLayerTableModel;
import com.revolsys.swing.map.layer.dataobject.table.predicate.ErrorPredicate;
import com.revolsys.swing.map.layer.dataobject.table.predicate.ModifiedAttributePredicate;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.table.BaseJxTable;
import com.revolsys.swing.table.dataobject.editor.DataObjectTableCellEditor;
import com.revolsys.swing.table.dataobject.model.DataObjectRowTableModel;
import com.revolsys.swing.table.dataobject.renderer.DataObjectRowTableCellRenderer;
import com.vividsolutions.jts.geom.Geometry;

public class DataObjectRowTable extends BaseJxTable implements MouseListener {
  private static final long serialVersionUID = 1L;

  private final ListSelectionModel defaultSeletionModel = getSelectionModel();

  private final DataObjectTableCellEditor tableCellEditor;

  public DataObjectRowTable(final DataObjectRowTableModel model) {
    this(model, new DataObjectRowTableCellRenderer());
  }

  public DataObjectRowTable(final DataObjectRowTableModel model,
    final TableCellRenderer cellRenderer) {
    super(model);
    setSortable(false);

    final JTableHeader tableHeader = getTableHeader();

    final TableColumnModel columnModel = getColumnModel();
    tableCellEditor = new DataObjectTableCellEditor(this);
    tableCellEditor.addCellEditorListener(model);
    for (int columnIndex = 0; columnIndex < model.getColumnCount(); columnIndex++) {
      final TableColumn column = columnModel.getColumn(columnIndex);
      if (columnIndex >= model.getAttributesOffset()) {
        column.setCellEditor(tableCellEditor);
      }
      column.setCellRenderer(cellRenderer);
    }
    tableHeader.addMouseListener(this);
    model.setTable(this);

    ModifiedAttributePredicate.add(this);
    ErrorPredicate.add(this);

  }

  public DataObjectMetaData getMetaData() {
    final DataObjectRowTableModel model = (DataObjectRowTableModel)getModel();
    return model.getMetaData();
  }

  public DataObject getSelectedRecord() {
    final int row = getSelectedRow();
    if (row == -1) {
      return null;
    } else {
      final DataObjectRowTableModel tableModel = getTableModel();
      return tableModel.getRecord(row);
    }
  }

  @Override
  public ListSelectionModel getSelectionModel() {
    if (getTableModel() instanceof DataObjectLayerTableModel) {
      final DataObjectLayerTableModel layerTableModel = (DataObjectLayerTableModel)getTableModel();
      if (layerTableModel.getAttributeFilterMode().equals(
        DataObjectLayerTableModel.MODE_SELECTED)) {
        return layerTableModel.getHighlightedModel();
      }
    }
    return super.getSelectionModel();
  }

  public DataObjectTableCellEditor getTableCellEditor() {
    return tableCellEditor;
  }

  @Override
  protected void initializeColumnPreferredWidth(final TableColumn column) {
    super.initializeColumnPreferredWidth(column);
    final DataObjectRowTableModel model = getTableModel();
    final DataObjectMetaData metaData = model.getMetaData();
    final int viewIndex = column.getModelIndex();
    final int attributesOffset = model.getAttributesOffset();
    if (viewIndex < attributesOffset) {
      final String attributeName = model.getFieldName(viewIndex
        - attributesOffset);
      final Attribute attribute = metaData.getAttribute(attributeName);
      if (attribute != null) {
        Integer columnWidth = attribute.getProperty("tableColumnWidth");
        final String columnName = attribute.getTitle();
        if (columnWidth == null) {
          columnWidth = attribute.getMaxStringLength() * 7;
          columnWidth = Math.min(columnWidth, 200);
          attribute.setProperty("tableColumnWidth", columnWidth);
        }
        column.setMinWidth(columnName.length() * 7 + 15);
        column.setPreferredWidth(columnWidth);
      }
    }
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    if (e.getSource() == getTableHeader()) {
      final DataObjectRowTableModel model = (DataObjectRowTableModel)getModel();
      final DataObjectMetaData metaData = model.getMetaData();
      final int column = columnAtPoint(e.getPoint());
      if (column > -1 && SwingUtilities.isLeftMouseButton(e)) {
        final int index = convertColumnIndexToModel(column);
        final Class<?> attributeClass = metaData.getAttributeClass(index);
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
      if (model instanceof DataObjectLayerTableModel) {
        final DataObjectLayerTableModel layerModel = (DataObjectLayerTableModel)model;
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
