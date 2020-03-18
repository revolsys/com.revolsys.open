package com.revolsys.swing.map.overlay;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
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

import org.jeometry.common.awt.WebColors;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.number.Doubles;
import org.jeometry.coordinatesystem.model.unit.CustomUnits;

import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.geometry.model.editor.GeometryEditor;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.AbstractAction;
import com.revolsys.swing.action.RunnableAction;
import com.revolsys.swing.action.enablecheck.ObjectPropertyEnableCheck;
import com.revolsys.swing.events.KeyEvents;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.layer.record.style.TextStyle;
import com.revolsys.swing.map.overlay.record.SelectedRecordsVertexRenderer;
import com.revolsys.swing.map.view.graphics.Graphics2DViewRenderer;
import com.revolsys.swing.menu.MenuFactory;

import tech.units.indriya.quantity.Quantities;
import tech.units.indriya.unit.Units;

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

  @Override
  protected void cancel() {
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
    final Geometry geometry = getMeasureGeometry();

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
          final GeometryEditor<?> geometryEditor = geometry.newGeometryEditor();
          geometryEditor.deleteVertex(vertexId);
          if (geometryEditor.isModified()) {
            return geometryEditor.newGeometry();
          }
        } catch (final Exception e) {
          SwingUtil.beep();
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
    final GeometryFactory geometryFactory = getGeometryFactory2d();
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

  public void initMenuTools(final MenuFactory tools) {
    final AbstractAction measureLengthAction = new RunnableAction("Measure Length",
      Icons.getIcon("ruler_line"), () -> toggleMeasureMode(GeometryDataTypes.LINE_STRING))
        .setAcceleratorAltKey(KeyEvent.VK_M);
    tools.addCheckboxMenuItem("map", measureLengthAction,
      new ObjectPropertyEnableCheck(this, "measureDataType", GeometryDataTypes.LINE_STRING));

    final AbstractAction measureAreaAction = new RunnableAction("Measure Area",
      Icons.getIcon("ruler_polygon"), () -> toggleMeasureMode(GeometryDataTypes.POLYGON))
        .setAcceleratorAltKey(KeyEvent.VK_E);
    tools.addCheckboxMenuItem("map", measureAreaAction,
      new ObjectPropertyEnableCheck(this, "measureDataType", GeometryDataTypes.POLYGON));
  }

  @Override
  public void keyPressed(final KeyEvent event) {
    final int keyCode = event.getKeyCode();
    if (KeyEvents.altKey(event, KeyEvent.VK_M)) {
      // if (isOverlayAction(MEASURE)) {
      // setMeasureGeometry(EMPTY_GEOMETRY);
      // } else {
      // if (this.measureDataType == null) {
      // this.measureDataType = GeometryDataTypes.LINE_STRING;
      // }
      // setOverlayAction(MEASURE);
      // }
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
          final GeometryFactory geometryFactory = getGeometryFactory2d();
          if (measureGeometry.isEmpty()) {
            setMeasureGeometry(point);
          } else if (measureGeometry instanceof Point) {
            final Point from = (Point)measureGeometry;
            if (!from.equals(point)) {
              final LineString line = geometryFactory.lineString(from, point);
              setMeasureGeometry(line);
            }
          } else if (this.measureDataType == GeometryDataTypes.LINE_STRING) {
            if (measureGeometry instanceof LineString) {
              LineString line = (LineString)measureGeometry;
              final Point to = line.getToPoint();
              if (!to.equals(point)) {
                final Point newPoint = point;
                line = line.editLine(editor -> editor.appendVertex(newPoint));
                setMeasureGeometry(line);
              }
            }
          } else {
            if (measureGeometry instanceof LineString) {
              LineString line = (LineString)measureGeometry;
              final Point from = line.getToVertex(0);
              if (!from.equals(point)) {
                final Point newPoint = point;
                line = line.editLine(editor -> editor.appendVertex(newPoint));
                setMeasureGeometry(line);
              }
              if (line.getVertexCount() > 2) {
                if (!line.isClosed()) {
                  final Vertex firstPoint = line.getVertex(0);
                  line = line.editLine(editor -> editor.appendVertex(firstPoint));
                }
                setMeasureGeometry(geometryFactory.polygon(line));
              }
            } else if (measureGeometry instanceof Polygon) {
              final Polygon polygon = (Polygon)measureGeometry;
              final Point newPoint = point;
              setMeasureGeometry(polygon.edit(editor -> editor.appendVertex(new int[] {
                0
              }, newPoint)));
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
        if (!hasSnapPoint()) {
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
            final GeometryFactory geometryFactory = getGeometryFactory2d();
            final Point point;
            if (getSnapPoint() == null) {
              point = getPoint(geometryFactory, event);
            } else {
              point = getSnapPoint().newGeometry(geometryFactory);
            }
            final int[] vertexId = location.getVertexId();
            final GeometryEditor<?> geometryEditor = geometry.newGeometryEditor();
            final Point newPoint = point;
            if (vertexId == null) {
              final int[] segmentIndex = location.getSegmentId();
              final int[] newVertexId = segmentIndex.clone();
              newVertexId[newVertexId.length - 1] = newVertexId[newVertexId.length - 1] + 1;
              geometryEditor.insertVertex(newVertexId, newPoint);
            } else {
              geometryEditor.setVertex(vertexId, newPoint);
            }
            final Geometry newGeometry = geometryEditor.newGeometry();
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
      final MapPanel map = getMap();
      final CloseLocation location = map.findCloseLocation(null, null, this.measureGeometry);
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
    final Point point = getOverlayPoint();
    if (!hasSnapPoint()) {
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
      final GeometryFactory geometryFactory = getGeometryFactory2d();
      if (toPoint != null && !toPoint.isEmpty()) {
        if (this.measureGeometry instanceof Point) {
          xorGeometry = geometryFactory.lineString(toPoint, point);
        } else {
          if (toPoint.equals(fromPoint) || this.measureDataType == GeometryDataTypes.LINE_STRING) {
            xorGeometry = newXorLine(geometryFactory, toPoint, point);
          } else {
            final Point p1 = geometryFactory.point(toPoint);
            final Point p3 = geometryFactory.point(fromPoint);
            final GeometryFactory viewportGeometryFactory = getViewportGeometryFactory2d();
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
  protected void paintComponent(final Graphics2DViewRenderer view, final Graphics2D graphics) {
    if (!this.measureGeometry.isEmpty()) {
      MEASURE_RENDERER.paintSelected(view, this.measureGeometry);
      if (this.measureGeometry instanceof Polygon) {
        final Polygon polygon = (Polygon)this.measureGeometry;
        view.drawGeometry(polygon, POLYGON_STYLE);
      }

      if (!(this.measureGeometry instanceof Punctual)) {
        final TextStyle measureTextStyle = new TextStyle();
        measureTextStyle.setTextBoxColor(WebColors.Violet);
        measureTextStyle.setTextSize(Quantities.getQuantity(14, CustomUnits.PIXEL));
        measureTextStyle.setTextFaceName(Font.MONOSPACED);

        Point textPoint;
        measureTextStyle.setTextHorizontalAlignment("right");
        if (this.measureDataType == GeometryDataTypes.POLYGON
          && this.measureGeometry instanceof Polygon) {
          measureTextStyle.setTextDx(Quantities.getQuantity(-5, CustomUnits.PIXEL));
          measureTextStyle.setTextPlacementType("vertex(n-1)");
          measureTextStyle.setTextVerticalAlignment("middle");
          textPoint = this.measureGeometry.getToVertex(0, 1);
        } else {
          measureTextStyle.setTextDx(Quantities.getQuantity(-7, CustomUnits.PIXEL));
          measureTextStyle.setTextDy(Quantities.getQuantity(-2, CustomUnits.PIXEL));
          measureTextStyle.setTextPlacementType("vertex(n)");
          measureTextStyle.setTextVerticalAlignment("top");
          textPoint = this.measureGeometry.getToVertex(0);
        }
        view.newTextStyleViewRenderer(measureTextStyle)//
          .drawText(this.measureLabel, textPoint);
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
        final GeometryFactory geometryFactory = measureGeometry.getGeometryFactory();
        if (geometryFactory.isProjected()) {
          lengthUnit = geometryFactory.getHorizontalLengthUnit();
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

          if (this.measureDataType == GeometryDataTypes.POLYGON
            && measureGeometry instanceof Polygon) {
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
