package com.revolsys.swing.map.form;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import com.revolsys.awt.WebColors;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.table.BaseJxTable;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.geometry.GeometryCoordinatesTableModel;
import com.vividsolutions.jts.geom.Geometry;

@SuppressWarnings("serial")
public class GeometryCoordinatesPanel extends ValueField implements
  TableModelListener {
  private final GeometryCoordinatesTableModel model = new GeometryCoordinatesTableModel();

  private final BaseJxTable table;

  private final TablePanel tablePanel;

  private final DataObjectLayerForm form;

  public GeometryCoordinatesPanel(final DataObjectLayerForm form,
    final String fieldName) {
    super(fieldName, null);
    setLayout(new BorderLayout());

    this.form = form;
    this.model.addTableModelListener(this);
    this.model.setForm(form);
    this.table = new BaseJxTable(model);
    table.setAutoResizeMode(BaseJxTable.AUTO_RESIZE_OFF);

    tablePanel = new TablePanel(table);

    add(tablePanel, BorderLayout.WEST);
  }

  public DataObjectLayerForm getForm() {
    return form;
  }

  public BaseJxTable getTable() {
    return table;
  }

  public TablePanel getTablePanel() {
    return tablePanel;
  }

  @Override
  public void setFieldInvalid(final String message) {
    super.setFieldInvalid(message);
    setForeground(null);
    setBackground(null);
    tablePanel.setBorder(BorderFactory.createLineBorder(WebColors.Red, 3));
  }

  @Override
  public void setFieldValid() {
    tablePanel.setBorder(null);
    super.setFieldValid();
  }

  @Override
  public void setFieldValue(final Object value) {
    if (value instanceof Geometry) {
      final Geometry geometry = (Geometry)value;
      model.setGeometry(geometry);
    }
    super.setFieldValue(value);
  }

  @Override
  public void tableChanged(final TableModelEvent e) {
    for (int i = 0; i < model.getColumnCount(); i++) {
      int width;
      if (i < model.getNumIndexItems()) {
        width = (int)(Math.ceil(model.getRowCount() / 10.0)) * 20;
        if (i < model.getNumIndexItems() - 1) {
        } else {
          width += 20;
        }
        width = Math.max(50, width);
      } else {
        width = 120;
      }
      table.setColumnWidth(i, width);
    }
  }
}
