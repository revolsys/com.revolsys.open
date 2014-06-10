package com.revolsys.swing.map;

import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeSupport;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.measure.Measurable;
import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.gis.cs.ProjectedCoordinateSystem;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.Project;

public class Viewport2D implements PropertyChangeSupportProxy {

  public static final Geometry EMPTY_GEOMETRY = GeometryFactory.floating3()
    .geometry();

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

  public static double toDisplayValue(final Viewport2D viewport,
    final Measure<Length> measure) {
    if (viewport == null) {
      return measure.getValue().doubleValue();
    } else {
      return viewport.toDisplayValue(measure);
    }
  }

  private final Map<Layer, Map<String, Object>> layerValueCache = new WeakHashMap<Layer, Map<String, Object>>();

  private double pixelsPerXUnit;

  private double pixelsPerYUnit;

  private double originX;

  private double originY;

  /** The current bounding box of the project. */
  private BoundingBox boundingBox = new Envelope();

  private GeometryFactory geometryFactory = GeometryFactory.floating3(3005);

  private GeometryFactory geometryFactory2d = GeometryFactory.floating(3005, 2);

  private Reference<Project> project;

  private AffineTransform modelToScreenTransform;

  /** The property change listener support. */
  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
    this);

  private final ThreadLocal<AffineTransform> savedTransform = new ThreadLocal<AffineTransform>();

  private AffineTransform screenToModelTransform;

  private int viewWidth;

  private int viewHeight;

  private double unitsPerPixel;

  private double scale;

  private List<Long> scales = new ArrayList<Long>();

  private boolean initialized = false;

  public Viewport2D() {
  }

  public Viewport2D(final Project project) {
    this.project = new WeakReference<Project>(project);
    setGeometryFactory(project.getGeometryFactory());
  }

  public Viewport2D(final Project project, final int width, final int height,
    final BoundingBox boundingBox) {
    this(project);
    this.viewWidth = width;
    this.viewHeight = height;
    setBoundingBox(boundingBox);
    setGeometryFactory(boundingBox.getGeometryFactory());
  }

  public void clearLayerCache(final Layer layer) {
    layerValueCache.remove(layer);
  }

  public AffineTransform createModelToScreenTransform(
    final BoundingBox boundingBox, final double viewWidth,
    final double viewHeight) {
    final AffineTransform modelToScreenTransform = new AffineTransform();
    final double mapWidth = boundingBox.getWidth();
    this.pixelsPerXUnit = viewWidth / mapWidth;

    final double mapHeight = boundingBox.getHeight();
    this.pixelsPerYUnit = -viewHeight / mapHeight;

    this.originX = boundingBox.getMinX();
    this.originY = boundingBox.getMaxY();

    modelToScreenTransform.concatenate(AffineTransform.getScaleInstance(
      this.pixelsPerXUnit, this.pixelsPerYUnit));
    modelToScreenTransform.concatenate(AffineTransform.getTranslateInstance(
      -this.originX, -this.originY));
    return modelToScreenTransform;
  }

  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  public BoundingBox getBoundingBox(final GeometryFactory geometryFactory,
    final int x, final int y, final int pixels) {
    final Point p1 = toModelPoint(geometryFactory, x - pixels, y - pixels);
    final Point p2 = toModelPoint(geometryFactory, x + pixels, y + pixels);
    final BoundingBox boundingBox = new Envelope(geometryFactory, p1, p2);
    return boundingBox;
  }

  public BoundingBox getBoundingBox(final GeometryFactory geometryFactory,
    final MouseEvent event, final int pixels) {
    final int x = event.getX();
    final int y = event.getY();
    return getBoundingBox(geometryFactory, x, y, pixels);
  }

  public Geometry getGeometry(final Geometry geometry) {
    final BoundingBox viewExtent = getBoundingBox();
    if (geometry != null && !geometry.isEmpty()) {
      if (!viewExtent.isEmpty()) {
        final BoundingBox geometryExtent = geometry.getBoundingBox();
        if (geometryExtent.intersects(viewExtent)) {
          final GeometryFactory geometryFactory = getGeometryFactory();
          return geometryFactory.geometry(geometry);
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
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @SuppressWarnings("unchecked")
  public <V> V getLayerCacheValue(final Layer layer, final String name) {
    final Map<String, Object> cache = layerValueCache.get(layer);
    if (cache == null) {
      return null;
    } else {
      return (V)cache.get(name);
    }
  }

  public double getModelHeight() {
    final double height = getBoundingBox().getHeight();
    return height;
  }

  public Measurable<Length> getModelHeightLength() {
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

  public Measurable<Length> getModelWidthLength() {
    return getBoundingBox().getWidthLength();
  }

  public double getOriginX() {
    return this.originX;
  }

  public double getOriginY() {
    return this.originY;
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

  public <V extends Geometry> V getRoundedGeometry(final V geometry) {
    if (geometry == null) {
      return null;
    } else {
      final GeometryFactory geometryFactory = geometry.getGeometryFactory();

      final GeometryFactory roundedGeometryFactory = getRoundedGeometryFactory(geometryFactory);
      if (geometryFactory == roundedGeometryFactory) {
        return geometry;
      } else {
        return (V)geometry.copy(geometryFactory);
      }
    }
  }

  public com.revolsys.jts.geom.GeometryFactory getRoundedGeometryFactory(
    com.revolsys.jts.geom.GeometryFactory geometryFactory) {
    final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
    if (coordinateSystem instanceof ProjectedCoordinateSystem) {
      final double resolution = getUnitsPerPixel();
      if (resolution > 2) {
        final int srid = geometryFactory.getSrid();
        final int axisCount = geometryFactory.getAxisCount();
        geometryFactory = GeometryFactory.fixed(srid, axisCount, 1.0, 1.0);
      }
    }
    return geometryFactory;
  }

  public double getScale() {
    return this.scale;
  }

  public double getScaleForUnitsPerPixel(final double unitsPerPixel) {
    return unitsPerPixel * getScreenResolution() / 0.0254;
  }

  public List<Long> getScales() {
    return this.scales;
  }

  public int getScreenResolution() {
    final Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
    final int screenResolution = defaultToolkit.getScreenResolution();
    return 96;
  }

  public AffineTransform getScreenToModelTransform() {
    return this.screenToModelTransform;
  }

  public Unit<Length> getScreenUnit() {
    final int screenResolution = getScreenResolution();
    return NonSI.INCH.divide(screenResolution);
  }

  public double getUnitsPerPixel() {
    return this.unitsPerPixel;
  }

  public double getUnitsPerPixel(final double scale) {
    return scale * 0.0254 / getScreenResolution();
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
    return this.viewHeight;
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
    return this.viewWidth;
  }

  public double getZoomOutScale(final double scale) {
    final long scaleCeil = (long)Math.floor(scale);
    final List<Long> scales = new ArrayList<Long>(this.scales);
    Collections.reverse(scales);
    for (final double nextScale : scales) {
      final long newScale = (long)Math.floor(nextScale);
      if (newScale >= scaleCeil) {
        return nextScale;
      }
    }
    return scales.get(0);
  }

  private void internalSetBoundingBox(final BoundingBox boundingBox,
    final double unitsPerPixel) {
    final double oldScale = getScale();
    final BoundingBox oldBoundingBox = this.boundingBox;
    synchronized (this) {
      final int viewWidthPixels = getViewWidthPixels();
      final int viewHeightPixels = getViewHeightPixels();
      final Measurable<Length> viewWidthLength = getViewWidthLength();
      final Measurable<Length> modelWidthLength = boundingBox.getWidthLength();

      if (Double.isInfinite(unitsPerPixel) || Double.isNaN(unitsPerPixel)) {
        this.unitsPerPixel = 0;
        this.modelToScreenTransform = null;
        this.screenToModelTransform = null;
        this.scale = 0;
      } else {
        this.unitsPerPixel = unitsPerPixel;
        this.modelToScreenTransform = createModelToScreenTransform(boundingBox,
          viewWidthPixels, viewHeightPixels);
        this.screenToModelTransform = createScreenToModelTransform(boundingBox,
          viewWidthPixels, viewHeightPixels);
        this.scale = getScale(viewWidthLength, modelWidthLength);
      }
      setBoundingBoxInternal(boundingBox);
    }
    this.propertyChangeSupport.firePropertyChange("boundingBox",
      oldBoundingBox, boundingBox);
    if (this.scale > 0) {
      this.propertyChangeSupport.firePropertyChange("scale", oldScale,
        this.scale);
    }
  }

  public boolean isInitialized() {
    return initialized;
  }

  public boolean isUseModelCoordinates() {
    return this.savedTransform.get() != null;
  }

  public BoundingBox setBoundingBox(final BoundingBox boundingBox) {
    if (boundingBox != null && !boundingBox.isEmpty()) {
      double unitsPerPixel = 0;
      final GeometryFactory geometryFactory = getGeometryFactory();
      final BoundingBox convertedBoundingBox = boundingBox.convert(geometryFactory);
      if (!convertedBoundingBox.isEmpty()) {
        BoundingBox newBoundingBox = convertedBoundingBox;

        final int viewWidthPixels = getViewWidthPixels();
        final int viewHeightPixels = getViewHeightPixels();
        if (viewWidthPixels > 0) {
          final double viewAspectRatio = getViewAspectRatio();
          final double aspectRatio = newBoundingBox.getAspectRatio();
          if (viewAspectRatio != aspectRatio) {
            if (aspectRatio < viewAspectRatio) {
              final double width = newBoundingBox.getWidth();
              final double height = newBoundingBox.getHeight();
              final double newWidth = height * viewAspectRatio;
              final double expandX = (newWidth - width) / 2;
              newBoundingBox = newBoundingBox.expand(expandX, 0);
            } else if (aspectRatio > viewAspectRatio) {
              final double width = newBoundingBox.getWidth();
              final double height = newBoundingBox.getHeight();
              final double newHeight = width / viewAspectRatio;
              final double expandY = (newHeight - height) / 2;
              newBoundingBox = newBoundingBox.expand(0, expandY);
            }
          }
          final Measurable<Length> viewWidthLength = getViewWidthLength();
          final Measurable<Length> modelWidthLength = newBoundingBox.getWidthLength();
          unitsPerPixel = modelWidthLength.doubleValue(SI.METRE)
            / viewWidthPixels;
          double scale = getScale(viewWidthLength, modelWidthLength);
          if (!this.scales.isEmpty() && viewWidthPixels > 0
            && viewHeightPixels > 0) {
            final double minScale = this.scales.get(this.scales.size() - 1);
            final double maxScale = this.scales.get(0);
            if (scale < minScale) {
              scale = minScale;
              return setBoundingBox(newBoundingBox, scale);
            } else if (scale > maxScale) {
              scale = maxScale;
              return setBoundingBox(newBoundingBox, scale);
            } else {
              // scale = getZoomOutScale(scale);
            }

          }
        }

        internalSetBoundingBox(newBoundingBox, unitsPerPixel);
      }
    }
    return getBoundingBox();
  }

  private BoundingBox setBoundingBox(final BoundingBox boundingBox,
    final double scale) {
    final Point centre = boundingBox.getCentre();
    return setBoundingBox(centre, scale);
  }

  private BoundingBox setBoundingBox(Point centre, final double scale) {
    final double unitsPerPixel = getUnitsPerPixel(scale);
    final GeometryFactory geometryFactory = getGeometryFactory();
    centre = new PointDouble(centre);
    final int viewWidthPixels = getViewWidthPixels();
    final double viewWidth = viewWidthPixels * unitsPerPixel;
    final int viewHeightPixels = getViewHeightPixels();
    final double viewHeight = viewHeightPixels * unitsPerPixel;
    final GeometryFactory precisionModel = GeometryFactory.fixedNoSrid(1 / unitsPerPixel);
    centre = precisionModel.getPreciseCoordinates(centre);
    final double centreX = centre.getX();
    final double centreY = centre.getY();

    double leftOffset = precisionModel.makeXyPrecise(viewWidth / 2);
    if (viewWidthPixels % 2 == 1) {
      leftOffset -= unitsPerPixel;
    }
    final double rightOffset = precisionModel.makeXyPrecise(viewWidth / 2);
    final double topOffset = precisionModel.makeXyPrecise(viewHeight / 2);
    double bottomOffset = precisionModel.makeXyPrecise(viewHeight / 2);
    if (viewHeightPixels % 2 == 1) {
      bottomOffset -= unitsPerPixel;
    }
    final double x1 = centreX - leftOffset;
    final double y1 = centreY - bottomOffset;
    final double x2 = centreX + rightOffset;
    final double y2 = centreY + topOffset;
    final BoundingBox newBoundingBox = new Envelope(geometryFactory, 2, x1, y1,
      x2, y2);
    internalSetBoundingBox(newBoundingBox, unitsPerPixel);
    return newBoundingBox;
  }

  protected void setBoundingBoxInternal(final BoundingBox boundingBox) {
    this.boundingBox = boundingBox;
  }

  /**
   * Set the coordinate system the project is displayed in.
   * 
   * @param coordinateSystem The coordinate system the project is displayed in.
   */
  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    if (!EqualsRegistry.equal(this.geometryFactory, geometryFactory)) {
      final GeometryFactory oldGeometryFactory = this.geometryFactory;
      this.geometryFactory = geometryFactory;
      if (geometryFactory == null) {
        this.geometryFactory2d = null;
      } else {
        this.geometryFactory2d = geometryFactory.convertAxisCount(2);
      }
      this.propertyChangeSupport.firePropertyChange("geometryFactory",
        oldGeometryFactory, geometryFactory);
    }
  }

  public void setInitialized(final boolean initialized) {
    this.initialized = initialized;
  }

  public void setLayerCacheValue(final Layer layer, final String name,
    final Object value) {
    Map<String, Object> cache = layerValueCache.get(layer);
    if (cache == null) {
      cache = new HashMap<String, Object>();
      layerValueCache.put(layer, cache);
    }
    cache.put(name, value);
  }

  public void setScale(final double scale) {
    final double oldValue = getScale();
    if (scale > 0 && Math.abs(oldValue - scale) > 0.0001) {
      setBoundingBox(getBoundingBox(), scale);
      this.propertyChangeSupport.firePropertyChange("scale", oldValue, scale);
    }
  }

  public void setScales(final List<Long> scales) {
    this.scales = scales;
  }

  public boolean setUseModelCoordinates(final boolean useModelCoordinates,
    final Graphics2D graphics) {
    boolean savedUseModelCoordinates = false;
    final AffineTransform previousTransform = this.savedTransform.get();
    if (previousTransform != null) {
      graphics.setTransform(previousTransform);
      savedUseModelCoordinates = true;
    }
    if (useModelCoordinates && this.modelToScreenTransform != null) {
      this.savedTransform.set(graphics.getTransform());
      graphics.transform(this.modelToScreenTransform);
    } else {
      this.savedTransform.remove();
    }
    return savedUseModelCoordinates;
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
    } else {
      convertedValue = value.doubleValue(SI.METRE);
      final CoordinateSystem coordinateSystem = this.geometryFactory2d.getCoordinateSystem();
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
      return GeometryFactory.floating3().point();
    } else {
      final double[] coordinates = toModelCoordinates(viewCoordinates);
      return this.geometryFactory2d.point(coordinates);
    }
  }

  public Point toModelPoint(final GeometryFactory geometryFactory,
    final double... viewCoordinates) {
    final double[] coordinates = toModelCoordinates(viewCoordinates);
    if (Double.isInfinite(coordinates[0]) || Double.isInfinite(coordinates[1])
      || Double.isNaN(coordinates[0]) || Double.isNaN(coordinates[1])) {
      return geometryFactory.point();
    } else {
      final Point point = this.geometryFactory2d.point(coordinates);
      return (Point)point.copy(geometryFactory);
    }
  }

  public Point toModelPoint(final GeometryFactory geometryFactory,
    final java.awt.Point point) {
    final double x = point.getX();
    final double y = point.getY();
    return toModelPoint(geometryFactory, x, y);
  }

  public Point toModelPoint(final GeometryFactory geometryFactory,
    final MouseEvent event) {
    final java.awt.Point eventPoint = event.getPoint();
    return toModelPoint(geometryFactory, eventPoint);
  }

  public Point toModelPoint(final java.awt.Point point) {
    final double x = point.getX();
    final double y = point.getY();
    return toModelPoint(x, y);
  }

  public Point toModelPointRounded(
    com.revolsys.jts.geom.GeometryFactory geometryFactory,
    final java.awt.Point point) {
    final double x = point.getX();
    final double y = point.getY();
    geometryFactory = getRoundedGeometryFactory(geometryFactory);
    return toModelPoint(geometryFactory, x, y);
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
    point = this.geometryFactory2d.project(point);
    final double x = point.getX();
    final double y = point.getY();
    return toViewPoint(x, y);
  }

  public void update() {
  }

}
