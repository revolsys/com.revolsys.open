package com.revolsys.swing.map.overlay;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.SwingUtilities;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.ListCoordinatesList;
import com.revolsys.gis.model.geometry.LineSegment;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.layer.dataobject.renderer.MarkerStyleRenderer;
import com.revolsys.swing.map.layer.dataobject.style.GeometryStyle;
import com.revolsys.swing.map.layer.dataobject.style.MarkerStyle;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

@SuppressWarnings("serial")
public class EditGeometryOverlay extends SelectFeaturesOverlay implements
  PropertyChangeListener, MouseListener, MouseMotionListener {

  private final Project project;

  private final Viewport2D viewport;

  private ListCoordinatesList points = new ListCoordinatesList(2);

  private Geometry geometry;

  private Point firstPoint;

  private Point previousPoint;

  private GeometryFactory geometryFactory;

  private static final MarkerStyle XOR_POINT_STYLE = MarkerStyle.marker(
    "ellipse", 10, new Color(0, 0, 255), 1, new Color(0, 0, 255));

  private static final GeometryStyle XOR_LINE_STYLE = GeometryStyle.line(
    new Color(0, 0, 255), 2);

  private DataType geometryDataType;

  private DataObjectLayer layer;

  private String mode;

  private int actionId = 0;

  private Geometry xorGeometry;

  private Cursor cursor;

  private DataObject object;

  public DataObject getObject() {
    return object;
  }

  private final Cursor addNodeCursor = SilkIconLoader.getCursor(
    "cursor_new_node", 8, 7);

  private ActionListener completedAction;

  public EditGeometryOverlay(final MapPanel map) {
    super(map, new Color(0, 255, 255));

    this.viewport = map.getViewport();
    this.project = map.getProject();
    this.geometryFactory = viewport.getGeometryFactory();

    project.addPropertyChangeListener(this);

    map.addMapOverlay(this);
  }

  protected void actionAddGeometryCompleted() {
    if (isGeometryValid()) {
      try {
        firstPoint = null;
        previousPoint = null;
        xorGeometry = null;
        points = new ListCoordinatesList(2);
        if ("add".equals(mode)) {
          object = layer.createObject();
          if (object != null) {
            object.setGeometryValue(geometry);
            mode = "edit";
          }
        }
        fireActionPerformed(completedAction, "Geometry Complete");
      } finally {
      }
    }
  }

  protected Geometry createGeometry() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    Geometry geometry = null;
    final int size = points.size();
    if (size == 1) {
      geometry = geometryFactory.createPoint(points);
    } else if (size == 2 || DataTypes.LINE_STRING.equals(geometryDataType)
      || DataTypes.MULTI_LINE_STRING.equals(geometryDataType)) {
      geometry = geometryFactory.createLineString(points);
    } else if (DataTypes.POLYGON.equals(geometryDataType)) {
      final Coordinates endPoint = points.get(0);
      final CoordinatesList ring = CoordinatesListUtil.subList(points, null, 0,
        size, endPoint);
      geometry = geometryFactory.createPolygon(ring);
    }
    return geometry;
  }

  protected LineString createXorLine(final Point p0, final Point p1) {
    final GeometryFactory viewportGeometryFactory = viewport.getGeometryFactory();
    final Coordinates c0 = CoordinatesUtil.get(viewportGeometryFactory.copy(p0));
    final Coordinates c1 = CoordinatesUtil.get(viewportGeometryFactory.copy(p1));
    final LineSegment line = new LineSegment(viewportGeometryFactory, c0, c1);
    final double length = line.getLength();
    final double cursorRadius = viewport.getModelUnitsPerViewUnit() * 6;
    final Coordinates newC1 = line.pointAlongOffset((length - cursorRadius)
      / length, 0);
    Point point = viewportGeometryFactory.createPoint(newC1);
    point = geometryFactory.copy(point);
    return geometryFactory.createLineString(p0, point);
  }

  protected void drawXorGeometry(final Graphics2D graphics) {
    if (xorGeometry != null) {
      graphics.setXORMode(Color.WHITE);
      if (xorGeometry instanceof Point) {
        Point point = (Point)xorGeometry;
        MarkerStyleRenderer.renderMarker(viewport, graphics, point,
          XOR_POINT_STYLE);
      } else {
        GeometryStyleRenderer.renderGeometry(viewport, graphics, xorGeometry,
          XOR_LINE_STYLE);
      }
    }
  }

  protected void fireActionPerformed(ActionListener listener,
    final String command) {
    if (listener != null) {
      final ActionEvent actionEvent = new ActionEvent(this, actionId++, command);
      listener.actionPerformed(actionEvent);
    }
  }

  public DataObjectLayer getLayer() {
    return layer;
  }

  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  protected Point getPoint(final MouseEvent event) {
    final java.awt.Point eventPoint = event.getPoint();
    final Point point = viewport.toModelPoint(eventPoint);
    return geometryFactory.copy(point);
  }

  protected boolean isGeometryValid() {
    if (DataTypes.POINT.equals(geometryDataType)) {
      if (geometry instanceof Point) {
        return true;
      } else {
        return false;
      }
    } else if (DataTypes.MULTI_POINT.equals(geometryDataType)) {
      if ((geometry instanceof Point) || (geometry instanceof MultiPoint)) {
        return true;
      } else {
        return false;
      }
    } else if (DataTypes.LINE_STRING.equals(geometryDataType)) {
      if (geometry instanceof LineString) {
        return true;
      } else {
        return false;
      }
    } else if (DataTypes.MULTI_LINE_STRING.equals(geometryDataType)) {
      if ((geometry instanceof LineString)
        || (geometry instanceof MultiLineString)) {
        return true;
      } else {
        return false;
      }
    } else if (DataTypes.POLYGON.equals(geometryDataType)) {
      if (geometry instanceof Polygon) {
        return true;
      } else {
        return false;
      }
    } else if (DataTypes.MULTI_POLYGON.equals(geometryDataType)) {
      if ((geometry instanceof Polygon) || (geometry instanceof MultiPolygon)) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  @Override
  public void mouseClicked(final MouseEvent event) {
    if ("add".equals(mode)) {
      modeAddMouseClick(event);
    }
  }

  protected void modeAddMouseClick(final MouseEvent event) {
    if (SwingUtilities.isLeftMouseButton(event)) {
      final Point point = getPoint(event);
      final int size = points.size();
      if (size == 0) {
        points.add(point);
        firstPoint = point;
      } else {
        Coordinates lastPoint = points.get(size - 1);
        if (!CoordinatesUtil.get(point).equals(lastPoint)) {
          points.add(point);
          previousPoint = point;
        }
      }

      geometry = createGeometry();
      xorGeometry = null;
      event.consume();
      if (DataTypes.POINT.equals(geometryDataType)) {
        actionAddGeometryCompleted();
      }
      if (event.getClickCount() == 2) {
        actionAddGeometryCompleted();
      }
      repaint();
    }
  }

  public boolean isSelectEvent(final MouseEvent event) {
    if (!"add".equals(mode) && SwingUtilities.isLeftMouseButton(event)) {
      final boolean keyPress = event.isAltDown();
      return keyPress;
    }
    return false;
  }

  protected Collection<DataObject> getSelectedObjects(
    final DataObjectLayer layer) {
    if ("add".equals(mode)) {
      return Collections.emptyList();
    } else {
      return layer.getEditingObjects();
    }
  }

  @Override
  public void mouseDragged(final MouseEvent event) {
    super.mouseDragged(event);
  }

  @Override
  public void mouseEntered(final MouseEvent e) {
  }

  @Override
  public void mouseExited(final MouseEvent e) {
  }

  @Override
  public void mouseMoved(final MouseEvent event) {
    final Graphics2D graphics = (Graphics2D)getGraphics();
    final Point point = getPoint(event);
    drawXorGeometry(graphics);
    if (firstPoint == null) {
      xorGeometry = null;
    } else if (previousPoint == null) {
      xorGeometry = createXorLine(firstPoint, point);
    } else if (DataTypes.LINE_STRING.equals(geometryDataType)
      || DataTypes.MULTI_LINE_STRING.equals(geometryDataType)) {
      xorGeometry = createXorLine(previousPoint, point);
    } else {
      xorGeometry = geometryFactory.createLineString(previousPoint, point,
        firstPoint);
    }
    drawXorGeometry(graphics);
  }

  @Override
  public void mousePressed(final MouseEvent event) {
    if ("add".equals(mode) && SwingUtilities.isLeftMouseButton(event)) {
      event.consume();
    } else {
      super.mousePressed(event);
    }
  }

  @Override
  public void mouseReleased(final MouseEvent event) {
    super.mouseReleased(event);
  }

  @Override
  public void paintComponent(final Graphics graphics) {
    super.paintComponent(graphics);
    final Graphics2D graphics2d = (Graphics2D)graphics;

    if (geometry != null) {
      final GeometryFactory viewGeometryFactory = viewport.getGeometryFactory();
      final Geometry mapGeometry = viewGeometryFactory.copy(geometry);
      if (!(geometry instanceof Point)) {
        GeometryStyleRenderer.renderGeometry(viewport, graphics2d, mapGeometry,
          getHighlightStyle());
        GeometryStyleRenderer.renderOutline(viewport, graphics2d, mapGeometry,
          getOutlineStyle());
      }
      MarkerStyleRenderer.renderMarkerVertices(viewport, graphics2d,
        mapGeometry, getVertexStyle());
    }
    drawXorGeometry(graphics2d);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    super.propertyChange(event);
    final String propertyName = event.getPropertyName();
    if ("editable".equals(propertyName)) {
      updateSelectableLayers();
      repaint();
    }
    repaint();

  }

  private void restoreCursor() {
    if (cursor != null) {
      getParent().setCursor(cursor);
      cursor = null;
    }
  }

  private void saveCursor() {
    cursor = getParent().getCursor();
  }

  /**
   * Set the layer that a new feature is to be added to.
   * 
   * @param layer 
   */
  public void addObject(final DataObjectLayer layer,
    ActionListener completedAction) {
    // TODO what if there is already editing going on with unsaved changes?
    clearEditingObjects(project);
    if (layer == null) {
      this.layer = layer;
      restoreCursor();
      mode = null;
    } else {
      this.completedAction = completedAction;
      final DataObjectMetaData metaData = layer.getMetaData();
      final Attribute geometryAttribute = metaData.getGeometryAttribute();
      if (geometryAttribute == null) {
        this.layer = null;
        restoreCursor();
        mode = null;
      } else {
        mode = "add";
        this.layer = layer;
        this.geometryFactory = metaData.getGeometryFactory();
        this.geometryDataType = geometryAttribute.getType();
        saveCursor();
        getParent().setCursor(addNodeCursor);
      }
    }
  }

  private void clearEditingObjects(LayerGroup layerGroup) {
    for (Layer layer : layerGroup.getLayers()) {
      if (layer instanceof LayerGroup) {
        LayerGroup childGroup = (LayerGroup)layer;
        clearEditingObjects(childGroup);
      }
      if (layer instanceof DataObjectLayer) {
        DataObjectLayer dataObjectLayer = (DataObjectLayer)layer;
        dataObjectLayer.clearEditingObjects();
      }
    }

  }

  @Override
  public void selectObjects(BoundingBox boundingBox) {
    for (final DataObjectLayer layer : getEditableLayers()) {
      layer.setEditingObjects(boundingBox);
    }
  }

  public List<DataObjectLayer> getEditableLayers() {
    return getSelectableLayers();
  }

  public boolean hasEditableLayers() {
    return hasSelectableLayers();
  }

  @Override
  protected boolean isSelectable(DataObjectLayer dataObjectLayer) {
    return isEditable(dataObjectLayer);
  }

  protected boolean isEditable(DataObjectLayer dataObjectLayer) {
    return dataObjectLayer.isCanEditObjects();
  }
}
