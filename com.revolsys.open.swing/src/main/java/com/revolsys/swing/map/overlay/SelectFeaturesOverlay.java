package com.revolsys.swing.map.overlay;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.color.ColorUtil;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.SwingWorkerManager;
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
import com.vividsolutions.jts.geom.Point;

@SuppressWarnings("serial")
public class SelectFeaturesOverlay extends JComponent implements
  PropertyChangeListener, MouseListener, MouseMotionListener, KeyListener {

  private static final BasicStroke BOX_STROKE = new BasicStroke(2,
    BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 2, new float[] {
      6, 6
    }, 0f);

  private List<DataObjectLayer> selectableLayers = new ArrayList<DataObjectLayer>();

  private final Project project;

  private final MapPanel map;

  private final Viewport2D viewport;

  private Double selectBox;

  private java.awt.Point selectBoxFirstPoint;

  private Cursor cursor;

  private final GeometryStyle highlightStyle;

  private final GeometryStyle outlineStyle;

  private final MarkerStyle vertexStyle;

  private final Color boxFillColor;

  private final Color boxOutlineColor;

  public SelectFeaturesOverlay(final MapPanel map) {
    this(map, new Color(0, 255, 0));
  }

  protected SelectFeaturesOverlay(final MapPanel map, final Color color) {
    final Color transparentColor = ColorUtil.setAlpha(color, 127);
    highlightStyle = GeometryStyle.polygon(color, 3, transparentColor);
    outlineStyle = GeometryStyle.line(new Color(0, 0, 0, 255));
    vertexStyle = MarkerStyle.marker("ellipse", 6, new Color(0, 0, 0, 127), 1,
      transparentColor);

    boxOutlineColor = new Color(color.getRed() / 2, color.getGreen() / 2,
      color.getBlue() / 2);
    boxFillColor = ColorUtil.setAlpha(boxOutlineColor, 127);

    this.map = map;
    this.viewport = map.getViewport();
    this.project = map.getProject();

    map.addMapOverlay(this);
    updateSelectableLayers();
  }

  public boolean hasSelectableLayers() {
    return !this.selectableLayers.isEmpty();
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
      restoreCursor();
      selectBox = null;
      selectBoxFirstPoint = null;
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
    if (hasSelectableLayers()) {
      if (event.getClickCount() == 1 && isSelectEvent(event)) {
        final int x = event.getX();
        final int y = event.getY();
        final double[] location = viewport.toModelCoordinates(x, y);
        final GeometryFactory geometryFactory = viewport.getGeometryFactory();
        BoundingBox boundingBox = new BoundingBox(geometryFactory, location[0],
          location[1]);
        final double modelUnitsPerViewUnit = viewport.getModelUnitsPerViewUnit();
        boundingBox = boundingBox.expand(modelUnitsPerViewUnit * 5);
        SwingWorkerManager.execute("Select objects", this, "selectObjects",
          boundingBox);
        event.consume();
      }
    }
  }

  @Override
  public void mouseDragged(final MouseEvent event) {
    if (selectBoxFirstPoint != null) {
      selectBoxDrag(event);
      event.consume();
    }
  }

  @Override
  public void mouseEntered(final MouseEvent e) {
  }

  @Override
  public void mouseExited(final MouseEvent e) {
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
  }

  @Override
  public void mousePressed(final MouseEvent event) {
    if (hasSelectableLayers()) {
      if (isSelectEvent(event)) {
        selectBoxStart(event);
      }
    }
  }

  @Override
  public void mouseReleased(final MouseEvent event) {
    if (selectBoxFirstPoint != null) {
      selectBoxFinish(event);
    }
  }

  @Override
  public void paintComponent(final Graphics graphics) {
    final Graphics2D graphics2d = (Graphics2D)graphics;
    for (final DataObjectLayer layer : selectableLayers) {
      for (final DataObject object : getSelectedObjects(layer)) {
        if (object != null && layer.isVisible(object)) {
          final Geometry geometry = object.getGeometryValue();
          MarkerStyleRenderer.renderMarkerVertices(viewport, graphics2d,
            geometry, vertexStyle);
          GeometryStyleRenderer.renderGeometry(viewport, graphics2d, geometry,
            highlightStyle);
          GeometryStyleRenderer.renderOutline(viewport, graphics2d, geometry,
            outlineStyle);
        }
      }
    }
    if (selectBox != null) {
      graphics2d.setColor(boxOutlineColor);
      graphics2d.setStroke(BOX_STROKE);
      graphics2d.draw(selectBox);
      graphics2d.setPaint(boxFillColor);
      graphics2d.fill(selectBox);
    }
  }

  public GeometryStyle getOutlineStyle() {
    return outlineStyle;
  }

  public GeometryStyle getHighlightStyle() {
    return highlightStyle;
  }

  public MarkerStyle getVertexStyle() {
    return vertexStyle;
  }

  protected Collection<DataObject> getSelectedObjects(
    final DataObjectLayer layer) {
    return layer.getSelectedObjects();
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final String propertyName = event.getPropertyName();
    if ("layers".equals(propertyName)) {
      updateSelectableLayers();
      repaint();
    } else if ("selectable".equals(propertyName)) {
      updateSelectableLayers();
      repaint();
    } else if ("visible".equals(propertyName)) {
      updateSelectableLayers();
      repaint();
    }
  }

  private void restoreCursor() {
    if (cursor != null) {
      setCursor(cursor);
      cursor = null;
    }
  }

  private void saveCursor() {
    cursor = getCursor();
  }

  public void selectBoxDrag(final MouseEvent event) {
    final double width = Math.abs(event.getX() - selectBoxFirstPoint.getX());
    final double height = Math.abs(event.getY() - selectBoxFirstPoint.getY());
    final java.awt.Point topLeft = new java.awt.Point(); // java.awt.Point
    if (selectBoxFirstPoint.getX() < event.getX()) {
      topLeft.setLocation(selectBoxFirstPoint.getX(), 0);
    } else {
      topLeft.setLocation(event.getX(), 0);
    }

    if (selectBoxFirstPoint.getY() < event.getY()) {
      topLeft.setLocation(topLeft.getX(), selectBoxFirstPoint.getY());
    } else {
      topLeft.setLocation(topLeft.getX(), event.getY());
    }
    selectBox.setRect(topLeft.getX(), topLeft.getY(), width, height);
    event.consume();
    repaint();
  }

  public void selectBoxFinish(final MouseEvent event) {
    // Convert first point to envelope top left in map coords.
    final int minX = (int)selectBox.getMinX();
    final int minY = (int)selectBox.getMinY();
    final Point topLeft = viewport.toModelPoint(minX, minY);

    // Convert second point to envelope bottom right in map coords.
    final int maxX = (int)selectBox.getMaxX();
    final int maxY = (int)selectBox.getMaxY();
    final Point bottomRight = viewport.toModelPoint(maxX, maxY);

    final GeometryFactory geometryFactory = map.getGeometryFactory();
    final BoundingBox boundingBox = new BoundingBox(geometryFactory,
      topLeft.getX(), topLeft.getY(), bottomRight.getX(), bottomRight.getY());

    selectBoxFirstPoint = null;
    selectBox = null;
    restoreCursor();
    repaint();
    SwingWorkerManager.execute("Select objects", this, "selectObjects",
      boundingBox);
    event.consume();
  }

  public void selectBoxStart(final MouseEvent event) {
    saveCursor();
    setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
    selectBoxFirstPoint = event.getPoint();
    selectBox = new Rectangle2D.Double();
    event.consume();
  }

  public void selectObjects(final BoundingBox boundingBox) {
    for (final DataObjectLayer layer : selectableLayers) {
      layer.setSelectedObjects(boundingBox);
    }
  }

  protected void updateSelectableLayers() {
    final List<DataObjectLayer> selectableLayers = new ArrayList<DataObjectLayer>();
    updateSelectableLayers(project, selectableLayers);
    this.selectableLayers = selectableLayers;
    if (selectableLayers.isEmpty()) {
      setEnabled(false);
    } else {
      setEnabled(true);
    }
  }

  public List<DataObjectLayer> getSelectableLayers() {
    return selectableLayers;
  }

  protected void updateSelectableLayers(final LayerGroup group,
    final List<DataObjectLayer> selectableLayers) {
    for (final Layer layer : group.getLayers()) {
      if (layer instanceof LayerGroup) {
        final LayerGroup childGroup = (LayerGroup)layer;
        updateSelectableLayers(childGroup, selectableLayers);
      } else if (layer instanceof DataObjectLayer) {
        final DataObjectLayer dataObjectLayer = (DataObjectLayer)layer;
        if (isSelectable(dataObjectLayer)) {
          selectableLayers.add(dataObjectLayer);
        }
      }
    }

  }

  protected boolean isSelectable(final DataObjectLayer dataObjectLayer) {
    return dataObjectLayer.isSelectable();
  }
}
