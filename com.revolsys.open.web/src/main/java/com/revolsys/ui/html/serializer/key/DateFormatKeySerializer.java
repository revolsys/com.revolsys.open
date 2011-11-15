package com.revolsys.ui.html.serializer.key;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.revolsys.io.xml.io.XmlWriter;
import com.revolsys.util.JavaBeanUtil;

/**
 * Serialize a date with the specified date format
 * 
 * @author Paul Austin
 */
public class DateFormatKeySerializer implements KeySerializer {

  /** The date format style. */
  private String dateFormat = "dd-MMM-yyyy";

  /**
   * Construct a new DateFormatKeySerializer.
   */
  public DateFormatKeySerializer() {
  }

  /**
   * Construct a new DateFormatKeySerializer.
   * 
   * @param dateFormat The date format pattern.
   */
  public DateFormatKeySerializer(
    final String dateFormat) {
    this.dateFormat = dateFormat;
  }

  /**
   * Serialize the value to the XML writer using the settings from the Locale.
   * 
   * @param out The XML writer to serialize to.
   * @param object The object to get the value from.
   * @param key The key of the property on the object to serialize.
   * @param locale The locale.
   */
  public void serialize(
    final XmlWriter out,
    final Object object,
    final String key,
    final Locale locale) {
    Object value = JavaBeanUtil.getProperty(object, key);
    DateFormat format = new SimpleDateFormat(dateFormat, locale);
    if (value == null) {
      out.text("-");
    } else if (value instanceof Date) {
      out.text(format.format(value));
    } else {
      out.text(value);
    }
  }

  /**
   * Get the dete format.
   * 
   * @return The date format.
   */
  public String getDateFormat() {
    return dateFormat;
  }

  /**
   * Set the dete format.
   * 
   * @param dateFormat The date format.
   */
  public void setDateFormat(
    final String dateFormat) {
    this.dateFormat = dateFormat;
  }

}
