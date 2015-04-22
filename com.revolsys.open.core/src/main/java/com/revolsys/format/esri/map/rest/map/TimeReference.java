package com.revolsys.format.esri.map.rest.map;

import com.revolsys.format.esri.map.rest.AbstractMapWrapper;

public class TimeReference extends AbstractMapWrapper {
  public Boolean getRespectsDaylightSaving() {
    return getValue("respectsDaylightSaving");
  }

  public String getTimeZone() {
    return getValue("timeZone");
  }

}
