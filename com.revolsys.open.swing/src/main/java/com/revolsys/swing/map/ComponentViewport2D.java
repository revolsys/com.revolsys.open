package com.revolsys.swing.map;

import java.awt.Cursor;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Length;
import javax.swing.JComponent;

import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.BaseCloseable;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.view.graphics.Graphics2DViewRender;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.Property;
import com.revolsys.util.QuantityType;
import com.revolsys.value.GlobalBooleanValue;

public class ComponentViewport2D extends Viewport2D implements PropertyChangeListener {

  private final JComponent component;

  private int maxDecimalDigits;

  private int maxIntegerDigits;

  private final GlobalBooleanValue componentResizing = new GlobalBooleanValue(false);

  public ComponentViewport2D(final Project project, final JComponent component) {
    super(project);
    this.component = component;
    Property.addListener(project, "geometryFactory", this);
    Property.addListener(project, "viewBoundingBox", this);
    component.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(final ComponentEvent e) {
        if (isInitialized()) {
          try (
            BaseCloseable componentResizing = ComponentViewport2D.this.componentResizing
              .closeable(true)) {
            updateCachedFields();
          }
        }
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
  public BoundingBox getBoundingBox(final double x, final double y, final double scale) {
    final double width = getModelWidth(scale);
    final double height = getModelHeight(scale);

    final double x1 = x - width / 2;
    final double y1 = y - height / 2;
    final double x2 = x1 + width;
    final double y2 = y1 + height;
    final BoundingBox boundingBox = getGeometryFactory().newBoundingBox(x1, y1, x2, y2);
    return boundingBox;
  }

  public Cursor getCursor() {
    if (this.component == null) {
      return null;
    } else {
      return this.component.getCursor();
    }
  }

  public double getMaxScale() {
    final BoundingBox areaBoundingBox = getGeometryFactory().getAreaBoundingBox();
    final Quantity<Length> areaWidth = areaBoundingBox.getWidthLength();
    final Quantity<Length> areaHeight = areaBoundingBox.getHeightLength();

    final Quantity<Length> viewWidth = getViewWidthLength();
    final Quantity<Length> viewHeight = getViewHeightLength();

    final double maxHorizontalScale = getScale(viewWidth, areaWidth);
    final double maxVerticalScale = getScale(viewHeight, areaHeight);
    final double maxScale = Math.max(maxHorizontalScale, maxVerticalScale);
    return maxScale;
  }

  public double getModelHeight(final double scale) {
    final Unit<Length> scaleUnit = getScaleUnit(scale);

    final Quantity<Length> viewHeight = getViewHeightLength();
    final double height = QuantityType.doubleValue(viewHeight, scaleUnit);
    return height;
  }

  public <Q extends Quantity<Q>> Unit<Q> getModelToScreenUnit(final Unit<Q> modelUnit) {
    final double viewWidth = getViewWidthPixels();
    final double modelWidth = getModelWidth();
    return modelUnit.multiply(viewWidth).divide(modelWidth);
  }

  public double getModelWidth(final double scale) {
    final Unit<Length> scaleUnit = getScaleUnit(scale);

    final Quantity<Length> viewWidth = getViewWidthLength();
    final double width = QuantityType.doubleValue(viewWidth, scaleUnit);
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
  public Rectangle getRectangle(final int x1, final int y1, final int x2, final int y2) {
    final int x3 = Math.min(getViewWidthPixels() - 1, Math.max(0, x2));
    final int y3 = Math.min(getViewHeightPixels() - 1, Math.max(0, y2));

    final int x = Math.min(x1, x3);
    final int y = Math.min(y1, y3);
    final int width = Math.abs(x1 - x3);
    final int height = Math.abs(y1 - y3);
    return new Rectangle(x, y, width, height);
  }

  public Unit<Length> getScaleUnit(final double scale) {
    final Unit<Length> lengthUnit = getGeometryFactory().getHorizontalCoordinateSystem()
      .getLengthUnit();
    final Unit<Length> scaleUnit = lengthUnit.divide(scale);
    return scaleUnit;
  }

  public <Q extends Quantity<Q>> Unit<Q> getScreenToModelUnit(final Unit<Q> modelUnit) {
    final double viewWidth = getViewWidthPixels();
    final double modelWidth = getModelWidth();
    return modelUnit.multiply(modelWidth).divide(viewWidth);
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
      final double delta = getModelUnitsPerViewUnit() * 50;
      validBoundingBox = validBoundingBox.bboxEdit(editor -> editor.expandDelta(delta));
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
    if (logUnits < 0 && Math.abs(Math.floor(logUnits)) > this.maxDecimalDigits) {
      modelUnitsPerViewUnit = 2 * Math.pow(10, -this.maxDecimalDigits);
      final double minModelWidth = getViewWidthPixels() * modelUnitsPerViewUnit;
      final double minModelHeight = getViewHeightPixels() * modelUnitsPerViewUnit;
      final double deltaX = (minModelWidth - modelWidth) / 2;
      final double deltaY = (minModelHeight - modelHeight) / 2;
      validBoundingBox = validBoundingBox.bboxEdit(editor -> editor.expandDelta(deltaX, deltaY));
    }
    return validBoundingBox;
  }

  public boolean isComponentResizing() {
    return this.componentResizing.isTrue();
  }

  @Override
  public Graphics2DViewRender newViewRenderer() {
    return new Graphics2DViewRender(this);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    if (isInitialized() && event.getSource() == getProject()) {
      if (event.getPropertyName().equals("viewBoundingBox")) {
        // final BoundingBox boundingBox =
        // (BoundingBox)event.getNewValue();
        // if (isInitialized()) {
        // updateCachedFields();
        // } else {
        // setBoundingBoxInternal(boundingBox);
        // }
      } else {
        Invoke.later(this::updateCachedFields);
      }
    }
  }

  public void repaint() {
    this.component.repaint();
  }

  @Override
  protected void setGeometryFactoryPreEvent(final GeometryFactory geometryFactory) {
    final CoordinateSystem coordinateSystem = geometryFactory.getHorizontalCoordinateSystem();
    if (coordinateSystem != null) {
      final BoundingBox areaBoundingBox = geometryFactory.getAreaBoundingBox();
      final double minX = areaBoundingBox.getMinX();
      final double maxX = areaBoundingBox.getMaxX();
      final double minY = areaBoundingBox.getMinY();
      final double maxY = areaBoundingBox.getMaxY();
      final double logMinX = Math.log10(Math.abs(minX));
      final double logMinY = Math.log10(Math.abs(minY));
      final double logMaxX = Math.log10(Math.abs(maxX));
      final double logMaxY = Math.log10(Math.abs(maxY));
      final double maxLog = Math
        .abs(Math.max(Math.max(logMinX, logMinY), Math.max(logMaxX, logMaxY)));
      this.maxIntegerDigits = (int)Math.floor(maxLog + 1);
      this.maxDecimalDigits = 15 - this.maxIntegerDigits;

      final BoundingBox boundingBox = getBoundingBox();
      if (Property.hasValue(boundingBox)) {
        final BoundingBox newBoundingBox = boundingBox.bboxToCs(geometryFactory);
        BoundingBox intersection = newBoundingBox.bboxIntersection(areaBoundingBox);
        if (intersection.isEmpty()) {
          intersection = areaBoundingBox;
        }

        setBoundingBox(intersection);
      }
    }
  }

  @Override
  public void setInitialized(final boolean initialized) {
    if (initialized && !isInitialized()) {
      updateCachedFields();
    }
    super.setInitialized(initialized);
  }

  public void translate(final double dx, final double dy) {
    final BoundingBox boundingBox = getBoundingBox();
    final BoundingBox newBoundingBox = boundingBox.getGeometryFactory()
      .newBoundingBox(boundingBox.getMinX() + dx, boundingBox.getMinY() + dy,
        boundingBox.getMaxX() + dx, boundingBox.getMaxY() + dy);
    setBoundingBox(newBoundingBox);

  }

  @Override
  public void update() {
    repaint();
  }

  private void updateCachedFields() {
    final LayerGroup project = getProject();
    final GeometryFactory geometryFactory = project.getGeometryFactory();
    if (geometryFactory != null) {
      if (geometryFactory != getGeometryFactory()) {
        setGeometryFactory(geometryFactory);
      }
      final Insets insets = this.component.getInsets();

      final int viewWidth = this.component.getWidth() - insets.left - insets.right;
      final int viewHeight = this.component.getHeight() - insets.top - insets.bottom;

      setViewWidth(viewWidth);
      setViewHeight(viewHeight);
      setBoundingBox(getBoundingBox());

      this.component.repaint();
    }
  }

}
