package com.revolsys.gis.data.model.types;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;

import com.revolsys.gis.data.model.DataObject;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public final class DataTypes {

  public static final DataType OBJECT = new SimpleDataType("object",
    Object.class);

  public static final DataType ANY_URI = new SimpleDataType("anyURI", URI.class);

  public static final DataType BASE64_BINARY = new SimpleDataType(
    "base64Binary", byte[].class);

  public static final DataType BOOLEAN = new SimpleDataType("boolean",
    Boolean.class);

  public static final DataType BYTE = new SimpleDataType("byte", Byte.class);

  static final Map<String, DataType> CLASS_TYPE_MAP = new HashMap<String, DataType>();

  public static final DataType COLLECTION = new CollectionDataType(
    "Collection", Collection.class, OBJECT);

  public static final DataType DATA_OBJECT = new SimpleDataType("DataObject",
    DataObject.class);

  public static final DataType DATE = new SimpleDataType("date",
    java.util.Date.class);

  public static final DataType DATE_TIME = new SimpleDataType("dateTime",
    Timestamp.class);

  public static final DataType DECIMAL = new SimpleDataType("decimal",
    BigDecimal.class);

  public static final DataType DOUBLE = new SimpleDataType("double",
    Double.class);

  public static final DataType DURATION = new SimpleDataType("duration",
    Date.class);

  public static final DataType FLOAT = new SimpleDataType("float", Float.class);

  public static final DataType GEOMETRY = new SimpleDataType("Geometry",
    Geometry.class);

  public static final DataType POINT = new SimpleDataType("Point", Point.class);

  public static final DataType LINE_STRING = new SimpleDataType("LineString",
    LineString.class);

  public static final DataType MULTI_LINE_STRING = new SimpleDataType(
    "MultiLineString", MultiLineString.class);

  public static final DataType POLYGON = new SimpleDataType("Polygon",
    Polygon.class);

  public static final DataType MULTI_POINT = new SimpleDataType("MultiPoint",
    Point.class);

  public static final DataType MULTI_POLYGON = new SimpleDataType(
    "MultiPolygon", Polygon.class);

  public static final DataType INT = new SimpleDataType("int", Integer.class);

  public static final DataType INTEGER = new SimpleDataType("integer",
    BigInteger.class);

  public static final DataType LIST = new CollectionDataType("List",
    List.class, OBJECT);

  private static final Logger LOG = Logger.getLogger(DataTypes.class);

  public static final DataType LONG = new SimpleDataType("long", Long.class);

  static final Map<QName, DataType> NAME_TYPE_MAP = new HashMap<QName, DataType>();

  public static final DataType QNAME = new SimpleDataType("QName", QName.class);

  public static final DataType RELATION = new CollectionDataType("Relation",
    Collection.class, OBJECT);

  public static final DataType SET = new CollectionDataType("Set", Set.class,
    OBJECT);

  public static final DataType SHORT = new SimpleDataType("short", Short.class);

  public static final DataType STRING = new SimpleDataType("string",
    String.class);

  public static final DataType MAP = new SimpleDataType("Map", Map.class);

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
  }

  public static DataType getType(
    final Class<?> clazz) {
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

  public static DataType getType(
    final QName typeName) {
    final DataType type = NAME_TYPE_MAP.get(typeName);
    if (type == null) {
      return OBJECT;
    } else {
      return type;
    }
  }

  public static void register(
    final DataType type) {
    final QName typeName = type.getName();
    NAME_TYPE_MAP.put(typeName, type);
    final Class<?> typeClass = type.getJavaClass();
    final String typeClassName = typeClass.getName();
    CLASS_TYPE_MAP.put(typeClassName, type);
  }

  private DataTypes() {
  }

}
