package com.revolsys.gis.esri.gdb.file.arcobjects.type;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.esri.arcgis.geodatabase.IField;
import com.esri.arcgis.geodatabase.IGeometryDef;
import com.esri.arcgis.geodatabase.IRow;
import com.esri.arcgis.geodatabase.IRowBuffer;
import com.esri.arcgis.geometry.GeographicCoordinateSystem;
import com.esri.arcgis.geometry.GeometryBag;
import com.esri.arcgis.geometry.GeometryEnvironment;
import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.geometry.IPoint;
import com.esri.arcgis.geometry.IPointCollection;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.geometry.Multipoint;
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.geometry.Polygon;
import com.esri.arcgis.geometry.Polyline;
import com.esri.arcgis.geometry.ProjectedCoordinateSystem;
import com.esri.arcgis.geometry.Ring;
import com.esri.arcgis.geometry.esriGeometryType;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.system.Cleaner;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.cs.projection.ProjectionFactory;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.SimpleCoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;

public class GeometryAttribute extends AbstractFileGdbAttribute {
  private static final GeometryEnvironment GEOMETRY_ENVIRONMENT;
  static {
    try {
      GEOMETRY_ENVIRONMENT = new GeometryEnvironment();
    } catch (final Exception e) {
      throw new RuntimeException("Unable to get geometry environment", e);
    }
  }

  public static GeometryFactory getGeometryFactory(
    final ISpatialReference spatialReference) {
    try {
      double xYScale;
      double zScale;
      int code;
      if (spatialReference instanceof GeographicCoordinateSystem) {
        final GeographicCoordinateSystem geoCs = (GeographicCoordinateSystem)spatialReference;
        code = geoCs.getFactoryCode();
        xYScale = geoCs.getXYResolution(false);
        zScale = geoCs.getZResolution(false);
      } else if (spatialReference instanceof ProjectedCoordinateSystem) {
        final ProjectedCoordinateSystem projCs = (ProjectedCoordinateSystem)spatialReference;
        code = projCs.getFactoryCode();
        xYScale = projCs.getXYResolution(false);
        zScale = projCs.getZResolution(false);
      } else {
        return null;
      }
      final CoordinateSystem coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(code);
      if (coordinateSystem == null) {
        return null;
      } else {
        final CoordinatesPrecisionModel precisionModel;
        if (xYScale == 1.1258999068426238E13) {
          precisionModel = new SimpleCoordinatesPrecisionModel(0, zScale);
        } else {
          precisionModel = new SimpleCoordinatesPrecisionModel(xYScale, zScale);
        }
        final GeometryFactory geometryFactory = new GeometryFactory(
          coordinateSystem, precisionModel);
        return geometryFactory;
      }
    } catch (final Exception e) {
      throw new RuntimeException("Unable to get geometry factory for "
        + spatialReference, e);
    }
  }

  private GeometryFactory geometryFactory = new GeometryFactory();

  private int geometryType;

  private boolean hasZ;

  private boolean hasM;

  private int numAxis;

  public GeometryAttribute(final IField field) throws AutomationException,
    IOException {
    super(field.getName(), DataTypes.GEOMETRY,
      field.isRequired() == Boolean.TRUE || !field.isNullable(),
      field.isEditable());
    final IGeometryDef geometryDef = field.getGeometryDef();
    if (geometryDef == null) {
      throw new IllegalArgumentException(
        "IField definition does not include a geometry definition");

    } else {
      final ISpatialReference spatialReference = geometryDef.getSpatialReference();
      if (spatialReference == null) {
        throw new IllegalArgumentException(
          "IField definition does not include a spatial reference");
      } else {
        this.geometryFactory = getGeometryFactory(spatialReference);
        if (geometryFactory == null) {
          throw new IllegalArgumentException(
            "IField definition does not include a valid coordinate system "
              + spatialReference);
        }

        setProperty(AttributeProperties.GEOMETRY_FACTORY, geometryFactory);
      }
      geometryType = geometryDef.getGeometryType();
      hasZ = geometryDef.isHasZ();
      hasM = geometryDef.isHasM();
      if (hasM) {
        numAxis = 4;
      } else if (hasZ) {
        numAxis = 3;
      } else {
        numAxis = 2;
      }

    }
  }

  @Override
  public Object getValue(final IRow row) {
    try {
      final IGeometry iGeometry = (IGeometry)super.getValue(row);
      if (iGeometry == null
        || iGeometry.getGeometryType() == esriGeometryType.esriGeometryPolygon) {
        return null;
      } else {
        Geometry geometry;
        switch (geometryType) {
          case esriGeometryType.esriGeometryPoint:
            geometry = toPoint((Point)iGeometry);
          break;
          case esriGeometryType.esriGeometryMultipoint:
            geometry = toMultiPoint((Multipoint)iGeometry);
          break;
          case esriGeometryType.esriGeometryPolyline:
            geometry = toPolyline((Polyline)iGeometry);
          break;
          case esriGeometryType.esriGeometryPolygon:
            geometry = toPolygon((Polygon)iGeometry);
          break;

          default:
            throw new IllegalArgumentException("Unsupported geometry type "
              + geometryType);
        }
        return geometry;
      }
    } catch (final Exception e) {
      throw new RuntimeException("Unable to read geometry", e);
    }
  }

  public void setCoordinates(final DoubleCoordinatesList coordinates,
    final int index, final IPoint point) throws IOException,
    AutomationException {
    coordinates.setX(index, point.getX());
    coordinates.setY(index, point.getY());
    coordinates.setZ(index, point.getZ());
    coordinates.setM(index, point.getM());
  }

  @Override
  public void setValue(final IRowBuffer row, final Object value) {
    if (value == null) {
      super.setValue(row, value);
    } else if (value instanceof Geometry) {
      final Geometry geometry = (Geometry)value;
      final Geometry projectedGeometry = ProjectionFactory.convert(geometry,
        geometryFactory);

      try {
        IGeometry iGeometry;
        if (value instanceof com.vividsolutions.jts.geom.Point) {
          iGeometry = toIPoint((com.vividsolutions.jts.geom.Point)projectedGeometry);
        } else if (value instanceof MultiPoint) {
          iGeometry = toIMultiPoint((MultiPoint)projectedGeometry);
        } else if (value instanceof LineString) {
          iGeometry = toIPolyline((LineString)projectedGeometry);
        } else if (value instanceof MultiLineString) {
          iGeometry = toIPolyline((MultiLineString)projectedGeometry);
        } else if (value instanceof Polygon) {
          iGeometry = toIPolygon((com.vividsolutions.jts.geom.Polygon)projectedGeometry);
        } else if (value instanceof MultiPolygon) {
          iGeometry = toIMultiPatch((MultiPolygon)projectedGeometry);
        } else {
          throw new IllegalArgumentException("Unsupported geometry type "
            + value.getClass() + "=" + value);
        }
        super.setValue(row, iGeometry);
      } catch (final Exception e) {
        throw new RuntimeException("Unable to convert geometry "
          + projectedGeometry, e);
      }
    } else {
      throw new IllegalArgumentException("Expecting a " + Geometry.class
        + " not a " + value.getClass() + "=" + value);
    }

  }

  public CoordinatesList toCoordinatesList(final IPointCollection pointsList)
    throws IOException, AutomationException {
    final int numPoints = pointsList.getPointCount();
    final DoubleCoordinatesList coordinates = new DoubleCoordinatesList(
      numPoints, numAxis);
    for (int i = 0; i < numPoints; i++) {
      final IPoint point = pointsList.getPoint(i);
      setCoordinates(coordinates, i, point);
    }
    return coordinates;
  }

  private IGeometry toIMultiPatch(final MultiPolygon multiPolygon)
    throws UnknownHostException, IOException {
    // TODO Auto-generated method stub
    return null;
  }

  private IGeometry toIMultiPoint(final MultiPoint multiPoint)
    throws UnknownHostException, IOException {
    // TODO Auto-generated method stub
    return null;
  }

  private IGeometry toIPoint(final com.vividsolutions.jts.geom.Point point)
    throws UnknownHostException, IOException {
    // TODO Auto-generated method stub
    return null;
  }

  private IPoint[] toIPointArray(final CoordinatesList coordinates)
    throws UnknownHostException, IOException {
    final int numPoints = coordinates.size();
    final IPoint[] points = new IPoint[numPoints];
    for (int i = 0; i < numPoints; i++) {
      final Point point = new Point();
      point.setZAware(hasZ);
      point.setMAware(hasM);
      points[i] = point;
      final double x = coordinates.getX(i);
      point.setX(x);
      final double y = coordinates.getY(i);
      point.setY(y);
      if (numAxis > 2) {
        double z = coordinates.getZ(i);
        if (Double.isNaN(z)) {
          z = 0;
        }
        point.setZ(z);
        if (numAxis > 3) {
          double m = coordinates.getM(i);
          if (Double.isNaN(m)) {
            m = 0;
          }
          point.setM(m);
        }
      }
    }
    return points;
  }

  private IGeometry toIPolygon(final com.vividsolutions.jts.geom.Polygon polygon)
    throws UnknownHostException, IOException {
    // TODO Auto-generated method stub
    return null;
  }

  private IGeometry toIPolyline(final LineString line)
    throws UnknownHostException, IOException {
    final Polyline polyline = new Polyline();
    polyline.setZAware(hasZ);
    polyline.setMAware(hasM);
    final CoordinatesList coordinates = CoordinatesListUtil.get(line);
    final IPoint[] points = toIPointArray(coordinates);
    GEOMETRY_ENVIRONMENT.setPoints(polyline, points);
    for (IPoint iPoint : points) {
      Cleaner.release(iPoint);
    }
    return polyline;
  }

  private IGeometry toIPolyline(final MultiLineString projectedGeometry) {
    // TODO Auto-generated method stub
    return null;
  }

  public MultiPoint toMultiPoint(final Multipoint multiPoint)
    throws AutomationException, IOException {
    final CoordinatesList coordinates = toCoordinatesList(multiPoint);
    return geometryFactory.createMultiPoint(coordinates);
  }

  public Geometry toPoint(final Point point) throws IOException,
    AutomationException {
    final DoubleCoordinatesList coordinates = new DoubleCoordinatesList(1,
      numAxis);
    setCoordinates(coordinates, 0, point);
    return geometryFactory.createPoint(coordinates);
  }

  public Geometry toPolygon(final Polygon polygon) throws AutomationException,
    IOException {
    final GeometryBag exteriorRings = (GeometryBag)polygon.getExteriorRingBag();
    final int numPolygons = exteriorRings.getGeometryCount();
    if (numPolygons == 1) {
      return toPolygon(polygon, exteriorRings, 0);
    } else {
      final List<Geometry> polygons = new ArrayList<Geometry>();
      for (int i = 0; i < numPolygons; i++) {
        polygons.add(toPolygon(polygon, exteriorRings, i));
      }
      return geometryFactory.createMultiPolygon(polygons);
    }
  }

  public Geometry toPolygon(final Polygon polygon,
    final GeometryBag exteriorRings, final int index) throws IOException,
    AutomationException {
    final Ring exteriorRing = (Ring)exteriorRings.getGeometry(index);
    final GeometryBag interiorRings = (GeometryBag)polygon.getInteriorRingBag(exteriorRing);
    final List<CoordinatesList> rings = new ArrayList<CoordinatesList>();
    rings.add(toCoordinatesList(exteriorRing));
    for (int i = 0; i < interiorRings.getGeometryCount(); i++) {
      final Ring interiorRing = (Ring)interiorRings.getGeometry(i);
      rings.add(toCoordinatesList(interiorRing));
    }
    return geometryFactory.createPolygon(rings);
  }

  public Geometry toPolyline(final Polyline polyline)
    throws AutomationException, IOException {
    final int numLines = polyline.getGeometryCount();
    if (numLines == 1) {
      final CoordinatesList coordinates = toCoordinatesList(polyline);
      return geometryFactory.createLineString(coordinates);
    } else {
      final List<CoordinatesList> lines = new ArrayList<CoordinatesList>();
      for (int i = 0; i < numLines; i++) {
        final Polyline line = (Polyline)polyline.getGeometry(i);
        final CoordinatesList coordinates = toCoordinatesList(line);
        lines.add(coordinates);
      }
      return geometryFactory.createMultiLineString(lines);
    }
  }

  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }
}
