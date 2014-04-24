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
package com.revolsys.jts.testold.generator;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.operation.valid.IsValidOp;

/**
 * 
 * This class is used to create a polygon within the specified bounding box.
 * 
 * Sucessive calls to create may or may not return the same geometry topology.
 *
 * @author David Zwiers, Vivid Solutions. 
 */
public class PolygonGenerator extends GeometryGenerator {
  private static LinearRing createArc(final double cx, final double cy,
    final double radius, final int npoints, final GeometryFactory gf) {

    final Coordinates[] coords = new Coordinates[npoints + 1];

    final double theta = 360 / npoints;

    for (int i = 0; i < npoints; i++) {
      final double angle = Math.toRadians(theta * i);

      final double fx = Math.sin(angle) * radius; // may be neg.
      final double fy = Math.cos(angle) * radius; // may be neg.

      coords[i] = new Coordinate(cx + fx, cy + fy, Coordinates.NULL_ORDINATE);
      gf.getPrecisionModel().makePrecise(coords[i]);
    }

    coords[npoints] = new Coordinate(coords[0]);
    gf.getPrecisionModel().makePrecise(coords[npoints]);

    return gf.linearRing(coords);
  }

  private static LinearRing createBox(final double x, final double dx,
    final double y, final double dy, final int npoints, final GeometryFactory gf) {

    // figure out the number of points per side
    final int ptsPerSide = npoints / 4;
    int rPtsPerSide = npoints % 4;
    final Coordinates[] coords = new Coordinates[npoints + 1];
    coords[0] = new Coordinate(x, y, Coordinates.NULL_ORDINATE); // start
    gf.getPrecisionModel().makePrecise(coords[0]);

    int cindex = 1;
    for (int i = 0; i < 4; i++) { // sides
      final int npts = ptsPerSide + (rPtsPerSide-- > 0 ? 1 : 0);
      // npts atleast 1

      if (i % 2 == 1) { // odd vert
        double cy = dy / npts;
        if (i > 1) {
          cy *= -1;
        }
        final double tx = coords[cindex - 1].getX();
        final double sy = coords[cindex - 1].getY();

        for (int j = 0; j < npts; j++) {
          coords[cindex] = new Coordinate(tx, sy + (j + 1) * cy,
            Coordinates.NULL_ORDINATE);
          gf.getPrecisionModel().makePrecise(coords[cindex++]);
        }
      } else { // even horz
        double cx = dx / npts;
        if (i > 1) {
          cx *= -1;
        }
        final double ty = coords[cindex - 1].getY();
        final double sx = coords[cindex - 1].getX();

        for (int j = 0; j < npts; j++) {
          coords[cindex] = new Coordinate(sx + (j + 1) * cx, ty,
            Coordinates.NULL_ORDINATE);
          gf.getPrecisionModel().makePrecise(coords[cindex++]);
        }
      }
    }
    coords[npoints] = new Coordinate(x, y, Coordinates.NULL_ORDINATE); // end
    gf.getPrecisionModel().makePrecise(coords[npoints]);

    return gf.linearRing(coords);
  }

  private static Polygon createBox(final double x, final double dx,
    final double y, final double dy, final int nholes, final int npoints,
    final GeometryFactory gf) {
    // make outer ring first
    final LinearRing outer = createBox(x, dx, y, dy, npoints, gf);

    if (nholes == 0) {
      return gf.polygon(outer);
    }

    final List<LinearRing> rings = new ArrayList<>();
    rings.add(outer);
    final int nrow = (int)Math.sqrt(nholes);
    final int ncol = nholes / nrow;

    final double ddx = dx / (ncol + 1);
    final double ddy = dy / (nrow + 1);

    // spacers
    final double spx = ddx / (ncol + 1);
    final double spy = ddy / (nrow + 1);

    // should have more grids than required
    int cindex = 0;
    for (int i = 0; i < nrow; i++) {
      for (int j = 0; j < ncol; j++) {
        if (cindex < nholes) {
          // make another box
          int pts = npoints / 2;
          pts = pts < 4 ? 4 : pts;
          cindex++;
          rings.add(createBox(spx + x + j * (ddx + spx), ddx, spy + y + i
            * (ddy + spy), ddy, pts, gf));
        }
      }
    }

    return gf.polygon(rings);
  }

  protected int numberPoints = 4;

  protected int numberHoles = 0;

  protected int generationAlgorithm = 0;

  /**
   * Creates rectangular polygons
   */
  public static final int BOX = 0;

  /**
   * Creates polygons whose points will not be rectangular when there are more than 4 points 
   */
  public static final int ARC = 1;

  private static final int RUNS = 5;

  private static Polygon createArc(final double x, final double dx,
    final double y, final double dy, final int nholes, final int npoints,
    final GeometryFactory gf) {
    // make outer ring first
    double radius = dx < dy ? dx / 3 : dy / 3;

    final double cx = x + dx / 2; // center
    final double cy = y + dy / 2; // center

    final LinearRing outer = createArc(cx, cy, radius, npoints, gf);

    if (nholes == 0) {
      return gf.polygon(outer);
    }
    final List<LinearRing> rings = new ArrayList<LinearRing>();
    rings.add(outer);

    radius *= .75;
    int degreesPerHole = 360 / (nholes + 1);
    int degreesPerGap = degreesPerHole / nholes;
    degreesPerGap = degreesPerGap < 2 ? 2 : degreesPerGap;
    degreesPerHole = (360 - degreesPerGap * nholes) / nholes;

    if (degreesPerHole < 2) {
      throw new RuntimeException("Slices too small for poly. Use Box alg.");
    }

    final int start = degreesPerGap / 2;
    for (int i = 0; i < nholes; i++) {
      final int st = start + i * (degreesPerHole + degreesPerGap); // start
                                                                   // angle
      rings.add(createTri(cx, cy, st, st + degreesPerHole, radius, gf));
    }

    return gf.polygon(rings);
  }

  private static LinearRing createTri(final double cx, final double cy,
    final int startAngle, final int endAngle, final double radius,
    final GeometryFactory gf) {

    final Coordinates[] coords = new Coordinates[4];

    double fx1, fx2, fy1, fy2;

    double angle = Math.toRadians(startAngle);
    fx1 = Math.sin(angle) * radius; // may be neg.
    fy1 = Math.cos(angle) * radius; // may be neg.

    angle = Math.toRadians(endAngle);
    fx2 = Math.sin(angle) * radius; // may be neg.
    fy2 = Math.cos(angle) * radius; // may be neg.

    coords[0] = new Coordinate(cx, cy, Coordinates.NULL_ORDINATE);
    gf.getPrecisionModel().makePrecise(coords[0]);
    coords[1] = new Coordinate(cx + fx1, cy + fy1, Coordinates.NULL_ORDINATE);
    gf.getPrecisionModel().makePrecise(coords[1]);
    coords[2] = new Coordinate(cx + fx2, cy + fy2, Coordinates.NULL_ORDINATE);
    gf.getPrecisionModel().makePrecise(coords[2]);
    coords[3] = new Coordinate(cx, cy, Coordinates.NULL_ORDINATE);
    gf.getPrecisionModel().makePrecise(coords[3]);

    return gf.linearRing(coords);
  }

  /**
   * As the user increases the number of points, the probability of creating a random valid polygon decreases. 
   * Please take not of this when selecting the generation style, and the number of points. 
   * 
   * May return null if a geometry could not be created.
   * 
   * @see #getNumberPoints()
   * @see #setNumberPoints(int)
   * @see #getGenerationAlgorithm()
   * @see #setGenerationAlgorithm(int)
   * 
   * @see #BOX
   * @see #ARC
   * 
   * @see com.revolsys.jts.testold.generator.GeometryGenerator#create()
   * 
   * @throws IllegalStateException When the alg is not valid or the number of points is invalid
   * @throws NullPointerException when either the Geometry Factory, or the Bounding Box are undefined.
   */
  @Override
  public Geometry create() {

    if (this.geometryFactory == null) {
      throw new NullPointerException("GeometryFactoryI is not declared");
    }
    if (this.boundingBox == null || this.boundingBox.isEmpty()) {
      throw new NullPointerException("Bounding Box is not declared");
    }
    if (this.numberPoints < 4) {
      throw new IllegalStateException("Too few points");
    }

    final double x = this.boundingBox.getMinX(); // base x
    final double dx = this.boundingBox.getMaxX() - x;

    final double y = this.boundingBox.getMinY(); // base y
    final double dy = this.boundingBox.getMaxY() - y;

    Polygon p = null;

    for (int i = 0; i < RUNS; i++) {
      switch (getGenerationAlgorithm()) {
        case BOX:
          p = createBox(x, dx, y, dy, this.numberHoles, this.numberPoints,
            this.geometryFactory);
        break;
        case ARC:
          p = createArc(x, dx, y, dy, this.numberHoles, this.numberPoints,
            this.geometryFactory);
        break;
        default:
          throw new IllegalStateException("Invalid Alg. Specified");
      }

      final IsValidOp valid = new IsValidOp(p);
      if (valid.isValid()) {
        return p;
      }
    }
    return null;
  }

  /**
   * @return Returns the generationAlgorithm.
   */
  public int getGenerationAlgorithm() {
    return this.generationAlgorithm;
  }

  /**
   * @return Returns the numberHoles.
   */
  public int getNumberHoles() {
    return this.numberHoles;
  }

  /**
   * @return Returns the numberPoints.
   */
  public int getNumberPoints() {
    return this.numberPoints;
  }

  /**
   * @param generationAlgorithm The generationAlgorithm to set.
   */
  public void setGenerationAlgorithm(final int generationAlgorithm) {
    this.generationAlgorithm = generationAlgorithm;
  }

  /**
   * @param numberHoles The numberHoles to set.
   */
  public void setNumberHoles(final int numberHoles) {
    this.numberHoles = numberHoles;
  }

  /**
   * @param numberPoints The numberPoints to set.
   */
  public void setNumberPoints(final int numberPoints) {
    this.numberPoints = numberPoints;
  }

}
