package com.revolsys.propertyeditor;

import java.beans.PropertyEditorSupport;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;

public class SqlDateEditor extends PropertyEditorSupport {
  private final DateFormat dateFormat;

  public SqlDateEditor(final DateFormat dateFormat) {
    this.dateFormat = dateFormat;
  }

  @Override
  public String getAsText() {
    final Date value = (Date)getValue();
    if (value == null) {
      return "";
    } else {
      return dateFormat.format(value);
    }
  }

  @Override
  public void setAsText(final String text) throws IllegalArgumentException {
    if (text == null || text.trim().length() == 0) {
      setValue(null);
    } else {
      try {
        final java.util.Date date = dateFormat.parse(text);
        final long time = date.getTime();
        final Date sqlDate = new Date(time);
        setValue(sqlDate);
      } catch (final ParseException e) {
        throw new IllegalArgumentException("Could not parse date", e);
      }
    }
  }
}
