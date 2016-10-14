package com.revolsys.elevation.tin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.revolsys.geometry.index.SpatialIndex;
import com.revolsys.geometry.index.quadtree.QuadTree;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Triangle;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;
import com.revolsys.geometry.util.BoundingBoxUtil;
import com.revolsys.math.Angle;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.MathUtil;

public class SimpleTriangulatedIrregularNetworkBuilder
  extends BaseCompactTriangulatedIrregularNetwork {

  private double[] vertexCoordinates = new double[768];

  private int vertexCount = 4;

  private final double[] bounds = BoundingBoxUtil.newBounds(2);

  private double[] triangleCircumcircles = new double[768];

  private Resource resource;

  public SimpleTriangulatedIrregularNetworkBuilder(final GeometryFactory geometryFactory) {
    super(geometryFactory, 0, new int[768]);
  }

  private void addCircumCircle(final SpatialIndex<Integer> circumcircleIndex,
    final int triangleIndex) {
    final BoundingBox boundingBox = getCircumCircleBoundingBox(triangleIndex);
    circumcircleIndex.insertItem(boundingBox, triangleIndex);
  }

  public BoundingBox getBoundingBox() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.newBoundingBox(2, this.bounds);
  }

  private BoundingBox getCircumCircleBoundingBox(final int triangleIndex) {
    final int triangleOffset = triangleIndex * 3;
    final double centreX = this.triangleCircumcircles[triangleOffset];
    final double centreY = this.triangleCircumcircles[triangleOffset + 1];

    final double radius = this.triangleCircumcircles[triangleOffset + 2];
    final double minX = centreX - radius;
    final double minY = centreY - radius;
    final double maxX = centreX + radius;
    final double maxY = centreY + radius;
    return new BoundingBoxDoubleXY(minX, minY, maxX, maxY);
  }

  public Resource getResource() {
    return this.resource;
  }

  @Override
  public Point getVertex(final int vertexIndex) {
    final int offset = vertexIndex * 3;
    final double x = this.vertexCoordinates[offset];
    final double y = this.vertexCoordinates[offset + 1];
    final double z = this.vertexCoordinates[offset + 2];
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.point(x, y, z);
  }

  @Override
  protected double getVertexCoordinate(final int vertexIndex, final int axisIndex) {
    final int offset = vertexIndex * 3;
    return this.vertexCoordinates[offset + axisIndex];
  }

  @Override
  public int getVertexCount() {
    return this.vertexCount;
  }

  public void insertVertex(final double x, final double y, final double z) {
    final int vertexCount = this.vertexCount;
    final int offset = vertexCount * 3;
    double[] vertexCoordinates = this.vertexCoordinates;
    if (vertexCoordinates.length <= offset) {
      final int newLength = (vertexCount + (vertexCount >>> 1)) * 3;
      final double[] newVertexCoordinates = new double[newLength];
      System.arraycopy(vertexCoordinates, 0, newVertexCoordinates, 0, offset);
      vertexCoordinates = newVertexCoordinates;
      this.vertexCoordinates = newVertexCoordinates;
    }
    vertexCoordinates[offset] = x;
    vertexCoordinates[offset + 1] = y;
    vertexCoordinates[offset + 2] = z;
    BoundingBoxUtil.expandX(this.bounds, 2, x);
    BoundingBoxUtil.expandY(this.bounds, 2, y);
    this.vertexCount++;
  }

  public void insertVertex(final Point point) {
    final double x = point.getX();
    final double y = point.getY();
    final double z = point.getZ();
    insertVertex(x, y, z);
  }

  public void insertVertices(final Iterable<? extends Point> points) {
    for (final Point point : points) {
      insertVertex(point);
    }
  }

  public void insertVertices(final LineString line) {
    for (final Point point : line.vertices()) {
      insertVertex(point);
    }
  }

  public TriangulatedIrregularNetwork newTriangulatedIrregularNetwork() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final double minX = this.bounds[0];
    final double minY = this.bounds[1];

    final double maxX = this.bounds[2];
    final double maxY = this.bounds[3];

    this.vertexCoordinates[0] = minX - 1;
    this.vertexCoordinates[1] = minY - 1;
    this.vertexCoordinates[2] = 0;
    this.vertexCoordinates[3] = maxX + 1;
    this.vertexCoordinates[4] = minY - 1;
    this.vertexCoordinates[5] = 0;
    this.vertexCoordinates[6] = maxX + 1;
    this.vertexCoordinates[7] = maxY + 1;
    this.vertexCoordinates[8] = 0;
    this.vertexCoordinates[9] = minX - 1;
    this.vertexCoordinates[10] = maxY + 1;
    this.vertexCoordinates[11] = 0;

    QuadTree<Integer> circumcircleTriangleIndex = new QuadTree<>();
    circumcircleTriangleIndex.setUseEquals(true);
    setTriangleCapacity(this.vertexCount);
    final int triangleIndex1 = appendTriangleVertexIndices(0, 3, 2);
    addCircumCircle(circumcircleTriangleIndex, triangleIndex1);
    final int triangleIndex2 = appendTriangleVertexIndices(0, 2, 1);
    addCircumCircle(circumcircleTriangleIndex, triangleIndex2);
    final int vertexCount = getVertexCount();

    final int log10 = (int)Math.log10(vertexCount);
    int previousStep = (int)Math.pow(10, log10 + 1);
    for (int step = previousStep / 10; step > 0; step /= 10) {
      for (int vertexIndex = 4; vertexIndex < vertexCount; vertexIndex += step) {
        if (vertexIndex % previousStep == 0) {
          if (vertexIndex % 10000 == 0) {
            System.out.println(step + " " + vertexIndex);
          }
        } else {
          triangulateVertex(circumcircleTriangleIndex, vertexIndex);
        }
      }
      previousStep = step;
    }

    circumcircleTriangleIndex = null;

    final int triangleCount = getTriangleCount();

    final int[] triangleVertexIndices = getTriangleVertexIndices();
    return new CompactTriangulatedIrregularNetwork(geometryFactory, this.vertexCount,
      this.vertexCoordinates, triangleCount, triangleVertexIndices);
  }

  @Override
  protected void setTriangleCapacity(final int triangleCapacity) {
    super.setTriangleCapacity(triangleCapacity);
    final int newLength = triangleCapacity * 3;
    final int oldLength = this.triangleCircumcircles.length;
    if (oldLength < newLength) {
      final double[] newTriangleCircumcircles = new double[newLength];
      System.arraycopy(this.triangleCircumcircles, 0, newTriangleCircumcircles, 0, oldLength);
      this.triangleCircumcircles = newTriangleCircumcircles;
    }
  }

  @Override
  protected void setTriangleVertexIndices(final int triangleIndex, final int vertexIndex1,
    final int vertexIndex2, final int vertexIndex3) {
    super.setTriangleVertexIndices(triangleIndex, vertexIndex1, vertexIndex2, vertexIndex3);
    final int offset1 = vertexIndex1 * 3;
    final double x1 = this.vertexCoordinates[offset1];
    final double y1 = this.vertexCoordinates[offset1 + 1];
    final int offset2 = vertexIndex2 * 3;
    final double x2 = this.vertexCoordinates[offset2];
    final double y2 = this.vertexCoordinates[offset2 + 1];
    final int offset3 = vertexIndex3 * 3;
    final double x3 = this.vertexCoordinates[offset3];
    final double y3 = this.vertexCoordinates[offset3 + 1];

    double centreX;
    double centreY;
    double radius;
    try {
      final double[] centre = Triangle.getCircumcentreCoordinates(x1, y1, x2, y2, x3, y3);
      centreX = centre[0];
      centreY = centre[1];
      radius = Triangle.getCircumcircleRadius(centreX, centreY, x3, y3);
    } catch (final Throwable e) {
      final double[] bounds = BoundingBoxUtil.newBounds(2);
      BoundingBoxUtil.expand(bounds, 2, x1, y1);
      BoundingBoxUtil.expand(bounds, 2, x2, y2);
      BoundingBoxUtil.expand(bounds, 2, x3, y3);
      final double widthDiv2 = (bounds[2] - bounds[0]) / 2;
      final double heightDiv2 = (bounds[2] - bounds[0]) / 2;
      centreX = bounds[0] = widthDiv2;
      centreY = bounds[2] = heightDiv2;
      radius = Math.max(widthDiv2, heightDiv2);
    }
    final int triangleOffset = triangleIndex * 3;
    this.triangleCircumcircles[triangleOffset] = centreX;
    this.triangleCircumcircles[triangleOffset + 1] = centreY;
    this.triangleCircumcircles[triangleOffset + 2] = radius;
  }

  private void triangulateVertex(final SpatialIndex<Integer> circumcircleIndex, int vertexIndex) {
    final int offset = vertexIndex * 3;
    final double x = this.vertexCoordinates[offset];
    final double y = this.vertexCoordinates[offset + 1];

    final List<Integer> triangleIndices = circumcircleIndex.getItems(x, y, (triangleIndex) -> {
      final int triangleOffset = triangleIndex * 3;
      final double centreX = this.triangleCircumcircles[triangleOffset];
      final double centreY = this.triangleCircumcircles[triangleOffset + 1];

      final double radius = this.triangleCircumcircles[triangleOffset + 2];

      final double distanceFromCentre = MathUtil.distance(centreX, centreY, x, y);
      return distanceFromCentre < radius + 0.0001;
    });
    if (!triangleIndices.isEmpty()) {
      final List<Integer> exteriorVertexIndices = new ArrayList<>();
      for (final Integer triangleIndex : triangleIndices) {
        final BoundingBox envelope = getCircumCircleBoundingBox(triangleIndex);
        circumcircleIndex.removeItem(envelope, triangleIndex);
        for (int i = 0; i < 3; i++) {
          final int cornerVertexIndex = getTriangleVertexIndex(triangleIndex, i);
          if (cornerVertexIndex != vertexIndex) {
            boolean useVertex = true;
            final int cornerVertexOffset = cornerVertexIndex * 3;
            final double cornerVertexX = this.vertexCoordinates[cornerVertexOffset];
            if (cornerVertexX == x) {
              final double cornerVertexY = this.vertexCoordinates[cornerVertexOffset + 1];
              if (cornerVertexY == y) {
                final double cornerVertexZ = this.vertexCoordinates[cornerVertexOffset + 2];
                useVertex = false;
                if (!Double.isNaN(cornerVertexZ)) {
                  vertexIndex = cornerVertexIndex;
                }
              }
            }
            if (useVertex && !exteriorVertexIndices.contains(cornerVertexIndex)) {
              exteriorVertexIndices.add(cornerVertexIndex);
            }
          }
        }
      }
      if (!exteriorVertexIndices.isEmpty()) {
        final Comparator<Integer> comparator = (vertexIndex1, vertexIndex2) -> {
          final double x1 = getVertexCoordinate(vertexIndex1, 0);
          final double y1 = getVertexCoordinate(vertexIndex1, 1);
          final double x2 = getVertexCoordinate(vertexIndex2, 0);
          final double y2 = getVertexCoordinate(vertexIndex2, 1);
          final double angleC1 = Angle.angle2dClockwise(x, y, x1, y1);
          final double angleC2 = Angle.angle2dClockwise(x, y, x2, y2);
          final int compare = Double.compare(angleC1, angleC2);
          return compare;
        };

        exteriorVertexIndices.sort(comparator);
        int previousVertexIndex = exteriorVertexIndices.get(exteriorVertexIndices.size() - 1);
        for (final Integer currentVertexIndex : exteriorVertexIndices) {
          int triangleIndex;
          if (triangleIndices.isEmpty()) {
            triangleIndex = appendTriangleVertexIndices(vertexIndex, previousVertexIndex,
              currentVertexIndex);
          } else {
            triangleIndex = triangleIndices.remove(triangleIndices.size() - 1);
            setTriangleVertexIndices(triangleIndex, vertexIndex, previousVertexIndex,
              currentVertexIndex);
          }
          addCircumCircle(circumcircleIndex, triangleIndex);

          previousVertexIndex = currentVertexIndex;
        }
      }
    }

  }
}
