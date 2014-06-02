package com.revolsys.gis.data.io;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.query.Query;
import com.revolsys.gis.data.query.functions.F;
import com.revolsys.io.Reader;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.parallel.process.AbstractProcess;

public class DataStoreQueryTask extends AbstractProcess {

  private final DataObjectStore dataStore;

  private final BoundingBox boundingBox;

  private List<DataObject> objects;

  private final String path;

  public DataStoreQueryTask(final DataObjectStore dataStore, final String path,
    final BoundingBox boundingBox) {
    this.dataStore = dataStore;
    this.path = path;
    this.boundingBox = boundingBox;
  }

  public void cancel() {
    objects = null;
  }

  @Override
  public String getBeanName() {
    return getClass().getName();
  }

  @Override
  public void run() {
    objects = new ArrayList<DataObject>();
    final DataObjectMetaData metaData = dataStore.getMetaData(path);
    final Query query = new Query(metaData);
    final Attribute geometryAttribute = metaData.getGeometryAttribute();
    query.setWhereCondition(F.envelopeIntersects(geometryAttribute, boundingBox));
    try (
      final Reader<DataObject> reader = dataStore.query(query)) {
      for (final DataObject object : reader) {
        try {
          objects.add(object);
        } catch (final NullPointerException e) {
          return;
        }
      }
    }
  }

  @Override
  public void setBeanName(final String name) {
  }
}
