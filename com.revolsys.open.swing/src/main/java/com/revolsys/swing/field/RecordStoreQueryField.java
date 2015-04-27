package com.revolsys.swing.field;

import java.util.List;

import com.revolsys.data.identifier.Identifier;
import com.revolsys.data.query.Query;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.io.Reader;

public class RecordStoreQueryField extends AbstractRecordQueryField {
  private static final long serialVersionUID = 1L;

  private final RecordStore recordStore;

  public RecordStoreQueryField(final String fieldName, final RecordStore recordStore,
    final String typePath, final String displayFieldName) {
    super(fieldName, typePath, displayFieldName);
    this.recordStore = recordStore;
  }

  @Override
  protected Record getRecord(final Identifier identifier) {
    final String typePath = getTypePath();
    return this.recordStore.load(typePath, identifier);
  }

  @Override
  protected List<Record> getRecords(final Query query) {
    try (
      Reader<Record> reader = this.recordStore.query(query)) {
      return reader.read();
    }
  }

}
