package com.revolsys.elevation.gridded.esriascii;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.revolsys.collection.map.Maps;
import com.revolsys.elevation.gridded.DoubleArrayGriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.geometry.cs.esri.EsriCoordinateSystems;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Readers;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;
import com.revolsys.util.number.BigDecimals;
import com.revolsys.util.number.Doubles;

public class EsriAsciiGriddedElevationModelReader extends BaseObjectWithProperties
  implements BaseCloseable {

  private GeometryFactory geometryFactory = GeometryFactory.DEFAULT_3D;

  private final Resource resource;

  public EsriAsciiGriddedElevationModelReader(final Resource resource,
    final Map<String, ? extends Object> properties) {
    this.resource = resource;
    setProperties(properties);
  }

  protected BufferedReader getBufferedReader() {

    final String fileExtension = this.resource.getFileNameExtension();
    try {
      if (fileExtension.equals("zip")) {
        final ZipInputStream in = this.resource.newBufferedInputStream(ZipInputStream::new);
        final String fileName = this.resource.getBaseName();
        final String baseName = FileUtil.getBaseName(fileName);
        final String projName = baseName + ".prj";
        for (ZipEntry zipEntry = in.getNextEntry(); zipEntry != null; zipEntry = in
          .getNextEntry()) {
          final String name = zipEntry.getName();
          if (name.equals(projName)) {
            final String wkt = FileUtil.getString(new InputStreamReader(in, StandardCharsets.UTF_8),
              false);
            final GeometryFactory geometryFactory = EsriCoordinateSystems.getGeometryFactory(wkt);
            setGeometryFactory(geometryFactory);
          } else if (name.equals(fileName)) {
            return new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
          }
        }
        throw new IllegalArgumentException("Cannot find " + fileName + " in " + this.resource);
      } else if (fileExtension.equals("gz")) {
        final InputStream in = this.resource.newBufferedInputStream();
        final GZIPInputStream gzIn = new GZIPInputStream(in);
        return new BufferedReader(new InputStreamReader(gzIn, StandardCharsets.UTF_8));
      } else {
        final GeometryFactory geometryFactory = EsriCoordinateSystems
          .getGeometryFactory(this.resource);
        setGeometryFactory(geometryFactory);
        return this.resource.newBufferedReader();
      }
    } catch (final IOException e) {
      throw Exceptions.wrap("Unable to open: " + this.resource, e);
    }
  }

  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  public GriddedElevationModel read() {

    try (
      BufferedReader reader = getBufferedReader()) {
      double xCentre = Double.NaN;
      double yCentre = Double.NaN;
      double xCorner = Double.NaN;
      double yCorner = Double.NaN;
      double noDataValue = 0;
      int width = -1;
      int height = -1;
      double cellSize = 0;
      double elevation = Double.NaN;
      while (Double.isNaN(elevation)) {
        String keyword = Readers.readKeyword(reader);
        if (BigDecimals.isNumber(keyword)) {
          elevation = Doubles.toValid(keyword);
        } else {
          keyword = keyword.toLowerCase();
          if ("ncols".equals(keyword)) {
            width = Readers.readInteger(reader);
            if (width <= 0) {
              throw new IllegalArgumentException("ncols must be > 0\n" + this.resource);
            }
          } else if ("nrows".equals(keyword)) {
            height = Readers.readInteger(reader);
            if (height <= 0) {
              throw new IllegalArgumentException("nrows must be > 0\n" + this.resource);
            }
          } else if ("cellsize".equals(keyword)) {
            cellSize = Readers.readDouble(reader);
            if (cellSize <= 0) {
              throw new IllegalArgumentException("cellsize must be > 0\n" + this.resource);
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
            noDataValue = Readers.readDouble(reader);
          } else {
            // Skip unknown value
            Readers.readKeyword(reader);
          }
        }
      }
      double x;
      double y;
      if (width == 0) {
        throw new IllegalArgumentException("ncols not specified\n" + this.resource);
      } else if (height == 0) {
        throw new IllegalArgumentException("nrows not specified\n" + this.resource);
      } else if (cellSize == 0) {
        throw new IllegalArgumentException("cellsize not specified\n" + this.resource);
      } else if (Double.isNaN(xCentre)) {
        if (Double.isNaN(xCorner)) {
          throw new IllegalArgumentException(
            "xllcenter, yllcenter or xllcorner, yllcorner missing\n" + this.resource);
        } else {
          if (Double.isNaN(yCorner)) {
            throw new IllegalArgumentException(
              "xllcorner set must missing yllcorner\n" + this.resource);
          } else {
            x = xCorner;
            y = yCorner;
          }
        }
      } else {
        if (Double.isNaN(yCentre)) {
          throw new IllegalArgumentException(
            "xllcenter set must missing yllcenter\n" + this.resource);
        } else {
          x = xCentre - cellSize / 2.0;
          y = yCentre - cellSize / 2.0;
        }
      }
      final DoubleArrayGriddedElevationModel elevationModel = new DoubleArrayGriddedElevationModel(
        this.geometryFactory, x, y, width, height, cellSize);
      elevationModel.setResource(this.resource);
      if (Maps.getBool(getProperties(), EsriAsciiGriddedElevation.PROPERTY_READ_DATA, true)) {
        for (int gridY = height - 1; gridY >= 0; gridY--) {
          for (int gridX = 0; gridX < width; gridX++) {
            if (elevation == noDataValue) {
              elevationModel.setElevationNull(gridX, gridY);
            } else {
              elevationModel.setElevation(gridX, gridY, elevation);
            }
            elevation = Readers.readDouble(reader);
          }
        }
      }
      return elevationModel;
    } catch (final Throwable e) {
      throw Exceptions.wrap("Error reading: " + this.resource, e);
    }
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    if (geometryFactory == null) {
      this.geometryFactory = GeometryFactory.DEFAULT_3D;
    } else {
      this.geometryFactory = geometryFactory;
    }
  }

}
