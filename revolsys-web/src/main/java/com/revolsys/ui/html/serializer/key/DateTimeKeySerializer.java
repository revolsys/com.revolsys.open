package com.revolsys.ui.html.serializer.key;

import java.lang.reflect.Field;
import java.text.DateFormat;

/**
 * Serialize a date with the date and time fields.
 *
 * @author Paul Austin
 */
public class DateTimeKeySerializer extends DateKeySerializer {
  /** The date format style. */
  private int timeStyle = DateFormat.SHORT;

  public DateTimeKeySerializer() {
  }

  public DateTimeKeySerializer(final String name) {
    super(name);
  }

  /**
   * Get the date format instance.
   *
   * @return The date format instance.
   */
  @Override
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
   * {@link DateFormat#getDateTimeInstance(int, int)}
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
