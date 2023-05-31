package com.revolsys.record.io.format.json;

import java.math.BigDecimal;

public interface JsonCallback {
  default void booleanValue(boolean value) {

  }

  default void comma() {
  }

  default void endArray() {
  }

  default void endDocument() {
  }

  default void endObject() {
  }

  default void label(String label) {
  }

  default void nullValue() {
  }

  default void number(BigDecimal number) {
  }

  default void startArray() {
  }

  default void startDocument() {
  }

  default void startObject() {
  }

  default void string(String string) {
  }

}
