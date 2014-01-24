package com.revolsys.jdbc.io;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.io.Writer;

public interface JdbcWriter extends Writer<DataObject> {

  @Override
  public void flush();
}
