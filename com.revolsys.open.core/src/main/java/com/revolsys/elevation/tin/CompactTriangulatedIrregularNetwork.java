package com.revolsys.elevation.tin;

import java.util.function.Consumer;

import com.revolsys.geometry.index.rtree.RTree;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Triangle;
import com.revolsys.geometry.model.impl.LineStringDoubleGf;
import com.revolsys.geometry.util.BoundingBoxUtil;
import com.revolsys.spring.resource.Resource;

public class CompactTriangulatedIrregularNetwork extends BaseCompactTriangulatedIrregularNetwork
  implements TriangulatedIrregularNetwork {

  private final LineString vertexCoordinates;

  private final int vertexCount;

  private final RTree<Integer> triangleSpatialIndex = new RTree<>();

  private final BoundingBox boundingBox;

  public CompactTriangulatedIrregularNetwork(final GeometryFactory geometryFactory,
    final double[] vertexCoordinates, final int[] triangleVertexIndices) {
    this(geometryFactory, vertexCoordinates.length / 3, vertexCoordinates,
      triangleVertexIndices.length / 3, triangleVertexIndices);
  }

  public CompactTriangulatedIrregularNetwork(final GeometryFactory geometryFactory,
    final int vertexCount, final double[] vertexCoordinates, final int triangleCount,
    final int[] triangleVertexIndices) {
    super(geometryFactory, triangleCount, triangleVertexIndices);
    this.vertexCount = vertexCount;
    this.vertexCoordinates = LineStringDoubleGf.newLineStringDoubleGf(getGeometryFactory(), 3,
      vertexCoordinates);

    final double[] bounds = BoundingBoxUtil.newBounds(2);
    for (int triangleIndex = 0; triangleIndex < triangleCount; triangleIndex++) {
      final BoundingBox triangleBoundingBox = newTriangleBoundingBox(triangleIndex);
      this.triangleSpatialIndex.insertItem(triangleBoundingBox, triangleIndex);
      BoundingBoxUtil.expand(bounds, 2, triangleBoundingBox);
    }
    this.boundingBox = geometryFactory.newBoundingBox(2, bounds);
  }

  @Override
  public void forEachTriangle(final BoundingBox boundingBox,
    final Consumer<? super Triangle> action) {
    this.triangleSpatialIndex.forEach((triangleIndex) -> {
      final Triangle triangle = newTriangle(triangleIndex);
      if (triangle != null) {
        action.accept(triangle);
      }
    });
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  @Override
  public Resource getResource() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Point getVertex(final int vertexIndex) {
    return this.vertexCoordinates.getVertex(vertexIndex);
  }

  @Override
  protected double getVertexCoordinate(final int vertexIndex, final int axisIndex) {
    return this.vertexCoordinates.getCoordinate(vertexIndex, axisIndex);
  }

  @Override
  public int getVertexCount() {
    return this.vertexCount;
  }
}
