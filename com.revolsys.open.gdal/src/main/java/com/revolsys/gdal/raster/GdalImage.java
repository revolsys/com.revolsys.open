package com.revolsys.gdal.raster;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.springframework.core.io.Resource;

import com.revolsys.gdal.Gdal;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.cs.esri.EsriCoordinateSystems;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.raster.AbstractGeoReferencedImage;

public class GdalImage extends AbstractGeoReferencedImage {

  private Dataset dataset;

  public GdalImage(final GdalImageFactory readerSpi,
    final Resource imageResource) {
    setImageResource(imageResource);
    final Dataset dataset = getDataset();
    final Band band = dataset.GetRasterBand(1);
    for (int i = 0; i < band.GetOverviewCount(); i++) {
      final Band overview = band.GetOverview(i);
      final int overviewWidth = overview.getXSize();
      final int overviewHeight = overview.getYSize();
      addOverviewSize(overviewWidth, overviewHeight);
    }

    final String projection = dataset.GetProjection();
    final double[] geoTransform = dataset.GetGeoTransform();
    if (projection != null) {
      final CoordinateSystem esriCoordinateSystem = EsriCoordinateSystems.getCoordinateSystem(projection);
      if (esriCoordinateSystem != null) {
        CoordinateSystem epsgCoordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(esriCoordinateSystem);
        if (epsgCoordinateSystem == null) {
          epsgCoordinateSystem = esriCoordinateSystem;
        }
        final int srid = epsgCoordinateSystem.getId();
        if (srid > 0 && srid < 2000000) {
          setGeometryFactory(GeometryFactory.floating(srid, 2));
        } else {
          setGeometryFactory(GeometryFactory.fixed(epsgCoordinateSystem, 2, -1));
        }
      }
    }
    setBoundingBox(geoTransform[0], geoTransform[3], geoTransform[1],
      geoTransform[5]);
    postConstruct();
  }

  @Override
  public void drawImage(final Graphics2D graphics,
    final BoundingBox viewBoundingBox, final int viewWidth,
    final int viewHeight, final boolean useTransform) {
    try {
      final Dataset dataset = getDataset();

      final BoundingBox imageBoundingBox = getBoundingBox();
      final BoundingBox clipBoundingBox = viewBoundingBox.intersection(imageBoundingBox);
      final double scaleFactor = viewWidth / viewBoundingBox.getWidth();

      final double clipModelWidth = clipBoundingBox.getWidth();
      final int targetWidth = (int)Math.ceil(clipModelWidth * scaleFactor);

      final double clipModelHeight = clipBoundingBox.getHeight();
      final int targetHeight = (int)Math.ceil(clipModelHeight * scaleFactor);

      int bestOverviewIdx = -1;

      int srcWidth = getImageWidth();
      final double clipResolution = Math.abs(clipBoundingBox.getHeight()
        / targetHeight);
      final List<Dimension> overviewSizes = getOverviewSizes();
      for (int i = 0; i < overviewSizes.size(); i++) {
        final Dimension overviewSize = overviewSizes.get(i);
        final int width = overviewSize.width;
        final int height = overviewSize.height;

        if (0 != height && 0 != width) {
          final double overviewResolution = Math.abs(imageBoundingBox.getHeight()
            / height);
          if (overviewResolution <= clipResolution) {
            bestOverviewIdx = i;
            srcWidth = width;
          }
        }
      }

      final double scale = srcWidth / imageBoundingBox.getWidth();
      final int clipXoff = (int)Math.floor((clipBoundingBox.getMinX() - imageBoundingBox.getMinX())
        * scale);
      final int clipYoff = (int)Math.floor((imageBoundingBox.getMaxY() - clipBoundingBox.getMaxY())
        * scale);
      final int clipWidth = (int)Math.ceil(clipModelWidth * scale);
      final int clipHeight = (int)Math.ceil(clipModelHeight * scale);
      final BufferedImage bufferedImage = Gdal.getBufferedImage(dataset,
        bestOverviewIdx, clipXoff, clipYoff, clipWidth, clipHeight,
        targetWidth, targetHeight);

      super.drawRenderedImage(bufferedImage, clipBoundingBox, graphics,
        viewBoundingBox, viewWidth, useTransform);

    } catch (final Throwable e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    this.dataset = Gdal.closeDataSet(this.dataset);
  }

  public synchronized Dataset getDataset() {
    if (this.dataset == null) {
      final File file = getFile();
      this.dataset = Gdal.getDataset(file);
    }
    return this.dataset;
  }

  @Override
  public int getImageHeight() {
    return getDataset().getRasterYSize();
  }

  @Override
  public int getImageWidth() {
    return getDataset().getRasterXSize();
  }
}
