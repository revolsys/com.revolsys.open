package com.revolsys.io;

import java.io.PrintWriter;

public class StringPrinter {

  private String string;

  public StringPrinter(String string) {
    this.string = string;
  }

  public void write(PrintWriter out) {
    out.print(string);
  }
}
