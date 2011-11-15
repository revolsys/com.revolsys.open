package com.revolsys.ui.html.serializer.type;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.revolsys.io.xml.XmlWriter;

/**
 * Serialize a date with just the date fields.
 * 
 * @author Paul Austin
 */
public class BooleanSerializer implements TypeSerializer {
  /** The date format style. */
  private int dateStyle = DateFormat.DEFAULT;

  /**
   * Serialize the value to the XML writer using the settings from the Locale.
   * 
   * @param out The XML writer to serialize to.
   * @param value The object to get the value from.
   * @param locale The locale.
   */
  public void serialize(
    final XmlWriter out,
    final Object value,
    final Locale locale) {
    if (value instanceof Boolean) {
      Boolean bool = (Boolean)value;
      if (bool.booleanValue()) {
        out.text("Yes");
      } else {
        out.text("No");
      }
    } else {
      out.text("No");
    }
  }

  /**
   * Get the date format instance for the locale.
   * 
   * @param locale The locale.
   * @return The date format instance.
   */
  protected DateFormat getDateFormat(
    final Locale locale) {
    return SimpleDateFormat.getDateInstance(dateStyle, locale);
  }

  /**
   * Set the name of the style for use by
   * {@link DateFormat#getDateInstance(int, java.util.Locale)}.
   * 
   * @param styleName The name of the date format style;
   */
  public void setDateStyle(
    final String styleName) {
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
  public void setDateStyle(
    final int dateStyle) {
    this.dateStyle = dateStyle;
  }

}
