package com.revolsys.gis.converter.process;

import java.util.HashMap;
import java.util.Map;

import com.revolsys.gis.data.model.DataObject;

public class SourceToTargetAttributeMapping implements
  SourceToTargetProcess<DataObject, DataObject> {
  private Map<String, SourceToTargetProcess<DataObject, DataObject>> targetAttributeMappings = new HashMap<String, SourceToTargetProcess<DataObject, DataObject>>();

  public SourceToTargetAttributeMapping() {
  }

  public SourceToTargetAttributeMapping(
    final Map<String, SourceToTargetProcess<DataObject, DataObject>> targetAttributeMappings) {
    this.targetAttributeMappings = targetAttributeMappings;
  }

  public void process(
    final DataObject source,
    final DataObject target) {
    for (final SourceToTargetProcess<DataObject, DataObject> mapping : targetAttributeMappings.values()) {
      mapping.process(source, target);
    }
  }

  @Override
  public String toString() {
    return "mapping=" + targetAttributeMappings;
  }
}
