package com.revolsys.raster.io.format.tiff;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.common.ImageMetadata.ImageMetadataItem;
import org.apache.commons.imaging.common.bytesource.ByteSource;
import org.apache.commons.imaging.common.bytesource.ByteSourceFile;
import org.apache.commons.imaging.common.bytesource.ByteSourceInputStream;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata.TiffMetadataItem;
import org.apache.commons.imaging.formats.tiff.TiffImageParser;
import org.apache.commons.imaging.formats.tiff.constants.GeoTiffTagConstants;
import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.logging.Logs;
import org.jeometry.coordinatesystem.model.CoordinateOperationMethod;
import org.jeometry.coordinatesystem.model.CoordinateSystem;
import org.jeometry.coordinatesystem.model.GeographicCoordinateSystem;
import org.jeometry.coordinatesystem.model.ParameterName;
import org.jeometry.coordinatesystem.model.ParameterValue;
import org.jeometry.coordinatesystem.model.ParameterValueNumber;
import org.jeometry.coordinatesystem.model.ProjectedCoordinateSystem;
import org.jeometry.coordinatesystem.model.systems.EpsgCoordinateSystems;
import org.jeometry.coordinatesystem.model.unit.LinearUnit;

import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.raster.AbstractGeoreferencedImage;
import com.revolsys.spring.resource.Resource;

public class TiffImage extends AbstractGeoreferencedImage implements GeoTiffConstants {

  public static final int TAG_X_RESOLUTION = 282;

  public static final int TAG_Y_RESOLUTION = 283;

  public static void addDoubleParameter(final Map<ParameterName, ParameterValue> parameters,
    final ParameterName name, final Map<Integer, Object> geoKeys, final int key) {
    final Double value = Maps.getDouble(geoKeys, key);
    if (value != null) {
      parameters.put(name, new ParameterValueNumber(value));
    }
  }

  public static GeometryFactory getGeometryFactory(final Map<Integer, Object> geoKeys) {
    final int projectedCoordinateSystemId = Maps.getInteger(geoKeys, ProjectedCSTypeGeoKey, 0);
    final int geographicCoordinateSystemId = Maps.getInteger(geoKeys, GeographicTypeGeoKey, 0);

    switch (Maps.getInteger(geoKeys, GTModelTypeGeoKey, 0)) {
      case 1: // Projected
        if (projectedCoordinateSystemId <= 0) {
          return null;
        } else if (projectedCoordinateSystemId == 32767) {
          final GeographicCoordinateSystem geographicCoordinateSystem = EpsgCoordinateSystems
            .getCoordinateSystem(geographicCoordinateSystemId);
          final String name = "unknown";
          final CoordinateOperationMethod coordinateOperationMethod = getProjection(geoKeys);

          final Map<ParameterName, ParameterValue> parameters = TiffProjectionParameterName.getProjectionParameters(geoKeys);

          final LinearUnit linearUnit = getLinearUnit(geoKeys);
          final ProjectedCoordinateSystem coordinateSystem = new ProjectedCoordinateSystem(0, name,
            geographicCoordinateSystem, coordinateOperationMethod, parameters, linearUnit);
          final CoordinateSystem epsgCoordinateSystem = EpsgCoordinateSystems
            .getCoordinateSystem(coordinateSystem);
          return GeometryFactory.floating2d(epsgCoordinateSystem.getHorizontalCoordinateSystemId());
        } else {
          return GeometryFactory.floating2d(projectedCoordinateSystemId);
        }

      case 2: // Geographic
        if (geographicCoordinateSystemId <= 0) {
          return null;
        } else if (geographicCoordinateSystemId == 32767) {
          // TODO load from parameters
          return null;
        } else {
          return GeometryFactory.floating2d(geographicCoordinateSystemId);
        }

      case 3: // Geocentric
        return null;

      default:
        return null;
    }

  }

  public static LinearUnit getLinearUnit(final Map<Integer, Object> geoKeys) {
    final int linearUnitId = Maps.getInteger(geoKeys, ProjLinearUnitsGeoKey, 0);
    return EpsgCoordinateSystems.getUnit(linearUnitId);
  }

  public static CoordinateOperationMethod getProjection(final Map<Integer, Object> geoKeys) {
    final int projectionId = Maps.getInteger(geoKeys, ProjCoordTransGeoKey, 0);
    return TiffCoordinateTransformationCode.getCoordinateOperationMethod(projectionId);
  }

  public TiffImage(final Resource imageResource) {
    super("tfw");
    setImageResource(imageResource);

    readImage();

    loadImageMetaData();
    postConstruct();
  }

  @Override
  public void cancelChanges() {
    if (getImageResource() != null) {
      loadImageMetaData();
      setHasChanges(false);
    }
  }

  private double getDouble(final TiffImageMetadata metaData, final int tag,
    final double defaultValue) throws ImageReadException {
    final TiffField field = getTiffField(metaData, tag);
    if (field == null) {
      return defaultValue;
    } else {
      return field.getDoubleValue();
    }
  }

  private Map<Integer, Object> getGeoKeys(final TiffImageMetadata metaData)
    throws ImageReadException {
    final Map<Integer, Object> geoKeys = new LinkedHashMap<>();

    final TiffField keysField = metaData
      .findField(GeoTiffTagConstants.EXIF_TAG_GEO_KEY_DIRECTORY_TAG);
    final TiffField asciiParamsField = metaData
      .findField(GeoTiffTagConstants.EXIF_TAG_GEO_ASCII_PARAMS_TAG);
    final TiffField doubleParamsField = metaData
      .findField(GeoTiffTagConstants.EXIF_TAG_GEO_DOUBLE_PARAMS_TAG);

    double[] doubleParams;
    if (doubleParamsField == null) {
      doubleParams = new double[0];
    } else {
      doubleParams = doubleParamsField.getDoubleArrayValue();
    }
    String asciiParams;
    if (asciiParamsField == null) {
      asciiParams = "";
    } else {
      asciiParams = asciiParamsField.getStringValue();
    }

    if (keysField != null) {
      final int[] keys = keysField.getIntArrayValue();
      for (int i = 4; i < keys.length; i += 4) {
        final int keyId = keys[i];
        final int tiffTag = keys[i + 1];
        final int valueCount = keys[i + 2];
        final int valueOrOffset = keys[i + 3];

        Object value = null;
        switch (tiffTag) {
          case 34736: // DOUBLE
            value = doubleParams[valueOrOffset];
          break;
          case 34737: // ASCII
            value = asciiParams.substring(valueOrOffset, valueOrOffset + valueCount - 1);
          break;

          default:
            value = (short)valueOrOffset;
          break;
        }
        geoKeys.put(keyId, value);
      }

    }
    return geoKeys;
  }

  private TiffField getTiffField(final TiffImageMetadata metaData, final int tag) {
    for (final ImageMetadataItem item : metaData.getItems()) {
      if (item instanceof TiffMetadataItem) {
        final TiffMetadataItem tiffItem = (TiffMetadataItem)item;
        final TiffField field = tiffItem.getTiffField();
        if (field.getTag() == tag) {
          return field;
        }
      }
    }
    return null;
  }

  @Override
  public String getWorldFileExtension() {
    return "tfw";
  }

  @SuppressWarnings("unused")
  private boolean loadGeoTiffMetaData(final TiffImageMetadata metaData) throws ImageReadException {
    try {
      final int xResolution = (int)getDouble(metaData, TAG_X_RESOLUTION, 1);
      final int yResolution = (int)getDouble(metaData, TAG_Y_RESOLUTION, 1);
      setDpi(xResolution, yResolution);
    } catch (final Throwable e) {
      Logs.error(this, e);
    }
    final Map<Integer, Object> geoKeys = getGeoKeys(metaData);
    final GeometryFactory geometryFactory = getGeometryFactory(geoKeys);
    if (geometryFactory != null) {
      setGeometryFactory(geometryFactory);
    }

    final TiffField tiePoints = metaData.findField(GeoTiffTagConstants.EXIF_TAG_MODEL_TIEPOINT_TAG);
    if (tiePoints == null) {
      final TiffField geoTransform = metaData
        .findField(GeoTiffTagConstants.EXIF_TAG_MODEL_TRANSFORMATION_TAG);
      if (geoTransform == null) {
        return false;
      } else {
        final double[] geoTransformValues = geoTransform.getDoubleArrayValue();
        final double x1 = geoTransformValues[3];
        final double y1 = geoTransformValues[7];
        final double pixelWidth = geoTransformValues[0];
        final double pixelHeight = geoTransformValues[5];
        final double xRotation = geoTransformValues[4];
        final double yRotation = geoTransformValues[1];
        setResolution(pixelWidth);
        // TODO rotation
        setBoundingBox(x1, y1, pixelWidth, pixelHeight);
        return true;
      }
    } else {
      final TiffField pixelScale = metaData
        .findField(GeoTiffTagConstants.EXIF_TAG_MODEL_PIXEL_SCALE_TAG);
      if (pixelScale == null) {
        return false;
      } else {
        final double[] tiePointValues = tiePoints.getDoubleArrayValue();
        final double rasterXOffset = tiePointValues[0];
        final double rasterYOffset = tiePointValues[1];
        if (rasterXOffset != 0 && rasterYOffset != 0) {
          // These should be 0, not sure what to do if they are not
          throw new IllegalArgumentException(
            "Exepectig 0 for the raster x,y tie points in a GeoTIFF");
        }

        // Top left corner of image in model coordinates
        final double x1 = tiePointValues[3];
        final double y1 = tiePointValues[4];

        final double[] pixelScaleValues = pixelScale.getDoubleArrayValue();
        final double pixelWidth = pixelScaleValues[0];
        final double pixelHeight = pixelScaleValues[1];
        setResolution(pixelWidth);
        setBoundingBox(x1, y1, pixelWidth, -pixelHeight);
        return true;
      }
    }
  }

  @Override
  protected void loadMetaDataFromImage() {
    try {
      final ByteSource byteSource = newByteSource();
      final TiffImageParser imageParser = new TiffImageParser();
      final TiffImageMetadata metaData = (TiffImageMetadata)imageParser.getMetadata(byteSource);
      loadGeoTiffMetaData(metaData);
    } catch (ImageReadException | IOException e) {
      throw Exceptions.wrap("Unable to open:" + getImageResource(), e);
    }

  }

  private ByteSource newByteSource() {
    ByteSource byteSource;
    final Resource imageResource = getImageResource();
    if (imageResource.isFile()) {
      byteSource = new ByteSourceFile(imageResource.getFile());
    } else {
      final String filename = imageResource.getFilename();
      final InputStream in = imageResource.getInputStream();
      byteSource = new ByteSourceInputStream(in, filename);
    }
    return byteSource;
  }

  private void readImage() {
    final Map<String, Object> params = Collections.emptyMap();
    try {
      final ByteSource byteSource = newByteSource();
      final TiffImageParser imageParser = new TiffImageParser();
      final BufferedImage bufferedImage = imageParser.getBufferedImage(byteSource, params);
      setRenderedImage(bufferedImage);
    } catch (ImageReadException | IOException e) {
      throw Exceptions.wrap("Unable to open:" + getImageResource(), e);
    }
  }
}
