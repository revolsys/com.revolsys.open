package com.revolsys.odata.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;

public class CsdlUtil {
  public static CsdlProperty property(final String name, final EdmPrimitiveTypeKind type) {
    final FullQualifiedName fullQualifiedType = type.getFullQualifiedName();
    return new CsdlProperty()//
      .setName(name)//
      .setType(fullQualifiedType)//
    ;
  }

  public static CsdlProperty propertyInt32(final String name) {
    return property(name, EdmPrimitiveTypeKind.Int32);
  }

  public static CsdlPropertyRef propertyRef(final String ID) {
    final CsdlPropertyRef propertyRef = new CsdlPropertyRef();
    propertyRef.setName(ID);
    return propertyRef;
  }

  public static CsdlProperty propertyString(final String name) {
    return property(name, EdmPrimitiveTypeKind.String);
  }

  public static List<CsdlPropertyRef> proptertyRefList(final String... propertyNames) {
    final List<CsdlPropertyRef> refs = new ArrayList<>();
    for (final String name : propertyNames) {
      final CsdlPropertyRef propertyRef = propertyRef(name);
      refs.add(propertyRef);
    }
    return refs;
  }
}
