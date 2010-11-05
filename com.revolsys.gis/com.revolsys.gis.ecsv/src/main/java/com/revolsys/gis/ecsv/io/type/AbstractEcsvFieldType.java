package com.revolsys.gis.ecsv.io.type;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.ecsv.io.EcsvConstants;
import com.revolsys.gis.esri.gdb.xml.EsriGeodatabaseXmlConstants;
import com.revolsys.xml.XsiConstants;
import com.revolsys.xml.io.XmlWriter;

public abstract class AbstractEcsvFieldType implements EcsvFieldType,
  EcsvConstants {

  private DataType dataType;

  public AbstractEcsvFieldType(
    DataType dataType) {
    this.dataType = dataType;
  }

  public DataType getDataType() {
    return dataType;
  }

  public QName getTypeName() {
    return dataType.getName();
  }
}
