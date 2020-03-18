package com.revolsys.swing.map;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

import org.jeometry.common.number.Doubles;
import org.jeometry.coordinatesystem.model.Ellipsoid;

import com.revolsys.beans.PropertyChangeSupport;
import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.geometry.model.Point;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.view.ViewRenderer;
import com.revolsys.swing.map.view.graphics.Graphics2DViewRenderer;
import com.revolsys.util.Property;
import com.revolsys.util.QuantityType;

import tech.units.indriya.unit.Units;

public abstract class Viewport2D implements GeometryFactoryProxy, PropertyChangeSupportProxy {

  public static final Geometry EMPTY_GEOMETRY = GeometryFactory.DEFAULT_3D.geometry();

  public static final int HOTSPOT_PIXELS = 7;

  private static List<Double> GEOGRAPHIC_UNITS_PER_PIXEL = Arrays.asList(5.0, 2.0, 1.0, 0.5, 0.2,
    0.1, 0.05, 0.02, 0.01, 0.005, 0.002, 0.001, 0.0005, 0.0002, 0.0001, 0.00005, 0.00002, 0.00001,
    0.000005, 0.000002, 0.000001, 0.0000005, 0.0000002, 0.0000001);

  private static List<Double> PROJECTED_UNITS_PER_PIXEL = Arrays.asList(500000.0, 200000.0,
    100000.0, 50000.0, 20000.0, 10000.0, 5000.0, 2000.0, 1000.0, 500.0, 200.0, 100.0, 50.0, 20.0,
    10.0, 5.0, 2.0, 1.0, 0.5, 0.2, 0.1, 0.05, 0.02, 0.01, 0.005, 0.002, 0.001);

  public static final List<Long> SCALES = newScales(PROJECTED_UNITS_PER_PIXEL);

  private static final double PIXEL_SIZE_METRES = 2.5e-4;

  public static double getScale(final Quantity<Length> viewWidth,
    final Quantity<Length> modelWidth) {
    final double width1 = QuantityType.doubleValue(viewWidth, Units.METRE);
    final double width2 = QuantityType.doubleValue(modelWidth, Units.METRE);
    if (width1 == 0 || width2 == 0) {
      return Double.NaN;
    } else {
      final double scale = width2 / width1;
      return scale;
    }
  }

  private static List<Long> newScales(final List<Double> resolutions) {
    final List<Long> scales = new ArrayList<>(resolutions.size());
    for (final double resolution : resolutions) {

      final long scale = (long)Math.ceil(resolution * 4000);
      scales.add(scale);
    }
    return scales;
  }

  public static AffineTransform newScreenToModelTransform(final BoundingBox boundingBox,
    final double viewWidth, final double viewHeight) {
    final AffineTransform transform = new AffineTransform();
    final double mapWidth = boundingBox.getWidth();
    final double mapHeight = boundingBox.getHeight();
    final double xUnitsPerPixel = mapWidth / viewWidth;
    final double yUnitsPerPixel = mapHeight / viewHeight;

    final double originX = boundingBox.getMinX();
    final double originY = boundingBox.getMaxY();

    transform.concatenate(AffineTransform.getTranslateInstance(originX, originY));
    transform.concatenate(AffineTransform.getScaleInstance(xUnitsPerPixel, -yUnitsPerPixel));
    return transform;
  }

  private BoundingBox boundingBox = BoundingBox.empty();

  private GeometryFactory geometryFactory = GeometryFactory.floating3d(3857);

  private GeometryFactory geometryFactory2d = GeometryFactory.floating2d(3857);

  private boolean initialized = false;

  private AffineTransform modelToScreenTransform;

  private double pixelsPerXUnit;

  private double pixelsPerYUnit;

  private Reference<Project> project;

  /** The property change listener support. */
  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

  private double scale;

  private List<Long> scales = SCALES;

  private AffineTransform screenToModelTransform;

  private double metresPerPixel;

  private double unitsPerPixel;

  private double viewHeight;

  private double viewWidth;

  private double hotspotMapUnits = 6;

  private boolean zoomByUnitsPerPixel = true;

  private List<Double> unitsPerPixelList = PROJECTED_UNITS_PER_PIXEL;

  private double minUnitsPerPixel;

  private double maxUnitsPerPixel;

  protected ViewportCacheBoundingBox cacheBoundingBox = ViewportCacheBoundingBox.EMPTY;

  public Viewport2D() {
  }

  public Viewport2D(final Project project) {
    this(project, 0, 0, project.getViewBoundingBox());
  }

  public Viewport2D(final Project project, final double width, final double height,
    BoundingBox boundingBox) {
    this.project = new WeakReference<>(project);
    this.viewWidth = width;
    this.viewHeight = height;
    GeometryFactory geometryFactory;
    if (boundingBox == null) {
      geometryFactory = GeometryFactory.worldMercator();
      boundingBox = geometryFactory.getAreaBoundingBox();
    } else {
      geometryFactory = boundingBox.getGeometryFactory();
      if (!geometryFactory.isHasHorizontalCoordinateSystem()) {
        geometryFactory = GeometryFactory.worldMercator();
      }
      if (boundingBox.isEmpty()) {
        boundingBox = geometryFactory.getAreaBoundingBox();
      }
    }
    setGeometryFactory(geometryFactory);
    setBoundingBox(boundingBox);
  }

  protected Viewport2D(final Viewport2D parentViewport) {
    final ViewportCacheBoundingBox cacheBoundingBox = parentViewport.getCacheBoundingBox();
    this.project = new WeakReference<>(parentViewport.getProject());
    this.viewWidth = cacheBoundingBox.getViewWidthPixels();
    this.viewHeight = cacheBoundingBox.getViewHeightPixels();
    setGeometryFactory(cacheBoundingBox.getGeometryFactory());
    this.boundingBox = cacheBoundingBox.getBoundingBox();
    this.cacheBoundingBox = cacheBoundingBox;
  }

  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  public ViewportCacheBoundingBox getCacheBoundingBox() {
    return this.cacheBoundingBox;
  }

  public double getClosestUnitsPerPixel(final double unitsPerPixel) {
    final List<Double> values = this.unitsPerPixelList;
    for (int i = values.size() - 1; i >= 0; i--) {
      final double nextValue = values.get(i);
      final double percent = Math.abs(1 - unitsPerPixel / nextValue);
      if (nextValue > unitsPerPixel || percent < 0.1) {
        return nextValue;
      }
    }
    return this.maxUnitsPerPixel;
  }

  public Geometry getGeometry(final Geometry geometry) {
    final BoundingBox viewExtent = getBoundingBox();
    if (Property.hasValue(geometry)) {
      if (!viewExtent.isEmpty()) {
        final BoundingBox geometryExtent = geometry.getBoundingBox();
        if (geometryExtent.bboxIntersects(viewExtent)) {

          final GeometryFactory geometryFactory = getGeometryFactory2dFloating();
          if (geometryFactory.isSameCoordinateSystem(geometry)) {
            return geometry;
          } else {
            return geometryFactory.geometry(geometry);
          }
        }
      }
    }
    return EMPTY_GEOMETRY;
  }

  /**
   * Get the coordinate system the project is displayed in.
   *
   * @return The coordinate system the project is displayed in.
   */
  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  public GeometryFactory getGeometryFactory2dFloating() {
    return this.geometryFactory2d;
  }

  public double getHotspotMapUnits() {
    return this.hotspotMapUnits;
  }

  public double getMetresPerPixel() {
    return this.metresPerPixel;
  }

  public double getModelHeight() {
    final double height = getBoundingBox().getHeight();
    return height;
  }

  public Quantity<Length> getModelHeightLength() {
    return getBoundingBox().getHeightLength();
  }

  public AffineTransform getModelToScreenTransform() {
    return this.modelToScreenTransform;
  }

  public double getModelUnitsPerViewUnit() {
    return getModelHeight() / getViewHeightPixels();
  }

  public double getModelWidth() {
    final double width = getBoundingBox().getWidth();
    return width;
  }

  public Quantity<Length> getModelWidthLength() {
    return getBoundingBox().getWidthLength();
  }

  public double getPixelSizeMetres() {
    return PIXEL_SIZE_METRES;
  }

  public double getPixelsPerXUnit() {
    return this.pixelsPerXUnit;
  }

  public double getPixelsPerYUnit() {
    return this.pixelsPerYUnit;
  }

  public Project getProject() {
    if (this.project == null) {
      return null;
    } else {
      return this.project.get();
    }
  }

  /**
   * Get the property change support, used to fire property change
   * notifications. Returns null if no listeners are registered.
   *
   * @return The property change support.
   */
  @Override
  public PropertyChangeSupport getPropertyChangeSupport() {
    return this.propertyChangeSupport;
  }

  @SuppressWarnings("unchecked")
  public <V extends Geometry> V getRoundedGeometry(final V geometry) {
    if (geometry == null) {
      return null;
    } else {
      final GeometryFactory geometryFactory = geometry.getGeometryFactory();

      final GeometryFactory roundedGeometryFactory = getRoundedGeometryFactory(geometryFactory);
      if (geometryFactory == roundedGeometryFactory) {
        return geometry;
      } else {
        return (V)geometry.newGeometry(geometryFactory);
      }
    }
  }

  public GeometryFactory getRoundedGeometryFactory(GeometryFactory geometryFactory) {
    if (geometryFactory.isProjected()) {
      final double resolution = getMetresPerPixel();
      if (resolution > 2) {
        geometryFactory = geometryFactory.convertScales(1.0, 1.0);
      } else {
        geometryFactory = geometryFactory.convertScales(1000.0, 1000.0);
      }
    }
    return geometryFactory;
  }

  public double getScale() {
    return this.scale;
  }

  public List<Long> getScales() {
    return this.scales;
  }

  public AffineTransform getScreenToModelTransform() {
    return this.screenToModelTransform;
  }

  public double getUnitsPerPixel() {
    return this.unitsPerPixel;
  }

  public double getUnitsPerPixel(final double scale) {
    return scale * getMetresPerPixel();
  }

  public List<Double> getUnitsPerPixelList() {
    return this.unitsPerPixelList;
  }

  public double getViewAspectRatio() {
    final double viewWidthPixels = getViewWidthPixels();
    final double viewHeightPixels = getViewHeightPixels();
    if (viewHeightPixels == 0) {
      return 0;
    } else {
      return viewWidthPixels / viewHeightPixels;
    }
  }

  public double getViewHeightPixels() {
    return this.viewHeight;
  }

  public double getViewWidthPixels() {
    return this.viewWidth;
  }

  public long getZoomInScale(final double scale, final int steps) {
    final List<Long> values = this.scales;
    final long currentValue = Math.round(scale);
    long previousValue = values.get(0) * 2;
    for (int i = 0; i < values.size() - 1; i++) {
      long value = values.get(i);
      final long testValue = Math.round(value + (previousValue - value) * 0.2);
      if (testValue < currentValue) {
        for (int j = 1; j < steps && i + j < values.size(); j++) {
          value = values.get(i + j);
        }
        return value;
      }
      previousValue = value;
    }
    return values.get(values.size() - 1);
  }

  public double getZoomInUnitsPerPixel(final double unitsPerPixel, final int steps) {
    final List<Double> values = this.unitsPerPixelList;
    final double currentValue = unitsPerPixel;
    double previousValue = values.get(0) * 2;
    for (int i = 0; i < values.size() - 1; i++) {
      double value = values.get(i);
      final double testValue = value + (previousValue - value) * 0.2;
      if (testValue < currentValue) {
        for (int j = 1; j < steps && i + j < values.size(); j++) {
          value = values.get(i + j);
        }
        return value;
      }
      previousValue = value;
    }
    return this.minUnitsPerPixel;
  }

  public long getZoomOutScale(final double scale, final int steps) {
    final long currentValue = Math.round(scale);
    final List<Long> values = this.scales;
    long previousValue = 0;
    for (int i = values.size() - 1; i >= 0; i--) {
      long value = values.get(i);
      final long testValue = Math.round(previousValue + (value - previousValue) * 0.8);
      if (testValue > currentValue) {
        for (int j = 1; j < steps && i - j >= 0; j++) {
          value = values.get(i - j);
        }
        return value;
      }
      previousValue = value;
    }
    return values.get(0);
  }

  public double getZoomOutUnitsPerPixel(final double unitsPerPixel, final int steps) {
    final List<Double> values = this.unitsPerPixelList;
    final double currentValue = unitsPerPixel;
    double previousValue = 0;
    for (int i = values.size() - 1; i >= 0; i--) {
      double value = values.get(i);
      final double testValue = previousValue + (value - previousValue) * 0.8;
      if (testValue > currentValue) {
        for (int j = 1; j < steps && i - j >= 0; j++) {
          value = values.get(i - j);
        }
        return value;
      }
      previousValue = value;
    }
    return this.maxUnitsPerPixel;
  }

  private boolean isChanged(final double value1, final double value2) {
    return Double.isFinite(value1) && value1 > 0 && Math.abs(1 - value2 / value1) >= 1e-2;
  }

  public boolean isInitialized() {
    return this.initialized;
  }

  public boolean isViewValid() {
    return this.viewWidth > 0 && this.viewHeight > 0 && getProject() != null;
  }

  public boolean isZoomByUnitsPerPixel() {
    return this.zoomByUnitsPerPixel;
  }

  public AffineTransform newModelToScreenTransform(final BoundingBox boundingBox,
    final double viewWidth, final double viewHeight) {
    final AffineTransform modelToScreenTransform = new AffineTransform();
    final double mapWidth = boundingBox.getWidth();
    this.pixelsPerXUnit = viewWidth / mapWidth;
    this.hotspotMapUnits = HOTSPOT_PIXELS / this.pixelsPerXUnit;

    final double mapHeight = boundingBox.getHeight();
    this.pixelsPerYUnit = -viewHeight / mapHeight;

    final double originX = boundingBox.getMinX();
    final double originY = boundingBox.getMaxY();
    modelToScreenTransform
      .concatenate(AffineTransform.getScaleInstance(this.pixelsPerXUnit, this.pixelsPerYUnit));
    modelToScreenTransform.concatenate(AffineTransform.getTranslateInstance(-originX, -originY));
    return modelToScreenTransform;
  }

  public abstract ViewRenderer newViewRenderer();

  public Graphics2DViewRenderer newViewRenderer(final Graphics graphics) {
    return new Graphics2DViewRenderer(this, (Graphics2D)graphics);
  }

  protected synchronized void resetView(final double viewWidth, final double viewHeight) {
    if (viewWidth != this.viewWidth || viewHeight != this.viewHeight) {
      this.viewWidth = viewWidth;
      this.viewHeight = viewHeight;
      this.unitsPerPixel = 0;
      final BoundingBox boundingBox = getBoundingBox();
      setBoundingBox(boundingBox);
      updateCacheBoundingBox();
    }
  }

  public BoundingBox setBoundingBox(BoundingBox boundingBox) {
    if (boundingBox != null && !boundingBox.isEmpty()
      && (!this.boundingBox.equals(boundingBox) || this.unitsPerPixel == 0)) {
      final GeometryFactory geometryFactory = getGeometryFactory2dFloating();
      boundingBox = boundingBox.bboxToCs(geometryFactory);
      if (!boundingBox.isEmpty()) {
        BoundingBox newBoundingBox = boundingBox;
        double width = newBoundingBox.getWidth();
        final double height = newBoundingBox.getHeight();

        final double viewWidthPixels = getViewWidthPixels();
        final double viewHeightPixels = getViewHeightPixels();
        double unitsPerPixel;
        if (viewWidthPixels > 0) {
          final double viewAspectRatio = getViewAspectRatio();
          final double aspectRatio = newBoundingBox.getAspectRatio();
          if (viewAspectRatio != aspectRatio) {
            if (aspectRatio < viewAspectRatio) {
              final double newWidth = height * viewAspectRatio;
              final double expandX = (newWidth - width) / 2;
              newBoundingBox = newBoundingBox.bboxEdit(editor -> editor.expandDeltaX(expandX));
              width = newBoundingBox.getWidth();
            } else if (aspectRatio > viewAspectRatio) {
              final double newHeight = width / viewAspectRatio;
              final double expandY = (newHeight - height) / 2;
              newBoundingBox = newBoundingBox.bboxEdit(editor -> editor.expandDeltaY(expandY));
            }
          }
          unitsPerPixel = width / viewWidthPixels;
          if (!this.unitsPerPixelList.isEmpty() && viewHeightPixels > 0) {
            final double magnitudePower = Doubles.makePreciseFloor(1, Math.log10(unitsPerPixel));
            double newUnitsPerPixel;
            if (Math.abs(unitsPerPixel - this.unitsPerPixel) < 1e-8) {
              newUnitsPerPixel = unitsPerPixel;
            } else if (geometryFactory.isProjected() && magnitudePower < 0
              || geometryFactory.isGeographic() && magnitudePower < -5) {
              newUnitsPerPixel = getClosestUnitsPerPixel(unitsPerPixel);
            } else {
              final double resolution = Math.pow(10, magnitudePower - 1);
              final double scaleFactor = 1 / resolution;
              newUnitsPerPixel = Doubles.makePreciseCeil(scaleFactor, unitsPerPixel);
              if (this.minUnitsPerPixel > 0 && newUnitsPerPixel < this.minUnitsPerPixel) {
                newUnitsPerPixel = this.minUnitsPerPixel;
              } else if (this.maxUnitsPerPixel > 0 && newUnitsPerPixel > this.maxUnitsPerPixel) {
                newUnitsPerPixel = this.maxUnitsPerPixel;
              }
            }
            if (unitsPerPixel != newUnitsPerPixel) {
              unitsPerPixel = newUnitsPerPixel;
              final double centreX = Math.floor(newBoundingBox.getCentreX() / unitsPerPixel)
                * unitsPerPixel;
              final double centreY = Math.floor(newBoundingBox.getCentreY() / unitsPerPixel)
                * unitsPerPixel;
              final double minX = centreX - Math.floor(viewWidthPixels / 2) * unitsPerPixel;
              final double minY = centreY - Math.floor(viewHeightPixels / 2) * unitsPerPixel;
              final double maxX = minX + viewWidthPixels * unitsPerPixel;
              final double maxY = minY + viewHeightPixels * unitsPerPixel;
              newBoundingBox = geometryFactory.newBoundingBox(minX, minY, maxX, maxY);
            }

            // if (this.zoomByUnitsPerPixel) {
            // System.out.println(unitsPerPixel + "\t" + resolution + "\t" +
            // newUnitPerPixel);
            // unitsPerPixel = getClosestUnitsPerPixel(unitsPerPixel);
            // final double centreX = Math.floor(newBoundingBox.getCentreX() /
            // unitsPerPixel)
            // * unitsPerPixel;
            // final double centreY = Math.floor(newBoundingBox.getCentreY() /
            // unitsPerPixel)
            // * unitsPerPixel;
            // final double minX = centreX - Math.floor(viewWidthPixels / 2) *
            // unitsPerPixel;
            // final double minY = centreY - Math.floor(viewHeightPixels / 2) *
            // unitsPerPixel;
            // final double maxX = minX + viewWidthPixels * unitsPerPixel;
            // final double maxY = minY + viewHeightPixels * unitsPerPixel;
            // newBoundingBox = geometryFactory.newBoundingBox(minX, minY, maxX,
            // maxY);
            // } else if (unitsPerPixel < this.minUnitsPerPixel) {
            // unitsPerPixel = this.minUnitsPerPixel;
            // } else if (unitsPerPixel > this.maxUnitsPerPixel) {
            // unitsPerPixel = this.maxUnitsPerPixel;
            // }
          }
        } else {
          unitsPerPixel = Double.NaN;
        }
        setBoundingBoxAndUnitsPerPixel(newBoundingBox, unitsPerPixel);
      }
    }
    return getBoundingBox();
  }

  public void setBoundingBoxAndGeometryFactory(final BoundingBox boundingBox) {
    final GeometryFactory oldGeometryFactory = this.geometryFactory;
    final GeometryFactory geometryFactory = boundingBox.getGeometryFactory();
    setBoundingBox(boundingBox);

    if (setGeometryFactoryDo(geometryFactory)) {
      this.propertyChangeSupport.firePropertyChange("geometryFactory", oldGeometryFactory,
        this.geometryFactory);
    }
  }

  private void setBoundingBoxAndUnitsPerPixel(final BoundingBox boundingBox,
    final double unitsPerPixel) {
    final double oldScale = getScale();
    final double oldUnitsPerPixel = getUnitsPerPixel();
    final BoundingBox oldBoundingBox = this.boundingBox;
    synchronized (this) {
      setUnitsPerPixelInternal(boundingBox, unitsPerPixel);
      setBoundingBoxInternal(boundingBox);
    }
    if (this.unitsPerPixel > 0) {
      this.propertyChangeSupport.firePropertyChange("unitsPerPixel", oldUnitsPerPixel,
        this.unitsPerPixel);
    }
    if (this.scale > 0) {
      this.propertyChangeSupport.firePropertyChange("scale", oldScale, this.scale);
    }
    this.propertyChangeSupport.firePropertyChange("boundingBox", oldBoundingBox, boundingBox);
  }

  private BoundingBox setBoundingBoxForScale(final BoundingBox boundingBox, final double scale) {
    final Point centre = boundingBox.getCentre();
    return setCentreFromScale(centre, scale);
  }

  private synchronized void setBoundingBoxInternal(final BoundingBox boundingBox) {
    if (!boundingBox.equals(this.boundingBox)) {
      this.boundingBox = boundingBox;
      updateCacheBoundingBox();
    }
  }

  public void setCentre(Point centre) {
    if (centre != null) {
      final GeometryFactory geometryFactory = getGeometryFactory();
      centre = centre.convertGeometry(geometryFactory, 2);
      if (!centre.isEmpty()) {
        final double scale = getScale();
        setCentreFromScale(centre, scale);
      }
    }
  }

  private BoundingBox setCentreFromScale(final Point centre, final double scale) {
    final double unitsPerPixel = scale * getPixelSizeMetres();
    return setCentreFromUnitsPerPixel(centre, unitsPerPixel);
  }

  private BoundingBox setCentreFromUnitsPerPixel(final Point centre, final double unitsPerPixel) {
    final double viewWidthPixels = getViewWidthPixels();
    final double viewHeightPixels = getViewHeightPixels();

    final int minXPixelOffset = (int)Math.ceil(viewWidthPixels / 2.0);
    final int minYPixelOffset = (int)Math.ceil(viewHeightPixels / 2.0);

    final double centreX = centre.getX();
    final double centreY = centre.getY();

    final double minX = Math.floor(centreX / unitsPerPixel - minXPixelOffset) * unitsPerPixel;
    final double minY = Math.floor(centreY / unitsPerPixel - minYPixelOffset) * unitsPerPixel;

    final GeometryFactory geometryFactory = getGeometryFactory();

    final double width = viewWidthPixels * unitsPerPixel;
    final double height = viewHeightPixels * unitsPerPixel;

    final double maxX = minX + width;
    final double maxY = minY + height;
    final BoundingBox boundingBox = geometryFactory.newBoundingBox(minX, minY, maxX, maxY);
    setBoundingBoxAndUnitsPerPixel(boundingBox, unitsPerPixel);
    return boundingBox;
  }

  /**
   * Set the coordinate system the project is displayed in.
   *
   * @param coordinateSystem The coordinate system the project is displayed in.
   */
  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    final GeometryFactory oldGeometryFactory = this.geometryFactory;
    if (setGeometryFactoryDo(geometryFactory)) {
      setGeometryFactoryPreEvent(geometryFactory);
      this.propertyChangeSupport.firePropertyChange("geometryFactory", oldGeometryFactory,
        geometryFactory);
      setGeometryFactoryPostEvent(geometryFactory);
    }
  }

  protected boolean setGeometryFactoryDo(final GeometryFactory geometryFactory) {
    final GeometryFactory oldGeometryFactory = this.geometryFactory;
    if (oldGeometryFactory != null && oldGeometryFactory.equals(geometryFactory)) {
      return false;
    } else {
      if (geometryFactory == null) {
        this.geometryFactory = GeometryFactory.DEFAULT_3D;
      } else {
        this.geometryFactory = geometryFactory;
      }

      this.geometryFactory2d = this.geometryFactory.toFloating2d();
      if (geometryFactory.isGeographic()) {
        this.unitsPerPixelList = GEOGRAPHIC_UNITS_PER_PIXEL;
      } else {
        this.unitsPerPixelList = PROJECTED_UNITS_PER_PIXEL;
      }
      this.maxUnitsPerPixel = this.unitsPerPixelList.get(0);
      this.minUnitsPerPixel = this.unitsPerPixelList.get(this.unitsPerPixelList.size() - 1);
      return true;
    }
  }

  protected void setGeometryFactoryPostEvent(final GeometryFactory geometryFactory2) {
  }

  protected void setGeometryFactoryPreEvent(final GeometryFactory geometryFactory2) {
  }

  public void setInitialized(final boolean initialized) {
    this.initialized = initialized;
  }

  protected void setModelToScreenTransform(final AffineTransform modelToScreenTransform) {
    this.modelToScreenTransform = modelToScreenTransform;
  }

  public void setScale(final double scale) {
    final double oldScale = getScale();
    if (isChanged(oldScale, scale)) {
      setZoomByUnitsPerPixel(false);
      final double oldUnitsPerPixel = getUnitsPerPixel();
      final double oldMetresPerPixel = getMetresPerPixel();
      final BoundingBox boundingBox = getBoundingBox();
      setBoundingBoxForScale(boundingBox, scale);
      this.propertyChangeSupport.firePropertyChange("scale", oldScale, scale);

      final double metresPerPxel = getMetresPerPixel();
      if (isChanged(oldMetresPerPixel, metresPerPxel)) {
        this.propertyChangeSupport.firePropertyChange("metresPerPixel", oldMetresPerPixel,
          metresPerPxel);
      }
      final double unitsPerPxel = getUnitsPerPixel();
      if (isChanged(oldUnitsPerPixel, unitsPerPxel)) {
        this.propertyChangeSupport.firePropertyChange("unitsPerPxel", oldUnitsPerPixel,
          unitsPerPxel);
      }
    }
  }

  public void setScales(final List<Long> scales) {
    this.scales = scales;
  }

  public void setUnitsPerPixel(final double unitsPerPixel) {
    final double unitsPerPixel2 = getUnitsPerPixel();
    if (isChanged(unitsPerPixel, unitsPerPixel2)) {
      setZoomByUnitsPerPixel(true);

      final BoundingBox boundingBox = getBoundingBox();
      final Point centre = boundingBox.getCentre();
      setCentreFromUnitsPerPixel(centre, unitsPerPixel);
    }
  }

  private void setUnitsPerPixelInternal(final BoundingBox boundingBox, double unitsPerPixel) {
    if (Double.isFinite(unitsPerPixel)) {
      final double viewWidthPixels = getViewWidthPixels();
      final double viewHeightPixels = getViewHeightPixels();
      if (unitsPerPixel < this.minUnitsPerPixel) {
        unitsPerPixel = this.minUnitsPerPixel;
      }
      final double pixelSizeMetres = getPixelSizeMetres();
      this.unitsPerPixel = unitsPerPixel;
      setModelToScreenTransform(
        newModelToScreenTransform(boundingBox, viewWidthPixels, viewHeightPixels));
      this.screenToModelTransform = newScreenToModelTransform(boundingBox, viewWidthPixels,
        viewHeightPixels);
      final Quantity<Length> modelWidthLength = boundingBox.getWidthLength();
      double modelWidthMetres;
      final GeometryFactory geometryFactory = getGeometryFactory();
      if (geometryFactory.isProjected()) {
        modelWidthMetres = QuantityType.doubleValue(modelWidthLength, Units.METRE);
      } else {
        final double minX = boundingBox.getMinX();
        final double centreY = boundingBox.getCentreY();
        final double maxX = boundingBox.getMaxX();
        final Ellipsoid ellipsoid = geometryFactory.getEllipsoid();
        modelWidthMetres = ellipsoid.distanceMetres(minX, centreY, maxX, centreY);
      }
      this.metresPerPixel = modelWidthMetres / viewWidthPixels;
      this.scale = this.metresPerPixel / pixelSizeMetres;
    } else {
      this.metresPerPixel = 0;
      this.unitsPerPixel = 0;
      setModelToScreenTransform(null);
      this.screenToModelTransform = null;
      this.scale = 0;
    }
  }

  public void setZoomByUnitsPerPixel(final boolean zoomByUnitsPerPixel) {
    this.zoomByUnitsPerPixel = zoomByUnitsPerPixel;
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
    if (this.geometryFactory2d == null) {
      return GeometryFactory.DEFAULT_2D.point();
    } else {
      final double[] coordinates = toModelCoordinates(viewCoordinates);
      return this.geometryFactory2d.point(coordinates[0], coordinates[1]);
    }
  }

  public Point toModelPoint(final GeometryFactory geometryFactory,
    final double... viewCoordinates) {
    final double[] coordinates = toModelCoordinates(viewCoordinates);
    if (Double.isInfinite(coordinates[0]) || Double.isInfinite(coordinates[1])
      || Double.isNaN(coordinates[0]) || Double.isNaN(coordinates[1])) {
      return geometryFactory.point();
    } else {
      final Point point = this.geometryFactory2d.point(coordinates[0], coordinates[1]);
      return point.newGeometry(geometryFactory);
    }
  }

  public Point toModelPoint(final int x, final int y) {
    final AffineTransform transform = getScreenToModelTransform();
    if (transform == null) {
      return this.geometryFactory2d.point(x, y);
    } else {
      final double[] coordinates = new double[] {
        x, y
      };
      transform.transform(coordinates, 0, coordinates, 0, 1);
      return this.geometryFactory2d.point(coordinates);
    }
  }

  public Point toModelPoint(final java.awt.Point point) {
    final double x = point.getX();
    final double y = point.getY();
    return toModelPoint(x, y);
  }

  public Point toModelPointRounded(GeometryFactory geometryFactory, final int x, final int y) {
    geometryFactory = getRoundedGeometryFactory(geometryFactory);
    return toModelPoint(geometryFactory, x, y);
  }

  public void update() {
  }

  private void updateCacheBoundingBox() {
    if (this.viewWidth == 0 || this.viewHeight == 0 || this.boundingBox.isEmpty()) {
      if (this.cacheBoundingBox != ViewportCacheBoundingBox.EMPTY) {
        this.cacheBoundingBox.cancel();
        this.cacheBoundingBox = ViewportCacheBoundingBox.EMPTY;
      }
    } else if (!this.cacheBoundingBox.isDimension(this.viewWidth, this.viewHeight)
      || !this.boundingBox.equals(this.cacheBoundingBox.getBoundingBox())) {
      this.cacheBoundingBox.cancel();
      this.cacheBoundingBox = new ViewportCacheBoundingBox(this);
    }
  }

  public void zoomIn() {
    if (this.zoomByUnitsPerPixel) {
      final double unitsPerPixel = getUnitsPerPixel();
      final double newUnitsPerPixel = getZoomInUnitsPerPixel(unitsPerPixel, 1);
      setUnitsPerPixel(newUnitsPerPixel);
    } else {
      final double scale = getScale();
      final long newScale = getZoomInScale(scale, 1);
      setScale(newScale);
    }
  }

  public void zoomOut() {
    if (this.zoomByUnitsPerPixel) {
      final double unitsPerPixel = getUnitsPerPixel();
      final double newUnitsPerPixel = getZoomOutUnitsPerPixel(unitsPerPixel, 1);
      setUnitsPerPixel(newUnitsPerPixel);
    } else {
      final double scale = getScale();
      final long newScale = getZoomOutScale(scale, 1);
      setScale(newScale);
    }
  }

}
