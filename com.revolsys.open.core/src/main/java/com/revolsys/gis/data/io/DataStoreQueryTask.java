package com.revolsys.gis.data.io;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.io.Reader;
import com.revolsys.parallel.process.AbstractProcess;

public class DataStoreQueryTask extends AbstractProcess {

  private final DataObjectStore dataStore;

  private final BoundingBox boundingBox;

  private List<DataObject> objects;

  private final QName typeName;

  public DataStoreQueryTask(
    final DataObjectStore dataStore,
    final QName typeName,
    final BoundingBox boundingBox) {
    this.dataStore = dataStore;
    this.typeName = typeName;
    this.boundingBox = boundingBox;
  }

  public void cancel() {
    objects = null;
  }

  public String getBeanName() {
    return getClass().getName();
  }

  public void run() {
    objects = new ArrayList<DataObject>();
    final Reader<DataObject> reader = dataStore.query(typeName, boundingBox);
    try {
      for (final DataObject object : reader) {
        try {
          objects.add(object);
        } catch (final NullPointerException e) {
          return;
        }
      }
    } finally {
      reader.close();
    }
  }

  public void setBeanName(
    final String name) {
  }
}
