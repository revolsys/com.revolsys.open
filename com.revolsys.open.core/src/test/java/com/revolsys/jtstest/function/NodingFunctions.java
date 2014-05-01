package com.revolsys.jtstest.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.jts.algorithm.LineIntersector;
import com.revolsys.jts.algorithm.RobustLineIntersector;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.PrecisionModel;
import com.revolsys.jts.noding.BasicSegmentString;
import com.revolsys.jts.noding.FastNodingValidator;
import com.revolsys.jts.noding.IntersectionAdder;
import com.revolsys.jts.noding.MCIndexNoder;
import com.revolsys.jts.noding.NodedSegmentString;
import com.revolsys.jts.noding.Noder;
import com.revolsys.jts.noding.ScaledNoder;
import com.revolsys.jts.noding.SegmentString;
import com.revolsys.jts.noding.snapround.GeometryNoder;
import com.revolsys.jts.noding.snapround.MCIndexSnapRounder;
import com.revolsys.jts.precision.GeometryPrecisionReducer;

public class NodingFunctions {

  public static Geometry checkNoding(final Geometry geom) {
    final List segs = createSegmentStrings(geom);
    final FastNodingValidator nv = new FastNodingValidator(segs);
    nv.setFindAllIntersections(true);
    nv.isValid();
    final List intPts = nv.getIntersections();
    final Point[] pts = new Point[intPts.size()];
    for (int i = 0; i < intPts.size(); i++) {
      final Coordinates coord = (Coordinates)intPts.get(i);
      // use default factory in case intersections are not fixed
      pts[i] = FunctionsUtil.getFactoryOrDefault(null).point(coord);
    }
    return FunctionsUtil.getFactoryOrDefault(null).multiPoint(pts);
  }

  private static List createNodedSegmentStrings(final Geometry geom) {
    final List segs = new ArrayList();
    final List lines = geom.getGeometries(LineString.class);
    for (final Iterator i = lines.iterator(); i.hasNext();) {
      final LineString line = (LineString)i.next();
      segs.add(new NodedSegmentString(line.getCoordinatesList(), null));
    }
    return segs;
  }

  private static List createSegmentStrings(final Geometry geom) {
    final List segs = new ArrayList();
    final List lines = geom.getGeometries(LineString.class);
    for (final Iterator i = lines.iterator(); i.hasNext();) {
      final LineString line = (LineString)i.next();
      segs.add(new BasicSegmentString(
        CoordinatesListUtil.getCoordinateArray(line), null));
    }
    return segs;
  }

  private static Geometry fromSegmentStrings(final Collection segStrings) {
    final LineString[] lines = new LineString[segStrings.size()];
    int index = 0;
    for (final Iterator i = segStrings.iterator(); i.hasNext();) {
      final SegmentString ss = (SegmentString)i.next();
      final LineString line = FunctionsUtil.getFactoryOrDefault(null)
        .lineString(ss.getCoordinates());
      lines[index++] = line;
    }
    return FunctionsUtil.getFactoryOrDefault(null).multiLineString(lines);
  }

  public static Geometry MCIndexNoding(final Geometry geom) {
    final List segs = createNodedSegmentStrings(geom);
    final Noder noder = new MCIndexNoder(new IntersectionAdder(
      new RobustLineIntersector()));
    noder.computeNodes(segs);
    final Collection nodedSegStrings = noder.getNodedSubstrings();
    return fromSegmentStrings(nodedSegStrings);
  }

  public static Geometry MCIndexNodingWithPrecision(final Geometry geom,
    final double scaleFactor) {
    final List segs = createNodedSegmentStrings(geom);
    final PrecisionModel fixedPM = new PrecisionModel(scaleFactor);

    final LineIntersector li = new RobustLineIntersector();
    li.setPrecisionModel(fixedPM);

    final Noder noder = new MCIndexNoder(new IntersectionAdder(li));
    noder.computeNodes(segs);
    final Collection nodedSegStrings = noder.getNodedSubstrings();
    return fromSegmentStrings(nodedSegStrings);
  }

  /**
   * Runs a ScaledNoder on input.
   * Input vertices should be rounded to precision model.
   * 
   * @param geom
   * @param scaleFactor
   * @return the noded geometry
   */
  public static Geometry scaledNoding(final Geometry geom,
    final double scaleFactor) {
    final List segs = createSegmentStrings(geom);
    final PrecisionModel fixedPM = new PrecisionModel(scaleFactor);
    final Noder noder = new ScaledNoder(new MCIndexSnapRounder(
      new PrecisionModel(1.0)), fixedPM.getScale());
    noder.computeNodes(segs);
    final Collection nodedSegStrings = noder.getNodedSubstrings();
    return fromSegmentStrings(nodedSegStrings);
  }

  /**
   * Reduces precision pointwise, then snap-rounds.
   * Note that output set may not contain non-unique linework
   * (and thus cannot be used as input to Polygonizer directly).
   * UnaryUnion is one way to make the linework unique.
   * 
   * 
   * @param geom a geometry containing linework to node
   * @param scaleFactor the precision model scale factor to use
   * @return the noded, snap-rounded linework
   */
  public static Geometry snapRoundWithPointwisePrecisionReduction(
    final Geometry geom, final double scaleFactor) {
    final PrecisionModel pm = new PrecisionModel(scaleFactor);

    final Geometry roundedGeom = GeometryPrecisionReducer.reducePointwise(geom,
      pm);

    final List geomList = new ArrayList();
    geomList.add(roundedGeom);

    final GeometryNoder noder = new GeometryNoder(pm);
    final List lines = noder.node(geomList);

    return FunctionsUtil.getFactoryOrDefault(geom).buildGeometry(lines);
  }

}
