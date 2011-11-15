package com.revolsys.io.moep;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.AbstractReader;

public class MoepBinaryReader extends AbstractReader<DataObject> implements
  DataObjectReader {

  private MoepBinaryIterator iterator;

  /**
   * Construct a new MoepBinaryReader.
   * 
   * @param moepDirectoryReader
   * @param file The the file.
   * @param factory The factory used to create DataObject instances.
   */
  public MoepBinaryReader(
    final MoepDirectoryReader moepDirectoryReader,
    final Resource resource,
    final DataObjectFactory factory) {
    try {
      final InputStream in = resource.getInputStream();
      this.iterator = new MoepBinaryIterator(moepDirectoryReader,
        resource.getFilename(), in, factory);
    } catch (final IOException e) {
    }
  }

  /**
   * Construct a new MoepBinaryReader.
   * 
   * @param url The url to the file.
   * @param factory The factory used to create DataObject instances.
   */
  public MoepBinaryReader(
    final URL url,
    final DataObjectFactory factory) {
    try {
      final InputStream in = url.openStream();
      final String path = url.getPath();
      String fileName = path;
      final int slashIndex = fileName.lastIndexOf('/');
      if (slashIndex != -1) {
        fileName = fileName.substring(slashIndex + 1);
      }
      this.iterator = new MoepBinaryIterator(null, fileName, in, factory);
    } catch (final IOException e) {
    }
  }

  public void close() {
    iterator.close();
  }

  public DataObjectMetaData getMetaData() {
    return MoepConstants.META_DATA;
  }

  @Override
  public Map<String, Object> getProperties() {
    return iterator.getProperties();
  }

  public void open() {
    iterator.hasNext();
  }

  /**
   * Get the iterator for the MOEP file.
   * 
   * @return The iterator.
   */
  public Iterator iterator() {
    return iterator;
  }

}
