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

import com.revolsys.geometry.model.Geometry;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.table.BaseJTable;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.editor.BaseTableCellEditor;
import com.revolsys.swing.table.geometry.GeometryCoordinatesTableModel;
import com.revolsys.swing.table.renderer.BaseTableCellRenderer;
import com.revolsys.util.Property;

public class GeometryCoordinatesPanel extends ValueField implements TableModelListener {
  private static final long serialVersionUID = 1L;

  final BaseTableCellEditor cellEditor;

  final TableCellRenderer cellRenderer = new BaseTableCellRenderer();

  private final Reference<LayerRecordForm> form;

  private final GeometryCoordinatesTableModel model = new GeometryCoordinatesTableModel(this);

  private final BaseJTable table;

  private final TablePanel tablePanel;

  public GeometryCoordinatesPanel(final LayerRecordForm form, final String fieldName) {
    super(new BorderLayout(), fieldName, null);
    setLayout(new BorderLayout());

    this.form = new WeakReference<>(form);
    this.model.addTableModelListener(this);
    Property.addListener(this.model, form);
    this.model.setForm(form);
    this.table = new BaseJTable(this.model);
    this.table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    this.cellEditor = new BaseTableCellEditor(this.table);
    tableChanged(null);
    this.tablePanel = new TablePanel(this.table);
    GeometryCoordinateErrorPredicate.add(this.table);
    add(this.tablePanel, BorderLayout.WEST);
  }

  public LayerRecordForm getForm() {
    return this.form.get();
  }

  public BaseJTable getTable() {
    return this.table;
  }

  public TablePanel getTablePanel() {
    return this.tablePanel;
  }

  @Override
  public void setFieldInvalid(final String message, final Color foregroundColor,
    final Color backgroundColor) {
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
  public boolean setFieldValue(final Object value) {
    if (value instanceof Geometry) {
      final Geometry geometry = (Geometry)value;
      this.model.setGeometry(geometry);
      final TableColumnModel columnModel = this.table.getColumnModel();
      for (int columnIndex = 0; columnIndex < this.model.getColumnCount(); columnIndex++) {
        final TableColumn column = columnModel.getColumn(columnIndex);
        column.setCellEditor(this.cellEditor);
        column.setCellRenderer(this.cellRenderer);
      }
    }
    return super.setFieldValue(value);
  }

  @Override
  public void tableChanged(final TableModelEvent e) {
    for (int columnIndex = 0; columnIndex < this.model.getColumnCount(); columnIndex++) {
      int width;
      final TableColumn column = this.table.getColumn(columnIndex);
      if (columnIndex < this.model.getNumIndexItems()) {

        width = (int)Math.ceil(Math.log10(this.model.getRowCount())) * 20;
        if (columnIndex < this.model.getNumIndexItems() - 1) {
        } else {

          width += 20;
        }
        width = Math.max(50, width);
        column.setCellRenderer(this.cellRenderer);
      } else {
        width = 120;
        column.setCellEditor(this.cellEditor);
        column.setCellRenderer(this.cellRenderer);
      }
      this.table.setColumnWidth(columnIndex, width);
    }
  }
}
