# Data Types

Values in software programming languages have some kind of data type. This can be a  primitive
(e.g. int, double, boolean) or complex (e.g. String, UUID, URI, Point, Line, Polygon, List, Map. Json).
The `org.jeometry.common.data.type.DataType` interface defines a data type and conversion from that
data  type to strings or other data types.

The `org.jeometry.common.data.type.DataTypes` class contains constants for the standard data types
and a registry to get a DataType instance from a Java Class.

Other constants for data types are defined on the following classes.

```java
com.revolsys.record.io.format.json.Json
com.revolsys.geometry.model.GeometryDataTypes
```

# Geometry

The Revolsys library includes a Geometry API. This is a heavily modified fork of the Java Topology
Suite (JTS) 1.x that is no longer backwards compatible.

The `com.revolsys.geometry.model.Geometry` interface is the parent of all geometries. With `Point`, `LineString`,
`Polygon`, `MultiPoint`, `MultiLineString`, `MultiPolygon`, `GeometryCollection` interfaces and a various implementations.

The `com.revolsys.geometry.model.GeometryFactory` class defines the coordinate systems (`jeometry-coordinatesystem`
library), axis count (2=xy, 3=xyz, 4=xyzm) and precision model (1000 = 1mm precision). The geometry
factory can then be used to create instances of the Geometry interfaces.

Geometries can be converted from one coordinate system to another using a projection library;

```
GeometryFactory bcAlbers = GeometryFactory.fixed3d(3005, 1000, 1000, 1000); // XYZ BC Albers mm precision
GeometryFactory utm10 = GeometryFactory.fixed2d(26910, 1, 1); // XY UTM Zone 10 m precision
Point bcAlbersPoint = bcAlbers.point(120000, 500000);
Point utmPoint = bcAlbersPoint.convertGeometry(bcAlbersPoint);
```

Eventually the geometry code should be moved to the `jeometry` library. The `jeometry` and `revolsys`
libraries are developed together.
 
# Unstructured Data

The Revolsys libraries use the [JSON](https://www.json.org/json-en.html) encoded files for configuration.

Parsing and access to Json data is in the `com.revolsys.record.io.format.json` package.

The `JsonObject` interface is an extended version of the `java.util.Map` for json encoded data. `JsonList`
is an extended version of `java.util.List` for json encoded data. In may places `JsonObject` should be
used instead of `Map<String, Object>` instances.

`JsonObject` currently extends form `com.revolsys.collection.map.MapEx` which defines the following methods
to access field values.

The getValue method returns and field value and auto casts it to the expected type.

```java
String text = getValue("text");
```

There are also `getXXX` methods for common data types (e.g. `getString`, `getInteger`, `getDouble`).

```java
Integer count = getInteger("count");
```

Variations on the methods are provided to return a default value for a field if the value was null.

```java
int count = getInteger("count", 0);
```

There is also a getValue method that accepts a `DataType` parameter to convert the field value. This
can be used where there is uncertainty if the field value is of that data type. For example when
reading JSON from a file it will be a WKT encoded string not a LineString geometry.

```java
LineString line = getValue("line', GeometryDataType.LINE_STRING);
```

# Structured Data Records

The Revolsys Library uses the `com.revolsys.record.Record interface` (`ArrayRecord` class)
as an abstraction for a record in a database or structured file. The `Record` interface also extends
from `MapEx` but is designed to work with structured records that have a fixed list of fields. Records
also have the `getGeometry()` method to return the geometry field.

The list of supported fields and other field/record level metadata is described using the
`com.revolsys.record.schema.RecordDefinition` interface (`RecordDefinitionImpl` implementation). The 
`com.revolsys.record.schema.FieldDefinition.FieldDefinition` class stores the metadata (e.g. name, DataType,
length, required). If a database or file format can contain multiple record types the
`com.revolsys.record.schema.RecordStoreSchema` can be used to navigate through nested schema and record
definitions. Each record definition is identified `org.jeometry.common.io.PathName` instance that abstracts
the different notations for qualified table names. So /HR/EMPLOYEE instead of HR.EMPLOYEE.

The `com.revolsys.record.schema.RecordStore` interface defines a connection to a database. For example a
PostgreSQL Database, Oracle Database, FileGDB file, GeoPackage file, OData web service, or 
ArcGIS Restweb service.

The `com.revolsys.record.query.Query` classes is used to build queries to be executed against a record store.
The following example creates a new record store connection and then queries for all employees with
the first name "Jane". This example also includes the `RecordReader` interface which is an extended
implementation of `Iterable`;

```java
JsonObject config = JsonObject.hash(
  "connection", JsonObject.hash()
    .addValue("url", "jdbc:postgresql://localhost:5432/hr")
    .addValue("username", "hruser")
    .addValue("password", "*****")
);
try (
  RecordStore recordStore = RecordStore.newRecordStoreInitialized(config);
  RecordReader reader = recordStore
    .newQuery(PathName.newPathName("/HR/EMPLOYEE)
    .and("firstName", "Jane");
) {
  for (Record record : reader) {
    System.out.println(record.getString("fullName"));
  }
}
```

Records can also be read from files (e.g. .shp, .dbf, .csv, .xlsx etc). The supported types
are in implementations of the `com.revolsys.record.io.RecordReaderFactory`. Which is an implementation
of `com.revolsys.io.IoFactory` that supports; record (read/write), geometry, geo-referenced images,
gridded dem, point cloud, tin.

```java
Path file = Paths.get("/data/test.shp");
try (
  RecordReader reader = RecordReaderFactory.newRecordReader(file);
) {
  for (Record record : reader) {
    System.out.println(record);
  }
}
```
