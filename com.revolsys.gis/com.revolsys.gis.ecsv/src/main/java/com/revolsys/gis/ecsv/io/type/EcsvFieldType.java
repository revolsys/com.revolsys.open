package com.revolsys.gis.ecsv.io.type;

import java.io.PrintWriter;

public interface EcsvFieldType {

  void writeValue(
    PrintWriter out,
    Object value);

  Object parseValue(
    String text);
}
