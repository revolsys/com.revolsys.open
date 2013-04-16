package com.revolsys.swing.map.layer.raster;

import java.awt.geom.AffineTransform;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

import javax.media.jai.RenderedOp;

import org.libtiff.jai.codec.XTIFF;
import org.libtiff.jai.codec.XTIFFDirectory;
import org.libtiff.jai.codec.XTIFFField;
import org.libtiff.jai.operator.XTIFFDescriptor;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.WktCsParser;
import com.revolsys.gis.cs.esri.EsriCoordinateSystems;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.io.FileUtil;

public class GeoTiffRaster extends GeoReferencedRaster {

  static {
    XTIFFDescriptor.register();
  }
  
  public GeoTiffRaster(String fileName) {
    super(fileName);
    getImage();
  }

  private boolean loadGeoTiffMetaData(XTIFFDirectory dir) {
    XTIFFField fieldModelTiePoints = dir.getField(XTIFF.TIFFTAG_GEO_TIEPOINTS);
    if (fieldModelTiePoints == null) {
      XTIFFField fieldModelGeoTransform = dir.getField(XTIFF.TIFFTAG_GEO_TRANS_MATRIX);
      if (fieldModelGeoTransform == null) {
        return false;
      } else {
        double xCoordinate = fieldModelGeoTransform.getAsDouble(3);
        double yCoordinate = fieldModelGeoTransform.getAsDouble(7);
        double xPixelSize = fieldModelGeoTransform.getAsDouble(0);
        double yPixelSize = fieldModelGeoTransform.getAsDouble(5);
        double xRotation = fieldModelGeoTransform.getAsDouble(4);
        double yRotation = fieldModelGeoTransform.getAsDouble(1);

        double[] transformationMatrix = {
          xPixelSize, yRotation, xRotation, yPixelSize, xCoordinate,
          yCoordinate
        };
        setTransformationMatrix(transformationMatrix);
        return true;
      }
    } else {
      XTIFFField fieldModelPixelScale = dir.getField(XTIFF.TIFFTAG_GEO_PIXEL_SCALE);
      if (fieldModelPixelScale == null) {
        return false;
      } else {
        setTopLeftRasterPoint(new DoubleCoordinates(
          fieldModelTiePoints.getAsDouble(0),
          fieldModelTiePoints.getAsDouble(1)));
        setTopLeftModelPoint(new DoubleCoordinates(
          fieldModelTiePoints.getAsDouble(3),
          fieldModelTiePoints.getAsDouble(4)));
        setXModelUnitsPerRasterUnit(fieldModelPixelScale.getAsDouble(0));
        setYModelUnitsPerRasterUnit(fieldModelPixelScale.getAsDouble(1));
        setEnvelope();
        return true;
      }
    }
  }

  protected void setTransformationMatrix(double[] transformationMatrix) {
    setTopLeftRasterPoint(new DoubleCoordinates(0.0, 0.0));
    setTopLeftModelPoint(new DoubleCoordinates(0.0, 0.0));
    setAffineTransformation(new AffineTransform(transformationMatrix));
  }

  private void loadWorldFile() {
    File worldFile = FileUtil.getFileWithExtension(new File(getFileName()),
      "tfw");
    if (worldFile.exists()) {
      try {
        BufferedReader reader = new BufferedReader(new FileReader(worldFile));
        try {
          double xPixelSize = Double.parseDouble(reader.readLine());
          double yRotation = Double.parseDouble(reader.readLine());
          double xRotation = Double.parseDouble(reader.readLine());
          double yPixelSize = Double.parseDouble(reader.readLine());
          double xCoordinate = Double.parseDouble(reader.readLine());
          double yCoordinate = Double.parseDouble(reader.readLine());

          double[] transformationMatrix = {
            xPixelSize, yRotation, xRotation, yPixelSize, xCoordinate,
            yCoordinate
          };
          setTransformationMatrix(transformationMatrix);
        } finally {
          reader.close();
        }
      } catch (IOException e) {
        throw new RuntimeException(
          "Error reading TIFF world file " + worldFile, e);
      }
    } else {
      throw new IllegalArgumentException("Cannot find world file " + worldFile);
    }
  }

  private void loadProjectionFile() {
    File projectionFile = FileUtil.getFileWithExtension(
      new File(getFileName()), "prj");
    if (projectionFile.exists()) {
      try {
        CoordinateSystem coordinateSystem = new WktCsParser(
          new FileInputStream(projectionFile)).parse();
        coordinateSystem = EsriCoordinateSystems.getCoordinateSystem(coordinateSystem);
        GeometryFactory geometryFactory = GeometryFactory.getFactory(coordinateSystem);
        setGeometryFactory(geometryFactory);
      } catch (IOException e) {
        throw new IllegalArgumentException("Unable to read .prj file "
          + projectionFile, e);
      }
    }
  }

  protected void loadImageMetaData() {
    RenderedOp image = getImage();
    XTIFFDirectory dir = (XTIFFDirectory)image.getProperty("tiff.directory");
    if (dir == null) {
      throw new IllegalArgumentException(
        "This is not a (geo)tiff file. Missing TIFF directory.");
    } else {
      if (!loadGeoTiffMetaData(dir)) {
        loadProjectionFile();
        loadWorldFile();
      }
    }
  }

}
