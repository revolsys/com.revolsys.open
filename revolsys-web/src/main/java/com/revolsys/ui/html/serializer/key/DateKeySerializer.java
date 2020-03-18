package com.revolsys.ui.html.serializer.key;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.Date;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.util.Property;

/**
 * Serialize a date with just the date fields.
 *
 * @author Paul Austin
 */
public class DateKeySerializer extends AbstractKeySerializer {
  /** The date format style. */
  private int dateStyle = DateFormat.DEFAULT;

  public DateKeySerializer() {
  }

  public DateKeySerializer(final String name) {
    super(name);
  }

  public DateKeySerializer(final String name, final String label) {
    super(name, label);
  }

  protected DateFormat getDateFormat() {
    return DateFormat.getDateInstance(this.dateStyle);
  }

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
   * @param object The object to get the value from.
   */
  @Override
  public void serialize(final XmlWriter out, final Object object) {
    final Object value = Property.get(object, getName());
    final DateFormat dateFormat = getDateFormat();
    if (value == null) {
      out.text("-");
    } else if (value instanceof Date) {
      out.text(dateFormat.format(value));
    } else {
      out.text(value);
    }
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
