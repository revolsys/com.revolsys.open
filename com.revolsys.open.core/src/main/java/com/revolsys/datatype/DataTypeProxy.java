package com.revolsys.datatype;

import java.util.Arrays;
import java.util.Collection;

public interface DataTypeProxy {
  default boolean equals(final Object value1, final Object value2) {
    final DataType dataType = getDataType();
    return dataType.equals(value1, value2);
  }

  default boolean equals(final Object value1, final Object value2,
    final Collection<String> excludeFieldNames) {
    final DataType dataType = getDataType();
    return dataType.equals(value1, value2, excludeFieldNames);
  }

  default boolean equals(final Object value1, final Object value2,
    final String... excludeFieldNames) {
    final DataType dataType = getDataType();
    return dataType.equals(value1, value2, Arrays.asList(excludeFieldNames));
  }

  DataType getDataType();
}
