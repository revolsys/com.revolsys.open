package com.revolsys.geopackage;

import com.revolsys.jdbc.io.JdbcRecordWriter;
import com.revolsys.record.io.RecordStoreRecordWriter;
import com.revolsys.record.schema.RecordDefinition;

public class GeoPackageRecordWriter extends RecordStoreRecordWriter {

  public GeoPackageRecordWriter(final GeoPackageRecordStore recordStore,
    final RecordDefinition recordDefinition) {
    super(recordStore, recordDefinition);

  }

  @Override
  public void close() {
    final JdbcRecordWriter writer = (JdbcRecordWriter)getWriter();
    writer.commit();
    super.close();
  }

}
