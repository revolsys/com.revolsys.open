package com.revolsys.datatype;

public interface DataType {

  Class<?> getJavaClass();

  String getName();

  String getValidationName();
}
