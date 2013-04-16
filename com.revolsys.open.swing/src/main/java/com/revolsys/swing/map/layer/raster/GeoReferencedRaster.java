package com.revolsys.swing.map.layer.raster;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;

public abstract class GeoReferencedRaster {
  private final String fileName;

  private RenderedOp image;

  private GeometryFactory geometryFactory = GeometryFactory.getFactory();

  private BoundingBox modelEnvelope;

  private double[] min;

  private double[] max;

  private Coordinates topLeftRasterPoint;

  private Coordinates topLeftModelPoint;

  private double xModelUnitsPerRasterUnit;

  private double yModelUnitsPerRasterUnit;

  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  public GeoReferencedRaster(final String fileName) {
    this.fileName = fileName;
  }

  public RenderedOp fullContrast() {
    final int bands = image.getNumBands();
    final double[] constants = new double[bands];
    final double[] offsets = new double[bands];
    for (int i = 0; i < bands; i++) {
      constants[i] = 1.2 * 255 / (max[i] - min[i]);
      offsets[i] = 255 * min[i] / (min[i] - max[i]);
    }

    final ParameterBlock pb = new ParameterBlock();
    pb.addSource(image);
    pb.add(constants);
    pb.add(offsets);
    return JAI.create("rescale", pb, null);
  }

  public BoundingBox getEnvelope() {
    return modelEnvelope;
  }

  public String getFileName() {
    return fileName;
  }

  public RenderedOp getImage() {
    synchronized (this) {
      if (image == null) {
        loadImage();
        loadImageMetaData();
        normalizeImage(image);
      }
      return image;
    }
  }

  public double[] getMaximumExtreme() {
    return max;
  }

  public double[] getMinimumExtreme() {
    return min;
  }

  public Coordinates getTopLeftModelPoint() {
    return topLeftModelPoint;
  }

  public Coordinates getTopLeftRasterPoint() {
    return topLeftRasterPoint;
  }

  public double getXModelUnitsPerRasterUnit() {
    return xModelUnitsPerRasterUnit;
  }

  public double getYModelUnitsPerRasterUnit() {
    return yModelUnitsPerRasterUnit;
  }

  protected void loadImage() {
    image = JAI.create("fileload", fileName);
  }

  protected void loadImageMetaData() {
  }

  protected void normalizeImage(final RenderedOp image) {
  }

  private Coordinates rasterToModelSpace(final Coordinates point) {
    final double x = topLeftModelPoint.getX()
      + (point.getX() - topLeftRasterPoint.getX()) * xModelUnitsPerRasterUnit;
    final double y = topLeftModelPoint.getY()
      - (point.getY() + topLeftRasterPoint.getY()) * yModelUnitsPerRasterUnit;

    return new DoubleCoordinates(x, y);
  }

  public void setAffineTransformation(final AffineTransform transform) {
    final double scaleX = Math.abs(transform.getScaleX());
    final double scaleY = Math.abs(transform.getScaleY());

    setXModelUnitsPerRasterUnit(scaleX);
    setYModelUnitsPerRasterUnit(scaleY);

    final Point2D rasterLT = new Point2D.Double(image.getMinX(),
      image.getMinY());
    final Point2D modelLT = new Point2D.Double();
    transform.transform(rasterLT, modelLT);

    setTopLeftRasterPoint(new DoubleCoordinates(rasterLT.getX(),
      rasterLT.getY()));
    setTopLeftModelPoint(new DoubleCoordinates(modelLT.getX(), modelLT.getY()));
  }

  public void setGeometryFactory(GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
    this.modelEnvelope = modelEnvelope.convert(geometryFactory);
  }

  protected void setEnvelope() {
    final Coordinates coorRaster_imageLB = new DoubleCoordinates(
      topLeftRasterPoint.getX(), image.getHeight() - 1.0);
    final Coordinates coorRaster_imageRT = new DoubleCoordinates(
      image.getWidth() - 1.0, 0);
    final Coordinates coorModel_imageLB = rasterToModelSpace(coorRaster_imageLB);
    final Coordinates coorModel_imageRT = rasterToModelSpace(coorRaster_imageRT);

    GeometryFactory geometryFactory = getGeometryFactory();
    modelEnvelope = new BoundingBox(geometryFactory, coorModel_imageLB,
      coorModel_imageRT);
  }

  public void setTopLeftModelPoint(final Coordinates topLeftModelPoint) {
    this.topLeftModelPoint = topLeftModelPoint;
    setEnvelope();
  }

  public void setTopLeftRasterPoint(final Coordinates topLeftRasterPoint) {
    this.topLeftRasterPoint = topLeftRasterPoint;
  }

  public void setXModelUnitsPerRasterUnit(final double xModelUnitsPerRasterUnit) {
    this.xModelUnitsPerRasterUnit = xModelUnitsPerRasterUnit;
  }

  public void setYModelUnitsPerRasterUnit(final double yModelUnitsPerRasterUnit) {
    this.yModelUnitsPerRasterUnit = yModelUnitsPerRasterUnit;
    setEnvelope();
  }
}
