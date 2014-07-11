package com.revolsys.data.io;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.data.query.Query;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.Reader;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.parallel.process.AbstractProcess;

public class RecordStoreQueryTask extends AbstractProcess {

  private final RecordStore recordStore;

  private final BoundingBox boundingBox;

  private List<Record> objects;

  private final String path;

  public RecordStoreQueryTask(final RecordStore recordStore, final String path,
    final BoundingBox boundingBox) {
    this.recordStore = recordStore;
    this.path = path;
    this.boundingBox = boundingBox;
  }

  public void cancel() {
    this.objects = null;
  }

  @Override
  public String getBeanName() {
    return getClass().getName();
  }

  @Override
  public void run() {
    this.objects = new ArrayList<Record>();
    final RecordDefinition recordDefinition = this.recordStore.getRecordDefinition(this.path);
    final Query query = Query.intersects(recordDefinition, this.boundingBox);
    try (
        final Reader<Record> reader = this.recordStore.query(query)) {
      for (final Record object : reader) {
        try {
          this.objects.add(object);
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
