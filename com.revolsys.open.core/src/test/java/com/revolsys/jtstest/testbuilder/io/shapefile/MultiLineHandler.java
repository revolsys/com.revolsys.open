package com.revolsys.jtstest.testbuilder.io.shapefile;

import java.io.IOException;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.MultiLineString;

/**
 * Wrapper for a Shapefile arc.
 */
public class MultiLineHandler implements ShapeHandler {

  int myShapeType = -1;

  public MultiLineHandler() {
    myShapeType = 3;
  }

  public MultiLineHandler(final int type) throws InvalidShapefileException {
    if ((type != 3) && (type != 13) && (type != 23)) {
      throw new InvalidShapefileException(
        "MultiLineHandler constructor - expected type to be 3,13 or 23");
    }

    myShapeType = type;
  }

  @Override
  public int getLength(final Geometry geometry) {
    final MultiLineString multi = (MultiLineString)geometry;

    int numlines, numpoints;

    numlines = multi.getGeometryCount();
    numpoints = multi.getVertexCount();

    if (myShapeType == 3) {
      return 22 + 2 * numlines + (numpoints * 8);
    }
    if (myShapeType == 23) {
      return 22 + 2 * numlines + (numpoints * 8) + 4 + 4 + 4 * numpoints;
    }

    return 22 + 2 * numlines + (numpoints * 8) + 4 + 4 + 4 * numpoints + 4 + 4
      + 4 * numpoints;

    // return 22 + 2*numlines + (numpoints * 8);

    // return (44+(4*((GeometryCollection)geometry).getNumGeometries()));
  }

  /**
   * Get the type of shape stored (Shapefile.ARC)
   */
  @Override
  public int getShapeType() {
    return myShapeType;
  }

  @Override
  public Geometry read(final EndianDataInputStream file,
    final GeometryFactory geometryFactory, final int contentLength)
    throws IOException, InvalidShapefileException {

    double junk;
    int actualReadWords = 0; // actual number of words read (word = 16bits)

    // file.setLittleEndianMode(true);

    final int shapeType = file.readIntLE();
    actualReadWords += 2;

    if (shapeType == 0) {
      return GeometryFactory.getFactory().multiLineString(
        (LineString[])null); // null shape
    }

    if (shapeType != myShapeType) {
      throw new InvalidShapefileException(
        "MultilineHandler.read()  - file says its type " + shapeType
          + " but i'm expecting type " + myShapeType);
    }

    // read bounding box (not needed)
    junk = file.readDoubleLE();
    junk = file.readDoubleLE();
    junk = file.readDoubleLE();
    junk = file.readDoubleLE();
    actualReadWords += 4 * 4;

    final int numParts = file.readIntLE();
    final int numPoints = file.readIntLE();// total number of points
    actualReadWords += 4;

    final int[] partOffsets = new int[numParts];

    // points = new Coordinates[numPoints];

    for (int i = 0; i < numParts; i++) {
      partOffsets[i] = file.readIntLE();
      actualReadWords += 2;
    }

    final LineString lines[] = new LineString[numParts];
    final Coordinates[] coords = new Coordinates[numPoints];

    for (int t = 0; t < numPoints; t++) {
      coords[t] = new Coordinate(file.readDoubleLE(), file.readDoubleLE());
      actualReadWords += 8;
    }

    if (myShapeType == 13) {
      junk = file.readDoubleLE(); // z min, max
      junk = file.readDoubleLE();
      actualReadWords += 8;

      for (int t = 0; t < numPoints; t++) {
        coords[t].setZ(file.readDoubleLE()); // z value
        actualReadWords += 4;
      }
    }

    if (myShapeType >= 13) {
      // int fullLength = 22 + 2*numParts + (numPoints * 8) + 4+4+4*numPoints+
      // 4+4+4*numPoints;
      int fullLength;
      if (myShapeType == 13) {
        // polylineZ (with M)
        fullLength = 22 + 2 * numParts + (numPoints * 8) + 4 + 4 + 4
          * numPoints + 4 + 4 + 4 * numPoints;
      } else {
        // polylineM (with M)
        fullLength = 22 + 2 * numParts + (numPoints * 8) + 4 + 4 + 4
          * numPoints;
      }
      if (contentLength >= fullLength) // are ms actually there?
      {
        junk = file.readDoubleLE(); // m min, max
        junk = file.readDoubleLE();
        actualReadWords += 8;

        for (int t = 0; t < numPoints; t++) {
          junk = file.readDoubleLE(); // m value
          actualReadWords += 4;
        }
      }
    }

    // verify that we have read everything we need
    while (actualReadWords < contentLength) {
      final int junk2 = file.readShortBE();
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
      lines[part] = geometryFactory.lineString(points);

    }
    if (numParts == 1) {
      return lines[0];
    } else {
      return geometryFactory.multiLineString(lines);
    }
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
 * $Log: MultiLineHandler.java,v $ Revision 1.1 2009/10/14 04:21:21 mbdavis
 * added drag-n-drop for reading shp files Revision 1.4 2003/07/25 18:49:15
 * dblasby Allow "extra" data after the content. Fixes the ICI shapefile bug.
 * Revision 1.3 2003/02/04 02:10:37 jaquino Feature: EditWMSQuery dialog
 * Revision 1.2 2003/01/22 18:31:05 jaquino Enh: Make About Box configurable
 * Revision 1.3 2002/10/30 22:36:11 dblasby Line reader now returns
 * LINESTRING(..) if there is only one part to the arc polyline. Revision 1.2
 * 2002/09/09 20:46:22 dblasby Removed LEDatastream refs and replaced with
 * EndianData[in/out]putstream Revision 1.1 2002/08/27 21:04:58 dblasby orginal
 * Revision 1.2 2002/03/05 10:23:59 jmacgill made sure geometries were created
 * using the factory methods Revision 1.1 2002/02/28 00:38:50 jmacgill Renamed
 * files to more intuitve names Revision 1.3 2002/02/13 00:23:53 jmacgill First
 * semi working JTS version of Shapefile code Revision 1.2 2002/02/11 18:42:45
 * jmacgill changed read and write statements so that they produce and take
 * Geometry objects instead of specific MultiLine objects changed parts[] array
 * name to partOffsets[] for clarity and consistency with ShapePolygon Revision
 * 1.1 2002/02/11 16:54:43 jmacgill added shapefile code and directories
 */
