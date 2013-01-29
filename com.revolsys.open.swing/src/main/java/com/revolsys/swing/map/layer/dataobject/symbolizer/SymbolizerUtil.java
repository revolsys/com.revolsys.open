package com.revolsys.swing.map.layer.dataobject.symbolizer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;

import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.symbolizer.CssUtil;
import com.revolsys.swing.map.symbolizer.Fill;
import com.revolsys.swing.map.symbolizer.Graphic;
import com.revolsys.swing.map.symbolizer.Stroke;

public class SymbolizerUtil {

  public static BasicStroke getBasicStroke(
    final Stroke stroke,
    final Viewport2D viewport) {
    Measure<Length> strokeWidth = stroke.getWidth();
    if (strokeWidth == null) {
      strokeWidth = Measure.valueOf(1.0, NonSI.PIXEL);
    }
    float width;
    if (viewport == null) {
      width = strokeWidth.getValue().floatValue();
    } else {
      width = (float)viewport.toDisplayValue(strokeWidth);
    }
    final float mitreLimit = (float)Math.max(1, stroke.getMitreLimit());

    final Measure<Length> strokeDashPhase = stroke.getDashOffset();
    float dashPhase;
    if (viewport == null) {
      dashPhase = strokeDashPhase.getValue().floatValue();
    } else {
      dashPhase = (float)viewport.toDisplayValue(strokeDashPhase);
    }
    float[] dashArray;
    final List<Measure<Length>> dashes = stroke.getDashArray();
    if (dashes == null) {
      dashArray = null;
    } else {
      dashArray = new float[dashes.size()];

      for (int i = 0; i < dashArray.length; i++) {
        final Measure<Length> dash = dashes.get(i);
        if (viewport == null) {
          dashArray[i] = dash.getValue().floatValue();
        } else {
          dashArray[i] = (float)viewport.toDisplayValue(dash);
        }
      }
    }
    int lineCap = BasicStroke.CAP_BUTT;
    if (stroke.getLineCap().equals("round")) {
      lineCap = BasicStroke.CAP_ROUND;
    } else if (stroke.getLineCap().equals("square")) {
      lineCap = BasicStroke.CAP_SQUARE;
    }
    int lineJoin = BasicStroke.JOIN_MITER;
    if (stroke.getLineJoin().equals("round")) {
      lineJoin = BasicStroke.JOIN_ROUND;
    } else if (stroke.getLineJoin().equals("bevel")) {
      lineJoin = BasicStroke.JOIN_BEVEL;
    }
    return new BasicStroke(width, lineCap, lineJoin, mitreLimit, dashArray,
      dashPhase);

  }

  public static void setFill(final Graphics2D graphics, final Fill fill) {
    if (fill != null) {
      final CharSequence fillColor = fill.getColor();
      final Color color = CssUtil.getColor(fillColor, fill.getAlpha());
      graphics.setPaint(color);
      final Graphic fillPattern = fill.getPattern();
      if (fillPattern != null) {
        // TODO fillPattern
        // double width = fillPattern.getWidth();
        // double height = fillPattern.getHeight();
        // Rectangle2D.Double patternRect;
        // // TODO units
        // // if (isUseModelUnits()) {
        // // patternRect = new Rectangle2D.Double(0, 0, width
        // // * viewport.getModelUnitsPerViewUnit(), height
        // // * viewport.getModelUnitsPerViewUnit());
        // // } else {
        // patternRect = new Rectangle2D.Double(0, 0, width, height);
        // // }
        // graphics.setPaint(new TexturePaint(fillPattern, patternRect));

      }
    }
  }

  public static void setStroke(
    final Viewport2D viewport,
    final Graphics2D graphics,
    final Stroke stroke) {
    if (stroke != null) {
      final CharSequence strokeColor = stroke.getColor();
      final Color color = CssUtil.getColor(strokeColor, stroke.getAlpha());
      graphics.setColor(color);
      final BasicStroke basicStroke = getBasicStroke(stroke, viewport);
      graphics.setStroke(basicStroke);
      final Fill fillPattern = stroke.getFillPattern();
      if (fillPattern != null) {
        // TODO fill
        // double width = fillPattern.getWidth();
        // double height = fillPattern.getHeight();
        // Rectangle2D.Double patternRect;
        // // TODO units
        // // if (isUseModelUnits()) {
        // // patternRect = new Rectangle2D.Double(0, 0, width
        // // * viewport.getModelUnitsPerViewUnit(), height
        // // * viewport.getModelUnitsPerViewUnit());
        // // } else {
        // patternRect = new Rectangle2D.Double(0, 0, width, height);
        // // }
        // graphics.setPaint(new TexturePaint(fillPattern, patternRect));

      }
    }
  }
}
