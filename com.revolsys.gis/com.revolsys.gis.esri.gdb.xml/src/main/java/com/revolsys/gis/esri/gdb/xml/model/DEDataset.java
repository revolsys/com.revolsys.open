package com.revolsys.gis.esri.gdb.xml.model;

public class DEDataset extends DataElement {
  private String datasetType;

  private int dsid;

  private boolean versioned;

  private boolean canVersion;

  private String configurationKeyword;

  public String getConfigurationKeyword() {
    return configurationKeyword;
  }

  public String getDatasetType() {
    return datasetType;
  }

  public int getDSID() {
    return dsid;
  }

  public boolean isCanVersion() {
    return canVersion;
  }

  public boolean isVersioned() {
    return versioned;
  }

  public void setCanVersion(final boolean canVersion) {
    this.canVersion = canVersion;
  }

  public void setConfigurationKeyword(final String configurationKeyword) {
    this.configurationKeyword = configurationKeyword;
  }

  public void setDatasetType(final String datasetType) {
    this.datasetType = datasetType;
  }

  public void setDSID(final int dsid) {
    this.dsid = dsid;
  }

  public void setVersioned(final boolean versioned) {
    this.versioned = versioned;
  }

}
