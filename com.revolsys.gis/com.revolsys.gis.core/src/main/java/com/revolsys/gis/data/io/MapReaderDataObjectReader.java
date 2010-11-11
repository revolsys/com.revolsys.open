package com.revolsys.gis.data.io;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import com.revolsys.gis.data.model.ArrayDataObject;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.MapReader;

public class MapReaderDataObjectReader extends AbstractReader<DataObject>
  implements DataObjectReader, Iterator<DataObject> {

  private DataObjectMetaData metaData;

  private MapReader mapReader;

  private boolean open;

  private Iterator<Map<String, Object>> mapIterator;

  public MapReaderDataObjectReader(
    DataObjectMetaData metaData,
    MapReader mapReader) {
    this.metaData = metaData;
    this.mapReader = mapReader;
  }

  public DataObjectMetaData getMetaData() {
    return metaData;
  }

  public Iterator<DataObject> iterator() {
    return this;
  }

  public boolean hasNext() {
    if (!open) {
      open();
    }
    return mapIterator.hasNext();
  }

  public DataObject next() {
    if (hasNext()) {
      Map<String, Object> values = mapIterator.next();
      DataObject object = new ArrayDataObject(metaData);
      for (Entry<String, Object> entry : values.entrySet()) {
        object.setValue(entry.getKey(), entry.getValue());
      }
      return object;
    } else {
      throw new NoSuchElementException();
    }
  }

  public void remove() {
    mapIterator.remove();
  }

  public void close() {
    mapReader.close();
  }

  public void open() {
    open = true;
    this.mapIterator = mapReader.iterator();
  }
}
