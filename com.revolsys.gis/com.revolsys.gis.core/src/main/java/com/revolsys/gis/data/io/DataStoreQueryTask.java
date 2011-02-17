package com.revolsys.gis.data.io;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.parallel.process.AbstractProcess;
import com.vividsolutions.jts.geom.Envelope;

public class DataStoreQueryTask extends AbstractProcess {

  private final DataObjectStore dataStore;

  private final Envelope envelope;

  private List<DataObject> objects;

  private final QName typeName;

  public DataStoreQueryTask(
    final DataObjectStore dataStore,
    final QName typeName,
    final Envelope envelope) {
    this.dataStore = dataStore;
    this.typeName = typeName;
    this.envelope = envelope;
  }

  public void cancel() {
    objects = null;
  }

  public String getBeanName() {
    return getClass().getName();
  }

  public void run() {
    objects = new ArrayList();
    final Reader<DataObject> reader = dataStore.query(typeName, envelope);
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
