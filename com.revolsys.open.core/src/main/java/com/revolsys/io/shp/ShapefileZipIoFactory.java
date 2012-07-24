package com.revolsys.io.shp;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.AbstractDataObjectAndGeometryIoFactory;
import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.gis.data.io.ZipDataObjectReader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Writer;
import com.revolsys.io.ZipWriter;

public class ShapefileZipIoFactory extends
  AbstractDataObjectAndGeometryIoFactory {

  public ShapefileZipIoFactory() {
    super("ESRI Shapefile ZIP", true, true);
    addMediaTypeAndFileExtension("application/x-shp+zip", "shpz");
  }

  public DataObjectReader createDataObjectReader(final Resource resource,
    final DataObjectFactory factory) {
    return new ZipDataObjectReader(resource, ShapefileConstants.FILE_EXTENSION,
      factory);
  }

  public Writer<DataObject> createDataObjectWriter(final String baseName,
    final DataObjectMetaData metaData, final OutputStream outputStream,
    final Charset charset) {
    File directory;
    try {
      directory = FileUtil.createTempDirectory(baseName, "zipDir");
    } catch (final Throwable e) {
      throw new RuntimeException("Unable to create temporary directory", e);
    }
    final Resource tempResource = new FileSystemResource(new File(directory,
      baseName + ".shp"));
    final Writer<DataObject> shapeWriter = new ShapefileDataObjectWriter(
      metaData, tempResource);
    return new ZipWriter<DataObject>(directory, shapeWriter, outputStream);
  }

}
