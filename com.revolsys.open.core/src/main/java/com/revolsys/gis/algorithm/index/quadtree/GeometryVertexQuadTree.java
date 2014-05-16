package com.revolsys.gis.algorithm.index.quadtree;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.vertex.Vertex;

public class GeometryVertexQuadTree extends IdObjectQuadTree<Vertex> {

  private final Geometry geometry;

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
    final BoundingBox boundingBox = vertex.getBoundingBox();
    return boundingBox.getBounds(2);
  }

  @Override
  protected Object getId(final Vertex vertex) {
    return vertex.getVertexId();
  }

  @Override
  protected Vertex getItem(final Object id) {
    final int[] vertexId = (int[])id;
    return geometry.getVertex(vertexId);
  }
}
