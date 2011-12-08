package com.revolsys.jdbc.process;

import com.revolsys.gis.parallel.OutsideBoundaryObjects;
import com.revolsys.jdbc.io.JdbcWriter;

public class JdbcWriterOutsideBoundaryObjects extends OutsideBoundaryObjects {

  private JdbcWriter writer;

  @Override
  public void clear() {
    super.clear();
    writer.commit();
  }

  public JdbcWriter getWriter() {
    return writer;
  }

  public void setWriter(JdbcWriter writer) {
    this.writer = writer;
  }

}
