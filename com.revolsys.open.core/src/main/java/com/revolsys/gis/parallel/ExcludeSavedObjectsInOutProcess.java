package com.revolsys.gis.parallel;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.util.GeometryProperties;
import com.revolsys.gis.io.StatisticsMap;
import com.revolsys.identifier.Identifier;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;
import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;

public class ExcludeSavedObjectsInOutProcess extends BaseInOutProcess<Record, Record> {

  private Set<String> originalIds = new HashSet<String>();

  private StatisticsMap statistics = new StatisticsMap(
    "Excluded as already loaded from previous area");

  @Override
  protected void destroy() {
    this.statistics.disconnect();
    this.originalIds = null;
    this.statistics = null;
  }

  private String getId(final Record object) {
    final Identifier id = object.getIdentifier();
    if (id == null) {
      return null;
    } else {
      final RecordDefinition recordDefinition = object.getRecordDefinition();
      return recordDefinition.getPath() + "." + id;
    }
  }

  public StatisticsMap getStatistics() {
    return this.statistics;
  }

  @Override
  protected void init() {
    this.statistics.connect();
  }

  @Override
  protected void process(final Channel<Record> in, final Channel<Record> out, final Record object) {
    final String id = getId(object);
    if (id == null) {
      out.write(object);
    } else if (this.originalIds.contains(id.toString())) {
      this.statistics.add("Excluded as already loaded from previous area", object);
    } else {
      final Set<String> ids = Collections.singleton(id);
      final Geometry geometry = object.getGeometry();
      GeometryProperties.setGeometryProperty(geometry, "ORIGINAL_IDS", ids);
      out.write(object);
    }
  }

  public void setObjects(final Collection<? extends Record> objects) {
    for (final Record object : objects) {
      final Set<String> ids = object.getValueByPath("GEOMETRY.ORIGINAL_IDS");
      if (ids != null) {
        this.originalIds.addAll(ids);
      }
    }
  }

  public void setStatistics(final StatisticsMap statistics) {
    this.statistics = statistics;
  }
}
