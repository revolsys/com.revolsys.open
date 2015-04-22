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
package com.revolsys.format.shp;

import java.io.IOException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.format.xbase.XBaseFieldDefinition;
import com.revolsys.format.xbase.XbaseRecordWriter;
import com.revolsys.gis.cs.esri.EsriCoordinateSystems;
import com.revolsys.gis.io.EndianOutput;
import com.revolsys.gis.io.ResourceEndianOutput;
import com.revolsys.io.IoConstants;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.spring.NonExistingResource;
import com.revolsys.spring.SpringUtil;
import com.revolsys.util.MathUtil;

public class ShapefileRecordWriter extends XbaseRecordWriter {
  private static final Logger LOG = Logger.getLogger(ShapefileRecordWriter.class);

  private static final ShapefileGeometryUtil SHP_WRITER = ShapefileGeometryUtil.SHP_INSTANCE;

  private BoundingBox envelope = new BoundingBoxDoubleGf();

  private GeometryFactory geometryFactory;

  private String geometryPropertyName = "geometry";

  private Method geometryWriteMethod;

  private boolean hasGeometry = false;

  private ResourceEndianOutput indexOut;

  private ResourceEndianOutput out;

  private int recordNumber = 1;

  private final Resource resource;

  private int shapeType = ShapefileConstants.NULL_SHAPE;

  private DataType geometryDataType;

  public ShapefileRecordWriter(final RecordDefinition recordDefinition,
    final Resource resource) {
    super(recordDefinition,
      SpringUtil.getResourceWithExtension(resource, "dbf"));
    this.resource = resource;
  }

  @Override
  protected int addDbaseField(final String name, final DataType dataType,
    final Class<?> typeJavaClass, final int length, final int scale) {
    if (Geometry.class.isAssignableFrom(typeJavaClass)) {
      if (this.hasGeometry) {
        return super.addDbaseField(name, DataTypes.STRING, String.class, 254, 0);
      } else {
        this.hasGeometry = true;
        addFieldDefinition(name, XBaseFieldDefinition.OBJECT_TYPE, 0, 0);
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
      updateHeader(this.out);
      if (this.indexOut != null) {
        updateHeader(this.indexOut);
      }
    } catch (final IOException e) {
      LOG.error(e.getMessage(), e);
    } finally {
      this.out = null;
      this.indexOut = null;
    }
  }

  private void doubleNotNaN(final ResourceEndianOutput out, final double value)
      throws IOException {
    if (MathUtil.isNanOrInfinite(value)) {
      out.writeLEDouble(0);
    } else {
      out.writeLEDouble(value);
    }
  }

  @Override
  protected void init() throws IOException {
    super.init();
    final RecordDefinitionImpl recordDefinition = (RecordDefinitionImpl)getRecordDefinition();
    if (recordDefinition != null) {
      this.geometryPropertyName = recordDefinition.getGeometryFieldName();
      if (this.geometryPropertyName != null) {

        this.out = new ResourceEndianOutput(this.resource);
        writeHeader(this.out);

        if (!hasField(this.geometryPropertyName)) {
          addFieldDefinition(this.geometryPropertyName,
            XBaseFieldDefinition.OBJECT_TYPE, 0, 0);
        }

        final Resource indexResource = SpringUtil.getResourceWithExtension(
          this.resource, "shx");
        if (!(indexResource instanceof NonExistingResource)) {
          this.indexOut = new ResourceEndianOutput(indexResource);
          writeHeader(this.indexOut);
        }
        this.geometryFactory = getProperty(IoConstants.GEOMETRY_FACTORY);
        final Object geometryType = getProperty(IoConstants.GEOMETRY_TYPE);
        if (geometryType != null) {
          this.geometryDataType = DataTypes.getType(geometryType.toString());
        }
      }
    }
  }

  @Override
  protected void preFirstWrite(final Record object) throws IOException {
    if (this.geometryPropertyName != null) {
      final Geometry geometry = object.getGeometryValue();
      if (geometry != null) {
        if (this.geometryFactory == null) {
          this.geometryFactory = geometry.getGeometryFactory();
        }
        if (this.geometryDataType == null) {
          this.geometryDataType = object.getRecordDefinition()
              .getGeometryField()
              .getType();
          if (DataTypes.GEOMETRY.equals(this.geometryDataType)) {
            final String geometryType = geometry.getGeometryType();
            this.geometryDataType = DataTypes.getType(geometryType);
          }
        }
        this.shapeType = ShapefileGeometryUtil.SHP_INSTANCE.getShapeType(
          this.geometryFactory, this.geometryDataType);
        this.geometryWriteMethod = ShapefileGeometryUtil.getWriteMethod(
          this.geometryFactory, this.geometryDataType);
      }
      EsriCoordinateSystems.createPrjFile(this.resource, this.geometryFactory);
    }
  }

  @Override
  public String toString() {
    return "ShapefileWriter(" + this.resource + ")";
  }

  private void updateHeader(final ResourceEndianOutput out) throws IOException {
    if (out != null) {

      out.seek(24);
      final int sizeInShorts = (int)(out.length() / 2);
      out.writeInt(sizeInShorts);
      out.seek(32);
      out.writeLEInt(this.shapeType);
      doubleNotNaN(out, this.envelope.getMinX());
      doubleNotNaN(out, this.envelope.getMinY());
      doubleNotNaN(out, this.envelope.getMaxX());
      doubleNotNaN(out, this.envelope.getMaxY());
      doubleNotNaN(out, this.envelope.getMin(2));
      doubleNotNaN(out, this.envelope.getMax(2));
      doubleNotNaN(out, this.envelope.getMin(3));
      doubleNotNaN(out, this.envelope.getMax(3));
      out.close();
    }
  }

  @Override
  protected boolean writeField(final Record object,
    final XBaseFieldDefinition field) throws IOException {
    if (field.getFullName().equals(this.geometryPropertyName)) {
      final long recordIndex = this.out.getFilePointer();
      Geometry geometry = object.getGeometryValue();
      if (geometry != null) {
        geometry = geometry.convert(this.geometryFactory);
      }
      this.out.writeInt(this.recordNumber++);
      if (geometry == null || geometry.isEmpty()) {
        writeNull(this.out);
      } else {
        this.envelope = this.envelope.expandToInclude(geometry.getBoundingBox());
        SHP_WRITER.write(this.geometryWriteMethod, this.out, geometry);
      }
      if (this.indexOut != null) {
        final long recordLength = this.out.getFilePointer() - recordIndex;
        final int offsetShort = (int)(recordIndex / MathUtil.BYTES_IN_SHORT);
        this.indexOut.writeInt(offsetShort);
        final int lengthShort = (int)(recordLength / MathUtil.BYTES_IN_SHORT) - 4;
        this.indexOut.writeInt(lengthShort);
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
