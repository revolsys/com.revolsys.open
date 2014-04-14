/*
 * MultiPointHandler.java
 *
 * Created on July 17, 2002, 4:13 PM
 */

package com.revolsys.jtstest.testbuilder.io.shapefile;

import java.io.IOException;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.MultiPoint;

/**
 *
 * @author  dblasby
 */
public class MultiPointHandler implements ShapeHandler {
  int myShapeType = -1;

  /** Creates new MultiPointHandler */
  public MultiPointHandler() {
    myShapeType = 8;
  }

  public MultiPointHandler(final int type) throws InvalidShapefileException {
    if ((type != 8) && (type != 18) && (type != 28)) {
      throw new InvalidShapefileException(
        "Multipointhandler constructor - expected type to be 8, 18, or 28");
    }

    myShapeType = type;
  }

  /**
   * Calcuates the record length of this object.
   * @return int The length of the record that this shapepoint will take up in a shapefile
   **/
  @Override
  public int getLength(final Geometry geometry) {
    final MultiPoint mp = (MultiPoint)geometry;

    if (myShapeType == 8) {
      return mp.getNumGeometries() * 8 + 20;
    }
    if (myShapeType == 28) {
      return mp.getNumGeometries() * 8 + 20 + 8 + 4 * mp.getNumGeometries();
    }

    return mp.getNumGeometries() * 8 + 20 + 8 + 4 * mp.getNumGeometries() + 8
      + 4 * mp.getNumGeometries();
  }

  /**
   * Returns the shapefile shape type value for a point
   * @return int Shapefile.POINT
   */
  @Override
  public int getShapeType() {
    return myShapeType;
  }

  @Override
  public Geometry read(final EndianDataInputStream file,
    final GeometryFactory geometryFactory, final int contentLength)
    throws IOException, InvalidShapefileException {
    // file.setLittleEndianMode(true);

    int actualReadWords = 0; // actual number of words read (word = 16bits)

    final int shapeType = file.readIntLE();
    actualReadWords += 2;

    if (shapeType == 0) {
      return GeometryFactory.getFactory().createMultiPoint();
    }
    if (shapeType != myShapeType) {
      throw new InvalidShapefileException(
        "Multipointhandler.read() - expected type code " + myShapeType
          + " but got " + shapeType);
    }
    // read bbox
    file.readDoubleLE();
    file.readDoubleLE();
    file.readDoubleLE();
    file.readDoubleLE();

    actualReadWords += 4 * 4;

    final int numpoints = file.readIntLE();
    actualReadWords += 2;

    final Coordinates[] coords = new Coordinates[numpoints];
    for (int t = 0; t < numpoints; t++) {

      final double x = file.readDoubleLE();
      final double y = file.readDoubleLE();
      actualReadWords += 8;
      coords[t] = new Coordinate(x, y);
    }
    if (myShapeType == 18) {
      file.readDoubleLE(); // z min/max
      file.readDoubleLE();
      actualReadWords += 8;
      for (int t = 0; t < numpoints; t++) {
        final double z = file.readDoubleLE();// z
        actualReadWords += 4;
        coords[t].setZ(z);
      }
    }

    if (myShapeType >= 18) {
      // int fullLength = numpoints * 8 + 20 +8 +4*numpoints + 8 +4*numpoints;
      int fullLength;
      if (myShapeType == 18) {
        // multipoint Z (with m)
        fullLength = 20 + (numpoints * 8) + 8 + 4 * numpoints + 8 + 4
          * numpoints;
      } else {
        // multipoint M (with M)
        fullLength = 20 + (numpoints * 8) + 8 + 4 * numpoints;
      }

      if (contentLength >= fullLength) // is the M portion actually there?
      {
        file.readDoubleLE(); // m min/max
        file.readDoubleLE();
        actualReadWords += 8;
        for (int t = 0; t < numpoints; t++) {
          file.readDoubleLE();// m
          actualReadWords += 4;
        }
      }
    }

    // verify that we have read everything we need
    while (actualReadWords < contentLength) {
      final int junk2 = file.readShortBE();
      actualReadWords += 1;
    }

    return geometryFactory.createMultiPoint(coords);
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
