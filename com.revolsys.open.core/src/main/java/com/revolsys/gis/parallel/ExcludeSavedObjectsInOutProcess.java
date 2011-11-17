package com.revolsys.gis.parallel;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.io.Statistics;
import com.revolsys.gis.jts.JtsGeometryUtil;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;
import com.vividsolutions.jts.geom.Geometry;

public class ExcludeSavedObjectsInOutProcess extends
  BaseInOutProcess<DataObject, DataObject> {

  private Set<String> originalIds = new HashSet<String>();

  private Statistics statistics = new Statistics(
    "Excluded as already loaded from previous area");

  @Override
  protected void destroy() {
    statistics.disconnect();
    originalIds = null;
    statistics = null;
  }

  public Statistics getStatistics() {
    return statistics;
  }

  @Override
  protected void init() {
    statistics.connect();
  }

  @Override
  protected void process(final Channel<DataObject> in,
    final Channel<DataObject> out, final DataObject object) {
    final String id = getId(object);
    if (id == null) {
      out.write(object);
    } else if (originalIds.contains(id.toString())) {
      statistics.add(object);
    } else {
      final Set<String> ids = Collections.singleton(id);
      final Geometry geometry = object.getGeometryValue();
      JtsGeometryUtil.setGeometryProperty(geometry, "ORIGINAL_IDS", ids);
    }
  }

  private String getId(final DataObject object) {
    final Object id = object.getIdValue();
    if (id == null) {
      return null;
    } else {
      DataObjectMetaData metaData = object.getMetaData();
      return metaData.getName() + "." + id;
    }
  }

  public void setObjects(final Collection<? extends DataObject> objects) {
    for (final DataObject object : objects) {
      final Set<String> ids = object.getValueByPath("GEOMETRY.ORIGINAL_IDS");
      if (ids != null) {
        originalIds.addAll(ids);
      }
    }
  }

  public void setStatistics(final Statistics statistics) {
    this.statistics = statistics;
  }
}
