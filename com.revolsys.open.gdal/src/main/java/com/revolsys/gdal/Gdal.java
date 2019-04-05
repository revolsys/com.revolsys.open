package com.revolsys.gdal;

import java.awt.Transparency;
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
import org.gdal.gdal.ColorTable;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.ogr.ogr;
import org.gdal.osr.SpatialReference;
import org.gdal.osr.osr;
import org.jeometry.common.logging.Logs;

import com.revolsys.gdal.raster.GdalImageFactory;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.spring.resource.PathResource;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.OS;
import com.revolsys.util.Property;
import com.revolsys.util.ServiceInitializer;

public class Gdal implements ServiceInitializer {
  private static boolean available = false;

  static {
    try {

      final String tempDir = System.getProperty("java.io.tmpdir");
      gdal.SetConfigOption("CPL_TMPDIR", tempDir);

      osr.UseExceptions();
      ogr.UseExceptions();

      String defaultDriverPath = null;
      if (OS.isMac()) {
        defaultDriverPath = "/usr/local/lib/gdalplugins";
      }
      setGdalProperty("GDAL_DRIVER_PATH", defaultDriverPath);
      setGdalProperty("GDAL_DATA", null);

      gdal.SetConfigOption("GDAL_PAM", "Yes");

      gdal.AllRegister();

      ogr.RegisterAll();

      available = true;
    } catch (final UnsatisfiedLinkError e) {
    } catch (final Throwable e) {
      // Logs.debug(Gdal.class, e);
    }
  }

  private static void addGeoreferencedImageFactory(
    final GdalImageFactory georeferencedImageFactory) {
    if (georeferencedImageFactory.isAvailable()) {
      IoFactoryRegistry.addFactory(georeferencedImageFactory);
    }
  }

  private static void addGeoreferencedImageFactory(final String driverName, final String formatName,
    final String fileExtension, final String mimeType) {
    final GdalImageFactory readerSpi = new GdalImageFactory(driverName, formatName, fileExtension,
      mimeType);
    addGeoreferencedImageFactory(readerSpi);
  }

  public static Dataset closeDataSet(final Dataset dataSet) {
    if (dataSet != null) {
      try {
        dataSet.delete();
      } catch (final Throwable e) {
      }
    }
    return null;
  }

  public static BufferedImage getBufferedImage(final Dataset dataset) {
    return getBufferedImage(dataset, -1);
  }

  /**
   * <p>
   * Convert the overview raster from {@link Dataset} to a
   * {@link BufferedImage} . The result image will be the dimensions of the
   * overview raster.
   * </p>
   *
   * @param dataset
   *            The image dataset.
   * @param overviewIndex
   *            The index of the overview raster data. Use -1 for the whole
   *            image.
   * @return The buffered image
   */
  public static BufferedImage getBufferedImage(final Dataset dataset, final int overviewIndex) {
    return getBufferedImage(dataset, overviewIndex, 0, 0, -1, -1, -1, -1);
  }

  /**
   * <p>
   * Convert the overview raster from {@link Dataset} to a
   * {@link BufferedImage} . The raster will be clipped to the
   * sourceOffsetX,sourceOffsetY -> sourceWidth, sourceHeight rectangle. The
   * clip rectangle will be adjusted to fit inside the bounds of the source
   * image. The result image will be the dimensions of sourceWidth,
   * sourceHeight.
   * </p>
   *
   * @param dataset
   *            The image dataset.
   * @param overviewIndex
   *            The index of the overview raster data. Use -1 for the whole
   *            image.
   * @param sourceOffsetX
   *            The x location of the clip rectangle.
   * @param sourceOffsetY
   *            The y location of the clip rectangle.
   * @param sourceWidth
   *            The width of the clip rectangle. Use -1 to auto calculate.
   * @param sourceHeight
   *            The height of the clip rectangle. Use -1 to auto calculate.
   * @return The buffered image.
   */
  public static BufferedImage getBufferedImage(final Dataset dataset, final int overviewIndex,
    final int sourceOffsetX, final int sourceOffsetY, final int sourceWidth,
    final int sourceHeight) {
    return getBufferedImage(dataset, overviewIndex, sourceOffsetX, sourceOffsetY, sourceWidth,
      sourceHeight, -1, -1);
  }

  /**
   * <p>
   * Convert the overview raster from {@link Dataset} to a
   * {@link BufferedImage} . The raster will be clipped to the
   * sourceOffsetX,sourceOffsetY -> sourceWidth, sourceHeight rectangle. The
   * clip rectangle will be adjusted to fit inside the bounds of the source
   * image. The result image will scaled to the dimensions of targetWidth,
   * targetHeight.
   * </p>
   *
   * @param dataset
   *            The image dataset.
   * @param overviewIndex
   *            The index of the overview raster data. Use -1 for the whole
   *            image.
   * @param sourceOffsetX
   *            The x location of the clip rectangle.
   * @param sourceOffsetY
   *            The y location of the clip rectangle.
   * @param sourceWidth
   *            The width of the clip rectangle. Use -1 to auto calculate.
   * @param sourceHeight
   *            The height of the clip rectangle. Use -1 to auto calculate.
   * @param targetWidth
   *            The width of the result image. Use -1 to auto calculate.
   * @param targetHeight
   *            The height of the result image. Use -1 to auto calculate.
   * @return The buffered image.
   */
  public static BufferedImage getBufferedImage(final Dataset dataset, final int overviewIndex,
    int sourceOffsetX, int sourceOffsetY, int sourceWidth, int sourceHeight, int targetWidth,
    int targetHeight) {
    synchronized (dataset) {

      final int bandCount = dataset.getRasterCount();
      final ByteBuffer[] bandData = new ByteBuffer[bandCount];
      final int[] banks = new int[bandCount];
      final int[] offsets = new int[bandCount];

      int pixels = 0;
      int bandDataType = 0;
      int rasterColorInterpretation = -1;
      ColorTable rasterColorTable = null;

      for (int bandIndex = 0; bandIndex < bandCount; bandIndex++) {
        final Band band = dataset.GetRasterBand(bandIndex + 1);
        try {
          Band overviewBand;
          if (overviewIndex == -1) {
            overviewBand = band;
          } else {
            overviewBand = band.GetOverview(overviewIndex);
          }
          try {
            if (rasterColorTable == null) {
              rasterColorTable = band.GetRasterColorTable();
              rasterColorInterpretation = band.GetRasterColorInterpretation();
              bandDataType = band.getDataType();
              final int overviewWidth = overviewBand.getXSize();
              final int overviewHeight = overviewBand.getYSize();
              if (sourceOffsetX < 0) {
                sourceOffsetX = 0;
              }
              if (sourceOffsetY < 0) {
                sourceOffsetY = 0;
              }
              if (sourceOffsetX >= overviewWidth || sourceOffsetY >= overviewHeight) {
                return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
              }

              if (sourceWidth < 0) {
                sourceWidth = overviewWidth;
              }
              if (sourceOffsetX + sourceWidth > overviewWidth) {
                sourceWidth = overviewWidth - sourceOffsetX;
              }
              if (targetWidth < 0) {
                targetWidth = sourceWidth;
              }

              if (sourceHeight < 0) {
                sourceHeight = overviewHeight;
              }
              if (sourceOffsetY + sourceHeight > overviewHeight) {
                sourceHeight = overviewHeight - sourceOffsetY;
              }
              if (targetHeight < 0) {
                targetHeight = sourceHeight;
              }

              pixels = targetWidth * targetHeight;
            }
            if (pixels > 0 && sourceHeight > 0 && sourceWidth > 0) {
              final int bufferSize = pixels * gdal.GetDataTypeSize(bandDataType) / 8;

              final ByteBuffer data = ByteBuffer.allocateDirect(bufferSize);
              data.order(ByteOrder.nativeOrder());

              final int result = overviewBand.ReadRaster_Direct(sourceOffsetX, sourceOffsetY,
                sourceWidth, sourceHeight, targetWidth, targetHeight, bandDataType, data);
              if (result == gdalconstConstants.CE_None) {
                bandData[bandIndex] = data;
              } else {
                throw new RuntimeException("Error converting image");
              }
            } else {
              return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            }
            banks[bandIndex] = bandIndex;
            offsets[bandIndex] = 0;
          } finally {
            overviewBand.delete();
          }
        } finally {
          band.delete();
        }
      }

      DataBuffer imageBuffer = null;
      SampleModel sampleModel = null;
      int dataType = 0;
      int dataBufferType = 0;

      if (bandDataType == gdalconstConstants.GDT_Byte) {
        final byte[][] bytes = new byte[bandCount][];
        for (int bandIndex = 0; bandIndex < bandCount; bandIndex++) {
          bytes[bandIndex] = new byte[pixels];
          bandData[bandIndex].get(bytes[bandIndex]);
        }
        imageBuffer = new DataBufferByte(bytes, pixels);
        dataBufferType = DataBuffer.TYPE_BYTE;
        sampleModel = new BandedSampleModel(dataBufferType, targetWidth, targetHeight, targetWidth,
          banks, offsets);
        dataType = rasterColorInterpretation == gdalconstConstants.GCI_PaletteIndex
          ? BufferedImage.TYPE_BYTE_INDEXED
          : BufferedImage.TYPE_BYTE_GRAY;
      } else if (bandDataType == gdalconstConstants.GDT_Int16) {
        final short[][] shorts = new short[bandCount][];
        for (int bandIndex = 0; bandIndex < bandCount; bandIndex++) {
          shorts[bandIndex] = new short[pixels];
          bandData[bandIndex].asShortBuffer().get(shorts[bandIndex]);
        }
        imageBuffer = new DataBufferShort(shorts, pixels);
        dataBufferType = DataBuffer.TYPE_USHORT;
        sampleModel = new BandedSampleModel(dataBufferType, targetWidth, targetHeight, targetWidth,
          banks, offsets);
        dataType = BufferedImage.TYPE_USHORT_GRAY;
      } else if (bandDataType == gdalconstConstants.GDT_Int32) {
        final int[][] ints = new int[bandCount][];
        for (int bandIndex = 0; bandIndex < bandCount; bandIndex++) {
          ints[bandIndex] = new int[pixels];
          bandData[bandIndex].asIntBuffer().get(ints[bandIndex]);
        }
        imageBuffer = new DataBufferInt(ints, pixels);
        dataBufferType = DataBuffer.TYPE_INT;
        sampleModel = new BandedSampleModel(dataBufferType, targetWidth, targetHeight, targetWidth,
          banks, offsets);
        dataType = BufferedImage.TYPE_CUSTOM;
      }

      final WritableRaster raster = Raster.createWritableRaster(sampleModel, imageBuffer, null);
      BufferedImage image = null;
      ColorModel colorModel = null;

      if (rasterColorInterpretation == gdalconstConstants.GCI_PaletteIndex) {
        dataType = BufferedImage.TYPE_BYTE_INDEXED;
        colorModel = rasterColorTable.getIndexColorModel(gdal.GetDataTypeSize(bandDataType));
        image = new BufferedImage(colorModel, raster, false, null);
      } else {
        ColorSpace colorSpace = null;
        if (bandCount > 2) {
          colorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
          colorModel = new ComponentColorModel(colorSpace, false, false, Transparency.OPAQUE,
            dataBufferType);
          image = new BufferedImage(colorModel, raster, true, null);
        } else {
          image = new BufferedImage(targetWidth, targetHeight, dataType);
          image.setData(raster);
        }
      }
      return image;
    }
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
    final int mode = gdalconstConstants.GA_ReadOnly;
    return getDataset(file, mode);
  }

  public static Dataset getDataset(final File file, final int mode) {
    if (isAvailable()) {
      final String path = file.getAbsolutePath();
      if (file.exists()) {
        final Dataset dataset = gdal.Open(path, mode);
        if (dataset == null) {
          throw new GdalException();
        } else {
          final Resource resource = new PathResource(file);
          setProjectionFromPrjFile(dataset, resource);
          final long modifiedTime = loadSettings(dataset, resource);
          // loadAuxXmlFile(modifiedTime);

          return dataset;
        }
      } else {
        throw new IllegalArgumentException("File no found: " + path);
      }
    } else {
      throw new IllegalStateException("GDAL is not available");
    }
  }

  public static Dataset getDataset(final String fileName) {
    final File file = FileUtil.getFile(fileName);
    return getDataset(file);
  }

  public static Dataset getDataset(final String name, final int mode) {
    if (Property.hasValue(name)) {
      final File file = new File(name);
      return getDataset(file, mode);
    } else {
      throw new IllegalArgumentException("File name must not be null or empty");
    }
  }

  public static GeometryFactory getGeometryFactory(final SpatialReference spatialReference,
    final int axisCount) {
    if (spatialReference == null) {
      return null;
    } else {
      final String wkt = spatialReference.ExportToWkt();
      return GeometryFactory.floating(wkt, axisCount);
    }
  }

  public static SpatialReference getSpatialReference(final GeometryFactory geometryFactory) {
    if (geometryFactory == null) {
      return null;
    } else {
      final int srid = geometryFactory.getHorizontalCoordinateSystemId();
      if (srid <= 0) {
        final String wkt = geometryFactory.toWktCs();
        return new SpatialReference(wkt);
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

  public static String getSpatialReferenceWkt(final int srid) {
    final SpatialReference spatialReference = getSpatialReference(srid);
    return spatialReference.ExportToWkt();
  }

  public static String getVersion() {
    if (Gdal.isAvailable()) {
      return gdal.VersionInfo();
    } else {
      return "0.0.0";
    }
  }

  public static void init() {
  }

  public static boolean isAvailable() {
    return available;
  }

  /**
   * Returns <code>true</code> if a driver for the specific format is
   * available. <code>false</code> otherwise.<BR>
   * It is worth to point out that a successful loading of the native library
   * is not sufficient to grant the support for a specific format. We should
   * also check if the proper driver is available.
   *
   * @return <code>true</code> if a driver for the specific format is
   *         available. <code>false</code> otherwise.<BR>
   */
  public static boolean isDriverAvailable(final String driverName) {
    if (isAvailable()) {
      try {
        final Driver driver = gdal.GetDriverByName(driverName);
        if (driver == null) {
          return false;
        } else {
          return true;
        }
      } catch (final UnsatisfiedLinkError e) {
        Logs.debug(Gdal.class, "Error loading driver: " + driverName, e);
        return false;
      }
    } else {
      return false;
    }
  }

  public static long loadSettings(final Dataset dataset, final Resource resource) {
    final Resource settingsFile = resource.newResourceAddExtension("rgobject");
    if (settingsFile.exists()) {
      try {

        final Map<String, Object> settings = Json.toMap(settingsFile);
        final String boundingBoxWkt = (String)settings.get("boundingBox");
        if (Property.hasValue(boundingBoxWkt)) {
          final BoundingBox boundingBox = BoundingBox.bboxNew(boundingBoxWkt);
          if (!boundingBox.isEmpty()) {
            setSpatialReference(dataset, boundingBox.getGeometryFactory());
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

        return settingsFile.getLastModified();
      } catch (final Throwable e) {
        Logs.error(Gdal.class, "Unable to load:" + settingsFile, e);
        return -1;
      }
    } else {
      return -1;
    }
  }

  private static void setGdalProperty(final String name, final String defaultValue) {
    String value = System.getProperty(name);
    if (!Property.hasValue(value)) {
      value = System.getenv(name);
      if (!Property.hasValue(value)) {
        value = defaultValue;
      }
    }
    if (Property.hasValue(value)) {
      gdal.SetConfigOption(name, value);
    }
  }

  public static void setProjectionFromPrjFile(final Dataset dataset, final Resource resource) {
    final GeometryFactory geometryFactory = GeometryFactory.floating2d(resource);
    if (geometryFactory != null) {
      setSpatialReference(dataset, geometryFactory);
    }
  }

  public static void setSpatialReference(final Dataset dataset,
    final GeometryFactory geometryFactory) {
    final SpatialReference spatialReference = getSpatialReference(geometryFactory);
    if (spatialReference != null) {
      dataset.SetProjection(spatialReference.ExportToWkt());
    }
  }

  @Override
  public void initializeService() {
    addGeoreferencedImageFactory("ECW", "ECW", "ecw", "image/ecw");
    addGeoreferencedImageFactory("JP2ECW", "JPEG 2000", "jp2", "image/jp2");
  }

  @Override
  public int priority() {
    return 200;
  }
}
