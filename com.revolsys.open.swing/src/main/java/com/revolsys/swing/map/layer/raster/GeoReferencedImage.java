package com.revolsys.swing.map.layer.raster;

import java.awt.image.BufferedImage;
import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;

import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.revolsys.beans.AbstractPropertyChangeObject;
import com.revolsys.collection.PropertyChangeArrayList;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.esri.EsriCoordinateSystems;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.spring.SpringUtil;
import com.revolsys.swing.map.layer.MapTile;
import com.revolsys.swing.map.layer.raster.filter.WarpAffineFilter;
import com.revolsys.swing.map.layer.raster.filter.WarpFilter;
import com.revolsys.swing.map.overlay.MappedLocation;
import com.revolsys.util.Property;

public class GeoReferencedImage extends AbstractPropertyChangeObject implements
  PropertyChangeListener {

  private BoundingBox boundingBox;

  private BufferedImage image;

  private int imageWidth = -1;

  private int imageHeight = -1;

  private GeometryFactory geometryFactory = GeometryFactory.getFactory();

  private PlanarImage jaiImage;

  private Resource imageResource;

  private double resolution;

  private final Map<CoordinateSystem, GeoReferencedImage> projectedImages = new HashMap<CoordinateSystem, GeoReferencedImage>();

  private final PropertyChangeArrayList<MappedLocation> tiePoints = new PropertyChangeArrayList<MappedLocation>();

  private WarpFilter warpFilter;

  private BufferedImage warpedImage;

  private final int degree = 1;

  public GeoReferencedImage(final BoundingBox boundingBox,
    final BufferedImage image) {
    this(boundingBox, image.getWidth(), image.getHeight());
    setImage(image);
  }

  public GeoReferencedImage(final BoundingBox boundingBox,
    final int imageWidth, final int imageHeight) {
    this.boundingBox = boundingBox;
    this.geometryFactory = boundingBox.getGeometryFactory();
    this.imageWidth = imageWidth;
    this.imageHeight = imageHeight;
    Property.addListener(tiePoints, this);
  }

  public GeoReferencedImage(final Resource imageResource) {
    this.imageResource = imageResource;
    setImage(createBufferedImage());
    loadImageMetaData();
    Property.addListener(tiePoints, this);
  }

  protected synchronized void clearWarp() {
    this.warpedImage = null;
    this.warpFilter = null;
  }

  protected BufferedImage createBufferedImage() {
    final File file = SpringUtil.getOrDownloadFile(this.imageResource);
    this.jaiImage = JAI.create("fileload", file.getAbsolutePath());
    return this.jaiImage.getAsBufferedImage();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof MapTile) {
      final MapTile tile = (MapTile)obj;
      return tile.getBoundingBox().equals(this.boundingBox);
    }
    return false;
  }

  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  public CoordinateSystem getCoordinateSystem() {
    return this.geometryFactory.getCoordinateSystem();
  }

  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  public BufferedImage getImage() {
    if (getWarpFilter() == null) {
      return this.image;
    } else {
      return getWarpedImage();
    }
  }

  public GeoReferencedImage getImage(final CoordinateSystem coordinateSystem) {
    synchronized (this.projectedImages) {
      if (coordinateSystem.equals(getCoordinateSystem())) {
        return this;
      } else {
        GeoReferencedImage projectedImage = this.projectedImages.get(coordinateSystem);
        if (projectedImage == null) {
          projectedImage = getImage(coordinateSystem, this.resolution);
          this.projectedImages.put(coordinateSystem, projectedImage);
        }
        return projectedImage;
      }
    }
  }

  public GeoReferencedImage getImage(final CoordinateSystem coordinateSystem,
    final double resolution) {
    final int imageSrid = getGeometryFactory().getSRID();
    if (imageSrid > 0 && imageSrid != coordinateSystem.getId()) {
      final BoundingBox boundingBox = getBoundingBox();
      final ProjectionImageFilter filter = new ProjectionImageFilter(
        boundingBox, coordinateSystem, resolution);

      final BufferedImage newImage = filter.filter(getImage());

      final BoundingBox destBoundingBox = filter.getDestBoundingBox();
      return new GeoReferencedImage(destBoundingBox, newImage);
    }
    return this;
  }

  public GeoReferencedImage getImage(final GeometryFactory geometryFactory) {
    final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
    return getImage(coordinateSystem);
  }

  public double getImageAspectRatio() {
    final int imageWidth = getImageWidth();
    final int imageHeight = getImageHeight();
    if (imageWidth > 0 && imageHeight > 0) {
      return (double)imageWidth / imageHeight;
    } else {
      return 0;
    }
  }

  public int getImageHeight() {
    if (this.imageHeight == -1) {
      this.imageHeight = this.image.getHeight();
    }
    return this.imageHeight;
  }

  public Resource getImageResource() {
    return this.imageResource;
  }

  public int getImageWidth() {
    if (this.imageWidth == -1) {
      this.imageWidth = this.image.getWidth();
    }
    return this.imageWidth;
  }

  public PlanarImage getJaiImage() {
    if (this.jaiImage == null && this.image != null) {
      this.jaiImage = PlanarImage.wrapRenderedImage(this.image);
    }
    return this.jaiImage;
  }

  public BufferedImage getOriginalImage() {
    return image;
  }

  public WarpFilter getOriginalWarpFilter() {
    return new WarpAffineFilter(getBoundingBox(), getImageWidth(),
      getImageHeight());
  }

  public double getResolution() {
    return resolution;
  }

  public List<MappedLocation> getTiePoints() {
    return tiePoints;
  }

  public synchronized BufferedImage getWarpedImage() {
    if (image == null || boundingBox.isEmpty()) {
      return this.image;
    } else if (this.warpedImage == null) {
      final WarpFilter warpFilter = getWarpFilter();
      this.warpedImage = warpFilter.filter(image);
    }
    return warpedImage;
  }

  public synchronized WarpFilter getWarpFilter() {
    if (this.image != null && this.warpFilter == null) {
      final int imageWidth = image.getWidth();
      final int imageHeight = image.getHeight();
      if (this.boundingBox.isEmpty()) {
        this.warpFilter = new WarpAffineFilter(boundingBox, imageWidth,
          imageHeight);
      } else {
        final List<MappedLocation> tiePoints = getTiePoints();
        this.warpFilter = WarpFilter.createWarpFilter(boundingBox, tiePoints,
          this.degree, imageWidth, imageHeight);
      }
    }
    return warpFilter;
  }

  @Override
  public int hashCode() {
    return this.boundingBox.hashCode();
  }

  protected void loadImageMetaData() {
  }

  protected void loadProjectionFile(final Resource resource) {
    final Resource projectionFile = SpringUtil.getResourceWithExtension(
      resource, "prj");
    if (projectionFile.exists()) {
      final CoordinateSystem coordinateSystem = EsriCoordinateSystems.getCoordinateSystem(projectionFile);
      setCoordinateSystem(coordinateSystem);
    }
  }

  @SuppressWarnings("unused")
  protected void loadWorldFile(final Resource resource,
    final String worldFileExtension) {

    final Resource worldFile = SpringUtil.getResourceWithExtension(resource,
      worldFileExtension);
    if (worldFile.exists()) {
      try {
        final BufferedReader reader = SpringUtil.getBufferedReader(worldFile);
        try {
          final double pixelWidth = Double.parseDouble(reader.readLine());
          final double yRotation = Double.parseDouble(reader.readLine());
          final double xRotation = Double.parseDouble(reader.readLine());
          final double pixelHeight = Double.parseDouble(reader.readLine());
          // Top left
          final double x1 = Double.parseDouble(reader.readLine());
          final double y1 = Double.parseDouble(reader.readLine());
          setResolution(pixelWidth);

          // TODO rotation
          setBoundingBox(x1, y1, pixelWidth, pixelHeight);
        } finally {
          reader.close();
        }
      } catch (final IOException e) {
        LoggerFactory.getLogger(getClass()).error(
          "Error reading world file " + worldFile, e);
      }
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    if (source == tiePoints) {
      if (event instanceof IndexedPropertyChangeEvent) {
        final Object oldValue = event.getOldValue();
        if (oldValue instanceof MappedLocation) {
          ((MappedLocation)oldValue).removeListener(this);
        }
        final Object newValue = event.getOldValue();
        if (newValue instanceof MappedLocation) {
          ((MappedLocation)newValue).addListener(this);
        }
      }
      clearWarp();
    } else if (source instanceof MappedLocation) {
      clearWarp();
    }
    firePropertyChange(event);
  }

  public void revert() {
    if (this.imageResource != null) {
      loadImageMetaData();
    }
  }

  public void setBoundingBox(final BoundingBox boundingBox) {
    this.geometryFactory = boundingBox.getGeometryFactory();
    this.boundingBox = boundingBox;
    clearWarp();
  }

  public void setBoundingBox(final double x1, final double y1,
    final double pixelWidth, final double pixelHeight) {
    final GeometryFactory geometryFactory = getGeometryFactory();

    final int imageWidth = getImageWidth();
    final double x2 = x1 + pixelWidth * imageWidth;

    final int imageHeight = getImageHeight();
    final double y2 = y1 - pixelHeight * imageHeight;
    final BoundingBox boundingBox = new BoundingBox(geometryFactory, x1, y1,
      x2, y2);
    setBoundingBox(boundingBox);
  }

  public void setCoordinateSystem(final CoordinateSystem coordinateSystem) {
    setGeometryFactory(GeometryFactory.getFactory(coordinateSystem));
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public void setImage(final BufferedImage image) {
    this.image = image;
  }

  protected void setImageHeight(final int imageHeight) {
    this.imageHeight = imageHeight;
  }

  protected void setImageWidth(final int imageWidth) {
    this.imageWidth = imageWidth;
  }

  public void setJaiImage(final RenderedOp jaiImage) {
    this.jaiImage = jaiImage;
  }

  protected void setResolution(final double resolution) {
    this.resolution = resolution;
  }

  public void setTiePoints(final List<MappedLocation> tiePoints) {
    if (!EqualsRegistry.equal(tiePoints, this.tiePoints)) {
      for (final MappedLocation mappedLocation : this.tiePoints) {
        mappedLocation.removeListener(this);
      }
      this.tiePoints.clear();
      this.tiePoints.addAll(tiePoints);
      for (final MappedLocation mappedLocation : tiePoints) {
        mappedLocation.addListener(this);
      }
      clearWarp();
    }
  }
}
