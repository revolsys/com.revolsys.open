package com.revolsys.swing.map.form;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.revolsys.swing.table.geometry.GeometryCoordinatesTableModel;
import com.vividsolutions.jts.geom.Geometry;

@SuppressWarnings("serial")
public class GeometryCoordinatesPanel extends JPanel {
  private final GeometryCoordinatesTableModel model = new GeometryCoordinatesTableModel();

  public GeometryCoordinatesPanel() {
    super(new BorderLayout());

    final JTable table = new JTable(model);

    final JScrollPane scrollPane = new JScrollPane(table);
    add(scrollPane, BorderLayout.EAST);
  }

  public void setGeometry(final Geometry geometry) {
    model.setGeometry(geometry);
  }
}
