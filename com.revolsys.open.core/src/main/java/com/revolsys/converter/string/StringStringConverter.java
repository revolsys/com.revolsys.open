package com.revolsys.converter.string;

public class StringStringConverter implements StringConverter<String> {
  public boolean requiresQuotes() {
    return true;
  }

  public String toString(String string) {
    return string;
  }

  public String toObject(Object value) {
    if (value == null) {
      return null;
    } else {
      return value.toString();
    }
  }

  public String toObject(String string) {
    return string;
  }

}
