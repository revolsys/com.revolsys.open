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

import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.PrecisionModel;
import com.revolsys.jts.operation.valid.IsValidOp;

/**
 * 
 * This class is used to create a line string within the specified bounding box.
 * 
 * Sucessive calls to create may or may not return the same geometry topology.
 *
 * @author David Zwiers, Vivid Solutions. 
 */
public class LineStringGenerator extends GeometryGenerator {
  private static void fillArc(final double x, final double dx, final double y,
    final double dy, final Coordinates[] coords, final GeometryFactory gf) {
    if (coords.length == 2) {
      throw new IllegalStateException("Too few points for Arc");
    }

    final double theta = 360 / coords.length;
    final double start = theta / 2;

    final double radius = dx < dy ? dx / 3 : dy / 3;

    final double cx = x + dx / 2; // center
    final double cy = y + dy / 2; // center

    final PrecisionModel precisionModel = gf.getPrecisionModel();
    for (int i = 0; i < coords.length; i++) {
      final double angle = Math.toRadians(start + theta * i);

      final double fx = Math.sin(angle) * radius; // may be neg.
      final double fy = Math.cos(angle) * radius; // may be neg.

      coords[i] = new DoubleCoordinates(precisionModel.makePrecise(cx + fx),
        precisionModel.makePrecise(cy + fy));
    }
  }

  private static void fillHorz(final double x, final double dx, final double y,
    final double dy, final Coordinates[] coords, final GeometryFactory gf) {
    final double fy = y + Math.random() * dy;
    double rx = dx; // remainder of x distance
    final PrecisionModel precisionModel = gf.getPrecisionModel();
    coords[0] = new DoubleCoordinates(precisionModel.makePrecise(x),
      precisionModel.makePrecise(fy));
    for (int i = 1; i < coords.length - 1; i++) {
      rx -= Math.random() * rx;
      coords[i] = new DoubleCoordinates(
        precisionModel.makePrecise(x + dx - rx), precisionModel.makePrecise(fy));
    }
    coords[coords.length - 1] = new DoubleCoordinates(
      precisionModel.makePrecise(x + dx), precisionModel.makePrecise(fy));
  }

  private static void fillVert(final double x, final double dx, final double y,
    final double dy, final Coordinates[] coords, final GeometryFactory gf) {
    final double fx = x + Math.random() * dx;
    double ry = dy; // remainder of y distance
    final PrecisionModel precisionModel = gf.getPrecisionModel();

    coords[0] = new DoubleCoordinates(precisionModel.makePrecise(fx),
      precisionModel.makePrecise(y));
    for (int i = 1; i < coords.length - 1; i++) {
      ry -= Math.random() * ry;
      coords[i] = new DoubleCoordinates(precisionModel.makePrecise(fx),
        precisionModel.makePrecise(y + dy - ry));
    }
    coords[coords.length - 1] = new DoubleCoordinates(
      precisionModel.makePrecise(fx), precisionModel.makePrecise(y + dy));
  }

  protected int numberPoints = 2;

  protected int generationAlgorithm = 0;

  /**
   * Create the points in a vertical line
   */
  public static final int VERT = 1;

  /**
   * Create the points in a horizontal line
   */
  public static final int HORZ = 2;

  /**
   * Create the points in an approximation of an open circle (one edge will not be included).
   * 
   * Note: this requires the number of points to be greater than 2.
   * 
   * @see #getNumberPoints()
   * @see #setNumberPoints(int)
   */
  public static final int ARC = 0;

  /**
   * Number of interations attempting to create a valid line string
   */
  private static final int RUNS = 5;

  /**
   * As the user increases the number of points, the probability of creating a random valid linestring decreases. 
   * Please take not of this when selecting the generation style, and the number of points. 
   * 
   * May return null if a geometry could not be created.
   * 
   * @see #getNumberPoints()
   * @see #setNumberPoints(int)
   * @see #getGenerationAlgorithm()
   * @see #setGenerationAlgorithm(int)
   * 
   * @see #VERT
   * @see #HORZ
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
    if (this.numberPoints < 2) {
      throw new IllegalStateException("Too few points");
    }

    final Coordinates[] coords = new Coordinates[this.numberPoints];

    final double x = this.boundingBox.getMinX(); // base x
    final double dx = this.boundingBox.getMaxX() - x;

    final double y = this.boundingBox.getMinY(); // base y
    final double dy = this.boundingBox.getMaxY() - y;

    for (int i = 0; i < RUNS; i++) {
      switch (getGenerationAlgorithm()) {
        case VERT:
          fillVert(x, dx, y, dy, coords, this.geometryFactory);
        break;
        case HORZ:
          fillHorz(x, dx, y, dy, coords, this.geometryFactory);
        break;
        case ARC:
          fillArc(x, dx, y, dy, coords, this.geometryFactory);
        break;
        default:
          throw new IllegalStateException("Invalid Alg. Specified");
      }

      final LineString ls = this.geometryFactory.lineString(coords);
      final IsValidOp valid = new IsValidOp(ls);
      if (valid.isValid()) {
        return ls;
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
   * @param numberPoints The numberPoints to set.
   */
  public void setNumberPoints(final int numberPoints) {
    this.numberPoints = numberPoints;
  }

}
