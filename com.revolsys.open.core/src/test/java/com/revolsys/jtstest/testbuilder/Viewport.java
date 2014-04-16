package com.revolsys.jtstest.testbuilder;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.text.NumberFormat;

import com.revolsys.jts.awt.PointTransformation;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.PrecisionModel;
import com.revolsys.jts.math.MathUtil;

/**
 * Maintains the information associated with mapping 
 * the model view to the screen
 * 
 * @author Martin Davis
 *
 */
public class Viewport implements PointTransformation {
  private static int INITIAL_VIEW_ORIGIN_X = -10;

  private static int INITIAL_VIEW_ORIGIN_Y = -10;

  private final GeometryEditPanel panel;

  /**
   * Origin of view in model space
   */
  private Point2D viewOriginInModel = new Point2D.Double(INITIAL_VIEW_ORIGIN_X,
    INITIAL_VIEW_ORIGIN_Y);

  /**
   * The scale is the factor which model distance 
   * is multiplied by to get view distance
   */
  private double scale = 1;

  private PrecisionModel scalePM = new PrecisionModel(scale);

  private NumberFormat scaleFormat;

  private BoundingBox viewEnvInModel;

  private AffineTransform modelToViewTransform;

  private final java.awt.geom.Point2D.Double srcPt = new java.awt.geom.Point2D.Double(
    0, 0);

  private final java.awt.geom.Point2D.Double destPt = new java.awt.geom.Point2D.Double(
    0, 0);

  private static final double ROUND_ERROR_REMOVAL = 0.00000001;

  private static final int MIN_GRID_RESOLUTION_PIXELS = 2;

  /**
     * Snaps scale to nearest multiple of 2, 5 or 10.
     * This ensures that model coordinates entered
     * via the geometry view
     * don't carry more precision than the zoom level warrants.
   * 
   * @param scaleRaw
   * @return
   */
  private static double snapScale(final double scaleRaw) {
    final double scale = snapScaleToSingleDigitPrecision(scaleRaw);
    // System.out.println("requested scale = " + scaleRaw + " scale = " + scale
    // + "   Pow10 = " + pow10);
    return scale;
  }

  /**
   * Not used - scaling to multiples of 10,5,2 is too coarse.
   *  
   * 
   * @param scaleRaw
   * @return
   */
  private static double snapScaleTo_10_2_5(final double scaleRaw) {
    // if the rounding error is not nudged, snapping can "stick" at some values
    final double pow10 = Math.floor(MathUtil.log10(scaleRaw)
      + ROUND_ERROR_REMOVAL);
    final double scaleRoundedToPow10 = Math.pow(10, pow10);

    double scale = scaleRoundedToPow10;
    // rounding to a power of 10 is too coarse, so allow some finer gradations
    // *
    if (3.5 * scaleRoundedToPow10 <= scaleRaw) {
      scale = 5 * scaleRoundedToPow10;
    } else if (2 * scaleRoundedToPow10 <= scaleRaw) {
      scale = 2 * scaleRoundedToPow10;
      // */
    }

    // System.out.println("requested scale = " + scaleRaw + " scale = " + scale
    // + "   Pow10 = " + pow10);
    return scale;
  }

  private static double snapScaleToSingleDigitPrecision(final double scaleRaw) {
    // if the rounding error is not nudged, snapping can "stick" at some values
    final double pow10 = Math.floor(MathUtil.log10(scaleRaw)
      + ROUND_ERROR_REMOVAL);
    final double nearestLowerPow10 = Math.pow(10, pow10);

    final int scaleDigit = (int)(scaleRaw / nearestLowerPow10);
    final double scale = scaleDigit * nearestLowerPow10;

    // System.out.println("requested scale = " + scaleRaw + " scale = " + scale
    // + "   Pow10 = " + pow10);
    return scale;
  }

  public Viewport(final GeometryEditPanel panel) {
    this.panel = panel;
    setScaleNoUpdate(1.0);
  }

  private BoundingBox computeEnvelopeInModel() {
    final double widthInModel = panel.getWidth() / scale;
    final double heighInModel = panel.getHeight() / scale;

    return new Envelope(viewOriginInModel.getX(), viewOriginInModel.getY(),
      viewOriginInModel.getX() + widthInModel, viewOriginInModel.getY()
        + heighInModel);
  }

  public boolean containsInModel(final Coordinates p) {
    return viewEnvInModel.contains(p);
  }

  /**
   * Gets a PrecisionModel corresponding to the grid size.
   * 
   * @return the precision model
   */
  public PrecisionModel getGridPrecisionModel() {
    final double gridSizeModel = getGridSizeModel();
    return new PrecisionModel(1.0 / gridSizeModel);
  }

  public double getGridSizeModel() {
    return Math.pow(10, gridMagnitudeModel());
  }

  public double getHeightInModel() {
    return getUpperRightCornerInModel().getY()
      - getLowerLeftCornerInModel().getY();
  }

  public double getHeightInView() {
    return panel.getSize().height;
  }

  public Point2D getLowerLeftCornerInModel() {
    final Dimension size = panel.getSize();
    return toModel(new Point(0, size.height));
  }

  public BoundingBox getModelEnv() {
    return viewEnvInModel;
  }

  public AffineTransform getModelToViewTransform() {
    if (modelToViewTransform == null) {
      updateModelToViewTransform();
    }
    return modelToViewTransform;
  }

  public double getScale() {
    return scale;
  }

  public NumberFormat getScaleFormat() {
    return scaleFormat;
  }

  public Point2D getUpperRightCornerInModel() {
    final Dimension size = panel.getSize();
    return toModel(new Point(size.width, 0));
  }

  public BoundingBox getViewEnv() {
    return new Envelope(0, 0, getWidthInView(), getHeightInView());
  }

  public double getViewOriginX() {
    return viewOriginInModel.getX();
  }

  public double getViewOriginY() {
    return viewOriginInModel.getY();
  }

  public double getWidthInModel() {
    return getUpperRightCornerInModel().getX()
      - getLowerLeftCornerInModel().getX();
  }

  public double getWidthInView() {
    return panel.getSize().getWidth();
  }

  /**
   * Gets the magnitude (power of 10)
   * for the basic grid size.
   * 
   * @return the magnitude
   */
  public int gridMagnitudeModel() {
    final double pixelSizeModel = toModel(1);
    final double pixelSizeModelLog = MathUtil.log10(pixelSizeModel);
    int gridMag = (int)Math.ceil(pixelSizeModelLog);

    /**
     * Check if grid size is too small and if so increase it one magnitude
     */
    final double gridSizeModel = Math.pow(10, gridMag);
    final double gridSizeView = toView(gridSizeModel);
    // System.out.println("\ncand gridSizeView= " + gridSizeView);
    if (gridSizeView <= MIN_GRID_RESOLUTION_PIXELS) {
      gridMag += 1;
    }

    // System.out.println("pixelSize= " + pixelSize + "  pixelLog10= " +
    // pixelSizeLog);
    return gridMag;
  }

  public boolean intersectsInModel(final BoundingBox env) {
    return viewEnvInModel.intersects(env);
  }

  public void setScale(final double scale) {
    setScaleNoUpdate(scale);
    update();
  }

  public void setScaleNoUpdate(final double scale) {
    this.scale = snapScale(scale);
    scalePM = new PrecisionModel(this.scale);

    scaleFormat = NumberFormat.getInstance();
    int fracDigits = (int)(MathUtil.log10(this.scale));
    if (fracDigits < 0) {
      fracDigits = 0;
    }
    // System.out.println("scale = " + this.scale);
    // System.out.println("fracdigits = " + fracDigits);
    scaleFormat.setMaximumFractionDigits(fracDigits);
    // don't show commas
    scaleFormat.setGroupingUsed(false);
  }

  public void setViewOrigin(final double viewOriginX, final double viewOriginY) {
    this.viewOriginInModel = new Point2D.Double(viewOriginX, viewOriginY);
    update();
  }

  /**
   * Converts a distance in the view to a distance in the model.
   * 
   * @param viewDist
   * @return the model distance
   */
  public double toModel(final double viewDist) {
    return viewDist / scale;
  }

  public Point2D toModel(final Point2D viewPt) {
    srcPt.x = viewPt.getX();
    srcPt.y = viewPt.getY();
    try {
      getModelToViewTransform().inverseTransform(srcPt, destPt);
    } catch (final NoninvertibleTransformException ex) {
      return new Point2D.Double(0, 0);
      // Assert.shouldNeverReachHere();
    }

    // snap to scale grid
    final double x = scalePM.makePrecise(destPt.x);
    final double y = scalePM.makePrecise(destPt.y);

    return new Point2D.Double(x, y);
  }

  public Coordinates toModelCoordinate(final Point2D viewPt) {
    final Point2D p = toModel(viewPt);
    return new Coordinate(p.getX(), p.getY());
  }

  public Point2D toView(final Coordinates modelCoordinate) {
    final Point2D.Double pt = new Point2D.Double();
    transform(modelCoordinate, pt);
    return pt;
  }

  /**
   * Converts a distance in the model to a distance in the view.
   * 
   * @param modelDist
   * @return the view distance
   */
  public double toView(final double modelDist) {
    return modelDist * scale;
  }

  public Point2D toView(final Point2D modelPt) {
    return toView(modelPt, new Point2D.Double());
  }

  public Point2D toView(final Point2D modelPt, final Point2D viewPt) {
    return getModelToViewTransform().transform(modelPt, viewPt);
  }

  @Override
  public void transform(final Coordinates modelCoordinate, final Point2D point) {
    point.setLocation(modelCoordinate.getX(), modelCoordinate.getY());
    getModelToViewTransform().transform(point, point);
  }

  public void update() {
    updateModelToViewTransform();
    viewEnvInModel = computeEnvelopeInModel();
    panel.forceRepaint();
  }

  private void updateModelToViewTransform() {
    modelToViewTransform = new AffineTransform();
    modelToViewTransform.translate(0, panel.getSize().height);
    modelToViewTransform.scale(1, -1);
    modelToViewTransform.scale(scale, scale);
    modelToViewTransform.translate(-viewOriginInModel.getX(),
      -viewOriginInModel.getY());
  }

  public void zoom(final BoundingBox zoomEnv) {
    zoomToInitialExtent();
    final double xScale = getWidthInModel() / zoomEnv.getWidth();
    final double yScale = getHeightInModel() / zoomEnv.getHeight();
    setScale(Math.min(xScale, yScale));
    final double xCentering = (getWidthInModel() - zoomEnv.getWidth()) / 2d;
    final double yCentering = (getHeightInModel() - zoomEnv.getHeight()) / 2d;
    setViewOrigin(zoomEnv.getMinX() - xCentering, zoomEnv.getMinY()
      - yCentering);
  }

  public void zoomToInitialExtent() {
    setScale(1);
    setViewOrigin(INITIAL_VIEW_ORIGIN_X, INITIAL_VIEW_ORIGIN_Y);
  }
}
