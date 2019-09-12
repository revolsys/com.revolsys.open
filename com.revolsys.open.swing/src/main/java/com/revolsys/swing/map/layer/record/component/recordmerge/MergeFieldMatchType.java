package com.revolsys.swing.map.layer.record.component.recordmerge;

import com.revolsys.util.CaseConverter;

enum MergeFieldMatchType {
  ERROR, //
  OVERRIDDEN, //
  ALLOWED_NOT_EQUAL, //
  EQUAL;

  private String label;

  private MergeFieldMatchType() {
    this.label = CaseConverter.toCapitalizedWords(name());
  }

  @Override
  public String toString() {
    return this.label;
  }
}
