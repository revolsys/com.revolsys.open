package com.revolsys.swing.field;

import java.util.List;
import java.util.function.Supplier;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.io.PathName;

import com.revolsys.io.Reader;
import com.revolsys.record.Record;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;

public class RecordStoreQueryField extends AbstractRecordQueryField {
  private static final long serialVersionUID = 1L;

  public static Supplier<Field> factory(final String fieldName, final RecordStore recordStore,
    final PathName typePath, final String displayFieldName) {
    return () -> {
      return new RecordStoreQueryField(fieldName, recordStore, typePath, displayFieldName);
    };
  }

  public static Supplier<Field> factory(final String fieldName, final RecordStore recordStore,
    final String typePath, final String displayFieldName) {
    return factory(fieldName, recordStore, PathName.newPathName(typePath), displayFieldName);
  }

  private final RecordDefinition recordDefinition;

  private final RecordStore recordStore;

  public RecordStoreQueryField(final String fieldName, final RecordStore recordStore,
    final PathName typePath, final String displayFieldName) {
    super(fieldName, typePath,
      recordStore.getRecordDefinition(typePath).getFieldDefinition(displayFieldName));
    this.recordStore = recordStore;
    this.recordDefinition = recordStore.getRecordDefinition(typePath);
  }

  @Override
  public Field clone() {
    final String fieldName = getFieldName();
    final PathName typePath = getTypePath();
    final String displayFieldName = getDisplayFieldName();
    return new RecordStoreQueryField(fieldName, this.recordStore, typePath, displayFieldName);
  }

  @Override
  protected Record getRecord(final Identifier identifier) {
    final PathName typePath = getTypePath();
    return this.recordStore.getRecord(typePath, identifier);
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  @Override
  protected List<Record> getRecords(final Query query) {
    try (
      Reader<Record> reader = this.recordStore.getRecords(query)) {
      return reader.toList();
    }
  }
}
