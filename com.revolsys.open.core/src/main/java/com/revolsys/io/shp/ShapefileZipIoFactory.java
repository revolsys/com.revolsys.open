package com.revolsys.io.shp;

import java.io.File;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.data.io.AbstractRecordAndGeometryIoFactory;
import com.revolsys.data.io.RecordReader;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Writer;
import com.revolsys.io.ZipWriter;
import com.revolsys.io.zip.ZipRecordReader;

public class ShapefileZipIoFactory extends
AbstractRecordAndGeometryIoFactory {

  public ShapefileZipIoFactory() {
    super("ESRI Shapefile inside a ZIP archive", true, true);
    addMediaTypeAndFileExtension("application/x-shp+zip", "shpz");
  }

  @Override
  public Writer<Record> createRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    File directory;
    try {
      directory = FileUtil.createTempDirectory(baseName, "zipDir");
    } catch (final Throwable e) {
      throw new RuntimeException("Unable to create temporary directory", e);
    }
    final Resource tempResource = new FileSystemResource(new File(directory,
      baseName + ".shp"));
    final Writer<Record> shapeWriter = new ShapefileRecordWriter(recordDefinition,
      tempResource);
    return new ZipWriter<Record>(directory, shapeWriter, outputStream);
  }

  @Override
  public RecordReader createRecordReader(final Resource resource,
    final RecordFactory factory) {
    return new ZipRecordReader(resource, ShapefileConstants.FILE_EXTENSION,
      factory);
  }

}
