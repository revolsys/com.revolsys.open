package com.revolsys.elevation.tin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.revolsys.geometry.index.quadtree.IdObjectQuadTree;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Triangle;
import com.revolsys.geometry.util.BoundingBoxUtil;
import com.revolsys.math.Angle;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.MathUtil;

public class SimpleTriangulatedIrregularNetworkBuilder
  extends BaseCompactTriangulatedIrregularNetwork {

  private final double[] bounds = BoundingBoxUtil.newBounds(2);

  private double[] triangleCircumcentreXCoordinates = new double[1024];

  private double[] triangleCircumcentreYCoordinates = new double[1024];

  private double[] triangleCircumcircleRadiuses = new double[1024];

  private Resource resource;

  private IdObjectQuadTree<Integer> triangleCircumCircleIndex;

  public SimpleTriangulatedIrregularNetworkBuilder(final GeometryFactory geometryFactory) {
    super(geometryFactory, 4, new double[1024], new double[1024], new double[1024], 0,
      new int[1024], new int[1024], new int[1024]);
  }

  public BoundingBox getBoundingBox() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.newBoundingBox(2, this.bounds);
  }

  public Resource getResource() {
    return this.resource;
  }

  public void insertVertex(final double x, final double y, final double z) {
    final int vertexIndex = this.vertexCount;
    if (this.vertexXCoordinates.length < vertexIndex + 1) {
      this.vertexXCoordinates = increaseSize(this.vertexXCoordinates);
      this.vertexYCoordinates = increaseSize(this.vertexYCoordinates);
      this.vertexZCoordinates = increaseSize(this.vertexZCoordinates);
    }
    this.vertexXCoordinates[this.vertexCount] = x;
    this.vertexYCoordinates[this.vertexCount] = y;
    this.vertexZCoordinates[this.vertexCount] = z;
    this.vertexCount++;
    BoundingBoxUtil.expandX(this.bounds, 2, x);
    BoundingBoxUtil.expandY(this.bounds, 2, y);
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
    final int vertexCount = line.getVertexCount();
    for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
      final double x = line.getX(vertexIndex);
      final double y = line.getY(vertexIndex);
      final double z = line.getZ(vertexIndex);
      insertVertex(x, y, z);
    }
  }

  public TriangulatedIrregularNetwork newTriangulatedIrregularNetwork() {
    return newTriangulatedIrregularNetwork(this.vertexCount);
  }

  public TriangulatedIrregularNetwork newTriangulatedIrregularNetwork(final int maxPoints) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final double minX = this.bounds[0];
    final double minY = this.bounds[1];

    final double maxX = this.bounds[2];
    final double maxY = this.bounds[3];

    this.vertexXCoordinates[0] = minX - 1;
    this.vertexYCoordinates[0] = minY - 1;
    this.vertexZCoordinates[0] = Double.NaN;

    this.vertexXCoordinates[1] = maxX + 1;
    this.vertexYCoordinates[1] = minY - 1;
    this.vertexZCoordinates[1] = Double.NaN;

    this.vertexXCoordinates[2] = maxX + 1;
    this.vertexYCoordinates[2] = maxY + 1;
    this.vertexZCoordinates[2] = Double.NaN;

    this.vertexXCoordinates[3] = minX - 1;
    this.vertexYCoordinates[3] = maxY + 1;
    this.vertexZCoordinates[3] = Double.NaN;

    this.triangleCircumCircleIndex = new IdObjectQuadTree<Integer>(geometryFactory) {
      @Override
      protected boolean intersectsBounds(final Object id, final double x, final double y) {
        final int triangleIndex = (Integer)id;
        final double centreX = SimpleTriangulatedIrregularNetworkBuilder.this.triangleCircumcentreXCoordinates[triangleIndex];
        final double centreY = SimpleTriangulatedIrregularNetworkBuilder.this.triangleCircumcentreYCoordinates[triangleIndex];
        final double radius = SimpleTriangulatedIrregularNetworkBuilder.this.triangleCircumcircleRadiuses[triangleIndex];

        final double distanceFromCentre = MathUtil.distance(centreX, centreY, x, y);
        return distanceFromCentre < radius + 0.0001;

        // final double minX = centreX - radius;
        // final double minY = centreY - radius;
        // final double maxX = centreX + radius;
        // final double maxY = centreY + radius;
        //
        // return BoundingBoxUtil.intersects(minX, minY, maxX, maxY, x, y);
      }

      @Override
      protected boolean intersectsBounds(final Object id, final double minX, final double minY,
        final double maxX, final double maxY) {
        final Integer triangleIndex = (Integer)id;
        final double centreX = SimpleTriangulatedIrregularNetworkBuilder.this.triangleCircumcentreXCoordinates[triangleIndex];
        final double centreY = SimpleTriangulatedIrregularNetworkBuilder.this.triangleCircumcentreYCoordinates[triangleIndex];
        final double radius = SimpleTriangulatedIrregularNetworkBuilder.this.triangleCircumcircleRadiuses[triangleIndex];

        final double minX2 = centreX - radius;
        final double minY2 = centreY - radius;
        final double maxX2 = centreX + radius;
        final double maxY2 = centreY + radius;

        return BoundingBoxUtil.intersects(minX, minY, maxX, maxY, minX2, minY2, maxX2, maxY2);
      }
    };
    this.triangleCircumCircleIndex.setUseEquals(true);
    setTriangleCapacity(this.vertexCount * 2);
    appendTriangleVertexIndices(0, 3, 2);
    appendTriangleVertexIndices(0, 2, 1);

    triangulateVertices(maxPoints);

    this.triangleCircumCircleIndex = null;

    final int triangleCount = getTriangleCount();

    final int[] triangle0VertexIndices = getTriangleVertex0Indices();
    final int[] triangle1VertexIndices = getTriangleVertex1Indices();
    final int[] triangle2VertexIndices = getTriangleVertex2Indices();
    return new CompactTriangulatedIrregularNetwork(geometryFactory, this.vertexCount,
      this.vertexXCoordinates, this.vertexYCoordinates, this.vertexZCoordinates, triangleCount,
      triangle0VertexIndices, triangle1VertexIndices, triangle2VertexIndices);
  }

  @Override
  protected void setTriangleCapacity(final int triangleCapacity) {
    super.setTriangleCapacity(triangleCapacity);
    if (this.triangleCircumcentreXCoordinates.length < triangleCapacity) {
      this.triangleCircumcentreXCoordinates = increaseSize(this.triangleCircumcentreXCoordinates,
        triangleCapacity);
      this.triangleCircumcentreYCoordinates = increaseSize(this.triangleCircumcentreYCoordinates,
        triangleCapacity);
      this.triangleCircumcircleRadiuses = increaseSize(this.triangleCircumcircleRadiuses,
        triangleCapacity);
    }
  }

  @Override
  protected void setTriangleVertexIndices(final int triangleIndex, final int vertexIndex1,
    final int vertexIndex2, final int vertexIndex3) {
    super.setTriangleVertexIndices(triangleIndex, vertexIndex1, vertexIndex2, vertexIndex3);
    final double x1 = this.vertexXCoordinates[vertexIndex1];
    final double y1 = this.vertexYCoordinates[vertexIndex1];
    final double x2 = this.vertexXCoordinates[vertexIndex2];
    final double y2 = this.vertexYCoordinates[vertexIndex2];
    final double x3 = this.vertexXCoordinates[vertexIndex3];
    final double y3 = this.vertexYCoordinates[vertexIndex3];

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
      final double heightDiv2 = (bounds[3] - bounds[1]) / 2;
      centreX = bounds[0] + widthDiv2;
      centreY = bounds[2] + heightDiv2;
      radius = Math.max(widthDiv2, heightDiv2);
    }
    this.triangleCircumcentreXCoordinates[triangleIndex] = centreX;
    this.triangleCircumcentreYCoordinates[triangleIndex] = centreY;
    this.triangleCircumcircleRadiuses[triangleIndex] = radius;
    final double minX = this.geometryFactory.makeXyPreciseFloor(centreX - radius);
    final double minY = this.geometryFactory.makeXyPreciseFloor(centreY - radius);
    final double maxX = this.geometryFactory.makeXyPreciseCeil(centreX + radius);
    final double maxY = this.geometryFactory.makeXyPreciseCeil(centreY + radius);
    this.triangleCircumCircleIndex.insertItem(minX, minY, maxX, maxY, triangleIndex);
  }

  private void triangulateVertex(int vertexIndex) {
    final double x = this.vertexXCoordinates[vertexIndex];
    final double y = this.vertexYCoordinates[vertexIndex];

    final List<Integer> triangleIndices = this.triangleCircumCircleIndex.getItems(x, y);
    if (!triangleIndices.isEmpty()) {
      final List<Integer> exteriorVertexIndices = new ArrayList<>();
      for (final Integer triangleIndex : triangleIndices) {
        final double centreX = this.triangleCircumcentreXCoordinates[triangleIndex];
        final double centreY = this.triangleCircumcentreYCoordinates[triangleIndex];
        final double radius = this.triangleCircumcircleRadiuses[triangleIndex];
        final double minX = this.geometryFactory.makeXyPreciseFloor(centreX - radius);
        final double minY = this.geometryFactory.makeXyPreciseFloor(centreY - radius);
        final double maxX = this.geometryFactory.makeXyPreciseCeil(centreX + radius);
        final double maxY = this.geometryFactory.makeXyPreciseCeil(centreY + radius);
        this.triangleCircumCircleIndex.removeItem(minX, minY, maxX, maxY, triangleIndex);
        for (int i = 0; i < 3; i++) {
          final int cornerVertexIndex = getTriangleVertexIndex(triangleIndex, i);
          if (cornerVertexIndex != vertexIndex) {
            boolean useVertex = true;
            final double cornerVertexX = this.vertexXCoordinates[cornerVertexIndex];
            if (cornerVertexX == x) {
              final double cornerVertexY = this.vertexYCoordinates[cornerVertexIndex];
              if (cornerVertexY == y) {
                final double cornerVertexZ = this.vertexZCoordinates[cornerVertexIndex];
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

          previousVertexIndex = currentVertexIndex;
        }
      }
    }

  }

  private void triangulateVertices(final int maxPoints) {
    if (maxPoints > 0) {
      final int vertexCount = getVertexCount();
      final int log10 = (int)Math.log10(vertexCount);
      int previousStep = (int)Math.pow(10, log10 + 1);
      int i = 0;
      for (int step = previousStep / 10; step > 0; step /= 10) {
        System.out.println(step);
        for (int vertexIndex = 4; vertexIndex < vertexCount; vertexIndex += step) {
          if (vertexIndex % previousStep == 0) {
            if (vertexIndex % 10000 == 0) {
              System.out.println(vertexIndex);
            }
          } else {
            triangulateVertex(vertexIndex);
          }
          i++;
          if (i > maxPoints) {
            break;
          }
        }
        previousStep = step;
      }
    }
  }
}
