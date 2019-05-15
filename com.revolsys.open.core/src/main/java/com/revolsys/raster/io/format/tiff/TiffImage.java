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
import org.jeometry.coordinatesystem.model.ParameterNames;
import org.jeometry.coordinatesystem.model.ParameterValue;
import org.jeometry.coordinatesystem.model.ParameterValueNumber;
import org.jeometry.coordinatesystem.model.ProjectedCoordinateSystem;
import org.jeometry.coordinatesystem.model.systems.EpsgCoordinateSystems;
import org.jeometry.coordinatesystem.model.unit.LinearUnit;

import com.revolsys.collection.map.IntHashMap;
import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.raster.AbstractGeoreferencedImage;
import com.revolsys.spring.resource.Resource;

public class TiffImage extends AbstractGeoreferencedImage {
  /** ProjFalseEastingGeoKey (1024) */
  public static final int MODEL_TYPE_KEY = 1024;

  /** ProjFalseEastingGeoKey (3082) */
  public static final int FALSE_EASTING_KEY = 3082;

  /** ProjFalseNorthingGeoKey (3083) */
  public static final int FALSE_NORTHING_KEY = 3083;

  /** GeographicTypeGeoKey (2048) */
  public static final int GEOGRAPHIC_COORDINATE_SYSTEM_ID = 2048;

  /** ProjNatOriginLatGeoKey (3081) */
  public static final int LATITUDE_OF_CENTER_2_KEY = 3081;

  /** ProjLinearUnitsGeoKey (3076) */
  public static final int LINEAR_UNIT_ID = 3076;

  /** ProjNatOriginLongGeoKey (3080) */
  public static final int LONGITUDE_OF_CENTER_2_KEY = 3080;

  /** ProjectedCSTypeGeoKey (3072) */
  public static final int PROJECTED_COORDINATE_SYSTEM_ID = 3072;

  /** ProjCoordTransGeoKey (3075) */
  public static final int PROJECTION_ID = 3075;

  private static IntHashMap<CoordinateOperationMethod> PROJECTION_BY_ID = new IntHashMap<>();

  /** ProjStdParallel1GeoKey (3078) */
  public static final int STANDARD_PARALLEL_1_KEY = 3078;

  /** ProjStdParallel2GeoKey (3079) */
  public static final int STANDARD_PARALLEL_2_KEY = 3079;

  public static final int TAG_X_RESOLUTION = 282;

  public static final int TAG_Y_RESOLUTION = 283;

  static {
    // try {
    // final OperationRegistry reg =
    // JAI.getDefaultInstance().getOperationRegistry();
    // ImageCodec.unregisterCodec("tiff");
    // reg.unregisterOperationDescriptor("tiff");
    //
    // ImageCodec.registerCodec(new XTIFFCodec());
    // final XTIFFDescriptor descriptor = new XTIFFDescriptor();
    //
    // reg.registerDescriptor(descriptor);
    //
    // } catch (final Throwable t) {
    // }

    addProjection(1, "Transverse_Mercator");
    // CT_TransvMercator_Modified_Alaska = 2
    // CT_ObliqueMercator = 3
    // CT_ObliqueMercator_Laborde = 4
    // CT_ObliqueMercator_Rosenmund = 5
    // CT_ObliqueMercator_Spherical = 6
    addProjection(7, "Mercator");
    addProjection(8, "Lambert_Conic_Conformal_(2SP)");
    addProjection(9, "Lambert_Conic_Conformal_(1SP)");
    // CT_LambertAzimEqualArea = 10
    addProjection(11, "Albers_Equal_Area");
    // CT_AzimuthalEquidistant = 12
    // CT_EquidistantConic = 13
    // CT_Stereographic = 14
    // CT_PolarStereographic = 15
    // CT_ObliqueStereographic = 16
    // CT_Equirectangular = 17
    // CT_CassiniSoldner = 18
    // CT_Gnomonic = 19
    // CT_MillerCylindrical = 20
    // CT_Orthographic = 21
    // CT_Polyconic = 22
    // CT_Robinson = 23
    // CT_Sinusoidal = 24
    // CT_VanDerGrinten = 25
    // CT_NewZealandMapGrid = 26
    // CT_TransvMercator_SouthOriented= 27

    // registerCoordinatesProjection("Popular_Visualisation_Pseudo_Mercator",
    // WebMercator.class);
    // registerCoordinatesProjection("Mercator_(1SP)", Mercator1SP.class);
    // registerCoordinatesProjection("Mercator_(2SP)", Mercator2SP.class);
    // registerCoordinatesProjection("Mercator_(1SP)_(Spherical)",
    // Mercator1SPSpherical.class);
    // registerCoordinatesProjection("Lambert_Conic_Conformal_(2SP_Belgium)",
    // LambertConicConformal.class);
  }

  public static void addDoubleParameter(final Map<ParameterName, ParameterValue> parameters,
    final ParameterName name, final Map<Integer, Object> geoKeys, final int key) {
    final Double value = Maps.getDouble(geoKeys, key);
    if (value != null) {
      parameters.put(name, new ParameterValueNumber(value));
    }
  }

  private static void addProjection(final int id, final String name) {
    final CoordinateOperationMethod coordinateOperationMethod = new CoordinateOperationMethod(name);
    PROJECTION_BY_ID.put(id, coordinateOperationMethod);
  }

  public static GeometryFactory getGeometryFactory(final Map<Integer, Object> geoKeys) {
    final int projectedCoordinateSystemId = Maps.getInteger(geoKeys, PROJECTED_COORDINATE_SYSTEM_ID,
      0);
    final int geographicCoordinateSystemId = Maps.getInteger(geoKeys,
      GEOGRAPHIC_COORDINATE_SYSTEM_ID, 0);

    switch (Maps.getInteger(geoKeys, MODEL_TYPE_KEY, 0)) {
      case 1: // Projected
        if (projectedCoordinateSystemId <= 0) {
          return null;
        } else if (projectedCoordinateSystemId == 32767) {
          final GeographicCoordinateSystem geographicCoordinateSystem = EpsgCoordinateSystems
            .getCoordinateSystem(geographicCoordinateSystemId);
          final String name = "unknown";
          final CoordinateOperationMethod coordinateOperationMethod = getProjection(geoKeys);

          final Map<ParameterName, ParameterValue> parameters = new LinkedHashMap<>();
          addDoubleParameter(parameters, ParameterNames.STANDARD_PARALLEL_1, geoKeys,
            STANDARD_PARALLEL_1_KEY);
          addDoubleParameter(parameters, ParameterNames.STANDARD_PARALLEL_2, geoKeys,
            STANDARD_PARALLEL_2_KEY);
          addDoubleParameter(parameters, ParameterNames.CENTRAL_MERIDIAN, geoKeys,
            LONGITUDE_OF_CENTER_2_KEY);
          addDoubleParameter(parameters, ParameterNames.LATITUDE_OF_ORIGIN, geoKeys,
            LATITUDE_OF_CENTER_2_KEY);
          addDoubleParameter(parameters, ParameterNames.FALSE_EASTING, geoKeys, FALSE_EASTING_KEY);
          addDoubleParameter(parameters, ParameterNames.FALSE_NORTHING, geoKeys,
            FALSE_NORTHING_KEY);

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
    final int linearUnitId = Maps.getInteger(geoKeys, LINEAR_UNIT_ID, 0);
    return EpsgCoordinateSystems.getUnit(linearUnitId);
  }

  public static CoordinateOperationMethod getProjection(final Map<Integer, Object> geoKeys) {
    final int projectionId = Maps.getInteger(geoKeys, PROJECTION_ID, 0);
    return PROJECTION_BY_ID.get(projectionId);
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
    String[] asciiParams;
    if (asciiParamsField == null) {
      asciiParams = new String[0];
    } else {
      asciiParams = asciiParamsField.getStringValue().split("\\|");
    }

    if (keysField != null) {
      final int[] keys = keysField.getIntArrayValue();
      for (int i = 4; i < keys.length; i += 4) {
        final int keyId = keys[i];
        final int tiffTag = keys[i + 1];
        @SuppressWarnings("unused")
        final int valueCount = keys[i + 2];
        final int valueOrOffset = keys[i + 3];

        Object value = null;
        switch (tiffTag) {
          case 34736: // DOUBLE
            value = doubleParams[valueOrOffset];
          break;
          case 34737: // ASCII
            value = asciiParams[valueOrOffset];
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
