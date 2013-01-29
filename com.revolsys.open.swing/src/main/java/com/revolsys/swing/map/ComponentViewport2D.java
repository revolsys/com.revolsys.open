package com.revolsys.swing.map;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.measure.Measurable;
import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.swing.listener.InvokeMethodPropertyChangeListener;
import com.revolsys.swing.map.layer.Project;

public class ComponentViewport2D extends Viewport2D {

  private final Component component;

  private int maxIntegerDigits;

  private int maxDecimalDigits;

  public ComponentViewport2D(final Project project, final Component component) {
    super(project);
    this.component = component;
    project.getPropertyChangeSupport().addPropertyChangeListener(
      new InvokeMethodPropertyChangeListener(this, "updateCachedFields"));
    updateCachedFields();
    component.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(final ComponentEvent e) {
        updateCachedFields();
      }
    });
  }

  /**
   * Get the bounding box for the dimensions of the viewport at the specified
   * scale, centred at the x, y model coordinates.
   * 
   * @param x The model x coordinate.
   * @param y The model y coordinate.
   * @param scale The scale.
   * @return The bounding box.
   */
  public BoundingBox getBoundingBox(final double x, final double y,
    final double scale) {
    final double width = getModelWidth(scale);
    final double height = getModelHeight(scale);

    final double x1 = x - width / 2;
    final double y1 = y - height / 2;
    final double x2 = x1 + width;
    final double y2 = y1 + height;
    final BoundingBox boundingBox = new BoundingBox(getGeometryFactory(), x1,
      y1, x2, y2);
    return boundingBox;
  }

  /**
   * Get the bounding box in model units for the pair of coordinates in view
   * units. The bounding box will be clipped to the model's bounding box.
   * 
   * @param x1 The first x value.
   * @param y1 The first y value.
   * @param x2 The second x value.
   * @param y2 The second y value.
   * @return The bounding box.
   */
  public BoundingBox getBoundingBox(final double x1, final double y1,
    final double x2, final double y2) {
    final double[] c1 = toModelCoordinates(x1, y1);
    final double[] c2 = toModelCoordinates(x2, y2);
    final BoundingBox boundingBox = new BoundingBox(getGeometryFactory(),
      c1[0], c1[1], c2[0], c2[1]);

    // Clip the bounding box with the map's visible area
    BoundingBox intersection = boundingBox.intersection(boundingBox);
    // If the clipped bounding box is null then move the map to the new BBOX
    if (intersection.isNull()) {
      intersection = boundingBox;
    }
    return intersection;
  }

  /**
   * Get the bounding box for the dimensions of the viewport at the specified
   * scale, centred at the x, y view coordinates.
   * 
   * @param x The view x coordinate.
   * @param y The view y coordinate.
   * @param scale The scale.
   * @return The bounding box.
   */
  public BoundingBox getBoundingBox(final int x, final int y, final double scale) {
    final double[] ordinates = toModelCoordinates(x, y);
    final double mapX = ordinates[0];
    final double mapY = ordinates[1];
    return getBoundingBox(mapX, mapY, scale);
  }

  public double getMaxScale() {
    final BoundingBox areaBoundingBox = getGeometryFactory().getCoordinateSystem()
      .getAreaBoundingBox();
    final Measurable<Length> areaWidth = areaBoundingBox.getWidthLength();
    final Measurable<Length> areaHeight = areaBoundingBox.getHeightLength();

    final Measurable<Length> viewWidth = getViewWidthLength();
    final Measurable<Length> viewHeight = getViewHeightLength();

    final double maxHorizontalScale = getScale(viewWidth, areaWidth);
    final double maxVerticalScale = getScale(viewHeight, areaHeight);
    final double maxScale = Math.max(maxHorizontalScale, maxVerticalScale);
    return maxScale;
  }

  public double getModelHeight(final double scale) {
    final Unit<Length> scaleUnit = getScaleUnit(scale);

    final Measurable<Length> viewHeight = getViewHeightLength();
    final double height = viewHeight.doubleValue(scaleUnit);
    return height;
  }

  public <Q extends Quantity> Unit<Q> getModelToScreenUnit(
    final Unit<Q> modelUnit) {
    final double viewWidth = getViewWidthPixels();
    final double modelWidth = getModelWidth();
    return modelUnit.times(viewWidth).divide(modelWidth);
  }

  public double getModelWidth(final double scale) {
    final Unit<Length> scaleUnit = getScaleUnit(scale);

    final Measurable<Length> viewWidth = getViewWidthLength();
    final double width = viewWidth.doubleValue(scaleUnit);
    return width;
  }

  /**
   * Get the rectangle in view units for the pair of coordinates in view units.
   * The bounding box will be clipped to the view's dimensions.
   * 
   * @param x1 The first x value.
   * @param y1 The first y value.
   * @param x2 The second x value.
   * @param y2 The second y value.
   * @return The rectangle.
   */
  public Rectangle getRectangle(final int x1, final int y1, final int x2,
    final int y2) {
    final int x3 = (int)Math.min(getViewWidthPixels() - 1, Math.max(0, x2));
    final int y3 = (int)Math.min(getViewHeightPixels() - 1, Math.max(0, y2));

    final int x = Math.min(x1, x3);
    final int y = Math.min(y1, y3);
    final int width = Math.abs(x1 - x3);
    final int height = Math.abs(y1 - y3);
    return new Rectangle(x, y, width, height);
  }

  public Unit<Length> getScaleUnit(final double scale) {
    final Unit<Length> lengthUnit = getGeometryFactory().getCoordinateSystem()
      .getLengthUnit();
    final Unit<Length> scaleUnit = lengthUnit.divide(scale);
    return scaleUnit;
  }

  public <Q extends Quantity> Unit<Q> getScreenToModelUnit(
    final Unit<Q> modelUnit) {
    final double viewWidth = getViewWidthPixels();
    final double modelWidth = getModelWidth();
    return modelUnit.times(modelWidth).divide(viewWidth);
  }


  public BoundingBox getValidBoundingBox(final BoundingBox boundingBox) {
    BoundingBox validBoundingBox = boundingBox;
    final double viewAspectRatio = getViewAspectRatio();
    double modelWidth = validBoundingBox.getWidth();
    double modelHeight = validBoundingBox.getHeight();

    /*
     * If the new bounding box has a zero width and height, expand it by 50 view
     * units.
     */
    if (modelWidth == 0 && modelHeight == 0) {
      validBoundingBox = new BoundingBox(validBoundingBox);
      validBoundingBox.expandBy(getModelUnitsPerViewUnit() * 50,
        getModelUnitsPerViewUnit() * 50);
      modelWidth = validBoundingBox.getWidth();
      modelHeight = validBoundingBox.getHeight();
    }
    final double newModelAspectRatio = modelWidth / modelHeight;
    double modelUnitsPerViewUnit;
    if (viewAspectRatio <= newModelAspectRatio) {
      modelUnitsPerViewUnit = modelWidth / getViewWidthPixels();
    } else {
      modelUnitsPerViewUnit = modelHeight / getViewHeightPixels();
    }
    final double logUnits = Math.log10(Math.abs(modelUnitsPerViewUnit));
    if (logUnits < 0 && Math.abs(Math.floor(logUnits)) > maxDecimalDigits) {
      validBoundingBox = new BoundingBox(validBoundingBox);
      modelUnitsPerViewUnit = 2 * Math.pow(10, -maxDecimalDigits);
      final double minModelWidth = getViewWidthPixels() * modelUnitsPerViewUnit;
      final double minModelHeight = getViewHeightPixels()
        * modelUnitsPerViewUnit;
      validBoundingBox.expandBy((minModelWidth - modelWidth) / 2,
        (minModelHeight - modelHeight) / 2);
    }
    return validBoundingBox;
  }

  @Override
  public int getViewHeightPixels() {
    return component.getHeight();
  }

  @Override
  public int getViewWidthPixels() {
    return component.getWidth();
  }

  public void repaint() {
    component.repaint();
  }

  /**
   * Set the coordinate system the map is displayed in.
   * 
   * @param coordinateSystem The coordinate system the map is displayed in.
   */
  @Override
  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    if (geometryFactory != getGeometryFactory()) {
      final GeometryFactory oldGeometryFactory = getGeometryFactory();
      super.setGeometryFactory(geometryFactory);
      final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
      final BoundingBox areaBoundingBox = coordinateSystem.getAreaBoundingBox();
      final double minX = areaBoundingBox.getMinX();
      final double maxX = areaBoundingBox.getMaxX();
      final double minY = areaBoundingBox.getMinY();
      final double maxY = areaBoundingBox.getMaxY();
      final double logMinX = Math.log10(Math.abs(minX));
      final double logMinY = Math.log10(Math.abs(minY));
      final double logMaxX = Math.log10(Math.abs(maxX));
      final double logMaxY = Math.log10(Math.abs(maxY));
      final double maxLog = Math.abs(Math.max(Math.max(logMinX, logMinY),
        Math.max(logMaxX, logMaxY)));
      maxIntegerDigits = (int)Math.floor(maxLog + 1);
      maxDecimalDigits = 15 - maxIntegerDigits;
      getPropertyChangeSupport().firePropertyChange("geometryFactory",
        oldGeometryFactory, geometryFactory);
      BoundingBox boundingBox = getBoundingBox();
      if (boundingBox != null) {
        final BoundingBox newBoundingBox = boundingBox.convert(geometryFactory);
        final BoundingBox intersection = newBoundingBox.intersection(areaBoundingBox);
        if (intersection.isNull()) {
          setBoundingBox(areaBoundingBox);
        } else {
          setBoundingBox(intersection);
        }
      }
    }
  }

  public void translate(final double dx, final double dy) {
    final BoundingBox boundingBox = getBoundingBox();
    final BoundingBox newBoundingBox = new BoundingBox(
      boundingBox.getGeometryFactory(), boundingBox.getMinX() + dx,
      boundingBox.getMinY() + dy, boundingBox.getMaxX() + dx,
      boundingBox.getMaxY() + dy);
    setBoundingBox(newBoundingBox);

  }

  public void updateCachedFields() {
    final Project project = getProject();
    final GeometryFactory geometryFactory = project.getGeometryFactory();
    if (geometryFactory != null) {
      if (geometryFactory != getGeometryFactory()) {
        setGeometryFactory(geometryFactory);

      }
      BoundingBox boundingBox = getBoundingBox();
      if (boundingBox != null && getViewAspectRatio() != boundingBox.getAspectRatio()) {
        setBoundingBox(this.getBoundingBox());
      }
      component.repaint();
    }
  }
}
