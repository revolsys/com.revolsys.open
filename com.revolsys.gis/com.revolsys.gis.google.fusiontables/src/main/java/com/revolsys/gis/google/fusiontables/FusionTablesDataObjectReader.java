package com.revolsys.gis.google.fusiontables;

import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import com.revolsys.csv.CsvMapReader;
import com.revolsys.gis.data.io.AbstractReader;
import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.gis.data.model.ArrayDataObject;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.kml.io.KmlGeometryIterator;
import com.revolsys.io.MapReader;

public class FusionTablesDataObjectReader extends AbstractReader<DataObject>
  implements DataObjectReader, Iterator<DataObject> {
  private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

  private DataObjectMetaData metaData;

  private MapReader mapReader;

  private boolean open;

  private Iterator<Map<String, Object>> mapIterator;

  public FusionTablesDataObjectReader(
    DataObjectMetaData metaData,
    InputStream in) {
    this.metaData = metaData;
    this.mapReader = new CsvMapReader(in);
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
      for (Attribute attribute : metaData.getAttributes()) {
        String name = attribute.getName();
        String stringValue = (String)values.get(name);
        if (stringValue != null) {
          DataType type = attribute.getType();
          Object value = stringValue;
          if (type.equals(DataTypes.DECIMAL)) {
            value = new BigDecimal(stringValue);
          } else if (type.equals(DataTypes.DATE_TIME)) {
            try {
              value = dateFormat.parse(stringValue);
            } catch (ParseException e) {
              throw new IllegalArgumentException("Expecting a YYYY-MM-DD date");
            }
          } else if (type.equals(DataTypes.GEOMETRY)) {
            final KmlGeometryIterator geometryIterator = new KmlGeometryIterator(
              new StringReader(stringValue));
            if (geometryIterator.hasNext()) {
              value = geometryIterator.next();
            }
          }
          object.setValue(name, value);
        }
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
