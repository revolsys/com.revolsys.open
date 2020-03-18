package com.revolsys.ui.html.serializer.type;

import java.text.DateFormat;

/**
 * Serialize a date with the date and timestamp fields.
 *
 * @author Paul Austin
 */
public class TimestampSerializer extends DateTimeSerializer {
  /**
   * Construct a new TimestampSerializer.
   */
  public TimestampSerializer() {
    setTimeStyle(DateFormat.LONG);
  }
}
