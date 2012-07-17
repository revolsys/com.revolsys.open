package com.revolsys.io.esri.map.rest.map;

import com.revolsys.io.esri.map.rest.AbstractMapWrapper;

public class TimeInfo extends AbstractMapWrapper {
  // "timeExtent" : [<startTime>, <endTime>],

  public TimeReference getTimeReference() {
    return getObject(TimeReference.class, "timeReference");
  }

}
