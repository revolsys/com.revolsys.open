package com.revolsys.swing.map.layer.record;

import com.revolsys.identifier.Identifier;

public class IdentifierProxyLayerRecord extends AbstractProxyLayerRecord {
  private Identifier identifier;

  public IdentifierProxyLayerRecord(final AbstractRecordLayer layer, final Identifier identifier) {
    super(layer);
    this.identifier = identifier;
    layer.addProxiedRecordIdentifier(identifier);
  }

  @Override
  protected void finalize() throws Throwable {
    this.identifier = removeProxiedRecordIdentifier(this.identifier);
    super.finalize();
  }

  @Override
  public Identifier getIdentifier() {
    return this.identifier;
  }
}
