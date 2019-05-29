package org.jeometry.common.data.type;

import java.util.Arrays;
import java.util.Collection;

public interface DataTypeProxy {
  default boolean equals(final Object value1, final Object value2) {
    final DataType dataType = getDataType();
    return dataType.equals(value1, value2);
  }

  default boolean equals(final Object value1, final Object value2,
    final CharSequence... excludeFieldNames) {
    final DataType dataType = getDataType();
    return dataType.equals(value1, value2, Arrays.asList(excludeFieldNames));
  }

  default boolean equals(final Object value1, final Object value2,
    final Collection<? extends CharSequence> excludeFieldNames) {
    final DataType dataType = getDataType();
    return dataType.equals(value1, value2, excludeFieldNames);
  }

  DataType getDataType();
}
