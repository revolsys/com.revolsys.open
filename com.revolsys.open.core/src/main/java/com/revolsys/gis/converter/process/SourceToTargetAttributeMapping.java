package com.revolsys.gis.converter.process;

import java.util.HashMap;
import java.util.Map;

import com.revolsys.data.record.Record;

public class SourceToTargetAttributeMapping extends
  AbstractSourceToTargetProcess<Record, Record> {
  private Map<String, SourceToTargetProcess<Record, Record>> targetAttributeMappings = new HashMap<String, SourceToTargetProcess<Record, Record>>();

  public SourceToTargetAttributeMapping() {
  }

  public SourceToTargetAttributeMapping(
    final Map<String, SourceToTargetProcess<Record, Record>> targetAttributeMappings) {
    this.targetAttributeMappings = targetAttributeMappings;
  }

  @Override
  public void close() {
    for (final SourceToTargetProcess<Record, Record> process : targetAttributeMappings.values()) {
      process.close();
    }
  }

  @Override
  public void init() {
    for (final SourceToTargetProcess<Record, Record> process : targetAttributeMappings.values()) {
      process.init();
    }
  }

  @Override
  public void process(final Record source, final Record target) {
    for (final SourceToTargetProcess<Record, Record> mapping : targetAttributeMappings.values()) {
      mapping.process(source, target);
    }
  }

  @Override
  public String toString() {
    return "mapping=" + targetAttributeMappings;
  }
}
