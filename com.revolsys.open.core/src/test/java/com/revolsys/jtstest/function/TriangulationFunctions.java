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
package com.revolsys.jtstest.function;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.triangulate.ConformingDelaunayTriangulationBuilder;
import com.revolsys.jts.triangulate.DelaunayTriangulationBuilder;
import com.revolsys.jts.triangulate.VertexTaggedGeometryDataMapper;
import com.revolsys.jts.triangulate.VoronoiDiagramBuilder;
import com.revolsys.jts.triangulate.quadedge.LocateFailureException;

public class TriangulationFunctions {
  public static Geometry conformingDelaunayEdges(final Geometry sites,
    final Geometry constraints) {
    return conformingDelaunayEdgesWithTolerance(sites, constraints,
      TRIANGULATION_TOLERANCE);
  }

  public static Geometry conformingDelaunayEdgesWithTolerance(
    final Geometry sites, final Geometry constraints, final double tol) {
    final ConformingDelaunayTriangulationBuilder builder = new ConformingDelaunayTriangulationBuilder();
    builder.setSites(sites);
    builder.setConstraints(constraints);
    builder.setTolerance(tol);

    final GeometryFactory geomFact = sites != null ? sites.getGeometryFactory()
      : constraints.getGeometryFactory();
    final Geometry tris = builder.getEdges(geomFact);
    return tris;
  }

  public static Geometry conformingDelaunayTriangles(final Geometry sites,
    final Geometry constraints) {
    return conformingDelaunayTrianglesWithTolerance(sites, constraints,
      TRIANGULATION_TOLERANCE);
  }

  public static Geometry conformingDelaunayTrianglesWithTolerance(
    final Geometry sites, final Geometry constraints, final double tol) {
    final ConformingDelaunayTriangulationBuilder builder = new ConformingDelaunayTriangulationBuilder();
    builder.setSites(sites);
    builder.setConstraints(constraints);
    builder.setTolerance(tol);

    final GeometryFactory geomFact = sites != null ? sites.getGeometryFactory()
      : constraints.getGeometryFactory();
    final Geometry tris = builder.getTriangles(geomFact);
    return tris;
  }

  public static Geometry delaunayEdges(final Geometry geom) {
    final DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();
    builder.setSites(geom);
    builder.setTolerance(TRIANGULATION_TOLERANCE);
    final Geometry edges = builder.getEdges(geom.getGeometryFactory());
    return edges;
  }

  public static Geometry delaunayEdgesWithTolerance(final Geometry geom,
    final double tolerance) {
    final DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();
    builder.setSites(geom);
    builder.setTolerance(tolerance);
    final Geometry edges = builder.getEdges(geom.getGeometryFactory());
    return edges;
  }

  public static Geometry delaunayTriangles(final Geometry geom) {
    final DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();
    builder.setSites(geom);
    builder.setTolerance(TRIANGULATION_TOLERANCE);
    final Geometry tris = builder.getTriangles(geom.getGeometryFactory());
    return tris;
  }

  public static Geometry delaunayTrianglesWithTolerance(final Geometry geom,
    final double tolerance) {
    final DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();
    builder.setSites(geom);
    builder.setTolerance(tolerance);
    final Geometry tris = builder.getTriangles(geom.getGeometryFactory());
    return tris;
  }

  public static Geometry delaunayTrianglesWithToleranceNoError(
    final Geometry geom, final double tolerance) {
    final DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();
    builder.setSites(geom);
    builder.setTolerance(tolerance);
    try {
      final Geometry tris = builder.getTriangles(geom.getGeometryFactory());
      return tris;
    } catch (final LocateFailureException ex) {
      System.out.println(ex);
      // ignore this exception and drop thru
    }
    /**
     * Get the triangles created up until the error
     */
    final Geometry tris = builder.getSubdivision().getTriangles(
      geom.getGeometryFactory());
    return tris;
  }

  public static Geometry voronoiDiagram(final Geometry sitesGeom,
    final Geometry clipGeom) {
    final VoronoiDiagramBuilder builder = new VoronoiDiagramBuilder();
    builder.setSites(sitesGeom);
    if (clipGeom != null) {
      builder.setClipEnvelope(clipGeom.getBoundingBox());
    }
    builder.setTolerance(TRIANGULATION_TOLERANCE);
    final Geometry diagram = builder.getDiagram(sitesGeom.getGeometryFactory());
    return diagram;
  }

  public static Geometry voronoiDiagramWithData(final Geometry sitesGeom,
    final Geometry clipGeom) {

    final VertexTaggedGeometryDataMapper mapper = new VertexTaggedGeometryDataMapper();
    mapper.loadSourceGeometries(sitesGeom);

    final VoronoiDiagramBuilder builder = new VoronoiDiagramBuilder();
    builder.setSites(mapper.getCoordinates());
    if (clipGeom != null) {
      builder.setClipEnvelope(clipGeom.getBoundingBox());
    }
    builder.setTolerance(TRIANGULATION_TOLERANCE);
    final Geometry diagram = builder.getDiagram(sitesGeom.getGeometryFactory());
    mapper.transferData(diagram);
    return diagram;
  }

  private static final double TRIANGULATION_TOLERANCE = 0.0;

}
