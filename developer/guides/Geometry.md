# Geometry Guide

# Coordinate Systems

The org.jeometry.coordinatesystem.model includes definitions of coordinate systems generally following the models defined by the EPSG.

* CoordinateSystem
    * HorizontalCoordinateSystem
        * EngineeringCoordinateSystem
        * GeocentricCoordinateSystem
        * geographicCoordinateSystem
        * ProjectedCoordinateSystem
    * CompoundCoordinateSystem
    * VerticalCoordinateSystem

The following CoordinatesProjection implementations are provided.

* AlbersConicEqualArea
* CoordinatesProjection
* LambertConicConformal
* LambertConicConformal1SP
* Mercator1SP
* Mercator1SPSpherical
* Mercator2SP
* TransverseMercator
* TransverseMercatorJhs
* TransverseMercatorSouthOriented
* TransverseMercatorThomas
* TransverseMercatorUsgs
* WebMercator

Typically applications will use methods on Geometry classes to convert to a different coordinate system rather than use these classes directly.

The EpsgCoordinateSystems class returns CoordinateSystem instances based on the EPSG database. The EsriCoordinateSystems class
returns CoordinateSystems based on ESRI's definitions as used in ESRI Shapefiles.

# Geometry Factory

The com.revolsys.geometry.model.GeometryFactory is used to create com.revolsys.geometry.model.Geometry
instances and defines the org.jeometry.coordinatesystem.model.CoordinateSystem and precision model
for those geometries.

Geometry factories can be created using either the EPSG code or from org.jeometry.coordinatesystem.model.CoordinateSystem instances obtained from org.jeometry.coordinatesystem.model.systems.EpsgCoordinateSystems or org.jeometry.coordinatesystem.model.systems.EsriCoordinateSystems.

Construct a new 2D geometry factory using EPSG SRID 4326 (WGS 84).

```java
GeometryFactory geometryFactory = GeometryFactory.floating2d(4326);
```


Construct a new 3D geometry factory using ESRI Coordinate system 3005 (BC Albers) with 1mm precision model for X, Y and 1m precision model for Z.

```java
ErsiCoordinateSystems coordinateSystem = ErsiCoordinateSystems.getCoordinateSystem(3005);
GeometryFactory geometryFactory = GeometryFactory.fixed3d(coordinateSystem, 1000,1000, 1);
```

# Geometry Interfaces

The com.revolsys.geometry.model package contains the interfaces for the Geometry instances.

* Geometry
	* Punctual
	    * Point
	    * MultiPoint
	* Lineal
	    * LineString
	    * LinearRing
	    * MultiLineString
	* Polygonal
	    * Polygon
	    * MultiPolygon
	* GeometryCollection
	    * MultiPoint
	    * MultiLineString
	    * MultiPolygon

Where possible these interfaces include default implementations for each of the methods. These can be overridden in any implementations where direct access to fields would improve performance.

The following methods can convert from one coordinate system to another using the project and inverse operations on the CoordinatesProjection as required.

```java
GeometryFactory wgs84 = GeometryFactory.floating(4326, 3);

line.convertGeometry(wgs84); // Change geometry factory and convert to the number of axis in the new coordinate system
line.convertGeometry(wgs84, 2); // Change geometry factory and convert to 2d
```

Most spatial operators will automatically convert geometries to the other geometries coordinate systems and precision models before applying the spatial operator. When converting coordinate systems or precision model switching the order of the 2 geometries can have an effect on the result of the operator.

# Geometry Implementations

The com.revolsys.geometry.model.impl package contains the implementations of the geometry interfaces. In most cases these should be constructed using the methods on the GeometryFactory class rather than directly.

Most geometry implementations use double precision floating point values. For Point implementations the PointDoubleXY and PointDoubleXYZ classes have fields for the x,y and z values. The PointDouble and LineString double classes use a double[] array with axisCount (e.g. 3) double values for each vertex in a progression x0,y0...xn,yn. Other classes such as PolygonImpl, MultiPointImpl, MultiLineStringImpl, MultiPolygonImpl, and GeometryCollectionImpl use arrays of the component parts. These design decisions were made to reduce memory overhead while retaining good performance.
 

