package com.revolsys.swing.map.overlay;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import com.revolsys.awt.WebColors;
import com.revolsys.data.record.Record;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.map.ImageViewport;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.renderer.AbstractRecordLayerRenderer;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.Property;

public class SelectRecordsOverlay extends AbstractOverlay {
  public static final String ACTION_SELECT_RECORDS = "Select Records";

  protected static final BasicStroke BOX_STROKE = new BasicStroke(2, BasicStroke.CAP_SQUARE,
    BasicStroke.JOIN_MITER, 2, new float[] {
      6, 6
  }, 0f);

  private static final Color COLOR_BOX = WebColors.Green;

  private static final Color COLOR_BOX_TRANSPARENT = WebColors.setAlpha(COLOR_BOX, 127);

  private static final Cursor CURSOR_SELECT_BOX = Icons.getCursor("cursor_select_box", 8, 7);

  private static final Cursor CURSOR_SELECT_BOX_ADD = Icons.getCursor("cursor_select_box_add", 8,
    7);

  private static final Cursor CURSOR_SELECT_BOX_DELETE = Icons.getCursor("cursor_select_box_delete",
    8, 7);

  public static final SelectedRecordsRenderer HIGHLIGHT_RENDERER = new SelectedRecordsRenderer(
    WebColors.Black, WebColors.Yellow);

  private static final Set<String> REDRAW_PROPERTY_NAMES = new HashSet<>(
    Arrays.asList("refresh", "viewBoundingBox", "unitsPerPixel", "scale"));

  private static final Set<String> REDRAW_REPAINT_PROPERTY_NAMES = new HashSet<>(
    Arrays.asList("layers", "selectable", "visible", "editable", "recordsChanged", "updateRecord",
      "hasSelectedRecords", "hasHighlightedRecords", "minimumScale", "maximumScale"));

  public static final SelectedRecordsRenderer SELECT_RENDERER = new SelectedRecordsRenderer(
    WebColors.Black, WebColors.Lime);

  private static final long serialVersionUID = 1L;

  private final AtomicLong redrawCount = new AtomicLong();

  private long redrawId = -1;

  private double selectBoxX1 = -1;

  private double selectBoxY1 = -1;

  private double selectBoxX2 = -1;

  private double selectBoxY2 = -1;

  private int selectBoxButton;

  private BufferedImage selectImage;

  public SelectRecordsOverlay(final MapPanel map) {
    super(map);
    setOverlayActionCursor(ACTION_SELECT_RECORDS, CURSOR_SELECT_BOX);
    addOverlayActionOverride(ACTION_SELECT_RECORDS, ZoomOverlay.ACTION_PAN,
      ZoomOverlay.ACTION_ZOOM);
    Property.addListener(map.getProject(), this);
  }

  public void addSelectedRecords(final BoundingBox boundingBox) {
    final LayerGroup project = getProject();
    addSelectedRecords(project, boundingBox);
    final LayerRendererOverlay overlay = getMap().getLayerOverlay();
    overlay.redraw();
  }

  private void addSelectedRecords(final LayerGroup group, final BoundingBox boundingBox) {

    final double scale = getViewport().getScale();
    final List<Layer> layers = group.getLayers();
    Collections.reverse(layers);
    for (final Layer layer : layers) {
      if (layer instanceof LayerGroup) {
        final LayerGroup childGroup = (LayerGroup)layer;
        addSelectedRecords(childGroup, boundingBox);
      } else if (layer instanceof AbstractRecordLayer) {
        final AbstractRecordLayer recordLayer = (AbstractRecordLayer)layer;
        if (recordLayer.isSelectable(scale)) {
          recordLayer.addSelectedRecords(boundingBox);
        }
      }
    }
  }

  protected void cancel() {
    selectBoxClear();
    repaint();
  }

  protected void doSelectRecords(final InputEvent event, final BoundingBox boundingBox) {
    String methodName;
    if (SwingUtil.isShiftDown(event)) {
      methodName = "addSelectedRecords";
    } else if (SwingUtil.isAltDown(event)) {
      methodName = "unSelectRecords";
    } else {
      methodName = "selectRecords";
    }
    Invoke.background("Select records", this, methodName, boundingBox);
  }

  @Override
  public void focusLost(final FocusEvent e) {
    cancel();
  }

  protected boolean isSelectable(final AbstractLayer recordLayer) {
    return recordLayer.isSelectable();
  }

  public boolean isSelectEvent(final MouseEvent event) {
    if (event.getButton() == MouseEvent.BUTTON1) {
      final boolean keyPress = SwingUtil.isControlOrMetaDown(event);
      return keyPress;
    }
    return false;
  }

  @Override
  public void keyPressed(final KeyEvent event) {
    final int keyCode = event.getKeyCode();
    if (keyCode == KeyEvent.VK_ESCAPE) {
      cancel();
    } else if (keyCode == KeyEvent.VK_CONTROL || keyCode == KeyEvent.VK_META) {
      if (isMouseInMap() && !hasOverlayAction()) {
        setSelectCursor(event);
      }
    } else if (isOverlayAction(ACTION_SELECT_RECORDS)) {
      if (keyCode == KeyEvent.VK_SHIFT) {
        setSelectCursor(event);
      } else if (keyCode == KeyEvent.VK_ALT) {
        setSelectCursor(event);
      }
    }
  }

  @Override
  public void keyReleased(final KeyEvent event) {
    final int keyCode = event.getKeyCode();
    if (isOverlayAction(ACTION_SELECT_RECORDS)) {
      if (keyCode == KeyEvent.VK_SHIFT) {
        setSelectCursor(event);
      } else if (keyCode == KeyEvent.VK_ALT) {
        setSelectCursor(event);
      } else if (keyCode == KeyEvent.VK_CONTROL || keyCode == KeyEvent.VK_META) {
        setSelectCursor(event);
      }
    }
  }

  @Override
  public void mouseDragged(final MouseEvent event) {
    if (selectBoxDrag(event)) {
    }
  }

  @Override
  public void mouseExited(final MouseEvent e) {
    if (this.selectBoxX1 == -1) {
      selectBoxClear();
    }
  }

  @Override
  public void mouseMoved(final MouseEvent event) {
    if (canOverrideOverlayAction(ACTION_SELECT_RECORDS)) {
      setSelectCursor(event);
    }
  }

  @Override
  public void mousePressed(final MouseEvent event) {
    if (selectBoxStart(event)) {
    }
  }

  @Override
  public void mouseReleased(final MouseEvent event) {
    if (selectBoxFinish(event)) {
    }
  }

  @Override
  public void paintComponent(final Graphics2D graphics) {
    final LayerGroup layerGroup = getProject();
    final long redrawId = this.redrawCount.longValue();
    if (redrawId != this.redrawId) {
      final Viewport2D viewport = getViewport();
      final int width = viewport.getViewWidthPixels();
      final int height = viewport.getViewHeightPixels();
      if (width > 0 && height > 0) {

        final Cursor oldCursor = getMapCursor();
        try {
          setMapCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

          try (
            final ImageViewport imageViewport = new ImageViewport(viewport)) {
            paintSelected(imageViewport, layerGroup);
            paintHighlighted(imageViewport, layerGroup);
            this.selectImage = imageViewport.getImage();
          }

          this.redrawId = redrawId;
        } finally {
          setMapCursor(oldCursor);
        }
      }
    }
    if (this.selectImage != null) {
      graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      graphics.drawRenderedImage(this.selectImage, null);
    }
    paintSelectBox(graphics);
  }

  protected void paintHighlighted(final ImageViewport viewport, final LayerGroup layerGroup) {
    final GeometryFactory viewportGeometryFactory = getViewportGeometryFactory();
    for (final Layer layer : layerGroup.getLayers()) {
      if (layer instanceof LayerGroup) {
        final LayerGroup childGroup = (LayerGroup)layer;
        paintHighlighted(viewport, childGroup);
      } else if (layer instanceof AbstractRecordLayer) {
        final AbstractRecordLayer recordLayer = (AbstractRecordLayer)layer;
        for (final LayerRecord record : recordLayer.getHighlightedRecords()) {
          if (record != null && recordLayer.isVisible(record)) {
            final Geometry geometry = record.getGeometry();
            final AbstractRecordLayerRenderer layerRenderer = layer.getRenderer();
            layerRenderer.renderSelectedRecord(viewport, recordLayer, record);
            HIGHLIGHT_RENDERER.paintSelected(viewport, viewportGeometryFactory, geometry);
          }
        }
      }
    }
  }

  protected void paintSelectBox(final Graphics2D graphics2d) {
    if (this.selectBoxX1 != -1) {
      final Viewport2D viewport = getViewport();
      final Point2D from = viewport.toViewPoint(this.selectBoxX1, this.selectBoxY1);
      final Point2D to = viewport.toViewPoint(this.selectBoxX2, this.selectBoxY2);

      final double x1 = from.getX();
      final double x2 = to.getX();
      final int x = (int)Math.min(x1, x2);
      final int width = (int)Math.abs(x1 - x2);

      final double y1 = from.getY();
      final double y2 = to.getY();
      final int y = (int)Math.min(y1, y2);
      final int height = (int)Math.abs(y1 - y2);

      graphics2d.setColor(COLOR_BOX);
      graphics2d.setStroke(BOX_STROKE);
      graphics2d.drawRect(x, y, width, height);
      graphics2d.setPaint(COLOR_BOX_TRANSPARENT);
      graphics2d.fillRect(x, y, width, height);
    }
  }

  protected void paintSelected(final ImageViewport viewport, final LayerGroup layerGroup) {
    final GeometryFactory viewportGeometryFactory = getViewportGeometryFactory();
    for (final Layer layer : layerGroup.getLayers()) {
      if (layer instanceof LayerGroup) {
        final LayerGroup childGroup = (LayerGroup)layer;
        paintSelected(viewport, childGroup);
      } else if (layer instanceof AbstractRecordLayer) {
        final AbstractRecordLayer recordLayer = (AbstractRecordLayer)layer;
        final AbstractRecordLayerRenderer layerRenderer = layer.getRenderer();
        if (recordLayer.isSelectable()) {
          final List<LayerRecord> selectedRecords = recordLayer.getSelectedRecords();
          for (final LayerRecord record : selectedRecords) {
            if (record != null && recordLayer.isVisible(record)) {
              if (!recordLayer.isHighlighted(record)) {
                if (!recordLayer.isDeleted(record)) {
                  final Geometry geometry = record.getGeometry();
                  layerRenderer.renderSelectedRecord(viewport, recordLayer, record);
                  SELECT_RENDERER.paintSelected(viewport, viewportGeometryFactory, geometry);
                }
              }
            }
          }
        }
      }
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final String propertyName = event.getPropertyName();
    final Object source = event.getSource();
    if (source instanceof Record || source instanceof LayerRenderer) {
      redraw();
    } else if (REDRAW_PROPERTY_NAMES.contains(propertyName)) {
      redraw();
    } else if (REDRAW_REPAINT_PROPERTY_NAMES.contains(propertyName)) {
      redrawAndRepaint();
    }
  }

  public void redraw() {
    this.redrawCount.incrementAndGet();
  }

  public void redrawAndRepaint() {
    this.redrawCount.incrementAndGet();
    repaint();
  }

  private void selectBoxClear() {
    clearOverlayAction(ACTION_SELECT_RECORDS);
    this.selectBoxX1 = -1;
    this.selectBoxY1 = -1;
    this.selectBoxX2 = -1;
    this.selectBoxY2 = -1;
  }

  private boolean selectBoxDrag(final MouseEvent event) {
    if (isOverlayAction(ACTION_SELECT_RECORDS) && this.selectBoxX1 != -1) {
      final Point point = getPoint(event);
      this.selectBoxX2 = point.getX();
      this.selectBoxY2 = point.getY();

      setSelectCursor(event);
      repaint();
      return true;
    }
    return false;
  }

  private boolean selectBoxFinish(final MouseEvent event) {
    if (event.getButton() == this.selectBoxButton && this.selectBoxX1 != -1) {
      if (clearOverlayAction(ACTION_SELECT_RECORDS)) {
        final MapPanel map = getMap();
        final GeometryFactory geometryFactory = map.getGeometryFactory();
        BoundingBox boundingBox = new BoundingBoxDoubleGf(geometryFactory, 2, this.selectBoxX1,
          this.selectBoxY1, this.selectBoxX2, this.selectBoxY2);
        final Viewport2D viewport = getViewport();
        final double minSize = viewport.getModelUnitsPerViewUnit() * 10;
        final double width = boundingBox.getWidth();
        double deltaX = 0;
        if (width < minSize) {
          deltaX = (minSize - width) / 2;
        }
        final double height = boundingBox.getWidth();
        double deltaY = 0;
        if (height < minSize) {
          deltaY = (minSize - height) / 2;
        }
        boundingBox = boundingBox.expand(deltaX, deltaY);
        if (!boundingBox.isEmpty()) {
          doSelectRecords(event, boundingBox);
        }
        selectBoxClear();
        event.consume();
        repaint();
        return true;
      }
    }
    return false;
  }

  private boolean selectBoxStart(final MouseEvent event) {
    if (isOverlayAction(ACTION_SELECT_RECORDS) && SwingUtil.isLeftButtonOnly(event)) {
      this.selectBoxButton = event.getButton();
      final Point point = getPoint(event);
      this.selectBoxX1 = this.selectBoxX2 = point.getX();
      this.selectBoxY1 = this.selectBoxY2 = point.getY();
      return true;
    }
    return false;
  }

  public void selectRecords(final BoundingBox boundingBox) {
    final LayerGroup project = getProject();
    selectRecords(project, boundingBox);
    final LayerRendererOverlay overlay = getMap().getLayerOverlay();
    overlay.redraw();
  }

  private void selectRecords(final LayerGroup group, final BoundingBox boundingBox) {

    final double scale = getViewport().getScale();
    final List<Layer> layers = group.getLayers();
    Collections.reverse(layers);
    for (final Layer layer : layers) {
      if (layer instanceof LayerGroup) {
        final LayerGroup childGroup = (LayerGroup)layer;
        selectRecords(childGroup, boundingBox);
      } else if (layer instanceof AbstractRecordLayer) {
        final AbstractRecordLayer recordLayer = (AbstractRecordLayer)layer;
        if (recordLayer.isSelectable(scale)) {
          recordLayer.setSelectedRecords(boundingBox);
        } else {
          recordLayer.clearSelectedRecords();
        }
      }
    }
  }

  protected void setSelectCursor(final InputEvent event) {
    Cursor cursor = null;
    if (event != null) {
      final boolean selectBox = SwingUtil.isControlOrMetaDown(event) || this.selectBoxX1 != -1;
      if (SwingUtil.isShiftDown(event)) {
        if (selectBox) {
          cursor = CURSOR_SELECT_BOX_ADD;
        }
      } else if (SwingUtil.isAltDown(event)) {
        if (selectBox) {
          cursor = CURSOR_SELECT_BOX_DELETE;
        }
      } else if (SwingUtil.isControlOrMetaDown(event)) {
        if (selectBox || !hasOverlayAction()) {
          cursor = CURSOR_SELECT_BOX;
        }
      } else if (this.selectBoxX1 != -1) {
        cursor = CURSOR_SELECT_BOX;
      }
    }
    if (cursor == null) {
      clearOverlayAction(ACTION_SELECT_RECORDS);
    } else {
      setOverlayAction(ACTION_SELECT_RECORDS);
      setMapCursor(cursor);
    }
  }

  public void unSelectRecords(final BoundingBox boundingBox) {
    final LayerGroup project = getProject();
    unSelectRecords(project, boundingBox);
    final LayerRendererOverlay overlay = getMap().getLayerOverlay();
    overlay.redraw();
  }

  private void unSelectRecords(final LayerGroup group, final BoundingBox boundingBox) {

    final double scale = getViewport().getScale();
    final List<Layer> layers = group.getLayers();
    Collections.reverse(layers);
    for (final Layer layer : layers) {
      if (layer instanceof LayerGroup) {
        final LayerGroup childGroup = (LayerGroup)layer;
        unSelectRecords(childGroup, boundingBox);
      } else if (layer instanceof AbstractRecordLayer) {
        final AbstractRecordLayer recordLayer = (AbstractRecordLayer)layer;
        if (recordLayer.isSelectable(scale)) {
          recordLayer.unSelectRecords(boundingBox);
        }
      }
    }
  }
}
