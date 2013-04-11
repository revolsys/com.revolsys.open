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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import com.revolsys.famfamfam.silk.SilkIconLoader;
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
public class AddGeometryOverlay extends JComponent implements
  PropertyChangeListener, MouseListener, MouseMotionListener {

  private final Project project;

  private final Viewport2D viewport;

  private ListCoordinatesList points = new ListCoordinatesList(2);

  private Geometry geometry;

  private Point firstPoint;

  private Point previousPoint;

  private GeometryFactory geometryFactory;

  private static final GeometryStyle HIGHLIGHT_STYLE = GeometryStyle.polygon(
    new Color(0, 255, 255, 255), 3, new Color(0, 255, 255, 127));

  private static final GeometryStyle XOR_LINE_STYLE = GeometryStyle.line(
    new Color(0, 0, 255), 2);

  private static final GeometryStyle OUTLINE_STYLE = GeometryStyle.line(new Color(
    0, 0, 0, 255));

  private static final MarkerStyle VERTEX_STYLE = MarkerStyle.marker("ellipse",
    6, new Color(0, 0, 0, 127), 1, new Color(0, 255, 255, 127));

  private DataType geometryDataType;

  private DataObjectLayer addFeatureLayer;

  private Geometry completedGeometry;

  private int actionId = 0;

  private Geometry xorGeometry;

  private EventListenerList completedActions = new EventListenerList();

  private List<DataObjectLayer> editableLayers = new ArrayList<DataObjectLayer>();

  private Cursor cursor;

  private final Cursor addNodeCursor = SilkIconLoader.getCursor(
    "cursor_new_node", 8, 7);

  public AddGeometryOverlay(final MapPanel map) {
    this.viewport = map.getViewport();
    this.project = map.getProject();
    this.geometryFactory = viewport.getGeometryFactory();

    project.addPropertyChangeListener(this);

    map.addMapOverlay(this);
    updateEditableLayers();
    setEnabled(false);
  }

  protected void actionAddGeometryCompleted() {
    if (isGeometryValid()) {
      firstPoint = null;
      previousPoint = null;
      xorGeometry = null;
      this.completedGeometry = geometry;
      fireActionPerformed("Geometry Complete");
      geometry = null;
      points = new ListCoordinatesList(2);
    }
  }

  public void addCompletedAction(final ActionListener listener) {
    completedActions.add(ActionListener.class, listener);
  }

  public void clearCompletedActions() {
    completedActions = new EventListenerList();
  }

  protected Geometry createGeometry() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final ListCoordinatesList points = getPoints();
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
      if (!(xorGeometry instanceof Point)) {
        GeometryStyleRenderer.renderGeometry(viewport, graphics, xorGeometry,
          XOR_LINE_STYLE);
      }
    }
  }

  protected void fireActionPerformed(final String command) {
    final ActionEvent actionEvent = new ActionEvent(this, actionId++, command);
    for (final ActionListener listener : completedActions.getListeners(ActionListener.class)) {
      listener.actionPerformed(actionEvent);
    }
  }

  public DataObjectLayer getAddFeatureLayer() {
    return addFeatureLayer;
  }

  @SuppressWarnings("unchecked")
  public <G extends Geometry> G getCompletedGeometry() {
    return (G)completedGeometry;
  }

  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  protected Point getPoint(final MouseEvent event) {
    final java.awt.Point eventPoint = event.getPoint();
    final Point point = viewport.toModelPoint(eventPoint);
    return geometryFactory.copy(point);
  }

  protected ListCoordinatesList getPoints() {
    return points;
  }

  public boolean hasEditableLayers() {
    return !this.editableLayers.isEmpty();
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

  @Override
  public void mouseDragged(final MouseEvent event) {
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
    if (SwingUtilities.isLeftMouseButton(event)) {
      event.consume();
    }
  }

  @Override
  public void mouseReleased(final MouseEvent event) {
  }

  @Override
  public void paintComponent(final Graphics graphics) {
    final Graphics2D graphics2d = (Graphics2D)graphics;
    for (final DataObjectLayer layer : editableLayers) {
      for (final DataObject object : layer.getEditingObjects()) {
        final Geometry geometry = object.getGeometryValue();
        GeometryStyleRenderer.renderGeometry(viewport, graphics2d, geometry,
          HIGHLIGHT_STYLE);
        GeometryStyleRenderer.renderOutline(viewport, graphics2d, geometry,
          OUTLINE_STYLE);
        MarkerStyleRenderer.renderMarkerVertices(viewport, graphics2d,
          geometry, VERTEX_STYLE);
      }
    }

    if (geometry != null) {
      final GeometryFactory viewGeometryFactory = viewport.getGeometryFactory();
      final Geometry mapGeometry = viewGeometryFactory.copy(geometry);
      if (!(geometry instanceof Point)) {
        GeometryStyleRenderer.renderGeometry(viewport, graphics2d, mapGeometry,
          HIGHLIGHT_STYLE);
        GeometryStyleRenderer.renderOutline(viewport, graphics2d, mapGeometry,
          OUTLINE_STYLE);
      }
      MarkerStyleRenderer.renderMarkerVertices(viewport, graphics2d,
        mapGeometry, VERTEX_STYLE);
    }
    drawXorGeometry(graphics2d);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final String propertyName = event.getPropertyName();
    if ("layers".equals(propertyName)) {
      updateEditableLayers();
    } else if ("editable".equals(propertyName)) {
      updateEditableLayers();
    } else if ("visible".equals(propertyName)) {
      updateEditableLayers();
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
   * @param addFeatureLayer 
   */
  public void setAddFeatureLayer(final DataObjectLayer addFeatureLayer) {
    if (addFeatureLayer == null) {
      this.addFeatureLayer = addFeatureLayer;
      setEnabled(false);
      restoreCursor();
    } else {
      final DataObjectMetaData metaData = addFeatureLayer.getMetaData();
      final Attribute geometryAttribute = metaData.getGeometryAttribute();
      if (geometryAttribute == null) {
        this.addFeatureLayer = null;
        setEnabled(false);
        restoreCursor();
      } else {
        this.addFeatureLayer = addFeatureLayer;
        this.geometryFactory = metaData.getGeometryFactory();
        this.geometryDataType = geometryAttribute.getType();
        setEnabled(true);
        saveCursor();
        getParent().setCursor(addNodeCursor);
      }
    }
  }

  public void setCompletedAction(final ActionListener listener) {
    clearCompletedActions();
    addCompletedAction(listener);
  }

  private void updateEditableLayers() {
    final List<DataObjectLayer> editableLayers = new ArrayList<DataObjectLayer>();
    updateEditableLayers(project, editableLayers);
    this.editableLayers = editableLayers;
  }

  private void updateEditableLayers(final LayerGroup group,
    final List<DataObjectLayer> editableLayers) {
    for (final Layer layer : group.getLayers()) {
      if (layer instanceof LayerGroup) {
        final LayerGroup childGroup = (LayerGroup)layer;
        updateEditableLayers(childGroup, editableLayers);
      } else if (layer instanceof DataObjectLayer) {
        final DataObjectLayer dataObjectLayer = (DataObjectLayer)layer;
        if (dataObjectLayer.isEditable()) {
          editableLayers.add(dataObjectLayer);
        }
      }
    }

  }
}
