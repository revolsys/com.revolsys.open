package com.revolsys.gis.ecsv.io.type;

import java.io.PrintWriter;

import javax.xml.namespace.QName;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.types.DataTypes;

public class QNameFieldType extends AbstractEcsvFieldType {
  public QNameFieldType() {
    super(DataTypes.QNAME);
  }

  public Object parseValue(
    final String text) {
    if (StringUtils.hasLength(text)) {
         return QName.valueOf(text);
  } else {
    return null;
  }
 }

  public void writeValue(
    final PrintWriter out,
    final Object value) {
        StringFieldType.writeQuotedString(out, value);
  }
}
