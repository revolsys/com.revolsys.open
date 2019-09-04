package com.revolsys.raster.io.format.tiff;

import java.util.Map;

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
import com.revolsys.io.AbstractIoFactory;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.raster.GeoreferencedImageReadFactory;
import com.revolsys.raster.GeoreferencedImageWriter;
import com.revolsys.raster.GeoreferencedImageWriterFactory;
import com.revolsys.raster.io.format.tiff.code.GeoTiffCoordinateTransformationCode;
import com.revolsys.raster.io.format.tiff.code.GeoTiffKey;
import com.revolsys.raster.io.format.tiff.code.GeoTiffKeyProjectionParameterName;
import com.revolsys.raster.io.format.tiff.code.GeoTiffKeys;
import com.revolsys.raster.io.format.tiff.image.TiffCommonsImagingImage;
import com.revolsys.raster.io.format.tiff.image.TiffImage;
import com.revolsys.spring.resource.Resource;

public class TiffImageFactory extends AbstractIoFactory
  implements GeoreferencedImageReadFactory, GeoreferencedImageWriterFactory {

  public static void addDoubleParameter(final Map<ParameterName, ParameterValue> parameters,
    final ParameterName name, final Map<GeoTiffKey, Object> geoKeys, final GeoTiffKey key) {
    final Double value = Maps.getDouble(geoKeys, key);
    if (value != null) {
      parameters.put(name, new ParameterValueNumber(value));
    }
  }

  public static GeometryFactory getGeometryFactory(final Map<GeoTiffKey, Object> geoKeys) {
    final int projectedCoordinateSystemId = GeoTiffKeys.ProjectedCSTypeGeoKey.getInteger(geoKeys,
      0);
    final int geographicCoordinateSystemId = GeoTiffKeys.GeographicTypeGeoKey.getInteger(geoKeys,
      0);

    switch (GeoTiffKeys.GTModelTypeGeoKey.getInteger(geoKeys, 0)) {
      case 1: // Projected
        if (projectedCoordinateSystemId <= 0) {
          return null;
        } else if (projectedCoordinateSystemId == 32767) {
          final GeographicCoordinateSystem geographicCoordinateSystem = EpsgCoordinateSystems
            .getCoordinateSystem(geographicCoordinateSystemId);
          final String name = "unknown";
          final CoordinateOperationMethod coordinateOperationMethod = getProjection(geoKeys);

          final Map<ParameterName, ParameterValue> parameters = GeoTiffKeyProjectionParameterName
            .getProjectionParameters(geoKeys);

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

  public static LinearUnit getLinearUnit(final Map<GeoTiffKey, Object> geoKeys) {
    final int linearUnitId = GeoTiffKeys.ProjLinearUnitsGeoKey.getInteger(geoKeys, 0);
    return EpsgCoordinateSystems.getUnit(linearUnitId);
  }

  public static CoordinateOperationMethod getProjection(final Map<GeoTiffKey, Object> geoKeys) {
    final int projectionId = GeoTiffKeys.ProjCoordTransGeoKey.getInteger(geoKeys, 0);
    return GeoTiffCoordinateTransformationCode.getCoordinateOperationMethod(projectionId);
  }

  public TiffImageFactory() {
    super("TIFF/GeoTIFF");
    addMediaTypeAndFileExtension("image/tiff", "tif");
    addMediaTypeAndFileExtension("image/tiff", "tiff");
  }

  @Override
  public GeoreferencedImageWriter newGeoreferencedImageWriter(final Resource resource) {
    return new TiffGeoreferencedImageWriter(resource);
  }

  @Override
  public GeoreferencedImage readGeoreferencedImage(final Resource resource) {
    try (
      TiffDirectoryIterator iterator = new TiffDirectoryIterator(resource)) {
      for (final TiffDirectory directory : iterator) {
        final TiffImage image = directory.getImage();
        image.getRenderedImage();
        return image;
      }
    } catch (final Exception e) {
      Logs.error(TiffImageFactory.class,
        "Cannot read TIFF, falling back to commons-imaging library: " + resource);
    }
    return new TiffCommonsImagingImage(resource);
  }

}
