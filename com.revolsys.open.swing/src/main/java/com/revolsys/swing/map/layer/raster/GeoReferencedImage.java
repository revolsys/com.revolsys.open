package com.revolsys.swing.map.layer.raster;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.IOException;

import org.springframework.core.io.Resource;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.WktCsParser;
import com.revolsys.gis.cs.esri.EsriCoordinateSystems;
import com.revolsys.spring.SpringUtil;
import com.revolsys.swing.map.layer.MapTile;

public class GeoReferencedImage {

  private BoundingBox boundingBox;

  private Image image;

  private int imageWidth = -1;

  private int imageHeight = -1;

  private GeometryFactory geometryFactory = GeometryFactory.getFactory();

  public GeoReferencedImage() {
  }

  public GeoReferencedImage(final BoundingBox boundingBox,
    final int imageWidth, final int imageHeight) {
    this.boundingBox = boundingBox;
    this.imageWidth = imageWidth;
    this.imageHeight = imageHeight;
  }

  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof MapTile) {
      final MapTile tile = (MapTile)obj;
      return tile.getBoundingBox().equals(boundingBox);
    }
    return false;
  }

  public BoundingBox getBoundingBox() {
    return boundingBox;
  }

  public CoordinateSystem getCoordinateSystem() {
    return geometryFactory.getCoordinateSystem();
  }

  public Image getImage() {
    return image;
  }

  public int getImageHeight() {
    if (imageHeight == -1) {
      imageHeight = image.getHeight(null);
    }
    return imageHeight;
  }

  public int getImageWidth() {
    if (imageWidth == -1) {
      imageWidth = image.getWidth(null);
    }
    return imageWidth;
  }

  @Override
  public int hashCode() {
    return boundingBox.hashCode();
  }

  public Image loadImage() {
    return image;
  }

  protected void loadProjectionFile(final Resource resource) {
    final Resource projectionFile = SpringUtil.getResourceWithExtension(
      resource, "prj");
    if (projectionFile.exists()) {
      CoordinateSystem coordinateSystem = new WktCsParser(projectionFile).parse();
      coordinateSystem = EsriCoordinateSystems.getCoordinateSystem(coordinateSystem);
      setCoordinateSystem(coordinateSystem);
    }
  }

  protected void loadWorldFile(final Resource resource,
    final String worldFileExtension) {

    final Resource worldFile = SpringUtil.getResourceWithExtension(resource,
      worldFileExtension);
    if (worldFile.exists()) {
      try {
        final BufferedReader reader = SpringUtil.getBufferedReader(resource);
        try {
          final double xPixelSize = Double.parseDouble(reader.readLine());
          final double yRotation = Double.parseDouble(reader.readLine());
          final double xRotation = Double.parseDouble(reader.readLine());
          final double yPixelSize = Double.parseDouble(reader.readLine());
          final double xCoordinate = Double.parseDouble(reader.readLine());
          final double yCoordinate = Double.parseDouble(reader.readLine());

          final double[] transformationMatrix = {
            xPixelSize, yRotation, xRotation, yPixelSize, xCoordinate,
            yCoordinate
          };
          System.out.println(transformationMatrix);
          // setTransformationMatrix(transformationMatrix);
          // final BoundingBox boundingBox = new
          // BoundingBox(getCoordinateSystem());
        } finally {
          reader.close();
        }
      } catch (final IOException e) {
        throw new RuntimeException(
          "Error reading TIFF world file " + worldFile, e);
      }
    } else {
      throw new IllegalArgumentException("Cannot find world file " + worldFile);
    }
  }

  protected void setBoundingBox(final BoundingBox boundingBox) {
    this.boundingBox = boundingBox;
  }

  public void setCoordinateSystem(final CoordinateSystem coordinateSystem) {
    setGeometryFactory(GeometryFactory.getFactory(coordinateSystem));
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public void setImage(final Image image) {
    this.image = image;
  }

  protected void setImageHeight(final int imageHeight) {
    this.imageHeight = imageHeight;
  }

  protected void setImageWidth(final int imageWidth) {
    this.imageWidth = imageWidth;
  }
}
