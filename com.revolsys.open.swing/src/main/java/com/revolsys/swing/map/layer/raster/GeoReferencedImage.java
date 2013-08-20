package com.revolsys.swing.map.layer.raster;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;

import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.esri.EsriCoordinateSystems;
import com.revolsys.spring.SpringUtil;
import com.revolsys.swing.map.layer.MapTile;

public class GeoReferencedImage {

  private BoundingBox boundingBox;

  private BufferedImage image;

  private int imageWidth = -1;

  private int imageHeight = -1;

  private GeometryFactory geometryFactory = GeometryFactory.getFactory();

  private PlanarImage jaiImage;

  private Resource imageResource;

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
  }

  public GeoReferencedImage(final Resource imageResource) {
    this.imageResource = imageResource;
    setImage(createBufferedImage());
    loadImageMetaData();
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
    return this.image;
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

  @Override
  public int hashCode() {
    return this.boundingBox.hashCode();
  }

  public BufferedImage loadImage() {
    return this.image;
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

  public void revert() {
    if (this.imageResource != null) {
      loadImageMetaData();
    }
  }

  protected void setBoundingBox(final BoundingBox boundingBox) {
    this.geometryFactory = boundingBox.getGeometryFactory();
    this.boundingBox = boundingBox;
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
}
