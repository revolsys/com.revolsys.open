package com.revolsys.io.ecsv.type;

import java.io.PrintWriter;
import java.util.Date;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.util.DateUtil;

public class DateFieldType extends AbstractEcsvFieldType {
  private final String dateFormat;

  public DateFieldType(final DataType dataType, final String dateFormat) {
    super(dataType);
    this.dateFormat = dateFormat;
  }

  @Override
  public Object parseValue(final String text) {
    if (StringUtils.hasLength(text)) {
      return DateUtil.parse(dateFormat, text);
    } else {
      return null;
    }
  }

  @Override
  public void writeValue(final PrintWriter out, final Object value) {
    if (value instanceof Date) {
      final Date date = (Date)value;
      final String formattedDate = DateUtil.format(dateFormat, date);
      out.print(formattedDate);
    }
  }
}
