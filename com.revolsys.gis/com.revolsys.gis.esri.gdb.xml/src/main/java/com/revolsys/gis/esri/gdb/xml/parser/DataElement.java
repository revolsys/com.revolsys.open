package com.revolsys.gis.esri.gdb.xml.parser;

import java.util.ArrayList;
import java.util.List;

public class DataElement {
  private String catalogPath;

  private String name;

  private boolean childrenExpanded;

  private String datasetType;

  private int dsid;

  private boolean versioned;

  private boolean canVersion;

  private String configurationKeyword;

  private boolean hasOID;

  private String oidFieldName;

  private List<Field> field = new ArrayList<Field>();

  private List<Index> indexes = new ArrayList<Index>();

  private SpatialReference spatialReference;

  public String getCatalogPath() {
    return catalogPath;
  }

  public String getConfigurationKeyword() {
    return configurationKeyword;
  }

  public String getDatasetType() {
    return datasetType;
  }

  public int getDsid() {
    return dsid;
  }

  public List<Field> getField() {
    return field;
  }

  public List<Index> getIndexes() {
    return indexes;
  }

  public String getName() {
    return name;
  }

  public String getOidFieldName() {
    return oidFieldName;
  }

  public SpatialReference getSpatialReference() {
    return spatialReference;
  }

  public boolean isCanVersion() {
    return canVersion;
  }

  public boolean isChildrenExpanded() {
    return childrenExpanded;
  }

  public boolean isHasOID() {
    return hasOID;
  }

  public boolean isVersioned() {
    return versioned;
  }

  public void setCanVersion(final boolean canVersion) {
    this.canVersion = canVersion;
  }

  public void setCatalogPath(final String catalogPath) {
    this.catalogPath = catalogPath;
  }

  public void setChildrenExpanded(final boolean childrenExpanded) {
    this.childrenExpanded = childrenExpanded;
  }

  public void setConfigurationKeyword(final String configurationKeyword) {
    this.configurationKeyword = configurationKeyword;
  }

  public void setDatasetType(final String datasetType) {
    this.datasetType = datasetType;
  }

  public void setDsid(final int dsid) {
    this.dsid = dsid;
  }

  public void setField(final List<Field> field) {
    this.field = field;
  }

  public void setHasOID(final boolean hasOID) {
    this.hasOID = hasOID;
  }

  public void setIndexes(final List<Index> indexes) {
    this.indexes = indexes;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setOidFieldName(final String oidFieldName) {
    this.oidFieldName = oidFieldName;
  }

  public void setSpatialReference(final SpatialReference spatialReference) {
    this.spatialReference = spatialReference;
  }

  public void setVersioned(final boolean versioned) {
    this.versioned = versioned;
  }
}
