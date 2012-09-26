package com.revolsys.gis.model.geometry.operation.noding.snapround;

import java.util.Collection;

import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.gis.model.geometry.operation.chain.NodedSegmentString;
import com.revolsys.gis.model.geometry.operation.chain.Noder;
import com.revolsys.gis.model.geometry.operation.chain.SegmentString;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.util.CollectionUtil;

/**
 * Wraps a {@link Noder} and transforms its input into the integer domain. This
 * is intended for use with Snap-Rounding noders, which typically are only
 * intended to work in the integer domain. Offsets can be provided to increase
 * the number of digits of available precision.
 * 
 * @version 1.7
 */
public class ScaledNoder implements Noder {
  private Noder noder;

  private double scaleFactor;

  private double offsetX;

  private double offsetY;

  private boolean isScaled = false;

  public ScaledNoder(Noder noder, double scaleFactor) {
    this(noder, scaleFactor, 0, 0);
  }

  public ScaledNoder(Noder noder, double scaleFactor, double offsetX,
    double offsetY) {
    this.noder = noder;
    this.scaleFactor = scaleFactor;
    // no need to scale if input precision is already integral
    isScaled = !isIntegerPrecision();
  }

  public boolean isIntegerPrecision() {
    return scaleFactor == 1.0;
  }

  public Collection getNodedSubstrings() {
    Collection splitSS = noder.getNodedSubstrings();
    if (isScaled)
      rescale(splitSS);
    return splitSS;
  }

  public void computeNodes(Collection inputSegStrings) {
    Collection intSegStrings = inputSegStrings;
    if (isScaled)
      intSegStrings = scale(inputSegStrings);
    noder.computeNodes(intSegStrings);
  }

  private Collection scale(Collection segStrings) {
    // System.out.println("Scaled: scaleFactor = " + scaleFactor);
    return CollectionUtil.transform(segStrings, new CollectionUtil.Function() {
      public Object execute(Object obj) {
        SegmentString ss = (SegmentString)obj;
        return new NodedSegmentString(scale(ss.getCoordinates()), ss.getData());
      }
    });
  }

  private CoordinatesList scale(CoordinatesList pts) {
    CoordinatesList roundPts = new DoubleCoordinatesList(pts.size(),
      pts.getNumAxis());
    for (int i = 0; i < pts.size(); i++) {
      roundPts.setX(i, Math.round((pts.getX(i) - offsetX) * scaleFactor));
      roundPts.setX(i, Math.round((pts.getY(i) - offsetY) * scaleFactor));
    }
    CoordinatesList roundPtsNoDup = CoordinatesListUtil.removeRepeatedPoints(roundPts);
    return roundPtsNoDup;
  }

  // private double scale(double val) { return (double) Math.round(val *
  // scaleFactor); }

  private void rescale(Collection segStrings) {
    // System.out.println("Rescaled: scaleFactor = " + scaleFactor);
    CollectionUtil.apply(segStrings, new CollectionUtil.Function() {
      public Object execute(Object obj) {
        SegmentString ss = (SegmentString)obj;
        rescale(ss.getCoordinates());
        return null;
      }
    });
  }

  private void rescale(CoordinatesList pts) {
    for (int i = 0; i < pts.size(); i++) {
      pts.setX(i, pts.getX(i) / scaleFactor + offsetX);
      pts.setY(i, pts.getY(i) / scaleFactor + offsetY);
    }
  }

  // private double rescale(double val) { return val / scaleFactor; }
}
