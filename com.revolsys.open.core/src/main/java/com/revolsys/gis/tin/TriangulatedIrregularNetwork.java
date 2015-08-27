package com.revolsys.gis.tin;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.revolsys.geometry.algorithm.CGAlgorithms;
import com.revolsys.geometry.algorithm.index.EnvelopeSpatialIndex;
import com.revolsys.geometry.algorithm.index.RTree;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.coordinates.CoordinatesUtil;
import com.revolsys.geometry.model.coordinates.comparator.AngleFromPointComparator;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.geometry.model.impl.PointDouble;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.LineSegmentDoubleGF;
import com.revolsys.math.Angle;

public class TriangulatedIrregularNetwork {
  private static final int[] OPPOSITE_INDEXES = {
    2, 1, 0
  };

  private final BoundingBox boundingBox;

  private RTree<Triangle> circumCircleIndex;

  private GeometryFactory geometryFactory;

  private final Set<Point> nodes = new HashSet<Point>();

  private RTree<Triangle> triangleIndex;

  public TriangulatedIrregularNetwork(final BoundingBox boundingBox) {
    this(boundingBox.getGeometryFactory(), boundingBox);
  }

  protected TriangulatedIrregularNetwork(final BoundingBox boundingBox, final boolean loadMode) {
    this(boundingBox.getGeometryFactory(), boundingBox, loadMode);
  }

  public TriangulatedIrregularNetwork(final GeometryFactory geometryFactory) {
    this(geometryFactory.getCoordinateSystem().getAreaBoundingBox());
  }

  public TriangulatedIrregularNetwork(final GeometryFactory geometryFactory,
    final BoundingBox boundingBox) {
    this(geometryFactory, boundingBox, false);
  }

  public TriangulatedIrregularNetwork(final GeometryFactory geometryFactory,
    final BoundingBox boundingBox, final boolean loadMode) {
    this.boundingBox = boundingBox;
    setGeometryFactory(geometryFactory);
    if (loadMode) {
      this.triangleIndex = new RTree<Triangle>();
    } else {
      this.circumCircleIndex = new RTree<Triangle>();
      final double minX = geometryFactory.makeXyPrecise(boundingBox.getMinX() - 100);
      final double minY = geometryFactory.makeXyPrecise(boundingBox.getMinY() - 100);
      final double maxX = geometryFactory.makeXyPrecise(boundingBox.getMaxX() + 100);
      final double maxY = geometryFactory.makeXyPrecise(boundingBox.getMaxY() + 100);
      final Point c1 = new PointDouble(minX, minY, 0);
      final Point c2 = new PointDouble(maxX, minY, 0);
      final Point c3 = new PointDouble(maxX, maxY, 0);
      final Point c4 = new PointDouble(minX, maxY, 0);
      final Triangle triangle1 = Triangle.createClockwiseTriangle(c1, c2, c3);
      addTriangle(triangle1);
      final Triangle triangle2 = Triangle.createClockwiseTriangle(c1, c3, c4);
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
          final Triangle newTriangle = Triangle.createClockwiseTriangle(lc0, lc1,
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
    Point previousCoord = triangle.getPoint(0);
    for (int i = 1; i < 3; i++) {
      final Point triCorner = triangle.getPoint(i);
      if (!triCorner.equals(2, intersectCoord) && !previousCoord.equals(2, intersectCoord)) {
        final double distance = new LineSegmentDoubleGF(previousCoord, triCorner)
          .distance(intersectCoord);
        if (distance == 0) {
          final Point nextCoordinates = triangle.getPoint((i + 1) % 3);
          replaceTriangle(triangle,
            Triangle.createClockwiseTriangle(intersectCoord, triCorner, nextCoordinates),
            Triangle.createClockwiseTriangle(intersectCoord, nextCoordinates, previousCoord));
        }
      }
      previousCoord = triCorner;
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
   * @param coordinates The coordinates of the triangle.
   * @param index The index of the closest corner to i0.
   * @param l0 The start coordinate of the line.
   * @param l1 The end coordinate of the line.
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
      final double angleCornerLine = Angle.angle(t0, l0, l1);
      final double angleCornerLineCorner = Angle.angle(t0, l0, t2);
      if (angleCornerLine > angleCornerLineCorner) {
        addTrianglesContained(triangle, t0, t1, t2, l0, l1);
      } else if (angleCornerLine == angleCornerLineCorner) {
        addTrianglesContained(triangle, t2, t0, t1, l1, l0);
      } else {
        addTrianglesContained(triangle, t1, t2, t0, l0, l1);
      }

    } else {
      final double angleCornerLine = Angle.angle(t0, l0, l1);
      final double angleCornerLineCorner = Angle.angle(t0, l0, t1);
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
    replaceTriangle(triangle, Triangle.createClockwiseTriangle(cPrevious, c, cOpposite),
      Triangle.createClockwiseTriangle(c, cNext, cOpposite));
  }

  protected void addTriangle(final Triangle triangle) {
    for (int i = 0; i < 3; i++) {
      final Point point = triangle.getPoint(i);
      if (!this.nodes.contains(point)) {
        this.nodes.add(point);
      }
    }
    if (this.circumCircleIndex != null) {
      final Circle circle = triangle.getCircumcircle();
      final BoundingBox envelope = circle.getEnvelopeInternal();
      this.circumCircleIndex.put(envelope, triangle);
    }
    if (this.triangleIndex != null) {
      final BoundingBoxDoubleGf envelope = triangle.getEnvelopeInternal();
      this.triangleIndex.put(envelope, triangle);
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
   * will be 5 triangles created. The triangle coordinate t0 will be part of two
   * triangles, the other two triangle coordinates will be part of 3 triangles.
   * l1 must not be closer than l0 to t0.
   *
   * @param triangle TODO
   * @param t0 The first triangle coordinate.
   * @param t1 The second triangle coordinate.
   * @param t2 The third triangle coordinate.
   * @param l0 The first line coordinate.
   * @param l1 The second line coordinate.
   */
  private void addTrianglesContained(final Triangle triangle, final Point t0, final Point t1,
    final Point t2, final Point l0, final Point l1) {
    replaceTriangle(triangle, Triangle.createClockwiseTriangle(t0, t1, l0),
      Triangle.createClockwiseTriangle(l0, t1, l1), Triangle.createClockwiseTriangle(l1, t1, t2),
      Triangle.createClockwiseTriangle(l0, l1, t2), Triangle.createClockwiseTriangle(t0, l0, t2));
  }

  private void addTriangleStartCornerEndInside(final Triangle triangle, final int cornerIndex,
    final Point cCorner, final Point cInside) {
    final Point cNext = triangle.getPoint((cornerIndex + 1) % 3);
    final Point cPrevious = triangle.getPoint((cornerIndex + 2) % 3);
    replaceTriangle(triangle, Triangle.createClockwiseTriangle(cCorner, cNext, cInside),
      Triangle.createClockwiseTriangle(cInside, cNext, cPrevious),
      Triangle.createClockwiseTriangle(cInside, cPrevious, cCorner));
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
      replaceTriangle(triangle, Triangle.createClockwiseTriangle(cPrevious, lc0, cOpposite),
        Triangle.createClockwiseTriangle(cOpposite, lc0, lc1),
        Triangle.createClockwiseTriangle(cOpposite, lc1, cNext),
        Triangle.createClockwiseTriangle(lc0, lc1, cNext));
    } else {
      replaceTriangle(triangle, Triangle.createClockwiseTriangle(cPrevious, lc0, lc1),
        Triangle.createClockwiseTriangle(cNext, lc0, lc1),
        Triangle.createClockwiseTriangle(cNext, lc1, cOpposite),
        Triangle.createClockwiseTriangle(cPrevious, lc1, cOpposite));
    }
  }

  private void addTriangleTouchingTwoEdges(final Triangle triangle, final Point lc0,
    final Point lc1, final int startEdge, final int endEdge) {
    final Point cPrevious = triangle.getPoint(startEdge);
    final Point cNext = triangle.getPoint((startEdge + 1) % 3);
    final Point cOpposite = triangle.getPoint((startEdge + 2) % 3);
    if (startEdge == endEdge) {
      if (cPrevious.distance(lc0) < cPrevious.distance(lc1)) {
        replaceTriangle(triangle, Triangle.createClockwiseTriangle(cPrevious, lc0, cOpposite),
          Triangle.createClockwiseTriangle(lc0, lc1, cOpposite),
          Triangle.createClockwiseTriangle(lc1, cNext, cOpposite));
      } else {
        replaceTriangle(triangle, Triangle.createClockwiseTriangle(cPrevious, lc1, cOpposite),
          Triangle.createClockwiseTriangle(lc0, lc1, cOpposite),
          Triangle.createClockwiseTriangle(lc0, cNext, cOpposite));
      }
    } else if (endEdge == (startEdge + 1) % 3) {
      replaceTriangle(triangle, Triangle.createClockwiseTriangle(cPrevious, lc0, cOpposite),
        Triangle.createClockwiseTriangle(lc0, lc1, cOpposite),
        Triangle.createClockwiseTriangle(lc0, cNext, lc1));
    } else {
      replaceTriangle(triangle, Triangle.createClockwiseTriangle(cPrevious, lc0, lc1),
        Triangle.createClockwiseTriangle(lc0, cNext, lc1),
        Triangle.createClockwiseTriangle(lc1, cNext, cOpposite));
    }
  }

  public void finishEditing() {
    if (this.circumCircleIndex != null) {
      if (this.triangleIndex == null) {
        this.triangleIndex = new RTree<Triangle>();
        for (final Triangle triangle : this.circumCircleIndex.findAll()) {
          final Circle circumcircle = triangle.getCircumcircle();
          final BoundingBox circleEnvelope = circumcircle.getEnvelopeInternal();
          this.circumCircleIndex.remove(circleEnvelope, triangle);

          final BoundingBox envelope = circumcircle.getEnvelopeInternal();
          this.triangleIndex.put(envelope, triangle);
        }
      }
      this.circumCircleIndex = null;
    }
  }

  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  private EnvelopeSpatialIndex<Triangle> getCircumcircleIndex() {
    if (this.circumCircleIndex == null) {
      this.circumCircleIndex = new RTree<Triangle>();
      if (this.triangleIndex != null) {
        for (final Triangle triangle : this.triangleIndex.findAll()) {
          final Circle circumcircle = triangle.getCircumcircle();
          final BoundingBox envelope = circumcircle.getEnvelopeInternal();
          this.circumCircleIndex.put(envelope, triangle);
        }
      }
    }
    return this.circumCircleIndex;
  }

  public LineString getElevation(final LineString line) {
    final GeometryFactory geometryFactory = line.getGeometryFactory();
    final LineString oldPoints = line;
    final int vertexCount = line.getVertexCount();
    final int axisCount = line.getAxisCount();
    final double[] newCoordinates = new double[vertexCount * axisCount];

    boolean modified = false;
    int i = 0;
    for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        double value = line.getCoordinate(vertexIndex, axisIndex);
        if (axisIndex == 2) {
          final double newZ = getElevation(line.getPoint(vertexIndex));
          if (!Double.isNaN(newZ)) {
            if (value != newZ) {
              value = newZ;
              modified = true;
            }
          }
        }
        newCoordinates[i] = value;
        i++;
      }
    }
    if (modified) {
      return geometryFactory.lineString(axisCount, newCoordinates);
    } else {
      return line;
    }
  }

  public double getElevation(final Point coordinate) {
    final Point point = this.geometryFactory.createCoordinates(coordinate);
    final List<Triangle> triangles = getTriangles(coordinate);
    for (final Triangle triangle : triangles) {
      final Point t0 = triangle.getP0();
      if (t0.equals(point)) {
        return t0.getZ();
      }
      final Point t1 = triangle.getP1();
      if (t1.equals(point)) {
        return t1.getZ();
      }
      final Point t2 = triangle.getP2();
      if (t2.equals(point)) {
        return t2.getZ();
      }
      Point closestCorner = t0;
      LineSegment oppositeEdge = new LineSegmentDoubleGF(t1, t2);
      double closestDistance = coordinate.distance(closestCorner);
      final double t1Distance = coordinate.distance(t1);
      if (closestDistance > t1Distance) {
        closestCorner = t1;
        oppositeEdge = new LineSegmentDoubleGF(t2, t0);
        closestDistance = t1Distance;
      }
      if (closestDistance > coordinate.distance(t2)) {
        closestCorner = t2;
        oppositeEdge = new LineSegmentDoubleGF(t0, t1);
      }
      LineSegment segment = new LineSegmentDoubleGF(closestCorner, coordinate).extend(0,
        t0.distance(t1) + t1.distance(t2) + t0.distance(t2));
      final Geometry intersectCoordinates = oppositeEdge.getIntersection(segment);
      if (intersectCoordinates.getVertexCount() > 0) {
        final Point intersectPoint = intersectCoordinates.getVertex(0);
        final double z = oppositeEdge.getElevation(intersectPoint);
        if (!Double.isNaN(z)) {
          final double x = intersectPoint.getX();
          final double y = intersectPoint.getY();
          final Point end = new PointDouble(x, y, z);
          segment = new LineSegmentDoubleGF(t0, end);
          return segment.getElevation(coordinate);
        }
      }
    }
    return Double.NaN;
  }

  public Set<Point> getNodes() {
    return Collections.unmodifiableSet(this.nodes);
  }

  private Point getOtherCoordinates(final LineString coords, final int i1, final int i2) {
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

  public int getSize() {
    if (this.circumCircleIndex != null) {
      return this.circumCircleIndex.getSize();
    } else if (this.triangleIndex != null) {
      return this.triangleIndex.getSize();
    } else {
      return 0;
    }
  }

  public EnvelopeSpatialIndex<Triangle> getTriangleIndex() {
    if (this.triangleIndex == null) {
      this.triangleIndex = new RTree<Triangle>();
      for (final Triangle triangle : this.circumCircleIndex.findAll()) {
        this.triangleIndex.put(triangle.getEnvelopeInternal(), triangle);
      }
    }
    return this.triangleIndex;
  }

  public List<Triangle> getTriangles() {
    if (this.circumCircleIndex != null) {
      return this.circumCircleIndex.findAll();
    } else if (this.triangleIndex != null) {
      return this.triangleIndex.findAll();
    } else {
      return Collections.emptyList();
    }
  }

  public List<Triangle> getTriangles(final BoundingBox envelope) {
    return getTriangleIndex().find(envelope);
  }

  public List<Triangle> getTriangles(final LineSegment segment) {
    final BoundingBox envelope = segment.getBoundingBox();
    return getTriangles(envelope);
  }

  public List<Triangle> getTriangles(final Point coordinate) {
    final BoundingBox envelope = new BoundingBoxDoubleGf(coordinate);
    final TriangleContainsPointFilter filter = new TriangleContainsPointFilter(coordinate);
    return getTriangleIndex().find(envelope, filter);
  }

  private List<Triangle> getTrianglesCircumcircleIntersections(final Point point) {
    final BoundingBox envelope = new BoundingBoxDoubleGf(point);
    final List<Triangle> triangles = getCircumcircleIndex().find(envelope);
    for (final Iterator<Triangle> iterator = triangles.iterator(); iterator.hasNext();) {
      final Triangle triangle = iterator.next();
      if (!triangle.intersectsCircumCircle(point)) {
        iterator.remove();
      }
    }
    return triangles;
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

  public void insertEdge(final LineString coordinates) {
    Point previousCoordinates = coordinates.getPoint(0);
    for (int i = 1; i < coordinates.getVertexCount(); i++) {
      final Point coordinate = coordinates.getPoint(i);
      final LineSegment segment = new LineSegmentDoubleGF(this.geometryFactory, previousCoordinates,
        coordinate);
      insertEdge(segment);
      previousCoordinates = coordinate;
    }
  }

  public void insertNode(final Point coordinate) {
    if (this.boundingBox.covers(coordinate)) {
      final Point point = new PointDouble(this.geometryFactory.createCoordinates(coordinate), 3);
      if (!this.nodes.contains(point)) {
        final List<Triangle> triangles = getTrianglesCircumcircleIntersections(point);
        if (!triangles.isEmpty()) {
          final AngleFromPointComparator comparator = new AngleFromPointComparator(point);
          final TreeSet<Point> exterior = new TreeSet<Point>(comparator);
          for (final Triangle triangle : triangles) {
            final Circle circle = triangle.getCircumcircle();
            if (circle.contains(point)) {
              removeTriangle(triangle);
              if (!point.equals(2, triangle.getP0())) {
                exterior.add(triangle.getP0());
              }
              if (!point.equals(2, triangle.getP1())) {
                exterior.add(triangle.getP1());
              }
              if (!point.equals(2, triangle.getP2())) {
                exterior.add(triangle.getP2());
              }
            }
          }

          if (!exterior.isEmpty()) {
            Point previousCorner = exterior.last();
            for (final Point corner : exterior) {
              addTriangle(new Triangle(point, previousCorner, corner));
              previousCorner = corner;
            }
          }
        }
      }
    }
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
      final BoundingBoxDoubleGf envelope = triangle.getEnvelopeInternal();
      this.triangleIndex.remove(envelope, triangle);
    }
    if (this.circumCircleIndex != null) {
      final Circle circumcircle = triangle.getCircumcircle();
      final BoundingBox envelope = circumcircle.getEnvelopeInternal();
      if (!this.circumCircleIndex.remove(envelope, triangle)) {
        System.err.println(circumcircle.toGeometry());
        System.err.println(envelope.toPolygon(1));
        System.err.println(triangle);
        this.circumCircleIndex.remove(envelope, triangle);
      }
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
    if (geometryFactory.getAxisCount() != 3) {
      final int srid = geometryFactory.getSrid();
      final double scaleXY = geometryFactory.getScaleXY();
      this.geometryFactory = GeometryFactory.fixed(srid, scaleXY, 1.0);
    } else {
      this.geometryFactory = geometryFactory;
    }
  }
}
