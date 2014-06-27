package com.revolsys.gis.parallel;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.RecordIdentifier;
import com.revolsys.gis.io.StatisticsMap;
import com.revolsys.gis.jts.GeometryProperties;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;

public class ExcludeSavedObjectsInOutProcess extends
BaseInOutProcess<DataObject, DataObject> {

  private Set<String> originalIds = new HashSet<String>();

  private StatisticsMap statistics = new StatisticsMap(
    "Excluded as already loaded from previous area");

  @Override
  protected void destroy() {
    this.statistics.disconnect();
    this.originalIds = null;
    this.statistics = null;
  }

  private String getId(final DataObject object) {
    final RecordIdentifier id = object.getIdentifier();
    if (id == null) {
      return null;
    } else {
      final DataObjectMetaData metaData = object.getMetaData();
      return metaData.getPath() + "." + id;
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
  protected void process(final Channel<DataObject> in,
    final Channel<DataObject> out, final DataObject object) {
    final String id = getId(object);
    if (id == null) {
      out.write(object);
    } else if (this.originalIds.contains(id.toString())) {
      this.statistics.add("Excluded as already loaded from previous area",
        object);
    } else {
      final Set<String> ids = Collections.singleton(id);
      final Geometry geometry = object.getGeometryValue();
      GeometryProperties.setGeometryProperty(geometry, "ORIGINAL_IDS", ids);
      out.write(object);
    }
  }

  public void setObjects(final Collection<? extends DataObject> objects) {
    for (final DataObject object : objects) {
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
