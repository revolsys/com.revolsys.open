package com.revolsys.gis.data.model;

import javax.xml.namespace.QName;

public interface AttributeProperties {

  QName GEOMETRY_FACTORY = new QName("http://gis.revolsys.com/",
    "geometryFactory");

  QName ALLOWED_VALUES = new QName("http://gis.revolsys.com/", "allowedValues");

  QName ATTRIBUTE_ALLOWED_VALUES = new QName("http://gis.revolsys.com/",
    "attributeAllowedValues");

  QName ALLOWED_TYPE_NAMES = new QName("http://gis.revolsys.com/",
    "allowedTypeNames");

  QName ATTRIBUTE_ALLOWED_TYPE_NAMES = new QName("http://gis.revolsys.com/",
    "attributeTypeNames");
}
