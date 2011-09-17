package com.revolsys.gis.parallel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.comparator.DataObjectAttributeComparator;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;

public class Sort extends BaseInOutProcess<DataObject, DataObject> {

  private Comparator<DataObject> comparator;

  private final List<DataObject> objects = new ArrayList<DataObject>();

  private String attributeName;

  public String getAttributeName() {
    return attributeName;
  }

  public Comparator<DataObject> getComparator() {
    return comparator;
  }

  @Override
  protected void postRun(final Channel<DataObject> in,
    final Channel<DataObject> out) {
    if (comparator != null) {
      Collections.sort(objects, comparator);
    }
    for (final DataObject object : objects) {
      out.write(object);
    }
  }

  @Override
  protected void process(final Channel<DataObject> in,
    final Channel<DataObject> out, final DataObject object) {
    objects.add(object);
  }

  public void setAttributeName(final String attributeName) {
    this.attributeName = attributeName;
    this.comparator = new DataObjectAttributeComparator(attributeName);
  }

  public void setComparator(final Comparator<DataObject> comparator) {
    this.comparator = comparator;
  }

}
