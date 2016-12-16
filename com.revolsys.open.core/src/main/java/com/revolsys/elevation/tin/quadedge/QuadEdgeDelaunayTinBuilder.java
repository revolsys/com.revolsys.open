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
package com.revolsys.elevation.tin.quadedge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.revolsys.collection.list.Lists;
import com.revolsys.elevation.tin.IntArrayScaleTriangulatedIrregularNetwork;
import com.revolsys.elevation.tin.TinBuilder;
import com.revolsys.elevation.tin.TriangleConsumer;
import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.Side;
import com.revolsys.geometry.model.impl.PointDoubleXYZ;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.geometry.util.BoundingBoxUtil;

/**
 * A utility class which creates Delaunay Trianglulations
 * from collections of points and extract the resulting
 * triangulation edges or triangles as geometries.
 *
 * @author Martin Davis
 *
 */
public class QuadEdgeDelaunayTinBuilder implements TinBuilder {
  private double[] bounds = BoundingBoxUtil.newBounds(2);

  private final List<Point> vertices = new ArrayList<>();

  private QuadEdgeSubdivision subdivision;

  private GeometryFactory geometryFactory;

  private double scaleXY;

  private double scaleZ;

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
        this.scaleZ);
    }
  }

  public QuadEdgeDelaunayTinBuilder(final GeometryFactory geometryFactory, final double minX,
    final double minY, final double maxX, final double maxY) {
    this(geometryFactory);
    this.bounds = BoundingBoxUtil.newBounds(this.geometryFactory, minX, minY, maxX, maxY);
  }

  public void addVertex(final Point vertex) {
    this.vertices.add(vertex);
  }

  public void buildTin() {
    if (this.subdivision == null) {
      this.subdivision = new QuadEdgeSubdivision(this.bounds, this.geometryFactory);
      insertVertices(this.subdivision, this.vertices);
    }
  }

  protected void expandBoundingBox(final Iterable<? extends Point> points) {
    for (final Point point : points) {
      BoundingBoxUtil.expand(this.bounds, 2, point);
    }
  }

  protected void expandBounds(final double delta) {
    this.bounds[0] -= delta;
    this.bounds[1] -= delta;
    this.bounds[2] += delta;
    this.bounds[3] += delta;
  }

  /**
   * Gets the faces of the computed triangulation as a {@link Polygonal}.
   *
   * @return the faces of the triangulation
   */
  @Override
  public void forEachTriangle(final TriangleConsumer action) {
    buildTin();
    this.subdivision.forEachTriangle(action);
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.geometryFactory.newBoundingBox(2, this.bounds);
  }

  /**
   * Gets the edges of the computed triangulation as a {@link Lineal}.
   *
   * @return the edges of the triangulation
   */
  public Lineal getEdges() {
    buildTin();
    return this.subdivision.getEdgesLineal(this.geometryFactory);
  }

  @Override
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

  public List<QuadEdge> getTriangleEdges(final double x, final double y) {
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

  /**
   * Gets the faces of the computed triangulation as a {@link Polygonal}.
   *
   * @return the faces of the triangulation
   */
  public Polygonal getTrianglesPolygonal() {
    buildTin();
    return this.subdivision.getTrianglesPolygonal(this.geometryFactory);
  }

  protected List<Point> getVertices() {
    return this.vertices;
  }

  @Override
  public void insertVertex(double x, double y, double z) {
    x = Math.round(x * this.scaleXY) / this.scaleXY;
    y = Math.round(y * this.scaleXY) / this.scaleXY;
    z = Math.round(z * this.scaleZ) / this.scaleZ;
    BoundingBoxUtil.expand(this.bounds, 2, 0, x);
    BoundingBoxUtil.expand(this.bounds, 2, 1, y);
    final Point vertex = newVertex(x, y, z);
    this.vertices.add(vertex);
    if (this.subdivision != null) {
      this.subdivision.insertVertex(vertex);
    }
  }

  @Override
  public void insertVertex(final Point point) {
    final double x = point.getX();
    final double y = point.getY();
    final double z = point.getZ();
    insertVertex(x, y, z);
  }

  /**
   * Sets the sites (vertices) which will be triangulated.
   * All vertices of the given geometry will be used as sites.
   *
   * @param geometry the geometry from which the sites will be extracted.
   */
  public void insertVertices(final Geometry geometry) {
    for (final Vertex point : geometry.vertices()) {
      final double x = point.getX();
      final double y = point.getY();
      final double z = point.getZ();
      insertVertex(x, y, z);
    }
  }

  /**
   * Sets the sites (vertices) which will be triangulated
   * from a collection of {@link Coordinates}s.
   *
   * @param points a collection of Coordinates.
   */
  @Override
  public void insertVertices(final Iterable<? extends Point> points) {
    for (final Point point : points) {
      insertVertex(point);
    }
  }

  protected void insertVertices(final QuadEdgeSubdivision subdivision, final List<Point> vertices) {
    Collections.sort(vertices);
    subdivision.insertVertices(vertices);
  }

  @Override
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
    forEachTriangle(new TriangleConsumer() {

      private int coordinateIndex = 0;

      @Override
      public void accept(final double x1, final double y1, final double z1, final double x2,
        final double y2, final double z2, final double x3, final double y3, final double z3) {
        triangleXCoordinates[this.coordinateIndex] = (int)Math
          .round(x1 * QuadEdgeDelaunayTinBuilder.this.scaleXY);
        triangleYCoordinates[this.coordinateIndex] = (int)Math
          .round(y1 * QuadEdgeDelaunayTinBuilder.this.scaleXY);
        triangleZCoordinates[this.coordinateIndex++] = (int)Math
          .round(z1 * QuadEdgeDelaunayTinBuilder.this.scaleZ);
        triangleXCoordinates[this.coordinateIndex] = (int)Math
          .round(x2 * QuadEdgeDelaunayTinBuilder.this.scaleXY);
        triangleYCoordinates[this.coordinateIndex] = (int)Math
          .round(y2 * QuadEdgeDelaunayTinBuilder.this.scaleXY);
        triangleZCoordinates[this.coordinateIndex++] = (int)Math
          .round(z2 * QuadEdgeDelaunayTinBuilder.this.scaleZ);
        triangleXCoordinates[this.coordinateIndex] = (int)Math
          .round(x3 * QuadEdgeDelaunayTinBuilder.this.scaleXY);
        triangleYCoordinates[this.coordinateIndex] = (int)Math
          .round(y3 * QuadEdgeDelaunayTinBuilder.this.scaleXY);
        triangleZCoordinates[this.coordinateIndex++] = (int)Math
          .round(z3 * QuadEdgeDelaunayTinBuilder.this.scaleZ);
      }
    });
    return new IntArrayScaleTriangulatedIrregularNetwork(this.geometryFactory, boundingBox,
      triangleCount, triangleXCoordinates, triangleYCoordinates, triangleZCoordinates);
  }

  protected PointDoubleXYZ newVertex(final double x, final double y, final double z) {
    return new PointDoubleXYZ(x, y, z);
  }
}
