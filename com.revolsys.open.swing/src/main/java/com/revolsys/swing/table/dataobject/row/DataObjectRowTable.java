package com.revolsys.swing.table.dataobject.row;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.swing.map.table.DataObjectLayerTableModel;
import com.revolsys.swing.table.BaseJxTable;
import com.vividsolutions.jts.geom.Geometry;

public class DataObjectRowTable extends BaseJxTable implements MouseListener {
  private static final long serialVersionUID = 1L;

  public DataObjectRowTable(final DataObjectRowTableModel model) {
    super(model);
    setSortable(false);

    final DataObjectMetaData metaData = model.getMetaData();

    final DataObjectRowTableCellRenderer cellRenderer = new DataObjectRowTableCellRenderer();
    final JTableHeader tableHeader = getTableHeader();

    final List<TableColumn> removeColumns = new ArrayList<TableColumn>();
    final TableColumnModel columnModel = getColumnModel();
    for (int i = 0; i < model.getColumnCount(); i++) {
      final TableColumn column = columnModel.getColumn(i);
      final Class<?> attributeClass = metaData.getAttributeClass(i);
      if (Geometry.class.isAssignableFrom(attributeClass)) {
        removeColumns.add(column);
      } else {
        final String columnName = model.getColumnName(i) + "XX";

        final Attribute attribute = model.getColumnAttribute(i);
        final int attributeLength = Math.min(
          Math.max(columnName.length(), attribute.getMaxStringLength()), 40) + 2;
        final StringBuffer text = new StringBuffer(attributeLength);
        for (int j = 0; j < attributeLength + 2; j++) {
          text.append('X');
        }

        final Component c = tableHeader.getDefaultRenderer()
          .getTableCellRendererComponent(null, text.toString(), false, false,
            0, 0);
        column.setMinWidth(c.getMinimumSize().width);
        column.setMaxWidth(c.getMaximumSize().width);
        column.setPreferredWidth(c.getPreferredSize().width);
        column.setWidth(column.getPreferredWidth());

        column.setCellRenderer(cellRenderer);
      }
    }
    for (final TableColumn column : removeColumns) {
      removeColumn(column);
    }
    tableHeader.addMouseListener(this);
    model.addTableModelListener(this);
  }

  public DataObjectMetaData getMetaData() {
    final DataObjectRowTableModel model = (DataObjectRowTableModel)getModel();
    return model.getMetaData();
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
          model.setSortOrder(column);
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
  public void tableChanged(final TableModelEvent e) {
    final TableModel model = getModel();
    if (model instanceof DataObjectLayerTableModel) {
      final DataObjectLayerTableModel layerModel = (DataObjectLayerTableModel)model;
      if (layerModel.getMode().equals(DataObjectLayerTableModel.MODE_ALL)) {
        setSortable(false);
      } else {
        setSortable(true);
      }
    }
    super.tableChanged(e);
    if (tableHeader != null) {
      tableHeader.resizeAndRepaint();
    }
  }
}
