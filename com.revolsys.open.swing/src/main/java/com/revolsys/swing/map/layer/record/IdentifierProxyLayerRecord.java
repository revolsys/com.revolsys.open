package com.revolsys.swing.map.layer.record;

import com.revolsys.identifier.Identifier;

public class IdentifierProxyLayerRecord extends AbstractProxyLayerRecord {
  private final Identifier identifier;

  public IdentifierProxyLayerRecord(final AbstractRecordLayer layer, final Identifier identifier) {
    super(layer);
    this.identifier = identifier;
  }

  @Override
  public Identifier getIdentifier() {
    return this.identifier;
  }
}
