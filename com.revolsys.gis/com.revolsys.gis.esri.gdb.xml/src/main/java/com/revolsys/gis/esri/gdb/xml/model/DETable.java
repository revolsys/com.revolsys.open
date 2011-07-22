package com.revolsys.gis.esri.gdb.xml.model;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.gis.esri.gdb.xml.EsriGeodatabaseXmlConstants;

public class DETable extends DEDataset {

  private boolean hasOID;

  private String oidFieldName;

  private List<Field> fields = new ArrayList<Field>();

  private List<Index> indexes = new ArrayList<Index>();

  private String clsid = "{7A566981-C114-11D2-8A28-006097AFF44E}";

  private String extclsid = "";

  private List<String> relationshipClassNames = new ArrayList<String>();

  private String aliasName;

  private String modelName = "";

  private boolean hasGlobalID;

  private String globalIDFieldName = "";

  private String rasterFieldName = "";

  private List<PropertySetProperty> extensionProperties = new ArrayList<PropertySetProperty>();

  private String subtypeFieldName;

  private String defaultSubtypeCode;

  private List<Subtype> subtypes = new ArrayList<Subtype>();

  private List<ControllerMembership> controllerMemberships = new ArrayList<ControllerMembership>();

  public DETable() {
    setDatasetType(EsriGeodatabaseXmlConstants.DATASET_TYPE_TABLE);
  }

  public DETable(final String clsid) {
    this.clsid = clsid;
  }

  public void addField(final Field field) {
    fields.add(field);
  }

  public void addIndex(final Field field, final boolean unique, final String indexName) {
    final Index index = new Index();
    index.setName(indexName);
    index.setIsUnique(unique);
    index.addField(field);
    addIndex(index);
  }

  public void addIndex(final Index index) {
    this.indexes.add(index);
  }

  public String getAliasName() {
    return aliasName;
  }

  public String getCLSID() {
    return clsid;
  }

  public List<ControllerMembership> getControllerMemberships() {
    return controllerMemberships;
  }

  public String getDefaultSubtypeCode() {
    return defaultSubtypeCode;
  }

  public String getEXTCLSID() {
    return extclsid;
  }

  public List<PropertySetProperty> getExtensionProperties() {
    return extensionProperties;
  }

  public List<Field> getFields() {
    return fields;
  }

  public String getGlobalIDFieldName() {
    return globalIDFieldName;
  }

  public List<Index> getIndexes() {
    return indexes;
  }

  public String getModelName() {
    return modelName;
  }

  public String getOIDFieldName() {
    return oidFieldName;
  }

  public String getRasterFieldName() {
    return rasterFieldName;
  }

  public List<String> getRelationshipClassNames() {
    return relationshipClassNames;
  }

  public String getSubtypeFieldName() {
    return subtypeFieldName;
  }

  public List<Subtype> getSubtypes() {
    return subtypes;
  }

  public boolean isHasGlobalID() {
    return hasGlobalID;
  }

  public boolean isHasOID() {
    return hasOID;
  }

  public void setAliasName(final String aliasName) {
    this.aliasName = aliasName;
  }

  public void setCLSID(final String clsid) {
    this.clsid = clsid;
  }

  public void setControllerMemberships(
    final List<ControllerMembership> controllerMemberships) {
    this.controllerMemberships = controllerMemberships;
  }

  public void setDefaultSubtypeCode(final String defaultSubtypeCode) {
    this.defaultSubtypeCode = defaultSubtypeCode;
  }

  public void setEXTCLSID(final String extclsid) {
    this.extclsid = extclsid;
  }

  public void setExtensionProperties(
    final List<PropertySetProperty> extensionProperties) {
    this.extensionProperties = extensionProperties;
  }

  public void setFields(final List<Field> fields) {
    this.fields = fields;
  }

  public void setGlobalIDFieldName(final String globalIDFieldName) {
    this.globalIDFieldName = globalIDFieldName;
  }

  public void setHasGlobalID(final boolean hasGlobalID) {
    this.hasGlobalID = hasGlobalID;
  }

  public void setHasOID(final boolean hasOID) {
    this.hasOID = hasOID;
  }

  public void setIndexes(final List<Index> indexes) {
    this.indexes = indexes;
  }

  public void setModelName(final String modelName) {
    this.modelName = modelName;
  }

  public void setOIDFieldName(final String oidFieldName) {
    this.oidFieldName = oidFieldName;
  }

  public void setRasterFieldName(final String rasterFieldName) {
    this.rasterFieldName = rasterFieldName;
  }

  public void setRelationshipClassNames(
    final List<String> relationshipClassNames) {
    this.relationshipClassNames = relationshipClassNames;
  }

  public void setSubtypeFieldName(final String subtypeFieldName) {
    this.subtypeFieldName = subtypeFieldName;
  }

  public void setSubtypes(final List<Subtype> subtypes) {
    this.subtypes = subtypes;
  }

}
