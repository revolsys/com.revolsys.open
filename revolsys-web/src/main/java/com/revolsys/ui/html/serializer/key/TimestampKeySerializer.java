package com.revolsys.ui.html.serializer.key;

import java.text.DateFormat;

/**
 * Serialize a date with the date and timestamp fields.
 *
 * @author Paul Austin
 */
public class TimestampKeySerializer extends DateTimeKeySerializer {
  /**
   * Construct a new TimestampKeySerializer.
   */
  public TimestampKeySerializer() {
    setTimeStyle(DateFormat.LONG);
  }

  /**
   * Construct a new TimestampKeySerializer.
   */
  public TimestampKeySerializer(final String name) {
    super(name);
    setTimeStyle(DateFormat.LONG);
  }
}
