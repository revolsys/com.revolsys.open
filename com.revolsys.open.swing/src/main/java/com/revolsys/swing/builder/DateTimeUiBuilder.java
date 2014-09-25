package com.revolsys.swing.builder;

import java.text.DateFormat;
import java.util.Date;

import com.revolsys.util.DateUtil;

public class DateTimeUiBuilder extends AbstractUiBuilder {

  @Override
  public void appendHtml(final StringBuilder s, final Object object) {
    if (object instanceof Date) {
      final Date date = (Date)object;
      final DateFormat format = getDateFormat();
      s.append(escapeHTML(DateUtil.format(format, date), false, false));
    }

  }

  protected DateFormat getDateFormat() {
    return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
  }
}
