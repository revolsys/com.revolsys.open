package com.revolsys.gis.tin;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.revolsys.gis.algorithm.index.EnvelopeSpatialIndex;
import com.revolsys.gis.algorithm.index.RTree;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.comparator.AngleFromPointComparator;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.gis.model.coordinates.list.InPlaceIterator;
import com.revolsys.gis.model.geometry.Circle;
import com.revolsys.gis.model.geometry.LineSegment;
import com.revolsys.gis.model.geometry.Triangle;
import com.revolsys.gis.model.geometry.visitor.TriangleContainsPointFilter;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public class TriangulatedIrregularNetwork {
  private static final int[] OPPOSITE_INDEXES = {
    2, 1, 0
  };

  private RTree<Triangle> circumCircleIndex;

  private GeometryFactory geometryFactory;

  private RTree<Triangle> triangleIndex;

  private final BoundingBox boundingBox;

  private final Set<Coordinates> nodes = new HashSet<Coordinates>();

  public TriangulatedIrregularNetwork(final BoundingBox boundingBox) {
    this(boundingBox.getGeometryFactory(), boundingBox);
  }

  protected TriangulatedIrregularNetwork(final BoundingBox boundingBox,
    final boolean loadMode) {
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
      triangleIndex = new RTree<Triangle>();
    } else {
      circumCircleIndex = new RTree<Triangle>();
      final double minX = geometryFactory.makeXyPrecise(boundingBox.getMinX() - 100);
      final double minY = geometryFactory.makeXyPrecise(boundingBox.getMinY() - 100);
      final double maxX = geometryFactory.makeXyPrecise(boundingBox.getMaxX() + 100);
      final double maxY = geometryFactory.makeXyPrecise(boundingBox.getMaxY() + 100);
      final Coordinates c1 = new DoubleCoordinates(minX, minY, 0);
      final Coordinates c2 = new DoubleCoordinates(maxX, minY, 0);
      final Coordinates c3 = new DoubleCoordinates(maxX, maxY, 0);
      final Coordinates c4 = new DoubleCoordinates(minX, maxY, 0);
      final Triangle triangle1 = Triangle.createClockwiseTriangle(c1, c2, c3);
      addTriangle(triangle1);
      final Triangle triangle2 = Triangle.createClockwiseTriangle(c1, c3, c4);
      addTriangle(triangle2);
    }
  }

  public TriangulatedIrregularNetwork(final GeometryFactory geometryFactory,
    final Envelope envelope) {
    this(geometryFactory, new BoundingBox(geometryFactory, envelope));
  }

  private void addBreaklineIntersect(
    final Triangle triangle,
    final Coordinates intersectCoord) {
    Coordinates previousCoord = triangle.get(0);
    for (int i = 1; i < 3; i++) {
      final Coordinates triCorner = triangle.get(i);
      if (!triCorner.equals2d(intersectCoord)
        && !previousCoord.equals2d(intersectCoord)) {
        final double distance = new LineSegment(previousCoord, triCorner).distance(intersectCoord);
        if (distance == 0) {
          final Coordinates nextCoordinates = triangle.get((i + 1) % 3);
          replaceTriangle(triangle, Triangle.createClockwiseTriangle(
            intersectCoord, triCorner, nextCoordinates),
            Triangle.createClockwiseTriangle(intersectCoord, nextCoordinates,
              previousCoord));
        }
      }
      previousCoord = triCorner;
    }
  }

  private void addBreaklineIntersect(
    final Triangle triangle,
    final LineSegment intersectLine) {
    final Coordinates lc0 = intersectLine.get(0);
    final Coordinates lc1 = intersectLine.get(1);
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
        final Coordinates corner = triangle.get(i);
        final Coordinates nextCorner = triangle.get((i + 1) % 3);

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

        final LineSegment edge = new LineSegment(corner, nextCorner);
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
          final Triangle newTriangle = Triangle.createClockwiseTriangle(lc0,
            lc1,
            getOtherCoordinates(triangle, startClosestCorner, endClosestCorner));
          replaceTriangle(triangle, newTriangle);
        } else {
          // Touching start corner
          final double edgeDistance = endEdgeDistance;
          addTriangleTouchingOneCorner(triangle, lc0, lc1, startClosestCorner,
            endClosestEdge, edgeDistance);
        }
      } else if (endCornerDistance < 0.01) {
        // Touching end corner
        final double edgeDistance = startEdgeDistance;
        addTriangleTouchingOneCorner(triangle, lc1, lc0, endClosestCorner,
          startClosestEdge, edgeDistance);
      } else if (startEdgeDistance < 0.01) {
        if (endEdgeDistance < 0.01) {
          addTriangleTouchingTwoEdges(triangle, lc0, lc1, startClosestEdge,
            endClosestEdge);
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

  private void addBreaklineItersect(
    final Triangle triangle,
    final LineSegment breakline,
    final LineSegment intersectLine) {
    final Coordinates lc0 = intersectLine.get(0);
    final Coordinates lc1 = intersectLine.get(0);
    breakline.setElevationOnPoint(geometryFactory, lc0);
    breakline.setElevationOnPoint(geometryFactory, lc1);
    final LineSegment lineSegment = new LineSegment(lc0, lc1);
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
  private void addContainedLine(
    final Triangle triangle,
    final int index,
    final Coordinates l0,
    final Coordinates l1) {
    final Coordinates t0 = triangle.get(index);
    final Coordinates t1 = triangle.get((index + 1) % 3);
    final Coordinates t2 = triangle.get((index + 2) % 3);

    final int c0i0i1Orientation = CoordinatesUtil.orientationIndex(t0, l0, l1);
    if (c0i0i1Orientation == CGAlgorithms.COLLINEAR) {
      addTrianglesContained(triangle, t0, t1, t2, l0, l1);

    } else if (c0i0i1Orientation == CGAlgorithms.CLOCKWISE) {
      final double angleCornerLine = CoordinatesUtil.angle(t0, l0, l1);
      final double angleCornerLineCorner = CoordinatesUtil.angle(t0, l0, t2);
      if (angleCornerLine > angleCornerLineCorner) {
        addTrianglesContained(triangle, t0, t1, t2, l0, l1);
      } else if (angleCornerLine == angleCornerLineCorner) {
        addTrianglesContained(triangle, t2, t0, t1, l1, l0);
      } else {
        addTrianglesContained(triangle, t1, t2, t0, l0, l1);
      }

    } else {
      final double angleCornerLine = CoordinatesUtil.angle(t0, l0, l1);
      final double angleCornerLineCorner = CoordinatesUtil.angle(t0, l0, t1);
      if (angleCornerLine > angleCornerLineCorner) {
        addTrianglesContained(triangle, t0, t1, t2, l0, l1);
      } else if (angleCornerLine == angleCornerLineCorner) {
        addTrianglesContained(triangle, t1, t2, t0, l1, l0);
      } else {
        addTrianglesContained(triangle, t2, t0, t1, l1, l0);
      }
    }
  }

  private void addTrangleCornerAndEdgeTouch(
    final Triangle triangle,
    final Coordinates cPrevious,
    final Coordinates c,
    final Coordinates cNext,
    final Coordinates cOpposite) {
    replaceTriangle(triangle,
      Triangle.createClockwiseTriangle(cPrevious, c, cOpposite),
      Triangle.createClockwiseTriangle(c, cNext, cOpposite));
  }

  protected void addTriangle(final Triangle triangle) {
    for (int i = 0; i < 3; i++) {
      final Coordinates point = triangle.get(i);
      if (!nodes.contains(point)) {
        nodes.add(point);
      }
    }
    if (circumCircleIndex != null) {
      final Circle circle = triangle.getCircumcircle();
      final Envelope envelope = circle.getEnvelopeInternal();
      circumCircleIndex.put(envelope, triangle);
    }
    if (triangleIndex != null) {
      final Envelope envelope = triangle.getEnvelopeInternal();
      triangleIndex.put(envelope, triangle);
    }
  }

  private void addTriangleCorderEdge(
    final Triangle triangle,
    final Coordinates lc0,
    final Coordinates lc1,
    final int startCorner,
    final int startEdge) {
    final Coordinates cNext = triangle.get((startCorner + 1) % 3);
    final Coordinates cPrevious = triangle.get((startCorner + 2) % 3);
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
  private void addTrianglesContained(
    final Triangle triangle,
    final Coordinates t0,
    final Coordinates t1,
    final Coordinates t2,
    final Coordinates l0,
    final Coordinates l1) {
    replaceTriangle(triangle, Triangle.createClockwiseTriangle(t0, t1, l0),
      Triangle.createClockwiseTriangle(l0, t1, l1),
      Triangle.createClockwiseTriangle(l1, t1, t2),
      Triangle.createClockwiseTriangle(l0, l1, t2),
      Triangle.createClockwiseTriangle(t0, l0, t2));
  }

  private void addTriangleStartCornerEndInside(
    final Triangle triangle,
    final int cornerIndex,
    final Coordinates cCorner,
    final Coordinates cInside) {
    final Coordinates cNext = triangle.get((cornerIndex + 1) % 3);
    final Coordinates cPrevious = triangle.get((cornerIndex + 2) % 3);
    replaceTriangle(triangle,
      Triangle.createClockwiseTriangle(cCorner, cNext, cInside),
      Triangle.createClockwiseTriangle(cInside, cNext, cPrevious),
      Triangle.createClockwiseTriangle(cInside, cPrevious, cCorner));
  }

  private void addTriangleTouchingOneCorner(
    final Triangle triangle,
    final Coordinates lc0,
    final Coordinates lc1,
    final int startCorner,
    final int endEdge,
    final double endEdgeDistance) {
    if (endEdgeDistance < 1) {
      addTriangleCorderEdge(triangle, lc0, lc1, startCorner, endEdge);
    } else {
      addTriangleStartCornerEndInside(triangle, startCorner, lc0, lc1);
    }
  }

  private void addTriangleTouchingOneEdge(
    final Triangle triangle,
    final Coordinates lc0,
    final Coordinates lc1,
    final int edgeIndex) {
    final Coordinates cPrevious = triangle.get((edgeIndex) % 3);
    final Coordinates cNext = triangle.get((edgeIndex + 1) % 3);
    final Coordinates cOpposite = triangle.get((edgeIndex + 2) % 3);
    if (CoordinatesUtil.orientationIndex(cPrevious, lc0, lc1) == CGAlgorithms.COLLINEAR) {
      replaceTriangle(triangle,
        Triangle.createClockwiseTriangle(cPrevious, lc0, cOpposite),
        Triangle.createClockwiseTriangle(cOpposite, lc0, lc1),
        Triangle.createClockwiseTriangle(cOpposite, lc1, cNext),
        Triangle.createClockwiseTriangle(lc0, lc1, cNext));
    } else {
      replaceTriangle(triangle,
        Triangle.createClockwiseTriangle(cPrevious, lc0, lc1),
        Triangle.createClockwiseTriangle(cNext, lc0, lc1),
        Triangle.createClockwiseTriangle(cNext, lc1, cOpposite),
        Triangle.createClockwiseTriangle(cPrevious, lc1, cOpposite));
    }
  }

  private void addTriangleTouchingTwoEdges(
    final Triangle triangle,
    final Coordinates lc0,
    final Coordinates lc1,
    final int startEdge,
    final int endEdge) {
    final Coordinates cPrevious = triangle.get(startEdge);
    final Coordinates cNext = triangle.get((startEdge + 1) % 3);
    final Coordinates cOpposite = triangle.get((startEdge + 2) % 3);
    if (startEdge == endEdge) {
      if (cPrevious.distance(lc0) < cPrevious.distance(lc1)) {
        replaceTriangle(triangle,
          Triangle.createClockwiseTriangle(cPrevious, lc0, cOpposite),
          Triangle.createClockwiseTriangle(lc0, lc1, cOpposite),
          Triangle.createClockwiseTriangle(lc1, cNext, cOpposite));
      } else {
        replaceTriangle(triangle,
          Triangle.createClockwiseTriangle(cPrevious, lc1, cOpposite),
          Triangle.createClockwiseTriangle(lc0, lc1, cOpposite),
          Triangle.createClockwiseTriangle(lc0, cNext, cOpposite));
      }
    } else if (endEdge == ((startEdge + 1) % 3)) {
      replaceTriangle(triangle,
        Triangle.createClockwiseTriangle(cPrevious, lc0, cOpposite),
        Triangle.createClockwiseTriangle(lc0, lc1, cOpposite),
        Triangle.createClockwiseTriangle(lc0, cNext, lc1));
    } else {
      replaceTriangle(triangle,
        Triangle.createClockwiseTriangle(cPrevious, lc0, lc1),
        Triangle.createClockwiseTriangle(lc0, cNext, lc1),
        Triangle.createClockwiseTriangle(lc1, cNext, cOpposite));
    }
  }

  public void finishEditing() {
    if (circumCircleIndex != null) {
      if (triangleIndex == null) {
        triangleIndex = new RTree<Triangle>();
        for (final Triangle triangle : circumCircleIndex.findAll()) {
          final Circle circumcircle = triangle.getCircumcircle();
          final Envelope circleEnvelope = circumcircle.getEnvelopeInternal();
          circumCircleIndex.remove(circleEnvelope, triangle);

          final Envelope envelope = circumcircle.getEnvelopeInternal();
          triangleIndex.put(envelope, triangle);
        }
      }
      circumCircleIndex = null;
    }
  }

  public BoundingBox getBoundingBox() {
    return boundingBox;
  }

  private EnvelopeSpatialIndex<Triangle> getCircumcircleIndex() {
    if (circumCircleIndex == null) {
      circumCircleIndex = new RTree<Triangle>();
      if (triangleIndex != null) {
        for (final Triangle triangle : triangleIndex.findAll()) {
          final Circle circumcircle = triangle.getCircumcircle();
          final Envelope envelope = circumcircle.getEnvelopeInternal();
          circumCircleIndex.put(envelope, triangle);
        }
      }
    }
    return circumCircleIndex;
  }

  public double getElevation(final Coordinates coordinate) {
    final DoubleCoordinates point = new DoubleCoordinates(coordinate);
    geometryFactory.makePrecise(point);
    final List<Triangle> triangles = getTriangles(coordinate);
    for (final Triangle triangle : triangles) {
      final Coordinates t0 = triangle.getP0();
      if (t0.equals(point)) {
        return t0.getZ();
      }
      final Coordinates t1 = triangle.getP1();
      if (t1.equals(point)) {
        return t1.getZ();
      }
      final Coordinates t2 = triangle.getP2();
      if (t2.equals(point)) {
        return t2.getZ();
      }
      Coordinates closestCorner = t0;
      LineSegment oppositeEdge = new LineSegment(t1, t2);
      double closestDistance = coordinate.distance(closestCorner);
      final double t1Distance = coordinate.distance(t1);
      if (closestDistance > t1Distance) {
        closestCorner = t1;
        oppositeEdge = new LineSegment(t2, t0);
        closestDistance = t1Distance;
      }
      if (closestDistance > coordinate.distance(t2)) {
        closestCorner = t2;
        oppositeEdge = new LineSegment(t0, t1);
      }
      LineSegment segment = new LineSegment(closestCorner, coordinate).extend(
        0, t0.distance(t1) + t1.distance(t2) + t0.distance(t2));
      final CoordinatesList intersectCoordinates = oppositeEdge.getIntersection(segment);
      if (intersectCoordinates.size() > 0) {
        Coordinates intersectPoint = intersectCoordinates.get(0);
        final double z = oppositeEdge.getElevation(intersectPoint);
        if (!Double.isNaN(z)) {
          final double x = intersectPoint.getX();
          final double y = intersectPoint.getY();
          final Coordinates end = new DoubleCoordinates(x, y, z);
          segment = new LineSegment(t0, end);
          return segment.getElevation(coordinate);
        }
      }
    }
    return Double.NaN;
  }

  public LineString getElevation(final LineString line) {
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(line);
    final CoordinatesList oldPoints = CoordinatesListUtil.get(line);
    final CoordinatesList newPoints = new DoubleCoordinatesList(oldPoints);
    boolean modified = false;
    for (final Coordinates point : new InPlaceIterator(newPoints)) {
      final double oldZ = point.getZ();
      final double newZ = getElevation(point);
      if (!Double.isNaN(newZ)) {
        if (oldZ != newZ) {
          point.setZ(newZ);
          modified = true;
        }
      }
    }
    if (modified) {
      return geometryFactory.createLineString(newPoints);
    } else {
      return line;
    }
  }

  public Set<Coordinates> getNodes() {
    return Collections.unmodifiableSet(nodes);
  }

  private Coordinates getOtherCoordinates(
    final CoordinatesList coords,
    final int i1,
    final int i2) {
    final int index = getOtherIndex(i1, i2);
    return coords.get(index);
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
    if (circumCircleIndex != null) {
      return circumCircleIndex.getSize();
    } else if (triangleIndex != null) {
      return triangleIndex.getSize();
    } else {
      return 0;
    }
  }

  public EnvelopeSpatialIndex<Triangle> getTriangleIndex() {
    if (triangleIndex == null) {
      triangleIndex = new RTree<Triangle>();
      for (final Triangle triangle : circumCircleIndex.findAll()) {
        triangleIndex.put(triangle.getEnvelopeInternal(), triangle);
      }
    }
    return triangleIndex;
  }

  public List<Triangle> getTriangles() {
    if (circumCircleIndex != null) {
      return circumCircleIndex.findAll();
    } else if (triangleIndex != null) {
      return triangleIndex.findAll();
    } else {
      return Collections.emptyList();
    }
  }

  public List<Triangle> getTriangles(final Coordinates coordinate) {
    final Envelope envelope = new BoundingBox(coordinate);
    final TriangleContainsPointFilter filter = new TriangleContainsPointFilter(
      coordinate);
    return getTriangleIndex().find(envelope, filter);
  }

  public List<Triangle> getTriangles(final Envelope envelope) {
    return getTriangleIndex().find(envelope);
  }

  public List<Triangle> getTriangles(final LineSegment segment) {
    final Envelope envelope = new BoundingBox(segment.getGeometryFactory(),
      segment.get(0), segment.get(1));
    return getTriangles(envelope);
  }

  private List<Triangle> getTrianglesCircumcircleIntersections(
    final Coordinates point) {
    final Envelope envelope = new BoundingBox(point);
    final List<Triangle> triangles = getCircumcircleIndex().find(envelope);
    for (final Iterator<Triangle> iterator = triangles.iterator(); iterator.hasNext();) {
      final Triangle triangle = iterator.next();
      if (!triangle.intersectsCircumCircle(point)) {
        iterator.remove();
      }
    }
    return triangles;
  }

  public void insertEdge(final CoordinatesList coordinates) {
    Coordinates previousCoordinates = coordinates.get(0);
    for (int i = 1; i < coordinates.size(); i++) {
      final Coordinates coordinate = coordinates.get(i);
      final LineSegment segment = new LineSegment(this.geometryFactory,
        previousCoordinates, coordinate);
      insertEdge(segment);
      previousCoordinates = coordinate;
    }
  }

  public void insertEdge(LineSegment breakline) {
    breakline = breakline.getIntersection(boundingBox);
    if (!breakline.isEmpty()) {
      final List<Triangle> triangles = getTriangles(breakline);
      for (final Triangle triangle : triangles) {
        final LineSegment intersection = triangle.intersection(geometryFactory,
          breakline);
        if (intersection != null) {
          final double length = intersection.getLength();
          if (length < 0.01) {
            addBreaklineIntersect(triangle, intersection.get(0));
          } else {
            addBreaklineItersect(triangle, breakline, intersection);
          }
        }
      }
    }
  }

  public void insertEdge(final LineString breakline) {
    final CoordinatesList coordinates = CoordinatesListUtil.get(breakline);
    insertEdge(coordinates);
  }

  public void insertNode(final Coordinates coordinate) {
    if (boundingBox.contains(coordinate)) {
      final Coordinates point = new DoubleCoordinates(coordinate, 3);
      geometryFactory.makePrecise(point);
      if (!nodes.contains(point)) {
        final List<Triangle> triangles = getTrianglesCircumcircleIntersections(point);
        if (!triangles.isEmpty()) {
          final AngleFromPointComparator comparator = new AngleFromPointComparator(
            point);
          final TreeSet<Coordinates> exterior = new TreeSet<Coordinates>(
            comparator);
          for (final Triangle triangle : triangles) {
            final Circle circle = triangle.getCircumcircle();
            if (circle.contains(point)) {
              removeTriangle(triangle);
              if (!point.equals2d(triangle.getP0())) {
                exterior.add(triangle.getP0());
              }
              if (!point.equals2d(triangle.getP1())) {
                exterior.add(triangle.getP1());
              }
              if (!point.equals2d(triangle.getP2())) {
                exterior.add(triangle.getP2());
              }
            }
          }

          if (!exterior.isEmpty()) {
            Coordinates previousCorner = exterior.last();
            for (final Coordinates corner : exterior) {
              addTriangle(new Triangle(point, previousCorner, corner));
              previousCorner = corner;
            }
          }
        }
      }
    }
  }

  public void insertNode(final Point point) {
    final Coordinates coordinate = CoordinatesUtil.get(point);
    insertNode(coordinate);
  }

  public void insertNodes(final Collection<Coordinates> points) {
    for (final Coordinates point : points) {
      insertNode(point);
    }
  }

  public void insertNodes(final CoordinatesList points) {
    for (final Coordinates point : new InPlaceIterator(points)) {
      insertNode(point);
    }
  }

  public void insertNodes(final LineString nodes) {
    final CoordinatesList coordinates = CoordinatesListUtil.get(nodes);
    insertNodes(coordinates);
  }

  private void removeTriangle(final Triangle triangle) {
    if (triangleIndex != null) {
      final Envelope envelope = triangle.getEnvelopeInternal();
      triangleIndex.remove(envelope, triangle);
    }
    if (circumCircleIndex != null) {
      final Circle circumcircle = triangle.getCircumcircle();
      final Envelope envelope = circumcircle.getEnvelopeInternal();
      if (!circumCircleIndex.remove(envelope, triangle)) {
        System.err.println(circumcircle.toGeometry());
        System.err.println(new BoundingBox(GeometryFactory.getFactory(),
          envelope).toPolygon(1));
        System.err.println(triangle);
        circumCircleIndex.remove(envelope, triangle);
      }
    }
  }

  private void replaceTriangle(
    final Triangle triangle,
    final Triangle newTriangle) {
    removeTriangle(triangle);
    addTriangle(newTriangle);
  }

  private void replaceTriangle(
    final Triangle triangle,
    final Triangle... newTriangles) {
    removeTriangle(triangle);
    for (final Triangle newTriangle : newTriangles) {
      addTriangle(newTriangle);
    }
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    if (geometryFactory.getNumAxis() != 3) {
      final int srid = geometryFactory.getSRID();
      final double scaleXY = geometryFactory.getScaleXY();
      this.geometryFactory = GeometryFactory.getFactory(srid, 3, scaleXY, 1);
    } else {
      this.geometryFactory = geometryFactory;
    }
  }
}
