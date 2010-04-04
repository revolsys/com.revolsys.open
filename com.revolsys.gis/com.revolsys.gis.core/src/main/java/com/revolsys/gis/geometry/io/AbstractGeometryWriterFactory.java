package com.revolsys.gis.geometry.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import com.revolsys.io.AbstractIoFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Writer;
import com.vividsolutions.jts.geom.Geometry;

public abstract class AbstractGeometryWriterFactory extends AbstractIoFactory
  implements GeometryWriterFactory {

  public AbstractGeometryWriterFactory(
    final String name) {
    super(name);
  }

  /**
   * Create a reader for the file using the ({@link ArrayGeometryFactory}).
   * 
   * @param file The file to read.
   * @return The reader for the file.
   */
  public Writer<Geometry> createGeometryWriter(
    final File file) {
    try {
      final FileOutputStream in = new FileOutputStream(file);
      final String baseName = FileUtil.getBaseName(file);
      return createGeometryWriter(baseName, in);
    } catch (final FileNotFoundException e) {
      throw new IllegalArgumentException("File does not exist:" + file, e);
    }
  }

  /**
   * Create a reader for the file using the ({@link ArrayGeometryFactory}).
   * 
   * @param inputStream The file to read.
   * @return The reader for the file.
   */
  public Writer<Geometry> createGeometryWriter(
    String baseName,
    final OutputStream inputStream) {
    return createGeometryWriter(baseName, inputStream, Charset.forName("UTF-8"));

  }

}
