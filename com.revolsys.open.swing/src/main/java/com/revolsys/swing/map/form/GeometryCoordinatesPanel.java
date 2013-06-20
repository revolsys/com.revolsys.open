package com.revolsys.swing.map.form;

import java.awt.BorderLayout;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

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

  public GeometryCoordinatesPanel(final String fieldName) {
    super(fieldName, null);
    setLayout(new BorderLayout());

    model.addTableModelListener(this);
    table = new BaseJxTable(model);
    table.setAutoResizeMode(BaseJxTable.AUTO_RESIZE_OFF);

    tablePanel = new TablePanel(table);

    add(tablePanel, BorderLayout.WEST);
  }

  public BaseJxTable getTable() {
    return table;
  }

  public TablePanel getTablePanel() {
    return tablePanel;
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
      if (i < model.getNumIndexItems()) {
        table.setColumnWidth(i, (model.getRowCount() / 10 + 1) * 20);
      } else {
        table.setColumnWidth(i, 120);
      }
    }
  }
}
