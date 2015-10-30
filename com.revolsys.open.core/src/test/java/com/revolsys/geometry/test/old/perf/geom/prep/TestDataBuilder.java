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
package com.revolsys.geometry.test.old.perf.geom.prep;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDouble;
import com.revolsys.geometry.model.util.SineStarFactory;
import com.revolsys.geometry.util.GeometricShapeFactory;

public class TestDataBuilder {
  private GeometryFactory geomFact = GeometryFactory.floating3();

  private Point origin = new PointDouble((double)0, 0, Point.NULL_ORDINATE);

  private double size = 100.0;

  public TestDataBuilder() {

  }

  public TestDataBuilder(final GeometryFactory geomFact) {
    this.geomFact = geomFact;
  }

  public Geometry newCircle(final int nPts) {
    final GeometricShapeFactory gsf = new GeometricShapeFactory();
    gsf.setCentre(this.origin);
    gsf.setSize(this.size);
    gsf.setNumPoints(nPts);
    final Geometry circle = gsf.newCircle();
    // Polygon gRect = gsf.createRectangle();
    // Geometry g = gRect.getExteriorRing();
    return circle;
  }

  Geometry newLine(final Point base, final double size, final int nPts) {
    final GeometricShapeFactory gsf = new GeometricShapeFactory();
    gsf.setCentre(base);
    gsf.setSize(size);
    gsf.setNumPoints(nPts);
    final Geometry circle = gsf.newCircle();
    // System.out.println(circle);
    return circle.getBoundary();
  }

  public Geometry newSineStar(final int nPts) {
    final SineStarFactory gsf = new SineStarFactory();
    gsf.setCentre(this.origin);
    gsf.setSize(this.size);
    gsf.setNumPoints(nPts);
    gsf.setArmLengthRatio(0.1);
    gsf.setNumArms(20);
    final Geometry poly = gsf.newSineStar();
    return poly;
  }

  public List newTestGeoms(final BoundingBox env, final int nItems, final double size,
    final int nPts) {
    final int nCells = (int)Math.sqrt(nItems);

    final List geoms = new ArrayList();
    final double width = env.getWidth();
    final double xInc = width / nCells;
    final double yInc = width / nCells;
    for (int i = 0; i < nCells; i++) {
      for (int j = 0; j < nCells; j++) {
        final Point base = new PointDouble(env.getMinX() + i * xInc, env.getMinY() + j * yInc,
          Geometry.NULL_ORDINATE);
        final Geometry line = newLine(base, size, nPts);
        geoms.add(line);
      }
    }
    return geoms;
  }

  public void setExtent(final Point origin, final double size) {
    this.origin = origin;
    this.size = size;
  }

  public void setTestDimension(final int testDim) {
  }

}
