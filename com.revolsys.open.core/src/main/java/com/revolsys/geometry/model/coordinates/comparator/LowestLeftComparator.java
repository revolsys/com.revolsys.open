package com.revolsys.geometry.model.coordinates.comparator;

import java.util.Comparator;

import com.revolsys.geometry.model.Point;

/**
 * Compare the coordinates, lowest Y first if equal then X comparison.
 */
public class LowestLeftComparator implements Comparator<Point> {
  public static int compare(final Point point, final double x, final double y) {
    final double y1 = point.getY();

    final int yCompare = Double.compare(y1, y);
    if (yCompare == 0) {
      final double x1 = point.getX();
      return Double.compare(x1, x);
    } else {
      return yCompare;
    }
  }

  public static int compareCoordinates(final Point point1, final Point point2) {
    final double y1 = point1.getY();
    final double y2 = point2.getY();

    final int yCompare = Double.compare(y1, y2);
    if (yCompare == 0) {
      final double x1 = point1.getX();
      final double x2 = point2.getX();
      return Double.compare(x1, x2);
    } else {
      return yCompare;
    }
  }

  public LowestLeftComparator() {
  }

  @Override
  public int compare(final Point point1, final Point point2) {
    final double y1 = point1.getY();
    final double y2 = point2.getY();

    final int yCompare = Double.compare(y1, y2);
    if (yCompare == 0) {
      final double x1 = point1.getX();
      final double x2 = point2.getX();
      return Double.compare(x1, x2);
    } else {
      return yCompare;
    }
  }
}
