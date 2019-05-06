package com.revolsys.swing.map.print;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;

import javax.measure.Unit;
import javax.measure.quantity.Length;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.view.ViewRenderer;
import com.revolsys.swing.map.view.graphics.Graphics2DViewRenderer;

import systems.uom.common.USCustomary;

public class PrintViewport2D extends Viewport2D {
  private final Rectangle2D contentRect;

  private final int dpi;

  private final Graphics2D graphics;

  public PrintViewport2D(final Project map, final Graphics2D graphics, final PageFormat pageFormat,
    final BoundingBox boundingBox, final Rectangle2D contentRect, final int dpi) {
    super(map);
    this.contentRect = contentRect;
    this.dpi = dpi;
    this.graphics = graphics;
    BoundingBox newBoundingBox = boundingBox;
    final double viewAspectRatio = getViewAspectRatio();
    final double modelAspectRatio = newBoundingBox.getAspectRatio();
    if (viewAspectRatio != modelAspectRatio) {
      final double width = newBoundingBox.getWidth();
      final double height = newBoundingBox.getHeight();
      if (viewAspectRatio > modelAspectRatio) {
        final double newWidth = height * viewAspectRatio;
        final double deltaX = (newWidth - width) / 2;

        newBoundingBox = newBoundingBox.bboxEdit(editor -> editor.expandDeltaX(deltaX));
      } else if (viewAspectRatio < modelAspectRatio) {
        final double newHeight = width / viewAspectRatio;
        final double deltaY = (newHeight - height) / 2;
        newBoundingBox = newBoundingBox.bboxEdit(editor -> editor.expandDeltaY(deltaY));
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
    return USCustomary.INCH.divide(this.dpi);
  }

  @Override
  public double getViewHeightPixels() {
    return this.contentRect.getHeight();
  }

  @Override
  public double getViewWidthPixels() {
    return this.contentRect.getWidth();
  }

  @Override
  public ViewRenderer newViewRenderer() {
    return new Graphics2DViewRenderer(this, this.graphics);
  }
}
