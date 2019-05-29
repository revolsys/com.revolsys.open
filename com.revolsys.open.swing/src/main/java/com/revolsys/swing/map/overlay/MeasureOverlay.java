package com.revolsys.swing.map.overlay;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.measure.Unit;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.awt.WebColors;
import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.geometry.cs.unit.CustomUnits;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.io.BaseCloseable;
import com.revolsys.swing.Icons;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.record.renderer.GeometryStyleRecordLayerRenderer;
import com.revolsys.swing.map.layer.record.renderer.TextStyleRenderer;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.layer.record.style.TextStyle;
import com.revolsys.util.number.Doubles;

import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.Units;

public class MeasureOverlay extends AbstractOverlay {

  private static final Cursor CURSOR = Icons.getCursor("cursor_ruler", 8, 7);

  private static final Geometry EMPTY_GEOMETRY = GeometryFactory.wgs84().geometry();

  public static final String MEASURE = "Measure";

  private static final NumberFormat MEASURE_FORMAT = new DecimalFormat("#,##0.00");

  private static final SelectedRecordsVertexRenderer MEASURE_RENDERER = new SelectedRecordsVertexRenderer(
    WebColors.Magenta, false);

  private static final GeometryStyle POLYGON_STYLE = GeometryStyle.polygon(WebColors.Black,
    WebColors.newAlpha(WebColors.Magenta, 75));

  private static final long serialVersionUID = 1L;

  private boolean dragged = false;

  private DataType measureDataType;

  private Geometry measureGeometry = EMPTY_GEOMETRY;

  private String measureLabel;

  private List<CloseLocation> mouseOverLocations = Collections.emptyList();

  public MeasureOverlay(final MapPanel map) {
    super(map);
    addOverlayAction( //
      MEASURE, //
      CURSOR, //
      ZoomOverlay.ACTION_PAN, //
      ZoomOverlay.ACTION_ZOOM, //
      ZoomOverlay.ACTION_ZOOM_BOX //
    );
  }

  private void cancel() {
    modeMeasureClear();
  }

  public void clearMouseOverGeometry() {
    if (isOverlayAction(MEASURE)) {
      setMapCursor(CURSOR);
    }
    this.mouseOverLocations = Collections.emptyList();
    clearSnapLocations();
  }

  protected void clearMouseOverLocations() {
    setXorGeometry(null);
    clearMouseOverGeometry();
    getMap().clearToolTipText();
    repaint();
  }

  private Geometry deleteVertex() {
    Geometry geometry = getMeasureGeometry();

    for (final CloseLocation location : getMouseOverLocations()) {
      final int[] vertexId = location.getVertexId();
      if (vertexId != null) {
        if (geometry instanceof Point) {
          return null;
        } else if (geometry instanceof LineString) {
          final LineString line = (LineString)geometry;
          if (line.getVertexCount() == 2) {
            if (vertexId.length == 1) {
              if (vertexId[0] == 0) {
                return line.getPoint(1);
              } else {
                return line.getPoint(0);
              }
            }
          }
        } else if (geometry instanceof Polygon) {
          final Polygon polygon = (Polygon)geometry;
          final LinearRing ring = polygon.getRing(0);
          if (ring.getVertexCount() == 4) {
            if (vertexId.length == 2) {
              final GeometryFactory geometryFactory = geometry.getGeometryFactory();
              final Vertex point0 = ring.getVertex(0);
              final Vertex point1 = ring.getVertex(1);
              final Vertex point2 = ring.getVertex(2);
              switch (vertexId[1]) {
                case 0:
                  return geometryFactory.lineString(point1, point2);
                case 1:
                  return geometryFactory.lineString(point2, point0);
                default:
                  return geometryFactory.lineString(point0, point1);
              }
            }
          }
        }
        try {
          geometry = geometry.deleteVertex(vertexId);
        } catch (final Exception e) {
          Toolkit.getDefaultToolkit().beep();
          return geometry;
        }
      }
    }
    return geometry;
  }

  public DataType getMeasureDataType() {
    return this.measureDataType;
  }

  public Geometry getMeasureGeometry() {
    return this.measureGeometry;
  }

  public List<CloseLocation> getMouseOverLocations() {
    return this.mouseOverLocations;
  }

  protected Geometry getVertexGeometry(final MouseEvent event, final CloseLocation location) {
    final Geometry geometry = location.getGeometry();

    int previousPointOffset;
    int[] vertexId = location.getVertexId();
    if (vertexId == null) {
      previousPointOffset = 0;
      vertexId = location.getSegmentId();
    } else {
      previousPointOffset = -1;
    }
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (geometry instanceof Point) {
    } else {
      final Point point = getPoint(geometryFactory, event);

      final Vertex vertex = geometry.getVertex(vertexId);
      Point previousPoint = null;
      Point nextPoint = null;

      if (previousPointOffset == 0) {
        previousPoint = vertex;
      } else {
        previousPoint = vertex.getLinePrevious();
      }
      nextPoint = vertex.getLineNext();

      final List<LineString> lines = new ArrayList<>();
      if (previousPoint != null && !previousPoint.isEmpty()) {
        lines.add(newXorLine(geometryFactory, previousPoint, point));
      }
      if (nextPoint != null && !nextPoint.isEmpty()) {
        lines.add(newXorLine(geometryFactory, nextPoint, point));
      }
      if (!lines.isEmpty()) {
        return geometryFactory.lineal(lines);
      }
    }
    return null;
  }

  @Override
  public void keyPressed(final KeyEvent event) {
    final int keyCode = event.getKeyCode();
    if (keyCode == KeyEvent.VK_M) {
      if (isOverlayAction(MEASURE)) {
        setMeasureGeometry(EMPTY_GEOMETRY);
      } else {
        setOverlayAction(MEASURE);
      }
    } else if (keyCode == KeyEvent.VK_ESCAPE) {
      if (isOverlayAction(MEASURE) && !this.dragged) {
        cancel();
      }
    } else if (keyCode == KeyEvent.VK_BACK_SPACE || keyCode == KeyEvent.VK_DELETE) {
      if (!getMouseOverLocations().isEmpty()) {
        final Geometry geometry = deleteVertex();
        setMeasureGeometry(geometry);

        clearMouseOverLocations();
      }
    }
  }

  private void modeMeasureClear() {
    clearOverlayAction(MEASURE);
    setMeasureGeometry(EMPTY_GEOMETRY);
    this.dragged = false;

    final DataType oldValue = this.measureDataType;
    this.measureDataType = null;
    firePropertyChange("measureDataType", oldValue, null);
  }

  protected boolean modeMeasureClick(final MouseEvent event) {
    final int modifiers = event.getModifiersEx();
    if (modifiers == 0 && event.getButton() == MouseEvent.BUTTON1) {
      if (isOverlayAction(MEASURE)) {
        final int clickCount = event.getClickCount();
        Point point = getSnapPoint();
        if (point == null) {
          point = getPoint(event);
        }
        if (clickCount == 1) {
          final Geometry measureGeometry = getMeasureGeometry();
          final GeometryFactory geometryFactory = getGeometryFactory();
          if (measureGeometry.isEmpty()) {
            setMeasureGeometry(point);
          } else if (measureGeometry instanceof Point) {
            final Point from = (Point)measureGeometry;
            if (!from.equals(point)) {
              final LineString line = geometryFactory.lineString(from, point);
              setMeasureGeometry(line);
            }
          } else if (this.measureDataType == DataTypes.LINE_STRING) {
            if (measureGeometry instanceof LineString) {
              LineString line = (LineString)measureGeometry;
              final Point to = line.getToPoint();
              if (!to.equals(point)) {
                line = line.appendVertex(point);
                setMeasureGeometry(line);
              }
            }
          } else {
            if (measureGeometry instanceof LineString) {
              LineString line = (LineString)measureGeometry;
              final Point from = line.getToVertex(0);
              if (!from.equals(point)) {
                line = line.appendVertex(point);
                setMeasureGeometry(line);
              }
              if (line.getVertexCount() > 2) {
                if (!line.isClosed()) {
                  final Vertex firstPoint = line.getVertex(0);
                  line = line.appendVertex(firstPoint);
                }
                setMeasureGeometry(geometryFactory.polygon(line));
              }
            } else if (measureGeometry instanceof Polygon) {
              final Polygon polygon = (Polygon)measureGeometry;
              setMeasureGeometry(polygon.appendVertex(point, 0));
            }
          }
          event.consume();

          repaint();
          return true;
        }
      }
    }
    return false;
  }

  protected boolean modeMeasureDrag(final MouseEvent event) {
    if (isOverlayAction(MEASURE)) {
      this.dragged = true;
      if (getMouseOverLocations().isEmpty()) {
        modeMeasureUpdateXorGeometry();
      } else {
        getMap().clearToolTipText();
        final BoundingBox boundingBox = getHotspotBoundingBox(event);

        Geometry xorGeometry = null;
        for (final CloseLocation location : getMouseOverLocations()) {
          final Geometry locationGeometry = getVertexGeometry(event, location);
          if (locationGeometry != null) {
            if (xorGeometry == null) {
              xorGeometry = locationGeometry;
            } else {
              xorGeometry = xorGeometry.union(locationGeometry);
            }
          }
        }
        setXorGeometry(xorGeometry);
        if (!hasSnapPoint(event, boundingBox)) {
          setMapCursor(CURSOR_NODE_ADD);
        }
        return true;
      }
    }
    return false;
  }

  protected boolean modeMeasureFinish(final MouseEvent event) {
    if (isOverlayAction(MEASURE)) {
      if (!getMouseOverLocations().isEmpty()) {
        if (event.getButton() == MouseEvent.BUTTON1) {
          for (final CloseLocation location : getMouseOverLocations()) {
            final Geometry geometry = location.getGeometry();
            final GeometryFactory geometryFactory = getGeometryFactory();
            final Point point;
            if (getSnapPoint() == null) {
              point = getPoint(geometryFactory, event);
            } else {
              point = getSnapPoint().newGeometry(geometryFactory);
            }
            final int[] vertexIndex = location.getVertexId();
            Geometry newGeometry;
            final Point newPoint = point;
            if (vertexIndex == null) {
              final int[] segmentIndex = location.getSegmentId();
              final int[] newIndex = segmentIndex.clone();
              newIndex[newIndex.length - 1] = newIndex[newIndex.length - 1] + 1;
              newGeometry = geometry.insertVertex(newPoint, newIndex);
            } else {
              newGeometry = geometry.moveVertex(newPoint, vertexIndex);
            }
            setMeasureGeometry(newGeometry);
          }
          return true;
        }
      }
      this.dragged = false;
    }
    return false;
  }

  protected boolean modeMeasureMove(final MouseEvent event) {
    if (isOverlayAction(MEASURE)) {
      final BoundingBox boundingBox = getHotspotBoundingBox();
      final CloseLocation location = getMap().findCloseLocation(null, null, this.measureGeometry,
        boundingBox);
      final List<CloseLocation> locations = new ArrayList<>();
      if (location != null) {
        locations.add(location);
      }
      final boolean hasMouseOver = setMouseOverLocations(locations);

      // TODO make work with multi-part
      if (!hasMouseOver) {
        modeMeasureUpdateXorGeometry();
      }
      return true;
    }
    return false;
  }

  protected boolean modeMeasureStart(final MouseEvent event) {
    final int modifiers = event.getModifiersEx();
    if (modifiers == InputEvent.BUTTON1_DOWN_MASK) {
      if (isOverlayAction(MEASURE)) {
        if (!getMouseOverLocations().isEmpty()) {
          repaint();
          return true;
        }
      }
    }
    return false;
  }

  protected void modeMeasureUpdateXorGeometry() {
    final BoundingBox boundingBox = getHotspotBoundingBox();
    final Point point = getOverlayPoint();
    if (!hasSnapPoint(boundingBox)) {
      setMapCursor(CURSOR);
    }
    Geometry xorGeometry = null;

    if (this.measureGeometry.isEmpty()) {
    } else {
      Vertex fromPoint;
      final Vertex toPoint;
      if (this.measureGeometry instanceof LineString) {
        fromPoint = this.measureGeometry.getVertex(0);
        toPoint = this.measureGeometry.getToVertex(0);
      } else {
        fromPoint = this.measureGeometry.getVertex(0, 0);
        toPoint = this.measureGeometry.getToVertex(0, 0);
      }
      final GeometryFactory geometryFactory = getGeometryFactory();
      if (toPoint != null && !toPoint.isEmpty()) {
        if (this.measureGeometry instanceof Point) {
          xorGeometry = geometryFactory.lineString(toPoint, point);
        } else {
          if (toPoint.equals(fromPoint) || this.measureDataType == DataTypes.LINE_STRING) {
            xorGeometry = newXorLine(geometryFactory, toPoint, point);
          } else {
            final Point p1 = geometryFactory.point(toPoint);
            final Point p3 = geometryFactory.point(fromPoint);
            final GeometryFactory viewportGeometryFactory = getViewportGeometryFactory();
            xorGeometry = viewportGeometryFactory.lineString(p1, point, p3);
          }
        }
      }
    }
    setXorGeometry(xorGeometry);
  }

  @Override
  public void mouseClicked(final MouseEvent event) {
    if (modeMeasureClick(event)) {
    }
  }

  @Override
  public void mouseDragged(final MouseEvent event) {
    if (modeMeasureDrag(event)) {
    }
  }

  @Override
  public void mouseEntered(final MouseEvent e) {
    modeMeasureMove(e);
  }

  @Override
  public void mouseExited(final MouseEvent e) {
    setXorGeometry(null);
  }

  @Override
  public void mouseMoved(final MouseEvent event) {
    if (modeMeasureMove(event)) {
    }
  }

  @Override
  public void mousePressed(final MouseEvent event) {
    if (modeMeasureStart(event)) {
    }
  }

  @Override
  public void mouseReleased(final MouseEvent event) {
    if (modeMeasureFinish(event)) {
      modeMeasureMove(event);
    }
  }

  @Override
  protected void paintComponent(final Viewport2D viewport, final Graphics2D graphics) {
    if (!this.measureGeometry.isEmpty()) {
      final GeometryFactory viewportGeometryFactory = viewport
        .getRoundedGeometryFactory(getViewportGeometryFactory());
      try (
        BaseCloseable transformCloseable = viewport.setUseModelCoordinates(graphics, true)) {
        MEASURE_RENDERER.paintSelected(viewport, graphics, viewportGeometryFactory,
          this.measureGeometry);
        if (this.measureGeometry instanceof Polygon) {
          final Polygon polygon = (Polygon)this.measureGeometry;
          GeometryStyleRecordLayerRenderer.renderPolygon(viewport, graphics, polygon,
            POLYGON_STYLE);
        }
      }

      if (!(this.measureGeometry instanceof Punctual)) {
        final TextStyle measureTextStyle = new TextStyle();
        measureTextStyle.setTextBoxColor(WebColors.Violet);
        measureTextStyle.setTextSize(Quantities.getQuantity(14, CustomUnits.PIXEL));
        measureTextStyle.setTextFaceName(Font.MONOSPACED);

        Point textPoint;
        measureTextStyle.setTextHorizontalAlignment("right");
        if (this.measureDataType == DataTypes.POLYGON && this.measureGeometry instanceof Polygon) {
          measureTextStyle.setTextDx(Quantities.getQuantity(-5, CustomUnits.PIXEL));
          measureTextStyle.setTextPlacementType("vertex(n-1)");
          measureTextStyle.setTextVerticalAlignment("middle");
          textPoint = this.measureGeometry.getVertex(0, -2);
        } else {
          measureTextStyle.setTextDx(Quantities.getQuantity(-7, CustomUnits.PIXEL));
          measureTextStyle.setTextDy(Quantities.getQuantity(-2, CustomUnits.PIXEL));
          measureTextStyle.setTextPlacementType("vertex(n)");
          measureTextStyle.setTextVerticalAlignment("top");
          textPoint = this.measureGeometry.getVertex(-1);
        }
        TextStyleRenderer.renderText(viewport, graphics, this.measureLabel, textPoint,
          measureTextStyle);
      }
    }
    drawXorGeometry(graphics);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final String propertyName = event.getPropertyName();
    if (propertyName.equals("overlayAction")) {
      final MapPanel map = getMap();
      if (!map.hasOverlayAction(MEASURE)) {
        modeMeasureClear();
      }
    }
  }

  public void setMeasureDataType(final DataType measureDataType) {
    final DataType oldValue = this.measureDataType;
    if (oldValue != measureDataType) {
      this.measureDataType = measureDataType;

      setMeasureGeometry(EMPTY_GEOMETRY);
      this.dragged = false;

      this.measureDataType = measureDataType;
      firePropertyChange("measureDataType", oldValue, measureDataType);
    }
  }

  public void setMeasureGeometry(Geometry measureGeometry) {
    if (measureGeometry == null) {
      measureGeometry = EMPTY_GEOMETRY;
    }
    if (measureGeometry != this.measureGeometry) {
      this.measureGeometry = measureGeometry;
      if (measureGeometry == null) {
        this.measureLabel = "";
      } else {

        Unit<Length> lengthUnit = Units.METRE;
        final CoordinateSystem coordinateSystem = measureGeometry.getCoordinateSystem();
        if (coordinateSystem instanceof ProjectedCoordinateSystem) {
          lengthUnit = coordinateSystem.getLengthUnit();
        }
        final double length = measureGeometry.getLength(lengthUnit);

        @SuppressWarnings("unchecked")
        final Unit<Area> areaUnit = (Unit<Area>)lengthUnit.multiply(lengthUnit);
        final double area = measureGeometry.getArea(areaUnit);
        final String unitString = lengthUnit.toString();

        synchronized (MEASURE_FORMAT) {
          final StringBuilder label = new StringBuilder();
          final String lengthString = MEASURE_FORMAT.format(Doubles.makePrecise(100, length));
          label.append(lengthString);
          label.append(unitString);

          if (this.measureDataType == DataTypes.POLYGON && measureGeometry instanceof Polygon) {
            final String areaString = MEASURE_FORMAT.format(Doubles.makePrecise(100, area));
            label.append(" \n");
            label.append(areaString);
            label.append(unitString);
            label.append('\u00B2');
          }
          this.measureLabel = label.toString();
        }
      }
      setXorGeometry(null);
    }
  }

  protected boolean setMouseOverLocations(final List<CloseLocation> mouseOverLocations) {
    if (this.mouseOverLocations.equals(mouseOverLocations)) {
      return !this.mouseOverLocations.isEmpty();
    } else {
      this.mouseOverLocations = mouseOverLocations;
      setSnapPoint(null);
      setXorGeometry(null);

      return updateMouseOverLocations();
    }
  }

  public void toggleMeasureMode(final DataType measureDataType) {
    final MapPanel map = getMap();
    if (this.measureDataType == null) {
      if (map.setOverlayAction(MEASURE)) {
        setMeasureDataType(measureDataType);
      }
    } else if (measureDataType == this.measureDataType && map.hasOverlayAction(MEASURE)) {
      modeMeasureClear();
    } else {
      if (map.setOverlayAction(MEASURE)) {
        setMeasureDataType(measureDataType);
      }
    }
  }

  private boolean updateMouseOverLocations() {
    final MapPanel map = getMap();
    if (this.mouseOverLocations.isEmpty()) {
      map.clearToolTipText();
      return false;
    } else {
      final Map<String, Set<CloseLocation>> vertexLocations = new TreeMap<>();
      final Map<String, Set<CloseLocation>> segmentLocations = new TreeMap<>();

      for (final CloseLocation location : this.mouseOverLocations) {
        if (location.getVertexId() == null) {
          Maps.addToSet(segmentLocations, "Measure", location);
        } else {
          Maps.addToSet(vertexLocations, "Measure", location);
        }
      }
      final StringBuilder text = new StringBuilder("<html>");
      appendLocations(text, "Move Vertices", vertexLocations);
      appendLocations(text, "Insert Vertices", segmentLocations);
      text.append("</html>");

      final Point2D eventPoint = getEventPosition();
      map.setToolTipText(eventPoint, text);
      if (vertexLocations.isEmpty()) {
        setMapCursor(CURSOR_LINE_ADD_NODE);
      } else {
        setMapCursor(CURSOR_NODE_EDIT);
      }
    }
    return true;
  }
}
