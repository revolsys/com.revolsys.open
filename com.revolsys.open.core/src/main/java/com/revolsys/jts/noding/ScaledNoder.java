/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */

package com.revolsys.jts.noding;

import java.util.Collection;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.CoordinateArrays;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.util.CollectionUtil;

/**
 * Wraps a {@link Noder} and transforms its input
 * into the integer domain.
 * This is intended for use with Snap-Rounding noders,
 * which typically are only intended to work in the integer domain.
 * Offsets can be provided to increase the number of digits of available precision.
 * <p>
 * Clients should be aware that rescaling can involve loss of precision,
 * which can cause zero-length line segments to be created.
 * These in turn can cause problems when used to build a planar graph.
 * This situation should be checked for and collapsed segments removed if necessary.
 *
 * @version 1.7
 */
public class ScaledNoder implements Noder {
  private final Noder noder;

  private final double scaleFactor;

  private double offsetX;

  private double offsetY;

  private boolean isScaled = false;

  public ScaledNoder(final Noder noder, final double scaleFactor) {
    this(noder, scaleFactor, 0, 0);
  }

  public ScaledNoder(final Noder noder, final double scaleFactor,
    final double offsetX, final double offsetY) {
    this.noder = noder;
    this.scaleFactor = scaleFactor;
    // no need to scale if input precision is already integral
    isScaled = !isIntegerPrecision();
  }

  @Override
  public void computeNodes(final Collection inputSegStrings) {
    Collection intSegStrings = inputSegStrings;
    if (isScaled) {
      intSegStrings = scale(inputSegStrings);
    }
    noder.computeNodes(intSegStrings);
  }

  @Override
  public Collection getNodedSubstrings() {
    final Collection splitSS = noder.getNodedSubstrings();
    if (isScaled) {
      rescale(splitSS);
    }
    return splitSS;
  }

  public boolean isIntegerPrecision() {
    return scaleFactor == 1.0;
  }

  private void rescale(final Collection segStrings) {
    // System.out.println("Rescaled: scaleFactor = " + scaleFactor);
    CollectionUtil.apply(segStrings, new CollectionUtil.Function() {
      @Override
      public Object execute(final Object obj) {
        final SegmentString ss = (SegmentString)obj;
        rescale(ss.getCoordinates());
        return null;
      }
    });
  }

  private void rescale(final Coordinates[] pts) {
    Coordinates p0 = null;
    Coordinates p1 = null;

    if (pts.length == 2) {
      p0 = new Coordinate(pts[0]);
      p1 = new Coordinate(pts[1]);
    }

    for (int i = 0; i < pts.length; i++) {
      pts[i].setX(pts[i].getX() / scaleFactor + offsetX);
      pts[i].setY(pts[i].getY() / scaleFactor + offsetY);
    }

    if (pts.length == 2 && pts[0].equals2d(pts[1])) {
      System.out.println(pts);
    }
  }

  // private double scale(double val) { return (double) Math.round(val *
  // scaleFactor); }

  private Collection scale(final Collection segStrings) {
    // System.out.println("Scaled: scaleFactor = " + scaleFactor);
    return CollectionUtil.transform(segStrings, new CollectionUtil.Function() {
      @Override
      public Object execute(final Object obj) {
        final SegmentString ss = (SegmentString)obj;
        return new NodedSegmentString(scale(ss.getCoordinates()), ss.getData());
      }
    });
  }

  private Coordinates[] scale(final Coordinates[] pts) {
    final Coordinates[] roundPts = new Coordinates[pts.length];
    for (int i = 0; i < pts.length; i++) {
      roundPts[i] = new Coordinate(Math.round((pts[i].getX() - offsetX)
        * scaleFactor), Math.round((pts[i].getY() - offsetY) * scaleFactor),
        pts[i].getZ());
    }
    final Coordinates[] roundPtsNoDup = CoordinateArrays.removeRepeatedPoints(roundPts);
    return roundPtsNoDup;
  }

  // private double rescale(double val) { return val / scaleFactor; }
}
