package com.revolsys.swing.map.form;

import java.awt.Color;
import java.awt.Component;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.Border;

import org.jdesktop.swingx.decorator.BorderHighlighter;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jeometry.common.awt.WebColors;

import com.revolsys.swing.table.BaseJTable;
import com.revolsys.swing.table.geometry.GeometryCoordinatesTableModel;

public class GeometryCoordinateErrorPredicate extends BorderHighlighter
  implements HighlightPredicate {

  private static final Border ERROR_BORDER = BorderFactory.createLineBorder(WebColors.Red, 2);

  private static final ColorHighlighter ERROR_HIGHLIGHTER = new ColorHighlighter(
    WebColors.newAlpha(WebColors.Red, 127), Color.BLACK, Color.RED, Color.YELLOW);

  public static void add(final BaseJTable table) {
    final GeometryCoordinatesTableModel model = (GeometryCoordinatesTableModel)table.getModel();
    final GeometryCoordinateErrorPredicate predicate = new GeometryCoordinateErrorPredicate(model);
    table.addHighlighter(predicate);
  }

  public static String toString(final int[] vertexIndex) {
    return Arrays.toString(vertexIndex);
  }

  private final GeometryCoordinatesTableModel model;

  public GeometryCoordinateErrorPredicate(final GeometryCoordinatesTableModel model) {
    setHighlightPredicate(this);
    setBorder(ERROR_BORDER);
    this.model = model;
  }

  @Override
  protected boolean canHighlight(final Component component, final ComponentAdapter adapter) {
    return component instanceof JComponent;
  }

  @Override
  protected Component doHighlight(final Component renderer, final ComponentAdapter adapter) {
    ERROR_HIGHLIGHTER.highlight(renderer, adapter);
    final Component doHighlight = super.doHighlight(renderer, adapter);
    return doHighlight;
  }

  @Override
  public boolean isHighlighted(final Component renderer, final ComponentAdapter adapter) {
    final int rowIndex = adapter.convertRowIndexToModel(adapter.row);
    final int columnIndex = adapter.convertColumnIndexToModel(adapter.column);
    final int axisIndex = columnIndex - this.model.getNumIndexItems();
    if (axisIndex >= 0 && axisIndex <= 1) {
      final JComponent component = (JComponent)renderer;
      final double value = this.model.getCoordinate(rowIndex, columnIndex);
      if (Double.isNaN(value) || Double.isInfinite(value)) {
        component.setToolTipText("Coordinate value " + value + " is invalid");
        return true;
      }
    }
    return false;
  }
}
