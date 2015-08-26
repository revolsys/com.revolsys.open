package com.revolsys.gdal;

import org.gdal.gdal.gdal;

public class GdalException extends RuntimeException {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final int errorNumber = gdal.GetLastErrorNo();

  private final int errorType = gdal.GetLastErrorType();

  public GdalException() {
    super(gdal.GetLastErrorMsg().trim());
    gdal.ErrorReset();
  }

  public int getErrorNumber() {
    return this.errorNumber;
  }

  public int getErrorType() {
    return this.errorType;
  }
}
