package com.revolsys.ui.html.serializer.key;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.util.JavaBeanUtil;

/**
 * Serialize a date with the specified date format
 * 
 * @author Paul Austin
 */
public class DateFormatKeySerializer extends AbstractKeySerializer {

  /** The date format style. */
  private String dateFormat = "dd-MMM-yyyy";

  /**
   * Construct a new DateFormatKeySerializer.
   */
  public DateFormatKeySerializer(final String name, final String dateFormat) {
    super(name);
    this.dateFormat = dateFormat;
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
   * Serialize the value to the XML writer.
   * 
   * @param out The XML writer to serialize to.
   * @param object The object to get the value from.
   */
  public void serialize(final XmlWriter out, final Object object) {
    final Object value = JavaBeanUtil.getProperty(object, getName());
    final DateFormat format = new SimpleDateFormat(dateFormat);
    if (value == null) {
      out.text("-");
    } else if (value instanceof Date) {
      out.text(format.format(value));
    } else {
      out.text(value);
    }
  }

  /**
   * Set the dete format.
   * 
   * @param dateFormat The date format.
   */
  public void setDateFormat(final String dateFormat) {
    this.dateFormat = dateFormat;
  }

}
