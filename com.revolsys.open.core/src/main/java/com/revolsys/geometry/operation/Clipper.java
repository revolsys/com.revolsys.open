package com.revolsys.geometry.operation;

import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.editor.LineStringEditor;

public class Clipper {
  public static void addIntersection(final LineStringEditor result, final double line1x1,
    final double line1y1, final double line1x2, final double line1y2, final double line2x1,
    final double line2y1, final double line2x2, final double line2y2) {
    final double line1DeltaX = line1x1 - line1x2;
    final double line1DeltaY = line1y2 - line1y1;
    final double C1 = line1DeltaY * line1x1 + line1DeltaX * line1y1;

    final double B2 = line2x1 - line2x2;
    final double A2 = line2y2 - line2y1;
    final double C2 = A2 * line2x1 + B2 * line2y1;

    final double det = line1DeltaY * B2 - A2 * line1DeltaX;
    final double x = (B2 * C1 - line1DeltaX * C2) / det;
    final double y = (line1DeltaY * C2 - A2 * C1) / det;

    result.appendVertex(x, y);
  }

  public static LineString clip(final LineString line, final double line1x1, final double line1y1,
    final double line1x2, final double line1y2) {
    final LineStringEditor result = new LineStringEditor(line.getGeometryFactory(),
      line.getVertexCount());

    line.forEachSegment((line2x1, line2y1, line2x2, line2y2) -> {
      if (isInside(line1x1, line1y1, line1x2, line1y2, line2x2, line2y2)) {
        if (!isInside(line1x1, line1y1, line1x2, line1y2, line2x1, line2y1)) {
          addIntersection(result, line1x1, line1y1, line1x2, line1y2, line2x1, line2y1, line2x2,
            line2y2);
        }
        result.appendVertex(line2x2, line2y2);
      } else if (isInside(line1x1, line1y1, line1x2, line1y2, line2x1, line2y1)) {
        addIntersection(result, line1x1, line1y1, line1x2, line1y2, line2x1, line2y1, line2x2,
          line2y2);
      }
    });
    if (!result.isClosed() && !result.isEmpty()) {
      result.appendVertex(result.getX(0), result.getY(0));
    }
    return result;
  }

  public static Polygon clipRectangle(final LinearRing ring, final double minX, final double minY,
    final double maxX, final double maxY) {
    LineString line = ring;
    line = clip(line, minX, minY, maxX, minY);
    line = clip(line, maxX, minY, maxX, maxY);
    line = clip(line, maxX, maxY, minX, maxY);
    line = clip(line, minX, maxY, minX, minY);
    return line.newLinearRing().newPolygon();
  }

  public static boolean isInside(final double x1, final double y1, final double x2, final double y2,
    final double x, final double y) {
    return (x1 - x) * (y2 - y) > (y1 - y) * (x2 - x);
  }
}
