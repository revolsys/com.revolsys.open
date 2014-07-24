package com.revolsys.gis.jts;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.vertex.Vertex;

public class GeometryEditUtil {

  public static Point getVertex(final Geometry geometry, final int[] partId,
    final int pointIndex) {
    final int[] vertexId = new int[partId.length + 1];
    System.arraycopy(partId, 0, vertexId, 0, partId.length);
    vertexId[partId.length] = pointIndex;
    return geometry.getVertex(vertexId);
  }

  public static int getVertexIndex(final int[] index) {
    final int length = index.length;
    final int lastIndex = length - 1;
    return index[lastIndex];
  }

  public static int[] incrementVertexIndex(final int[] index) {
    final int length = index.length;
    final int lastIndex = length - 1;
    final int[] newIndex = new int[length];
    System.arraycopy(index, 0, newIndex, 0, lastIndex);
    newIndex[lastIndex] = index[lastIndex] + 1;
    return newIndex;
  }

  public static boolean isFromPoint(final Geometry geometry,
    final int[] vertexId) {
    if (geometry != null) {
      final Vertex vertex = geometry.getVertex(vertexId);
      if (vertex != null) {
        return vertex.isFrom();
      }
    }
    return false;
  }

  public static boolean isToPoint(final Geometry geometry, final int[] vertexId) {
    if (geometry != null) {
      final Vertex vertex = geometry.getVertex(vertexId);
      if (vertex != null) {
        return vertex.isTo();
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public static <G extends Geometry> G moveVertexIfEqual(final G geometry,
    final Point originalLocation, final Point newLocation,
    final int... vertexId) {
    final Point coordinates = geometry.getVertex(vertexId);
    CoordinatesUtil.setElevation(newLocation, originalLocation);
    if (coordinates.equals(2, originalLocation)) {
      final Point newCoordinates = CoordinatesUtil.setElevation(newLocation,
        originalLocation);
      return (G)geometry.moveVertex(newCoordinates, vertexId);
    } else {
      return geometry;
    }

  }

  public static int[] setVertexIndex(final int[] index, final int vertexIndex) {
    final int length = index.length;
    final int lastIndex = length - 1;
    final int[] newIndex = new int[length];
    System.arraycopy(index, 0, newIndex, 0, lastIndex);
    newIndex[lastIndex] = vertexIndex;
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
