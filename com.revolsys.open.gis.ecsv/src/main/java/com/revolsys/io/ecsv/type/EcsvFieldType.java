package com.revolsys.io.ecsv.type;

import java.io.PrintWriter;

import com.revolsys.gis.data.model.types.DataType;

public interface EcsvFieldType {
  DataType getDataType();

  String getTypeName();

  Object parseValue(String text);

  void writeValue(PrintWriter out, Object value);
}
