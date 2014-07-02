package com.revolsys.swing.map.layer.openstreetmap;

import com.revolsys.gis.data.model.RecordIdentifier;
import com.revolsys.io.openstreetmap.model.OsmDocument;
import com.revolsys.io.openstreetmap.model.OsmElement;
import com.revolsys.swing.map.layer.record.AbstractDataObjectLayer;
import com.revolsys.swing.map.layer.record.ArrayLayerRecord;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.ProxyLayerRecord;

public class OsmProxyLayerRecord extends ProxyLayerRecord {
  private final OsmDocument document;

  public OsmProxyLayerRecord(final OpenStreetMapApiLayer layer,
    final OsmDocument document, final RecordIdentifier identifier) {
    super(layer, identifier);
    this.document = document;
  }

  @Override
  protected LayerRecord getLayerRecord() {
    final RecordIdentifier identifier = getIdentifier();
    final OsmElement record = this.document.getRecord(identifier);
    final AbstractDataObjectLayer layer = getLayer();
    return new ArrayLayerRecord(layer, record);
  }
}
