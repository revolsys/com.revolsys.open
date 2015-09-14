package com.revolsys.gis.parallel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;
import com.revolsys.record.Record;
import com.revolsys.record.comparator.RecordDefinitionNameComparator;
import com.revolsys.record.schema.RecordDefinition;

public class SortByType extends BaseInOutProcess<Record, Record> {

  private final Map<RecordDefinition, Collection<Record>> objectsByType = new TreeMap<RecordDefinition, Collection<Record>>(
    new RecordDefinitionNameComparator());

  @Override
  protected void postRun(final Channel<Record> in, final Channel<Record> out) {
    for (final Collection<Record> objects : this.objectsByType.values()) {
      for (final Record object : objects) {
        out.write(object);
      }
    }
  }

  @Override
  protected void process(final Channel<Record> in, final Channel<Record> out, final Record object) {
    final RecordDefinition recordDefinition = object.getRecordDefinition();
    Collection<Record> objects = this.objectsByType.get(recordDefinition);
    if (objects == null) {
      objects = new ArrayList<Record>();
      this.objectsByType.put(recordDefinition, objects);
    }
    objects.add(object);
  }
}
