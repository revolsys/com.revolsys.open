package com.revolsys.record.io.format.esri.gdb.xml.model;

import com.revolsys.record.io.format.esri.gdb.xml.model.enums.FieldType;

public class CodedValueDomain extends Domain {

  public CodedValueDomain() {
  }

  public CodedValueDomain(final String domainName, final FieldType fieldType,
    final String description) {
    super(domainName, fieldType, description);
  }
}
