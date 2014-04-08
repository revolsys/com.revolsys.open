package com.revolsys.gis.model.geometry.operation.buffer;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.ListCoordinatesList;
import com.revolsys.gis.model.geometry.Geometry;
import com.revolsys.gis.model.geometry.GeometryCollection;
import com.revolsys.gis.model.geometry.LineString;
import com.revolsys.gis.model.geometry.LinearRing;
import com.revolsys.gis.model.geometry.MultiLineString;
import com.revolsys.gis.model.geometry.MultiLinearRing;
import com.revolsys.gis.model.geometry.MultiPoint;
import com.revolsys.gis.model.geometry.MultiPolygon;
import com.revolsys.gis.model.geometry.Point;
import com.revolsys.gis.model.geometry.Polygon;
import com.revolsys.gis.model.geometry.Triangle;
import com.revolsys.gis.model.geometry.impl.BoundingBox;
import com.revolsys.gis.model.geometry.operation.chain.NodedSegmentString;
import com.revolsys.gis.model.geometry.operation.chain.SegmentString;
import com.revolsys.gis.model.geometry.operation.geomgraph.Label;
import com.revolsys.gis.model.geometry.operation.geomgraph.Position;
import com.revolsys.jts.geom.Location;

/**
 * @version 1.7
 */

/**
 * Creates all the raw offset curves for a buffer of a {@link Geometry}. Raw
 * curves need to be noded together and polygonized to form the final buffer
 * area.
 * 
 * @version 1.7
 */
public class OffsetCurveSetBuilder {

  private final Geometry inputGeom;

  private final double distance;

  private final OffsetCurveBuilder curveBuilder;

  private final List<SegmentString> curveList = new ArrayList<SegmentString>();

  public OffsetCurveSetBuilder(final Geometry inputGeom, final double distance,
    final OffsetCurveBuilder curveBuilder) {
    this.inputGeom = inputGeom;
    this.distance = distance;
    this.curveBuilder = curveBuilder;
  }

  private void add(final Geometry g) {
    if (g.isEmpty()) {
      return;
    }

    if (g instanceof Polygon) {
      addPolygon((Polygon)g);
    } else if (g instanceof LineString) {
      addLineString((LineString)g);
    } else if (g instanceof Point) {
      addPoint((Point)g);
    } else if (g instanceof MultiPoint) {
      addCollection((MultiPoint)g);
    } else if (g instanceof MultiLineString) {
      addCollection((MultiLineString)g);
    } else if (g instanceof MultiPolygon) {
      addCollection((MultiPolygon)g);
    } else if (g instanceof GeometryCollection) {
      addCollection((GeometryCollection)g);
    } else {
      throw new UnsupportedOperationException(g.getClass().getName());
    }
  }

  private void addCollection(final GeometryCollection gc) {
    for (int i = 0; i < gc.getGeometryCount(); i++) {
      final Geometry g = gc.getGeometry(i);
      add(g);
    }
  }

  /**
   * Creates a {@link SegmentString} for a coordinate list which is a raw offset
   * curve, and adds it to the list of buffer curves. The SegmentString is
   * tagged with a Label giving the topology of the curve. The curve may be
   * oriented in either direction. If the curve is oriented CW, the locations
   * will be: <br>
   * Left: Location.EXTERIOR <br>
   * Right: Location.INTERIOR
   */
  private void addCurve(final CoordinatesList coord, final int leftLoc,
    final int rightLoc) {
    // don't add null or trivial curves
    if (coord == null || coord.size() < 2) {
      return;
    }
    // add the edge for a coordinate list which is a raw offset curve
    final SegmentString e = new NodedSegmentString(coord, new Label(0,
      Location.BOUNDARY, leftLoc, rightLoc));
    curveList.add(e);
  }

  private void addLineString(final LineString line) {
    // a zero or negative width buffer of a line/point is empty
    if (distance <= 0.0 && !curveBuilder.getBufferParameters().isSingleSided()) {
      return;
    }
    final CoordinatesList coord = CoordinatesListUtil.removeRepeatedPoints(line);
    final CoordinatesList curve = curveBuilder.getLineCurve(coord, distance);
    addCurve(curve, Location.EXTERIOR, Location.INTERIOR);
  }

  /**
   * Add a Point to the graph.
   */
  private void addPoint(final Point p) {
    // a zero or negative width buffer of a line/point is empty
    if (distance <= 0.0) {
      return;
    }
    final CoordinatesList coord = new ListCoordinatesList(p.getNumAxis(), p);
    final CoordinatesList curve = curveBuilder.getLineCurve(coord, distance);
    addCurve(curve, Location.EXTERIOR, Location.INTERIOR);
  }

  private void addPolygon(final Polygon polygon) {
    double offsetDistance = distance;
    int offsetSide = Position.LEFT;
    if (distance < 0.0) {
      offsetDistance = -distance;
      offsetSide = Position.RIGHT;
    }

    final LinearRing shell = polygon.getExteriorRing();
    final CoordinatesList shellCoord = CoordinatesListUtil.removeRepeatedPoints(shell);
    // optimization - don't bother computing buffer
    // if the polygon would be completely eroded
    if (distance < 0.0 && isErodedCompletely(shell, distance)) {
      return;
    }
    // don't attemtp to buffer a polygon with too few distinct vertices
    if (distance <= 0.0 && shellCoord.size() < 3) {
      return;
    }

    addPolygonRing(shellCoord, offsetDistance, offsetSide, Location.EXTERIOR,
      Location.INTERIOR);
    final MultiLinearRing rings = polygon.getRings();
    for (int i = 1; i < rings.getGeometryCount(); i++) {

      final LinearRing hole = rings.getGeometry(i);
      final CoordinatesList holeCoord = CoordinatesListUtil.removeRepeatedPoints(hole);

      // optimization - don't bother computing buffer for this hole
      // if the hole would be completely covered
      if (distance > 0.0 && isErodedCompletely(hole, -distance)) {
        continue;
      }

      // Holes are topologically labelled opposite to the shell, since
      // the interior of the polygon lies on their opposite side
      // (on the left, if the hole is oriented CCW)
      addPolygonRing(holeCoord, offsetDistance, Position.opposite(offsetSide),
        Location.INTERIOR, Location.EXTERIOR);
    }
  }

  /**
   * Adds an offset curve for a polygon ring. The side and left and right
   * topological location arguments assume that the ring is oriented CW. If the
   * ring is in the opposite orientation, the left and right locations must be
   * interchanged and the side flipped.
   * 
   * @param coord the coordinates of the ring (must not contain repeated points)
   * @param offsetDistance the distance at which to create the buffer
   * @param side the side of the ring on which to construct the buffer line
   * @param cwLeftLoc the location on the L side of the ring (if it is CW)
   * @param cwRightLoc the location on the R side of the ring (if it is CW)
   */
  private void addPolygonRing(final CoordinatesList coord,
    final double offsetDistance, int side, final int cwLeftLoc,
    final int cwRightLoc) {
    // don't bother adding ring if it is "flat" and will disappear in the output
    if (offsetDistance == 0.0 && coord.size() < LinearRing.MINIMUM_VALID_SIZE) {
      return;
    }

    int leftLoc = cwLeftLoc;
    int rightLoc = cwRightLoc;
    if (coord.size() >= LinearRing.MINIMUM_VALID_SIZE
      && CoordinatesListUtil.isCCW(coord)) {
      leftLoc = cwRightLoc;
      rightLoc = cwLeftLoc;
      side = Position.opposite(side);
    }
    final CoordinatesList curve = curveBuilder.getRingCurve(coord, side,
      offsetDistance);
    addCurve(curve, leftLoc, rightLoc);
  }

  /**
   * Computes the set of raw offset curves for the buffer. Each offset curve has
   * an attached {@link Label} indicating its left and right location.
   * 
   * @return a Collection of SegmentStrings representing the raw buffer curves
   */
  public List<SegmentString> getCurves() {
    add(inputGeom);
    return curveList;
  }

  /**
   * The ringCoord is assumed to contain no repeated points. It may be
   * degenerate (i.e. contain only 1, 2, or 3 points). In this case it has no
   * area, and hence has a minimum diameter of 0.
   * 
   * @param ringCoord
   * @param offsetDistance
   * @return
   */
  private boolean isErodedCompletely(final LinearRing ring,
    final double bufferDistance) {
    final CoordinatesList ringCoord = ring;
    final double minDiam = 0.0;
    // degenerate ring has no area
    if (ringCoord.size() < 4) {
      return bufferDistance < 0;
    }

    // important test to eliminate inverted triangle bug
    // also optimizes erosion test for triangles
    if (ringCoord.size() == 4) {
      return isTriangleErodedCompletely(ringCoord, bufferDistance);
    }

    // if envelope is narrower than twice the buffer distance, ring is eroded
    final BoundingBox env = ring.getBoundingBox();
    final double envMinDimension = Math.min(env.getHeight(), env.getWidth());
    if (bufferDistance < 0.0 && 2 * Math.abs(bufferDistance) > envMinDimension) {
      return true;
    }

    return false;
    /**
     * The following is a heuristic test to determine whether an inside buffer
     * will be eroded completely. It is based on the fact that the minimum
     * diameter of the ring pointset provides an upper bound on the buffer
     * distance which would erode the ring. If the buffer distance is less than
     * the minimum diameter, the ring may still be eroded, but this will be
     * determined by a full topological computation.
     */
    // System.out.println(ring);
    /*
     * MD 7 Feb 2005 - there's an unknown bug in the MD code, so disable this
     * for now MinimumDiameter md = new MinimumDiameter(ring); minDiam =
     * md.getLength(); //System.out.println(md.getDiameter()); return minDiam <
     * 2 * Math.abs(bufferDistance);
     */
  }

  /**
   * Tests whether a triangular ring would be eroded completely by the given
   * buffer distance. This is a precise test. It uses the fact that the inner
   * buffer of a triangle converges on the inCentre of the triangle (the point
   * equidistant from all sides). If the buffer distance is greater than the
   * distance of the inCentre from a side, the triangle will be eroded
   * completely. This test is important, since it removes a problematic case
   * where the buffer distance is slightly larger than the inCentre distance. In
   * this case the triangle buffer curve "inverts" with incorrect topology,
   * producing an incorrect hole in the buffer.
   * 
   * @param triangleCoord
   * @param bufferDistance
   * @return
   */
  private boolean isTriangleErodedCompletely(
    final CoordinatesList triangleCoord, final double bufferDistance) {
    final Triangle tri = new Triangle(triangleCoord.get(0),
      triangleCoord.get(1), triangleCoord.get(2));
    final Coordinates inCentre = tri.getInCentre();
    final double distToCentre = LineSegmentUtil.distance(tri.get(0),
      tri.get(1), inCentre);
    return distToCentre < Math.abs(bufferDistance);
  }

}
