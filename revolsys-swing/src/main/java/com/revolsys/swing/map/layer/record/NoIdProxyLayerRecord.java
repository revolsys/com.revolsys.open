package com.revolsys.swing.map.layer.record;

import org.jeometry.common.data.identifier.Identifier;

import com.revolsys.util.UuidBuilder;

public class NoIdProxyLayerRecord extends AbstractProxyLayerRecord {
  private final Identifier identifier;

  private final LayerRecord record;

  public NoIdProxyLayerRecord(final RecordStoreLayer layer, final LayerRecord record) {
    super(layer);
    this.record = record;
    final UuidBuilder uuidBuilder = new UuidBuilder();
    for (final String fieldName : record.getFieldNames()) {
      final Object value = record.getValue(fieldName);
      if (value != null) {
        uuidBuilder.append(fieldName);
        uuidBuilder.append(value);
      }
    }
    this.identifier = uuidBuilder.newStringIdentifier();
    addProxiedRecord(record);
  }

  @Override
  public Identifier getIdentifier() {
    return this.identifier;
  }

  @Override
  protected LayerRecord getRecordProxied() {
    return this.record;
  }

  @Override
  public int hashCode() {
    return this.identifier.hashCode();
  }
}
