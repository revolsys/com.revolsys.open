package com.revolsys.gis.ecsv.io.type;

import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.types.DataType;

public class DateFieldType extends AbstractEcsvFieldType {
  private SimpleDateFormat dateFormat;

  public DateFieldType(
    DataType dataType,
    String dateFormat) {
    super(dataType);
    this.dateFormat = new SimpleDateFormat(dateFormat);
  }

  public void writeValue(
    PrintWriter out,
    Object value) {
    if (value instanceof Date) {
      Date date = (Date)value;
      final String formattedDate = dateFormat.format(date);
      out.print(formattedDate);
    }
  }

  public Object parseValue(
    String text) {
    if (StringUtils.hasLength(text)) {
      try {
        return dateFormat.parseObject(text);
      } catch (ParseException e) {
        throw new IllegalArgumentException("Invalid date " + text, e);
      }
    } else {
      return null;
    }
  }
}
