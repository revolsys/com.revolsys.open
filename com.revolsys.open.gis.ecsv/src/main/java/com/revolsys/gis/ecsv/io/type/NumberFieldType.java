package com.revolsys.gis.ecsv.io.type;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.revolsys.gis.data.model.types.DataType;

public abstract class NumberFieldType extends AbstractEcsvFieldType {
  private static final NumberFormat NUMBER_FORMAT = new DecimalFormat(
    "#.#########################");

  public NumberFieldType(
    DataType dataType) {
    super(dataType);
  }

  public void writeValue(
    PrintWriter out,
    Object value) {
    if (value instanceof Number) {
      Number number = (Number)value;
      final String formattedNumber = NUMBER_FORMAT.format(number);
      out.print(formattedNumber);
    }
  }
}
