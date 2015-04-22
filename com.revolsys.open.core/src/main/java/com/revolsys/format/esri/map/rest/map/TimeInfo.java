package com.revolsys.format.esri.map.rest.map;

import com.revolsys.format.esri.map.rest.AbstractMapWrapper;

public class TimeInfo extends AbstractMapWrapper {
  // "timeExtent" : [<startTime>, <endTime>],

  public TimeReference getTimeReference() {
    return getObject(TimeReference.class, "timeReference");
  }

}
