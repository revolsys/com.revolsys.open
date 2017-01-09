package com.revolsys.gis.esri.gdb.file.capi.type;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.gis.esri.gdb.file.FileGdbRecordStore;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.io.endian.EndianOutput;
import com.revolsys.io.endian.EndianOutputStream;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.esri.gdb.xml.model.Field;
import com.revolsys.record.io.format.esri.gdb.xml.model.GeometryDef;
import com.revolsys.record.io.format.esri.gdb.xml.model.SpatialReference;
import com.revolsys.record.io.format.esri.gdb.xml.model.enums.GeometryType;
import com.revolsys.record.io.format.shp.ShapefileGeometryHandler;
import com.revolsys.record.property.FieldProperties;
import com.revolsys.util.Booleans;
import com.revolsys.util.Exceptions;
import com.revolsys.util.function.Function3;

public class GeometryFieldDefinition extends AbstractFileGdbFieldDefinition {

  public static final Map<GeometryType, DataType> GEOMETRY_TYPE_DATA_TYPE_MAP = new LinkedHashMap<>();

  private static final ShapefileGeometryHandler SHAPEFILE_GEOMETRY_HANDLER = new ShapefileGeometryHandler(
    false);

  static {
    GEOMETRY_TYPE_DATA_TYPE_MAP.put(GeometryType.esriGeometryPoint, DataTypes.POINT);
    GEOMETRY_TYPE_DATA_TYPE_MAP.put(GeometryType.esriGeometryMultipoint, DataTypes.MULTI_POINT);
    GEOMETRY_TYPE_DATA_TYPE_MAP.put(GeometryType.esriGeometryPolyline, DataTypes.MULTI_LINE_STRING);
    GEOMETRY_TYPE_DATA_TYPE_MAP.put(GeometryType.esriGeometryPolygon, DataTypes.MULTI_POLYGON);
  }

  private static final Integer MINUS1 = -1;

  private GeometryFactory geometryFactory = GeometryFactory.DEFAULT_3D;

  private BiConsumer<EndianOutput, Geometry> writeFunction;

  private Function3<GeometryFactory, ByteBuffer, Integer, Geometry> readFunction;

  public GeometryFieldDefinition(final Field field) {
    super(field.getName(), DataTypes.GEOMETRY,
      Booleans.getBoolean(field.getRequired()) || !field.isIsNullable());
    final GeometryDef geometryDef = field.getGeometryDef();
    if (geometryDef == null) {
      throw new IllegalArgumentException("Field definition does not include a geometry definition");

    } else {
      final SpatialReference spatialReference = geometryDef.getSpatialReference();
      if (spatialReference == null) {
        throw new IllegalArgumentException("Field definition does not include a spatial reference");
      } else {
        final GeometryType geometryType = geometryDef.getGeometryType();
        final DataType dataType = GEOMETRY_TYPE_DATA_TYPE_MAP.get(geometryType);
        setType(dataType);
        this.geometryFactory = spatialReference.getGeometryFactory();

        int axisCount = 2;
        final boolean hasZ = geometryDef.isHasZ();
        if (hasZ) {
          axisCount = 3;
        }
        final boolean hasM = geometryDef.isHasM();
        if (hasM) {
          axisCount = 4;
        }
        if (axisCount != this.geometryFactory.getAxisCount()) {
          final int srid = this.geometryFactory.getCoordinateSystemId();
          final double scaleXY = this.geometryFactory.getScaleXY();
          final double scaleZ = this.geometryFactory.getScaleZ();
          this.geometryFactory = GeometryFactory.fixed(srid, axisCount, scaleXY, scaleZ);
        }
        setProperty(FieldProperties.GEOMETRY_FACTORY, this.geometryFactory);

        final String geometryTypeKey = dataType.toString() + hasZ + hasM;
        this.readFunction = SHAPEFILE_GEOMETRY_HANDLER.getReadFunction(geometryTypeKey);
        if (this.readFunction == null) {
          throw new IllegalArgumentException("No read method for geometry type " + geometryTypeKey);
        }
        this.writeFunction = SHAPEFILE_GEOMETRY_HANDLER.getWriteFunction(geometryTypeKey);
        if (this.writeFunction == null) {
          throw new IllegalArgumentException(
            "No write method for geometry type " + geometryTypeKey);
        }
      }

    }
  }

  @Override
  public int getMaxStringLength() {
    return 40;
  }

  @Override
  public Object getValue(final Row row) {
    final String name = getName();
    final FileGdbRecordStore recordStore = getRecordStore();
    if (recordStore.isNull(row, name)) {
      return null;
    } else {
      final byte[] bytes;
      synchronized (getSync()) {
        bytes = row.getGeometry();
      }
      final ByteBuffer buffer = ByteBuffer.wrap(bytes);
      buffer.order(ByteOrder.LITTLE_ENDIAN);

      final int geometryType = buffer.getInt();
      if (geometryType == 0) {
        final DataType dataType = getDataType();
        if (DataTypes.POINT.equals(dataType)) {
          return this.geometryFactory.point();
        } else if (DataTypes.MULTI_POINT.equals(dataType)) {
          return this.geometryFactory.point();
        } else if (DataTypes.LINE_STRING.equals(dataType)) {
          return this.geometryFactory.lineString();
        } else if (DataTypes.MULTI_LINE_STRING.equals(dataType)) {
          return this.geometryFactory.lineString();
        } else if (DataTypes.POLYGON.equals(dataType)) {
          return this.geometryFactory.polygon();
        } else if (DataTypes.MULTI_POLYGON.equals(dataType)) {
          return this.geometryFactory.polygon();
        } else {
          return null;
        }
      } else {
        final Geometry geometry = this.readFunction.apply(this.geometryFactory, buffer, MINUS1);
        return geometry;
      }
    }
  }

  @Override
  public void setValue(final Record record, final Row row, final Object value) {
    if (value == null) {
      setNull(row);
    } else if (value instanceof Geometry) {
      final Geometry geometry = (Geometry)value;
      final Geometry projectedGeometry = geometry.convertGeometry(this.geometryFactory);
      if (projectedGeometry.isEmpty()) {
        setNull(row);
      } else {
        byte[] bytes;
        if (geometry.isEmpty()) {
          bytes = new byte[] {
            0, 0, 0, 0
          };
        } else {
          try (
            final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            final EndianOutput out = new EndianOutputStream(byteOut)) {
            this.writeFunction.accept(out, projectedGeometry);
            bytes = byteOut.toByteArray();
          } catch (final IOException e) {
            throw Exceptions.wrap(e);
          }
        }
        synchronized (getSync()) {
          row.setGeometry(bytes);
        }
      }
    } else {
      throw new IllegalArgumentException(
        "Expecting a " + Geometry.class + " not a " + value.getClass() + "=" + value);
    }
  }
}
