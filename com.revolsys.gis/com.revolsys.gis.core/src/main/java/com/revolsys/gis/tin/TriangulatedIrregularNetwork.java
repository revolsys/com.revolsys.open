package com.revolsys.gis.tin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import com.revolsys.gis.jts.JtsGeometryUtil;
import com.revolsys.gis.jts.LineSegment3D;
import com.revolsys.gis.jts.Triangle;
import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.index.ItemVisitor;
import com.vividsolutions.jts.index.quadtree.Quadtree;

public class TriangulatedIrregularNetwork {
  private static final int[] OPPOSITE_INDEXES = {
    2, 1, 0
  };

  private final Quadtree circumCircleIndex = new Quadtree();

  private final PrecisionModel precisionModel;

  private Quadtree triangleIndex = new Quadtree();

  public TriangulatedIrregularNetwork(
    final GeometryFactory geometryFactory,
    final Envelope envelope) {
    this.precisionModel = geometryFactory.getPrecisionModel();
    final Coordinate c1 = new Coordinate(envelope.getMinX(),
      envelope.getMinY(), 0);
    final Coordinate c2 = new Coordinate(envelope.getMaxX(),
      envelope.getMinY(), 0);
    final Coordinate c3 = new Coordinate(envelope.getMaxX(),
      envelope.getMaxY(), 0);
    final Coordinate c4 = new Coordinate(envelope.getMinX(),
      envelope.getMaxY(), 0);
    addTriangle(Triangle.createClockwiseTriangle(c1, c2, c3));
    addTriangle(Triangle.createClockwiseTriangle(c1, c3, c4));
  }

  public TriangulatedIrregularNetwork(
    final GeometryFactory geometryFactory,
    final Polygon polygon) {
    this.precisionModel = geometryFactory.getPrecisionModel();
    final CoordinateSequence coords = polygon.getExteriorRing()
      .getCoordinateSequence();
    addTriangle(Triangle.createClockwiseTriangle(coords.getCoordinate(0),
      coords.getCoordinate(1), coords.getCoordinate(2)));
  }

  public TriangulatedIrregularNetwork(
    final GeometryFactory geometryFactory,
    final Triangle triangle) {
    this.precisionModel = geometryFactory.getPrecisionModel();

    addTriangle(triangle);
  }

  private void addBreaklineIntersect(
    final Triangle triangle,
    final Coordinate intersectCoord) {
    final Coordinate[] triCoords = triangle.getCoordinates();
    Coordinate previousCoord = triCoords[0];
    for (int i = 1; i < triCoords.length; i++) {
      final Coordinate triCorner = triCoords[i];
      if (!triCorner.equals2D(intersectCoord)
        && !previousCoord.equals2D(intersectCoord)) {
        final double distance = new LineSegment(previousCoord, triCorner).distance(intersectCoord);
        if (distance == 0) {
          final Coordinate nextCoordinate = triCoords[(i + 1) % 3];
          replaceTriangle(triangle, new Triangle[] {
            Triangle.createClockwiseTriangle(intersectCoord, triCorner,
              nextCoordinate),
            Triangle.createClockwiseTriangle(intersectCoord, nextCoordinate,
              previousCoord)
          });
        }
      }
      previousCoord = triCorner;
    }
  }

  private void addBreaklineIntersect(
    final Triangle triangle,
    final LineSegment intersectLine) {
    final Coordinate lc0 = intersectLine.p0;
    final Coordinate lc1 = intersectLine.p1;
    double startCornerDistance = Double.MAX_VALUE;
    double startEdgeDistance = Double.MAX_VALUE;
    double endEdgeDistance = Double.MAX_VALUE;
    double endCornerDistance = Double.MAX_VALUE;
    int startClosestCorner = -1;
    int endClosestCorner = -1;
    int startClosestEdge = -1;
    int endClosestEdge = -1;
    final Coordinate[] triCoords = triangle.getCoordinates();
    for (int i = 0; i < triCoords.length; i++) {
      final Coordinate corner = triCoords[i];
      final Coordinate nextCorner = triCoords[(i + 1) % 3];

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
        final Triangle newTriangle = Triangle.createClockwiseTriangle(lc0, lc1,
          getOtherCoordinate(triCoords, startClosestCorner, endClosestCorner));
        replaceTriangle(triangle, newTriangle);
      } else {
        // Touching start corner
        final double edgeDistance = endEdgeDistance;
        addTriangleTouchingOneCorner(triangle, triCoords, lc0, lc1,
          startClosestCorner, endClosestEdge, edgeDistance);
      }
    } else if (endCornerDistance < 0.01) {
      // Touching end corner
      final double edgeDistance = startEdgeDistance;
      addTriangleTouchingOneCorner(triangle, triCoords, lc1, lc0,
        endClosestCorner, startClosestEdge, edgeDistance);
    } else if (startEdgeDistance < 0.01) {
      if (endEdgeDistance < 0.01) {
        addTriangleTouchingTwoEdges(triangle, triCoords, lc0, lc1,
          startClosestEdge, endClosestEdge);
      } else {
        addTriangleTouchingOneEdge(triangle, triCoords, lc0, lc1,
          startClosestEdge);
      }
    } else if (endEdgeDistance < 0.01) {
      addTriangleTouchingOneEdge(triangle, triCoords, lc1, lc0, endClosestEdge);

    } else {
      if (startCornerDistance <= endCornerDistance) {
        addContainedLine(triangle, triCoords, startClosestCorner, lc0, lc1);
      } else {
        addContainedLine(triangle, triCoords, endClosestCorner, lc1, lc0);
      }

    }
  }

  private void addBreaklineItersect(
    final Triangle triangle,
    final LineSegment3D breakline,
    final LineSegment intersectLine) {
    final Coordinate lc0 = intersectLine.p0;
    final Coordinate lc1 = intersectLine.p1;
    JtsGeometryUtil.addElevation(precisionModel, lc0, breakline);
    JtsGeometryUtil.addElevation(precisionModel, lc1, breakline);
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
    final Coordinate[] coordinates,
    final int index,
    final Coordinate l0,
    final Coordinate l1) {
    final Coordinate t0 = coordinates[index];
    final Coordinate t1 = coordinates[(index + 1) % 3];
    final Coordinate t2 = coordinates[(index + 2) % 3];

    final int c0i0i1Orientation = CGAlgorithms.computeOrientation(t0, l0, l1);
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

  private void addTrangleCornerAndEdgeTouch(
    final Triangle triangle,
    final Coordinate cPrevious,
    final Coordinate c,
    final Coordinate cNext,
    final Coordinate cOpposite) {
    replaceTriangle(triangle, new Triangle[] {
      Triangle.createClockwiseTriangle(cPrevious, c, cOpposite),
      Triangle.createClockwiseTriangle(c, cNext, cOpposite)
    });
  }

  private void addTriangle(
    final Triangle triangle) {
    final Circle circle = triangle.getCircumcircle();
    circumCircleIndex.insert(circle.getEnvelopeInternal(), triangle);
    // triangleIndex.insert(triangle.getEnvelopeInternal(), triangle);
  }

  private void addTriangleCorderEdge(
    final Triangle triangle,
    final Coordinate[] coords,
    final Coordinate lc0,
    final Coordinate lc1,
    final int startCorner,
    final int startEdge) {
    final Coordinate cNext = coords[(startCorner + 1) % 3];
    final Coordinate cPrevious = coords[(startCorner + 2) % 3];
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
    final Coordinate t0,
    final Coordinate t1,
    final Coordinate t2,
    final Coordinate l0,
    final Coordinate l1) {
    replaceTriangle(triangle, new Triangle[] {
      Triangle.createClockwiseTriangle(t0, t1, l0),
      Triangle.createClockwiseTriangle(l0, t1, l1),
      Triangle.createClockwiseTriangle(l1, t1, t2),
      Triangle.createClockwiseTriangle(l0, l1, t2),
      Triangle.createClockwiseTriangle(t0, l0, t2)
    });
  }

  private void addTriangleStartCornerEndInside(
    final Triangle triangle,
    final Coordinate[] coords,
    final int cornerIndex,
    final Coordinate cCorner,
    final Coordinate cInside) {
    final Coordinate cNext = coords[(cornerIndex + 1) % 3];
    final Coordinate cPrevious = coords[(cornerIndex + 2) % 3];
    replaceTriangle(triangle, new Triangle[] {
      Triangle.createClockwiseTriangle(cCorner, cNext, cInside),
      Triangle.createClockwiseTriangle(cInside, cNext, cPrevious),
      Triangle.createClockwiseTriangle(cInside, cPrevious, cCorner)
    });
  }

  private void addTriangleTouchingOneCorner(
    final Triangle triangle,
    final Coordinate[] coords,
    final Coordinate lc0,
    final Coordinate lc1,
    final int startCorner,
    final int endEdge,
    final double endEdgeDistance) {
    if (endEdgeDistance < 1) {
      addTriangleCorderEdge(triangle, coords, lc0, lc1, startCorner, endEdge);
    } else {
      addTriangleStartCornerEndInside(triangle, coords, startCorner, lc0, lc1);
    }
  }

  private void addTriangleTouchingOneEdge(
    final Triangle triangle,
    final Coordinate[] coords,
    final Coordinate lc0,
    final Coordinate lc1,
    final int edgeIndex) {
    final Coordinate cPrevious = coords[(edgeIndex) % 3];
    final Coordinate cNext = coords[(edgeIndex + 1) % 3];
    final Coordinate cOpposite = coords[(edgeIndex + 2) % 3];
    if (CGAlgorithms.computeOrientation(cPrevious, lc0, lc1) == CGAlgorithms.COLLINEAR) {
      replaceTriangle(triangle, new Triangle[] {
        Triangle.createClockwiseTriangle(cPrevious, lc0, cOpposite),
        Triangle.createClockwiseTriangle(cOpposite, lc0, lc1),
        Triangle.createClockwiseTriangle(cOpposite, lc1, cNext),
        Triangle.createClockwiseTriangle(lc0, lc1, cNext)
      });
    } else {
      replaceTriangle(triangle, new Triangle[] {
        Triangle.createClockwiseTriangle(cPrevious, lc0, lc1),
        Triangle.createClockwiseTriangle(cNext, lc0, lc1),
        Triangle.createClockwiseTriangle(cNext, lc1, cOpposite),
        Triangle.createClockwiseTriangle(cPrevious, lc1, cOpposite)
      });
    }
  }

  private void addTriangleTouchingTwoEdges(
    final Triangle triangle,
    final Coordinate[] coords,
    final Coordinate lc0,
    final Coordinate lc1,
    final int startEdge,
    final int endEdge) {
    final Coordinate cPrevious = coords[startEdge];
    final Coordinate cNext = coords[(startEdge + 1) % 3];
    final Coordinate cOpposite = coords[(startEdge + 2) % 3];
    if (startEdge == endEdge) {
      if (cPrevious.distance(lc0) < cPrevious.distance(lc1)) {
        replaceTriangle(triangle, new Triangle[] {
          Triangle.createClockwiseTriangle(cPrevious, lc0, cOpposite),
          Triangle.createClockwiseTriangle(lc0, lc1, cOpposite),
          Triangle.createClockwiseTriangle(lc1, cNext, cOpposite),
        });
      } else {
        replaceTriangle(triangle, new Triangle[] {
          Triangle.createClockwiseTriangle(cPrevious, lc1, cOpposite),
          Triangle.createClockwiseTriangle(lc0, lc1, cOpposite),
          Triangle.createClockwiseTriangle(lc0, cNext, cOpposite)
        });
      }
    } else if (endEdge == ((startEdge + 1) % 3)) {
      replaceTriangle(triangle, new Triangle[] {
        Triangle.createClockwiseTriangle(cPrevious, lc0, cOpposite),
        Triangle.createClockwiseTriangle(lc0, lc1, cOpposite),
        Triangle.createClockwiseTriangle(lc0, cNext, lc1)
      });
    } else {
      replaceTriangle(triangle, new Triangle[] {
        Triangle.createClockwiseTriangle(cPrevious, lc0, lc1),
        Triangle.createClockwiseTriangle(lc0, cNext, lc1),
        Triangle.createClockwiseTriangle(lc1, cNext, cOpposite)
      });
    }
  }

  @SuppressWarnings("unchecked")
  public void buildIndex() {
    triangleIndex = new Quadtree();
    for (final Triangle triangle : (List<Triangle>)circumCircleIndex.queryAll()) {
      triangleIndex.insert(triangle.getEnvelopeInternal(), triangle);
    }
  }

  public Circle getCircle(
    final Polygon polygon) {
    final CoordinateSequence coordinates = polygon.getExteriorRing()
      .getCoordinateSequence();
    final Coordinate a = coordinates.getCoordinate(0);
    final Coordinate b = coordinates.getCoordinate(1);
    final Coordinate c = coordinates.getCoordinate(2);
    final double angleB = Angle.angleBetween(a, b, c);

    final double radius = a.distance(c) / Math.sin(angleB) * 0.5;
    final Coordinate coordinate = com.vividsolutions.jts.geom.Triangle.circumcentre(
      a, b, c);
    return new Circle(coordinate, radius);
  }

  public double getElevation(
    final Coordinate coordinate) {
    final List<Triangle> triangles = getTriangles(coordinate);
    for (final Triangle triangle : triangles) {
      final Coordinate t0 = triangle.p0;
      final Coordinate t1 = triangle.p1;
      final Coordinate t2 = triangle.p2;
      Coordinate closestCorner = t0;
      LineSegment3D oppositeEdge = new LineSegment3D(t1, t2);
      double closestDistance = coordinate.distance(closestCorner);
      final double t1Distance = coordinate.distance(t1);
      if (closestDistance > t1Distance) {
        closestCorner = t1;
        oppositeEdge = new LineSegment3D(t2, t0);
        closestDistance = t1Distance;
      }
      if (closestDistance > coordinate.distance(t2)) {
        closestCorner = t2;
        oppositeEdge = new LineSegment3D(t0, t1);
      }
      LineSegment segment = JtsGeometryUtil.addLength(new LineSegment(
        closestCorner, coordinate), 0, t0.distance(t1) + t1.distance(t2)
        + t0.distance(t2));
      final Coordinate intersectCoordinate = oppositeEdge.intersection3D(segment);
      if (intersectCoordinate != null) {
        segment = new LineSegment(t0, intersectCoordinate);

        return JtsGeometryUtil.getElevation(segment, coordinate);
      }
    }
    return Double.NaN;
  }

  private Coordinate getOtherCoordinate(
    final Coordinate[] coords,
    final int i1,
    final int i2) {
    final int index = getOtherIndex(i1, i2);
    return coords[index];
  }

  /**
   * Get the index of the corner or a triangle opposite corners i1 -> i2. i1 and
   * i2 must have different values in the range 0..2.
   * 
   * @param i1
   * @param i2
   * @return
   */
  private int getOtherIndex(
    final int i1,
    final int i2) {
    return OPPOSITE_INDEXES[i1 + i2 - 1];
  }

  @SuppressWarnings("unchecked")
  public List<Triangle> getTriangles() {
    return triangleIndex.queryAll();

  }

  public List<Triangle> getTriangles(
    final Coordinate coordinate) {
    final Envelope envelope = new Envelope(coordinate);
    final List<Triangle> triangles = new ArrayList<Triangle>();
    triangleIndex.query(envelope, new ItemVisitor() {
      public void visitItem(
        final Object object) {
        final Triangle triangle = (Triangle)object;
        if (triangle.contains(coordinate)) {
          triangles.add(triangle);
        }
      }
    });
    return triangles;
  }

  public List<Triangle> getTriangles(
    final Envelope envelope) {
    final List<Triangle> triangles = new ArrayList<Triangle>();
    triangleIndex.query(envelope, new ItemVisitor() {
      public void visitItem(
        final Object object) {
        final Triangle triangle = (Triangle)object;
        final Envelope triangleEnvelope = triangle.getEnvelopeInternal();
        if (triangleEnvelope.intersects(envelope)) {
          triangles.add(triangle);
        }
      }
    });
    return triangles;
  }

  public List<Triangle> getTriangles(
    final LineSegment segment) {
    final Envelope envelope = new Envelope(segment.p0, segment.p1);
    return getTriangles(envelope);
  }

  public void insertEdge(
    final LineSegment3D breakline) {
    final List<Triangle> triangles = getTriangles(breakline);
    for (final Triangle triangle : triangles) {
      final LineSegment intersection = triangle.intersection(breakline);
      if (intersection != null) {
        final double length = intersection.getLength();
        if (length < 0.01) {
          addBreaklineIntersect(triangle, intersection.p0);
        } else {
          addBreaklineItersect(triangle, breakline, intersection);
        }
      }
    }
  }

  public void insertEdge(
    final LineString breakline) {
    final CoordinateSequence coordinates = breakline.getCoordinateSequence();
    Coordinate previousCoordinate = coordinates.getCoordinate(0);
    for (int i = 1; i < coordinates.size(); i++) {
      final Coordinate coordinate = coordinates.getCoordinate(i);
      final LineSegment3D segment = new LineSegment3D(previousCoordinate,
        coordinate);
      insertEdge(segment);
      previousCoordinate = coordinate;
    }
  }

  @SuppressWarnings("unchecked")
  public void insertNode(
    final Coordinate coordinate) {
    final List<Triangle> triangles = circumCircleIndex.query(new Envelope(
      coordinate));
    if (!triangles.isEmpty()) {
      final TreeSet<Coordinate> exterior = new TreeSet<Coordinate>(
        new Comparator<Coordinate>() {
          public int compare(
            final Coordinate c1,
            final Coordinate c2) {
            final double angleC1 = Angle.angle(coordinate, c1);
            final double angleC2 = Angle.angle(coordinate, c2);
            if (angleC1 < angleC2) {
              return 1;
            } else if (angleC1 > angleC2) {
              return -1;
            } else {
              return 0;
            }
          }
        });
      for (final Triangle triangle : triangles) {
        final Circle circle = triangle.getCircumcircle();
        if (circle.contains(coordinate)) {
          removeTriangle(triangle);
          if (!coordinate.equals2D(triangle.p0)) {
            exterior.add(triangle.p0);
          }
          if (!coordinate.equals2D(triangle.p1)) {
            exterior.add(triangle.p1);
          }
          if (!coordinate.equals2D(triangle.p2)) {
            exterior.add(triangle.p2);
          }
        }
      }

      if (!exterior.isEmpty()) {
        Coordinate previousCorner = exterior.last();
        for (final Coordinate corner : exterior) {
          addTriangle(new Triangle(coordinate, previousCorner, corner));
          previousCorner = corner;
        }
      }
    }
  }

  public void insertNode(
    final Point point) {
    final Coordinate coordinate = point.getCoordinate();
    insertNode(coordinate);
  }

  public void insertNodes(
    final LineString nodes) {
    final CoordinateSequence coordinates = nodes.getCoordinateSequence();
    for (int i = 0; i < coordinates.size(); i++) {
      final Coordinate coordinate = coordinates.getCoordinate(i);
      insertNode(coordinate);
    }
  }

  private void removeTriangle(
    final Triangle triangle) {
    circumCircleIndex.remove(triangle.getCircumcircle().getEnvelopeInternal(),
      triangle);
    // triangleIndex.remove(triangle.getEnvelopeInternal(), triangle);
  }

  private void replaceTriangle(
    final Triangle triangle,
    final Triangle newTriangle) {
    removeTriangle(triangle);
    addTriangle(newTriangle);
  }

  private void replaceTriangle(
    final Triangle triangle,
    final Triangle[] newTriangles) {
    removeTriangle(triangle);
    for (final Triangle newTriangle : newTriangles) {
      addTriangle(newTriangle);
    }
  }
}
