package com.revolsys.format.shp;

import java.io.File;
import java.io.OutputStream;
import java.nio.charset.Charset;

import com.revolsys.format.zip.ZipRecordReader;
import com.revolsys.io.FileUtil;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.io.AbstractRecordIoFactory;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.RecordWriterFactory;
import com.revolsys.record.io.ZipRecordWriter;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.FileSystemResource;
import com.revolsys.spring.resource.Resource;

public class ShapefileZip extends AbstractRecordIoFactory implements RecordWriterFactory {

  public ShapefileZip() {
    super("ESRI Shapefile inside a ZIP archive");
    addMediaTypeAndFileExtension("application/x-shp+zip", "shpz");
  }

  @Override
  public RecordReader createRecordReader(final Resource resource, final RecordFactory factory) {
    return new ZipRecordReader(resource, ShapefileConstants.FILE_EXTENSION, factory);
  }

  @Override
  public RecordWriter createRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    File directory;
    try {
      directory = FileUtil.createTempDirectory(baseName, "zipDir");
    } catch (final Throwable e) {
      throw new RuntimeException("Unable to create temporary directory", e);
    }
    final Resource tempResource = new FileSystemResource(new File(directory, baseName + ".shp"));
    final RecordWriter shapeWriter = new ShapefileRecordWriter(recordDefinition, tempResource);
    return new ZipRecordWriter(directory, shapeWriter, outputStream);
  }

  @Override
  public boolean isBinary() {
    return true;
  }
}
