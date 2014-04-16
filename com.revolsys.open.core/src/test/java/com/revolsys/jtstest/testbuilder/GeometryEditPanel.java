/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.jtstest.testbuilder;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jtstest.testbuilder.model.DrawingGrid;
import com.revolsys.jtstest.testbuilder.model.GeometryDepiction;
import com.revolsys.jtstest.testbuilder.model.GeometryEditModel;
import com.revolsys.jtstest.testbuilder.model.GeometryStretcherView;
import com.revolsys.jtstest.testbuilder.model.LayerList;
import com.revolsys.jtstest.testbuilder.model.StaticGeometryContainer;
import com.revolsys.jtstest.testbuilder.model.TestBuilderModel;
import com.revolsys.jtstest.testbuilder.ui.ColorUtil;
import com.revolsys.jtstest.testbuilder.ui.GeometryLocationsWriter;
import com.revolsys.jtstest.testbuilder.ui.render.GeometryPainter;
import com.revolsys.jtstest.testbuilder.ui.render.GridRenderer;
import com.revolsys.jtstest.testbuilder.ui.render.LayerRenderer;
import com.revolsys.jtstest.testbuilder.ui.render.RenderManager;
import com.revolsys.jtstest.testbuilder.ui.render.Renderer;
import com.revolsys.jtstest.testbuilder.ui.style.AWTUtil;
import com.revolsys.jtstest.testbuilder.ui.tools.Tool;

/**
 * Panel which displays rendered geometries.
 * 
 * @version 1.7
 */
public class GeometryEditPanel extends JPanel {

  /*
   * private static Color[] selectedPointColor = { new Color(0, 64, 128, 255),
   * new Color(170, 64, 0, 255) };
   */

  class GeometryEditPanelRenderer implements Renderer {
    private GeometryStretcherView stretchView = null;

    private Renderer currentRenderer = null;

    private boolean isMagnifyingTopology = false;

    private boolean isRenderingStretchVertices = false;

    public GeometryEditPanelRenderer() {
      if (tbModel.isMagnifyingTopology()) {
        stretchView = new GeometryStretcherView(getGeomModel());
        stretchView.setStretchSize(viewport.toModel(tbModel.getTopologyStretchSize()));
        stretchView.setNearnessTolerance(viewport.toModel(GeometryStretcherView.NEARNESS_TOL_IN_VIEW));
        stretchView.setEnvelope(viewport.getModelEnv());
        isMagnifyingTopology = tbModel.isMagnifyingTopology();
        isRenderingStretchVertices = stretchView.isViewPerformant();
      }
    }

    @Override
    public synchronized void cancel() {
      if (currentRenderer != null) {
        currentRenderer.cancel();
      }
    }

    @Override
    public void render(final Graphics2D g) {
      final Graphics2D g2 = g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);

      if (isMagnifyingTopology) {
        if (isRenderingStretchVertices) {
          // renderMagnifiedVertexShadows(g2);
          renderMagnifiedVertexMask(g2);
        } else {
          // render indicator that shows stretched view is non-performant
          renderMagnifyWarning(g2);
        }
      }

      gridRenderer.paint(g2);

      renderLayers(g2);

      if (isMagnifyingTopology && isRenderingStretchVertices) {
        renderMagnifiedVertices(g2);
      }

      drawMark(g2);

    }

    public void renderLayers(final Graphics2D g) {
      final LayerList layerList = getLayerList();
      final int n = layerList.size();
      for (int i = 0; i < n; i++) {
        if (isMagnifyingTopology && isRenderingStretchVertices
          && stretchView != null && i < 2) {
          // System.out.println("rendering stretch verts");
          currentRenderer = new LayerRenderer(layerList.getLayer(i),
            new StaticGeometryContainer(stretchView.getStretchedGeometry(i)),
            viewport);
        } else {
          currentRenderer = new LayerRenderer(layerList.getLayer(i), viewport);
        }
        currentRenderer.render(g);
      }
      currentRenderer = null;
    }

    public void renderMagnifiedVertexMask(final Graphics2D g) {
      if (stretchView == null) {
        return;
      }

      // render lowlight background
      final Rectangle2D rect = new Rectangle2D.Float();
      rect.setFrame(0, 0, viewport.getWidthInView(), viewport.getHeightInView());
      g.setColor(AppConstants.MASK_CLR);
      g.fill(rect);

      // highlight mag vertices
      for (int i = 0; i < 2; i++) {
        final List<Coordinates> stretchedVerts = stretchView.getStretchedVertices(i);
        if (stretchedVerts == null) {
          continue;
        }
        for (int j = 0; j < stretchedVerts.size(); j++) {
          final Coordinates p = stretchedVerts.get(j);
          drawVertexShadow(g, p, Color.WHITE);
        }
      }
    }

    public void renderMagnifiedVertexShadows(final Graphics2D g) {
      if (stretchView == null) {
        return;
      }
      for (int i = 0; i < 2; i++) {
        final List<Coordinates> stretchedVerts = stretchView.getStretchedVertices(i);
        if (stretchedVerts == null) {
          continue;
        }
        for (int j = 0; j < stretchedVerts.size(); j++) {
          final Coordinates p = stretchedVerts.get(j);
          drawVertexShadow(g, p, AppConstants.VERTEX_SHADOW_CLR);
        }
      }
    }

    public void renderMagnifiedVertices(final Graphics2D g) {
      final LayerList layerList = getLayerList();
      for (int i = 0; i < 2; i++) {
        // respect layer visibility
        if (!layerList.getLayer(i).isEnabled()) {
          continue;
        }

        final List<Coordinates> stretchedVerts = stretchView.getStretchedVertices(i);
        if (stretchedVerts == null) {
          continue;
        }
        for (int j = 0; j < stretchedVerts.size(); j++) {
          final Coordinates p = stretchedVerts.get(j);
          drawHighlightedVertex(g, p,
            i == 0 ? GeometryDepiction.GEOM_A_HIGHLIGHT_CLR
              : GeometryDepiction.GEOM_B_HIGHLIGHT_CLR);
        }
      }
    }

    public void renderMagnifyWarning(final Graphics2D g) {
      if (stretchView == null) {
        return;
      }

      final float maxx = (float)viewport.getWidthInView();
      final float maxy = (float)viewport.getHeightInView();
      final GeneralPath path = new GeneralPath();
      path.moveTo(0, 0);
      path.lineTo(maxx, maxy);
      path.moveTo(0, maxy);
      path.lineTo(maxx, 0);
      // render lowlight background
      g.setColor(AppConstants.MASK_CLR);
      g.setStroke(new BasicStroke(30));
      g.draw(path);

    }

  }

  class PopupClickListener extends MouseAdapter {
    private void doPopUp(final MouseEvent e) {
      menu.show(e.getComponent(), e.getX(), e.getY());
    }

    @Override
    public void mousePressed(final MouseEvent e) {
      if (e.isPopupTrigger()) {
        doPopUp(e);
      }
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
      if (e.isPopupTrigger()) {
        doPopUp(e);
      }
    }
  }

  private TestBuilderModel tbModel;

  private final DrawingGrid grid = new DrawingGrid();

  private final GridRenderer gridRenderer;

  boolean stateAddingPoints = false;

  Coordinates markPoint;

  Point2D lastPt = new Point2D.Double();

  private Tool currentTool = null; // PolygonTool.getInstance();

  private final Viewport viewport = new Viewport(this);

  private final RenderManager renderMgr;

  // private OperationMonitorManager opMonitor;

  // ----------------------------------------
  BorderLayout borderLayout1 = new BorderLayout();

  GeometryPopupMenu menu = new GeometryPopupMenu();

  private static int VERTEX_SIZE = AppConstants.VERTEX_SIZE + 1;

  private static double VERTEX_SIZE_OVER_2 = VERTEX_SIZE / 2;

  private static int INNER_SIZE = VERTEX_SIZE - 2;

  private static double INNER_SIZE_OVER_2 = INNER_SIZE / 2;

  private static double VERTEX_SHADOW_SIZE_OVER_2 = AppConstants.VERTEX_SHADOW_SIZE / 2;

  public GeometryEditPanel() {
    gridRenderer = new GridRenderer(viewport, grid);
    try {
      initUI();
    } catch (final Exception ex) {
      ex.printStackTrace();
    }
    renderMgr = new RenderManager(this);
    // opMonitor = new OperationMonitorManager(this, viewport);
  }

  public String cursorLocationString(final Point2D pView) {
    final Point2D p = getViewport().toModel(pView);
    final NumberFormat format = getViewport().getScaleFormat();
    return format.format(p.getX()) + ", " + format.format(p.getY());

    /*
     * double width = getViewport().getWidthInModel(); double height =
     * getViewport().getHeightInModel(); double extent = Math.min(width,
     * height); double precisionDigits =
     * -Math.floor(Math.log(extent)/Math.log(10.0)) + 3; double precisionScale =
     * Math.pow(10.0, precisionDigits); double xRound = Math.round(p.getX() *
     * precisionScale) / precisionScale; double yRound = Math.round(p.getY() *
     * precisionScale) / precisionScale; // System.out.println(precisionScale);
     * //return xRound + ", " + yRound;
     */
  }

  private void drawHighlightedVertex(final Graphics2D g, final Coordinates pt,
    final Color clr) {
    final Rectangle2D rect = new Rectangle2D.Double();
    final Point2D p = viewport.toView(pt);
    rect.setFrame(p.getX() - VERTEX_SIZE_OVER_2, p.getY() - VERTEX_SIZE_OVER_2,
      VERTEX_SIZE, VERTEX_SIZE);
    g.setColor(clr);
    g.fill(rect);
    final Rectangle2D rectInner = new Rectangle2D.Double(p.getX()
      - INNER_SIZE_OVER_2, p.getY() - INNER_SIZE_OVER_2, INNER_SIZE, INNER_SIZE);
    g.setColor(AppConstants.VERTEX_HIGHLIGHT_CLR);
    g.fill(rectInner);
  }

  private void drawHighlightedVertices(final Graphics2D g,
    final List<Coordinates> coords, final Color clr) {
    final Rectangle2D rect = new Rectangle2D.Double();
    for (int i = 0; i < coords.size(); i++) {
      final Coordinates pt = coords.get(i);
      final Point2D p = viewport.toView(pt);
      rect.setFrame(p.getX() - VERTEX_SIZE_OVER_2, p.getY()
        - VERTEX_SIZE_OVER_2, VERTEX_SIZE, VERTEX_SIZE);
      g.setColor(clr);
      g.fill(rect);
      final Rectangle2D rectInner = new Rectangle2D.Double(p.getX()
        - INNER_SIZE_OVER_2, p.getY() - INNER_SIZE_OVER_2, INNER_SIZE,
        INNER_SIZE);
      g.setColor(AppConstants.VERTEX_HIGHLIGHT_CLR);
      g.fill(rectInner);

    }
  }

  /**
   * Draws a mask surround to indicate that geometry is being visually altered
   * @param g
   */
  private void drawMagnifyMask(final Graphics2D g) {
    final double viewWidth = viewport.getWidthInView();
    final double viewHeight = viewport.getHeightInView();

    final float minExtent = (float)Math.min(viewWidth, viewHeight);
    final float maskWidth = (float)(minExtent * AppConstants.MASK_WIDTH_FRAC / 2);

    final Area mask = new Area(new Rectangle2D.Float(0, 0, (float)viewWidth,
      (float)viewHeight));

    final Area maskHole = new Area(new Rectangle2D.Float(maskWidth, maskWidth,
      ((float)viewWidth) - 2 * maskWidth, ((float)viewHeight) - 2 * maskWidth));

    mask.subtract(maskHole);
    g.setColor(AppConstants.MASK_CLR);
    g.fill(mask);
  }

  private void drawMark(final Graphics2D g) {
    if (markPoint == null) {
      return;
    }

    final String markLabel = markPoint.getX() + ",  " + markPoint.getY();
    final int strWidth = g.getFontMetrics().stringWidth(markLabel);

    final double markSize = AppConstants.HIGHLIGHT_SIZE;
    final Point2D highlightPointView = viewport.toView(markPoint);
    final double markX = highlightPointView.getX();
    final double markY = highlightPointView.getY();
    final Ellipse2D.Double shape = new Ellipse2D.Double(markX - markSize / 2,
      markY - markSize / 2, markSize, markSize);
    AWTUtil.setStroke(g, 4);
    g.setColor(AppConstants.HIGHLIGHT_CLR);
    g.draw(shape);

    // draw label box
    final BoundingBox viewEnv = viewport.getViewEnv();

    final int bottomOffset = 10;
    final int boxHgt = 20;
    final int boxPadX = 20;
    final int boxWidth = strWidth + 2 * boxPadX;
    final int arrowWidth = 10;
    final int arrowOffset = 2;
    final int labelOffsetY = 5;

    final int bottom = (int)viewEnv.getMaxY() - bottomOffset;
    final int centreX = (int)(viewEnv.getMinX() + viewEnv.getMaxX()) / 2;

    final int boxMinX = centreX - boxWidth / 2;
    final int boxMaxX = centreX + boxWidth / 2;
    final int boxMinY = bottom - boxHgt;
    final int boxMaxY = bottom;

    final int[] xpts = new int[] {
      boxMinX, centreX - arrowWidth / 2, (int)markX, centreX + arrowWidth / 2,
      boxMaxX, boxMaxX, boxMinX
    };
    final int[] ypts = new int[] {
      boxMinY, boxMinY, (int)(markY + arrowOffset), boxMinY, boxMinY, boxMaxY,
      boxMaxY
    };

    final Polygon poly = new Polygon(xpts, ypts, xpts.length);

    g.setColor(AppConstants.HIGHLIGHT_FILL_CLR);
    g.fill(poly);
    AWTUtil.setStroke(g, 1);
    g.setColor(ColorUtil.opaque(AppConstants.HIGHLIGHT_CLR));
    g.draw(poly);

    // draw mark point label
    g.setColor(Color.BLACK);
    g.drawString(markLabel, centreX - strWidth / 2, boxMaxY - labelOffsetY);

  }

  private void drawVertexShadow(final Graphics2D g, final Coordinates pt,
    final Color clr) {
    final Ellipse2D rect = new Ellipse2D.Double();
    final Point2D p = viewport.toView(pt);
    rect.setFrame(p.getX() - VERTEX_SHADOW_SIZE_OVER_2, p.getY()
      - VERTEX_SHADOW_SIZE_OVER_2, AppConstants.VERTEX_SHADOW_SIZE,
      AppConstants.VERTEX_SHADOW_SIZE);
    g.setColor(clr);
    g.fill(rect);
  }

  public void flash(final Geometry g) {
    final Graphics2D gr = (Graphics2D)getGraphics();
    gr.setXORMode(Color.white);
    final Stroke stroke = new BasicStroke(5);

    Geometry flashGeom = g;
    if (g instanceof com.revolsys.jts.geom.Point) {
      flashGeom = flashPointGeom(g);
    }

    try {
      GeometryPainter.paint(flashGeom, viewport, gr, Color.RED, null, stroke);
      Thread.sleep(200);
      GeometryPainter.paint(flashGeom, viewport, gr, Color.RED, null, stroke);
    } catch (final Exception ex) {
      // nothing we can do
    }
    gr.setPaintMode();
  }

  private Geometry flashPointGeom(final Geometry g) {
    final double ptRadius = viewport.toModel(4);
    return g.buffer(ptRadius);
  }

  public void forceRepaint() {
    renderMgr.setDirty(true);

    Component source = SwingUtilities.windowForComponent(this);
    if (source == null) {
      source = this;
    }
    source.repaint();
  }

  public GeometryEditModel getGeomModel() {
    return tbModel.getGeometryEditModel();
  }

  public double getGridSize() {
    return grid.getGridSize();
  }

  public String getInfo(final Coordinates pt) {
    final double toleranceInModel = AppConstants.TOLERANCE_PIXELS
      / getViewport().getScale();
    final GeometryLocationsWriter writer = new GeometryLocationsWriter();
    writer.setHtml(false);
    return writer.writeLocationString(getLayerList(), pt, toleranceInModel);
  }

  private LayerList getLayerList() {
    return tbModel.getLayers();
  }

  public TestBuilderModel getModel() {
    return tbModel;
  }

  public Renderer getRenderer() {
    return new GeometryEditPanelRenderer();
  }

  public double getToleranceInModel() {
    return AppConstants.TOLERANCE_PIXELS / getViewport().getScale();
  }

  /*
   * // MD - obsolete public void render(Graphics g) { Graphics2D g2 =
   * (Graphics2D) g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
   * RenderingHints.VALUE_ANTIALIAS_ON); gridRenderer.paint(g2);
   * getLayerList().paint((Graphics2D) g2, viewport); }
   */

  @Override
  public String getToolTipText(final MouseEvent event) {
    // if (event.getPoint().getX() < 100) return null;
    final Coordinates pt = viewport.toModelCoordinate(event.getPoint());
    final double toleranceInModel = AppConstants.TOLERANCE_PIXELS
      / getViewport().getScale();
    // avoid wierd scale issues
    if (toleranceInModel <= 0.0) {
      return null;
    }
    return GeometryLocationsWriter.writeLocation(getLayerList(), pt,
      toleranceInModel);
    // return viewport.toModel(event.getPoint()).toString();
    // return null;
  }

  public Viewport getViewport() {
    return viewport;
  }

  void initUI() throws Exception {
    this.addComponentListener(new java.awt.event.ComponentAdapter() {

      @Override
      public void componentResized(final ComponentEvent e) {
        this_componentResized(e);
      }
    });
    this.setBackground(Color.white);
    this.setBorder(BorderFactory.createLoweredBevelBorder());
    this.setLayout(borderLayout1);

    setToolTipText("");
    setBorder(BorderFactory.createEmptyBorder());

    // deactivate for now, since it interferes with right-click zoom-out
    // addMouseListener(new PopupClickListener());
  }

  public boolean isAddingPoints() {
    return stateAddingPoints;
  }

  @Override
  public void paintComponent(final Graphics g) {
    super.paintComponent(g);
    renderMgr.render();
    renderMgr.copyImage(g);
  }

  /**
   * 
   * @param newTool tool to set, or null to clear tool
   */
  public void setCurrentTool(final Tool newTool) {
    removeMouseListener(currentTool);
    removeMouseMotionListener(currentTool);
    currentTool = newTool;
    // tool cleared
    if (newTool == null) {
      return;
    }

    currentTool.activate();
    setCursor(currentTool.getCursor());
    addMouseListener(currentTool);
    addMouseMotionListener(currentTool);
  }

  public void setGridEnabled(final boolean isEnabled) {
    gridRenderer.setEnabled(isEnabled);
  }

  public void setGridSize(final double gridSize) {
    grid.setGridSize(gridSize);
    forceRepaint();
  }

  public void setHighlightPoint(final Coordinates pt) {
    markPoint = pt;
  }

  public void setModel(final TestBuilderModel model) {
    this.tbModel = model;
  }

  public void setShowingGeometryA(final boolean isEnabled) {
    if (tbModel == null) {
      return;
    }
    getLayerList().getLayer(LayerList.LYR_A).setEnabled(isEnabled);
    forceRepaint();
  }

  public void setShowingGeometryB(final boolean isEnabled) {
    if (tbModel == null) {
      return;
    }
    getLayerList().getLayer(LayerList.LYR_B).setEnabled(isEnabled);
    forceRepaint();
  }

  public void setShowingInput(final boolean isEnabled) {
    if (tbModel == null) {
      return;
    }
    getLayerList().getLayer(LayerList.LYR_A).setEnabled(isEnabled);
    getLayerList().getLayer(LayerList.LYR_B).setEnabled(isEnabled);
    forceRepaint();
  }

  public void setShowingResult(final boolean isEnabled) {
    if (tbModel == null) {
      return;
    }
    getLayerList().getLayer(LayerList.LYR_RESULT).setEnabled(isEnabled);
    forceRepaint();
  }

  public Point2D snapToGrid(final Point2D modelPoint) {
    return grid.snapToGrid(modelPoint);
  }

  void this_componentResized(final ComponentEvent e) {
    renderMgr.componentResized();
    viewport.update();
  }

  public void updateGeom() {
    renderMgr.setDirty(true);
    getGeomModel().geomChanged();
  }

  public void updateView() {
    // fireGeometryChanged(new GeometryEvent(this));
    forceRepaint();
  }

  public void zoom(BoundingBox zoomEnv) {
    if (zoomEnv == null) {
      return;
    }

    renderMgr.setDirty(true);

    if (zoomEnv.isNull()) {
      viewport.zoomToInitialExtent();
      return;
    }

    double averageExtent = (zoomEnv.getWidth() + zoomEnv.getHeight()) / 2d;
    // fix to allow zooming to points
    if (averageExtent == 0.0) {
      averageExtent = 1.0;
    }
    final double buffer = averageExtent * 0.03;

    zoomEnv = zoomEnv.expand(zoomEnv.getMaxX() + buffer, zoomEnv.getMaxY()
      + buffer);
    zoomEnv = zoomEnv.expand(zoomEnv.getMinX() - buffer, zoomEnv.getMinY()
      - buffer);
    viewport.zoom(zoomEnv);
  }

  public void zoom(final Geometry geom) {
    if (geom == null) {
      return;
    }
    zoom(geom.getBoundingBox());
  }

  public void zoom(final Point center, final double realZoomFactor) {

    renderMgr.setDirty(true);

    final double width = getSize().width / realZoomFactor;
    final double height = getSize().height / realZoomFactor;
    final double bottomOfNewViewAsPerceivedByOldView = center.getY()
      + (height / 2d);
    final double leftOfNewViewAsPerceivedByOldView = center.getX()
      - (width / 2d);
    final Point bottomLeftOfNewViewAsPerceivedByOldView = new Point(
      (int)leftOfNewViewAsPerceivedByOldView,
      (int)bottomOfNewViewAsPerceivedByOldView);
    final Point2D bottomLeftOfNewViewAsPerceivedByModel = viewport.toModel(bottomLeftOfNewViewAsPerceivedByOldView);
    viewport.setScale(getViewport().getScale() * realZoomFactor);
    viewport.setViewOrigin(bottomLeftOfNewViewAsPerceivedByModel.getX(),
      bottomLeftOfNewViewAsPerceivedByModel.getY());
  }

  public void zoomPan(final double xDisplacement, final double yDisplacement) {
    renderMgr.setDirty(true);
    getViewport().setViewOrigin(getViewport().getViewOriginX() - xDisplacement,
      getViewport().getViewOriginY() - yDisplacement);
  }

  public void zoomToFullExtent() {
    zoom(getGeomModel().getEnvelopeAll());
  }

  public void zoomToGeometry(final int i) {
    final Geometry g = getGeomModel().getGeometry(i);
    if (g == null) {
      return;
    }
    zoom(g.getBoundingBox());
  }

  public void zoomToInput() {
    zoom(getGeomModel().getEnvelope());
  }

  public void zoomToResult() {
    zoom(getGeomModel().getEnvelopeResult());
  }
}
