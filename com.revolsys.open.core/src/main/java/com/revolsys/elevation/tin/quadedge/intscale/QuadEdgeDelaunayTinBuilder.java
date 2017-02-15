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
package com.revolsys.elevation.tin.quadedge.intscale;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.revolsys.collection.list.Lists;
import com.revolsys.elevation.tin.IntArrayScaleTriangulatedIrregularNetwork;
import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Side;
import com.revolsys.geometry.util.BoundingBoxUtil;

/**
 * A utility class which creates Delaunay Trianglulations
 * from collections of points and extract the resulting
 * triangulation edges or triangles as geometries.
 *
 * @author Martin Davis
 *
 */
public class QuadEdgeDelaunayTinBuilder {
  private int[] bounds = new int[] {
    Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE
  };

  private final List<PointIntXYZ> vertices = new ArrayList<>();

  private QuadEdgeSubdivision subdivision;

  private GeometryFactory geometryFactory;

  private double scaleXY;

  private double scaleZ;

  private boolean sortVertices = false;

  public QuadEdgeDelaunayTinBuilder(final GeometryFactory geometryFactory) {
    if (geometryFactory == null) {
      throw new NullPointerException("A geometryFactory must be specified");
    } else {
      this.geometryFactory = geometryFactory.convertAxisCount(3);
      this.scaleXY = geometryFactory.getScaleXY();
      if (this.scaleXY <= 0) {
        if (geometryFactory.isGeographics()) {
          this.scaleXY = 10000000;
        } else {
          this.scaleXY = 1000;
        }
      }
      this.scaleZ = geometryFactory.getScaleZ();
      if (this.scaleZ <= 0) {
        this.scaleZ = 1000;
      }
      this.geometryFactory = geometryFactory.convertAxisCountAndScales(3, this.scaleXY,
        this.scaleXY, this.scaleZ);
    }
  }

  public QuadEdgeDelaunayTinBuilder(final GeometryFactory geometryFactory, final int minX,
    final int minY, final int maxX, final int maxY) {
    this(geometryFactory);
    this.bounds = new int[] {
      minX, minY, maxX, maxY
    };
  }

  public void addVertex(final PointIntXYZ vertex) {
    this.vertices.add(vertex);
  }

  public void buildTin() {
    if (this.subdivision == null) {
      this.subdivision = new QuadEdgeSubdivision(this.bounds, this.geometryFactory);
      insertVertices(this.subdivision, this.vertices);
    }
  }

  protected void expandBoundingBox(final Iterable<? extends PointIntXYZ> points) {
    for (final PointIntXYZ point : points) {
      BoundingBoxUtil.expand(this.bounds, 2, point);
    }
  }

  protected void expandBounds(final double delta) {
    this.bounds[0] -= delta;
    this.bounds[1] -= delta;
    this.bounds[2] += delta;
    this.bounds[3] += delta;
  }

  public void forEachTriangle(final TriangleConsumerInt action) {
    buildTin();
    this.subdivision.forEachTriangle(action);
  }

  public BoundingBox getBoundingBox() {
    return this.geometryFactory.newBoundingBox(2, this.bounds);
  }

  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  /**
   * Gets the {@link QuadEdgeSubdivision} which models the computed triangulation.
   *
   * @return the subdivision containing the triangulation
   */
  public QuadEdgeSubdivision getSubdivision() {
    buildTin();
    return this.subdivision;
  }

  public int getTriangleCount() {
    buildTin();
    return this.subdivision.getTriangleCount();
  }

  public List<QuadEdge> getTriangleEdges(final int x, final int y) {
    final QuadEdgeSubdivision subdivision = getSubdivision();
    final QuadEdge edge1 = subdivision.findQuadEdge(x, y);
    final Side side = edge1.getSide(x, y);
    final QuadEdge edge2;
    final QuadEdge edge3;
    if (side.isLeft()) {
      edge2 = edge1.getLeftNext();
      edge3 = edge1.getLeftPrevious();
    } else {
      edge2 = edge1.getRightNext();
      edge3 = edge1.getRightPrevious();
    }
    return Lists.newArray(edge1, edge2, edge3);

  }

  public void insertVertex(final int x, final int y, final int z) {
    if (x < this.bounds[0]) {
      this.bounds[0] = x;
    }
    if (x > this.bounds[1]) {
      this.bounds[1] = x;
    }
    if (y < this.bounds[2]) {
      this.bounds[0] = y;
    }
    if (y > this.bounds[3]) {
      this.bounds[1] = y;
    }
    final PointIntXYZ vertex = new PointIntXYZ(x, y, z);
    this.vertices.add(vertex);
    if (this.subdivision != null) {
      this.subdivision.insertVertex(vertex);
    }
  }

  protected void insertVertices(final QuadEdgeSubdivision subdivision,
    final List<PointIntXYZ> vertices) {
    if (this.sortVertices) {
      Collections.sort(vertices);
    }
    subdivision.insertVertices(vertices);
  }

  public boolean isSortVertices() {
    return this.sortVertices;
  }

  public TriangulatedIrregularNetwork newTriangulatedIrregularNetwork() {
    buildTin();
    final BoundingBox boundingBox = getBoundingBox();
    final AtomicInteger triangleCounter = new AtomicInteger();
    forEachTriangle((x1, y1, z1, x2, y2, z2, x3, y3, z3) -> {
      triangleCounter.incrementAndGet();
    });
    final int triangleCount = triangleCounter.get();
    final int[] triangleXCoordinates = new int[triangleCount * 3];
    final int[] triangleYCoordinates = new int[triangleCount * 3];
    final int[] triangleZCoordinates = new int[triangleCount * 3];
    forEachTriangle(new TriangleConsumerInt() {

      private int coordinateIndex = 0;

      @Override
      public void accept(final int x1, final int y1, final int z1, final int x2, final int y2,
        final int z2, final int x3, final int y3, final int z3) {
        triangleXCoordinates[this.coordinateIndex] = x1;
        triangleYCoordinates[this.coordinateIndex] = y1;
        triangleZCoordinates[this.coordinateIndex++] = z1;
        triangleXCoordinates[this.coordinateIndex] = x2;
        triangleYCoordinates[this.coordinateIndex] = y2;
        triangleZCoordinates[this.coordinateIndex++] = z2;
        triangleXCoordinates[this.coordinateIndex] = x3;
        triangleYCoordinates[this.coordinateIndex] = y3;
        triangleZCoordinates[this.coordinateIndex++] = z3;
      }
    });
    return new IntArrayScaleTriangulatedIrregularNetwork(this.geometryFactory, boundingBox,
      triangleCount, triangleXCoordinates, triangleYCoordinates, triangleZCoordinates);
  }

  public void setSortVertices(final boolean sortVertices) {
    this.sortVertices = sortVertices;
  }
}
