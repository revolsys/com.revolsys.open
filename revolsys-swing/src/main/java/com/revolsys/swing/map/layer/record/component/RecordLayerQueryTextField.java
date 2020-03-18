package com.revolsys.swing.map.layer.record.component;

import java.util.List;
import java.util.function.Supplier;

import org.jeometry.common.data.identifier.Identifier;

import com.revolsys.record.Record;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.field.AbstractRecordQueryField;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;

public class RecordLayerQueryTextField extends AbstractRecordQueryField {

  private static final long serialVersionUID = 1L;

  public static Supplier<Field> factory(final AbstractRecordLayer layer, final String fieldName,
    final String displayFieldName) {
    return () -> {
      return new RecordLayerQueryTextField(fieldName, layer, displayFieldName);
    };
  }

  private final AbstractRecordLayer layer;

  public RecordLayerQueryTextField(final String fieldName, final AbstractRecordLayer layer,
    final String displayFieldName) {
    super(fieldName, layer.getPathName(), layer.getFieldDefinition(displayFieldName));
    this.layer = layer;
  }

  @Override
  protected LayerRecord getRecord(final Identifier identifier) {
    return this.layer.getRecordById(identifier);
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.layer.getRecordDefinition();
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  @Override
  protected List<Record> getRecords(final Query query) {
    return (List)this.layer.getRecords(query);
  }
}
