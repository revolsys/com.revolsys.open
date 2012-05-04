package com.revolsys.io.ecsv.type;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.io.ecsv.EcsvConstants;

public abstract class AbstractEcsvFieldType implements EcsvFieldType,
  EcsvConstants {

  private final DataType dataType;

  public AbstractEcsvFieldType(final DataType dataType) {
    this.dataType = dataType;
  }

  public DataType getDataType() {
    return dataType;
  }

  public String getTypeName() {
    return dataType.getName();
  }
}
