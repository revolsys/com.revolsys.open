package com.revolsys.gis.format.xbase.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.io.AbstractReader;
import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.io.EndianInputStream;

public class XBaseReader extends AbstractReader<DataObject> implements
  DataObjectReader {
  private XbaseIterator iterator;

  public XBaseReader(
    final File file,
    final DataObjectFactory factory) {
    try {
      this.iterator = new XbaseIterator(file, factory);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Unable to open file: " + file, e);
    }
  }

  public XBaseReader(
    final InputStream in,
    final DataObjectFactory factory) {
    try {
      this.iterator = new XbaseIterator(new QName("dbase"),
        new EndianInputStream(in), factory);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Unable to open file", e);
    }
  }

  public void close() {
    iterator.close();
  }

  public DataObjectMetaData getMetaData() {
    return iterator.getMetaData();
  }

  public Iterator<DataObject> iterator() {
    return iterator;
  }
}
