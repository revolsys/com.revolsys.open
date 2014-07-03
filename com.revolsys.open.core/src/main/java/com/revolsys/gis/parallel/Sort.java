package com.revolsys.gis.parallel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.revolsys.data.comparator.RecordAttributeComparator;
import com.revolsys.data.record.Record;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;

public class Sort extends BaseInOutProcess<Record, Record> {

  private Comparator<Record> comparator;

  private final List<Record> objects = new ArrayList<Record>();

  private String attributeName;

  public String getAttributeName() {
    return attributeName;
  }

  public Comparator<Record> getComparator() {
    return comparator;
  }

  @Override
  protected void postRun(final Channel<Record> in,
    final Channel<Record> out) {
    if (comparator != null) {
      Collections.sort(objects, comparator);
    }
    for (final Record object : objects) {
      out.write(object);
    }
  }

  @Override
  protected void process(final Channel<Record> in,
    final Channel<Record> out, final Record object) {
    objects.add(object);
  }

  public void setAttributeName(final String attributeName) {
    this.attributeName = attributeName;
    this.comparator = new RecordAttributeComparator(attributeName);
  }

  public void setComparator(final Comparator<Record> comparator) {
    this.comparator = comparator;
  }

}
