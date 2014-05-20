package com.revolsys.gis.oracle.io;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.jdbc.attribute.JdbcAttributeAdder;

public class OracleBlobAttributeAdder extends JdbcAttributeAdder {

  public OracleBlobAttributeAdder() {
  }

  @Override
  public Attribute addAttribute(final DataObjectMetaDataImpl metaData,
    final String dbName, final String name, final String dataTypeName,
    final int sqlType, final int length, final int scale,
    final boolean required, final String description) {
    final OracleJdbcBlobAttribute attribute = new OracleJdbcBlobAttribute(
      dbName, name, sqlType, length, required, description);
    metaData.addAttribute(attribute);
    return attribute;
  }

}
