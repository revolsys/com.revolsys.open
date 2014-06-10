package com.revolsys.gdal;

import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.ogr.ogr;
import org.gdal.osr.SpatialReference;
import org.gdal.osr.osr;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.esri.EsriCoordinateSystems;
import com.revolsys.gis.cs.esri.EsriCsWktWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.io.json.JsonMapIoFactory;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.spring.SpringUtil;
import com.revolsys.util.ExceptionUtil;

public class Gdal {
  static {
    osr.UseExceptions();

    gdal.SetConfigOption("GDAL_DRIVER_PATH", "/usr/local/lib/gdalplugins");

    gdal.AllRegister();

    ogr.RegisterAll();
    ogr.UseExceptions();
  }

  public static BufferedImage getBufferedImage(final Dataset dataset) {
    Band band = null;
    final int bandCount = dataset.getRasterCount();
    final ByteBuffer[] bands = new ByteBuffer[bandCount];
    final int[] banks = new int[bandCount];
    final int[] offsets = new int[bandCount];

    final int width = dataset.getRasterXSize();
    final int height = dataset.getRasterYSize();
    final int pixels = width * height;
    int bandDataType = 0;

    for (int bandIndex = 0; bandIndex < bandCount; bandIndex++) {
      /* Bands are not 0-base indexed, so we must add 1 */
      band = dataset.GetRasterBand(bandIndex + 1);

      bandDataType = band.getDataType();
      final int bufferSize = pixels * gdal.GetDataTypeSize(bandDataType) / 8;

      final ByteBuffer data = ByteBuffer.allocateDirect(bufferSize);
      data.order(ByteOrder.nativeOrder());

      final int bandWidth = band.getXSize();
      final int bandHeight = band.getYSize();
      final int result = band.ReadRaster_Direct(0, 0, bandWidth, bandHeight,
        width, height, bandDataType, data);
      if (result == gdalconstConstants.CE_None) {
        bands[bandIndex] = data;
      } else {
        throw new GdalException();
      }
      banks[bandIndex] = bandIndex;
      offsets[bandIndex] = 0;
    }

    DataBuffer imageBuffer = null;
    SampleModel sampleModel = null;
    int dataType = 0;
    int dataBufferType = 0;

    if (bandDataType == gdalconstConstants.GDT_Byte) {
      final byte[][] bytes = new byte[bandCount][];
      for (int bandIndex = 0; bandIndex < bandCount; bandIndex++) {
        bytes[bandIndex] = new byte[pixels];
        bands[bandIndex].get(bytes[bandIndex]);
      }
      imageBuffer = new DataBufferByte(bytes, pixels);
      dataBufferType = DataBuffer.TYPE_BYTE;
      sampleModel = new BandedSampleModel(dataBufferType, width, height, width,
        banks, offsets);
      dataType = (band.GetRasterColorInterpretation() == gdalconstConstants.GCI_PaletteIndex) ? BufferedImage.TYPE_BYTE_INDEXED
        : BufferedImage.TYPE_BYTE_GRAY;
    } else if (bandDataType == gdalconstConstants.GDT_Int16) {
      final short[][] shorts = new short[bandCount][];
      for (int bandIndex = 0; bandIndex < bandCount; bandIndex++) {
        shorts[bandIndex] = new short[pixels];
        bands[bandIndex].asShortBuffer().get(shorts[bandIndex]);
      }
      imageBuffer = new DataBufferShort(shorts, pixels);
      dataBufferType = DataBuffer.TYPE_USHORT;
      sampleModel = new BandedSampleModel(dataBufferType, width, height, width,
        banks, offsets);
      dataType = BufferedImage.TYPE_USHORT_GRAY;
    } else if (bandDataType == gdalconstConstants.GDT_Int32) {
      final int[][] ints = new int[bandCount][];
      for (int bandIndex = 0; bandIndex < bandCount; bandIndex++) {
        ints[bandIndex] = new int[pixels];
        bands[bandIndex].asIntBuffer().get(ints[bandIndex]);
      }
      imageBuffer = new DataBufferInt(ints, pixels);
      dataBufferType = DataBuffer.TYPE_INT;
      sampleModel = new BandedSampleModel(dataBufferType, width, height, width,
        banks, offsets);
      dataType = BufferedImage.TYPE_CUSTOM;
    }

    final WritableRaster raster = Raster.createWritableRaster(sampleModel,
      imageBuffer, null);
    BufferedImage image = null;
    ColorModel colorModel = null;

    if (band.GetRasterColorInterpretation() == gdalconstConstants.GCI_PaletteIndex) {
      dataType = BufferedImage.TYPE_BYTE_INDEXED;
      colorModel = band.GetRasterColorTable().getIndexColorModel(
        gdal.GetDataTypeSize(bandDataType));
      image = new BufferedImage(colorModel, raster, false, null);
    } else {
      ColorSpace colorSpace = null;
      if (bandCount > 2) {
        colorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        colorModel = new ComponentColorModel(colorSpace, false, false,
          ColorModel.OPAQUE, dataBufferType);
        image = new BufferedImage(colorModel, raster, true, null);
      } else {
        image = new BufferedImage(width, height, dataType);
        image.setData(raster);
      }
    }
    return image;
  }

  public static BufferedImage getBufferedImage(final File file) {
    final Dataset dataset = getDataset(file);
    return getBufferedImage(dataset);
  }

  public static BufferedImage getBufferedImage(final String fileName) {
    final File file = FileUtil.getFile(fileName);
    return getBufferedImage(file);
  }

  public static Dataset getDataset(final File file) {
    final Dataset dataset = gdal.Open(file.getAbsolutePath(),
      gdalconst.GA_ReadOnly);
    if (dataset == null) {
      throw new GdalException();
    } else {
      final Resource resource = new FileSystemResource(file);
      setProjectionFromPrjFile(dataset, resource);
      final long modifiedTime = loadSettings(dataset, resource);
      // loadAuxXmlFile(modifiedTime);

      return dataset;
    }
  }

  public static Dataset getDataset(final String fileName) {
    final File file = FileUtil.getFile(fileName);
    return getDataset(file);
  }

  public static SpatialReference getSpatialReference(
    CoordinateSystem coordinateSystem) {
    if (coordinateSystem == null) {
      return null;
    } else {
      final int srid = coordinateSystem.getId();
      if (srid <= 0) {
        coordinateSystem = EsriCoordinateSystems.getCoordinateSystem(coordinateSystem);
        final String wkt = EsriCsWktWriter.toWkt(coordinateSystem);
        final SpatialReference spatialReference = new SpatialReference(wkt);
        return spatialReference;
      } else {
        return getSpatialReference(srid);
      }
    }
  }

  public static SpatialReference getSpatialReference(final int srid) {
    final SpatialReference spatialReference = new SpatialReference("");
    spatialReference.ImportFromEPSG(srid);
    return spatialReference;
  }

  public static void init() {
  }

  public static long loadSettings(final Dataset dataset, final Resource resource) {
    final Resource settingsFile = SpringUtil.addExtension(resource, "rgobject");
    if (settingsFile.exists()) {
      try {
        System.out.println(dataset.GetGCPCount());

        GeometryFactory geometryFactory = null;
        final Map<String, Object> settings = JsonMapIoFactory.toMap(settingsFile);
        final String boundingBoxWkt = (String)settings.get("boundingBox");
        if (StringUtils.hasText(boundingBoxWkt)) {
          final BoundingBox boundingBox = Envelope.create(boundingBoxWkt);
          if (!boundingBox.isEmpty()) {
            geometryFactory = boundingBox.getGeometryFactory();
            setSpatialReference(dataset, boundingBox.getCoordinateSystem());
            final double x = boundingBox.getMinX();
            final double width = boundingBox.getWidth();
            final int imageWidth = dataset.getRasterXSize();
            final double y = boundingBox.getMaxY();
            final double height = boundingBox.getHeight();
            final int imageHeight = dataset.getRasterYSize();
            final double[] transform = new double[] {
              x, width / imageWidth, 0, y, 0, -height / imageHeight
            };
            dataset.SetGeoTransform(transform);
          }
        }

        return SpringUtil.getLastModified(settingsFile);
      } catch (final Throwable e) {
        ExceptionUtil.log(Gdal.class, "Unable to load:" + settingsFile, e);
        return -1;
      }
    } else {
      return -1;
    }
  }

  public static void setProjectionFromPrjFile(final Dataset dataset,
    final Resource resource) {
    final Resource projectionFile = SpringUtil.getResourceWithExtension(
      resource, "prj");
    if (projectionFile.exists()) {
      final CoordinateSystem coordinateSystem = EsriCoordinateSystems.getCoordinateSystem(projectionFile);
      setSpatialReference(dataset, coordinateSystem);
    }
  }

  public static void setSpatialReference(final Dataset dataset,
    final CoordinateSystem coordinateSystem) {
    final SpatialReference spatialReference = getSpatialReference(coordinateSystem);
    if (spatialReference != null) {
      dataset.SetProjection(spatialReference.ExportToWkt());
    }
  }
}
