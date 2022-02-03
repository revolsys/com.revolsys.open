package com.revolsys.record.io.format.odata;

import org.jeometry.common.io.PathName;

import com.revolsys.record.schema.RecordStoreSchema;

public class ODataRecordStoreSchema extends RecordStoreSchema {

  public ODataRecordStoreSchema(final ODataRecordStore recordStore) {
    super(recordStore);
  }

  public ODataRecordStoreSchema(final ODataRecordStoreSchema schema, final PathName pathName) {
    super(schema, pathName);
  }

}
