package com.revolsys.datatype;

import java.awt.Color;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.net.URI;
import java.sql.Blob;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.measure.Quantity;
import javax.xml.namespace.QName;

import org.apache.log4j.Logger;

import com.revolsys.awt.WebColors;
import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryCollection;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.MultiLineString;
import com.revolsys.geometry.model.MultiPoint;
import com.revolsys.geometry.model.MultiPolygon;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.identifier.Identifier;
import com.revolsys.io.FileUtil;
import com.revolsys.io.PathName;
import com.revolsys.record.RecordDataType;
import com.revolsys.record.code.CodeDataType;
import com.revolsys.util.Booleans;
import com.revolsys.util.Dates;
import com.revolsys.util.QuantityType;
import com.revolsys.util.UrlUtil;
import com.revolsys.util.number.BigDecimals;
import com.revolsys.util.number.BigIntegers;
import com.revolsys.util.number.Bytes;
import com.revolsys.util.number.Doubles;
import com.revolsys.util.number.Floats;
import com.revolsys.util.number.Integers;
import com.revolsys.util.number.Longs;
import com.revolsys.util.number.Shorts;

// TODO manage data types by classloader and allow unloading of registered classes.
public final class DataTypes {
  private static final Map<String, DataType> CLASS_TYPE_MAP = new HashMap<>();

  private static final Map<String, DataType> NAME_TYPE_MAP = new HashMap<>();

  public static final DataType ANY_URI = new FunctionDataType("anyURI", URI.class, UrlUtil::toUri);

  public static final DataType BASE64_BINARY = new SimpleDataType("base64Binary", byte[].class);

  public static final DataType BLOB = new SimpleDataType("blob", Blob.class);

  public static final DataType BOOLEAN = new FunctionDataType("boolean", Boolean.class,
    Booleans::valueOf);

  public static final DataType BOUNDING_BOX = new FunctionDataType("boolean", BoundingBox.class,
    BoundingBox::newBoundingBox);

  public static final DataType BYTE = new Bytes();

  public static final DataType COLOR = new FunctionDataType("color", Color.class,
    WebColors::toColor, WebColors::toString);

  public static final DataType CODE = new CodeDataType();

  public static final DataType DATE = new FunctionDataType("date", java.util.Date.class,
    Dates::getDate, Dates::toDateTimeString, Dates::equalsNotNull);

  public static final DataType DATE_TIME = new FunctionDataType("dateTime", Timestamp.class,
    Dates::getTimestamp, Dates::toTimestampString, Dates::equalsNotNull);

  public static final DataType DECIMAL = new BigDecimals();

  public static final DataType DOUBLE = new Doubles();

  public static final DataType DURATION = new SimpleDataType("duration", Date.class);

  public static final DataType FILE = new FunctionDataType("File", File.class, FileUtil::newFile);

  public static final DataType FLOAT = new Floats();

  public static final DataType GEOMETRY = FunctionDataType.newToObjectEquals("Geometry",
    Geometry.class, Geometry::newGeometry, Geometry::equalsExact);

  public static final DataType GEOMETRY_COLLECTION = FunctionDataType.newToObjectEquals(
    "GeometryCollection", GeometryCollection.class, GeometryCollection::newGeometryCollection,
    Geometry::equalsExact);

  public static final DataType IDENTIFIER = new FunctionDataType("identifier", Identifier.class,
    Identifier::newIdentifier);

  public static final DataType INT = new Integers();

  public static final DataType INTEGER = new BigIntegers();

  public static final DataType LINE_STRING = FunctionDataType.newToObjectEquals("LineString",
    LineString.class, LineString::newLineString, Geometry::equalsExact);

  public static final DataType LINEAR_RING = FunctionDataType.newToObjectEquals("LinearRing",
    LinearRing.class, LinearRing::newLinearRing, Geometry::equalsExact);

  private static final Logger LOG = Logger.getLogger(DataTypes.class);

  public static final DataType LONG = new Longs();

  public static final DataType MEASURE = new FunctionDataType("measure", Quantity.class,
    QuantityType::newQuantity, QuantityType::toString);

  public static final DataType MULTI_LINE_STRING = FunctionDataType.newToObjectEquals(
    "MultiLineString", MultiLineString.class, Lineal::newLineal, Geometry::equalsExact);

  public static final DataType MULTI_POINT = FunctionDataType.newToObjectEquals("MultiPoint",
    MultiPoint.class, Punctual::newPunctual, Geometry::equalsExact);

  public static final DataType MULTI_POLYGON = FunctionDataType.newToObjectEquals("MultiPolygon",
    MultiPolygon.class, Polygonal::newPolygonal, Geometry::equalsExact);

  public static final DataType OBJECT = new ObjectDataType();

  public static final DataType POINT = FunctionDataType.newToObjectEquals("Point", Point.class,
    Point::newPoint, Geometry::equalsExact);

  public static final DataType POLYGON = FunctionDataType.newToObjectEquals("Polygon",
    Polygon.class, Polygon::newPolygon, Geometry::equalsExact);

  public static final DataType QNAME = new SimpleDataType("QName", QName.class);

  public static final DataType PATH_NAME = new FunctionDataType("pathName", PathName.class,
    PathName::newPathName);

  public static final DataType RECORD = new RecordDataType();

  public static final DataType SHORT = new Shorts();

  public static final DataType SQL_DATE = new FunctionDataType("date", java.sql.Date.class,
    Dates::getSqlDate, Dates::toSqlDateString, Dates::equalsNotNull);

  public static final DataType STRING = new FunctionDataType("string", String.class,
    Object::toString);

  public static final DataType XML = new FunctionDataType("xml", String.class, Object::toString);

  public static final DataType TIME = new SimpleDataType("time", Time.class);

  public static final DataType TIMESTAMP = new FunctionDataType("timestamp", Timestamp.class,
    Dates::getTimestamp, Dates::toTimestampString, Dates::equalsNotNull);

  public static final DataType URL = new FunctionDataType("url", java.net.URL.class,
    UrlUtil::toUrl);

  public static final DataType UUID = new SimpleDataType("uuid", UUID.class);

  public static final DataType COLLECTION = new CollectionDataType("Collection", Collection.class,
    OBJECT);

  public static final DataType LIST = new CollectionDataType("List", List.class, OBJECT);

  public static final DataType MAP = new FunctionDataType("Map", Map.class, (value) -> {
    if (value instanceof Map) {
      return (Map)value;
    } else {
      return value;
    }
  }, Maps::equalsNotNull, Maps::equalsNotNull);

  public static final DataType RELATION = new CollectionDataType("Relation", Collection.class,
    OBJECT);

  public static final DataType SET = new CollectionDataType("Set", Set.class, OBJECT);

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

  public static DataType getDataType(final Class<?> clazz) {
    if (clazz == null) {
      return OBJECT;
    } else {
      DataType dataType = CLASS_TYPE_MAP.get(clazz.getName());
      if (dataType == null) {
        final Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces != null) {
          for (final Class<?> inter : interfaces) {
            dataType = getDataType(inter);
            if (dataType != null && dataType != OBJECT) {
              return dataType;
            }
          }
        }
        return getDataType(clazz.getSuperclass());
      } else {
        return dataType;
      }
    }
  }

  public static DataType getDataType(final Object object) {
    if (object == null) {
      return OBJECT;
    } else if (object instanceof DataTypeProxy) {
      final DataTypeProxy proxy = (DataTypeProxy)object;
      return proxy.getDataType();
    } else if (object instanceof DataType) {
      final DataType type = (DataType)object;
      return type;
    } else {
      final Class<?> clazz = object.getClass();
      return getDataType(clazz);
    }
  }

  public static DataType getDataType(final String name) {
    if (name == null) {
      return OBJECT;
    } else {
      final DataType type = NAME_TYPE_MAP.get(name.toLowerCase());
      if (type == null) {
        return OBJECT;
      } else {
        return type;
      }
    }
  }

  public static DataType getDataType(final Type type) {
    if (type instanceof Class) {
      final Class<?> clazz = (Class<?>)type;
      return getDataType(clazz);
    } else {
      throw new IllegalArgumentException("Cannot get dataType for: " + type);
    }
  }

  public static void register(final Class<?> typeClass, final DataType type) {
    final String typeClassName = typeClass.getName();
    CLASS_TYPE_MAP.put(typeClassName, type);
  }

  public static void register(final DataType type) {
    final String name = type.getName().toLowerCase();
    if (!NAME_TYPE_MAP.containsKey(name)) {
      NAME_TYPE_MAP.put(name, type);
    }
    final Class<?> typeClass = type.getJavaClass();
    register(typeClass, type);
  }

  public static void register(final String name, final Class<?> javaClass) {
    final DataType type = new SimpleDataType(name, javaClass);
    register(type);
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public static <V> V toObject(final Class<?> clazz, final Object value) {
    // TODO enum
    if (value == null) {
      return null;
    } else if (clazz.isAssignableFrom(value.getClass())) {
      return (V)value;
    } else {
      if (clazz.isEnum()) {
        try {
          return (V)Enum.valueOf((Class<Enum>)clazz, value.toString());
        } catch (final Throwable e) {
        }
      }
      final DataType dataType = getDataType(clazz);
      if (dataType == null) {
        return (V)value;
      } else {
        return dataType.toObject(value);
      }
    }
  }

  public static String toString(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof String) {
      return (String)value;
    } else {
      final Class<?> valueClass = value.getClass();
      final DataType dataType = getDataType(valueClass);
      if (dataType == null) {
        return value.toString();
      } else {
        return dataType.toString(value);
      }
    }
  }

  private DataTypes() {
  }
}
