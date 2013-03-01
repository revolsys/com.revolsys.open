package com.revolsys.swing.map;

import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.measure.Measurable;
import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.swing.map.layer.Project;
import com.vividsolutions.jts.geom.Point;

public class Viewport2D {

  public static AffineTransform createModelToScreenTransform(
    final BoundingBox boundingBox, final double viewWidth,
    final double viewHeight) {
    final AffineTransform modelToScreenTransform = new AffineTransform();
    final double mapWidth = boundingBox.getWidth();
    final double mapHeight = boundingBox.getHeight();
    final double pixelsPerXUnit = viewWidth / mapWidth;
    final double pixelsPerYUnit = viewHeight / mapHeight;

    final double originX = boundingBox.getMinX();
    final double originY = boundingBox.getMaxY();

    modelToScreenTransform.concatenate(AffineTransform.getScaleInstance(
      pixelsPerXUnit, -pixelsPerYUnit));
    modelToScreenTransform.concatenate(AffineTransform.getTranslateInstance(
      -originX, -originY));
    return modelToScreenTransform;
  }

  public static AffineTransform createScreenToModelTransform(
    final BoundingBox boundingBox, final double viewWidth,
    final double viewHeight) {
    final AffineTransform transform = new AffineTransform();
    final double mapWidth = boundingBox.getWidth();
    final double mapHeight = boundingBox.getHeight();
    final double xUnitsPerPixel = mapWidth / viewWidth;
    final double yUnitsPerPixel = mapHeight / viewHeight;

    final double originX = boundingBox.getMinX();
    final double originY = boundingBox.getMaxY();

    transform.concatenate(AffineTransform.getTranslateInstance(originX, originY));
    transform.concatenate(AffineTransform.getScaleInstance(xUnitsPerPixel,
      -yUnitsPerPixel));
    return transform;
  }

  public static double getScale(final Measurable<Length> viewWidth,
    final Measurable<Length> modelWidth) {
    final double width1 = viewWidth.doubleValue(SI.METRE);
    final double width2 = modelWidth.doubleValue(SI.METRE);
    if (width1 == 0 || width2 == 0) {
      return Double.NaN;
    } else {
      final double scale = width2 / width1;
      return scale;
    }
  }

  /** The current bounding box of the project. */
  private BoundingBox boundingBox = new BoundingBox();

  private GeometryFactory geometryFactory = GeometryFactory.getFactory(3005);

  private Project project;

  private AffineTransform modelToScreenTransform;

  /** The property change listener support. */
  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
    this);

  private AffineTransform savedTransform;

  private AffineTransform screenToModelTransform;

  private int viewWidth;

  private int viewHeight;

  /** Multiplier to convert a value to be 1 pixel size at 72DPI.*/
  private double standardPixelScaleFactor;

  private double metresPerPixel;

  private double scale;

  public Viewport2D() {
  }

  public Viewport2D(final Project project) {
    this.project = project;
    this.geometryFactory = project.getGeometryFactory();
  }

  public Viewport2D(final Project project, final int width, final int height,
    final BoundingBox boundingBox) {
    this(project);
    this.viewWidth = width;
    this.viewHeight = height;
    setBoundingBox(boundingBox);
    setGeometryFactory(boundingBox.getGeometryFactory());
  }

  /**
   * Add the property change listener.
   * 
   * @param listener The listener.
   */
  public void addPropertyChangeListener(final PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(listener);
  }

  /**
   * Remove the property change listener from the specified property.
   * 
   * @param propertyName The property name.
   * @param listener The listener.
   */
  public void addPropertyChangeListener(final String propertyName,
    final PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
  }

  public BoundingBox getBoundingBox() {
    return boundingBox;
  }

  /**
   * Get the coordinate system the project is displayed in.
   * 
   * @return The coordinate system the project is displayed in.
   */
  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  public double getMetresPerPixel() {
    return metresPerPixel;
  }

  public double getModelHeight() {
    final double height = boundingBox.getHeight();
    return height;
  }

  public Measurable<Length> getModelHeightLength() {
    return boundingBox.getHeightLength();
  }

  public AffineTransform getModelToScreenTransform() {
    return modelToScreenTransform;
  }

  public double getModelUnitsPerViewUnit() {
    return getModelHeight() / getViewHeightPixels();
  }

  public double getModelWidth() {
    final double width = boundingBox.getWidth();
    return width;
  }

  public Measurable<Length> getModelWidthLength() {
    return boundingBox.getWidthLength();
  }

  public Project getProject() {
    return project;
  }

  /**
   * Get the property change support, used to fire property change
   * notifications. Returns null if no listeners are registered.
   * 
   * @return The property change support.
   */
  protected PropertyChangeSupport getPropertyChangeSupport() {
    return propertyChangeSupport;
  }

  public double getScale() {
    return scale;
  }

  public AffineTransform getScreenToModelTransform() {
    return screenToModelTransform;
  }

  public Unit<Length> getScreenUnit() {
    final Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
    final int screenResolution = defaultToolkit.getScreenResolution();
    return NonSI.INCH.divide(screenResolution);
  }

  public double getViewAspectRatio() {
    final int viewWidthPixels = getViewWidthPixels();
    final int viewHeightPixels = getViewHeightPixels();
    if (viewHeightPixels == 0) {
      return 0;
    } else {
      return (double)viewWidthPixels / viewHeightPixels;
    }
  }

  public Measurable<Length> getViewHeightLength() {
    double width = getViewHeightPixels();
    if (width < 0) {
      width = 0;
    }
    return Measure.valueOf(width, getScreenUnit());
  }

  public int getViewHeightPixels() {
    return viewHeight;
  }

  public <Q extends Quantity> Unit<Q> getViewToModelUnit(final Unit<Q> modelUnit) {
    final double viewWidth = getViewWidthPixels();
    final double modelWidth = getModelWidth();
    return modelUnit.times(modelWidth).divide(viewWidth);
  }

  public Measurable<Length> getViewWidthLength() {
    double width = getViewWidthPixels();
    if (width < 0) {
      width = 0;
    }
    return Measure.valueOf(width, getScreenUnit());
  }

  public int getViewWidthPixels() {
    return viewWidth;
  }

  public boolean isUseModelCoordinates() {
    return savedTransform != null;
  }

  /**
   * Remove the property change listener.
   * 
   * @param listener The listener.
   */
  public void removePropertyChangeListener(final PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(listener);
  }

  /**
   * Remove the property change listener from the specified property.
   * 
   * @param propertyName The property name.
   * @param listener The listener.
   */
  public void removePropertyChangeListener(final String propertyName,
    final PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
  }

  public void setBoundingBox(final BoundingBox boundingBox) {
    if (boundingBox != null) {
      GeometryFactory geometryFactory = getGeometryFactory();
      final BoundingBox convertedBoundingBox = boundingBox.convert(geometryFactory);
      if (!convertedBoundingBox.isNull()) {
        final BoundingBox oldBoundingBox = this.boundingBox;
        final double oldScale = getScale();
        this.boundingBox = convertedBoundingBox;

        final double viewWidth = getViewWidthPixels();
        final double viewHeight = getViewHeightPixels();
        if (viewWidth > 0) {
          final double viewAspectRatio = getViewAspectRatio();
          final double aspectRatio = this.boundingBox.getAspectRatio();
          if (viewAspectRatio != aspectRatio) {
            if (aspectRatio < viewAspectRatio) {
              final double width = convertedBoundingBox.getWidth();
              final double height = convertedBoundingBox.getHeight();
              final double newWidth = height * viewAspectRatio;
              final double expandX = (newWidth - width) / 2;
              this.boundingBox = this.boundingBox.clone();
              this.boundingBox.expandBy(expandX, 0);

            } else if (aspectRatio > viewAspectRatio) {
              final double width = convertedBoundingBox.getWidth();
              final double height = convertedBoundingBox.getHeight();
              final double newHeight = width / viewAspectRatio;
              final double expandY = (newHeight - height) / 2;
              this.boundingBox = this.boundingBox.clone();
              this.boundingBox.expandBy(0, expandY);

            }
          }

          final Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
          final int screenResolution = defaultToolkit.getScreenResolution();
          standardPixelScaleFactor = screenResolution / 72.0;

          modelToScreenTransform = createModelToScreenTransform(
            this.boundingBox, viewWidth, viewHeight);
          screenToModelTransform = createScreenToModelTransform(
            this.boundingBox, viewWidth, viewHeight);
          metresPerPixel = getModelHeightLength().doubleValue(SI.METRE)
            / getViewHeightPixels();
        }
        final Measurable<Length> modelWidth = getModelWidthLength();
        scale = getScale(getViewHeightLength(), modelWidth);
        propertyChangeSupport.firePropertyChange("boundingBox", oldBoundingBox,
          convertedBoundingBox);
        propertyChangeSupport.firePropertyChange("scale", oldScale, scale);

      }
    }
  }

  /**
   * Set the coordinate system the project is displayed in.
   * 
   * @param coordinateSystem The coordinate system the project is displayed in.
   */
  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    final GeometryFactory oldGeometryFactory = this.geometryFactory;
    this.geometryFactory = geometryFactory;
    propertyChangeSupport.firePropertyChange("geometryFactory",
      oldGeometryFactory, geometryFactory);
  }

  public void setScale(final double scale) {
    double oldValue = getScale();
    propertyChangeSupport.firePropertyChange("scale", oldValue, scale);
  }

  public void setUseModelCoordinates(final boolean useModelCoordinates,
    final Graphics2D graphics) {
    if (savedTransform != null) {
      graphics.setTransform(savedTransform);
    }
    if (useModelCoordinates && modelToScreenTransform != null) {
      savedTransform = graphics.getTransform();
      graphics.transform(modelToScreenTransform);
    } else {
      savedTransform = null;
    }

  }

  protected void setViewHeight(final int height) {
    this.viewHeight = height;
  }

  protected void setViewWidth(final int width) {
    this.viewWidth = width;
  }

  public double toDisplayValue(final Measure<Length> value) {
    double convertedValue;
    final Unit<Length> unit = value.getUnit();
    final double modelUnitsPerViewUnit = getModelUnitsPerViewUnit();
    if (unit.equals(NonSI.PIXEL)) {
      convertedValue = value.doubleValue(NonSI.PIXEL);
      if (isUseModelCoordinates()) {
        convertedValue = convertedValue * modelUnitsPerViewUnit;
      }
      convertedValue *= standardPixelScaleFactor;
    } else {
      convertedValue = value.doubleValue(SI.METRE);
      final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
      if (coordinateSystem instanceof GeographicCoordinateSystem) {
        final GeographicCoordinateSystem geoCs = (GeographicCoordinateSystem)coordinateSystem;
        final double radius = geoCs.getDatum().getSpheroid().getSemiMajorAxis();
        convertedValue = Math.toDegrees(convertedValue / radius);

      }
      if (!isUseModelCoordinates()) {
        convertedValue = convertedValue / modelUnitsPerViewUnit;
      }
    }
    return convertedValue;
  }

  public double toDisplayValue(final Viewport2D viewport,
    final Measure<Length> value) {
    if (viewport == null) {
      return value.getValue().doubleValue();
    } else {
      return viewport.toDisplayValue(value);
    }
  }

  public double[] toModelCoordinates(final double... viewCoordinates) {
    final AffineTransform transform = getScreenToModelTransform();
    if (transform == null) {
      return viewCoordinates;
    } else {
      final double[] coordinates = new double[2];
      transform.transform(viewCoordinates, 0, coordinates, 0, 1);
      return coordinates;
    }
  }

  public Point toModelPoint(final double... viewCoordinates) {
    if (geometryFactory == null) {
      return GeometryFactory.getFactory().createPoint();
    } else {
      final double[] coordinates = toModelCoordinates(viewCoordinates);
      return geometryFactory.createPoint(coordinates);
    }
  }

  public Point toModelPoint(final GeometryFactory geometryFactory,
    final double... viewCoordinates) {
    final double[] coordinates = toModelCoordinates(viewCoordinates);
    final Point point = this.geometryFactory.createPoint(coordinates);
    return (Point)geometryFactory.createGeometry(point);
  }

  public double[] toViewCoordinates(final double... modelCoordinates) {
    final double[] ordinates = new double[2];
    final AffineTransform transform = getModelToScreenTransform();
    if (transform == null) {
      return modelCoordinates;
    } else {
      transform.transform(modelCoordinates, 0, ordinates, 0, 1);
      return ordinates;
    }
  }

  public Point2D toViewPoint(final double x, final double y) {
    final double[] coordinates = toViewCoordinates(x, y);
    final double viewX = coordinates[0];
    final double viewY = coordinates[1];
    return new Point2D.Double(viewX, viewY);
  }

  public Point2D toViewPoint(Point point) {
    point = (Point)geometryFactory.createGeometry(point);
    final double x = point.getX();
    final double y = point.getY();
    final double[] coordinates = toViewCoordinates(x, y);
    final double viewX = coordinates[0];
    final double viewY = coordinates[1];
    return new Point2D.Double(viewX, viewY);
  }

  public void update() {
  }

  public Point toModelPoint(java.awt.Point point) {
    double x = point.getX();
    double y = point.getY();
    return toModelPoint(x, y);
  }
}
