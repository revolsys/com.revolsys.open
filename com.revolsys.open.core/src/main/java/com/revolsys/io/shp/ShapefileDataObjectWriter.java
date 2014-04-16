/*
 * $URL $
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
package com.revolsys.io.shp;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.CoordinateSystems;
import com.revolsys.gis.cs.esri.EsriCsWktWriter;
import com.revolsys.gis.cs.projection.GeometryProjectionUtil;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.io.EndianOutput;
import com.revolsys.gis.io.ResourceEndianOutput;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.io.xbase.FieldDefinition;
import com.revolsys.io.xbase.XbaseDataObjectWriter;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.spring.NonExistingResource;
import com.revolsys.spring.SpringUtil;
import com.revolsys.util.MathUtil;

public class ShapefileDataObjectWriter extends XbaseDataObjectWriter {
  private static final Logger LOG = Logger.getLogger(ShapefileDataObjectWriter.class);

  private static final ShapefileGeometryUtil SHP_WRITER = ShapefileGeometryUtil.SHP_INSTANCE;

  private final Envelope envelope = new Envelope();

  private GeometryFactory geometryFactory;

  private String geometryPropertyName = "geometry";

  private Method geometryWriteMethod;

  private boolean hasGeometry = false;

  private ResourceEndianOutput indexOut;

  private ResourceEndianOutput out;

  private int recordNumber = 1;

  private final Resource resource;

  private final double zMax = 0; // Double.MIN_VALUE;

  private final double zMin = 0; // Double.MAX_VALUE;

  private int shapeType = ShapefileConstants.NULL_SHAPE;

  public ShapefileDataObjectWriter(final DataObjectMetaData metaData,
    final Resource resource) {
    super(metaData, SpringUtil.getResourceWithExtension(resource, "dbf"));
    this.resource = resource;
  }

  @Override
  protected int addDbaseField(final String name, final DataType dataType,
    final Class<?> typeJavaClass, final int length, final int scale) {
    if (Geometry.class.isAssignableFrom(typeJavaClass)) {
      if (hasGeometry) {
        return super.addDbaseField(name, DataTypes.STRING, String.class, 254, 0);
      } else {
        hasGeometry = true;
        addFieldDefinition(name, FieldDefinition.OBJECT_TYPE, 0);
        return 0;
      }
    } else {
      return super.addDbaseField(name, dataType, typeJavaClass, length, scale);
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
    geometryWriteMethod = ShapefileGeometryUtil.getWriteMethod(geometry);
    shapeType = ShapefileGeometryUtil.getShapeType(geometry);
  }

  private void createPrjFile(
    final com.revolsys.jts.geom.GeometryFactory geometryFactory)
    throws IOException {
    if (geometryFactory != null) {
      final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
      if (coordinateSystem != null) {
        final int srid = coordinateSystem.getId();
        final Resource prjResource = SpringUtil.getResourceWithExtension(
          resource, "prj");
        if (!(prjResource instanceof NonExistingResource)) {
          final OutputStream out = SpringUtil.getOutputStream(prjResource);
          final PrintWriter writer = new PrintWriter(
            FileUtil.createUtf8Writer(out));
          final CoordinateSystem esriCoordinateSystem = CoordinateSystems.getCoordinateSystem(new QName(
            "ESRI", String.valueOf(srid)));
          EsriCsWktWriter.write(writer, esriCoordinateSystem, -1);
          writer.close();
        }
      }
    }
  }

  @Override
  protected void init() throws IOException {
    super.init();
    final DataObjectMetaDataImpl metaData = (DataObjectMetaDataImpl)getMetaData();
    if (metaData != null) {
      geometryPropertyName = metaData.getGeometryAttributeName();
      if (geometryPropertyName != null) {

        this.out = new ResourceEndianOutput(resource);
        writeHeader(this.out);

        if (!hasField(geometryPropertyName)) {
          addFieldDefinition(geometryPropertyName, FieldDefinition.OBJECT_TYPE,
            0);
        }

        final Resource indexResource = SpringUtil.getResourceWithExtension(
          resource, "shx");
        if (!(indexResource instanceof NonExistingResource)) {
          indexOut = new ResourceEndianOutput(indexResource);
          writeHeader(indexOut);
        }
        geometryFactory = getProperty(IoConstants.GEOMETRY_FACTORY);
      }
    }
  }

  @Override
  protected void preFirstWrite(final DataObject object) throws IOException {
    if (geometryPropertyName != null) {
      if (geometryFactory == null) {
        final Geometry geometry = object.getGeometryValue();
        if (geometry != null) {
          geometryFactory = GeometryFactory.getFactory(geometry);
        }
      }
      createPrjFile(geometryFactory);
    }
  }

  @Override
  public String toString() {
    return "ShapefileWriter(" + resource + ")";
  }

  private void updateHeader(final ResourceEndianOutput out) throws IOException {
    if (out != null) {

      out.seek(24);
      final int sizeInShorts = (int)(out.length() / 2);
      out.writeInt(sizeInShorts);
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
  }

  @Override
  protected boolean writeField(final DataObject object,
    final FieldDefinition field) throws IOException {
    if (field.getFullName().equals(geometryPropertyName)) {
      final long recordIndex = out.getFilePointer();
      Geometry geometry = object.getGeometryValue();
      geometry = GeometryProjectionUtil.performCopy(geometry, geometryFactory);
      envelope.expandToInclude(geometry.getBoundingBox());
      if (geometry == null || geometry.isEmpty()) {
        writeNull(out);
      } else {
        if (geometryWriteMethod == null) {
          createGeometryWriter(geometry);
        }
        out.writeInt(recordNumber);
        SHP_WRITER.write(geometryWriteMethod, out, geometry);

        recordNumber++;
        if (indexOut != null) {
          final long recordLength = out.getFilePointer() - recordIndex;
          final int offsetShort = (int)(recordIndex / MathUtil.BYTES_IN_SHORT);
          indexOut.writeInt(offsetShort);
          final int lengthShort = (int)(recordLength / MathUtil.BYTES_IN_SHORT) - 4;
          indexOut.writeInt(lengthShort);
        }
      }
      return true;
    } else {
      return super.writeField(object, field);
    }
  }

  private void writeHeader(final EndianOutput out) throws IOException {
    out.writeInt(ShapefileConstants.FILE_CODE);
    for (int i = 0; i < 5; i++) { // Unused
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
}
