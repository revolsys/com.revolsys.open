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
package com.revolsys.geometry.test.function;

import com.revolsys.elevation.tin.quadedge.QuadEdgeConformingDelaunayTinBuilder;
import com.revolsys.elevation.tin.quadedge.LocateFailureException;
import com.revolsys.elevation.tin.quadedge.QuadEdgeDelaunayTinBuilder;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.Polygonal;

public class TriangulationFunctions {
  private static final double TRIANGULATION_TOLERANCE = 0.0;

  public static Lineal conformingDelaunayEdges(final Geometry sites, final Geometry constraints) {
    return conformingDelaunayEdgesWithTolerance(sites, constraints, TRIANGULATION_TOLERANCE);
  }

  public static Lineal conformingDelaunayEdgesWithTolerance(final Geometry sites,
    final Geometry constraints, final double tol) {
    final QuadEdgeConformingDelaunayTinBuilder builder = new QuadEdgeConformingDelaunayTinBuilder();
    builder.setSites(sites);
    builder.setConstraints(constraints);
    builder.setTolerance(tol);

    final GeometryFactory geomFact = sites != null ? sites.getGeometryFactory()
      : constraints.getGeometryFactory();
    final Lineal triangles = builder.getEdgesLineal(geomFact);
    return triangles;
  }

  public static Polygonal conformingDelaunayTriangles(final Geometry sites,
    final Geometry constraints) {
    return conformingDelaunayTrianglesWithTolerance(sites, constraints, TRIANGULATION_TOLERANCE);
  }

  public static Polygonal conformingDelaunayTrianglesWithTolerance(final Geometry sites,
    final Geometry constraints, final double tol) {
    final QuadEdgeConformingDelaunayTinBuilder builder = new QuadEdgeConformingDelaunayTinBuilder();
    builder.setSites(sites);
    builder.setConstraints(constraints);
    builder.setTolerance(tol);

    final GeometryFactory geomFact = sites != null ? sites.getGeometryFactory()
      : constraints.getGeometryFactory();
    final Polygonal triangles = builder.getTrianglesPolygonal(geomFact);
    return triangles;
  }

  public static Lineal delaunayEdges(final Geometry geom) {
    GeometryFactory geometryFactory = geom.getGeometryFactory();
    geometryFactory = geometryFactory.convertScales(0, 0);
    final QuadEdgeDelaunayTinBuilder builder = new QuadEdgeDelaunayTinBuilder(geometryFactory);
    builder.insertVertices(geom);
    final Lineal edges = builder.getEdges();
    return edges;
  }

  public static Geometry delaunayEdgesWithTolerance(final Geometry geom, final double tolerance) {
    GeometryFactory geometryFactory = geom.getGeometryFactory();
    geometryFactory = geometryFactory.convertScales(tolerance, tolerance);
    final QuadEdgeDelaunayTinBuilder builder = new QuadEdgeDelaunayTinBuilder(geometryFactory);
    builder.insertVertices(geom);
    final Lineal edges = builder.getEdges();
    return edges;
  }

  public static Polygonal delaunayTriangles(final Geometry geometry) {
    final GeometryFactory geometryFactory = geometry.getGeometryFactory();
    final QuadEdgeDelaunayTinBuilder builder = new QuadEdgeDelaunayTinBuilder(geometryFactory);
    builder.insertVertices(geometry);
    final Polygonal triangles = builder.getTrianglesPolygonal();
    return triangles;
  }

  public static Polygonal delaunayTrianglesWithTolerance(final Geometry geom,
    final double tolerance) {
    GeometryFactory geometryFactory = geom.getGeometryFactory();
    geometryFactory = geometryFactory.convertScales(tolerance, tolerance);
    final QuadEdgeDelaunayTinBuilder builder = new QuadEdgeDelaunayTinBuilder(geometryFactory);
    builder.insertVertices(geom);
    final Polygonal triangles = builder.getTrianglesPolygonal();
    return triangles;
  }

  public static Polygonal delaunayTrianglesWithToleranceNoError(final Geometry geom,
    final double tolerance) {
    GeometryFactory geometryFactory = geom.getGeometryFactory();
    geometryFactory = geometryFactory.convertScales(tolerance, tolerance);
    final QuadEdgeDelaunayTinBuilder builder = new QuadEdgeDelaunayTinBuilder(geometryFactory);
    builder.insertVertices(geom);
    try {
      final Polygonal triangles = builder.getTrianglesPolygonal();
      return triangles;
    } catch (final LocateFailureException ex) {
      System.out.println(ex);
      // ignore this exception and drop thru
    }
    /**
     * Get the triangles created up until the error
     */
    final Polygonal triangles = builder.getSubdivision()
      .getTrianglesPolygonal(geom.getGeometryFactory());
    return triangles;
  }

}
