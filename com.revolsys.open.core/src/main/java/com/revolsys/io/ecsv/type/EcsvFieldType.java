package com.revolsys.io.ecsv.type;

import java.io.PrintWriter;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.types.DataType;

public interface EcsvFieldType {
  DataType getDataType();

  QName getTypeName();
  
  Object parseValue(
    String text);

  void writeValue(
    PrintWriter out,
    Object value);
}
