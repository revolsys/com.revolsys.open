/*
 * Copyright 2004-2005 Revolution Systems Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.ui.html.fields;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.form.Form;

public class DateSelectField extends Field {

  private static final String YEAR_KEY = "Year";

  private static final String DAY_KEY = "Day";

  private static final String MONTH_KEY = "Month";

  private static int getYear(final int offset) {
    final Calendar date = new GregorianCalendar();
    date.add(Calendar.YEAR, offset);
    return date.get(Calendar.YEAR);
  }

  private final Logger log = Logger.getLogger(DateSelectField.class);

  private static final List<FieldValue> DAY_OPTIONS;

  private static final List<FieldValue> MONTH_OPTIONS;

  private static final String[] MONTHS = {
    "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct",
    "Nov", "Dec"
  };

  static {
    final List<FieldValue> dayOptions = new ArrayList<FieldValue>();
    for (byte i = 1; i <= 31; i++) {
      final String val = String.valueOf(i);
      dayOptions.add(new FieldValue(new Byte(i), val, val));
    }
    DAY_OPTIONS = Collections.unmodifiableList(dayOptions);
    final List<FieldValue> monthOptions = new ArrayList<FieldValue>();
    for (byte i = 0; i < MONTHS.length; i++) {
      final String val = String.valueOf(i);
      monthOptions.add(new FieldValue(new Byte(i), val, MONTHS[i]));
    }
    MONTH_OPTIONS = Collections.unmodifiableList(monthOptions);
  }

  private final List<FieldValue> yearOptions = new ArrayList<FieldValue>();

  private String dayStringValue;

  private String monthStringValue;

  private String yearStringValue;

  private final int startYear;

  private final int endYear;

  /**
   * @param name
   * @param required
   */
  public DateSelectField(final String name, final boolean required) {
    this(name, required, getYear(-10), getYear(0));
  }

  /**
   * @param name
   * @param required
   */
  public DateSelectField(final String name, final boolean required,
    final int startYear, final int endYear) {
    super(name, required);
    for (int i = startYear; i <= endYear; i++) {
      final String val = String.valueOf(i);
      yearOptions.add(new FieldValue(new Integer(i), val, val));
    }
    this.startYear = startYear;
    this.endYear = endYear;
  }

  @Override
  public boolean hasValue() {
    return dayStringValue != null && !dayStringValue.equals("")
      && monthStringValue != null && !monthStringValue.equals("")
      && yearStringValue != null && !yearStringValue.equals("");
  }

  @Override
  public void initialize(final Form form, final HttpServletRequest request) {
    dayStringValue = request.getParameter(getName() + DAY_KEY);
    monthStringValue = request.getParameter(getName() + MONTH_KEY);
    yearStringValue = request.getParameter(getName() + YEAR_KEY);
    final Date date = (Date)getInitialValue(request);
    final Calendar calendar = Calendar.getInstance();
    if (date != null) {
      calendar.setTime(date);
    }
    if (dayStringValue == null) {
      dayStringValue = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
    }
    if (monthStringValue == null) {
      monthStringValue = String.valueOf(calendar.get(Calendar.MONTH));
    }
    if (yearStringValue == null) {
      yearStringValue = String.valueOf(calendar.get(Calendar.YEAR));
    }

  }

  @Override
  public boolean isValid() {
    boolean valid = true;
    if (!super.isValid()) {
      valid = false;
    } else if (hasValue()) {
      int day = 1;
      int month = 1;
      int year = 1900;
      try {
        day = Integer.parseInt(dayStringValue);
      } catch (final NumberFormatException e) {
        addValidationError("Invalid Day");
        valid = false;
      }
      try {
        month = Integer.parseInt(monthStringValue);
      } catch (final NumberFormatException e) {
        addValidationError("Invalid Month");
        valid = false;
      }
      try {
        year = Integer.parseInt(yearStringValue);
        if (year < startYear || year > endYear) {
          addValidationError("Year must be between " + startYear + " and "
            + endYear);
          valid = false;
        }
      } catch (final NumberFormatException e) {
        addValidationError("Invalid Year");
        valid = false;
      }

      if (valid) {
        try {
          final Calendar date = Calendar.getInstance();
          date.clear();
          date.setLenient(false);
          date.set(year, month, day);
          setValue(date.getTime());
        } catch (final Throwable e) {
          log.debug(e.getMessage(), e);
          addValidationError("Invalid Date");
          valid = false;

        }
      }
    }
    return valid;
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    serializeSelect(out, MONTH_KEY, MONTH_OPTIONS);
    out.entityRef("nbsp");
    serializeSelect(out, DAY_KEY, DAY_OPTIONS);
    out.entityRef("nbsp");
    serializeSelect(out, YEAR_KEY, yearOptions);
  }

  private void serializeOptions(
    final XmlWriter out,
    final String part,
    final List<FieldValue> options) {
    String stringValue = "";
    if (part.equals(DAY_KEY)) {
      stringValue = dayStringValue;
    } else if (part.equals(MONTH_KEY)) {
      stringValue = monthStringValue;
    } else if (part.equals(YEAR_KEY)) {
      stringValue = yearStringValue;
    }
    for (final FieldValue option : options) {
      out.startTag(HtmlUtil.OPTION);
      if (option.getStringValue().equals(stringValue)) {
        out.attribute(HtmlUtil.ATTR_SELECTED, "true");
      }
      if (!option.getStringValue().equals(option.getLabel())) {
        out.attribute(HtmlUtil.ATTR_VALUE, option.getStringValue());
      }
      out.text(option.getLabel());
      out.endTag(HtmlUtil.OPTION);
    }
  }

  private void serializeSelect(
    final XmlWriter out,
    final String part,
    final List<FieldValue> options) {
    final String name = getName() + part;
    out.startTag(HtmlUtil.SELECT);
    out.attribute(HtmlUtil.ATTR_NAME, name);
    serializeOptions(out, part, options);
    out.endTag(HtmlUtil.SELECT);
  }
}
