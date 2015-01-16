package com.revolsys.gis.algorithm.index.quadtree;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

import com.revolsys.data.equals.GeometryEqualsExact3d;
import com.revolsys.gis.jts.GeometryProperties;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.vertex.Vertex;

public class GeometryVertexQuadTree extends IdObjectQuadTree<Vertex> {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public static GeometryVertexQuadTree getGeometryVertexIndex(
    final Geometry geometry) {
    if (geometry != null && !geometry.isEmpty()) {
      final Reference<GeometryVertexQuadTree> reference = GeometryProperties.getGeometryProperty(
        geometry, GEOMETRY_VERTEX_INDEX);
      GeometryVertexQuadTree index;
      if (reference == null) {
        index = null;
      } else {
        index = reference.get();
      }
      if (index == null) {
        index = new GeometryVertexQuadTree(geometry);
        GeometryProperties.setGeometryProperty(geometry, GEOMETRY_VERTEX_INDEX,
          new SoftReference<GeometryVertexQuadTree>(index));
      }
      return index;
    }
    return new GeometryVertexQuadTree(null);
  }

  private final Geometry geometry;

  public static final String GEOMETRY_VERTEX_INDEX = "GeometryVertexQuadTree";

  static {
    GeometryEqualsExact3d.addExclude(GEOMETRY_VERTEX_INDEX);
  }

  public GeometryVertexQuadTree(final Geometry geometry) {
    this.geometry = geometry;
    if (geometry != null) {
      setGeometryFactory(geometry.getGeometryFactory());
      for (final Vertex vertex : geometry.vertices()) {
        final BoundingBox boundingBox = vertex.getBoundingBox();
        insert(boundingBox, vertex);
      }
    }
  }

  @Override
  protected double[] getBounds(final Object id) {
    final Vertex vertex = getItem(id);
    if (vertex == null) {
      return null;
    } else {
      final BoundingBox boundingBox = vertex.getBoundingBox();
      return boundingBox.getBounds(2);
    }
  }

  @Override
  protected Object getId(final Vertex vertex) {
    return vertex.getVertexId();
  }

  @Override
  protected Vertex getItem(final Object id) {
    final int[] vertexId = (int[])id;
    return this.geometry.getVertex(vertexId);
  }
}
