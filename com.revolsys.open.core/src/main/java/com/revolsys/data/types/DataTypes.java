package com.revolsys.data.types;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.sql.Blob;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;

import com.revolsys.data.record.Record;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;

public final class DataTypes {

  public static final DataType OBJECT = new SimpleDataType("object", Object.class);

  public static final DataType ANY_URI = new SimpleDataType("anyURI", URI.class);

  public static final DataType BASE64_BINARY = new SimpleDataType("base64Binary", byte[].class);

  public static final DataType BOOLEAN = new SimpleDataType("boolean", Boolean.class);

  public static final DataType BYTE = new SimpleDataType("byte", Byte.class);

  static final Map<String, DataType> CLASS_TYPE_MAP = new HashMap<String, DataType>();

  public static final DataType COLLECTION = new CollectionDataType("Collection", Collection.class,
    OBJECT);

  public static final DataType DATA_OBJECT = new SimpleDataType("Record", Record.class);

  public static final DataType DATE = new SimpleDataType("date", java.util.Date.class);

  public static final DataType DATE_TIME = new SimpleDataType("dateTime", Timestamp.class);

  public static final DataType DECIMAL = new SimpleDataType("decimal", BigDecimal.class);

  public static final DataType DOUBLE = new SimpleDataType("double", Double.class);

  public static final DataType DURATION = new SimpleDataType("duration", Date.class);

  public static final DataType FLOAT = new SimpleDataType("float", Float.class);

  public static final DataType GEOMETRY = new SimpleDataType("Geometry", Geometry.class);

  public static final DataType GEOMETRY_COLLECTION = new SimpleDataType("GeometryCollection",
    GeometryCollection.class);

  public static final DataType INT = new SimpleDataType("int", Integer.class);

  public static final DataType INTEGER = new SimpleDataType("integer", BigInteger.class);

  public static final DataType LINE_STRING = new SimpleDataType("LineString", LineString.class);

  public static final DataType LINEAR_RING = new SimpleDataType("LinearRing", LinearRing.class);

  public static final DataType LIST = new CollectionDataType("List", List.class, OBJECT);

  private static final Logger LOG = Logger.getLogger(DataTypes.class);

  public static final DataType LONG = new SimpleDataType("long", Long.class);

  public static final DataType MAP = new SimpleDataType("Map", Map.class);

  public static final DataType MULTI_LINE_STRING = new SimpleDataType("MultiLineString",
    MultiLineString.class);

  public static final DataType MULTI_POINT = new SimpleDataType("MultiPoint", MultiPoint.class);

  public static final DataType MULTI_POLYGON = new SimpleDataType("MultiPolygon",
    MultiPolygon.class);

  static final Map<String, DataType> NAME_TYPE_MAP = new HashMap<String, DataType>();

  public static final DataType POINT = new SimpleDataType("Point", Point.class);

  public static final DataType POLYGON = new SimpleDataType("Polygon", Polygon.class);

  public static final DataType QNAME = new SimpleDataType("QName", QName.class);

  public static final DataType RELATION = new CollectionDataType("Relation", Collection.class,
    OBJECT);

  public static final DataType SET = new CollectionDataType("Set", Set.class, OBJECT);

  public static final DataType SHORT = new SimpleDataType("short", Short.class);

  public static final DataType STRING = new SimpleDataType("string", String.class);

  public static final DataType URL = new SimpleDataType("url", java.net.URL.class);

  public static final DataType BLOB = new SimpleDataType("blob", Blob.class);

  public static final DataType COLOR = new SimpleDataType("color", Color.class);

  static {
    final Field[] fields = DataTypes.class.getDeclaredFields();
    for (final Field field : fields) {
      if (Modifier.isStatic(field.getModifiers())) {
        if (DataType.class.isAssignableFrom(field.getType())) {
          try {
            final DataType type = (DataType)field.get(null);
            register(type);
          } catch (final Throwable e) {
            LOG.error("Error registering type " + field.getName(), e);
          }
        }
      }
    }
    register(Boolean.TYPE, BOOLEAN);
    register(Byte.TYPE, BYTE);
    register(Short.TYPE, SHORT);
    register(Integer.TYPE, INT);
    register(Long.TYPE, LONG);
    register(Float.TYPE, FLOAT);
    register(Double.TYPE, DOUBLE);
  }

  public static DataType getType(final Class<?> clazz) {
    final String className = clazz.getName();
    final DataType type = CLASS_TYPE_MAP.get(className);
    if (type == null) {

      if (List.class.isAssignableFrom(clazz)) {
        return LIST;
      } else if (Set.class.isAssignableFrom(clazz)) {
        return SET;
      } else {
        return OBJECT;
      }
    } else {
      return type;
    }
  }

  public static DataType getType(final Object object) {
    if (object == null) {
      return null;
    } else if (object instanceof DataTypeProxy) {
      final DataTypeProxy proxy = (DataTypeProxy)object;
      return proxy.getDataType();
    } else {
      final Class<?> clazz = object.getClass();
      return getType(clazz);
    }
  }

  public static DataType getType(final String name) {
    final DataType type = NAME_TYPE_MAP.get(name);
    if (type == null) {
      return OBJECT;
    } else {
      return type;
    }
  }

  public static void register(final Class<?> typeClass, final DataType type) {
    final String typeClassName = typeClass.getName();
    CLASS_TYPE_MAP.put(typeClassName, type);
  }

  public static void register(final DataType type) {
    final String name = type.getName();
    NAME_TYPE_MAP.put(name, type);
    final Class<?> typeClass = type.getJavaClass();
    register(typeClass, type);
  }

  public static void register(final String name, final Class<?> javaClass) {
    final DataType type = new SimpleDataType(name, javaClass);
    register(type);
  }

  private DataTypes() {
  }

}
