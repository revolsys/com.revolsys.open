package com.revolsys.gis.model.geometry.operation.overlay;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.gis.model.geometry.Geometry;
import com.revolsys.gis.model.geometry.GeometryCollection;
import com.revolsys.gis.model.geometry.GeometryFactory;
import com.revolsys.gis.model.geometry.LineString;
import com.revolsys.gis.model.geometry.LinearRing;
import com.revolsys.gis.model.geometry.MultiLineString;
import com.revolsys.gis.model.geometry.MultiPoint;
import com.revolsys.gis.model.geometry.MultiPolygon;
import com.revolsys.gis.model.geometry.Point;
import com.revolsys.gis.model.geometry.Polygon;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.util.GeometryEditor;

/**
 * A framework for processes which transform an input {@link Geometry} into an
 * output {@link Geometry}, possibly changing its structure and type(s). This
 * class is a framework for implementing subclasses which perform
 * transformations on various different Geometry subclasses. It provides an easy
 * way of applying specific transformations to given geometry types, while
 * allowing unhandled types to be simply copied. Also, the framework ensures
 * that if subcomponents change type the parent geometries types change
 * appropriately to maintain valid structure. Subclasses will override whichever
 * <code>transformX</code> methods they need to to handle particular Geometry
 * types.
 * <p>
 * A typically usage would be a transformation class that transforms
 * <tt>Polygons</tt> into <tt>Polygons</tt>, <tt>LineStrings</tt> or
 * <tt>Points</tt>, depending on the geometry of the input (For instance, a
 * simplification operation). This class would likely need to override the
 * {@link #transformMultiPolygon(MultiPolygon, Geometry)transformMultiPolygon}
 * method to ensure that if input Polygons change type the result is a
 * <tt>GeometryCollection</tt>, not a <tt>MultiPolygon</tt>.
 * <p>
 * The default behaviour of this class is simply to recursively transform each
 * Geometry component into an identical object by deep copying down to the level
 * of, but not including, coordinates.
 * <p>
 * All <code>transformX</code> methods may return <code>null</code>, to avoid
 * creating empty or invalid geometry objects. This will be handled correctly by
 * the transformer. <code>transform<i>XXX</i></code> methods should always
 * return valid geometry - if they cannot do this they should return
 * <code>null</code> (for instance, it may not be possible for a
 * transformLineString implementation to return at least two points - in this
 * case, it should return <code>null</code>). The {@link #transform(Geometry)
 * transform} method itself will always return a non-null Geometry object (but
 * this may be empty).
 * 
 * @version 1.7
 * @see GeometryEditor
 */
public class GeometryTransformer {

  /**
   * Possible extensions: getParent() method to return immediate parent e.g. of
   * LinearRings in Polygons
   */

  private Geometry inputGeom;

  protected GeometryFactory factory = null;

  // these could eventually be exposed to clients
  /**
   * <code>true</code> if empty geometries should not be included in the result
   */
  private final boolean pruneEmptyGeometry = true;

  /**
   * <code>true</code> if a homogenous collection result from a
   * {@link GeometryCollection} should still be a general GeometryCollection
   */
  private final boolean preserveGeometryCollectionType = true;

  /**
   * <code>true</code> if the output from a collection argument should still be
   * a collection
   */
  private final boolean preserveCollections = false;

  /**
   * <code>true</code> if the type of the input should be preserved
   */
  private final boolean preserveType = false;

  public GeometryTransformer() {
  }

  /**
   * Utility function to make input geometry available
   * 
   * @return the input geometry
   */
  public Geometry getInputGeometry() {
    return inputGeom;
  }

  public final Geometry transform(final Geometry inputGeom) {
    this.inputGeom = inputGeom;
    this.factory = inputGeom.getGeometryFactory();

    if (inputGeom instanceof Point) {
      return transformPoint((Point)inputGeom, null);
    }
    if (inputGeom instanceof MultiPoint) {
      return transformMultiPoint((MultiPoint)inputGeom, null);
    }
    if (inputGeom instanceof LinearRing) {
      return transformLinearRing((LinearRing)inputGeom, null);
    }
    if (inputGeom instanceof LineString) {
      return transformLineString((LineString)inputGeom, null);
    }
    if (inputGeom instanceof MultiLineString) {
      return transformMultiLineString((MultiLineString)inputGeom, null);
    }
    if (inputGeom instanceof Polygon) {
      return transformPolygon((Polygon)inputGeom, null);
    }
    if (inputGeom instanceof MultiPolygon) {
      return transformMultiPolygon((MultiPolygon)inputGeom, null);
    }
    if (inputGeom instanceof GeometryCollection) {
      return transformGeometryCollection((GeometryCollection)inputGeom, null);
    }

    throw new IllegalArgumentException("Unknown Geometry subtype: "
      + inputGeom.getClass().getName());
  }

  protected Coordinates transformCoordinates(final Coordinates coords,
    final Geometry parent) {
    return new DoubleCoordinates(coords);
  }

  /**
   * Transforms a {@link CoordinateSequence}. This method should always return a
   * valid coordinate list for the desired result type. (E.g. a coordinate list
   * for a LineString must have 0 or at least 2 points). If this is not
   * possible, return an empty sequence - this will be pruned out.
   * 
   * @param coords the coordinates to transform
   * @param parent the parent geometry
   * @return the transformed coordinates
   */
  protected CoordinatesList transformCoordinates(final CoordinatesList coords,
    final Geometry parent) {
    return new DoubleCoordinatesList(coords);
  }

  protected Geometry transformGeometryCollection(final GeometryCollection geom,
    final Geometry parent) {
    final List<Geometry> transGeomList = new ArrayList<Geometry>();
    final List<Geometry> parts = geom.getGeometries();
    for (final Geometry part : parts) {
      final Geometry transformGeom = transform(part);
      if (transformGeom != null
        && (!pruneEmptyGeometry || !transformGeom.isEmpty())) {
        transGeomList.add(transformGeom);
      }
    }
    if (preserveGeometryCollectionType) {
      return factory.createGeometryCollection(transGeomList);
    }
    return factory.createGeometry(transGeomList);
  }

  /**
   * Transforms a LinearRing. The transformation of a LinearRing may result in a
   * coordinate sequence which does not form a structurally valid ring (i.e. a
   * degnerate ring of 3 or fewer points). In this case a LineString is
   * returned. Subclasses may wish to override this method and check for this
   * situation (e.g. a subclass may choose to eliminate degenerate linear rings)
   * 
   * @param geom the ring to simplify
   * @param parent the parent geometry
   * @return a LinearRing if the transformation resulted in a structurally valid
   *         ring
   * @return a LineString if the transformation caused the LinearRing to
   *         collapse to 3 or fewer points
   */
  protected LineString transformLinearRing(final LinearRing geom,
    final Geometry parent) {
    final CoordinatesList points = transformCoordinates(geom, geom);
    final int seqSize = points.size();
    // ensure a valid LinearRing
    if (seqSize > 0 && seqSize < 4 && !preserveType) {
      return factory.createLineString(points);
    }
    return factory.createLinearRing(points);

  }

  /**
   * Transforms a {@link LineString} geometry.
   * 
   * @param geom
   * @param parent
   * @return
   */
  protected Geometry transformLineString(final LineString geom,
    final Geometry parent) {
    // should check for 1-point sequences and downgrade them to points
    return factory.createLineString(transformCoordinates(geom, geom));
  }

  protected Geometry transformMultiLineString(final MultiLineString geom,
    final Geometry parent) {
    final List transGeomList = new ArrayList();
    for (int i = 0; i < geom.getGeometryCount(); i++) {
      final Geometry transformGeom = transformLineString(
        (LineString)geom.getGeometry(i), geom);
      if (transformGeom == null) {
        continue;
      }
      if (transformGeom.isEmpty()) {
        continue;
      }
      transGeomList.add(transformGeom);
    }
    return factory.createGeometry(transGeomList);
  }

  protected Geometry transformMultiPoint(final MultiPoint geom,
    final Geometry parent) {
    final List transGeomList = new ArrayList();
    for (int i = 0; i < geom.getGeometryCount(); i++) {
      final Geometry transformGeom = transformPoint((Point)geom.getGeometry(i),
        geom);
      if (transformGeom == null) {
        continue;
      }
      if (transformGeom.isEmpty()) {
        continue;
      }
      transGeomList.add(transformGeom);
    }
    return factory.createGeometry(transGeomList);
  }

  protected Geometry transformMultiPolygon(final MultiPolygon geom,
    final Geometry parent) {
    final List<Geometry> transGeomList = new ArrayList<Geometry>();
    final List<Polygon> polygons = geom.getGeometries();
    for (final Polygon polygon : polygons) {
      final Geometry transformGeom = transformPolygon(polygon, geom);
      if (transformGeom != null && !transformGeom.isEmpty()) {
        transGeomList.add(transformGeom);
      }
    }
    return factory.createGeometry(transGeomList);
  }

  protected Geometry transformPoint(final Point geom, final Geometry parent) {
    return factory.createPoint(transformCoordinates(geom, geom));
  }

  protected Geometry transformPolygon(final Polygon geom, final Geometry parent) {
    boolean isAllValidLinearRings = true;
    final List<Geometry> rings = new ArrayList<Geometry>();
    for (final LinearRing ring : geom) {
      final LineString line = transformLinearRing(ring, geom);
      if (line != null && !line.isEmpty()) {
        rings.add(line);
        if (!(line instanceof LinearRing)) {
          isAllValidLinearRings = false;
        }
      }

    }

    if (isAllValidLinearRings) {
      return factory.createPolygon(rings);
    } else {
      return factory.createGeometry(rings);
    }
  }

}
