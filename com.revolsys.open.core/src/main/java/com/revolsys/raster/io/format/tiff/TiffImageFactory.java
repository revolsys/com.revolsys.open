package com.revolsys.raster.io.format.tiff;

import java.util.Map;

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
import com.revolsys.spring.resource.Resource;

public class TiffImageFactory extends AbstractIoFactory
  implements GeoreferencedImageReadFactory, GeoreferencedImageWriterFactory {

  public static void addDoubleParameter(final Map<ParameterName, ParameterValue> parameters,
    final ParameterName name, final Map<Integer, Object> geoKeys, final int key) {
    final Double value = Maps.getDouble(geoKeys, key);
    if (value != null) {
      parameters.put(name, new ParameterValueNumber(value));
    }
  }

  public static GeometryFactory getGeometryFactory(final Map<Integer, Object> geoKeys) {
    final int projectedCoordinateSystemId = Maps.getInteger(geoKeys,
      GeoTiffConstants.ProjectedCSTypeGeoKey, 0);
    final int geographicCoordinateSystemId = Maps.getInteger(geoKeys,
      GeoTiffConstants.GeographicTypeGeoKey, 0);

    switch (Maps.getInteger(geoKeys, GeoTiffConstants.GTModelTypeGeoKey, 0)) {
      case 1: // Projected
        if (projectedCoordinateSystemId <= 0) {
          return null;
        } else if (projectedCoordinateSystemId == 32767) {
          final GeographicCoordinateSystem geographicCoordinateSystem = EpsgCoordinateSystems
            .getCoordinateSystem(geographicCoordinateSystemId);
          final String name = "unknown";
          final CoordinateOperationMethod coordinateOperationMethod = getProjection(geoKeys);

          final Map<ParameterName, ParameterValue> parameters = TiffProjectionParameterName
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

  public static LinearUnit getLinearUnit(final Map<Integer, Object> geoKeys) {
    final int linearUnitId = Maps.getInteger(geoKeys, GeoTiffConstants.ProjLinearUnitsGeoKey, 0);
    return EpsgCoordinateSystems.getUnit(linearUnitId);
  }

  public static CoordinateOperationMethod getProjection(final Map<Integer, Object> geoKeys) {
    final int projectionId = Maps.getInteger(geoKeys, GeoTiffConstants.ProjCoordTransGeoKey, 0);
    return TiffCoordinateTransformationCode.getCoordinateOperationMethod(projectionId);
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
    return new TiffImage(resource);
  }

}
