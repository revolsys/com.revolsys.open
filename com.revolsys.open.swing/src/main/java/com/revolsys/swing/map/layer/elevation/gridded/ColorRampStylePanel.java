package com.revolsys.swing.map.layer.elevation.gridded;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.revolsys.elevation.gridded.rasterizer.ColorRampGriddedElevationModelRasterizer;
import com.revolsys.elevation.gridded.rasterizer.ColorRange;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.table.LambdaTableModel;
import com.revolsys.swing.table.TablePanel;

public class ColorRampStylePanel extends ValueField {

  private final ColorRampGriddedElevationModelRasterizer style;

  private final List<ColorRange> colorRanges;

  public ColorRampStylePanel(final ColorRampGriddedElevationModelRasterizer style) {
    super("colorRanges", null);
    this.style = style;
    this.colorRanges = new ArrayList<>(style.getColorRanges());
    final LambdaTableModel<ColorRange> tableModel = new LambdaTableModel<ColorRange>(
      this.colorRanges);

    tableModel.addColumnIndex();

    tableModel.addColumn("Elevation", Double.TYPE, ColorRange::getPercent, ColorRange::setPercent);

    tableModel
      .addColumn("Color", Color.class, ColorRange::getColor, ColorRange::setColor,
        new ColorTableCellEditor())//
      .setCellRenderer(new ColorTableCellRenderer());

    tableModel.addMenuItem("row", "Delete", "delete", (rowIndex, columnIndex) -> {
      this.colorRanges.remove(rowIndex);
      tableModel.fireTableRowsDeleted(rowIndex, columnIndex);
    });

    final TablePanel tablePanel = tableModel.newTablePanel();
    add(tablePanel);
  }

  @Override
  protected void saveDo() {
    this.style.setColorRanges(this.colorRanges);
    super.saveDo();
  }
}
