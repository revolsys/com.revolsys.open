package com.revolsys.ui.html.serializer.type;

import java.lang.reflect.Field;
import java.text.DateFormat;

/**
 * Serialize a date with the date and time fields.
 *
 * @author Paul Austin
 */
public class DateTimeSerializer extends DateSerializer {
  /** The date format style. */
  private int timeStyle = DateFormat.SHORT;

  /**
   * Get the date format instance.
   *
   * @return The date format instance.
   */
  protected DateFormat getDateFormat() {
    return DateFormat.getDateTimeInstance(getDateStyle(), getTimeStyle());
  }

  /**
   * Get the time style.
   *
   * @return The time style.
   */
  public int getTimeStyle() {
    return this.timeStyle;
  }

  /**
   * Set the time style.
   *
   * @param timeStyle The time style.
   */
  public void setTimeStyle(final int timeStyle) {
    this.timeStyle = timeStyle;
  }

  /**
   * Set the name of the style for use by
   * {@link DateFormat#getDateInstance(int)}.
   *
   * @param styleName The name of the date format style;
   */
  public void setTimeStyle(final String styleName) {
    try {
      final Field styleField = DateFormat.class.getField(styleName.toUpperCase());
      setTimeStyle(styleField.getInt(DateFormat.class));
    } catch (final SecurityException e) {
      throw new RuntimeException(e.getMessage(), e);
    } catch (final NoSuchFieldException e) {
      throw new IllegalArgumentException(styleName + " is not a valid DateFormat style");
    } catch (final IllegalAccessException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
