package com.revolsys.io.ecsv.type;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.io.ecsv.EcsvConstants;

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
