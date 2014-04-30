package com.revolsys.jts.geom.vertex;

import java.util.NoSuchElementException;

import com.revolsys.jts.geom.Geometry;

public class GeometryCollectionVertex extends AbstractVertex {

  private int partIndex = -1;

  private Vertex vertex;

  public GeometryCollectionVertex(final Geometry geometry,
    final int... vertexId) {
    super(geometry);
    setVertexId(vertexId);
  }

  public Geometry getGeometryCollection() {
    return getGeometry();
  }

  @Override
  public int getPartIndex() {
    return super.getPartIndex();
  }

  @Override
  public double getValue(final int axisIndex) {
    if (vertex == null) {
      return Double.NaN;
    } else {
      return vertex.getValue(axisIndex);
    }
  }

  @Override
  public int[] getVertexId() {
    if (partIndex < 0) {
      return new int[] {
        -1
      };
    } else if (vertex == null) {
      return new int[] {
        partIndex
      };
    } else {
      final int[] partVertexId = vertex.getVertexId();
      final int[] vertexId = new int[partVertexId.length + 1];
      vertexId[0] = partIndex;
      System.arraycopy(partVertexId, 0, vertexId, 1, partVertexId.length);
      return vertexId;
    }
  }

  @Override
  public boolean hasNext() {
    if (this.partIndex == -2) {
      return false;
    } else {
      final Geometry geometryCollection = getGeometryCollection();
      int partIndex = this.partIndex;
      Vertex vertex = this.vertex;
      if (vertex != null && !vertex.hasNext()) {
        partIndex++;
        vertex = null;
      }
      while (vertex == null
        && partIndex < geometryCollection.getGeometryCount()) {
        if (partIndex >= 0) {
          final Geometry part = geometryCollection.getGeometry(partIndex);
          if (part != null) {
            vertex = (Vertex)part.vertices().iterator();
            if (vertex.hasNext()) {
              return true;
            } else {
              vertex = null;
            }
          }
        }
        if (partIndex > -2) {
          partIndex++;
        }
      }
      if (vertex == null) {
        return false;
      } else {
        return vertex.hasNext();
      }
    }
  }

  @Override
  public Vertex next() {
    if (this.partIndex == -2) {
      throw new NoSuchElementException();
    } else {
      final Geometry geometryCollection = getGeometryCollection();
      if (vertex != null && !vertex.hasNext()) {
        partIndex++;
        vertex = null;
      }
      while (vertex == null
        && partIndex < geometryCollection.getGeometryCount()) {
        if (partIndex >= 0) {
          final Geometry part = geometryCollection.getGeometry(partIndex);
          if (part != null) {
            vertex = (Vertex)part.vertices().iterator();
            if (vertex.hasNext()) {
              return vertex.next();
            } else {
              vertex = null;
            }
          }
        }
        if (partIndex > -2) {
          this.partIndex++;
        }
      }
      if (vertex != null && vertex.hasNext()) {
        return vertex.next();
      } else {
        throw new NoSuchElementException();
      }
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Removing vertices not supported");
  }

  public void setVertexId(final int[] vertexId) {
    this.vertex = null;
    if (vertexId.length > 0) {
      this.partIndex = vertexId[0];
      final Geometry geometryCollection = getGeometryCollection();
      if (partIndex >= 0 && partIndex < geometryCollection.getGeometryCount()) {
        final Geometry part = geometryCollection.getGeometry(partIndex);
        if (part != null) {
          final int[] partVertexId = new int[vertexId.length - 1];
          System.arraycopy(vertexId, 1, partVertexId, 0, partVertexId.length);
          this.vertex = part.getVertex(partVertexId);
        }
      }
    } else {
      this.partIndex = -2;
    }
  }
}
