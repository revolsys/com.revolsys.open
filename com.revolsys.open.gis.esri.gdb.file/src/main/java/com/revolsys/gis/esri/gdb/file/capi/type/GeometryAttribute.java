package com.revolsys.gis.esri.gdb.file.capi.type;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.projection.ProjectionFactory;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.gis.esri.gdb.file.capi.swig.ShapeBuffer;
import com.revolsys.gis.io.EndianInputStream;
import com.revolsys.gis.io.EndianOutput;
import com.revolsys.gis.io.EndianOutputStream;
import com.revolsys.io.EndianInput;
import com.revolsys.io.esri.gdb.xml.model.Field;
import com.revolsys.io.esri.gdb.xml.model.GeometryDef;
import com.revolsys.io.esri.gdb.xml.model.SpatialReference;
import com.revolsys.io.esri.gdb.xml.model.enums.GeometryType;
import com.revolsys.io.shp.geometry.ShapefileGeometryUtil;
import com.revolsys.util.JavaBeanUtil;
import com.vividsolutions.jts.geom.Geometry;

public class GeometryAttribute extends AbstractFileGdbAttribute {
  private static final Map<String, Method> GEOMETRY_TYPE_READ_METHOD_MAP = new LinkedHashMap<String, Method>();

  private static final Map<String, Method> GEOMETRY_TYPE_WRITE_METHOD_MAP = new LinkedHashMap<String, Method>();

  private static final Map<GeometryType, DataType> GEOMETRY_TYPE_DATA_TYPE_MAP = new LinkedHashMap<GeometryType, DataType>();

  static {
    GEOMETRY_TYPE_DATA_TYPE_MAP.put(GeometryType.esriGeometryPoint,
      DataTypes.POINT);
    GEOMETRY_TYPE_DATA_TYPE_MAP.put(GeometryType.esriGeometryMultipoint,
      DataTypes.MULTI_POINT);
    GEOMETRY_TYPE_DATA_TYPE_MAP.put(GeometryType.esriGeometryPolyline,
      DataTypes.MULTI_LINE_STRING);
    GEOMETRY_TYPE_DATA_TYPE_MAP.put(GeometryType.esriGeometryPolygon,
      DataTypes.MULTI_POLYGON);

    addReadWriteMethods("Point");
    addReadWriteMethods("Polygon");
    addReadWriteMethods("Polyline");
    addReadWriteMethods("Multipoint");
    addReadWriteMethods("MultiPatch");
  }

  private static void addMethod(
    final String action,
    final Map<String, Method> methodMap,
    final String geometryType,
    final boolean hasZ,
    final boolean hasM,
    final Class<?>... parameterTypes) {
    final String geometryTypeKey = "esriGeometry" + geometryType + hasZ + hasM;
    String methodName = action + geometryType;
    if (hasZ) {
      methodName += "Z";
    }
    if (hasM) {
      methodName += "M";
    }
    final Method method = JavaBeanUtil.getMethod(ShapefileGeometryUtil.class,
      methodName, parameterTypes);
    methodMap.put(geometryTypeKey, method);
  }

  private static void addReadWriteMethods(final String geometryType) {
    addMethod("read", GEOMETRY_TYPE_READ_METHOD_MAP, geometryType, false,
      false, GeometryFactory.class, EndianInput.class);
    addMethod("read", GEOMETRY_TYPE_READ_METHOD_MAP, geometryType, true, false,
      GeometryFactory.class, EndianInput.class);
    addMethod("read", GEOMETRY_TYPE_READ_METHOD_MAP, geometryType, false, true,
      GeometryFactory.class, EndianInput.class);
    addMethod("read", GEOMETRY_TYPE_READ_METHOD_MAP, geometryType, true, true,
      GeometryFactory.class, EndianInput.class);

    addMethod("write", GEOMETRY_TYPE_WRITE_METHOD_MAP, geometryType, false,
      false, EndianOutput.class, Geometry.class);
    addMethod("write", GEOMETRY_TYPE_WRITE_METHOD_MAP, geometryType, true,
      false, EndianOutput.class, Geometry.class);
    addMethod("write", GEOMETRY_TYPE_WRITE_METHOD_MAP, geometryType, false,
      true, EndianOutput.class, Geometry.class);
    addMethod("write", GEOMETRY_TYPE_WRITE_METHOD_MAP, geometryType, true,
      true, EndianOutput.class, Geometry.class);
  }

  private GeometryFactory geometryFactory = GeometryFactory.getFactory();

  private Method readMethod;

  private Method writeMethod;

  public GeometryAttribute(final Field field) {
    super(field.getName(), DataTypes.GEOMETRY,
      field.getRequired() == Boolean.TRUE || !field.isIsNullable());
    final GeometryDef geometryDef = field.getGeometryDef();
    if (geometryDef == null) {
      throw new IllegalArgumentException(
        "Field definition does not include a geometry definition");

    } else {
      final SpatialReference spatialReference = geometryDef.getSpatialReference();
      if (spatialReference == null) {
        throw new IllegalArgumentException(
          "Field definition does not include a spatial reference");
      } else {
        final GeometryType geometryType = geometryDef.getGeometryType();
        final DataType dataType = GEOMETRY_TYPE_DATA_TYPE_MAP.get(geometryType);
        setType(dataType);
        this.geometryFactory = spatialReference.getGeometryFactory();
        if (geometryFactory == null) {
          throw new IllegalArgumentException(
            "Field definition does not include a valid coordinate system "
              + spatialReference.getLatestWKID());
        }

        setProperty(AttributeProperties.GEOMETRY_FACTORY, geometryFactory);
        final boolean hasZ = geometryDef.isHasZ();
        final boolean hasM = geometryDef.isHasM();
        final String geometryTypeKey = geometryType.toString() + hasZ + hasM;
        readMethod = GEOMETRY_TYPE_READ_METHOD_MAP.get(geometryTypeKey);
        if (readMethod == null) {
          throw new IllegalArgumentException(
            "No read method for geometry type " + geometryTypeKey);
        }
        writeMethod = GEOMETRY_TYPE_WRITE_METHOD_MAP.get(geometryTypeKey);
        if (writeMethod == null) {
          throw new IllegalArgumentException(
            "No write method for geometry type " + geometryTypeKey);
        }
      }

    }
  }

  @Override
  public Object getValue(final Row row) {
    final String name = getName();
    if (row.isNull(name)) {
      return null;
    } else {
      final ShapeBuffer shapeBuffer = row.getGeometry();
      // final EndianInput in = new ByteArrayEndianInput(
      // shapeBuffer.getShapeBuffer());

      final byte[] buffer = shapeBuffer.getBuffer();
      final ByteArrayInputStream byteIn = new ByteArrayInputStream(buffer);
      final EndianInput in = new EndianInputStream(byteIn);
      try {
        final int type = in.readLEInt();
        if (type == 0) {
          return null;
        } else {
          final Geometry geometry = JavaBeanUtil.invokeMethod(readMethod,
            ShapefileGeometryUtil.class, geometryFactory, in);
          return geometry;
        }
      } catch (final IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public Object setValue(
    final DataObject object,
    final Row row,
    final Object value) {
    final String name = getName();
    if (value == null) {
      if (isRequired()) {
        throw new IllegalArgumentException(name
          + " is required and cannot be null");
      } else {
        row.setNull(name);
      }
      return null;
    } else if (value instanceof Geometry) {
      final Geometry geometry = (Geometry)value;
      final Geometry projectedGeometry = ProjectionFactory.convert(geometry,
        geometryFactory);
      final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
      final EndianOutput out = new EndianOutputStream(byteOut);
      JavaBeanUtil.invokeMethod(writeMethod, ShapefileGeometryUtil.class, out,
        projectedGeometry);
      final byte[] bytes = byteOut.toByteArray();
      final ShapeBuffer shape = new ShapeBuffer(bytes);
      // final ShapeBuffer shape = new ShapeBuffer(bytes.length);
      // shape.setAllocatedLength(bytes.length);
      // shape.setInUseLength(bytes.length);
      // for (int i = 0; i < bytes.length; i++) {
      // final byte b = bytes[i];
      // shape.set(i, b);
      // }
      row.setGeometry(shape);
      return new Object[] {
        shape, bytes
      };
    } else {
      throw new IllegalArgumentException("Expecting a " + Geometry.class
        + " not a " + value.getClass() + "=" + value);
    }
  }
}
