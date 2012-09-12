package com.revolsys.io.ecsv.type;

import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.types.DataType;

public class DateFieldType extends AbstractEcsvFieldType {
  private final SimpleDateFormat dateFormat;

  public DateFieldType(final DataType dataType, final String dateFormat) {
    super(dataType);
    this.dateFormat = new SimpleDateFormat(dateFormat);
  }

  @Override
  public Object parseValue(final String text) {
    if (StringUtils.hasLength(text)) {
      try {
        return dateFormat.parseObject(text);
      } catch (final ParseException e) {
        throw new IllegalArgumentException("Invalid date " + text, e);
      }
    } else {
      return null;
    }
  }

  @Override
  public void writeValue(final PrintWriter out, final Object value) {
    if (value instanceof Date) {
      final Date date = (Date)value;
      final String formattedDate = dateFormat.format(date);
      out.print(formattedDate);
    }
  }
}
