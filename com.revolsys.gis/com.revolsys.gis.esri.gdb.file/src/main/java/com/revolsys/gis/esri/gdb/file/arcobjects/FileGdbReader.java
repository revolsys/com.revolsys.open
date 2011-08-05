package com.revolsys.gis.esri.gdb.file.arcobjects;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.io.AbstractMultipleIteratorReader;

public class FileGdbReader extends AbstractMultipleIteratorReader<DataObject> {

  private List<QName> typeNames = new ArrayList<QName>();

  private final ArcObjectsFileGdbDataObjectStore dataStore;

  private BoundingBox boundingBox;

  private int index = 0;

  public FileGdbReader(final ArcObjectsFileGdbDataObjectStore dataStore) {
    this.dataStore = dataStore;
  }

  public BoundingBox getBoundingBox() {
    return boundingBox;
  }

  @Override
  protected AbstractIterator<DataObject> getNextIterator() {
    if (index < typeNames.size()) {
      final QName typeName = typeNames.get(index);
      final AbstractIterator<DataObject> iterator;

      if (boundingBox == null) {
        iterator = new FileGdbQueryIterator(dataStore, typeName);
      } else {
        iterator = new FileGdbFeatureClassQueryIterator(dataStore, typeName,
          boundingBox);
      }
      index++;
      return iterator;
    } else {
      return null;
    }
  }

  public List<QName> getTypeNames() {
    return typeNames;
  }

  public void setBoundingBox(final BoundingBox boundingBox) {
    this.boundingBox = boundingBox;
  }

  public void setTypeNames(final List<QName> typeNames) {
    this.typeNames = typeNames;
  }

  @Override
  public String toString() {
    return "Reader " + dataStore.getLabel() + " " + typeNames + " "
      + boundingBox;
  }
}
