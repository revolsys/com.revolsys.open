package com.revolsys.io;

import java.io.PrintWriter;

public class StringPrinter {

  private final String string;

  public StringPrinter(final String string) {
    this.string = string;
  }

  public void write(final PrintWriter out) {
    out.print(this.string);
  }
}
