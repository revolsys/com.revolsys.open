package com.revolsys.record.io.format.esri.rest.map;

import com.revolsys.record.io.format.esri.rest.AbstractMapWrapper;

public class TimeInfo extends AbstractMapWrapper {
  // "timeExtent" : [<startTime>, <endTime>],

  public TimeReference getTimeReference() {
    return getObject(TimeReference.class, "timeReference");
  }

}
