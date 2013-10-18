package com.revolsys.gis.oracle.io;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.jdbc.attribute.JdbcAttributeAdder;

public class OracleClobAttributeAdder extends JdbcAttributeAdder {

  public OracleClobAttributeAdder() {
  }

  @Override
  public Attribute addAttribute(final DataObjectMetaDataImpl metaData,
    final String name, final String dataTypeName, final int sqlType,
    final int length, final int scale, final boolean required) {
    final OracleJdbcClobAttribute attribute = new OracleJdbcClobAttribute(name,
      sqlType, length, required);
    metaData.addAttribute(attribute);
    return attribute;
  }

}
