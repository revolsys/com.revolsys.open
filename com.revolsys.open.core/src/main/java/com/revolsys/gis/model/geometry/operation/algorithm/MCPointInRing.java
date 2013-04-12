package com.revolsys.gis.model.geometry.operation.algorithm;

import java.util.Iterator;
import java.util.List;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.geometry.LineSegment;
import com.revolsys.gis.model.geometry.LinearRing;
import com.revolsys.gis.model.geometry.algorithm.locate.IndexedPointInAreaLocator;
import com.revolsys.gis.model.geometry.impl.BoundingBox;
import com.revolsys.gis.model.geometry.operation.chain.MonotoneChain;
import com.revolsys.gis.model.geometry.operation.chain.MonotoneChainBuilder;
import com.revolsys.gis.model.geometry.operation.chain.MonotoneChainSelectAction;
import com.vividsolutions.jts.algorithm.RobustDeterminant;
import com.vividsolutions.jts.index.bintree.Bintree;
import com.vividsolutions.jts.index.bintree.Interval;

/**
 * Implements {@link PointInRing} using {@link MonotoneChain}s and a
 * {@link Bintree} index to increase performance.
 * 
 * @version 1.7
 * @see IndexedPointInAreaLocator for more general functionality
 */
public class MCPointInRing implements PointInRing {

  class MCSelecter extends MonotoneChainSelectAction {
    Coordinates p;

    public MCSelecter(final Coordinates p) {
      this.p = p;
    }

    @Override
    public void select(final LineSegment ls) {
      testLineSegment(p, ls);
    }
  }

  private final LinearRing ring;

  private Bintree tree;

  private int crossings = 0; // number of segment/ray crossings

  private final Interval interval = new Interval();

  public MCPointInRing(final LinearRing ring) {
    this.ring = ring;
    buildIndex();
  }

  private void buildIndex() {
    // Envelope env = ring.getEnvelopeInternal();
    tree = new Bintree();

    final CoordinatesList pts = CoordinatesListUtil.removeRepeatedPoints(ring);
    final List mcList = MonotoneChainBuilder.getChains(pts);

    for (int i = 0; i < mcList.size(); i++) {
      final MonotoneChain mc = (MonotoneChain)mcList.get(i);
      final BoundingBox mcEnv = mc.getBoundingBox();
      interval.min = mcEnv.getMinY();
      interval.max = mcEnv.getMaxY();
      tree.insert(interval, mc);
    }
  }

  @Override
  public boolean isInside(final Coordinates pt) {
    crossings = 0;

    // test all segments intersected by ray from pt in positive x direction
    final BoundingBox rayEnv = new BoundingBox(null, Double.NEGATIVE_INFINITY,
      pt.getY(), Double.POSITIVE_INFINITY, pt.getY());

    interval.min = pt.getY();
    interval.max = pt.getY();
    final List segs = tree.query(interval);
    // System.out.println("query size = " + segs.size());

    final MCSelecter mcSelecter = new MCSelecter(pt);
    for (final Iterator i = segs.iterator(); i.hasNext();) {
      final MonotoneChain mc = (MonotoneChain)i.next();
      testMonotoneChain(rayEnv, mcSelecter, mc);
    }

    /*
     * p is inside if number of crossings is odd.
     */
    if ((crossings % 2) == 1) {
      return true;
    }
    return false;
  }

  private void testLineSegment(final Coordinates p, final LineSegment seg) {
    double xInt; // x intersection of segment with ray
    double x1; // translated coordinates
    double y1;
    double x2;
    double y2;

    /*
     * Test if segment crosses ray from test point in positive x direction.
     */
    final Coordinates p1 = seg.get(0);
    final Coordinates p2 = seg.get(1);
    x1 = p1.getX() - p.getX();
    y1 = p1.getY() - p.getY();
    x2 = p2.getX() - p.getX();
    y2 = p2.getY() - p.getY();

    if (((y1 > 0) && (y2 <= 0)) || ((y2 > 0) && (y1 <= 0))) {
      /*
       * segment straddles x axis, so compute intersection.
       */
      xInt = RobustDeterminant.signOfDet2x2(x1, y1, x2, y2) / (y2 - y1);
      // xsave = xInt;
      /*
       * crosses ray if strictly positive intersection.
       */
      if (0.0 < xInt) {
        crossings++;
      }
    }
  }

  private void testMonotoneChain(final BoundingBox rayEnv,
    final MCSelecter mcSelecter, final MonotoneChain mc) {
    mc.select(rayEnv, mcSelecter);
  }

}
