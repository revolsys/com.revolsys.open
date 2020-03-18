package com.revolsys.swing.builder;

import java.text.DateFormat;

public class DateUIBuilder extends DateTimeUiBuilder {

  @Override
  protected DateFormat getDateFormat() {
    return DateFormat.getDateInstance(DateFormat.MEDIUM);
  }
}
