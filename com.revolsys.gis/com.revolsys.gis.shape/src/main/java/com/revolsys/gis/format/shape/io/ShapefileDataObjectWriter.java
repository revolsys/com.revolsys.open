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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;

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
import com.revolsys.gis.format.xbase.io.XbaseDataObjectWriter;
import com.revolsys.gis.io.EndianOutput;
import com.revolsys.gis.io.ResourceEndianOutput;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.spring.NonExistingResource;
import com.revolsys.spring.SpringUtil;
import com.revolsys.util.MathUtil;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class ShapefileDataObjectWriter extends XbaseDataObjectWriter {
  private static final Logger LOG = Logger.getLogger(ShapefileDataObjectWriter.class);

  private Envelope envelope = new Envelope();

  private ShapefileGeometryWriter geometryConverter;

  private String geometryPropertyName = "geometry";

  private ResourceEndianOutput indexOut;

  private ResourceEndianOutput out;

  private Resource resource;

  private int recordNumber = 1;

  private double zMax = 0; // Double.MIN_VALUE;

  private double zMin = 0; // Double.MAX_VALUE;

  private CoordinateSystem coordinateSystem;

  private GeometryFactory geometryFactory;

  public ShapefileDataObjectWriter(final DataObjectMetaData metaData,
    final Resource resource) throws IOException {
    super(metaData, SpringUtil.getResourceWithExtension(resource, "dbf"));
    this.resource = resource;
  }

  @Override
  protected void preFirstWrite(DataObject object) throws IOException {
    if (geometryFactory == null) {
      final Geometry geometry = object.getGeometryValue();
      if (geometry != null) {
        geometryFactory = GeometryFactory.getFactory(geometry);
      }
    }
    createPrjFile(geometryFactory);
  }

  protected void init() throws IOException {
    super.init();

    this.out = new ResourceEndianOutput(resource);
    writeHeader(this.out);

    final DataObjectMetaDataImpl metaData = (DataObjectMetaDataImpl)getMetaData();
    if (metaData != null) {
      if (!metaData.hasAttribute(geometryPropertyName)) {
        metaData.addAttribute(geometryPropertyName, DataTypes.GEOMETRY, true);
      }
      if (!hasField(geometryPropertyName)) {
        addField(new FieldDefinition(geometryPropertyName,
          FieldDefinition.OBJECT_TYPE, 0));
      }
    }

    Resource indexResource = SpringUtil.getResourceWithExtension(resource,
      "shx");
    if (!(indexResource instanceof NonExistingResource)) {
      indexOut = new ResourceEndianOutput(indexResource);
      writeHeader(indexOut);
    }

    geometryFactory = getProperty(IoConstants.GEOMETRY_FACTORY);
  }

  private void createPrjFile(GeometryFactory geometryFactory)
    throws IOException {
    if (geometryFactory != null) {
      CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
      int srid = coordinateSystem.getId();
      Resource prjResource = SpringUtil.getResourceWithExtension(resource,
        "prj");
      if (!(prjResource instanceof NonExistingResource)) {
        try {
          OutputStream out = SpringUtil.getOutputStream(prjResource);
          final PrintWriter writer = new PrintWriter(
            new OutputStreamWriter(out));
          CoordinateSystem esriCoordinateSystem = CoordinateSystems.getCoordinateSystem(new QName(
            "ESRI", String.valueOf(srid)));
          EsriCsWktWriter.write(writer, esriCoordinateSystem);
          writer.close();
        } catch (final IOException e) {
          LOG.error("Unable to create .prj file: " + prjResource, e);
        }
      }
    }
  }

  @Override
  protected int addDbaseField(final String name, final Class<?> typeJavaClass,
    final int length, final int scale) {
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
      updateHeader(out);
      if (indexOut != null) {
        updateHeader(indexOut);
      }
    } catch (final IOException e) {
      LOG.error(e.getMessage(), e);
    } finally {
      out = null;
      indexOut = null;
    }
  }

  private void createGeometryWriter(final Geometry geometry) {
    final CoordinatesList points = CoordinatesListUtil.get(geometry);
    final byte numAxis = points.getNumAxis();
    if (geometry instanceof Point) {
      if (numAxis == 2) {
        geometryConverter = new Point2DConverter();
      } else {
        geometryConverter = new Point3DConverter();
      }
    } else if (geometry instanceof LineString) {
      if (numAxis == 2) {
        geometryConverter = new LineString2DConverter();
      } else {
        geometryConverter = new LineString3DConverter();
      }
    } else if (geometry instanceof MultiLineString) {
      if (numAxis == 2) {
        geometryConverter = new LineString2DConverter();
      } else {
        geometryConverter = new LineString3DConverter();
      }
    } else if (geometry instanceof Polygon) {
      if (numAxis == 2) {
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

  private void updateHeader(final ResourceEndianOutput out) throws IOException {

    int shapeType = ShapefileConstants.NULL_SHAPE;
    if (geometryConverter != null) {
      shapeType = geometryConverter.getShapeType();
    }
    out.seek(24);
    out.writeInt((int)(out.length() / 2));
    out.seek(32);
    out.writeLEInt(shapeType);
    out.writeLEDouble(envelope.getMinX());
    out.writeLEDouble(envelope.getMinY());
    out.writeLEDouble(envelope.getMaxX());
    out.writeLEDouble(envelope.getMaxY());
    switch (shapeType) {
      case ShapefileConstants.POINT_ZM_SHAPE:
      case ShapefileConstants.MULTI_POINT_ZM_SHAPE:
      case ShapefileConstants.POLYLINE_ZM_SHAPE:
      case ShapefileConstants.POLYGON_ZM_SHAPE:
        out.writeLEDouble(zMin);
        out.writeLEDouble(zMax);
      break;

      default:
        out.writeLEDouble(0.0);
        out.writeLEDouble(0.0);
      break;
    }
    out.writeLEDouble(0.0);
    out.writeLEDouble(0.0);
    out.close();
  }

  @Override
  protected boolean writeField(final DataObject object,
    final FieldDefinition field) throws IOException {
    if (field.getName() == geometryPropertyName) {
      final long recordIndex = out.getFilePointer();
      Geometry geometry = object.getGeometryValue();
      geometry = GeometryProjectionUtil.perform(geometry, coordinateSystem);
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
        if (indexOut != null) {
          final long recordLength = out.getFilePointer() - recordIndex;
          indexOut.writeInt((int)(recordIndex / MathUtil.BYTES_IN_SHORT));
          indexOut.writeInt((int)(recordLength / MathUtil.BYTES_IN_SHORT) - 4);
        }
      }
      return true;
    } else {
      return super.writeField(object, field);
    }
  }

  private void writeHeader(final EndianOutput out) throws IOException {
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

  private int writeNull(final EndianOutput out) throws IOException {
    final int recordLength = MathUtil.BYTES_IN_INT;
    out.writeInt(recordLength);
    out.writeLEInt(ShapefileConstants.NULL_SHAPE);
    return ShapefileConstants.NULL_SHAPE;
  }

  @Override
  public String toString() {
    return "ShapefileWriter(" + resource + ")";
  }
}
