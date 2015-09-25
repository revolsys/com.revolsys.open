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
package com.revolsys.geometry.test.old.generator;

import java.util.ArrayList;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;

/**
 *
 * Cascades the effort of creating a set of topologically valid geometries.
 *
 * @author David Zwiers, Vivid Solutions.
 */
public class MultiGenerator extends GeometryGenerator {

  /**
   * Grid style blocks
   */
  public static final int BOX = 0;

  /**
   * Horizontal strips
   */
  public static final int HORZ = 2;

  /**
   * vertical strips
   */
  public static final int VERT = 1;

  private final int generationAlgorithm = 0;

  private GeometryGenerator generator = null;

  private int numberGeometries = 2;

  /**
   * @param generator
   */
  public MultiGenerator(final GeometryGenerator generator) {
    this.generator = generator;
  }

  /**
   * Creates a geometry collection representing the set of child geometries created.
   *
   * @see #setNumberGeometries(int)
   * @see com.revolsys.geometry.testold.generator.GeometryGenerator#create()
   *
   * @see #BOX
   * @see #VERT
   * @see #HORZ
   *
   * @throws NullPointerException when the generator is missing
   * @throws IllegalStateException when the number of child geoms is too small
   * @throws IllegalStateException when the selected alg. is invalid
   */
  @Override
  public Geometry create() {
    if (this.generator == null) {
      throw new NullPointerException("Missing child generator");
    }

    if (this.numberGeometries < 1) {
      throw new IllegalStateException("Too few child geoms to create");
    }

    final ArrayList geoms = new ArrayList(this.numberGeometries);

    final GridGenerator grid = GeometryGenerator.createGridGenerator();
    grid.setBoundingBox(this.boundingBox);
    grid.setGeometryFactory(this.geometryFactory);

    switch (this.generationAlgorithm) {
      case BOX:

        final int nrow = (int)Math.sqrt(this.numberGeometries);
        final int ncol = this.numberGeometries / nrow;
        grid.setNumberRows(nrow);
        grid.setNumberColumns(ncol);

      break;
      case VERT:

        grid.setNumberRows(1);
        grid.setNumberColumns(this.numberGeometries);

      break;
      case HORZ:

        grid.setNumberRows(this.numberGeometries);
        grid.setNumberColumns(1);

      break;
      default:
        throw new IllegalStateException("Invalid Alg. Specified");
    }

    while (grid.canCreate()) {
      this.generator.setBoundingBox(grid.createEnv());
      geoms.add(this.generator.create());
    }

    // yes ... there are better ways
    if (this.generator instanceof PointGenerator) {
      return this.geometryFactory.multiPoint(geoms);
    } else {
      if (this.generator instanceof LineStringGenerator) {
        return this.geometryFactory.multiLineString(geoms);
      } else {
        if (this.generator instanceof PolygonGenerator) {
          return this.geometryFactory.multiPolygon(geoms);
        } else {
          // same as multi
          return this.geometryFactory.geometryCollection(geoms);
        }
      }
    }
  }

  /**
   * @return Returns the generator.
   */
  public GeometryGenerator getGenerator() {
    return this.generator;
  }

  /**
   * @return Returns the numberGeometries.
   */
  public int getNumberGeometries() {
    return this.numberGeometries;
  }

  @Override
  public void setBoundingBox(final BoundingBox boundingBox) {
    super.setBoundingBox(boundingBox);
    if (this.generator != null) {
      this.generator.setBoundingBox(boundingBox);
    }
  }

  /**
   * @see com.revolsys.geometry.testold.generator.GeometryGenerator#setDimensions(int)
   */
  @Override
  public void setDimensions(final int dimensions) {
    super.setDimensions(dimensions);
    if (this.generator != null) {
      this.generator.setDimensions(dimensions);
    }
  }

  /**
   * @see com.revolsys.geometry.testold.generator.GeometryGenerator#setGeometryFactory(com.revolsys.geometry.model.GeometryFactory)
   */
  @Override
  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    super.setGeometryFactory(geometryFactory);
    if (this.generator != null) {
      this.generator.setGeometryFactory(geometryFactory);
    }
  }

  /**
   * @param numberGeometries The numberGeometries to set.
   */
  public void setNumberGeometries(final int numberGeometries) {
    this.numberGeometries = numberGeometries;
  }
}
