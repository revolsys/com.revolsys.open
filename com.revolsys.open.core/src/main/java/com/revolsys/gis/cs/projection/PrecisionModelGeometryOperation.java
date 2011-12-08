package com.revolsys.gis.cs.projection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class PrecisionModelGeometryOperation implements GeometryOperation {
  private final GeometryFactory geometryFactory;

  public PrecisionModelGeometryOperation(
    final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  @SuppressWarnings("unchecked")
  private void addUserData(
    final Geometry oldGeometry,
    final Geometry newGeometry) {
    final Object userData = oldGeometry.getUserData();
    if (userData != null) {
      if (userData instanceof Map) {
        final Map attrs = (Map)userData;
        newGeometry.setUserData(new TreeMap(attrs));
      } else {
        newGeometry.setUserData(userData);
      }
    }
  }

  public CoordinatesList perform(
    final CoordinatesList coordinates) {
    final int numAxis = geometryFactory.getNumAxis();
    final CoordinatesList newCoordinates =  new DoubleCoordinatesList(numAxis,
      coordinates);
     newCoordinates.makePrecise(geometryFactory);
    return newCoordinates;
  }

  public LinearRing perform(
    final LinearRing ring) {
    if (ring != null) {
      final CoordinatesList newCoordinates = perform(CoordinatesListUtil.get(ring));
      final LinearRing newRing = geometryFactory.createLinearRing(newCoordinates);
      addUserData(ring, newRing);
      return newRing;
    } else {
      return null;
    }
  }

  public LineString perform(
    final LineString line) {
    if (line != null) {
      final CoordinatesList newCoordinates = perform(CoordinatesListUtil.get(line));
      final LineString newLine = geometryFactory.createLineString(newCoordinates);
      addUserData(line, newLine);
      return newLine;
    } else {
      return null;
    }
  }

  public MultiLineString perform(
    final MultiLineString multiLineString) {
    if (multiLineString != null) {
      final LineString[] newLineStrings = new LineString[multiLineString.getNumGeometries()];
      for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
        final LineString line = (LineString)multiLineString.getGeometryN(i);
        final LineString newLineString = perform(line);
        addUserData(line, newLineString);
        newLineStrings[i] = newLineString;
      }
      final MultiLineString newMultiLineString = geometryFactory.createMultiLineString(newLineStrings);
      addUserData(multiLineString, newMultiLineString);
      return newMultiLineString;
    } else {
      return null;
    }
  }

  public Geometry perform(
    final MultiPoint multiPoint) {
    if (multiPoint != null) {
      final Point[] newPoints = new Point[multiPoint.getNumGeometries()];
      for (int i = 0; i < multiPoint.getNumGeometries(); i++) {
        final Point point = (Point)multiPoint.getGeometryN(i);
        final Point newPoint = perform(point);
        addUserData(point, newPoint);
        newPoints[i] = newPoint;
      }
      final MultiPoint newMultiPoint = geometryFactory.createMultiPoint(newPoints);
      addUserData(multiPoint, newMultiPoint);
      return newMultiPoint;
    } else {
      return null;
    }
  }

  public MultiPolygon perform(
    final MultiPolygon multiPolygon) {
    if (multiPolygon != null) {
      final Polygon[] newPolygons = new Polygon[multiPolygon.getNumGeometries()];
      for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
        final Polygon polygon = (Polygon)multiPolygon.getGeometryN(i);
        final Polygon newPolygon = perform(polygon);
        addUserData(polygon, newPolygon);
        newPolygons[i] = newPolygon;
      }
      final MultiPolygon newMultiPolygon = geometryFactory.createMultiPolygon(newPolygons);
      addUserData(multiPolygon, newMultiPolygon);
      return newMultiPolygon;
    } else {
      return null;
    }
  }

  public Point perform(
    final Point point) {
    if (point != null) {
      final CoordinatesList newCoordinate = perform(CoordinatesListUtil.get(point));
      final Point newPoint = geometryFactory.createPoint(newCoordinate);
      addUserData(point, newPoint);
      return newPoint;
    } else {
      return null;
    }
  }

  public Polygon perform(
    final Polygon polygon) {

    final LinearRing shell = (LinearRing)polygon.getExteriorRing();
    final LinearRing newShell = perform(shell);
    final LinearRing[] newHoles = new LinearRing[polygon.getNumInteriorRing()];
    for (int i = 0; i < newHoles.length; i++) {
      final LinearRing hole = (LinearRing)polygon.getInteriorRingN(i);
      newHoles[i] = perform(hole);
    }
    final Polygon newPolygon = geometryFactory.createPolygon(newShell, newHoles);
    addUserData(newPolygon, polygon);
    return newPolygon;

  }

  @SuppressWarnings("unchecked")
  public <T extends Geometry> T perform(
    final T geometry) {
    try {
      final Method method = getClass().getMethod("perform", geometry.getClass());
      return (T)method.invoke(this, geometry);
    } catch (final NoSuchMethodException e) {
      return geometry;
    } catch (final IllegalAccessException e) {
      throw new RuntimeException("Unable to invoke method", e);
    } catch (final InvocationTargetException e) {
      final Throwable cause = e.getCause();
      if (cause instanceof RuntimeException) {
        throw (RuntimeException)cause;
      } else if (cause instanceof Error) {
        throw (Error)cause;
      } else {
        throw new RuntimeException(cause);
      }
    }
  }

}
