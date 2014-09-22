package com.revolsys.swing.map.form;

import java.awt.BorderLayout;
import java.awt.Color;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.table.BaseJxTable;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.geometry.GeometryCoordinatesTableModel;
import com.revolsys.swing.table.renderer.BaseTableCellRenderer;

public class GeometryCoordinatesPanel extends ValueField implements
TableModelListener {
  private static final long serialVersionUID = 1L;

  private final GeometryCoordinatesTableModel model = new GeometryCoordinatesTableModel();

  private final BaseJxTable table;

  private final TablePanel tablePanel;

  private final Reference<LayerRecordForm> form;

  public GeometryCoordinatesPanel(final LayerRecordForm form,
    final String fieldName) {
    super(fieldName, null);
    setLayout(new BorderLayout());

    this.form = new WeakReference<>(form);
    this.model.addTableModelListener(this);
    this.model.setForm(form);
    this.table = new BaseJxTable(this.model);
    this.table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    this.tablePanel = new TablePanel(this.table);

    add(this.tablePanel, BorderLayout.WEST);
  }

  public LayerRecordForm getForm() {
    return this.form.get();
  }

  public BaseJxTable getTable() {
    return this.table;
  }

  public TablePanel getTablePanel() {
    return this.tablePanel;
  }

  @Override
  public void setFieldInvalid(final String message,
    final Color foregroundColor, final Color backgroundColor) {
    super.setFieldInvalid(message, foregroundColor, backgroundColor);
    setForeground(null);
    setBackground(null);
    this.tablePanel.setBorder(BorderFactory.createLineBorder(foregroundColor, 3));
  }

  @Override
  public void setFieldValid() {
    this.tablePanel.setBorder(null);
    super.setFieldValid();
  }

  @Override
  public void setFieldValue(final Object value) {
    if (value instanceof Geometry) {
      final Geometry geometry = (Geometry)value;
      this.model.setGeometry(geometry);
      final TableCellRenderer cellRenderer = new BaseTableCellRenderer();
      final TableColumnModel columnModel = this.table.getColumnModel();
      for (int columnIndex = 0; columnIndex < this.model.getColumnCount(); columnIndex++) {
        final TableColumn column = columnModel.getColumn(columnIndex);

        column.setCellRenderer(cellRenderer);
      }
    }
    super.setFieldValue(value);
  }

  @Override
  public void tableChanged(final TableModelEvent e) {
    for (int i = 0; i < this.model.getColumnCount(); i++) {
      int width;
      if (i < this.model.getNumIndexItems()) {

        width = (int)Math.ceil(Math.log10(this.model.getRowCount())) * 20;
        if (i < this.model.getNumIndexItems() - 1) {
        } else {
          width += 20;
        }
        width = Math.max(50, width);
      } else {
        width = 120;
      }
      this.table.setColumnWidth(i, width);
    }
  }
}
