package com.revolsys.jtstest.testbuilder.ui.tools;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.PrecisionModel;
import com.revolsys.jtstest.testbuilder.AppConstants;
import com.revolsys.jtstest.testbuilder.GeometryEditPanel;
import com.revolsys.jtstest.testbuilder.JTSTestBuilder;
import com.revolsys.jtstest.testbuilder.JTSTestBuilderFrame;
import com.revolsys.jtstest.testbuilder.Viewport;
import com.revolsys.jtstest.testbuilder.model.GeometryEditModel;

public abstract class BasicTool implements Tool {
  protected Cursor cursor = Cursor.getDefaultCursor();

  private PrecisionModel gridPM;

  public BasicTool() {
    super();
  }

  /**
   * Called when tool is activated.
   * 
   * If subclasses override this method they must call <tt>super.activate()</tt>.
   */
  @Override
  public void activate() {
    gridPM = getViewport().getGridPrecisionModel();
  }

  // protected void gestureFinished() throws Exception;

  protected GeometryEditModel geomModel() {
    // this should probably be passed in during setup
    return JTSTestBuilder.model().getGeometryEditModel();
  }

  @Override
  public Cursor getCursor() {
    return cursor;
  }

  protected Graphics2D getGraphics2D() {
    final Graphics2D g = (Graphics2D)panel().getGraphics();
    if (g != null) {
      // guard against g == null
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    }
    return g;
  }

  double getModelSnapTolerance() {
    return toModel(AppConstants.TOLERANCE_PIXELS);
  }

  private Viewport getViewport() {
    return panel().getViewport();
  }

  protected double gridSize() {
    return getViewport().getGridSizeModel();
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
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
  public void mousePressed(final MouseEvent e) {
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
  }

  protected GeometryEditPanel panel() {
    // this should probably be passed in during setup
    return JTSTestBuilderFrame.instance()
      .getTestCasePanel()
      .getGeometryEditPanel();
  }

  double toModel(final double viewDist) {
    return viewDist / getViewport().getScale();
  }

  Point2D toModel(final java.awt.Point viewPt) {
    return getViewport().toModel(viewPt);
  }

  Coordinates toModelCoordinate(final java.awt.Point viewPt) {
    return getViewport().toModelCoordinate(viewPt);
  }

  protected Coordinates toModelSnapped(final Point2D p) {
    return toModelSnappedIfCloseToViewGrid(p);
  }

  protected Coordinates toModelSnappedIfCloseToViewGrid(final Point2D p) {
    // snap to view grid if close to view grid point
    final Coordinates pModel = getViewport().toModelCoordinate(p);
    final Coordinates pSnappedModel = new Coordinate(pModel);
    gridPM.makePrecise(pSnappedModel);

    final double tol = getModelSnapTolerance();
    if (pModel.distance(pSnappedModel) <= tol) {
      return pSnappedModel;
    }
    return pModel;
  }

  protected Coordinates toModelSnappedToViewGrid(final Point2D p) {
    // snap to view grid
    final Coordinates pModel = getViewport().toModelCoordinate(p);
    gridPM.makePrecise(pModel);
    return pModel;
  }

  Point2D toView(final Coordinates modePt) {
    return getViewport().toView(modePt);
  }

  double toView(final double distance) {
    return getViewport().toView(distance);
  }

  /*
   * protected Coordinates toModelSnappedToDrawingGrid(Point2D p) { Point2D pt =
   * panel().snapToGrid(getViewport().toModel(p)); return new
   * Coordinate(pt.getX(), pt.getY()); }
   */
}
