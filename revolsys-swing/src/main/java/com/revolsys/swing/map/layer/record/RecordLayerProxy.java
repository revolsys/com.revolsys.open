package com.revolsys.swing.map.layer.record;

import com.revolsys.record.schema.RecordDefinitionProxy;

public interface RecordLayerProxy extends RecordDefinitionProxy {

  <L extends AbstractRecordLayer> L getRecordLayer();
}
