package com.revolsys.record.io.format.esri.rest.map;

import com.revolsys.record.io.format.esri.rest.AbstractMapWrapper;

public class TimeReference extends AbstractMapWrapper {
  public Boolean getRespectsDaylightSaving() {
    return getValue("respectsDaylightSaving");
  }

  public String getTimeZone() {
    return getValue("timeZone");
  }

}
