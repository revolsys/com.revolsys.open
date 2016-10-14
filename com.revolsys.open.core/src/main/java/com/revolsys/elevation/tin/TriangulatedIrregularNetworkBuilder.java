package com.revolsys.elevation.tin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.collection.list.Lists;
import com.revolsys.geometry.algorithm.CGAlgorithms;
import com.revolsys.geometry.index.SpatialIndex;
import com.revolsys.geometry.index.rtree.RTree;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.Triangle;
import com.revolsys.geometry.model.coordinates.CoordinatesUtil;
import com.revolsys.geometry.model.coordinates.comparator.AngleFromPointComparator;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.LineSegmentDoubleGF;
import com.revolsys.math.Angle;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Debug;

public class TriangulatedIrregularNetworkBuilder implements TriangulatedIrregularNetwork {
  private static final int[] OPPOSITE_INDEXES = {
    2, 1, 0
  };

  private final BoundingBox boundingBox;

  private RTree<Triangle> circumCircleIndex;

  private GeometryFactory geometryFactory;

  private final Set<Point> nodes = new HashSet<>();

  private RTree<Triangle> triangleIndex;

  private Resource resource;

  public TriangulatedIrregularNetworkBuilder(final BoundingBox boundingBox) {
    this(boundingBox.getGeometryFactory(), boundingBox);
  }

  public TriangulatedIrregularNetworkBuilder(final BoundingBox boundingBox,
    final boolean loadMode) {
    this(boundingBox.getGeometryFactory(), boundingBox, loadMode);
  }

  public TriangulatedIrregularNetworkBuilder(final GeometryFactory geometryFactory) {
    this(geometryFactory.getCoordinateSystem().getAreaBoundingBox());
  }

  public TriangulatedIrregularNetworkBuilder(final GeometryFactory geometryFactory,
    final BoundingBox boundingBox) {
    this(geometryFactory, boundingBox, false);
  }

  public TriangulatedIrregularNetworkBuilder(final GeometryFactory geometryFactory,
    final BoundingBox boundingBox, final boolean loadMode) {
    this.boundingBox = boundingBox;
    setGeometryFactory(geometryFactory);
    if (loadMode) {
      this.triangleIndex = new RTree<>();
    } else {
      this.circumCircleIndex = new RTree<>();
      final double minX = geometryFactory.makeXyPrecise(boundingBox.getMinX());
      final double minY = geometryFactory.makeXyPrecise(boundingBox.getMinY());
      final double maxX = geometryFactory.makeXyPrecise(boundingBox.getMaxX());
      final double maxY = geometryFactory.makeXyPrecise(boundingBox.getMaxY());
      final Triangle triangle1 = TriangleWithCircumcircle.newClockwiseTriangle(minX, minY, maxX,
        minY, maxX, maxY);
      addTriangle(triangle1);
      final Triangle triangle2 = TriangleWithCircumcircle.newClockwiseTriangle(minX, minY, maxX,
        maxY, minX, maxY);
      addTriangle(triangle2);
    }
  }

  private void addBreaklineIntersect(final Triangle triangle, final LineSegment intersectLine) {
    final Point lc0 = intersectLine.getPoint(0);
    final Point lc1 = intersectLine.getPoint(1);
    if (intersectLine.getLength() > 0) {
      double startCornerDistance = Double.MAX_VALUE;
      double startEdgeDistance = Double.MAX_VALUE;
      double endEdgeDistance = Double.MAX_VALUE;
      double endCornerDistance = Double.MAX_VALUE;
      int startClosestCorner = -1;
      int endClosestCorner = -1;
      int startClosestEdge = -1;
      int endClosestEdge = -1;
      for (int i = 0; i < 3; i++) {
        final Point corner = triangle.getPoint(i);
        final Point nextCorner = triangle.getPoint((i + 1) % 3);

        final double startCorner = corner.distance(lc0);
        if (startClosestCorner == -1 || startCorner < startCornerDistance) {
          startClosestCorner = i;
          startCornerDistance = startCorner;
        }

        final double endCorner = corner.distance(lc1);
        if (endClosestCorner == -1 || endCorner < endCornerDistance) {
          endClosestCorner = i;
          endCornerDistance = endCorner;
        }

        final LineSegment edge = new LineSegmentDoubleGF(corner, nextCorner);
        final double startEdge = edge.distance(lc0);
        if (startClosestEdge == -1 || startEdge < startEdgeDistance) {
          startClosestEdge = i;
          startEdgeDistance = startEdge;
        }

        final double endEdge = edge.distance(lc1);
        if (endClosestEdge == -1 || endEdge < endEdgeDistance) {
          endClosestEdge = i;
          endEdgeDistance = endEdge;
        }
      }
      // Start of algorithm

      if (startCornerDistance < 0.01) {
        // Touching Start corner
        if (endCornerDistance < 0.01) {
          // Touching two corners
          final Triangle newTriangle = TriangleWithCircumcircle.newClockwiseTriangle(lc0, lc1,
            getOtherCoordinates(triangle, startClosestCorner, endClosestCorner));
          replaceTriangle(triangle, newTriangle);
        } else {
          // Touching start corner
          final double edgeDistance = endEdgeDistance;
          addTriangleTouchingOneCorner(triangle, lc0, lc1, startClosestCorner, endClosestEdge,
            edgeDistance);
        }
      } else if (endCornerDistance < 0.01) {
        // Touching end corner
        final double edgeDistance = startEdgeDistance;
        addTriangleTouchingOneCorner(triangle, lc1, lc0, endClosestCorner, startClosestEdge,
          edgeDistance);
      } else if (startEdgeDistance < 0.01) {
        if (endEdgeDistance < 0.01) {
          addTriangleTouchingTwoEdges(triangle, lc0, lc1, startClosestEdge, endClosestEdge);
        } else {
          addTriangleTouchingOneEdge(triangle, lc0, lc1, startClosestEdge);
        }
      } else if (endEdgeDistance < 0.01) {
        addTriangleTouchingOneEdge(triangle, lc1, lc0, endClosestEdge);

      } else {
        if (startCornerDistance <= endCornerDistance) {
          addContainedLine(triangle, startClosestCorner, lc0, lc1);
        } else {
          addContainedLine(triangle, endClosestCorner, lc1, lc0);
        }
      }
    }
  }

  private void addBreaklineIntersect(final Triangle triangle, final Point intersectCoord) {
    Point previousPoint = triangle.getPoint(0);
    for (int i = 1; i < 3; i++) {
      final Point triCorner = triangle.getPoint(i);
      if (!triCorner.equals(2, intersectCoord) && !previousPoint.equals(2, intersectCoord)) {
        final double distance = new LineSegmentDoubleGF(previousPoint, triCorner)
          .distance(intersectCoord);
        if (distance == 0) {
          final Point nextPoint = triangle.getPoint((i + 1) % 3);
          replaceTriangle(triangle,
            TriangleWithCircumcircle.newClockwiseTriangle(intersectCoord, triCorner, nextPoint),
            TriangleWithCircumcircle.newClockwiseTriangle(intersectCoord, nextPoint,
              previousPoint));
        }
      }
      previousPoint = triCorner;
    }
  }

  private void addBreaklineItersect(final Triangle triangle, final LineSegment breakline,
    final LineSegment intersectLine) {
    final Point lc0 = intersectLine.getPoint(0);
    final Point lc1 = intersectLine.getPoint(0);
    final double x1 = lc0.getX();
    final double y1 = lc0.getY();
    final double x2 = lc1.getX();
    final double y2 = lc1.getY();
    final double z1 = breakline.getElevation(lc0);
    final double z2 = breakline.getElevation(lc1);
    final LineSegment lineSegment = new LineSegmentDoubleGF(this.geometryFactory, 3, x1, y1, z1, x2,
      y2, z2);
    addBreaklineIntersect(triangle, lineSegment);
  }

  /**
   * Split a triangle where the line segment i0 -> i1 is fully contained in the
   * triangle. Creates 3 new triangles.
   *
   * @param triangle
   * @param points The points of the triangle.
   * @param index The index of the closest corner to i0.
   * @param l0 The start point of the line.
   * @param l1 The end point of the line.
   */
  private void addContainedLine(final Triangle triangle, final int index, final Point l0,
    final Point l1) {
    final Point t0 = triangle.getPoint(index);
    final Point t1 = triangle.getPoint((index + 1) % 3);
    final Point t2 = triangle.getPoint((index + 2) % 3);

    final int c0i0i1Orientation = CoordinatesUtil.orientationIndex(t0, l0, l1);
    if (c0i0i1Orientation == CGAlgorithms.COLLINEAR) {
      addTrianglesContained(triangle, t0, t1, t2, l0, l1);

    } else if (c0i0i1Orientation == CGAlgorithms.CLOCKWISE) {
      final double angleCornerLine = Angle.angleBetween(t0, l0, l1);
      final double angleCornerLineCorner = Angle.angleBetween(t0, l0, t2);
      if (angleCornerLine > angleCornerLineCorner) {
        addTrianglesContained(triangle, t0, t1, t2, l0, l1);
      } else if (angleCornerLine == angleCornerLineCorner) {
        addTrianglesContained(triangle, t2, t0, t1, l1, l0);
      } else {
        addTrianglesContained(triangle, t1, t2, t0, l0, l1);
      }

    } else {
      final double angleCornerLine = Angle.angleBetween(t0, l0, l1);
      final double angleCornerLineCorner = Angle.angleBetween(t0, l0, t1);
      if (angleCornerLine > angleCornerLineCorner) {
        addTrianglesContained(triangle, t0, t1, t2, l0, l1);
      } else if (angleCornerLine == angleCornerLineCorner) {
        addTrianglesContained(triangle, t1, t2, t0, l1, l0);
      } else {
        addTrianglesContained(triangle, t2, t0, t1, l1, l0);
      }
    }
  }

  private void addTrangleCornerAndEdgeTouch(final Triangle triangle, final Point cPrevious,
    final Point c, final Point cNext, final Point cOpposite) {
    replaceTriangle(triangle,
      TriangleWithCircumcircle.newClockwiseTriangle(cPrevious, c, cOpposite),
      TriangleWithCircumcircle.newClockwiseTriangle(c, cNext, cOpposite));
  }

  public void addTriangle(final Triangle triangle) {
    for (int i = 0; i < 3; i++) {
      final Point point = triangle.getPoint(i);
      if (!this.nodes.contains(point)) {
        this.nodes.add(point);
      }
    }
    if (this.circumCircleIndex != null) {
      final BoundingBox boundingBox = triangle.getCircumcircleBoundingBox();
      this.circumCircleIndex.insertItem(boundingBox, triangle);
    }
    if (this.triangleIndex != null) {
      final BoundingBox envelope = triangle.getBoundingBox();
      this.triangleIndex.insertItem(envelope, triangle);
    }
  }

  private void addTriangleCorderEdge(final Triangle triangle, final Point lc0, final Point lc1,
    final int startCorner, final int startEdge) {
    final Point cNext = triangle.getPoint((startCorner + 1) % 3);
    final Point cPrevious = triangle.getPoint((startCorner + 2) % 3);
    if (startEdge == startCorner) {
      addTrangleCornerAndEdgeTouch(triangle, lc0, lc1, cNext, cPrevious);
    } else if (startEdge == (startCorner + 1) % 3) {
      addTrangleCornerAndEdgeTouch(triangle, cPrevious, lc1, cNext, lc0);
    } else {
      addTrangleCornerAndEdgeTouch(triangle, lc0, lc1, cPrevious, cNext);
    }
  }

  /**
   * Add the triangles where the line is fully contained in the triangle. There
   * will be 5 triangles created. The triangle point t0 will be part of two
   * triangles, the other two triangle points will be part of 3 triangles.
   * l1 must not be closer than l0 to t0.
   *
   * @param triangle TODO
   * @param t0 The first triangle point.
   * @param t1 The second triangle point.
   * @param t2 The third triangle point.
   * @param l0 The first line point.
   * @param l1 The second line point.
   */
  private void addTrianglesContained(final Triangle triangle, final Point t0, final Point t1,
    final Point t2, final Point l0, final Point l1) {
    replaceTriangle(triangle, TriangleWithCircumcircle.newClockwiseTriangle(t0, t1, l0),
      TriangleWithCircumcircle.newClockwiseTriangle(l0, t1, l1),
      TriangleWithCircumcircle.newClockwiseTriangle(l1, t1, t2),
      TriangleWithCircumcircle.newClockwiseTriangle(l0, l1, t2),
      TriangleWithCircumcircle.newClockwiseTriangle(t0, l0, t2));
  }

  private void addTriangleStartCornerEndInside(final Triangle triangle, final int cornerIndex,
    final Point cCorner, final Point cInside) {
    final Point cNext = triangle.getPoint((cornerIndex + 1) % 3);
    final Point cPrevious = triangle.getPoint((cornerIndex + 2) % 3);
    replaceTriangle(triangle,
      TriangleWithCircumcircle.newClockwiseTriangle(cCorner, cNext, cInside),
      TriangleWithCircumcircle.newClockwiseTriangle(cInside, cNext, cPrevious),
      TriangleWithCircumcircle.newClockwiseTriangle(cInside, cPrevious, cCorner));
  }

  private void addTriangleTouchingOneCorner(final Triangle triangle, final Point lc0,
    final Point lc1, final int startCorner, final int endEdge, final double endEdgeDistance) {
    if (endEdgeDistance < 1) {
      addTriangleCorderEdge(triangle, lc0, lc1, startCorner, endEdge);
    } else {
      addTriangleStartCornerEndInside(triangle, startCorner, lc0, lc1);
    }
  }

  private void addTriangleTouchingOneEdge(final Triangle triangle, final Point lc0, final Point lc1,
    final int edgeIndex) {
    final Point cPrevious = triangle.getPoint(edgeIndex % 3);
    final Point cNext = triangle.getPoint((edgeIndex + 1) % 3);
    final Point cOpposite = triangle.getPoint((edgeIndex + 2) % 3);
    if (CoordinatesUtil.orientationIndex(cPrevious, lc0, lc1) == CGAlgorithms.COLLINEAR) {
      replaceTriangle(triangle,
        TriangleWithCircumcircle.newClockwiseTriangle(cPrevious, lc0, cOpposite),
        TriangleWithCircumcircle.newClockwiseTriangle(cOpposite, lc0, lc1),
        TriangleWithCircumcircle.newClockwiseTriangle(cOpposite, lc1, cNext),
        TriangleWithCircumcircle.newClockwiseTriangle(lc0, lc1, cNext));
    } else {
      replaceTriangle(triangle, TriangleWithCircumcircle.newClockwiseTriangle(cPrevious, lc0, lc1),
        TriangleWithCircumcircle.newClockwiseTriangle(cNext, lc0, lc1),
        TriangleWithCircumcircle.newClockwiseTriangle(cNext, lc1, cOpposite),
        TriangleWithCircumcircle.newClockwiseTriangle(cPrevious, lc1, cOpposite));
    }
  }

  private void addTriangleTouchingTwoEdges(final Triangle triangle, final Point lc0,
    final Point lc1, final int startEdge, final int endEdge) {
    final Point cPrevious = triangle.getPoint(startEdge);
    final Point cNext = triangle.getPoint((startEdge + 1) % 3);
    final Point cOpposite = triangle.getPoint((startEdge + 2) % 3);
    if (startEdge == endEdge) {
      if (cPrevious.distance(lc0) < cPrevious.distance(lc1)) {
        replaceTriangle(triangle,
          TriangleWithCircumcircle.newClockwiseTriangle(cPrevious, lc0, cOpposite),
          TriangleWithCircumcircle.newClockwiseTriangle(lc0, lc1, cOpposite),
          TriangleWithCircumcircle.newClockwiseTriangle(lc1, cNext, cOpposite));
      } else {
        replaceTriangle(triangle,
          TriangleWithCircumcircle.newClockwiseTriangle(cPrevious, lc1, cOpposite),
          TriangleWithCircumcircle.newClockwiseTriangle(lc0, lc1, cOpposite),
          TriangleWithCircumcircle.newClockwiseTriangle(lc0, cNext, cOpposite));
      }
    } else if (endEdge == (startEdge + 1) % 3) {
      replaceTriangle(triangle,
        TriangleWithCircumcircle.newClockwiseTriangle(cPrevious, lc0, cOpposite),
        TriangleWithCircumcircle.newClockwiseTriangle(lc0, lc1, cOpposite),
        TriangleWithCircumcircle.newClockwiseTriangle(lc0, cNext, lc1));
    } else {
      replaceTriangle(triangle, TriangleWithCircumcircle.newClockwiseTriangle(cPrevious, lc0, lc1),
        TriangleWithCircumcircle.newClockwiseTriangle(lc0, cNext, lc1),
        TriangleWithCircumcircle.newClockwiseTriangle(lc1, cNext, cOpposite));
    }
  }

  public void finishEditing() {
    if (this.circumCircleIndex != null) {
      if (this.triangleIndex == null) {
        this.triangleIndex = new RTree<>();
        for (final Triangle triangle : this.circumCircleIndex.getItems()) {
          final BoundingBox circleBoundingBox = triangle.getCircumcircleBoundingBox();
          this.circumCircleIndex.removeItem(circleBoundingBox, triangle);

          final BoundingBox envelope = triangle.getBoundingBox();
          this.triangleIndex.insertItem(envelope, triangle);
        }
      }
      this.circumCircleIndex = null;
    }
  }

  @Override
  public void forEachTriangle(final BoundingBox boundingBox,
    final Consumer<? super Triangle> action) {
    final SpatialIndex<Triangle> index = getTriangleIndex();
    if (index != null) {
      index.forEach(boundingBox, action);
    }
  }

  @Override
  public void forEachTriangle(final BoundingBox boundingBox,
    final Predicate<? super Triangle> filter, final Consumer<? super Triangle> action) {
    final SpatialIndex<Triangle> index = getTriangleIndex();
    if (index != null) {
      index.forEach(boundingBox, filter, action);
    }
  }

  @Override
  public void forEachTriangle(final Consumer<? super Triangle> action) {
    final SpatialIndex<Triangle> index = getTriangleIndex();
    if (index != null) {
      index.forEach(action);
    }
  }

  @Override
  public void forEachTriangle(final Predicate<? super Triangle> filter,
    final Consumer<? super Triangle> action) {
    final SpatialIndex<Triangle> index = getTriangleIndex();
    if (index != null) {
      index.forEach(filter, action);
    }
  }

  @Override
  public void forEachVertex(final Consumer<Point> action) {
    this.nodes.forEach(action);
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  private SpatialIndex<Triangle> getCircumcircleIndex() {
    if (this.circumCircleIndex == null) {
      this.circumCircleIndex = new RTree<>();
      if (this.triangleIndex != null) {
        for (final Triangle triangle : this.triangleIndex.getItems()) {
          final BoundingBox envelope = triangle.getCircumcircleBoundingBox();
          this.circumCircleIndex.insertItem(envelope, triangle);
        }
      }
    }
    return this.circumCircleIndex;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  private Point getOtherCoordinates(final Triangle coords, final int i1, final int i2) {
    final int index = getOtherIndex(i1, i2);
    return coords.getPoint(index);
  }

  /**
   * Get the index of the corner or a triangle opposite corners i1 -> i2. i1 and
   * i2 must have different values in the range 0..2.
   *
   * @param i1
   * @param i2
   * @return
   */
  private int getOtherIndex(final int i1, final int i2) {
    return OPPOSITE_INDEXES[i1 + i2 - 1];
  }

  @Override
  public Resource getResource() {
    return this.resource;
  }

  @Override
  public int getTriangleCount() {
    if (this.circumCircleIndex != null) {
      return this.circumCircleIndex.getSize();
    } else {
      final SpatialIndex<Triangle> index = getTriangleIndex();
      if (index == null) {
        return 0;
      } else {
        return index.getSize();
      }
    }
  }

  public SpatialIndex<Triangle> getTriangleIndex() {
    if (this.triangleIndex == null) {
      this.triangleIndex = new RTree<>();
      for (final Triangle triangle : this.circumCircleIndex.getItems()) {
        this.triangleIndex.insertItem(triangle.getBoundingBox(), triangle);
      }
    }
    return this.triangleIndex;
  }

  @Override
  public List<Triangle> getTriangles() {
    if (this.circumCircleIndex != null) {
      return this.circumCircleIndex.getItems();
    } else {
      return TriangulatedIrregularNetwork.super.getTriangles();
    }
  }

  private List<Triangle> getTrianglesCircumcircleIntersections(final double x, final double y) {
    final SpatialIndex<Triangle> circumcircleIndex = getCircumcircleIndex();
    final List<Triangle> triangles = circumcircleIndex.getItems(x, y, (triangle) -> {
      return triangle.circumcircleContains(x, y);
    });
    return triangles;
  }

  @Override
  public int getVertexCount() {
    return this.nodes.size();
  }

  public void insertEdge(LineSegment breakline) {
    breakline = breakline.getIntersection(this.boundingBox);
    if (!breakline.isEmpty()) {
      final List<Triangle> triangles = getTriangles(breakline);
      for (final Triangle triangle : triangles) {
        final LineSegment intersection = triangle.intersection(this.geometryFactory, breakline);
        if (intersection != null) {
          final double length = intersection.getLength();
          if (length < 0.01) {
            addBreaklineIntersect(triangle, intersection.getPoint(0));
          } else {
            addBreaklineItersect(triangle, breakline, intersection);
          }
        }
      }
    }
  }

  public void insertEdge(final LineString points) {
    Point previousPoint = points.getPoint(0);
    for (int i = 1; i < points.getVertexCount(); i++) {
      final Point point = points.getPoint(i);
      final LineSegment segment = new LineSegmentDoubleGF(this.geometryFactory, previousPoint,
        point);
      insertEdge(segment);
      previousPoint = point;
    }
  }

  public void insertNode(final double x, final double y, final double z) {
    if (this.boundingBox.covers(x, y) && !Double.isNaN(z)) {
      final List<Triangle> triangles = getTrianglesCircumcircleIntersections(x, y);
      if (!triangles.isEmpty()) {
        final Set<Point> exterior = new HashSet<>();
        for (final Triangle triangle : triangles) {
          if (triangle.circumcircleContains(x, y)) {
            removeTriangle(triangle);
            for (int i = 0; i < 3; i++) {
              final Point corner = triangle.getPoint(i);
              if (!corner.equals(x, y)) {
                exterior.add(corner);
              }
            }
          }
        }

        if (!exterior.isEmpty()) {
          final AngleFromPointComparator comparator = new AngleFromPointComparator(x, y);
          final List<Point> points = Lists.toArray(exterior);
          points.sort(comparator);
          final Point previousCorner = points.get(points.size() - 1);
          double x2 = previousCorner.getX();
          double y2 = previousCorner.getY();
          double z2 = previousCorner.getZ();
          final List<Triangle> triangles1 = new ArrayList<>();
          for (final Point point : points) {
            final double x3 = point.getX();
            final double y3 = point.getY();
            final double z3 = point.getZ();
            final TriangleWithCircumcircle triangle = TriangleWithCircumcircle
              .newClockwiseTriangle(x, y, z, x2, y2, z2, x3, y3, z3);
            for (final Triangle triangle2 : triangles1) {
              try {
                if (triangle2.intersection(triangle) instanceof Polygonal) {
                  Debug.noOp();
                }
              } catch (final Throwable e) {
                Debug.noOp();

              }
            }
            triangles1.add(triangle);
            addTriangle(triangle);
            x2 = x3;
            y2 = y3;
            z2 = z3;
          }
        }
      }
    }
  }

  public void insertNode(final Point point) {
    final double x = point.getX();
    final double y = point.getY();
    final double z = point.getZ();
    insertNode(x, y, z);
  }

  public void insertNodes(final Iterable<Point> points) {
    for (final Point point : points) {
      insertNode(point);
    }
  }

  public void insertNodes(final LineString line) {
    for (final Point point : line.vertices()) {
      insertNode(point);
    }
  }

  private void removeTriangle(final Triangle triangle) {
    if (this.triangleIndex != null) {
      final BoundingBox envelope = triangle.getBoundingBox();
      this.triangleIndex.removeItem(envelope, triangle);
    }
    if (this.circumCircleIndex != null) {
      final BoundingBox envelope = triangle.getCircumcircleBoundingBox();
      this.circumCircleIndex.removeItem(envelope, triangle);
    }
  }

  private void replaceTriangle(final Triangle triangle, final Triangle newTriangle) {
    removeTriangle(triangle);
    addTriangle(newTriangle);
  }

  private void replaceTriangle(final Triangle triangle, final Triangle... newTriangles) {
    removeTriangle(triangle);
    for (final Triangle newTriangle : newTriangles) {
      addTriangle(newTriangle);
    }
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory.convertAxisCount(3);
  }
}
