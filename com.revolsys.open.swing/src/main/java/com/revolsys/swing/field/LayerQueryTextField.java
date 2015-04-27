package com.revolsys.swing.field;

import java.util.List;

import com.revolsys.data.identifier.Identifier;
import com.revolsys.data.query.Query;
import com.revolsys.data.record.Record;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;

public class LayerQueryTextField extends AbstractRecordQueryField {

  private static final long serialVersionUID = 1L;

  private final AbstractRecordLayer layer;

  public LayerQueryTextField(final String fieldName, final AbstractRecordLayer layer,
    final String displayFieldName) {
    super(fieldName, layer.getTypePath(), displayFieldName);
    this.layer = layer;
  }

  @Override
  protected LayerRecord getRecord(final Identifier identifier) {
    return this.layer.getRecordById(identifier);
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  @Override
  protected List<Record> getRecords(final Query query) {
    return (List)this.layer.query(query);
  }
}
