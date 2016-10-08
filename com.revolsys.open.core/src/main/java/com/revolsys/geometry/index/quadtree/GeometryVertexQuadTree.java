package com.revolsys.geometry.index.quadtree;

import com.revolsys.collection.map.WeakKeyValueMap;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.util.Property;

public class GeometryVertexQuadTree extends IdObjectQuadTree<Vertex> {

  public static final String GEOMETRY_VERTEX_INDEX = "_GeometryVertexQuadTree";

  private static final long serialVersionUID = 1L;

  private static final WeakKeyValueMap<Geometry, GeometryVertexQuadTree> CACHE = new WeakKeyValueMap<>();

  public static GeometryVertexQuadTree get(final Geometry geometry) {
    if (Property.hasValue(geometry)) {
      GeometryVertexQuadTree index = CACHE.get(geometry);
      if (index == null) {
        index = new GeometryVertexQuadTree(geometry);
        CACHE.put(geometry, index);
      }
      return index;
    } else {
      return null;
    }
  }

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
    if (vertex == null) {
      return null;
    } else {
      final BoundingBox boundingBox = vertex.getBoundingBox();
      return boundingBox.getMinMaxValues(2);
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
