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

package com.revolsys.jts.triangulate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Geometry;

/**
 * Creates a map between the vertex {@link Coordinates}s of a 
 * set of {@link Geometry}s,
 * and the parent geometry, and transfers the source geometry
 * data objects to geometry components tagged with the coordinates.
 * <p>
 * This class can be used in conjunction with {@link VoronoiDiagramBuilder}
 * to transfer data objects from the input site geometries
 * to the constructed Voronoi polygons.
 * 
 * @author Martin Davis
 * @see VoronoiDiagramBuilder
 *
 */
public class VertexTaggedGeometryDataMapper {
  private final Map<Point, Object> coordDataMap = new TreeMap<>();

  public VertexTaggedGeometryDataMapper() {

  }

  public List<Point> getCoordinates() {
    return new ArrayList<>(coordDataMap.keySet());
  }

  public void loadSourceGeometries(final Collection<Geometry> geoms) {
    for (final Geometry geom : geoms) {
      loadVertices(geom.vertices(), geom.getUserData());
    }
  }

  public void loadSourceGeometries(final Geometry geomColl) {
    for (int i = 0; i < geomColl.getGeometryCount(); i++) {
      final Geometry geom = geomColl.getGeometry(i);
      loadVertices(geom.vertices(), geom.getUserData());
    }
  }

  private void loadVertices(final Iterable<? extends Point> points,
    final Object data) {
    for (final Point point : points) {
      coordDataMap.put(point.cloneCoordinates(), data);
    }
  }

  /**
   * Input is assumed to be a multiGeometry
   * in which every component has its userData
   * set to be a Point which is the key to the output data.
   * The Point is used to determine
   * the output data object to be written back into the component. 
   * 
   * @param targetGeom
   */
  public void transferData(final Geometry targetGeom) {
    for (int i = 0; i < targetGeom.getGeometryCount(); i++) {
      final Geometry geom = targetGeom.getGeometry(i);
      final Point vertexKey = (Point)geom.getUserData();
      if (vertexKey == null) {
        continue;
      }
      geom.setUserData(coordDataMap.get(vertexKey));
    }
  }
}
