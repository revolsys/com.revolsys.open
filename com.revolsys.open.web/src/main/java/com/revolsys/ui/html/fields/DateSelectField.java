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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.form.Form;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;

public class DateSelectField extends Field {

  private static final String DAY_KEY = "Day";

  private static final List<FieldValue> DAY_OPTIONS;

  private static final String MONTH_KEY = "Month";

  private static final List<FieldValue> MONTH_OPTIONS;

  private static final String[] MONTHS = {
    "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
  };

  private static final String YEAR_KEY = "Year";

  static {
    final List<FieldValue> dayOptions = new ArrayList<>();
    for (byte i = 1; i <= 31; i++) {
      final String val = String.valueOf(i);
      dayOptions.add(new FieldValue(new Byte(i), val, val));
    }
    DAY_OPTIONS = Collections.unmodifiableList(dayOptions);
    final List<FieldValue> monthOptions = new ArrayList<>();
    for (byte i = 0; i < MONTHS.length; i++) {
      final String val = String.valueOf(i);
      monthOptions.add(new FieldValue(new Byte(i), val, MONTHS[i]));
    }
    MONTH_OPTIONS = Collections.unmodifiableList(monthOptions);
  }

  private static int getYear(final int offset) {
    final Calendar date = new GregorianCalendar();
    date.add(Calendar.YEAR, offset);
    return date.get(Calendar.YEAR);
  }

  private String dayStringValue;

  private final int endYear;

  private final Logger log = LoggerFactory.getLogger(DateSelectField.class);

  private String monthStringValue;

  private final int startYear;

  private final List<FieldValue> yearOptions = new ArrayList<>();

  private String yearStringValue;

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
  public DateSelectField(final String name, final boolean required, final int startYear,
    final int endYear) {
    super(name, required);
    for (int i = startYear; i <= endYear; i++) {
      final String val = String.valueOf(i);
      this.yearOptions.add(new FieldValue(i, val, val));
    }
    this.startYear = startYear;
    this.endYear = endYear;
  }

  @Override
  public boolean hasValue() {
    return this.dayStringValue != null && !this.dayStringValue.equals("")
      && this.monthStringValue != null && !this.monthStringValue.equals("")
      && this.yearStringValue != null && !this.yearStringValue.equals("");
  }

  @Override
  public void initialize(final Form form, final HttpServletRequest request) {
    this.dayStringValue = request.getParameter(getName() + DAY_KEY);
    this.monthStringValue = request.getParameter(getName() + MONTH_KEY);
    this.yearStringValue = request.getParameter(getName() + YEAR_KEY);
    final Date date = (Date)getInitialValue(request);
    final Calendar calendar = Calendar.getInstance();
    if (date != null) {
      calendar.setTime(date);
    }
    if (this.dayStringValue == null) {
      this.dayStringValue = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
    }
    if (this.monthStringValue == null) {
      this.monthStringValue = String.valueOf(calendar.get(Calendar.MONTH));
    }
    if (this.yearStringValue == null) {
      this.yearStringValue = String.valueOf(calendar.get(Calendar.YEAR));
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
        day = Integer.parseInt(this.dayStringValue);
      } catch (final NumberFormatException e) {
        addValidationError("Invalid Day");
        valid = false;
      }
      try {
        month = Integer.parseInt(this.monthStringValue);
      } catch (final NumberFormatException e) {
        addValidationError("Invalid Month");
        valid = false;
      }
      try {
        year = Integer.parseInt(this.yearStringValue);
        if (year < this.startYear || year > this.endYear) {
          addValidationError("Year must be between " + this.startYear + " and " + this.endYear);
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
          this.log.debug(e.getMessage(), e);
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
    serializeSelect(out, YEAR_KEY, this.yearOptions);
  }

  private void serializeOptions(final XmlWriter out, final String part,
    final List<FieldValue> options) {
    String stringValue = "";
    if (part.equals(DAY_KEY)) {
      stringValue = this.dayStringValue;
    } else if (part.equals(MONTH_KEY)) {
      stringValue = this.monthStringValue;
    } else if (part.equals(YEAR_KEY)) {
      stringValue = this.yearStringValue;
    }
    for (final FieldValue option : options) {
      out.startTag(HtmlElem.OPTION);
      if (option.getStringValue().equals(stringValue)) {
        out.attribute(HtmlAttr.SELECTED, "true");
      }
      if (!option.getStringValue().equals(option.getLabel())) {
        out.attribute(HtmlAttr.VALUE, option.getStringValue());
      }
      out.text(option.getLabel());
      out.endTag(HtmlElem.OPTION);
    }
  }

  private void serializeSelect(final XmlWriter out, final String part,
    final List<FieldValue> options) {
    final String name = getName() + part;
    out.startTag(HtmlElem.SELECT);
    out.attribute(HtmlAttr.NAME, name);
    out.attribute(HtmlAttr.CLASS, "form-control input-sm");
    serializeOptions(out, part, options);
    out.endTag(HtmlElem.SELECT);
  }
}
