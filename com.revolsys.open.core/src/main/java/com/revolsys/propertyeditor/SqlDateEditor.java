package com.revolsys.propertyeditor;

import java.beans.PropertyEditorSupport;
import java.sql.Date;

import com.revolsys.util.DateUtil;

public class SqlDateEditor extends PropertyEditorSupport {
  private final String pattern;

  public SqlDateEditor(final String pattern) {
    this.pattern = pattern;
  }

  @Override
  public String getAsText() {
    final Date value = (Date)getValue();
    if (value == null) {
      return "";
    } else {
      return DateUtil.format(pattern, value);
    }
  }

  @Override
  public void setAsText(final String text) throws IllegalArgumentException {
    if (text == null || text.trim().length() == 0) {
      setValue(null);
    } else {
      final java.util.Date date = DateUtil.parse(pattern, text);
      final long time = date.getTime();
      final Date sqlDate = new Date(time);
      setValue(sqlDate);
    }
  }
}
