package com.revolsys.ui.html.serializer.type;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Serialize a date with the date and time fields.
 * 
 * @author Paul Austin
 */
public class DateTimeSerializer extends DateSerializer {
  /** The date format style. */
  private int timeStyle = DateFormat.SHORT;

  /**
   * Get the date format instance for the locale.
   * 
   * @param locale The locale.
   * @return The date format instance.
   */
  protected DateFormat getDateFormat(final Locale locale) {
    return SimpleDateFormat.getDateTimeInstance(getDateStyle(), getTimeStyle(),
      locale);
  }

  /**
   * Set the name of the style for use by
   * {@link DateFormat#getDateInstance(int, java.util.Locale)}.
   * 
   * @param styleName The name of the date format style;
   */
  public void setTimeStyle(final String styleName) {
    try {
      Field styleField = DateFormat.class.getField(styleName.toUpperCase());
      setTimeStyle(styleField.getInt(DateFormat.class));
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
   * Get the time style.
   * 
   * @return The time style.
   */
  public int getTimeStyle() {
    return timeStyle;
  }

  /**
   * Set the time style.
   * 
   * @param timeStyle The time style.
   */
  public void setTimeStyle(final int timeStyle) {
    this.timeStyle = timeStyle;
  }
}
