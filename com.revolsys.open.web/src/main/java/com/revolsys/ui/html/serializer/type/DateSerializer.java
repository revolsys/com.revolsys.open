package com.revolsys.ui.html.serializer.type;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DateFormat;

import com.revolsys.record.io.format.xml.XmlWriter;

/**
 * Serialize a date with just the date fields.
 *
 * @author Paul Austin
 */
public class DateSerializer implements TypeSerializer {
  /** The date format style. */
  private int dateStyle = DateFormat.DEFAULT;

  /**
   * Get the dete style.
   *
   * @return The date style.
   */
  public int getDateStyle() {
    return this.dateStyle;
  }

  /**
   * Serialize the value to the XML writer.
   *
   * @param out The XML writer to serialize to.
   * @param value The object to get the value from.
   * @throws IOException If there was an I/O error serializing the value.
   */
  @Override
  public void serialize(final XmlWriter out, final Object value) {
    final DateFormat dateFormat = DateFormat.getDateInstance(this.dateStyle);
    out.text(dateFormat.format(value));
  }

  /**
   * Set the dete style.
   *
   * @param dateStyle The date style.
   */
  public void setDateStyle(final int dateStyle) {
    this.dateStyle = dateStyle;
  }

  /**
   * Set the name of the style for use by
   * {@link DateFormat#getDateInstance(int)}.
   *
   * @param styleName The name of the date format style;
   */
  public void setDateStyle(final String styleName) {
    try {
      final Field styleField = DateFormat.class.getField(styleName.toUpperCase());
      setDateStyle(styleField.getInt(DateFormat.class));
    } catch (final SecurityException e) {
      throw new RuntimeException(e.getMessage(), e);
    } catch (final NoSuchFieldException e) {
      throw new IllegalArgumentException(styleName + " is not a valid DateFormat style");
    } catch (final IllegalAccessException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

}
