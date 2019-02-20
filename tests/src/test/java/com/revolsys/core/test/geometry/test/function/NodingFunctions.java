package com.revolsys.core.test.geometry.test.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.revolsys.geometry.algorithm.LineIntersector;
import com.revolsys.geometry.algorithm.RobustLineIntersector;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.noding.BasicSegmentString;
import com.revolsys.geometry.noding.FastNodingValidator;
import com.revolsys.geometry.noding.IntersectionAdder;
import com.revolsys.geometry.noding.MCIndexNoder;
import com.revolsys.geometry.noding.NodedSegmentString;
import com.revolsys.geometry.noding.Noder;
import com.revolsys.geometry.noding.ScaledNoder;
import com.revolsys.geometry.noding.SegmentString;
import com.revolsys.geometry.noding.snapround.MCIndexSnapRounder;

public class NodingFunctions {

  public static Geometry checkNoding(final Geometry geom) {
    final List segs = newSegmentStrings(geom);
    final FastNodingValidator nv = new FastNodingValidator(segs);
    nv.setFindAllIntersections(true);
    nv.isValid();
    final List intPts = nv.getIntersections();
    final Point[] pts = new Point[intPts.size()];
    for (int i = 0; i < intPts.size(); i++) {
      final Point coord = (Point)intPts.get(i);
      // use default factory in case intersections are not fixed
      pts[i] = FunctionsUtil.getFactoryOrDefault(null).point(coord);
    }
    return FunctionsUtil.getFactoryOrDefault(null).punctual(pts);
  }

  private static Geometry fromSegmentStrings(final Collection segStrings) {
    final LineString[] lines = new LineString[segStrings.size()];
    int index = 0;
    for (final Iterator i = segStrings.iterator(); i.hasNext();) {
      final SegmentString ss = (SegmentString)i.next();
      final LineString line = FunctionsUtil.getFactoryOrDefault(null)
        .lineString(ss.getLineString());
      lines[index++] = line;
    }
    return FunctionsUtil.getFactoryOrDefault(null).lineal(lines);
  }

  public static Geometry MCIndexNoding(final Geometry geom) {
    final List segs = newNodedSegmentStrings(geom);
    final Noder noder = new MCIndexNoder(new IntersectionAdder(new RobustLineIntersector()));
    noder.computeNodes(segs);
    final Collection nodedSegStrings = noder.getNodedSubstrings();
    return fromSegmentStrings(nodedSegStrings);
  }

  public static Geometry MCIndexNodingWithPrecision(final Geometry geom, final double scaleFactor) {
    final List segs = newNodedSegmentStrings(geom);

    final LineIntersector li = new RobustLineIntersector(scaleFactor);

    final Noder noder = new MCIndexNoder(new IntersectionAdder(li));
    noder.computeNodes(segs);
    final Collection nodedSegStrings = noder.getNodedSubstrings();
    return fromSegmentStrings(nodedSegStrings);
  }

  private static List<NodedSegmentString> newNodedSegmentStrings(final Geometry geom) {
    final List<NodedSegmentString> segs = new ArrayList<>();
    final List<LineString> lines = geom.getGeometries(LineString.class);
    for (final LineString line : lines) {
      segs.add(new NodedSegmentString(line, null));
    }
    return segs;
  }

  private static List<SegmentString> newSegmentStrings(final Geometry geom) {
    final List<SegmentString> segs = new ArrayList<>();
    final List<LineString> lines = geom.getGeometries(LineString.class);
    for (final LineString line : lines) {
      segs.add(new BasicSegmentString(line, null));
    }
    return segs;
  }

  /**
   * Runs a ScaledNoder on input.
   * Input vertices should be rounded to precision model.
   *
   * @param geom
   * @param scaleFactor
   * @return the noded geometry
   */
  public static Geometry scaledNoding(final Geometry geom, final double scaleFactor) {
    final List segs = newSegmentStrings(geom);
    final Noder noder = new ScaledNoder(new MCIndexSnapRounder(1.0), scaleFactor);
    noder.computeNodes(segs);
    final Collection nodedSegStrings = noder.getNodedSubstrings();
    return fromSegmentStrings(nodedSegStrings);
  }

}
