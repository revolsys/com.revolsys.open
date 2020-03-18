package com.revolsys.swing.map.layer.elevation.gridded;

import java.awt.Color;

import com.revolsys.elevation.gridded.rasterizer.ColorGradientGriddedElevationModelRasterizer;
import com.revolsys.elevation.gridded.rasterizer.gradient.GradientStop;
import com.revolsys.elevation.gridded.rasterizer.gradient.MultiStopLinearGradient;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.lambda.LambdaRowTableModel;

public class ColorGradientStylePanel extends ValueField {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public ColorGradientStylePanel(final ColorGradientGriddedElevationModelRasterizer style) {
    super("gradient", null);
    final MultiStopLinearGradient gradient = (MultiStopLinearGradient)style.getGradient();
    final LambdaRowTableModel<GradientStop> tableModel = new LambdaRowTableModel<>(
      gradient.getStops());

    tableModel.addColumnIndex();

    tableModel.addColumn("Elevation", Double.TYPE, GradientStop::getValue, GradientStop::setValue);

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
