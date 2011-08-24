/*
 * $URL:https://secure.revolsys.com/svn/open.revolsys.com/GIS/trunk/src/main/java/com/revolsys/gis/format/shape/io/ShapeFileWriter.java $
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
package com.revolsys.gis.format.shape.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.CoordinateSystems;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.esri.EsriCsWktWriter;
import com.revolsys.gis.cs.projection.GeometryProjectionUtil;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.format.shape.io.geometry.LineString2DConverter;
import com.revolsys.gis.format.shape.io.geometry.LineString3DConverter;
import com.revolsys.gis.format.shape.io.geometry.MultiPolygonConverter;
import com.revolsys.gis.format.shape.io.geometry.Point2DConverter;
import com.revolsys.gis.format.shape.io.geometry.Point3DConverter;
import com.revolsys.gis.format.shape.io.geometry.Polygon2DConverter;
import com.revolsys.gis.format.shape.io.geometry.Polygon3DConverter;
import com.revolsys.gis.format.shape.io.geometry.ShapefileGeometryWriter;
import com.revolsys.gis.format.xbase.io.FieldDefinition;
import com.revolsys.gis.format.xbase.io.XbaseFileWriter;
import com.revolsys.gis.io.EndianOutput;
import com.revolsys.gis.io.EndianOutputStream;
import com.revolsys.gis.io.LittleEndianRandomAccessFile;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.util.MathUtil;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class ShapeFileWriter extends XbaseFileWriter {
  private static final Logger LOG = Logger.getLogger(ShapeFileWriter.class);

  private Envelope envelope = new Envelope();

  private ShapefileGeometryWriter geometryConverter;

  private String geometryPropertyName = "geometry";

  private File indexFile;

  private EndianOutput indexOut;

  private EndianOutput out;

  private int recordNumber = 1;

  private final File shapeFile;

  private double zMax = 0; // Double.MIN_VALUE;

  private double zMin = 0; // Double.MAX_VALUE;

  private GeometryFactory geometryFactory;

  public ShapeFileWriter(
    final File file)
    throws IOException {
    this(file, false);
  }

  public ShapeFileWriter(
    final File file,
    final boolean append)
    throws IOException {
    this(file, FileUtil.getFileNamePrefix(file), append);
  }

  public ShapeFileWriter(
    final File file,
    final DataObjectMetaData metaData)
    throws IOException {
    this(file, false);
    setMetaData(metaData);
    geometryPropertyName = metaData.getGeometryAttributeName();
  }

  public ShapeFileWriter(
    final File file,
    final String typeName)
    throws IOException {
    this(file, typeName, false);
  }

  public ShapeFileWriter(
    final File file,
    final String typeName,
    final boolean append)
    throws IOException {
    super(FileUtil.getFileWithExtension(file, "dbf"), typeName, append);
    this.shapeFile = file;
  }

  protected void init()
    throws IOException {
    super.init();

    final boolean shapeExists = shapeFile.exists();
    if (isAppend() && shapeExists) {
      final LittleEndianRandomAccessFile out = new LittleEndianRandomAccessFile(
        shapeFile, "rw");
      out.seek(out.length());
      this.out = out;

    } else {
      final FileOutputStream fileOut = new FileOutputStream(shapeFile);
      final BufferedOutputStream bufferedOut = new BufferedOutputStream(fileOut);
      out = new EndianOutputStream(bufferedOut);
      writeHeader(out);
    }
    final DataObjectMetaDataImpl metaData = (DataObjectMetaDataImpl)getMetaData();
    if (metaData != null) {
      geometryPropertyName = metaData.getGeometryAttributeName();
      if (!metaData.hasAttribute(geometryPropertyName)) {
        metaData.addAttribute(geometryPropertyName, DataTypes.GEOMETRY, true);
        addField(new FieldDefinition(geometryPropertyName,
          FieldDefinition.OBJECT_TYPE, 0));
      }
    }
    indexFile = FileUtil.getFileWithExtension(shapeFile, "shx");
    final boolean indexExists = indexFile.exists();

    if (isAppend() && indexExists) {
      final LittleEndianRandomAccessFile indexOut = new LittleEndianRandomAccessFile(
        indexFile, "rw");
      indexOut.seek(indexOut.length());
      this.indexOut = indexOut;
    } else {
      final FileOutputStream indexFileOut = new FileOutputStream(indexFile);
      final BufferedOutputStream indexBufferedOut = new BufferedOutputStream(
        indexFileOut);
      indexOut = new EndianOutputStream(indexBufferedOut);
      writeHeader(indexOut);
    }

    GeometryFactory geometryFactory = getProperty(IoConstants.GEOMETRY_FACTORY);
     if (geometryFactory != null) {
      CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
      int srid = coordinateSystem.getId();
      File prjFile = FileUtil.getFileWithExtension(shapeFile, "prj");
      try {
        final PrintWriter out = new PrintWriter(new FileWriter(prjFile));
        CoordinateSystem esriCoordinateSystem = CoordinateSystems.getCoordinateSystem(new QName(
          "ESRI", String.valueOf(srid)));
        EsriCsWktWriter.write(out, esriCoordinateSystem);
        out.close();
      } catch (final IOException e) {
        LOG.error("Unable to create .prj file: " + prjFile, e);
      }
    }
  }

  @Override
  protected int addDbaseField(
    final String name,
    final Class<?> typeJavaClass,
    final int length,
    final int scale) {
    if (Geometry.class.isAssignableFrom(typeJavaClass)) {
      addField(new FieldDefinition(name, FieldDefinition.OBJECT_TYPE, 0));
      return 0;
    } else {
      return super.addDbaseField(name, typeJavaClass, length, scale);
    }
  }

  @Override
  public void close() {
    super.close();
    try {
      updateHeader(shapeFile, out);
      updateHeader(indexFile, indexOut);
    } catch (final IOException e) {
      LOG.error(e.getMessage(), e);
    } finally {
      out = null;
      indexOut = null;
    }
  }

  private void createGeometryWriter(
    final Geometry geometry) {
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      final CoordinateSequence coordinates = point.getCoordinateSequence();
      if (coordinates.getDimension() == 2) {
        geometryConverter = new Point2DConverter();
      } else {
        geometryConverter = new Point3DConverter();
      }
    } else if (geometry instanceof LineString) {
      final LineString line = (LineString)geometry;
      final CoordinateSequence coordinates = line.getCoordinateSequence();
      if (coordinates.getDimension() == 2) {
        geometryConverter = new LineString2DConverter();
      } else {
        geometryConverter = new LineString3DConverter();
      }
    } else if (geometry instanceof MultiLineString) {
      final MultiLineString multiLine = (MultiLineString)geometry;
      final LineString line = (LineString)multiLine.getGeometryN(0);
      final CoordinateSequence coordinates = line.getCoordinateSequence();
      if (coordinates.getDimension() == 2) {
        geometryConverter = new LineString2DConverter();
      } else {
        geometryConverter = new LineString3DConverter();
      }
    } else if (geometry instanceof Polygon) {
      final Polygon polygon = (Polygon)geometry;
      final LineString line = polygon.getExteriorRing();
      final CoordinateSequence coordinates = line.getCoordinateSequence();
      if (coordinates.getDimension() == 2) {
        geometryConverter = new Polygon2DConverter();
      } else {
        geometryConverter = new Polygon3DConverter();
      }
    } else if (geometry instanceof MultiPolygon) {
      geometryConverter = new MultiPolygonConverter();
    } else {
      throw new RuntimeException("Not supported" + geometry.getClass());
    }
  }

  @Override
  public void truncate()
    throws IOException {
    super.truncate();
    if (out instanceof LittleEndianRandomAccessFile) {
      final LittleEndianRandomAccessFile inOut = (LittleEndianRandomAccessFile)out;
      recordNumber = 1;
      zMin = Double.MAX_VALUE;
      zMax = Double.MIN_VALUE;
      envelope = new Envelope();
      inOut.setLength(0);
      writeHeader(out);
    }

    if (indexOut instanceof LittleEndianRandomAccessFile) {
      final LittleEndianRandomAccessFile indexInOut = (LittleEndianRandomAccessFile)indexOut;
      indexInOut.setLength(0);
      writeHeader(indexInOut);
    }
  }

  private void updateHeader(
    final File file,
    final EndianOutput out)
    throws IOException {

    LittleEndianRandomAccessFile inOut;
    if (out instanceof LittleEndianRandomAccessFile) {
      inOut = (LittleEndianRandomAccessFile)out;
    } else {
      out.flush();
      out.close();
      inOut = new LittleEndianRandomAccessFile(file, "rw");
    }

    int shapeType = ShapefileConstants.NULL_SHAPE;
    if (geometryConverter != null) {
      shapeType = geometryConverter.getShapeType();
    }
    inOut.seek(24);
    inOut.writeInt((int)(inOut.length() / 2));
    inOut.seek(32);
    inOut.writeLEInt(shapeType);
    inOut.writeLEDouble(envelope.getMinX());
    inOut.writeLEDouble(envelope.getMinY());
    inOut.writeLEDouble(envelope.getMaxX());
    inOut.writeLEDouble(envelope.getMaxY());
    switch (shapeType) {
      case ShapefileConstants.POINT_ZM_SHAPE:
      case ShapefileConstants.MULTI_POINT_ZM_SHAPE:
      case ShapefileConstants.POLYLINE_ZM_SHAPE:
      case ShapefileConstants.POLYGON_ZM_SHAPE:
        inOut.writeLEDouble(zMin);
        inOut.writeLEDouble(zMax);
      break;

      default:
        inOut.writeLEDouble(0.0);
        inOut.writeLEDouble(0.0);
      break;
    }
    inOut.writeLEDouble(0.0);
    inOut.writeLEDouble(0.0);
    inOut.close();
  }

  @Override
  protected boolean writeField(
    final DataObject object,
    final FieldDefinition field)
    throws IOException {
    if (field.getName().equals(geometryPropertyName)) {
      final long recordIndex = out.getFilePointer();
      Geometry geometry = object.getGeometryValue();
      geometry = GeometryProjectionUtil.perform(geometry, geometryFactory);
      envelope.expandToInclude(geometry.getEnvelopeInternal());
      if (geometry.isEmpty()) {
        writeNull(out);
      } else {
        if (geometryConverter == null) {
          createGeometryWriter(geometry);
        }
        out.writeInt(recordNumber);
        geometryConverter.write(out, geometry);

        recordNumber++;
        final long recordLength = out.getFilePointer() - recordIndex;
        indexOut.writeInt((int)(recordIndex / MathUtil.BYTES_IN_SHORT));
        indexOut.writeInt((int)(recordLength / MathUtil.BYTES_IN_SHORT) - 4);
      }
      return true;
    } else {
      return super.writeField(object, field);
    }
  }

  private void writeHeader(
    final EndianOutput out)
    throws IOException {
    out.writeInt(ShapefileConstants.FILE_CODE);
    for (int i = 0; i < 5; i++) {
      out.writeInt(0);
    }
    out.writeInt(0); // File length updated on close
    out.writeLEInt(ShapefileConstants.VERSION);
    out.writeLEInt(0); // Shape Type updated on close
    // shape type and bounding box will be updated on file close
    for (int i = 0; i < 8; i++) {
      out.writeLEDouble(0);
    }
  }

  private int writeNull(
    final EndianOutput out)
    throws IOException {
    final int recordLength = MathUtil.BYTES_IN_INT;
    out.writeInt(recordLength);
    out.writeLEInt(ShapefileConstants.NULL_SHAPE);
    return ShapefileConstants.NULL_SHAPE;
  }
}
