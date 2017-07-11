package com.revolsys.gis.elevation.gridded.esriascii;

import java.io.BufferedReader;
import java.util.Map;

import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.cs.esri.EsriCoordinateSystems;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.gis.elevation.gridded.FloatArrayGriddedElevationModel;
import com.revolsys.gis.elevation.gridded.GriddedElevationModel;
import com.revolsys.gis.elevation.gridded.GriddedElevationModelFactory;
import com.revolsys.gis.elevation.gridded.GriddedElevationModelWriter;
import com.revolsys.gis.elevation.gridded.GriddedElevationModelWriterFactory;
import com.revolsys.io.AbstractIoFactoryWithCoordinateSystem;
import com.revolsys.io.Readers;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;
import com.revolsys.util.number.BigDecimals;
import com.revolsys.util.number.Floats;

public class EsriAsciiGriddedElevation extends AbstractIoFactoryWithCoordinateSystem
  implements GriddedElevationModelFactory, GriddedElevationModelWriterFactory {
  public static final String PROPERTY_READ_DATA = "readData";

  public EsriAsciiGriddedElevation() {
    super("ESRI ASCII Grid");
    addMediaTypeAndFileExtension("image/x-esri-ascii-grid", "asc");
  }

  @Override
  public GriddedElevationModel newGriddedElevationModel(final Resource resource,
    final Map<String, ? extends Object> properties) {
    try (
      BufferedReader reader = resource.newBufferedReader()) {
      GeometryFactory geometryFactory = EsriCoordinateSystems.getGeometryFactory(resource);
      if (geometryFactory == null) {
        geometryFactory = Maps.get(properties, GriddedElevationModel.GEOMETRY_FACTORY);
        if (geometryFactory == null) {
          geometryFactory = GeometryFactory.DEFAULT_3D;
        }
      }
      double xCentre = Double.NaN;
      double yCentre = Double.NaN;
      double xCorner = Double.NaN;
      double yCorner = Double.NaN;
      float noDataValue = 0;
      int width = -1;
      int height = -1;
      int cellSize = 0;
      float elevation = Float.NaN;
      while (Float.isNaN(elevation)) {
        String keyword = Readers.readKeyword(reader);
        if (BigDecimals.isNumber(keyword)) {
          elevation = Floats.toValid(keyword);
        } else {
          keyword = keyword.toLowerCase();
          if ("ncols".equals(keyword)) {
            width = Readers.readInteger(reader);
            if (width <= 0) {
              throw new IllegalArgumentException("ncols must be > 0\n" + resource);
            }
          } else if ("nrows".equals(keyword)) {
            height = Readers.readInteger(reader);
            if (height <= 0) {
              throw new IllegalArgumentException("nrows must be > 0\n" + resource);
            }
          } else if ("cellsize".equals(keyword)) {
            cellSize = Readers.readInteger(reader);
            if (cellSize <= 0) {
              throw new IllegalArgumentException("cellsize must be > 0\n" + resource);
            }
          } else if ("xllcenter".equals(keyword)) {
            xCentre = Readers.readDouble(reader);
          } else if ("yllcenter".equals(keyword)) {
            yCentre = Readers.readDouble(reader);
          } else if ("xllcorner".equals(keyword)) {
            xCorner = Readers.readDouble(reader);
          } else if ("yllcorner".equals(keyword)) {
            yCorner = Readers.readDouble(reader);
          } else if ("nodata_value".equals(keyword)) {
            noDataValue = Readers.readFloat(reader);
          } else {
            // Skip unknown value
            Readers.readKeyword(reader);
          }
        }
      }
      double x;
      double y;
      if (width == 0) {
        throw new IllegalArgumentException("ncols not specified\n" + resource);
      } else if (height == 0) {
        throw new IllegalArgumentException("nrows not specified\n" + resource);
      } else if (cellSize == 0) {
        throw new IllegalArgumentException("cellsize not specified\n" + resource);
      } else if (Double.isNaN(xCentre)) {
        if (Double.isNaN(xCorner)) {
          throw new IllegalArgumentException(
            "xllcenter, yllcenter or xllcorner, yllcorner missing\n" + resource);
        } else {
          if (Double.isNaN(yCorner)) {
            throw new IllegalArgumentException("xllcorner set must missing yllcorner\n" + resource);
          } else {
            x = xCorner;
            y = yCorner;
          }
        }
      } else {
        if (Double.isNaN(yCentre)) {
          throw new IllegalArgumentException("xllcenter set must missing yllcenter\n" + resource);
        } else {
          x = xCentre - cellSize / 2.0;
          y = yCentre - cellSize / 2.0;
        }
      }
      final FloatArrayGriddedElevationModel elevationModel = new FloatArrayGriddedElevationModel(
        geometryFactory, x, y, width, height, cellSize);
      if (Maps.getBool(properties, PROPERTY_READ_DATA, true)) {
        for (int j = 0; j < height; j++) {
          for (int i = 0; i < width; i++) {
            if (elevation == noDataValue) {
              elevationModel.setElevationNull(i, j);
            } else {
              elevationModel.setElevation(i, j, elevation);
            }
            elevation = Readers.readFloat(reader);
          }
        }
      }
      return elevationModel;
    } catch (final Throwable e) {
      throw Exceptions.wrap("Error reading: " + resource, e);
    }
  }

  @Override
  public GriddedElevationModelWriter newGriddedElevationModelWriter(final Resource resource) {
    return new EsriAsciiGriddedElevationModelWriter(resource);
  }

}
