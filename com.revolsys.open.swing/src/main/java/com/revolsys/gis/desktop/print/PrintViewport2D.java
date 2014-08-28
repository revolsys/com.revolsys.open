package com.revolsys.gis.desktop.print;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;

import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.Project;

public class PrintViewport2D extends Viewport2D {
  private final PageFormat pageFormat;

  private final Rectangle2D contentRect;

  private final int dpi;

  public PrintViewport2D(final Project map, final Graphics2D graphics,
    final PageFormat pageFormat, final BoundingBox boundingBox,
    final Rectangle2D contentRect, final int dpi) {
    super(map);
    this.pageFormat = pageFormat;
    this.contentRect = contentRect;
    this.dpi = dpi;
    BoundingBox newBoundingBox = boundingBox;
    final double viewAspectRatio = getViewAspectRatio();
    final double modelAspectRatio = newBoundingBox.getAspectRatio();
    if (viewAspectRatio != modelAspectRatio) {
      final double width = newBoundingBox.getWidth();
      final double height = newBoundingBox.getHeight();
      if (viewAspectRatio > modelAspectRatio) {
        final double newWidth = height * viewAspectRatio;
        final double deltaX = (newWidth - width) / 2;

        newBoundingBox = newBoundingBox.expand(deltaX, 0);
      } else if (viewAspectRatio < modelAspectRatio) {
        final double newHeight = width / viewAspectRatio;
        final double deltaY = (newHeight - height) / 2;
        newBoundingBox = newBoundingBox.expand(0, deltaY);
      }
    }

    setGeometryFactory(newBoundingBox.getGeometryFactory());
    setBoundingBox(newBoundingBox);
  }

  /**
   * Get the unit of measure for the printable page. All measurements are in
   * 1/72 inch.
   */
  @Override
  public Unit<Length> getScreenUnit() {
    return NonSI.INCH.divide(dpi);
  }

  @Override
  public int getViewHeightPixels() {
    return (int)contentRect.getHeight();
  }

  @Override
  public int getViewWidthPixels() {
    return (int)contentRect.getWidth();
  }
}
