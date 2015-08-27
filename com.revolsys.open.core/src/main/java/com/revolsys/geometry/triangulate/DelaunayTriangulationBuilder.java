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
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.CoordinateArrays;
import com.revolsys.geometry.model.CoordinateList;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryCollection;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.MultiLineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.geometry.triangulate.quadedge.QuadEdgeSubdivision;
import com.revolsys.geometry.triangulate.quadedge.Vertex;

/**
 * A utility class which creates Delaunay Trianglulations
 * from collections of points and extract the resulting
 * triangulation edges or triangles as geometries.
 *
 * @author Martin Davis
 *
 */
public class DelaunayTriangulationBuilder {
  /**
   * Computes the {@link BoundingBoxDoubleGf} of a collection of {@link Coordinates}s.
   *
   * @param coords a List of Coordinates
   * @return the envelope of the set of coordinates
   */
  public static BoundingBoxDoubleGf envelope(final Collection<Point> coords) {
    return new BoundingBoxDoubleGf(coords);
  }

  /**
   * Extracts the unique {@link Coordinates}s from the given {@link Geometry}.
   * @param geom the geometry to extract from
   * @return a List of the unique Coordinates
   */
  public static CoordinateList extractUniqueCoordinates(final Geometry geom) {
    if (geom == null) {
      return new CoordinateList();
    }

    final Point[] coords = CoordinatesListUtil.getCoordinateArray(geom);
    return unique(coords);
  }

  /**
   * Converts all {@link Coordinates}s in a collection to {@link Vertex}es.
   * @param coords the coordinates to convert
   * @return a List of Vertex objects
   */
  public static List toVertices(final Collection coords) {
    final List verts = new ArrayList();
    for (final Iterator i = coords.iterator(); i.hasNext();) {
      final Point coord = (Point)i.next();
      verts.add(new Vertex(coord));
    }
    return verts;
  }

  public static CoordinateList unique(final Point[] coords) {
    final Point[] coordsCopy = CoordinateArrays.copyDeep(coords);
    Arrays.sort(coordsCopy);
    final CoordinateList coordList = new CoordinateList(coordsCopy, false);
    return coordList;
  }

  private Collection siteCoords;

  private QuadEdgeSubdivision subdiv = null;

  private double tolerance = 0.0;

  /**
   * Creates a new triangulation builder.
   *
   */
  public DelaunayTriangulationBuilder() {
  }

  private void create() {
    if (this.subdiv != null) {
      return;
    }

    final BoundingBox siteEnv = envelope(this.siteCoords);
    final List vertices = toVertices(this.siteCoords);
    this.subdiv = new QuadEdgeSubdivision(siteEnv, this.tolerance);
    final IncrementalDelaunayTriangulator triangulator = new IncrementalDelaunayTriangulator(
      this.subdiv);
    triangulator.insertSites(vertices);
  }

  /**
   * Gets the edges of the computed triangulation as a {@link MultiLineString}.
   *
   * @param geomFact the geometry factory to use to create the output
   * @return the edges of the triangulation
   */
  public Geometry getEdges(final GeometryFactory geomFact) {
    create();
    return this.subdiv.getEdges(geomFact);
  }

  /**
   * Gets the {@link QuadEdgeSubdivision} which models the computed triangulation.
   *
   * @return the subdivision containing the triangulation
   */
  public QuadEdgeSubdivision getSubdivision() {
    create();
    return this.subdiv;
  }

  /**
   * Gets the faces of the computed triangulation as a {@link GeometryCollection}
   * of {@link Polygon}.
   *
   * @param geomFact the geometry factory to use to create the output
   * @return the faces of the triangulation
   */
  public Geometry getTriangles(final GeometryFactory geomFact) {
    create();
    return this.subdiv.getTriangles(geomFact);
  }

  /**
   * Sets the sites (vertices) which will be triangulated
   * from a collection of {@link Coordinates}s.
   *
   * @param coords a collection of Coordinates.
   */
  public void setSites(final Collection coords) {
    // remove any duplicate points (they will cause the triangulation to fail)
    this.siteCoords = unique(CoordinateArrays.toCoordinateArray(coords));
  }

  /**
   * Sets the sites (vertices) which will be triangulated.
   * All vertices of the given geometry will be used as sites.
   *
   * @param geom the geometry from which the sites will be extracted.
   */
  public void setSites(final Geometry geom) {
    // remove any duplicate points (they will cause the triangulation to fail)
    this.siteCoords = extractUniqueCoordinates(geom);
  }

  /**
   * Sets the snapping tolerance which will be used
   * to improved the robustness of the triangulation computation.
   * A tolerance of 0.0 specifies that no snapping will take place.
   *
   * @param tolerance the tolerance distance to use
   */
  public void setTolerance(final double tolerance) {
    this.tolerance = tolerance;
  }
}
