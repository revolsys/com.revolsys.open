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

package com.revolsys.geometry.linearref;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.model.PointList;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.Point;

/**
 * Builds a linear geometry ({@link Lineal})
 * incrementally (point-by-point).
 *
 * @version 1.7
 */
public class LinearGeometryBuilder {
  private PointList coordList = null;

  private boolean fixInvalidLines = false;

  private final GeometryFactory geomFact;

  private boolean ignoreInvalidLines = false;

  private Point lastPt = null;

  private final List lines = new ArrayList();

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
    if (this.coordList == null) {
      this.coordList = new PointList();
    }
    this.coordList.add(pt, allowRepeatedPoints);
    this.lastPt = pt;
  }

  /**
   * Terminate the current LineString.
   */
  public void endLine() {
    if (this.coordList == null) {
      return;
    }
    if (this.ignoreInvalidLines && this.coordList.size() < 2) {
      this.coordList = null;
      return;
    }
    final Point[] rawPts = this.coordList.toPointArray();
    Point[] pts = rawPts;
    if (this.fixInvalidLines) {
      pts = validCoordinateSequence(rawPts);
    }

    this.coordList = null;
    LineString line = null;
    try {
      line = this.geomFact.lineString(pts);
    } catch (final IllegalArgumentException ex) {
      // exception is due to too few points in line.
      // only propagate if not ignoring short lines
      if (!this.ignoreInvalidLines) {
        throw ex;
      }
    }

    if (line != null) {
      this.lines.add(line);
    }
  }

  public Geometry getGeometry() {
    // end last line in case it was not done by user
    endLine();
    return this.geomFact.buildGeometry(this.lines);
  }

  public Point getLastCoordinate() {
    return this.lastPt;
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
