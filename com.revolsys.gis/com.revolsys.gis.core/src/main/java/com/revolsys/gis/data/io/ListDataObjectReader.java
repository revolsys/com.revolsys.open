package com.revolsys.gis.data.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.AbstractReader;

public class ListDataObjectReader extends AbstractReader<DataObject> implements
  DataObjectReader {
  private DataObjectMetaData metaData;

  private List<DataObject> objects = new ArrayList<DataObject>();

  public ListDataObjectReader(
    final DataObjectMetaData metaData,
    final DataObject... objects) {
    this(metaData, Arrays.asList(objects));
  }

  public ListDataObjectReader(
    final DataObjectMetaData metaData,
    final List<DataObject> objects) {
    this.metaData = metaData;
    this.objects = objects;
  }

  public void close() {
    metaData = null;
    objects = null;
  }

  public DataObjectMetaData getMetaData() {
    return metaData;
  }

  public Iterator<DataObject> iterator() {
    return objects.iterator();
  }

  public void open() {
  }
}
