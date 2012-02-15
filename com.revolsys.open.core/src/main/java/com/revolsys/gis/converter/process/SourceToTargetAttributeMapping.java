package com.revolsys.gis.converter.process;

import java.util.HashMap;
import java.util.Map;

import com.revolsys.gis.data.model.DataObject;

public class SourceToTargetAttributeMapping extends
  AbstractSourceToTargetProcess<DataObject, DataObject> {
  private Map<String, SourceToTargetProcess<DataObject, DataObject>> targetAttributeMappings = new HashMap<String, SourceToTargetProcess<DataObject, DataObject>>();

  public SourceToTargetAttributeMapping() {
  }

  public SourceToTargetAttributeMapping(
    final Map<String, SourceToTargetProcess<DataObject, DataObject>> targetAttributeMappings) {
    this.targetAttributeMappings = targetAttributeMappings;
  }

  @Override
  public void close() {
    for (final SourceToTargetProcess<DataObject, DataObject> process : targetAttributeMappings.values()) {
      process.close();
    }
  }

  @Override
  public void init() {
    for (final SourceToTargetProcess<DataObject, DataObject> process : targetAttributeMappings.values()) {
      process.init();
    }
  }

  public void process(final DataObject source, final DataObject target) {
    for (final SourceToTargetProcess<DataObject, DataObject> mapping : targetAttributeMappings.values()) {
      mapping.process(source, target);
    }
  }

  @Override
  public String toString() {
    return "mapping=" + targetAttributeMappings;
  }
}
