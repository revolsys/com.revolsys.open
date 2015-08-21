package com.revolsys.geometry.util;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;

public class GeometryEditUtil {

  public static int[] incrementVertexIndex(final int[] index) {
    final int length = index.length;
    final int lastIndex = length - 1;
    final int[] newIndex = new int[length];
    System.arraycopy(index, 0, newIndex, 0, lastIndex);
    newIndex[lastIndex] = index[lastIndex] + 1;
    return newIndex;
  }

  @SuppressWarnings("unchecked")
  public static <G extends Geometry> G toCounterClockwise(final G geometry) {
    if (geometry instanceof Point) {
      return geometry;
    } else if (geometry instanceof LineString) {
      final LineString line = (LineString)geometry;
      if (line.isCounterClockwise()) {
        return geometry;
      } else {
        return (G)line.reverse();
      }
    } else if (geometry instanceof Polygon) {
      final Polygon polygon = (Polygon)geometry;
      boolean changed = false;
      final List<LinearRing> rings = new ArrayList<>();
      int i = 0;
      for (final LinearRing ring : polygon.rings()) {
        final boolean counterClockwise = ring.isCounterClockwise();
        boolean pointsChanged;
        if (i == 0) {
          pointsChanged = !counterClockwise;
        } else {
          pointsChanged = counterClockwise;
        }
        if (pointsChanged) {
          changed = true;
          final LinearRing reverse = ring.reverse();
          rings.add(reverse);
        } else {
          rings.add(ring);
        }
        i++;
      }
      if (changed) {
        return (G)geometry.getGeometryFactory().polygon(rings);
      } else {
        return geometry;
      }
    } else {
      return geometry;
    }
  }

}
