package com.revolsys.ui.html.serializer;

import java.util.Collection;

public interface RowsTableSerializer extends TableSerializer {
  void setRows(final Collection<?> rows);
}
