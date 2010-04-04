package com.revolsys.jump.ui.builder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class DateUIBuilder extends DateTimeUiBuilder {

  protected DateFormat getDateFormat() {
    return SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM);
  }
}
