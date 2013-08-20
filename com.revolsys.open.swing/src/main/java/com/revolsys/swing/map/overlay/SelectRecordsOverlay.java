package com.revolsys.swing.map.overlay;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.SwingUtilities;

import org.jdesktop.swingx.color.ColorUtil;

import com.revolsys.awt.WebColors;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.jts.IsSimpleOp;
import com.revolsys.gis.jts.IsValidOp;
import com.revolsys.gis.model.coordinates.Coordinates;
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
import com.revolsys.swing.parallel.SwingWorkerManager;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.operation.valid.TopologyValidationError;

@SuppressWarnings("serial")
public class SelectRecordsOverlay extends AbstractOverlay {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

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

  protected static final MarkerStyle STYLE_ERROR = MarkerStyle.marker(
    "ellipse", 7, WebColors.Yellow, 1, WebColors.Red);

  protected static final GeometryStyle STYLE_HIGHLIGHT = GeometryStyle.polygon(
    COLOR_SELECT, 3, COLOR_SELECT_TRANSPARENT);

  protected static final GeometryStyle STYLE_OUTLINE = GeometryStyle.line(COLOR_OUTLINE);

  protected static final MarkerStyle STYLE_VERTEX = MarkerStyle.marker(
    "ellipse", 6, COLOR_OUTLINE, 1, COLOR_SELECT);

  static {
    MarkerStyle.setMarker(STYLE_HIGHLIGHT, "ellipse", 6,
      COLOR_OUTLINE_TRANSPARENT, 1, COLOR_SELECT_TRANSPARENT);
    MarkerStyle.setMarker(STYLE_OUTLINE, "ellipse", 6,
      COLOR_OUTLINE_TRANSPARENT, 1, COLOR_SELECT_TRANSPARENT);
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
    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
      clearMapCursor();
      this.selectBox = null;
      this.selectBoxFirstPoint = null;
      repaint();
    }
  }

  @Override
  public void keyReleased(final KeyEvent e) {
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
      SwingWorkerManager.execute("Select records", this, "selectRecords",
        boundingBox);
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
    for (final Layer layer : layerGroup.getLayers()) {
      if (layer instanceof LayerGroup) {
        final LayerGroup childGroup = (LayerGroup)layer;
        paint(graphics2d, childGroup);
      } else if (layer instanceof DataObjectLayer) {
        final DataObjectLayer dataObjectLayer = (DataObjectLayer)layer;
        for (final LayerDataObject object : getSelectedObjects(dataObjectLayer)) {
          if (object != null && dataObjectLayer.isVisible(object)) {
            final Geometry geometry = object.getGeometryValue();
            paintSelected(graphics2d, geometry, STYLE_HIGHLIGHT, STYLE_OUTLINE,
              STYLE_VERTEX);
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

  protected void paintSelected(final Graphics2D graphics2d, Geometry geometry,
    final GeometryStyle highlightStyle, final GeometryStyle outlineStyle,
    final MarkerStyle vertexStyle) {
    if (geometry != null && !geometry.isEmpty()) {
      final Viewport2D viewport = getViewport();
      final GeometryFactory viewportGeometryFactory = getViewportGeometryFactory();
      geometry = viewport.getGeometry(geometry);

      GeometryStyleRenderer.renderGeometry(viewport, graphics2d, geometry,
        highlightStyle);
      GeometryStyleRenderer.renderOutline(viewport, graphics2d, geometry,
        outlineStyle);
      MarkerStyleRenderer.renderMarkerVertices(viewport, graphics2d, geometry,
        vertexStyle);
      final IsValidOp validOp = new IsValidOp(geometry);
      if (validOp.isValid()) {
        final IsSimpleOp simpleOp = new IsSimpleOp(geometry);
        if (!simpleOp.isSimple()) {
          for (final Coordinates coordinates : simpleOp.getNonSimplePoints()) {
            final Point point = viewportGeometryFactory.createPoint(coordinates);
            MarkerStyleRenderer.renderMarker(viewport, graphics2d, point,
              STYLE_ERROR);
          }
        }
      } else {
        for (final TopologyValidationError error : validOp.getErrors()) {
          final Point point = viewportGeometryFactory.createPoint(error.getCoordinate());
          MarkerStyleRenderer.renderMarker(viewport, graphics2d, point,
            STYLE_ERROR);
        }
      }
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
    SwingWorkerManager.execute("Select records", this, methodName, boundingBox);
    event.consume();
  }

  public void selectBoxStart(final MouseEvent event) {
    setMapCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
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
