/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.jts.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.revolsys.jts.geom.CoordinateSequenceFactory;
import com.revolsys.jts.geom.CoordinateSequences;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.PrecisionModel;

/**
 * Reads a {@link Geometry}from a byte stream in Well-Known Binary format.
 * Supports use of an {@link InStream}, which allows easy use
 * with arbitrary byte stream sources.
 * <p>
 * This class reads the format describe in {@link WKBWriter}.  
 * It also partially handles
 * the <b>Extended WKB</b> format used by PostGIS, 
 * by parsing and storing SRID values.
 * The reader repairs structurally-invalid input
 * (specifically, LineStrings and LinearRings which contain
 * too few points have vertices added,
 * and non-closed rings are closed).
 * <p>
 * This class is designed to support reuse of a single instance to read multiple
 * geometries. This class is not thread-safe; each thread should create its own
 * instance.
 *
 * @see WKBWriter for a formal format specification
 */
public class WKBReader {
  private static final String INVALID_GEOM_TYPE_MSG = "Invalid geometry type encountered in ";

  /**
   * Converts a hexadecimal string to a byte array.
   * The hexadecimal digit symbols are case-insensitive.
   *
   * @param hex a string containing hex digits
   * @return an array of bytes with the value of the hex string
   */
  public static byte[] hexToBytes(final String hex) {
    final int byteLen = hex.length() / 2;
    final byte[] bytes = new byte[byteLen];

    for (int i = 0; i < hex.length() / 2; i++) {
      final int i2 = 2 * i;
      if (i2 + 1 > hex.length()) {
        throw new IllegalArgumentException("Hex string has odd length");
      }

      final int nib1 = hexToInt(hex.charAt(i2));
      final int nib0 = hexToInt(hex.charAt(i2 + 1));
      final byte b = (byte)((nib1 << 4) + (byte)nib0);
      bytes[i] = b;
    }
    return bytes;
  }

  private static int hexToInt(final char hex) {
    final int nib = Character.digit(hex, 16);
    if (nib < 0) {
      throw new IllegalArgumentException("Invalid hex digit: '" + hex + "'");
    }
    return nib;
  }

  private final GeometryFactory factory;

  private final CoordinateSequenceFactory csFactory;

  private final PrecisionModel precisionModel;

  // default dimension - will be set on read
  private int inputDimension = 2;

  private boolean hasSRID = false;

  private final int SRID = 0;

  /**
   * true if structurally invalid input should be reported rather than repaired.
   * At some point this could be made client-controllable.
   */
  private final boolean isStrict = false;

  private final ByteOrderDataInStream dis = new ByteOrderDataInStream();

  private double[] ordValues;

  public WKBReader() {
    this(GeometryFactory.getFactory());
  }

  public WKBReader(final GeometryFactory geometryFactory) {
    this.factory = geometryFactory;
    precisionModel = factory.getPrecisionModel();
    csFactory = factory.getCoordinateSequenceFactory();
  }

  /**
   * Reads a single {@link Geometry} in WKB format from a byte array.
   *
   * @param bytes the byte array to read from
   * @return the geometry read
   * @throws ParseException if the WKB is ill-formed
   */
  public Geometry read(final byte[] bytes) throws ParseException {
    // possibly reuse the ByteArrayInStream?
    // don't throw IOExceptions, since we are not doing any I/O
    try {
      return read(new ByteArrayInStream(bytes));
    } catch (final IOException ex) {
      throw new RuntimeException("Unexpected IOException caught: "
        + ex.getMessage());
    }
  }

  /**
   * Reads a {@link Geometry} in binary WKB format from an {@link InStream}.
   *
   * @param is the stream to read from
   * @return the Geometry read
   * @throws IOException if the underlying stream creates an error
   * @throws ParseException if the WKB is ill-formed
   */
  public Geometry read(final InStream is) throws IOException, ParseException {
    dis.setInStream(is);
    final Geometry g = readGeometry();
    return g;
  }

  /**
   * Reads a coordinate value with the specified dimensionality.
   * Makes the X and Y ordinates precise according to the precision model
   * in use.
   */
  private void readCoordinate() throws IOException {
    for (int i = 0; i < inputDimension; i++) {
      if (i <= 1) {
        ordValues[i] = precisionModel.makePrecise(dis.readDouble());
      } else {
        ordValues[i] = dis.readDouble();
      }

    }
  }

  private CoordinatesList readCoordinateSequence(final int size)
    throws IOException {
    final CoordinatesList seq = csFactory.create(size, inputDimension);
    int targetDim = seq.getAxisCount();
    if (targetDim > inputDimension) {
      targetDim = inputDimension;
    }
    for (int i = 0; i < size; i++) {
      readCoordinate();
      for (int j = 0; j < targetDim; j++) {
        seq.setValue(i, j, ordValues[j]);
      }
    }
    return seq;
  }

  private CoordinatesList readCoordinateSequenceLineString(final int size)
    throws IOException {
    final CoordinatesList seq = readCoordinateSequence(size);
    if (isStrict) {
      return seq;
    }
    if (seq.size() == 0 || seq.size() >= 2) {
      return seq;
    }
    return CoordinateSequences.extend(csFactory, seq, 2);
  }

  private CoordinatesList readCoordinateSequenceRing(final int size)
    throws IOException {
    final CoordinatesList seq = readCoordinateSequence(size);
    if (isStrict) {
      return seq;
    }
    if (CoordinateSequences.isRing(seq)) {
      return seq;
    }
    return CoordinateSequences.ensureValidRing(csFactory, seq);
  }

  private Geometry readGeometry() throws IOException, ParseException {

    // determine byte order
    final byte byteOrderWKB = dis.readByte();

    // always set byte order, since it may change from geometry to geometry
    if (byteOrderWKB == WKBConstants.wkbNDR) {
      dis.setOrder(ByteOrderValues.LITTLE_ENDIAN);
    } else if (byteOrderWKB == WKBConstants.wkbXDR) {
      dis.setOrder(ByteOrderValues.BIG_ENDIAN);
    } else if (isStrict) {
      throw new ParseException("Unknown geometry byte order (not NDR or XDR): "
        + byteOrderWKB);
    }
    // if not strict and not XDR or NDR, then we just use the dis default set at
    // the
    // start of the geometry (if a multi-geometry). This allows WBKReader to
    // work
    // with Spatialite native BLOB WKB, as well as other WKB variants that might
    // just
    // specify endian-ness at the start of the multigeometry.

    final int typeInt = dis.readInt();
    final int geometryType = typeInt & 0xff;
    // determine if Z values are present
    final boolean hasZ = (typeInt & 0x80000000) != 0;
    inputDimension = hasZ ? 3 : 2;
    // determine if SRIDs are present
    hasSRID = (typeInt & 0x20000000) != 0;

    int SRID = 0;
    if (hasSRID) {
      SRID = dis.readInt();
    }

    // only allocate ordValues buffer if necessary
    if (ordValues == null || ordValues.length < inputDimension) {
      ordValues = new double[inputDimension];
    }

    Geometry geom = null;
    switch (geometryType) {
      case WKBConstants.wkbPoint:
        geom = readPoint();
      break;
      case WKBConstants.wkbLineString:
        geom = readLineString();
      break;
      case WKBConstants.wkbPolygon:
        geom = readPolygon();
      break;
      case WKBConstants.wkbMultiPoint:
        geom = readMultiPoint();
      break;
      case WKBConstants.wkbMultiLineString:
        geom = readMultiLineString();
      break;
      case WKBConstants.wkbMultiPolygon:
        geom = readMultiPolygon();
      break;
      case WKBConstants.wkbGeometryCollection:
        geom = readGeometryCollection();
      break;
      default:
        throw new ParseException("Unknown WKB type " + geometryType);
    }
    setSRID(geom, SRID);
    return geom;
  }

  private GeometryCollection readGeometryCollection() throws IOException,
    ParseException {
    final int numGeom = dis.readInt();
    final Geometry[] geoms = new Geometry[numGeom];
    for (int i = 0; i < numGeom; i++) {
      geoms[i] = readGeometry();
    }
    return factory.geometryCollection(geoms);
  }

  private LinearRing readLinearRing() throws IOException {
    final int size = dis.readInt();
    final CoordinatesList pts = readCoordinateSequenceRing(size);
    return factory.linearRing(pts);
  }

  private LineString readLineString() throws IOException {
    final int size = dis.readInt();
    final CoordinatesList pts = readCoordinateSequenceLineString(size);
    return factory.lineString(pts);
  }

  private MultiLineString readMultiLineString() throws IOException,
    ParseException {
    final int numGeom = dis.readInt();
    final LineString[] geoms = new LineString[numGeom];
    for (int i = 0; i < numGeom; i++) {
      final Geometry g = readGeometry();
      if (!(g instanceof LineString)) {
        throw new ParseException(INVALID_GEOM_TYPE_MSG + "MultiLineString");
      }
      geoms[i] = (LineString)g;
    }
    return factory.createMultiLineString(geoms);
  }

  private MultiPoint readMultiPoint() throws IOException, ParseException {
    final int numGeom = dis.readInt();
    final Point[] geoms = new Point[numGeom];
    for (int i = 0; i < numGeom; i++) {
      final Geometry g = readGeometry();
      if (!(g instanceof Point)) {
        throw new ParseException(INVALID_GEOM_TYPE_MSG + "MultiPoint");
      }
      geoms[i] = (Point)g;
    }
    return factory.createMultiPoint(geoms);
  }

  private MultiPolygon readMultiPolygon() throws IOException, ParseException {
    final int numGeom = dis.readInt();
    final Polygon[] geoms = new Polygon[numGeom];

    for (int i = 0; i < numGeom; i++) {
      final Geometry g = readGeometry();
      if (!(g instanceof Polygon)) {
        throw new ParseException(INVALID_GEOM_TYPE_MSG + "MultiPolygon");
      }
      geoms[i] = (Polygon)g;
    }
    return factory.createMultiPolygon(geoms);
  }

  private Point readPoint() throws IOException {
    final CoordinatesList pts = readCoordinateSequence(1);
    return factory.point(pts);
  }

  private Polygon readPolygon() throws IOException {
    final int numRings = dis.readInt();
    final List<LinearRing> rings = new ArrayList<>();

    for (int i = 0; i < numRings; i++) {
      LinearRing ring = readLinearRing();
      rings.add(ring);
    }
    return factory.polygon(rings);
  }

  /**
   * Sets the SRID, if it was specified in the WKB
   *
   * @param g the geometry to update
   * @return the geometry with an updated SRID value, if required
   */
  private Geometry setSRID(final Geometry g, final int SRID) {
    // if (SRID != 0)
    // g.setSRID(SRID);
    return g;
  }

}
