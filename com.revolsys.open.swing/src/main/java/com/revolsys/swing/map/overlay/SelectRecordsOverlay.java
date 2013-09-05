package com.revolsys.swing.map.overlay;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.SwingUtilities;

import org.jdesktop.swingx.color.ColorUtil;

import com.revolsys.awt.WebColors;
import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.jts.IsSimpleOp;
import com.revolsys.gis.jts.IsValidOp;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.map.layer.dataobject.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.layer.dataobject.renderer.MarkerStyleRenderer;
import com.revolsys.swing.map.layer.dataobject.style.GeometryStyle;
import com.revolsys.swing.map.layer.dataobject.style.MarkerStyle;
import com.revolsys.swing.map.layer.dataobject.style.marker.Marker;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.MathUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.valid.TopologyValidationError;

public class SelectRecordsOverlay extends AbstractOverlay {
  protected static final BasicStroke BOX_STROKE = new BasicStroke(2,
    BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 2, new float[] {
      6, 6
    }, 0f);

  protected static final Color COLOR_BOX = WebColors.Green;

  protected static final Color COLOR_BOX_TRANSPARENT = ColorUtil.setAlpha(
    COLOR_BOX, 127);

  protected static final Color COLOR_OUTLINE = WebColors.Black;

  protected static final Color COLOR_OUTLINE_TRANSPARENT = new Color(0, 0, 0,
    127);

  protected static final Color COLOR_SELECT = WebColors.Lime;

  protected static final Color COLOR_SELECT_TRANSPARENT = ColorUtil.setAlpha(
    COLOR_SELECT, 127);

  private static final Cursor CURSOR_SELECT_BOX = SilkIconLoader.getCursor(
    "cursor_select_box", 9, 9);

  private static final long serialVersionUID = 1L;

  protected static final MarkerStyle STYLE_ERROR = MarkerStyle.marker(
    "ellipse", 7, WebColors.Yellow, 1, WebColors.Red);

  protected static final GeometryStyle STYLE_HIGHLIGHT = GeometryStyle.polygon(
    COLOR_SELECT, 3, COLOR_SELECT_TRANSPARENT);

  protected static final GeometryStyle STYLE_OUTLINE = GeometryStyle.line(COLOR_OUTLINE);

  protected static final MarkerStyle STYLE_VERTEX = MarkerStyle.marker(
    vertexShape(), 9, COLOR_OUTLINE, 1, COLOR_SELECT);

  protected static final MarkerStyle STYLE_VERTEX_LAST_POINT = MarkerStyle.marker(
    lastVertexShape(), 9, COLOR_OUTLINE, 1, COLOR_SELECT);

  protected static final MarkerStyle STYLE_VERTEX_FIRST_POINT = MarkerStyle.marker(
    firstVertexShape(), 9, COLOR_OUTLINE, 1, COLOR_SELECT);

  static {
    MarkerStyle.setMarker(STYLE_HIGHLIGHT, "ellipse", 6,
      COLOR_OUTLINE_TRANSPARENT, 1, COLOR_SELECT_TRANSPARENT);
    MarkerStyle.setMarker(STYLE_OUTLINE, "ellipse", 6,
      COLOR_OUTLINE_TRANSPARENT, 1, COLOR_SELECT_TRANSPARENT);

    STYLE_VERTEX.setMarkerOrientationType("auto");

    STYLE_VERTEX_LAST_POINT.setMarkerOrientationType("auto");
    STYLE_VERTEX_LAST_POINT.setMarkerPlacement("point(n)");
    STYLE_VERTEX_LAST_POINT.setMarkerHorizontalAlignment("right");
  }

  public static GeneralPath firstVertexShape() {
    final GeneralPath path = new GeneralPath(new Ellipse2D.Double(0, 0, 11, 11));
    path.moveTo(5, 4);
    path.lineTo(6, 5);
    path.lineTo(5, 6);
    path.lineTo(4, 5);
    path.lineTo(5, 4);
    return path;
  }

  public static GeneralPath lastVertexShape() {
    final GeneralPath path = new GeneralPath();
    path.moveTo(0, 0);
    path.lineTo(10, 5);
    path.lineTo(0, 10);
    path.lineTo(0, 0);
    path.closePath();
    return path;
  }

  public static void paintSelected(final Viewport2D viewport,
    final GeometryFactory viewportGeometryFactory, final Graphics2D graphics,
    Geometry geometry) {
    if (geometry != null && !geometry.isEmpty()) {
      geometry = viewport.getGeometry(geometry);

      GeometryStyleRenderer.renderGeometry(viewport, graphics, geometry,
        STYLE_HIGHLIGHT);
      GeometryStyleRenderer.renderOutline(viewport, graphics, geometry,
        STYLE_OUTLINE);

      if (!geometry.isEmpty()) {
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
          final Geometry part = geometry.getGeometryN(i);
          if (part instanceof LineString) {
            final LineString lineString = (LineString)part;
            final CoordinatesList points = CoordinatesListUtil.get(lineString);
            renderMarkers(viewport, graphics, points);
          } else if (part instanceof Polygon) {
            final Polygon polygon = (Polygon)part;
            final List<CoordinatesList> pointsList = CoordinatesListUtil.getAll(polygon);
            for (final CoordinatesList points : pointsList) {
              renderMarkers(viewport, graphics, points);
            }
          }
        }
      }

      final IsValidOp validOp = new IsValidOp(geometry);
      if (validOp.isValid()) {
        final IsSimpleOp simpleOp = new IsSimpleOp(geometry);
        if (!simpleOp.isSimple()) {
          for (final Coordinates coordinates : simpleOp.getNonSimplePoints()) {
            final Point point = viewportGeometryFactory.createPoint(coordinates);
            MarkerStyleRenderer.renderMarker(viewport, graphics, point,
              STYLE_ERROR);
          }
        }
      } else {
        for (final TopologyValidationError error : validOp.getErrors()) {
          final Point point = viewportGeometryFactory.createPoint(error.getCoordinate());
          MarkerStyleRenderer.renderMarker(viewport, graphics, point,
            STYLE_ERROR);
        }
      }
    }
  }

  private static void renderMarkers(final Viewport2D viewport,
    final Graphics2D graphics, final CoordinatesList points) {
    final boolean savedUseModelUnits = viewport.setUseModelCoordinates(false,
      graphics);
    final Paint paint = graphics.getPaint();
    try {
      final int pointCount = points.size();
      if (pointCount > 1) {
        for (int i = 0; i < pointCount; i++) {
          MarkerStyle style;
          if (i == 0) {
            style = STYLE_VERTEX_FIRST_POINT;
          } else if (i == pointCount - 1) {
            style = STYLE_VERTEX_LAST_POINT;
          } else {
            style = STYLE_VERTEX;
          }
          final double x = points.getX(i);
          final double y = points.getY(i);
          double orientation = 0;
          if (i == 0) {
            final double x1 = points.getX(i + 1);
            final double y1 = points.getY(i + 1);
            orientation = MathUtil.angleDegrees(x, y, x1, y1);
          } else {
            final double x1 = points.getX(i - 1);
            final double y1 = points.getY(i - 1);
            orientation = MathUtil.angleDegrees(x1, y1, x, y);
          }
          final Marker marker = style.getMarker();
          marker.render(viewport, graphics, style, x, y, orientation);
        }
      }
    } finally {
      graphics.setPaint(paint);
      viewport.setUseModelCoordinates(savedUseModelUnits, graphics);
    }
  }

  public static GeneralPath vertexShape() {
    final GeneralPath path = new GeneralPath();
    path.moveTo(5, 0);
    path.lineTo(10, 5);
    path.lineTo(5, 10);
    path.lineTo(0, 10);
    path.lineTo(0, 0);
    path.closePath();
    path.moveTo(5, 4);
    path.lineTo(6, 5);
    path.lineTo(5, 6);
    path.lineTo(4, 5);
    path.lineTo(5, 4);
    return path;
  }

  private Double selectBox;

  private java.awt.Point selectBoxFirstPoint;

  public SelectRecordsOverlay(final MapPanel map) {
    super(map);
  }

  public void addSelectedRecords(final BoundingBox boundingBox) {
    final LayerGroup project = getProject();
    addSelectedRecords(project, boundingBox);
    final LayerRendererOverlay overlay = getMap().getLayerOverlay();
    overlay.redraw();
  }

  private void addSelectedRecords(final LayerGroup group,
    final BoundingBox boundingBox) {

    final double scale = getViewport().getScale();
    final List<Layer> layers = group.getLayers();
    Collections.reverse(layers);
    for (final Layer layer : layers) {
      if (layer instanceof LayerGroup) {
        final LayerGroup childGroup = (LayerGroup)layer;
        addSelectedRecords(childGroup, boundingBox);
      } else if (layer instanceof DataObjectLayer) {
        final DataObjectLayer dataObjectLayer = (DataObjectLayer)layer;
        if (dataObjectLayer.isSelectable(scale)) {
          dataObjectLayer.addSelectedRecords(boundingBox);
        }
      }
    }
  }

  protected Collection<LayerDataObject> getSelectedObjects(
    final DataObjectLayer layer) {
    return layer.getSelectedRecords();
  }

  protected boolean isSelectable(final DataObjectLayer dataObjectLayer) {
    return dataObjectLayer.isSelectable();
  }

  public boolean isSelectEvent(final MouseEvent event) {
    if (SwingUtilities.isLeftMouseButton(event)) {
      final boolean keyPress = event.isControlDown() || event.isMetaDown();
      return keyPress;
    }
    return false;
  }

  @Override
  public void keyPressed(final KeyEvent e) {
    final int keyCode = e.getKeyCode();
    if (keyCode == KeyEvent.VK_ESCAPE) {
      clearMapCursor();
      this.selectBox = null;
      this.selectBoxFirstPoint = null;
      repaint();
    } else if (keyCode == KeyEvent.VK_CONTROL || keyCode == KeyEvent.VK_META) {
      setMapCursor(CURSOR_SELECT_BOX);
      e.consume();
    }
  }

  @Override
  public void keyReleased(final KeyEvent e) {
    clearMapCursor();
  }

  @Override
  public void keyTyped(final KeyEvent e) {
  }

  @Override
  public void mouseClicked(final MouseEvent event) {
    if (event.getClickCount() == 1 && isSelectEvent(event)) {
      final int x = event.getX();
      final int y = event.getY();
      final double[] location = getViewport().toModelCoordinates(x, y);
      final GeometryFactory geometryFactory = getViewportGeometryFactory();
      BoundingBox boundingBox = new BoundingBox(geometryFactory, location[0],
        location[1]);
      final double modelUnitsPerViewUnit = getViewport().getModelUnitsPerViewUnit();
      boundingBox = boundingBox.expand(modelUnitsPerViewUnit * 5);
      Invoke.background("Select records", this, "selectRecords", boundingBox);
      event.consume();
    }
  }

  @Override
  public void mouseDragged(final MouseEvent event) {
    if (this.selectBoxFirstPoint != null) {
      selectBoxDrag(event);
      event.consume();
    }
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
    if (e.getButton() == 0) {
      if (e.isControlDown() || e.isMetaDown()) {
        setMapCursor(CURSOR_SELECT_BOX);
        e.consume();
      }
    }
  }

  @Override
  public void mousePressed(final MouseEvent event) {
    if (isSelectEvent(event)) {
      selectBoxStart(event);
    }
  }

  @Override
  public void mouseReleased(final MouseEvent event) {
    if (this.selectBoxFirstPoint != null) {
      selectBoxFinish(event);
    }
  }

  protected void paint(final Graphics2D graphics2d, final LayerGroup layerGroup) {
    final Viewport2D viewport = getViewport();
    final GeometryFactory viewportGeometryFactory = getViewportGeometryFactory();
    for (final Layer layer : layerGroup.getLayers()) {
      if (layer instanceof LayerGroup) {
        final LayerGroup childGroup = (LayerGroup)layer;
        paint(graphics2d, childGroup);
      } else if (layer instanceof DataObjectLayer) {
        final DataObjectLayer dataObjectLayer = (DataObjectLayer)layer;
        for (final LayerDataObject object : getSelectedObjects(dataObjectLayer)) {
          if (object != null && dataObjectLayer.isVisible(object)) {
            final Geometry geometry = object.getGeometryValue();
            paintSelected(viewport, viewportGeometryFactory, graphics2d,
              geometry);
          }
        }
      }
    }
  }

  @Override
  public void paintComponent(final Graphics2D graphics) {
    final LayerGroup layerGroup = getProject();
    paint(graphics, layerGroup);
    paintSelectBox(graphics);
  }

  protected void paintSelectBox(final Graphics2D graphics2d) {
    if (this.selectBox != null) {
      graphics2d.setColor(COLOR_BOX);
      graphics2d.setStroke(BOX_STROKE);
      graphics2d.draw(this.selectBox);
      graphics2d.setPaint(COLOR_BOX_TRANSPARENT);
      graphics2d.fill(this.selectBox);
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final String propertyName = event.getPropertyName();
    if ("layers".equals(propertyName)) {
      repaint();
    } else if ("selectable".equals(propertyName)) {
      repaint();
    } else if ("visible".equals(propertyName)) {
      repaint();
    } else if ("editable".equals(propertyName)) {
      repaint();
    } else if ("updateObject".equals(propertyName)) {
      repaint();
    } else if ("hasSelectedRecords".equals(propertyName)) {
      clearUndoHistory();
    }
  }

  public void removeSelectedRecords(final BoundingBox boundingBox) {
    final LayerGroup project = getProject();
    removeSelectedRecords(project, boundingBox);
    final LayerRendererOverlay overlay = getMap().getLayerOverlay();
    overlay.redraw();
  }

  private void removeSelectedRecords(final LayerGroup group,
    final BoundingBox boundingBox) {

    final double scale = getViewport().getScale();
    final List<Layer> layers = group.getLayers();
    Collections.reverse(layers);
    for (final Layer layer : layers) {
      if (layer instanceof LayerGroup) {
        final LayerGroup childGroup = (LayerGroup)layer;
        removeSelectedRecords(childGroup, boundingBox);
      } else if (layer instanceof DataObjectLayer) {
        final DataObjectLayer dataObjectLayer = (DataObjectLayer)layer;
        if (dataObjectLayer.isSelectable(scale)) {
          dataObjectLayer.removeSelectedRecords(boundingBox);
        }
      }
    }
  }

  public void selectBoxDrag(final MouseEvent event) {
    setMapCursor(CURSOR_SELECT_BOX);
    final double width = Math.abs(event.getX()
      - this.selectBoxFirstPoint.getX());
    final double height = Math.abs(event.getY()
      - this.selectBoxFirstPoint.getY());
    final java.awt.Point topLeft = new java.awt.Point(); // java.awt.Point
    if (this.selectBoxFirstPoint.getX() < event.getX()) {
      topLeft.setLocation(this.selectBoxFirstPoint.getX(), 0);
    } else {
      topLeft.setLocation(event.getX(), 0);
    }

    if (this.selectBoxFirstPoint.getY() < event.getY()) {
      topLeft.setLocation(topLeft.getX(), this.selectBoxFirstPoint.getY());
    } else {
      topLeft.setLocation(topLeft.getX(), event.getY());
    }
    this.selectBox.setRect(topLeft.getX(), topLeft.getY(), width, height);
    event.consume();
    repaint();
  }

  public void selectBoxFinish(final MouseEvent event) {
    // Convert first point to envelope top left in map coords.
    final int minX = (int)this.selectBox.getMinX();
    final int minY = (int)this.selectBox.getMinY();
    final Point topLeft = getViewport().toModelPoint(minX, minY);

    // Convert second point to envelope bottom right in map coords.
    final int maxX = (int)this.selectBox.getMaxX();
    final int maxY = (int)this.selectBox.getMaxY();
    final Point bottomRight = getViewport().toModelPoint(maxX, maxY);

    final GeometryFactory geometryFactory = getMap().getGeometryFactory();
    final BoundingBox boundingBox = new BoundingBox(geometryFactory,
      topLeft.getX(), topLeft.getY(), bottomRight.getX(), bottomRight.getY());

    this.selectBoxFirstPoint = null;
    this.selectBox = null;
    clearMapCursor();
    repaint();
    String methodName;
    if (event.isAltDown()) {
      methodName = "removeSelectedRecords";
    } else if (event.isShiftDown()) {
      methodName = "addSelectedRecords";
    } else {
      methodName = "selectRecords";
    }
    Invoke.background("Select records", this, methodName, boundingBox);
    event.consume();
  }

  public void selectBoxStart(final MouseEvent event) {
    setMapCursor(CURSOR_SELECT_BOX);
    this.selectBoxFirstPoint = event.getPoint();
    this.selectBox = new Rectangle2D.Double();
    event.consume();
  }

  public void selectRecords(final BoundingBox boundingBox) {
    final LayerGroup project = getProject();
    selectRecords(project, boundingBox);
    final LayerRendererOverlay overlay = getMap().getLayerOverlay();
    overlay.redraw();
  }

  private void selectRecords(final LayerGroup group,
    final BoundingBox boundingBox) {

    final double scale = getViewport().getScale();
    final List<Layer> layers = group.getLayers();
    Collections.reverse(layers);
    for (final Layer layer : layers) {
      if (layer instanceof LayerGroup) {
        final LayerGroup childGroup = (LayerGroup)layer;
        selectRecords(childGroup, boundingBox);
      } else if (layer instanceof DataObjectLayer) {
        final DataObjectLayer dataObjectLayer = (DataObjectLayer)layer;
        if (dataObjectLayer.isSelectable(scale)) {
          dataObjectLayer.setSelectedRecords(boundingBox);
        } else {
          dataObjectLayer.clearSelectedRecords();
        }
      }
    }
  }
}
