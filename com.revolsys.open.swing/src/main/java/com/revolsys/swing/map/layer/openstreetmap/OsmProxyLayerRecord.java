package com.revolsys.swing.map.layer.openstreetmap;

import com.revolsys.data.identifier.Identifier;
import com.revolsys.format.openstreetmap.model.OsmDocument;
import com.revolsys.format.openstreetmap.model.OsmElement;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.ArrayLayerRecord;
import com.revolsys.swing.map.layer.record.IdentifierProxyLayerRecord;
import com.revolsys.swing.map.layer.record.LayerRecord;

public class OsmProxyLayerRecord extends IdentifierProxyLayerRecord {
  private final OsmDocument document;

  public OsmProxyLayerRecord(final OpenStreetMapApiLayer layer, final OsmDocument document,
    final Identifier identifier) {
    super(layer, identifier);
    this.document = document;
  }

  @Override
  protected LayerRecord getLayerRecord() {
    final Identifier identifier = getIdentifier();
    final OsmElement record = this.document.getRecord(identifier);
    final AbstractRecordLayer layer = getLayer();
    return new ArrayLayerRecord(layer, record);
  }
}
