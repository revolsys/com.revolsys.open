package com.revolsys.swing.map.layer.elevation.gridded;

import java.awt.Color;

import com.revolsys.awt.gradient.GradientStop;
import com.revolsys.awt.gradient.MultiStopLinearGradient;
import com.revolsys.elevation.gridded.rasterizer.ColorRampGriddedElevationModelRasterizer;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.table.LambdaTableModel;
import com.revolsys.swing.table.TablePanel;

public class ColorRampStylePanel extends ValueField {

  public ColorRampStylePanel(final ColorRampGriddedElevationModelRasterizer style) {
    super("colorRanges", null);
    final MultiStopLinearGradient gradient = (MultiStopLinearGradient)style.getGradient();
    final LambdaTableModel<GradientStop> tableModel = new LambdaTableModel<GradientStop>(
      gradient.getStops());

    tableModel.addColumnIndex();

    tableModel.addColumn("Elevation", Double.TYPE, GradientStop::getPercent,
      GradientStop::setPercent);

    tableModel
      .addColumn("Color", Color.class, GradientStop::getColor, GradientStop::setColor,
        new ColorTableCellEditor())//
      .setCellRenderer(new ColorTableCellRenderer());

    tableModel.addMenuItem("row", "Delete", "delete", (rowIndex, columnIndex) -> {
      gradient.removeStop(rowIndex);
      tableModel.fireTableRowsDeleted(rowIndex, columnIndex);
    });

    final TablePanel tablePanel = tableModel.newTablePanel();
    add(tablePanel);
  }

}
