package com.revolsys.jump.ui.builder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.vividsolutions.jump.workbench.ui.GUIUtil;

public class DateTimeUiBuilder extends AbstractUiBuilder {

  public void appendHtml(final StringBuffer s, final Object object) {
    if (object instanceof Date) {
      Date date = (Date)object;
      DateFormat format = getDateFormat();
      s.append(GUIUtil.escapeHTML(format.format(date), false, false));
    }

  }

  protected DateFormat getDateFormat() {
    return SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM,
      SimpleDateFormat.MEDIUM);
  }
}
