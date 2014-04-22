package com.revolsys.jtstest.testbuilder.io.shapefile;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import com.revolsys.jts.algorithm.CGAlgorithms;
import com.revolsys.jts.algorithm.RobustCGAlgorithms;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Polygon;

/**
 * Wrapper for a Shapefile polygon.
 */
public class PolygonHandler implements ShapeHandler {
  protected static CGAlgorithms cga = new RobustCGAlgorithms();

  /**
   * Finds a object in a list. Should be much faster than indexof
   * 
   * @param list
   * @param o
   * @return
   */
  private static int findIndex(final List<LinearRing> list, final Object o) {
    final int n = list.size();
    for (int i = 0; i < n; i++) {
      if (list.get(i) == o) {
        return i;
      }
    }
    return -1;
  }

  int myShapeType;

  public PolygonHandler() {
    myShapeType = 5;
  }

  public PolygonHandler(final int type) throws InvalidShapefileException {
    if ((type != 5) && (type != 15) && (type != 25)) {
      throw new InvalidShapefileException(
        "PolygonHandler constructor - expected type to be 5, 15, or 25.");
    }

    myShapeType = type;
  }

  private List<List<LinearRing>> assignHolesToShells(
    final List<LinearRing> shells, final List<LinearRing> holes) {
    // now we have a list of all shells and all holes
    final ArrayList holesForShells = new ArrayList(shells.size());
    for (int i = 0; i < shells.size(); i++) {
      holesForShells.add(new ArrayList());
    }

    // find homes
    for (int i = 0; i < holes.size(); i++) {
      final LinearRing testHole = holes.get(i);
      LinearRing minShell = null;
      BoundingBox minEnv = null;
      final BoundingBox testHoleEnv = testHole.getBoundingBox();
      final Coordinates testHolePt = testHole.getCoordinate(0);
      LinearRing tryShell;
      final int nShells = shells.size();
      for (int j = 0; j < nShells; j++) {
        tryShell = shells.get(j);
        final BoundingBox tryShellEnv = tryShell.getBoundingBox();
        if (!tryShellEnv.contains(testHoleEnv)) {
          continue;
        }

        boolean isContained = false;
        final Coordinates[] coordList = tryShell.getCoordinateArray();

        if (nShells <= 1 || CGAlgorithms.isPointInRing(testHolePt, coordList)
          || pointInList(testHolePt, coordList)) {
          isContained = true;
        }

        // check if new containing ring is smaller than the current minimum ring
        if (minShell != null) {
          minEnv = minShell.getBoundingBox();
        }
        if (isContained) {
          if (minShell == null || minEnv.contains(tryShellEnv)) {
            minShell = tryShell;
          }
        }
      }

      if (minShell == null) {
        System.err.println("Found polygon with a hole not inside a shell");
      } else {
        // ((ArrayList)holesForShells.get(shells.indexOf(minShell))).add(testRing);
        ((ArrayList)holesForShells.get(findIndex(shells, minShell))).add(testHole);
      }
    }
    return holesForShells;
  }

  @Override
  public int getLength(final Geometry geometry) {

    MultiPolygon multi;
    if (geometry instanceof MultiPolygon) {
      multi = (MultiPolygon)geometry;
    } else {
      multi = geometry.getGeometryFactory().createMultiPolygon(
        (Polygon)geometry);
    }

    int nrings = 0;

    for (int t = 0; t < multi.getGeometryCount(); t++) {
      Polygon p;
      p = (Polygon)multi.getGeometry(t);
      nrings = nrings + 1 + p.getNumInteriorRing();
    }

    final int npoints = multi.getVertexCount();

    if (myShapeType == 15) {
      return 22 + (2 * nrings) + 8 * npoints + 4 * npoints + 8 + 4 * npoints
        + 8;
    }
    if (myShapeType == 25) {
      return 22 + (2 * nrings) + 8 * npoints + 4 * npoints + 8;
    }

    return 22 + (2 * nrings) + 8 * npoints;
  }

  @Override
  public int getShapeType() {
    return myShapeType;
  }

  // returns true if testPoint is a point in the pointList list.
  boolean pointInList(final Coordinates testPoint, final Coordinates[] pointList) {
    int t, numpoints;
    Coordinates p;

    numpoints = Array.getLength(pointList);
    for (t = 0; t < numpoints; t++) {
      p = pointList[t];
      if ((testPoint.getX() == p.getX())
        && (testPoint.getY() == p.getY())
        && ((testPoint.getZ() == p.getZ()) || (!(testPoint.getZ() == testPoint.getZ()))) // nan
      // test;
      // x!=x iff
      // x is nan
      ) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Geometry read(final EndianDataInputStream file,
    final GeometryFactory geometryFactory, final int contentLength)
    throws IOException, InvalidShapefileException {

    int actualReadWords = 0; // actual number of words read (word = 16bits)

    // file.setLittleEndianMode(true);
    final int shapeType = file.readIntLE();
    actualReadWords += 2;

    if (shapeType == 0) {
      return GeometryFactory.getFactory().createMultiPolygon((Polygon[])null); // null
                                                                               // shape
    }

    if (shapeType != myShapeType) {
      throw new InvalidShapefileException(
        "PolygonHandler.read() - got shape type " + shapeType
          + " but was expecting " + myShapeType);
    }

    // bounds
    file.readDoubleLE();
    file.readDoubleLE();
    file.readDoubleLE();
    file.readDoubleLE();

    actualReadWords += 4 * 4;

    int partOffsets[];

    final int numParts = file.readIntLE();
    final int numPoints = file.readIntLE();
    actualReadWords += 4;

    partOffsets = new int[numParts];

    for (int i = 0; i < numParts; i++) {
      partOffsets[i] = file.readIntLE();
      actualReadWords += 2;
    }

    // LinearRing[] rings = new LinearRing[numParts];
    List<LinearRing> shells = new ArrayList<>();
    List<LinearRing> holes = new ArrayList<>();
    final Coordinates[] coords = new Coordinates[numPoints];

    for (int t = 0; t < numPoints; t++) {
      coords[t] = new Coordinate(file.readDoubleLE(), file.readDoubleLE());
      actualReadWords += 8;
    }

    if (myShapeType == 15) {
      // z
      file.readDoubleLE(); // zmin
      file.readDoubleLE(); // zmax
      actualReadWords += 8;
      for (int t = 0; t < numPoints; t++) {
        coords[t].setZ(file.readDoubleLE());
        actualReadWords += 4;
      }
    }

    if (myShapeType >= 15) {
      // int fullLength = 22 + (2*numParts) + (8*numPoints) + 8 + (4*numPoints)+
      // 8 + (4*numPoints);
      int fullLength;
      if (myShapeType == 15) {
        // polyZ (with M)
        fullLength = 22 + (2 * numParts) + (8 * numPoints) + 8
          + (4 * numPoints) + 8 + (4 * numPoints);
      } else {
        // polyM (with M)
        fullLength = 22 + (2 * numParts) + (8 * numPoints) + 8
          + (4 * numPoints);
      }
      if (contentLength >= fullLength) {
        file.readDoubleLE(); // mmin
        file.readDoubleLE(); // mmax
        actualReadWords += 8;
        for (int t = 0; t < numPoints; t++) {
          file.readDoubleLE();
          actualReadWords += 4;
        }
      }
    }

    // verify that we have read everything we need
    while (actualReadWords < contentLength) {
      final int junk = file.readShortBE();
      actualReadWords += 1;
    }

    int offset = 0;
    int start, finish, length;
    for (int part = 0; part < numParts; part++) {
      start = partOffsets[part];
      if (part == numParts - 1) {
        finish = numPoints;
      } else {
        finish = partOffsets[part + 1];
      }
      length = finish - start;
      final Coordinates points[] = new Coordinates[length];
      for (int i = 0; i < length; i++) {
        points[i] = coords[offset];
        offset++;
      }
      final LinearRing ring = geometryFactory.linearRing(points);
      if (cga.isCCW(points)) {
        holes.add(ring);
      } else {
        shells.add(ring);
      }
    }

    List<List<LinearRing>> holesForShells = assignHolesToShells(shells, holes);

    final Polygon[] polygons = new Polygon[shells.size()];
    for (int i = 0; i < shells.size(); i++) {
      final LinearRing shell = shells.get(i);
      final List<LinearRing> holesForShell = holesForShells.get(i);
      final List<LinearRing> rings = new ArrayList<LinearRing>();
      rings.add(shell);
      rings.addAll(holesForShell);
      polygons[i] = geometryFactory.polygon(rings);
    }

    if (polygons.length == 1) {
      return polygons[0];
    }

    holesForShells = null;
    shells = null;
    holes = null;
    // its a multi part

    final Geometry result = geometryFactory.createMultiPolygon(polygons);
    // if (!(result.isValid() ))
    // System.out.println("geom isnt valid");
    return result;
  }

  double[] zMinMax(final Geometry g) {
    double zmin, zmax;
    boolean validZFound = false;
    final Coordinates[] cs = g.getCoordinateArray();
    final double[] result = new double[2];

    zmin = Double.NaN;
    zmax = Double.NaN;
    double z;

    for (int t = 0; t < cs.length; t++) {
      z = cs[t].getZ();
      if (!(Double.isNaN(z))) {
        if (validZFound) {
          if (z < zmin) {
            zmin = z;
          }
          if (z > zmax) {
            zmax = z;
          }
        } else {
          validZFound = true;
          zmin = z;
          zmax = z;
        }
      }

    }

    result[0] = (zmin);
    result[1] = (zmax);
    return result;

  }

}

/*
 * $Log: PolygonHandler.java,v $ Revision 1.1 2009/10/14 04:21:22 mbdavis added
 * drag-n-drop for reading shp files Revision 1.5 2003/09/23 17:15:26 dblasby
 * *** empty log message *** Revision 1.4 2003/07/25 18:49:15 dblasby Allow
 * "extra" data after the content. Fixes the ICI shapefile bug. Revision 1.3
 * 2003/02/04 02:10:37 jaquino Feature: EditWMSQuery dialog Revision 1.2
 * 2003/01/22 18:31:05 jaquino Enh: Make About Box configurable Revision 1.2
 * 2002/09/09 20:46:22 dblasby Removed LEDatastream refs and replaced with
 * EndianData[in/out]putstream Revision 1.1 2002/08/27 21:04:58 dblasby orginal
 * Revision 1.3 2002/03/05 10:51:01 andyt removed use of factory from write
 * method Revision 1.2 2002/03/05 10:23:59 jmacgill made sure geometries were
 * created using the factory methods Revision 1.1 2002/02/28 00:38:50 jmacgill
 * Renamed files to more intuitve names Revision 1.4 2002/02/13 00:23:53
 * jmacgill First semi working JTS version of Shapefile code Revision 1.3
 * 2002/02/11 18:44:22 jmacgill replaced geometry constructions with calls to
 * geometryFactory.createX methods Revision 1.2 2002/02/11 18:28:41 jmacgill
 * rewrote to have static read and write methods Revision 1.1 2002/02/11
 * 16:54:43 jmacgill added shapefile code and directories
 */
