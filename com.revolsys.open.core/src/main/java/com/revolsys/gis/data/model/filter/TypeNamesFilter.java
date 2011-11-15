package com.revolsys.gis.data.model.filter;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import com.revolsys.filter.Filter;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;

public class TypeNamesFilter implements Filter<DataObject> {

  private final Set<QName> typeNames = new HashSet<QName>();

  public TypeNamesFilter() {
  }

  public TypeNamesFilter(
    final QName typeName) {
    typeNames.add(typeName);
  }

  public boolean accept(
    final DataObject object) {
    final DataObjectMetaData metaData = object.getMetaData();
    final QName name = metaData.getName();
    return typeNames.contains(name);
  }

  /**
   * @param typeNames the typeNames to set
   */
  public void setTypeNames(
    final Set<Object> typeNames) {
    for (final Object name : typeNames) {
      if (name instanceof QName) {
        final QName typeName = (QName)name;
        this.typeNames.add(typeName);
      } else if (name != null) {
        final QName typeName = QName.valueOf(name.toString());
        this.typeNames.add(typeName);
      }
    }
  }

  /**
   * @return the name
   */
  @Override
  public String toString() {
    if (typeNames.size() == 1) {
      return "typeName=" + typeNames.iterator().next();
    } else {
      return "typeName in " + typeNames;
    }
  }

}
