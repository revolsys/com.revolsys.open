package com.revolsys.gis.data.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;

import com.revolsys.gis.data.model.ArrayDataObjectFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.geometry.io.GeometryReaderFactory;
import com.vividsolutions.jts.geom.Geometry;

public abstract class AbstractDataObjectAndGeometryReaderFactory extends
  AbstractDataObjectReaderFactory implements GeometryReaderFactory {
  /** The default data object dataObjectFactory instance. */
  private static final DataObjectFactory DEFAULT_DATA_OBJECT_FACTORY = new ArrayDataObjectFactory();

  public AbstractDataObjectAndGeometryReaderFactory(
    final String name) {
    super(name);
  }

  /**
   * Create a reader for the file using the ({@link ArrayGeometryFactory}).
   * 
   * @param file The file to read.
   * @return The reader for the file.
   */
  public Reader<Geometry> createGeometryReader(
    final File file) {
    try {
      final FileInputStream in = new FileInputStream(file);
      return createGeometryReader(in);
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
  public Reader<Geometry> createGeometryReader(
    final InputStream inputStream) {
    return createGeometryReader(inputStream, Charset.defaultCharset());

  }

  public Reader<Geometry> createGeometryReader(
    final InputStream in,
    final Charset charset) {
    final InputStreamReader reader = new InputStreamReader(in, charset);
    return createGeometryReader(reader);
  }

  /**
   * Create a reader for the URL using the ({@link ArrayGeometryFactory}).
   * 
   * @param url The URL to read.
   * @return The reader for the URL.
   */
  public Reader<Geometry> createGeometryReader(
    final URL url) {
    try {
      final InputStream in = url.openStream();
      return createGeometryReader(in);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Unable to connect to URL:" + url, e);
    }
  }

  public Reader<Geometry> createGeometryReader(
    java.io.Reader in) {
    final Reader<DataObject> dataObjectReader = createDataObjectReader(in);
    final Iterator<DataObject> dataObjectIterator = dataObjectReader.iterator();
    final DataObjectGeometryIterator iterator = new DataObjectGeometryIterator(
      dataObjectIterator);
    final IteratorReader<Geometry> geometryReader = new IteratorReader<Geometry>(
      iterator);
    return geometryReader;
  }
}
