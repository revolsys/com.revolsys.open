/*
 * $URL:https://secure.revolsys.com/svn/open.revolsys.com/GIS/trunk/src/main/java/com/revolsys/gis/format/shape/io/geometry/JtsGeometryConverter.java $
 * $Author:paul.austin@revolsys.com $
 * $Date:2007-06-09 09:28:28 -0700 (Sat, 09 Jun 2007) $
 * $Revision:265 $

 * Copyright 2004-2005 Revolution Systems Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.gis.format.shape.io.geometry;

import java.io.IOException;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.format.shape.io.ShapefileConstants;
import com.revolsys.gis.io.EndianInputOutput;
import com.revolsys.gis.io.LittleEndianRandomAccessFile;
import com.revolsys.io.EndianInput;
import com.revolsys.util.MathUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class JtsGeometryConverter {
  private final GeometryFactory geometryFactory;

  public JtsGeometryConverter() {
    this(new GeometryFactory());
  }

  public JtsGeometryConverter(
    final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public Geometry readGeometry(
    final EndianInput in)
    throws IOException {
    final int x =in.readInt();
    final int recordLength = in.readInt();
    final int shapeType = in.readLEInt();
    return readGeometry(in, shapeType, recordLength);
  }

  public Geometry readGeometry(final EndianInput in, final int shapeType,
    final int recordLength) throws IOException {
    switch (shapeType) {
      case ShapefileConstants.NULL_SHAPE:
        return null;
      case ShapefileConstants.POINT_SHAPE:
        return new Point2DConverter(geometryFactory).read(in, recordLength);
      case ShapefileConstants.POINT_M_SHAPE:
        return new Point2DMConverter(geometryFactory).read(in, recordLength);
      case ShapefileConstants.POINT_Z_SHAPE:
        return new Point3DConverter(geometryFactory).read(in, recordLength);
      case ShapefileConstants.MULTI_POINT_SHAPE:
        return new Point2DConverter(geometryFactory).read(in, recordLength);
      case ShapefileConstants.MULTI_POINT_M_SHAPE:
        return new Point2DMConverter(geometryFactory).read(in, recordLength);
      case ShapefileConstants.MULTI_POINT_Z_SHAPE:
        return new Point3DConverter(geometryFactory).read(in, recordLength);
      case ShapefileConstants.POLYLINE_SHAPE:
        return new LineString2DConverter(geometryFactory).read(in, recordLength);
      case ShapefileConstants.POLYLINE_M_SHAPE:
        return new LineString2DMConverter(geometryFactory).read(in,
          recordLength);
      case ShapefileConstants.POLYLINE_Z_SHAPE:
        return new LineString3DConverter(geometryFactory).read(in, recordLength);
      case ShapefileConstants.POLYGON_SHAPE:
        return new Polygon2DConverter(geometryFactory).read(in, recordLength);
      case ShapefileConstants.POLYGON_M_SHAPE:
        return new Polygon2DMConverter(geometryFactory).read(in, recordLength);
      case ShapefileConstants.POLYGON_Z_SHAPE:
        return new Polygon3DConverter(geometryFactory).read(in, recordLength);
      case ShapefileConstants.MULTI_PATCH_SHAPE:
        return new MultiPolygonConverter(geometryFactory).read(in, recordLength);
      default:
      break;
    }
    return null;
  }

  private void write2DCoordinates(
    final EndianInputOutput out,
    final Coordinate[] coordinates)
    throws IOException {
    for (int i = 0; i < coordinates.length; i++) {
      final Coordinate coordinate = coordinates[i];
      out.writeLEDouble(coordinate.x);
      out.writeLEDouble(coordinate.y);
    }
  }

  private void writeEnvelope(
    final EndianInputOutput out,
    final Envelope envelope)
    throws IOException {
    out.writeLEDouble(envelope.getMinX());
    out.writeLEDouble(envelope.getMinY());
    out.writeLEDouble(envelope.getMaxX());
    out.writeLEDouble(envelope.getMaxY());
  }

  public int writeGeometry(
    final LittleEndianRandomAccessFile out,
    final int recordNumber,
    final Object geometry,
    final Envelope envelope,
    final int shapeType)
    throws IOException {
    out.writeInt(recordNumber);
    if (geometry == null) {
      return writeNull(out);
    } else if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      envelope.expandToInclude(point.getEnvelopeInternal());
      return writePoint(out, point, shapeType);
    } else if (geometry instanceof MultiPoint) {
      final MultiPoint point = (MultiPoint)geometry;
      envelope.expandToInclude(point.getEnvelopeInternal());
      return writeMultiPoint(out, point, shapeType);
    } else if (geometry instanceof LineString) {
      final LineString line = (LineString)geometry;
      envelope.expandToInclude(line.getEnvelopeInternal());
      return writeLineString(out, line, shapeType);
    } else if (geometry instanceof Polygon) {
      final Polygon polygon = (Polygon)geometry;
      envelope.expandToInclude(polygon.getEnvelopeInternal());
      return writePolygon(out, polygon, shapeType);
    } else {
      return ShapefileConstants.UNKNOWN_SHAPE;
    }
  }

  private int writeLineString(
    final LittleEndianRandomAccessFile out,
    final LineString line,
    final int shapeType)
    throws IOException {
    final Envelope envelope = line.getEnvelopeInternal();
    final Coordinate[] coordinates = line.getCoordinates();
    if (coordinates.length > 0) {
      if (shapeType == ShapefileConstants.POLYLINE_SHAPE
        || Double.isNaN(coordinates[0].z)) {
        final int recordLength = (4 * MathUtil.BYTES_IN_INT + (4 + 2 * coordinates.length)
          * MathUtil.BYTES_IN_DOUBLE) / 2;
        out.writeInt(recordLength);
        out.writeLEInt(ShapefileConstants.POLYLINE_SHAPE);
        writeEnvelope(out, envelope);
        out.writeLEInt(1);
        out.writeLEInt(coordinates.length);
        out.writeLEInt(0);

        for (int i = 0; i < coordinates.length; i++) {
          final Coordinate coordinate = coordinates[i];
          out.writeLEDouble(coordinate.x);
          out.writeLEDouble(coordinate.y);
        }
        return ShapefileConstants.POLYLINE_SHAPE;
      } else {
        final int recordLength = (4 * MathUtil.BYTES_IN_INT + (8 + 2 * coordinates.length)
          * MathUtil.BYTES_IN_DOUBLE) / 2;
        out.writeInt(recordLength);
        out.writeLEInt(ShapefileConstants.POLYLINE_Z_SHAPE);
        writeEnvelope(out, envelope);
        out.writeLEInt(1);
        out.writeLEInt(coordinates.length);
        out.writeLEInt(0);

        for (int i = 0; i < coordinates.length; i++) {
          final Coordinate coordinate = coordinates[i];
          out.writeLEDouble(coordinate.x);
          out.writeLEDouble(coordinate.y);
        }
        out.writeLEDouble(0);
        out.writeLEDouble(0);
        final long zMinIndex = out.getFilePointer();
        double minZ = Double.MAX_VALUE;
        double maxZ = Double.MIN_VALUE;
        for (int i = 0; i < coordinates.length; i++) {
          final Coordinate coordinate = coordinates[i];
          if (!Double.isNaN(coordinate.z)) {
            minZ = Math.min(minZ, coordinate.z);
            maxZ = Math.max(maxZ, coordinate.z);
          }
          out.writeLEDouble(coordinate.z);
        }
        final long endIndex = out.getFilePointer();
        out.seek(zMinIndex);
        out.writeLEDouble(minZ);
        out.writeLEDouble(maxZ);
        out.seek(endIndex);
        return ShapefileConstants.POLYLINE_Z_SHAPE;
      }
    } else {
      return writeNull(out);
    }
  }

  private int writeMultiPoint(
    final LittleEndianRandomAccessFile out,
    final MultiPoint point,
    final int shapeType)
    throws IOException {
    final com.vividsolutions.jts.geom.Envelope envelope = point.getEnvelopeInternal();
    final Coordinate[] coordinates = point.getCoordinates();
    if (coordinates.length > 0) {
      if (shapeType == ShapefileConstants.MULTI_POINT_SHAPE
        || Double.isNaN(coordinates[0].z)) {
        final int recordLength = 20 + coordinates.length * 8;
        out.writeInt(recordLength);
        out.writeLEInt(ShapefileConstants.MULTI_POINT_SHAPE);
        writeEnvelope(out, envelope);
        out.writeLEInt(coordinates.length);

        for (int i = 0; i < coordinates.length; i++) {
          final Coordinate coordinate = coordinates[i];
          out.writeLEDouble(coordinate.x);
          out.writeLEDouble(coordinate.y);
        }
        return ShapefileConstants.MULTI_POINT_SHAPE;
      } else {
        final int recordLength = 28 + coordinates.length * 12;
        out.writeInt(recordLength);
        out.writeLEInt(ShapefileConstants.MULTI_POINT_Z_SHAPE);
        writeEnvelope(out, envelope);
        out.writeLEInt(coordinates.length);

        for (int i = 0; i < coordinates.length; i++) {
          final Coordinate coordinate = coordinates[i];
          out.writeLEDouble(coordinate.x);
          out.writeLEDouble(coordinate.y);
        }
        out.writeLEDouble(0);
        out.writeLEDouble(0);
        final long zMinIndex = out.getFilePointer();
        double minZ = Double.MAX_VALUE;
        double maxZ = Double.MIN_VALUE;
        for (int i = 0; i < coordinates.length; i++) {
          final Coordinate coordinate = coordinates[i];
          if (!Double.isNaN(coordinate.z)) {
            minZ = Math.min(minZ, coordinate.z);
            maxZ = Math.max(maxZ, coordinate.z);
          }
          out.writeLEDouble(coordinate.z);
        }
        final long endIndex = out.getFilePointer();
        out.seek(zMinIndex);
        out.writeLEDouble(minZ);
        out.writeLEDouble(maxZ);
        out.seek(endIndex);
        return ShapefileConstants.MULTI_POINT_Z_SHAPE;
      }
    } else {
      return writeNull(out);
    }
  }

  private int writeNull(
    final LittleEndianRandomAccessFile out)
    throws IOException {
    final int recordLength = MathUtil.BYTES_IN_INT;
    out.writeInt(recordLength);
    out.writeLEInt(ShapefileConstants.NULL_SHAPE);
    return ShapefileConstants.NULL_SHAPE;
  }

  private int writePoint(
    final LittleEndianRandomAccessFile out,
    final Point point,
    final int shapeType)
    throws IOException {
    final Coordinate coordinate = point.getCoordinate();
    if (shapeType == ShapefileConstants.POINT_SHAPE || Double.isNaN(coordinate.z)) {
      final int recordLength = 10; // (BYTES_IN_INT + 2 * BYTES_IN_DOUBLE) /
      // BYTES_IN_SHORT;
      out.writeInt(recordLength);
      out.writeLEInt(ShapefileConstants.POINT_SHAPE);
      out.writeLEDouble(coordinate.x);
      out.writeLEDouble(coordinate.y);
      return ShapefileConstants.POINT_SHAPE;
    } else {
      final int recordLength = 18; // (BYTES_IN_INT + 2 * BYTES_IN_DOUBLE) /
      // BYTES_IN_SHORT;
      out.writeInt(recordLength);
      out.writeLEInt(ShapefileConstants.POINT_Z_SHAPE);
      out.writeLEDouble(coordinate.x);
      out.writeLEDouble(coordinate.y);
      out.writeLEDouble(coordinate.z);
      out.writeLEDouble(0);
      return ShapefileConstants.POINT_Z_SHAPE;
    }
  }

  private int writePolygon(
    final LittleEndianRandomAccessFile out,
    final Polygon polygon,
    final int type)
    throws IOException {
    int shapeType = type;
    if (!polygon.isEmpty()) {
      final Coordinate[] coordinates = polygon.getCoordinates();
      final int numPoints = coordinates.length;

      final int numHoles = polygon.getNumInteriorRing();
      final int numParts = 1 + numHoles;
      int recordLength = (4 + numParts) * MathUtil.BYTES_IN_INT + 4
        * MathUtil.BYTES_IN_DOUBLE;
      int dimension = 2;
      if (shapeType == ShapefileConstants.UNKNOWN_SHAPE) {
        if (shapeType == ShapefileConstants.POLYGON_SHAPE
          || Double.isNaN(polygon.getCoordinate().z)) {
          shapeType = ShapefileConstants.POLYGON_SHAPE;
        } else {
          dimension = 3;
          shapeType = ShapefileConstants.POLYGON_Z_SHAPE;
          recordLength += 2 * MathUtil.BYTES_IN_DOUBLE; // For minZ + maxZ
        }
      }
      recordLength += dimension * MathUtil.BYTES_IN_DOUBLE;

      out.writeInt(recordLength / 2);
      out.writeLEInt(shapeType);
      writeEnvelope(out, polygon.getEnvelopeInternal());
      out.writeLEInt(numParts);
      out.writeLEInt(numPoints);

      final LineString exterior = polygon.getExteriorRing();
      out.writeLEInt(0);
      int partIndex = exterior.getNumPoints();
      for (int i = 0; i < numHoles; i++) {
        out.writeLEInt(partIndex);
        final LineString interior = polygon.getInteriorRingN(i);
        partIndex += interior.getNumPoints();
      }

      write2DCoordinates(out, coordinates);

      if (shapeType == ShapefileConstants.POLYGON_Z_SHAPE) {
        final long zRangePos = out.getFilePointer();
        out.writeLEDouble(0);
        out.writeLEDouble(0);
        double minZ = Double.MAX_VALUE;
        double maxZ = Double.MIN_VALUE;
        for (int i = 0; i < coordinates.length; i++) {
          final Coordinate coordinate = coordinates[i];
          double z = coordinate.z;
          if (Double.isNaN(z)) {
            z = 0;
          }
          minZ = Math.min(z, minZ);
          maxZ = Math.max(z, maxZ);
          out.writeLEDouble(z);
        }
        final long zEnd = out.getFilePointer();
        out.seek(zRangePos);
        out.writeLEDouble(minZ);
        out.writeLEDouble(maxZ);
        out.seek(zEnd);
      }
      return shapeType;
    } else {
      return writeNull(out);
    }
  }
}
