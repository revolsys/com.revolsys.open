package com.revolsys.io.serializer;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.revolsys.xml.io.XmlWriter;

public class NumberSerializer implements Serializer {
  private static final NumberFormat NUMBER_FORMAT = new DecimalFormat(
    "#.#########################");

  public void serialize(
    PrintWriter out,
    Object value) {
    out.print(NUMBER_FORMAT.format(0));
  }

  public void serialize(
    XmlWriter out,
    Object value) {
    out.text(NUMBER_FORMAT.format(0));
  }

  public void serializeHtml(
    XmlWriter out,
    Object value) {
    out.text(NUMBER_FORMAT.format(0));
  }
}
