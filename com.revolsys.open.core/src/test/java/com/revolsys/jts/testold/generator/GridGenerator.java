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

import java.util.NoSuchElementException;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;

/**
 * This class should be used to generate a grid of bounding boxes,
 * most useful when creating multiple geometries.
 *
 * Successive calls to create() will walk the user though the grid.
 * Use canCreate() and reset() to control the walk through the grid.
 *
 * @see #canCreate()
 * @see #reset()
 *
 * @author David Zwiers, Vivid Solutions.
 */
public class GridGenerator extends GeometryGenerator {

  protected int numberColumns = 1;

  protected int numberRows = 1;

  protected int index = 0;

  /**
   * Sets some default values.
   */
  public GridGenerator() {
    this.dimensions = 2;
  }

  /**
   * @return true when more grids exist
   */
  public boolean canCreate() {
    return this.numberColumns * this.numberRows > this.index;
  }

  /**
   *
   * @see com.revolsys.jts.testold.generator.GeometryGenerator#create()
   *
   * @throws NoSuchElementException when all the grids have been created (@see #create())
   * @throws NullPointerException when either the Geometry Factory, or the Bounding Box are undefined.
   */
  @Override
  public Geometry create() {
    final GeometryFactory r = this.geometryFactory;
    return createEnv().toGeometry();
  }

  /**
   *
   * @return BoundingBoxDoubleGf
   *
   * @see com.revolsys.jts.testold.generator.GeometryGenerator#create()
   *
   * @throws NoSuchElementException when all the grids have been created (@see #create())
   * @throws NullPointerException when either the Geometry Factory, or the Bounding Box are undefined.
   */
  public BoundingBox createEnv() {
    if (!canCreate()) {
      throw new NoSuchElementException("There are not any grids left to create.");
    }
    if (this.geometryFactory == null) {
      throw new NullPointerException("GeometryFactoryI is not declared");
    }
    if (this.boundingBox == null || this.boundingBox.isEmpty()) {
      throw new NullPointerException("Bounding Box is not declared");
    }

    final double x = this.boundingBox.getMinX(); // base x
    final double dx = this.boundingBox.getMaxX() - x;

    final double y = this.boundingBox.getMinY(); // base y
    final double dy = this.boundingBox.getMaxY() - y;

    final int row = this.numberRows == 1 ? 0 : this.index / this.numberRows;
    final int col = this.numberColumns == 1 ? 0 : this.index % this.numberColumns;

    double sx, sy; // size of a step
    sx = dx / this.numberColumns;
    sy = dy / this.numberRows;

    double minx, miny;
    minx = x + col * sx;
    miny = y + row * sy;

    final BoundingBox box = new BoundingBoxDoubleGf(2, this.geometryFactory.makePrecise(0, minx),
      this.geometryFactory.makePrecise(1, miny), this.geometryFactory.makePrecise(0, minx + sx),
      this.geometryFactory.makePrecise(1, miny + sy));

    this.index++;
    return box;
  }

  /**
   * @return Returns the numberColumns.
   */
  public int getNumberColumns() {
    return this.numberColumns;
  }

  /**
   * @return Returns the numberRows.
   */
  public int getNumberRows() {
    return this.numberRows;
  }

  /**
   * Resets the grid counter
   */
  public void reset() {
    this.index = 0;
  }

  /**
   * @see com.revolsys.jts.testold.generator.GeometryGenerator#setDimensions(int)
   */
  @Override
  public void setDimensions(final int dimensions) {
    if (dimensions != 2) {
      throw new IllegalStateException("MAY NOT CHANGE GridGenerator's Dimensions");
    }
  }

  /**
   * @param numberColumns The numberColumns to set.
   */
  public void setNumberColumns(final int numberColumns) {
    if (numberColumns <= 0) {
      throw new IndexOutOfBoundsException("Index sizes must be positive, non zero");
    }
    this.numberColumns = numberColumns;
  }

  /**
   * @param numberRows The numberRows to set.
   */
  public void setNumberRows(final int numberRows) {
    if (numberRows <= 0) {
      throw new IndexOutOfBoundsException("Index sizes must be positive, non zero");
    }
    this.numberRows = numberRows;
  }

}
