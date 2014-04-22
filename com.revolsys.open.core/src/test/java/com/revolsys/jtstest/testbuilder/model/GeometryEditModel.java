package com.revolsys.jtstest.testbuilder.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.revolsys.jts.geom.CoordinateArrays;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.util.Assert;
import com.revolsys.jtstest.testbuilder.JTSTestBuilder;
import com.revolsys.jtstest.testbuilder.geom.AdjacentVertexFinder;
import com.revolsys.jtstest.testbuilder.geom.GeometryCombiner;
import com.revolsys.jtstest.testbuilder.geom.GeometryLocation;
import com.revolsys.jtstest.testbuilder.geom.GeometryPointLocater;
import com.revolsys.jtstest.testbuilder.geom.GeometryUtil;
import com.revolsys.jtstest.testbuilder.geom.GeometryVertexMover;

/**
 * Holds the current {@link TestCaseEdit}.
 * 
 * @author Martin Davis
 *
 */
public class GeometryEditModel {

  private static Coordinates[] getRing(final List<Coordinates> coordList) {
    List<Coordinates> closedPts = coordList;
    final Coordinates p0 = coordList.get(0);
    final Coordinates pn = coordList.get(coordList.size() - 1);
    if (!p0.equals2d(pn)) {
      closedPts = new ArrayList<Coordinates>(coordList);
      closedPts.add(p0.cloneCoordinates());
    }
    final Coordinates[] pts = CoordinateArrays.toCoordinateArray(closedPts);
    return pts;
  }

  public static String getText(final Geometry geom, final int textType) {
    switch (textType) {
      case GeometryType.WELLKNOWNTEXT:
        final String wkt = geom.toWkt();
        return wkt;
    }
    Assert.shouldNeverReachHere();
    return "";
  }

  public static String toStringVeryLarge(final Geometry g) {
    return "[[ " + GeometryUtil.structureSummary(g) + " ]]";
  }

  private boolean readOnly = true;

  private int editGeomIndex = 0; // the index of the currently selected geometry

  private int geomType = GeometryType.POLYGON; // from GeometryType

  private TestCaseEdit testCase;

  private transient Vector geometryListeners;

  public GeometryEditModel() {

  }

  /**
   * Adds a geometry component of the currently selected type,
   * to the currently selected geometry.
   * 
   * @param coordList
   */
  public void addComponent(final List<Coordinates> coordList) {
    final GeometryCombiner creator = new GeometryCombiner(
      JTSTestBuilder.getGeometryFactory());

    Geometry newGeom = null;
    switch (getGeometryType()) {
      case GeometryType.POLYGON:
        newGeom = creator.addPolygonRing(getGeometry(), getRing(coordList));
      break;
      case GeometryType.LINESTRING:
        final Coordinates[] pts = CoordinateArrays.toCoordinateArray(coordList);
        newGeom = creator.addLineString(getGeometry(), pts);
      break;
      case GeometryType.POINT:
        newGeom = creator.addPoint(getGeometry(), coordList.get(0));
      break;
    }
    setGeometry(newGeom);
  }

  public synchronized void addGeometryListener(final GeometryListener l) {
    final Vector<GeometryListener> v = geometryListeners == null ? new Vector<GeometryListener>(
      2) : (Vector<GeometryListener>)geometryListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      geometryListeners = v;
    }
  }

  public void clear() {
    setGeometry(null);
    geomChanged();
  }

  public void clear(final int i) {
    setGeometry(i, null);
    geomChanged();
  }

  public Coordinates[] findAdjacentVertices(final Coordinates vertex) {
    final Geometry geom = getGeometry();
    if (geom == null) {
      return null;
    }
    return AdjacentVertexFinder.findVertices(getGeometry(), vertex);
  }

  public void fireGeometryChanged(final GeometryEvent e) {
    if (geometryListeners != null) {
      final Vector listeners = geometryListeners;
      final int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((GeometryListener)listeners.elementAt(i)).geometryChanged(e);
      }
    }
  }

  public void geomChanged() {
    fireGeometryChanged(new GeometryEvent(this));
  }

  public Envelope getEnvelope() {
    final Envelope env = new Envelope();

    if (getGeometry(0) != null) {
      env.expandToInclude(getGeometry(0).getBoundingBox());
    }
    if (getGeometry(1) != null) {
      env.expandToInclude(getGeometry(1).getBoundingBox());
    }
    return env;
  }

  public Envelope getEnvelopeAll() {
    final Envelope env = new Envelope();

    if (getGeometry(0) != null) {
      env.expandToInclude(getGeometry(0).getBoundingBox());
    }
    if (getGeometry(1) != null) {
      env.expandToInclude(getGeometry(1).getBoundingBox());
    }
    if (getResult() != null) {
      env.expandToInclude(getResult().getBoundingBox());
    }
    return env;
  }

  public Envelope getEnvelopeResult() {
    final Envelope env = new Envelope();

    if (getResult() != null) {
      env.expandToInclude(getResult().getBoundingBox());
    }
    return env;
  }

  // ====================================

  public Geometry getGeometry() {
    return getGeometry(editGeomIndex);
  }

  public Geometry getGeometry(final int i) {
    return testCase.getGeometry(i);
  }

  public int getGeometryType() {
    return geomType;
  }

  public int getGeomIndex() {
    return editGeomIndex;
  }

  public Geometry getResult() {
    // return result;
    return testCase.getResult();
  }

  public String getText(final int textType) {
    String str = "";
    if (getGeometry(0) != null) {
      str += getText(getGeometry(0), textType);
      str += "\n\n";
    }
    if (getGeometry(1) != null) {
      str += getText(getGeometry(1), textType);
      str += "\n\n";
    }
    return str;
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  /**
   * Locates a non-vertex point on a line segment of the current geometry
   * within the given tolerance, if any.
   * 
   * Returns the closest point on the segment.
   * 
   * @param testPt
   * @param tolerance
   * @return the location found, or
   * null if no non-vertex point was within tolerance
   */
  public GeometryLocation locateNonVertexPoint(final Coordinates testPt,
    final double tolerance) {
    final Geometry geom = getGeometry();
    if (geom == null) {
      return null;
    }
    return GeometryPointLocater.locateNonVertexPoint(getGeometry(), testPt,
      tolerance);
  }

  /**
   * Locates a vertex of the current geometry
   * within the given tolerance, if any.
   * Returns the closest point on the segment.
   * 
   * @param testPt
   * @param tolerance
   * @return the location of the vertex found, or
   * null if no vertex was within tolerance
   */
  public GeometryLocation locateVertex(final Coordinates testPt,
    final double tolerance) {
    final Geometry geom = getGeometry();
    if (geom == null) {
      return null;
    }
    return GeometryPointLocater.locateVertex(getGeometry(), testPt, tolerance);
  }

  public Coordinates locateVertexPt(final Coordinates testPt,
    final double tolerance) {
    final Geometry geom = getGeometry();
    if (geom == null) {
      return null;
    }
    final GeometryLocation loc = locateVertex(testPt, tolerance);
    if (loc == null) {
      return null;
    }
    return loc.getCoordinate();
  }

  public void moveVertex(final Coordinates fromLoc, final Coordinates toLoc) {
    final Geometry modGeom = GeometryVertexMover.move(getGeometry(), fromLoc,
      toLoc);
    setGeometry(modGeom);
  }

  public synchronized void removeGeometryListener(final GeometryListener l) {
    if (geometryListeners != null && geometryListeners.contains(l)) {
      final Vector v = (Vector)geometryListeners.clone();
      v.removeElement(l);
      geometryListeners = v;
    }
  }

  public void setEditGeomIndex(final int index) {
    editGeomIndex = index;
  }

  public void setGeometry(final Geometry g) {
    setGeometry(editGeomIndex, g);
    geomChanged();
  }

  public void setGeometry(final int i, final Geometry g) {
    testCase.setGeometry(i, g);
    geomChanged();
  }

  // ============================================

  public void setGeometryType(final int geomType) {
    this.geomType = geomType;
  }

  public void setReadOnly(final boolean readOnly) {
    this.readOnly = readOnly;
  }

  public void setTestCase(final TestCaseEdit testCase) {
    this.testCase = testCase;
    geomChanged();
  }

}
