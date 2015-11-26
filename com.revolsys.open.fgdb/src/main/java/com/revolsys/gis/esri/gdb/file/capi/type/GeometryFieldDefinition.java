package com.revolsys.gis.esri.gdb.file.capi.type;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.gis.esri.gdb.file.FileGdbRecordStore;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.io.EndianInput;
import com.revolsys.io.FileUtil;
import com.revolsys.io.endian.EndianInputStream;
import com.revolsys.io.endian.EndianOutput;
import com.revolsys.io.endian.EndianOutputStream;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.esri.gdb.xml.model.Field;
import com.revolsys.record.io.format.esri.gdb.xml.model.GeometryDef;
import com.revolsys.record.io.format.esri.gdb.xml.model.SpatialReference;
import com.revolsys.record.io.format.esri.gdb.xml.model.enums.GeometryType;
import com.revolsys.record.io.format.shp.ShapefileConstants;
import com.revolsys.record.io.format.shp.ShapefileGeometryUtil;
import com.revolsys.record.property.FieldProperties;
import com.revolsys.util.Booleans;

public class GeometryFieldDefinition extends AbstractFileGdbFieldDefinition {

  public static final Map<GeometryType, DataType> GEOMETRY_TYPE_DATA_TYPE_MAP = new LinkedHashMap<GeometryType, DataType>();

  private static final ShapefileGeometryUtil SHP_UTIL = new ShapefileGeometryUtil(false);

  static {
    GEOMETRY_TYPE_DATA_TYPE_MAP.put(GeometryType.esriGeometryPoint, DataTypes.POINT);
    GEOMETRY_TYPE_DATA_TYPE_MAP.put(GeometryType.esriGeometryMultipoint, DataTypes.MULTI_POINT);
    GEOMETRY_TYPE_DATA_TYPE_MAP.put(GeometryType.esriGeometryPolyline, DataTypes.MULTI_LINE_STRING);
    GEOMETRY_TYPE_DATA_TYPE_MAP.put(GeometryType.esriGeometryPolygon, DataTypes.MULTI_POLYGON);
  }

  private GeometryFactory geometryFactory = GeometryFactory.floating3();

  private Method readMethod;

  private Method writeMethod;

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
        if (this.geometryFactory == null) {
          throw new IllegalArgumentException(
            "Field definition does not include a valid coordinate system "
              + spatialReference.getLatestWKID());
        }

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
        this.readMethod = ShapefileGeometryUtil.getReadMethod(geometryTypeKey);
        if (this.readMethod == null) {
          throw new IllegalArgumentException("No read method for geometry type " + geometryTypeKey);
        }
        this.writeMethod = ShapefileGeometryUtil.getWriteMethod(geometryTypeKey);
        if (this.writeMethod == null) {
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
      final byte[] buffer;
      synchronized (getSync()) {
        buffer = row.getGeometry();
      }
      final ByteArrayInputStream byteIn = new ByteArrayInputStream(buffer);
      final EndianInput in = new EndianInputStream(byteIn);
      try {
        final int type = in.readLEInt();
        if (type == 0) {
          final DataType dataType = getDataType();
          if (DataTypes.POINT.equals(dataType)) {
            return this.geometryFactory.point();
          } else if (DataTypes.MULTI_POINT.equals(dataType)) {
            return this.geometryFactory.multiPoint();
          } else if (DataTypes.LINE_STRING.equals(dataType)) {
            return this.geometryFactory.lineString();
          } else if (DataTypes.MULTI_LINE_STRING.equals(dataType)) {
            return this.geometryFactory.multiLineString();
          } else if (DataTypes.POLYGON.equals(dataType)) {
            return this.geometryFactory.polygon();
          } else if (DataTypes.MULTI_POLYGON.equals(dataType)) {
            return this.geometryFactory.multiPolygon();
          } else {
            return null;
          }
        } else {
          final Geometry geometry = SHP_UTIL.read(this.readMethod, this.geometryFactory, in, -1);
          return geometry;
        }
      } catch (final IOException e) {
        throw new RuntimeException(e);
      } finally {
        FileUtil.closeSilent(in);
      }
    }
  }

  @Override
  public void setValue(final Record record, final Row row, final Object value) {
    final String name = getName();
    if (value == null) {
      setNull(row);
    } else if (value instanceof Geometry) {
      final Geometry geometry = (Geometry)value;
      final Geometry projectedGeometry = geometry.convert(this.geometryFactory);
      final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
      final EndianOutput out = new EndianOutputStream(byteOut);
      if (geometry.isEmpty()) {
        out.writeLEInt(ShapefileConstants.NULL_SHAPE);
      } else {
        SHP_UTIL.write(this.writeMethod, out, projectedGeometry);
      }
      final byte[] bytes = byteOut.toByteArray();
      synchronized (getSync()) {
        row.setGeometry(bytes);
      }
    } else {
      throw new IllegalArgumentException(
        "Expecting a " + Geometry.class + " not a " + value.getClass() + "=" + value);
    }
  }
}
