package com.revolsys.swing.map.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.jeometry.common.awt.WebColors;
import org.jeometry.common.logging.Logs;
import org.jeometry.coordinatesystem.model.Ellipsoid;
import org.jeometry.coordinatesystem.model.unit.CustomUnits;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.TopologyException;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;
import com.revolsys.geometry.model.impl.PointDoubleXYOrientation;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.Segment;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.io.BaseCloseable;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.record.Record;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.layer.record.style.TextStyle;
import com.revolsys.swing.map.layer.record.style.marker.GeometryMarker;
import com.revolsys.swing.map.layer.record.style.marker.ImageMarker;
import com.revolsys.swing.map.layer.record.style.marker.Marker;
import com.revolsys.swing.map.layer.record.style.marker.MarkerRenderer;
import com.revolsys.swing.map.layer.record.style.marker.SvgMarker;
import com.revolsys.swing.map.layer.record.style.marker.TextMarker;
import com.revolsys.util.Cancellable;
import com.revolsys.util.Debug;
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

  private boolean backgroundDrawingEnabled = true;

  private boolean showHiddenRecords;

  protected Viewport2D viewport;

  private Cancellable cancellable = Cancellable.FALSE;

  protected GeometryFactory geometryFactory;

  protected double viewWidthPixels;

  protected double viewHeightPixels;

  private double modelUnitsPerViewUnit;

  protected BoundingBox boundingBox = BoundingBox.empty();

  private final List<Point> points = new ArrayList<>();

  private final List<LineString> lines = new ArrayList<>();

  private final List<Polygon> polygons = new ArrayList<>();

  private final BaseCloseable geometryListCloseable = () -> {
    this.points.clear();
    this.lines.clear();
    this.polygons.clear();
  };

  private boolean linesAdd;

  private boolean polygonsAdd;

  private boolean pointsAdd;

  private GeometryStyle geometryStyle;

  private final BaseCloseable drawGeometriesCloseable = () -> {
    try {
      if (this.linesAdd) {
        drawLines(this.geometryStyle, this.lines);
      }
      if (this.polygonsAdd) {
        fillPolygons(this.geometryStyle, this.polygons);
      }
      if (this.pointsAdd) {
        renderMarkers(this.geometryStyle, this.points);
      }
    } finally {
      this.geometryStyle = null;
      this.geometryListCloseable.close();
    }
  };

  public ViewRenderer(final Viewport2D viewport) {
    setViewport(viewport);
  }

  public void addGeometry(final Geometry geometry) {
    if (geometry != null) {
      try {
        if (geometry.isGeometryCollection()) {
          geometry.forEachGeometry(this::addGeometry);
        } else {
          if (geometry instanceof Point) {
            if (this.pointsAdd) {
              this.points.add((Point)geometry.as2d(this.geometryFactory));
            }
          } else if (geometry instanceof LineString) {
            if (this.linesAdd) {
              this.lines.add((LineString)geometry.as2d(this.geometryFactory));
            }
          } else if (geometry instanceof Polygon) {
            final Polygon polygon = (Polygon)geometry.as2d(this.geometryFactory);
            if (this.linesAdd) {
              this.lines.addAll(polygon.getRings());
            }
            if (this.polygonsAdd) {
              this.polygons.add(polygon);
            }
          } else {
            Debug.noOp();
          }
        }
      } catch (final TopologyException e) {
      }
    }
  }

  public abstract BaseCloseable applyMarkerStyle(MarkerStyle style);

  public void drawBboxOutline(final GeometryStyle style, final BoundingBoxProxy boundingBox) {
    final LinearRing ring = boundingBox.getBoundingBox().toLinearRing(this.geometryFactory, 50, 50);
    drawLine(style, ring);
  }

  public void drawDifferentCoordinateSystem(final BoundingBox boundingBox) {
    if (!isSameCoordinateSystem(boundingBox)) {
      drawBboxOutline(STYLE_DIFFERENT_COORDINATE_SYSTEM, boundingBox);
    }
  }

  public BaseCloseable drawGeometriesCloseable(final GeometryStyle geometryStyle,
    final boolean pointsAdd, final boolean linesAdd, final boolean polygonsAdd) {
    this.geometryStyle = geometryStyle;
    this.pointsAdd = pointsAdd;
    this.linesAdd = linesAdd;
    this.polygonsAdd = polygonsAdd;
    return this.drawGeometriesCloseable;
  }

  public abstract void drawGeometry(Geometry geometry, GeometryStyle geometryStyle);

  public abstract void drawGeometryOutline(GeometryStyle geometryStyle, Geometry geometry);

  public abstract void drawImage(GeoreferencedImage image, boolean useTransform);

  public abstract void drawImage(GeoreferencedImage image, boolean useTransform, double alpha,
    Object interpolationMethod);

  public abstract void drawImage(GeoreferencedImage image, boolean useTransform,
    Object interpolationMethod);

  public void drawLine(final GeometryStyle style, final LineString line) {
    drawLines(style, Collections.singletonList(line));
  }

  public abstract void drawLines(GeometryStyle style, Collection<LineString> lines);

  public void drawMarker(final MarkerStyle style, Point point, final double orientation) {
    point = getGeometry(point);
    if (Property.hasValue(point)) {
      try (
        MarkerRenderer renderer = style.newMarkerRenderer(this)) {
        renderer.renderMarkerPoint(point, orientation);
      }
    }
  }

  public void drawMarkers(LineString line, final MarkerStyle styleFirst,
    final MarkerStyle styleLast, final MarkerStyle styleVertex, final MarkerStyle centreStyle) {
    if (line != null) {
      line = line.convertGeometry(this.geometryFactory);
      for (final Vertex vertex : line.vertices()) {
        MarkerStyle style;
        final boolean to = vertex.isTo();
        if (vertex.isFrom()) {
          style = styleFirst;
        } else if (to) {
          style = styleLast;
        } else {
          style = styleVertex;
        }
        final double orientation = vertex.getOrientaton();
        drawMarker(style, vertex, orientation);
        if (!to) {
          renderMarker(centreStyle, vertex);
        }
      }
    }
  }

  public abstract void drawText(Record record, Geometry geometry, TextStyle style);

  public abstract void fillPolygons(GeometryStyle style, final Collection<Polygon> polygon);

  public BaseCloseable geometryListCloseable(final boolean pointsAdd, final boolean linesAdd,
    final boolean polygonsAdd) {
    this.pointsAdd = pointsAdd;
    this.linesAdd = linesAdd;
    this.polygonsAdd = polygonsAdd;
    return this.geometryListCloseable;
  }

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

  public double getViewHeightPixels() {
    return this.viewHeightPixels;
  }

  public Viewport2D getViewport() {
    return this.viewport;
  }

  public double getViewWidthPixels() {
    return this.viewWidthPixels;
  }

  public boolean isBackgroundDrawingEnabled() {
    return this.backgroundDrawingEnabled;
  }

  @Override
  public boolean isCancelled() {
    return this.cancellable.isCancelled();
  }

  public boolean isHidden(final AbstractRecordLayer layer, final LayerRecord record) {
    return layer.isHidden(record);
  }

  public boolean isShowHiddenRecords() {
    return this.showHiddenRecords;
  }

  public abstract MarkerRenderer newMarkerRendererEllipse(MarkerStyle style);

  public abstract MarkerRenderer newMarkerRendererGeometry(GeometryMarker geometryMarker,
    MarkerStyle style);

  public abstract MarkerRenderer newMarkerRendererImage(ImageMarker imageMarker, MarkerStyle style);

  public abstract MarkerRenderer newMarkerRendererRectangle(MarkerStyle style);

  public abstract MarkerRenderer newMarkerRendererSvg(SvgMarker svgMarker, MarkerStyle style);

  public MarkerRenderer newMarkerRendererText(final TextMarker textMarker,
    final MarkerStyle style) {
    // TODO Auto-generated method stub
    return null;
  }

  public abstract TextStyleViewRenderer newTextStyleViewRenderer(TextStyle textStyle);

  public final void renderLayer(final Layer layer) {
    final double scaleForVisible = getScaleForVisible();
    if (layer != null && layer.isExists() && layer.isVisible(scaleForVisible)) {
      final LayerRenderer<? extends Layer> renderer = layer.getRenderer();
      if (renderer != null && renderer.isVisible(this)) {
        try {
          renderLayerDo(layer, renderer);
        } catch (final Throwable e) {
          if (!isCancelled()) {
            Logs.error(this, "Error rendering layer: " + renderer.getLayer(), e);
          }
        }
      }
    }
  }

  protected void renderLayerDo(final Layer layer, final LayerRenderer<?> renderer) {
    renderer.render(this);
  }

  public void renderMarker(final MarkerStyle markerStyle, final Point point) {
    renderMarkers(markerStyle, Collections.singleton(point));
  }

  public void renderMarkers(final MarkerStyle markerStyle,
    final Collection<? extends Point> points) {
    final Marker marker = markerStyle.getMarker();
    marker.renderPoints(this, markerStyle, points);
  }

  public void setBackgroundDrawingEnabled(final boolean backgroundDrawingEnabled) {
    this.backgroundDrawingEnabled = backgroundDrawingEnabled;
  }

  public void setCancellable(Cancellable cancellable) {
    if (cancellable == null) {
      cancellable = Cancellable.FALSE;
    } else {
      this.cancellable = cancellable;
    }
  }

  public void setShowHiddenRecords(final boolean showHiddenRecords) {
    this.showHiddenRecords = showHiddenRecords;
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
      if (this.geometryFactory.isGeographic()) {
        final Ellipsoid ellipsoid = this.geometryFactory.getEllipsoid();
        final double radius = ellipsoid.getSemiMajorAxis();
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
      if (this.geometryFactory.isGeographic()) {
        final Ellipsoid ellipsoid = this.geometryFactory.getEllipsoid();
        final double radius = ellipsoid.getSemiMajorAxis();
        convertedValue = Math.toDegrees(convertedValue / radius);
      }
    }
    return convertedValue;
  }

  protected void updateFields() {
    if (this.viewport == null) {
      this.boundingBox = new BoundingBoxDoubleXY(0, 0, this.viewWidthPixels, this.viewHeightPixels);
      this.modelUnitsPerViewUnit = 1;
    } else {
      this.geometryFactory = this.viewport.getGeometryFactory2dFloating();
      this.boundingBox = this.viewport.getBoundingBox();
      this.viewWidthPixels = this.viewport.getViewWidthPixels();
      this.viewHeightPixels = this.viewport.getViewHeightPixels();
      this.modelUnitsPerViewUnit = this.viewport.getModelUnitsPerViewUnit();
    }
  }

  public abstract BaseCloseable useViewCoordinates();
}
