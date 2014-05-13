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

package com.revolsys.jts.linearref;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.jts.geom.CoordinateList;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.MultiLineString;

/**
 * Builds a linear geometry ({@link LineString} or {@link MultiLineString})
 * incrementally (point-by-point).
 *
 * @version 1.7
 */
public class LinearGeometryBuilder {
  private final GeometryFactory geomFact;

  private final List lines = new ArrayList();

  private CoordinateList coordList = null;

  private boolean ignoreInvalidLines = false;

  private boolean fixInvalidLines = false;

  private Point lastPt = null;

  public LinearGeometryBuilder(final GeometryFactory geomFact) {
    this.geomFact = geomFact;
  }

  /**
   * Adds a point to the current line.
   *
   * @param pt the Point to add
   */
  public void add(final Point pt) {
    add(pt, true);
  }

  /**
   * Adds a point to the current line.
   *
   * @param pt the Point to add
   */
  public void add(final Point pt, final boolean allowRepeatedPoints) {
    if (coordList == null) {
      coordList = new CoordinateList();
    }
    coordList.add(pt, allowRepeatedPoints);
    lastPt = pt;
  }

  /**
   * Terminate the current LineString.
   */
  public void endLine() {
    if (coordList == null) {
      return;
    }
    if (ignoreInvalidLines && coordList.size() < 2) {
      coordList = null;
      return;
    }
    final Point[] rawPts = coordList.toCoordinateArray();
    Point[] pts = rawPts;
    if (fixInvalidLines) {
      pts = validCoordinateSequence(rawPts);
    }

    coordList = null;
    LineString line = null;
    try {
      line = geomFact.lineString(pts);
    } catch (final IllegalArgumentException ex) {
      // exception is due to too few points in line.
      // only propagate if not ignoring short lines
      if (!ignoreInvalidLines) {
        throw ex;
      }
    }

    if (line != null) {
      lines.add(line);
    }
  }

  public Geometry getGeometry() {
    // end last line in case it was not done by user
    endLine();
    return geomFact.buildGeometry(lines);
  }

  public Point getLastCoordinate() {
    return lastPt;
  }

  /**
   * Allows invalid lines to be ignored rather than causing Exceptions.
   * An invalid line is one which has only one unique point.
   *
   * @param fixInvalidLines <code>true</code> if short lines are to be ignored
   */
  public void setFixInvalidLines(final boolean fixInvalidLines) {
    this.fixInvalidLines = fixInvalidLines;
  }

  /**
   * Allows invalid lines to be ignored rather than causing Exceptions.
   * An invalid line is one which has only one unique point.
   *
   * @param ignoreInvalidLines <code>true</code> if short lines are to be ignored
   */
  public void setIgnoreInvalidLines(final boolean ignoreInvalidLines) {
    this.ignoreInvalidLines = ignoreInvalidLines;
  }

  private Point[] validCoordinateSequence(final Point[] pts) {
    if (pts.length >= 2) {
      return pts;
    }
    final Point[] validPts = new Point[] {
      pts[0], pts[0]
    };
    return validPts;
  }
}
