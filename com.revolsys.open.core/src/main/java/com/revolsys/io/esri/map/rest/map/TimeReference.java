package com.revolsys.io.esri.map.rest.map;

import com.revolsys.io.esri.map.rest.AbstractMapWrapper;

public class TimeReference extends AbstractMapWrapper {
  public String getTimeZone() {
    return getValue("timeZone");
  }

  public Boolean getRespectsDaylightSaving() {
    return getValue("respectsDaylightSaving");
  }

}
