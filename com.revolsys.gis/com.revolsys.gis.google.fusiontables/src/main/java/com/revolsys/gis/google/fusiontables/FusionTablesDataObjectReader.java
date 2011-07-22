package com.revolsys.gis.google.fusiontables;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import com.revolsys.csv.CsvMapReader;
import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.gis.data.model.ArrayDataObject;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.google.fusiontables.attribute.FusionTablesAttribute;
import com.revolsys.io.AbstractReader;
import com.revolsys.io.MapReader;

public class FusionTablesDataObjectReader extends AbstractReader<DataObject>
  implements DataObjectReader, Iterator<DataObject> {
  private final DataObjectMetaData metaData;

  private final MapReader mapReader;

  private boolean open;

  private Iterator<Map<String, Object>> mapIterator;

  public FusionTablesDataObjectReader(final DataObjectMetaData metaData,
    final InputStream in) {
    this.metaData = metaData;
    this.mapReader = new CsvMapReader(in);
  }

  public void close() {
    mapReader.close();
  }

  public DataObjectMetaData getMetaData() {
    return metaData;
  }

  public boolean hasNext() {
    if (!open) {
      open();
    }
    return mapIterator.hasNext();
  }

  public Iterator<DataObject> iterator() {
    return this;
  }

  public DataObject next() {
    if (hasNext()) {
      final Map<String, Object> values = mapIterator.next();
      final DataObject object = new ArrayDataObject(metaData);
      for (final Attribute attribute : metaData.getAttributes()) {
        final String name = attribute.getName();
        final String stringValue = (String)values.get(name);
        if (stringValue != null) {
          Object value = stringValue;
          if (attribute instanceof FusionTablesAttribute) {
            final FusionTablesAttribute fusionTablesAttribute = (FusionTablesAttribute)attribute;
            value = fusionTablesAttribute.parseString(stringValue);
          }
          object.setValue(name, value);
        }
      }
      object.setState(DataObjectState.Persisted);
      return object;
    } else {
      throw new NoSuchElementException();
    }
  }

  public void open() {
    open = true;
    this.mapIterator = mapReader.iterator();
  }

  public void remove() {
    mapIterator.remove();
  }
}
