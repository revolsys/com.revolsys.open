package com.revolsys.gis.format.mdb;

import java.io.File;
import java.sql.SQLException;

import com.revolsys.gis.jdbc.io.JdbcWriter;

public class MdbWriter extends JdbcWriter {
  static {
    try {
      Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
    } catch (final ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  public MdbWriter(
    final File file)
    throws SQLException {
    super(null);
    // super(DriverManager.getConnection(
    // "jdbc:odbc:Driver={MicroSoft Access Driver (*.mdb)};DBQ="
    // + file.getAbsolutePath(), "admin", ""));
  }
}
