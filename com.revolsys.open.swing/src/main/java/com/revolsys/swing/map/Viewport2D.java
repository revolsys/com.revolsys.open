package com.revolsys.swing.map;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeSupport;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.measure.Measurable;
import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import com.revolsys.awt.CloseableAffineTransform;
import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.datatype.DataType;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.geometry.model.impl.PointDouble;
import com.revolsys.io.BaseCloseable;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.layer.record.renderer.TextStyleRenderer;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.layer.record.style.TextStyle;
import com.revolsys.swing.map.overlay.MouseOverlay;

public class Viewport2D implements GeometryFactoryProxy, PropertyChangeSupportProxy {

  public static final Geometry EMPTY_GEOMETRY = GeometryFactory.floating3().geometry();

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

  public static double toDisplayValue(final Viewport2D viewport, final Measure<Length> measure) {
    if (viewport == null) {
      return measure.getValue().doubleValue();
    } else {
      return viewport.toDisplayValue(measure);
    }
  }

  public static double toModelValue(final Viewport2D viewport, final Measure<Length> measure) {
    if (viewport == null) {
      return measure.getValue().doubleValue();
    } else {
      return viewport.toModelValue(measure);
    }
  }

  /** The current bounding box of the project. */
  private BoundingBox boundingBox = BoundingBox.EMPTY;

  private GeometryFactory geometryFactory = GeometryFactory.floating3(3857);

  private GeometryFactory geometryFactory2d = GeometryFactory.floating(3857, 2);

  private boolean initialized = false;

  private AffineTransform modelToScreenTransform;

  private double originX;

  private double originY;

  private double pixelsPerXUnit;

  private double pixelsPerYUnit;

  private Reference<Project> project;

  /** The property change listener support. */
  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

  private double scale;

  private List<Long> scales = new ArrayList<Long>();

  private AffineTransform screenToModelTransform;

  private double unitsPerPixel;

  private int viewHeight;

  private int viewWidth;

  public Viewport2D() {
  }

  public Viewport2D(final Project project) {
    this(project, 0, 0, project.getViewBoundingBox());
  }

  public Viewport2D(final Project project, final int width, final int height,
    BoundingBox boundingBox) {
    this.project = new WeakReference<Project>(project);
    this.viewWidth = width;
    this.viewHeight = height;
    GeometryFactory geometryFactory;
    if (boundingBox == null) {
      geometryFactory = GeometryFactory.worldMercator();
      final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
      boundingBox = coordinateSystem.getAreaBoundingBox();
    } else {
      geometryFactory = boundingBox.getGeometryFactory();
      if (geometryFactory == null) {
        geometryFactory = GeometryFactory.worldMercator();
      }
      if (boundingBox.isEmpty()) {
        final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
        if (coordinateSystem != null) {
          boundingBox = coordinateSystem.getAreaBoundingBox();
        }
      }
    }
    setGeometryFactory(geometryFactory);
    setBoundingBox(boundingBox);
  }

  public void drawGeometry(final Geometry geometry, final GeometryStyle style) {
    final Graphics2D graphics = getGraphics();
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    GeometryStyleRenderer.renderGeometry(this, graphics, geometry, style);
  }

  public void drawGeometryOutline(final Geometry geometry, final GeometryStyle style) {
    final Graphics2D graphics = getGraphics();
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    GeometryStyleRenderer.renderGeometryOutline(this, graphics, geometry, style);
  }

  public void drawText(final LayerRecord object, final Geometry geometry, final TextStyle style) {
    final Graphics2D graphics = getGraphics();
    if (graphics != null) {
      TextStyleRenderer.renderText(this, graphics, object, geometry, style);
    }

  }

  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  public BoundingBox getBoundingBox(final GeometryFactory geometryFactory, final int pixels) {
    final int x = MouseOverlay.getEventX();
    final int y = MouseOverlay.getEventY();
    return getBoundingBox(geometryFactory, x, y, pixels);
  }

  public BoundingBox getBoundingBox(final GeometryFactory geometryFactory, final int x, final int y,
    final int pixels) {
    final Point p1 = toModelPoint(geometryFactory, x - pixels, y - pixels);
    final Point p2 = toModelPoint(geometryFactory, x + pixels, y + pixels);
    final BoundingBox boundingBox = new BoundingBoxDoubleGf(geometryFactory, p1, p2);
    return boundingBox;
  }

  public BoundingBox getBoundingBox(final GeometryFactory geometryFactory, final MouseEvent event,
    final int pixels) {
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
  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Deprecated
  public Graphics2D getGraphics() {
    return null;
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

  protected double getPixelsPerYUnit(final double viewHeight, final double mapHeight) {
    return -viewHeight / mapHeight;
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

  public GeometryFactory getRoundedGeometryFactory(GeometryFactory geometryFactory) {
    if (geometryFactory.isProjected()) {
      final double resolution = getUnitsPerPixel();
      if (resolution > 2) {
        geometryFactory = geometryFactory.convertScales(1.0);
      } else {
        geometryFactory = geometryFactory.convertScales(1000.0);
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

  /**
   * Get the scale which dictates if a layer or renderer is visible. This is used when printing
   * to ensure the same layers and renderers are used for printing as is shown on the screen.
   *
   * @return
   */
  public double getScaleForVisible() {
    return getScale();
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

  public boolean isHidden(final AbstractRecordLayer layer, final LayerRecord record) {
    return layer.isHidden(record);
  }

  public boolean isInitialized() {
    return this.initialized;
  }

  public AffineTransform newModelToScreenTransform(final BoundingBox boundingBox,
    final double viewWidth, final double viewHeight) {
    final AffineTransform modelToScreenTransform = new AffineTransform();
    final double mapWidth = boundingBox.getWidth();
    this.pixelsPerXUnit = viewWidth / mapWidth;

    final double mapHeight = boundingBox.getHeight();
    this.pixelsPerYUnit = getPixelsPerYUnit(viewHeight, mapHeight);

    this.originX = boundingBox.getMinX();
    this.originY = boundingBox.getMaxY();

    modelToScreenTransform
      .concatenate(AffineTransform.getScaleInstance(this.pixelsPerXUnit, this.pixelsPerYUnit));
    modelToScreenTransform
      .concatenate(AffineTransform.getTranslateInstance(-this.originX, -this.originY));
    return modelToScreenTransform;
  }

  public void render(final Layer layer) {
    if (layer != null && layer.isExists() && layer.isVisible()) {
      final LayerRenderer<Layer> renderer = layer.getRenderer();
      if (renderer != null) {
        renderer.render(this);
      }
    }
  }

  public BoundingBox setBoundingBox(BoundingBox boundingBox) {
    if (boundingBox != null && !boundingBox.isEmpty()) {
      double unitsPerPixel = 0;
      final GeometryFactory geometryFactory = getGeometryFactory();
      boundingBox = boundingBox.convert(geometryFactory);
      if (!boundingBox.isEmpty()) {
        BoundingBox newBoundingBox = boundingBox;

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
          unitsPerPixel = modelWidthLength.doubleValue(SI.METRE) / viewWidthPixels;
          double scale = getScale(viewWidthLength, modelWidthLength);
          if (!this.scales.isEmpty() && viewWidthPixels > 0 && viewHeightPixels > 0) {
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

        setBoundingBoxDo(newBoundingBox, unitsPerPixel);
      }
    }
    return getBoundingBox();
  }

  private BoundingBox setBoundingBox(final BoundingBox boundingBox, final double scale) {
    final Point centre = boundingBox.getCentre();
    return setCentre(centre, scale);
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

  private void setBoundingBoxDo(final BoundingBox boundingBox, final double unitsPerPixel) {
    final double oldScale = getScale();
    final BoundingBox oldBoundingBox = this.boundingBox;
    synchronized (this) {
      final int viewWidthPixels = getViewWidthPixels();
      final int viewHeightPixels = getViewHeightPixels();
      final Measurable<Length> viewWidthLength = getViewWidthLength();
      final Measurable<Length> modelWidthLength = boundingBox.getWidthLength();

      if (Double.isInfinite(unitsPerPixel) || Double.isNaN(unitsPerPixel)) {
        this.unitsPerPixel = 0;
        setModelToScreenTransform(null);
        this.screenToModelTransform = null;
        this.scale = 0;
      } else {
        this.unitsPerPixel = unitsPerPixel;
        setModelToScreenTransform(
          newModelToScreenTransform(boundingBox, viewWidthPixels, viewHeightPixels));
        this.screenToModelTransform = newScreenToModelTransform(boundingBox, viewWidthPixels,
          viewHeightPixels);
        this.scale = getScale(viewWidthLength, modelWidthLength);
      }
      setBoundingBoxInternal(boundingBox);
    }
    this.propertyChangeSupport.firePropertyChange("boundingBox", oldBoundingBox, boundingBox);
    if (this.scale > 0) {
      this.propertyChangeSupport.firePropertyChange("scale", oldScale, this.scale);
    }
  }

  protected void setBoundingBoxInternal(final BoundingBox boundingBox) {
    this.boundingBox = boundingBox;
  }

  public void setCentre(Point centre) {
    if (centre != null) {
      final GeometryFactory geometryFactory = getGeometryFactory();
      centre = centre.convertGeometry(geometryFactory, 2);
      if (!centre.isEmpty()) {
        final double scale = getScale();
        setCentre(centre, scale);
      }
    }
  }

  private BoundingBox setCentre(Point centre, final double scale) {
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
    final BoundingBox newBoundingBox = new BoundingBoxDoubleGf(geometryFactory, 2, x1, y1, x2, y2);
    setBoundingBoxDo(newBoundingBox, unitsPerPixel);
    return newBoundingBox;
  }

  /**
   * Set the coordinate system the project is displayed in.
   *
   * @param coordinateSystem The coordinate system the project is displayed in.
   */
  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    final GeometryFactory oldGeometryFactory = this.geometryFactory;
    if (setGeometryFactoryDo(geometryFactory)) {
      this.propertyChangeSupport.firePropertyChange("geometryFactory", oldGeometryFactory,
        geometryFactory);
    }
  }

  protected boolean setGeometryFactoryDo(final GeometryFactory geometryFactory) {
    final GeometryFactory oldGeometryFactory = this.geometryFactory;
    if (DataType.equal(oldGeometryFactory, geometryFactory)) {
      return false;
    } else {
      this.geometryFactory = geometryFactory;
      if (geometryFactory == null) {
        this.geometryFactory2d = null;
      } else {
        this.geometryFactory2d = geometryFactory.convertAxisCount(2);
      }
      return true;
    }
  }

  public void setInitialized(final boolean initialized) {
    this.initialized = initialized;
  }

  protected void setModelToScreenTransform(final AffineTransform modelToScreenTransform) {
    this.modelToScreenTransform = modelToScreenTransform;
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

  public BaseCloseable setUseModelCoordinates(final boolean useModelCoordinates) {
    return null;
  }

  public BaseCloseable setUseModelCoordinates(final Graphics2D graphics,
    final boolean useModelCoordinates) {
    if (useModelCoordinates) {
      final CloseableAffineTransform transform = new CloseableAffineTransform(graphics,
        graphics.getTransform());
      final AffineTransform modelToScreenTransform = getModelToScreenTransform();
      transform.concatenate(modelToScreenTransform);
      return transform;
    }
    return null;
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
    if (unit.equals(NonSI.PIXEL)) {
      convertedValue = value.doubleValue(NonSI.PIXEL);
    } else {
      convertedValue = value.doubleValue(SI.METRE);
      final CoordinateSystem coordinateSystem = this.geometryFactory2d.getCoordinateSystem();
      if (coordinateSystem instanceof GeographicCoordinateSystem) {
        final GeographicCoordinateSystem geoCs = (GeographicCoordinateSystem)coordinateSystem;
        final double radius = geoCs.getDatum().getSpheroid().getSemiMajorAxis();
        convertedValue = Math.toDegrees(convertedValue / radius);

      }
      final double modelUnitsPerViewUnit = getModelUnitsPerViewUnit();
      convertedValue = convertedValue / modelUnitsPerViewUnit;
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

  public Point toModelPoint(final GeometryFactory geometryFactory, final java.awt.Point point) {
    final double x = point.getX();
    final double y = point.getY();
    return toModelPoint(geometryFactory, x, y);
  }

  public Point toModelPoint(final GeometryFactory geometryFactory, final MouseEvent event) {
    final java.awt.Point eventPoint = event.getPoint();
    return toModelPoint(geometryFactory, eventPoint);
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

  public double toModelValue(final Measure<Length> value) {
    double convertedValue;
    final Unit<Length> unit = value.getUnit();
    if (unit.equals(NonSI.PIXEL)) {
      convertedValue = value.doubleValue(NonSI.PIXEL);
      final double modelUnitsPerViewUnit = getModelUnitsPerViewUnit();
      convertedValue *= modelUnitsPerViewUnit;
    } else {
      convertedValue = value.doubleValue(SI.METRE);
      final CoordinateSystem coordinateSystem = this.geometryFactory2d.getCoordinateSystem();
      if (coordinateSystem instanceof GeographicCoordinateSystem) {
        final GeographicCoordinateSystem geoCs = (GeographicCoordinateSystem)coordinateSystem;
        final double radius = geoCs.getDatum().getSpheroid().getSemiMajorAxis();
        convertedValue = Math.toDegrees(convertedValue / radius);

      }
    }
    return convertedValue;
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
