package com.revolsys.data.filter;

import java.util.HashSet;
import java.util.Set;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.filter.Filter;

public class TypeNamesFilter implements Filter<Record> {

  private final Set<String> typePaths = new HashSet<String>();

  public TypeNamesFilter() {
  }

  public TypeNamesFilter(final String typePath) {
    typePaths.add(typePath);
  }

  @Override
  public boolean accept(final Record object) {
    final RecordDefinition recordDefinition = object.getRecordDefinition();
    final String typePath = recordDefinition.getPath();
    return typePaths.contains(typePath);
  }

  /**
   * @param typePaths the typePaths to set
   */
  public void setTypeNames(final Set<Object> typePaths) {
    for (final Object name : typePaths) {
      final String typePath = name.toString();
      this.typePaths.add(typePath);
    }
  }

  /**
   * @return the name
   */
  @Override
  public String toString() {
    if (typePaths.size() == 1) {
      return "typePath=" + typePaths.iterator().next();
    } else {
      return "typePath in " + typePaths;
    }
  }

}
