package com.revolsys.swing.map.view;

import java.awt.geom.AffineTransform;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Length;

import com.revolsys.awt.WebColors;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.Ellipsoid;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.cs.datum.GeodeticDatum;
import com.revolsys.geometry.cs.unit.CustomUnits;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;
import com.revolsys.geometry.model.impl.PointDoubleXYOrientation;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.Segment;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.record.Record;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.layer.record.style.TextStyle;
import com.revolsys.util.Cancellable;
import com.revolsys.util.Property;
import com.revolsys.util.QuantityType;

import tec.uom.se.unit.Units;

public abstract class ViewRenderer implements BoundingBoxProxy, Cancellable {
  private static final GeometryStyle STYLE_DIFFERENT_COORDINATE_SYSTEM = GeometryStyle
    .line(WebColors.Red, 1)
    .setLineDashArray(Arrays.asList(10));

  private static final Pattern PATTERN_INDEX_FROM_END = Pattern
    .compile("n(?:\\s*-\\s*(\\d+)\\s*)?");

  private static final Pattern PATTERN_SEGMENT_INDEX = Pattern.compile("segment\\((.*)\\)");

  private static final Pattern PATTERN_VERTEX_INDEX = Pattern.compile("vertex\\((.*)\\)");

  public static final AffineTransform IDENTITY_TRANSFORM = new AffineTransform();

  private static PointDoubleXYOrientation getPointWithOrientationCentre(
    final GeometryFactory geometryFactory2dFloating, final Geometry geometry) {
    double orientation = 0;
    Point point = null;
    if (geometry instanceof LineString) {
      final LineString line = geometry.convertGeometry(geometryFactory2dFloating, 2);

      final double totalLength = line.getLength();
      final double centreLength = totalLength / 2;
      double currentLength = 0;
      for (final Segment segment : line.segments()) {
        final double segmentLength = segment.getLength();
        currentLength += segmentLength;
        if (currentLength >= centreLength) {
          final double segmentFraction = 1 - (currentLength - centreLength) / segmentLength;
          point = segment.pointAlong(segmentFraction);
          orientation = segment.getOrientaton();
          break;
        }
      }
    } else {
      point = geometry.getPointWithin();
      point = point.convertPoint2d(geometryFactory2dFloating);
    }
    return new PointDoubleXYOrientation(point, orientation);
  }

  protected Viewport2D viewport;

  private Cancellable cancellable = Cancellable.FALSE;

  protected GeometryFactory geometryFactory;

  protected int viewWidthPixels;

  protected int viewHeightPixels;

  protected AffineTransform canvasOriginalTransform = IDENTITY_TRANSFORM;

  protected AffineTransform canvasModelTransform = IDENTITY_TRANSFORM;

  protected AffineTransform modelToScreenTransform;

  private final double[] coordinates = new double[2];

  private double modelUnitsPerViewUnit;

  protected BoundingBox boundingBox = BoundingBox.empty();

  public ViewRenderer(final Viewport2D viewport) {
    setViewport(viewport);
  }

  public void drawDifferentCoordinateSystem(final BoundingBox boundingBox) {
    if (!isSameCoordinateSystem(boundingBox)) {
      Polygon polygon = boundingBox.toPolygon(0);
      polygon = polygon.as2d(this.geometryFactory);
      drawGeometryOutline(polygon, STYLE_DIFFERENT_COORDINATE_SYSTEM);
    }
  }

  public abstract void drawGeometry(Geometry geometry, GeometryStyle geometryStyle);

  public abstract void drawGeometryOutline(Geometry geometry, GeometryStyle geometryStyle);

  public abstract void drawImage(GeoreferencedImage image, boolean useTransform);

  public abstract void drawImage(GeoreferencedImage image, boolean useTransform, double alpha,
    Object interpolationMethod);

  public abstract void drawImage(GeoreferencedImage image, boolean useTransform,
    Object interpolationMethod);

  public void drawMarker(final Geometry geometry, final MarkerStyle style) {
    if (geometry != null) {
      if ("vertices".equals(style.getMarkerPlacementType())) {
        drawMarkerVertices(geometry, style);
      } else if ("segments".equals(style.getMarkerPlacementType())) {
        drawMarkerSegments(geometry, style);
      } else {
        for (int i = 0; i < geometry.getGeometryCount(); i++) {
          final Geometry part = geometry.getGeometry(i);
          if (part instanceof Point) {
            final Point point = (Point)part;
            drawMarker(point, style, 0);
          } else if (part instanceof LineString) {
            final LineString line = (LineString)part;
            drawMarker(line, style);
          } else if (part instanceof Polygon) {
            final Polygon polygon = (Polygon)part;
            drawMarker(polygon, style);
          }
        }
      }
    }
  }

  private void drawMarker(final LineString line, final MarkerStyle style) {
    final PointDoubleXYOrientation point = getMarkerLocation(line, style);
    if (point != null) {
      final double orientation = point.getOrientation();
      drawMarker(point, style, orientation);
    }
  }

  public abstract void drawMarker(Point point, final MarkerStyle style, final double orientation);

  private void drawMarker(final Polygon polygon, final MarkerStyle style) {
    final Point point = polygon.getPointWithin();
    drawMarker(point, style, 0);
  }

  public void drawMarkers(LineString line, final MarkerStyle styleFirst,
    final MarkerStyle styleLast, final MarkerStyle styleVertex) {
    if (line != null) {
      line = line.convertGeometry(this.geometryFactory);
      for (final Vertex vertex : line.vertices()) {
        MarkerStyle style;
        if (vertex.isFrom()) {
          style = styleFirst;
        } else if (vertex.isTo()) {
          style = styleLast;
        } else {
          style = styleVertex;
        }
        final double orientation = vertex.getOrientaton();
        drawMarker(vertex, style, orientation);
      }
    }
  }

  public void drawMarkerSegments(Geometry geometry, final MarkerStyle style) {
    geometry = getGeometry(geometry);
    if (Property.hasValue(geometry)) {
      final String orientationType = style.getMarkerOrientationType();
      if ("none".equals(orientationType)) {
        for (final Segment segment : geometry.segments()) {
          final Point point = segment.midPoint();
          drawMarker(point, style, 0);
        }
      } else {
        for (final Segment segment : geometry.segments()) {
          final Point point = segment.midPoint();
          final double orientation = segment.getOrientaton();
          drawMarker(point, style, orientation);
        }
      }
    }
  }

  public void drawMarkerVertices(Geometry geometry, final MarkerStyle style) {
    geometry = getGeometry(geometry);
    if (Property.hasValue(geometry)) {
      final String orientationType = style.getMarkerOrientationType();
      if ("none".equals(orientationType)) {
        for (final Vertex vertex : geometry.vertices()) {
          drawMarker(vertex, style, 0);
        }
      } else {
        for (final Vertex vertex : geometry.vertices()) {
          final double orientation = vertex.getOrientaton();
          drawMarker(vertex, style, orientation);
        }
      }
    }
  };

  public abstract void drawText(Record record, Geometry geometry, TextStyle style);

  @Override
  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  public <G extends Geometry> G getGeometry(final G geometry) {
    final BoundingBox viewExtent = this.boundingBox;
    if (geometry != null) {
      if (!viewExtent.isEmpty()) {
        final BoundingBox geometryExtent = geometry.getBoundingBox();
        if (geometryExtent.bboxIntersects(viewExtent)) {
          return geometry.convertGeometry(this.geometryFactory);
        }
      }
    }
    return null;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  protected int getIndex(final Matcher matcher) {
    int index;
    final String argument = matcher.group(1);
    if (PATTERN_INDEX_FROM_END.matcher(argument).matches()) {
      final String indexString = argument.replaceAll("[^0-9\\-]+", "");
      if (indexString.isEmpty()) {
        index = -1;
      } else {
        index = Integer.parseInt(indexString) - 1;
      }
    } else {
      index = Integer.parseInt(argument);
    }
    return index;
  }

  public PointDoubleXYOrientation getMarkerLocation(final Geometry geometry,
    final MarkerStyle style) {
    final String placementType = style.getMarkerPlacementType();
    return getPointWithOrientation(geometry, placementType);
  }

  public double getMetresPerPixel() {
    return this.viewport.getMetresPerPixel();
  }

  public PointDoubleXYOrientation getPointWithOrientation(final Geometry geometry,
    final String placementType) {
    if (this.viewport == null) {
      return new PointDoubleXYOrientation(0.0, 0.0, 0);
    } else {
      final GeometryFactory viewportGeometryFactory2d = this.viewport
        .getGeometryFactory2dFloating();
      if (viewportGeometryFactory2d != null && geometry != null && !geometry.isEmpty()) {
        Point point = null;
        double orientation = 0;
        if (geometry instanceof Point) {
          point = (Point)geometry;
        } else {
          final Matcher vertexIndexMatcher = PATTERN_VERTEX_INDEX.matcher(placementType);
          if (vertexIndexMatcher.matches()) {
            final int vertexCount = geometry.getVertexCount();
            final int vertexIndex = getIndex(vertexIndexMatcher);
            if (vertexIndex >= -vertexCount && vertexIndex < vertexCount) {
              final Vertex vertex = geometry.getVertex(vertexIndex);
              orientation = vertex.getOrientaton(viewportGeometryFactory2d);
              point = vertex.convertGeometry(viewportGeometryFactory2d);
            }
          } else {
            final Matcher segmentIndexMatcher = PATTERN_SEGMENT_INDEX.matcher(placementType);
            if (segmentIndexMatcher.matches()) {
              final int segmentCount = geometry.getSegmentCount();
              if (segmentCount > 0) {
                final int index = getIndex(segmentIndexMatcher);
                LineSegment segment = geometry.getSegment(index);
                segment = segment.convertGeometry(viewportGeometryFactory2d);
                if (segment != null) {
                  point = segment.midPoint();
                  orientation = segment.getOrientaton();
                }
              }
            } else {
              PointDoubleXYOrientation pointDoubleXYOrientation = getPointWithOrientationCentre(
                viewportGeometryFactory2d, geometry);
              if (!this.boundingBox.bboxCovers(pointDoubleXYOrientation)) {
                try {
                  final Geometry clippedGeometry = geometry.intersectionBbox(this.boundingBox);
                  if (!clippedGeometry.isEmpty()) {
                    double maxArea = 0;
                    double maxLength = 0;
                    for (int i = 0; i < clippedGeometry.getGeometryCount(); i++) {
                      final Geometry part = clippedGeometry.getGeometry(i);
                      if (part instanceof Polygon) {
                        final double area = part.getArea();
                        if (area > maxArea) {
                          maxArea = area;
                          pointDoubleXYOrientation = getPointWithOrientationCentre(
                            viewportGeometryFactory2d, part);
                        }
                      } else if (part instanceof LineString) {
                        if (maxArea == 0 && "auto".equals(placementType)) {
                          final double length = part.getLength();
                          if (length > maxLength) {
                            maxLength = length;
                            pointDoubleXYOrientation = getPointWithOrientationCentre(
                              viewportGeometryFactory2d, part);
                          }
                        }
                      } else if (part instanceof Point) {
                        if (maxArea == 0 && maxLength == 0 && "auto".equals(placementType)) {
                          pointDoubleXYOrientation = getPointWithOrientationCentre(
                            viewportGeometryFactory2d, part);
                        }
                      }
                    }
                  }
                } catch (final Throwable t) {
                }
              }
              return pointDoubleXYOrientation;
            }
          }
        }
        if (Property.hasValue(point)) {
          if (this.boundingBox.bboxCovers(point)) {
            point = point.convertPoint2d(viewportGeometryFactory2d);
            return new PointDoubleXYOrientation(point, orientation);
          }
        }
      }
      return null;
    }
  }

  public double getScale() {
    if (this.viewport == null) {
      return 1;
    } else {
      return this.viewport.getScale();
    }
  }

  public double getScaleForVisible() {
    return this.viewport.getScaleForVisible();
  }

  public int getViewHeightPixels() {
    return this.viewHeightPixels;
  }

  public Viewport2D getViewport() {
    return this.viewport;
  }

  public int getViewWidthPixels() {
    return this.viewWidthPixels;
  }

  @Override
  public boolean isCancelled() {
    return this.cancellable.isCancelled();
  }

  public abstract TextStyleViewRenderer newTextStyleViewRenderer(TextStyle textStyle);

  public void setCancellable(Cancellable cancellable) {
    if (cancellable == null) {
      cancellable = Cancellable.FALSE;
    } else {
      this.cancellable = cancellable;
    }
  }

  public void setViewport(final Viewport2D viewport) {
    this.viewport = viewport;
    updateFields();
  }

  public double toDisplayValue(final Quantity<Length> value) {
    double convertedValue;
    final Unit<Length> unit = value.getUnit();
    if (unit.equals(CustomUnits.PIXEL)) {
      convertedValue = QuantityType.doubleValue(value, CustomUnits.PIXEL);
    } else {
      convertedValue = QuantityType.doubleValue(value, Units.METRE);
      final CoordinateSystem coordinateSystem = this.geometryFactory
        .getHorizontalCoordinateSystem();
      if (coordinateSystem instanceof GeographicCoordinateSystem) {
        final GeographicCoordinateSystem geoCs = (GeographicCoordinateSystem)coordinateSystem;
        final double radius = geoCs.getDatum().getEllipsoid().getSemiMajorAxis();
        convertedValue = Math.toDegrees(convertedValue / radius);

      }
      convertedValue = convertedValue / this.modelUnitsPerViewUnit;
    }
    return convertedValue;
  }

  public double toModelValue(final Quantity<Length> value) {
    double convertedValue;
    final Unit<Length> unit = value.getUnit();
    if (unit.equals(CustomUnits.PIXEL)) {
      convertedValue = QuantityType.doubleValue(value, CustomUnits.PIXEL);
      convertedValue *= this.modelUnitsPerViewUnit;
    } else {
      convertedValue = QuantityType.doubleValue(value, Units.METRE);
      final CoordinateSystem coordinateSystem = this.geometryFactory
        .getHorizontalCoordinateSystem();
      if (coordinateSystem instanceof GeographicCoordinateSystem) {
        final GeographicCoordinateSystem geoCs = (GeographicCoordinateSystem)coordinateSystem;
        final GeodeticDatum datum = geoCs.getDatum();
        final Ellipsoid ellipsoid = datum.getEllipsoid();
        final double radius = ellipsoid.getSemiMajorAxis();
        convertedValue = Math.toDegrees(convertedValue / radius);
      }
    }
    return convertedValue;
  }

  public double[] toViewCoordinates(final double modelX, final double modelY) {
    this.coordinates[0] = modelX;
    this.coordinates[1] = modelY;
    toViewCoordinates(this.coordinates);
    return this.coordinates;
  }

  public void toViewCoordinates(final double[] coordinates) {
    if (!this.modelToScreenTransform.isIdentity()) {
      this.modelToScreenTransform.transform(coordinates, 0, coordinates, 0, 1);
    }
  }

  protected void updateFields() {
    if (this.viewport == null) {
      this.boundingBox = new BoundingBoxDoubleXY(0, 0, this.viewWidthPixels, this.viewHeightPixels);
      this.modelToScreenTransform = IDENTITY_TRANSFORM;
      this.modelUnitsPerViewUnit = 1;
    } else {
      this.geometryFactory = this.viewport.getGeometryFactory2dFloating();
      this.boundingBox = this.viewport.getBoundingBox();
      this.viewWidthPixels = this.viewport.getViewWidthPixels();
      this.viewHeightPixels = this.viewport.getViewHeightPixels();
      this.modelToScreenTransform = this.viewport.getModelToScreenTransform();
      this.modelUnitsPerViewUnit = this.viewport.getModelUnitsPerViewUnit();
    }
    if (this.modelToScreenTransform == null) {
      this.canvasModelTransform = IDENTITY_TRANSFORM;
    } else {
      final AffineTransform transform = (AffineTransform)this.canvasOriginalTransform.clone();
      transform.concatenate(this.modelToScreenTransform);
      this.canvasModelTransform = transform;
    }
  }
}
