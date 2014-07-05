package com.revolsys.data.io;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.data.query.Query;
import com.revolsys.data.query.functions.F;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.Reader;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.parallel.process.AbstractProcess;

public class DataStoreQueryTask extends AbstractProcess {

  private final RecordStore dataStore;

  private final BoundingBox boundingBox;

  private List<Record> objects;

  private final String path;

  public DataStoreQueryTask(final RecordStore dataStore, final String path,
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
    objects = new ArrayList<Record>();
    final RecordDefinition metaData = dataStore.getRecordDefinition(path);
    final Query query = new Query(metaData);
    final Attribute geometryAttribute = metaData.getGeometryAttribute();
    query.setWhereCondition(F.envelopeIntersects(geometryAttribute, boundingBox));
    try (
      final Reader<Record> reader = dataStore.query(query)) {
      for (final Record object : reader) {
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
