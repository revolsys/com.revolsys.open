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
package com.revolsys.geometry.triangulate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.revolsys.elevation.tin.TriangleConsumer;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.geometry.triangulate.quadedge.QuadEdgeSubdivision;
import com.revolsys.geometry.triangulate.quadedge.QuadEdgeVertex;
import com.revolsys.geometry.util.BoundingBoxUtil;

/**
 * A utility class which creates Delaunay Trianglulations
 * from collections of points and extract the resulting
 * triangulation edges or triangles as geometries.
 *
 * @author Martin Davis
 *
 */
public class DelaunayTriangulationBuilder {

  private final double[] bounds = BoundingBoxUtil.newBounds(2);

  private final List<QuadEdgeVertex> vertices = new ArrayList<>();

  private QuadEdgeSubdivision subdiv;

  private GeometryFactory geometryFactory;

  /**
   * Creates a new triangulation builder.
   *
   */
  public DelaunayTriangulationBuilder() {
    this.geometryFactory = GeometryFactory.DEFAULT_3D;
  }

  public DelaunayTriangulationBuilder(final GeometryFactory geometryFactory) {
    if (geometryFactory == null) {
      this.geometryFactory = GeometryFactory.DEFAULT_3D;
    } else {
      this.geometryFactory = geometryFactory.convertAxisCount(3);
    }
  }

  public void addPoint(double x, double y, double z) {
    x = this.geometryFactory.makeXyPrecise(x);
    y = this.geometryFactory.makeXyPrecise(y);
    z = this.geometryFactory.makeXyPrecise(z);
    final QuadEdgeVertex vertex = new QuadEdgeVertex(x, y, z);
    BoundingBoxUtil.expand(this.bounds, 2, 0, x);
    BoundingBoxUtil.expand(this.bounds, 2, 1, y);
    this.vertices.add(vertex);
  }

  public void addPoint(final Point point) {
    final double x = point.getX();
    final double y = point.getY();
    final double z = point.getZ();
    addPoint(x, y, z);
  }

  /**
   * Sets the sites (vertices) which will be triangulated
   * from a collection of {@link Coordinates}s.
   *
   * @param points a collection of Coordinates.
   */
  public void addPoints(final Collection<? extends Point> points) {
    for (final Point point : points) {
      addPoint(point);
    }
  }

  /**
   * Sets the sites (vertices) which will be triangulated.
   * All vertices of the given geometry will be used as sites.
   *
   * @param geom the geometry from which the sites will be extracted.
   */
  public void addPoints(final Geometry geom) {
    for (final Vertex point : geom.vertices()) {
      final double x = point.getX();
      final double y = point.getY();
      final double z = point.getZ();
      addPoint(x, y, z);
    }
  }

  public void buildTin() {
    if (this.subdiv == null) {
      this.subdiv = new QuadEdgeSubdivision(this.bounds, this.geometryFactory);
      final IncrementalDelaunayTriangulator triangulator = new IncrementalDelaunayTriangulator(
        this.subdiv);
      Collections.sort(this.vertices);
      triangulator.insertSites(this.vertices);
    }
  }

  /**
   * Gets the faces of the computed triangulation as a {@link Polygonal}.
   *
   * @return the faces of the triangulation
   */
  public void forEachTriangle(final TriangleConsumer action) {
    buildTin();
    this.subdiv.forEachTriangle(action);
  }

  /**
   * Gets the edges of the computed triangulation as a {@link Lineal}.
   *
   * @return the edges of the triangulation
   */
  public Geometry getEdges() {
    buildTin();
    return this.subdiv.getEdges(this.geometryFactory);
  }

  /**
   * Gets the {@link QuadEdgeSubdivision} which models the computed triangulation.
   *
   * @return the subdivision containing the triangulation
   */
  public QuadEdgeSubdivision getSubdivision() {
    buildTin();
    return this.subdiv;
  }

  /**
   * Gets the faces of the computed triangulation as a {@link Polygonal}.
   *
   * @return the faces of the triangulation
   */
  public Polygonal getTriangles() {
    buildTin();
    return this.subdiv.getTriangles(this.geometryFactory);
  }
}
