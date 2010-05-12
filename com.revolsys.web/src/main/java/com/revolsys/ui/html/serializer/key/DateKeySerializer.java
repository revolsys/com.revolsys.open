package com.revolsys.ui.html.serializer.key;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.revolsys.util.JavaBeanUtil;
import com.revolsys.xml.io.XmlWriter;

/**
 * Serialize a date with just the date fields.
 * 
 * @author Paul Austin
 */
public class DateKeySerializer implements KeySerializer {
   /** The date format style. */
  private int dateStyle = DateFormat.DEFAULT;

  /**
   * Serialize the value to the XML writer using the settings from the Locale.
   * 
   * @param out The XML writer to serialize to.
   * @param object The object to get the value from.
   * @param key The key of the property on the object to serialize.
   * @param locale The locale.
   */
  public void serialize(final XmlWriter out, final Object object,
    final String key, final Locale locale) {
    Object value = JavaBeanUtil.getProperty(object, key);
    DateFormat dateFormat = getDateFormat(locale);
    if (value == null) {
      out.text("-");
    } else if (value instanceof Date) {
      out.text(dateFormat.format(value));
    } else {
      out.text(value);
    }
  }

  /**
   * Get the date format instance for the locale.
   * 
   * @param locale The locale.
   * @return The date format instance.
   */
  protected DateFormat getDateFormat(final Locale locale) {
    return SimpleDateFormat.getDateInstance(dateStyle, locale);
  }

  /**
   * Set the name of the style for use by
   * {@link DateFormat#getDateInstance(int, java.util.Locale)}.
   * 
   * @param styleName The name of the date format style;
   */
  public void setDateStyle(final String styleName) {
    try {
      Field styleField = DateFormat.class.getField(styleName.toUpperCase());
      setDateStyle(styleField.getInt(DateFormat.class));
    } catch (SecurityException e) {
      throw new RuntimeException(e.getMessage(), e);
    } catch (NoSuchFieldException e) {
      throw new IllegalArgumentException(styleName
        + " is not a valid DateFormat style");
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Get the dete style.
   * 
   * @return The date style.
   */
  public int getDateStyle() {
    return dateStyle;
  }

  /**
   * Set the dete style.
   * 
   * @param dateStyle The date style.
   */
  public void setDateStyle(final int dateStyle) {
    this.dateStyle = dateStyle;
  }

}
