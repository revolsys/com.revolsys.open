package com.revolsys.gis.format.shape.io;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import com.revolsys.gis.data.io.AbstractDataObjectAndGeometryWriterFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Writer;
import com.revolsys.io.ZipWriter;

public class ShapeZipWriterFactory extends
  AbstractDataObjectAndGeometryWriterFactory {
  public ShapeZipWriterFactory() {
    super("ESRI Shapefile ZIP");
    addMediaTypeAndFileExtension("application/x-shp+zip", "shpz");
  }

  public Writer<DataObject> createDataObjectWriter(
    String baseName,
    DataObjectMetaData metaData,
    OutputStream outputStream,
    Charset charset) {
    File directory;
    try {
      directory = FileUtil.createTempDirectory(baseName, "zipDir");
    } catch (IOException e) {
      throw new RuntimeException("Unable to create temporary directory", e);
    }
    File tempFile = new File(directory, baseName + ".shp");
    try {
      Writer<DataObject> shapeWriter = new ShapeFileWriter(tempFile, metaData);
      return new ZipWriter<DataObject>(directory, shapeWriter, outputStream);
    } catch (IOException e) {
      throw new RuntimeException("Unable to create shape writer", e);
    }
  }

}
