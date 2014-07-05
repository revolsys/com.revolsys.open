package com.revolsys.io.moep;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import org.springframework.core.io.Resource;

import com.revolsys.data.io.RecordReader;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.AbstractReader;

public class MoepBinaryReader extends AbstractReader<Record> implements
  RecordReader {

  private MoepBinaryIterator iterator;

  /**
   * Construct a new MoepBinaryReader.
   * 
   * @param moepDirectoryReader
   * @param file The the file.
   * @param factory The factory used to create Record instances.
   */
  public MoepBinaryReader(final MoepDirectoryReader moepDirectoryReader,
    final Resource resource, final RecordFactory factory) {
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
   * @param factory The factory used to create Record instances.
   */
  public MoepBinaryReader(final URL url, final RecordFactory factory) {
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

  @Override
  public void close() {
    iterator.close();
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return MoepConstants.META_DATA;
  }

  @Override
  public Map<String, Object> getProperties() {
    return iterator.getProperties();
  }

  /**
   * Get the iterator for the MOEP file.
   * 
   * @return The iterator.
   */
  @Override
  public Iterator iterator() {
    return iterator;
  }

  @Override
  public void open() {
    iterator.hasNext();
  }

}
