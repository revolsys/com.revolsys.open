package com.revolsys.io.ecsv.type;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.revolsys.gis.data.model.types.DataType;

public abstract class NumberFieldType extends AbstractEcsvFieldType {
  private static final NumberFormat NUMBER_FORMAT = new DecimalFormat(
    "#.#########################");

  public NumberFieldType(final DataType dataType) {
    super(dataType);
  }

  @Override
  public void writeValue(final PrintWriter out, final Object value) {
    if (value instanceof Number) {
      final Number number = (Number)value;
      final String formattedNumber = NUMBER_FORMAT.format(number);
      out.print(formattedNumber);
    }
  }
}
