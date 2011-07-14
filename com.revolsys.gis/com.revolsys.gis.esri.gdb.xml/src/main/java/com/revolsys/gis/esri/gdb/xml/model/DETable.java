package com.revolsys.gis.esri.gdb.xml.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.revolsys.gis.esri.gdb.xml.EsriGeodatabaseXmlConstants;

public class DETable extends DEDataset {

  private boolean hasOID;

  private String oidFieldName;

  private List<Field> fields = new ArrayList<Field>();

  private List<Index> indexes = new ArrayList<Index>();

  private String clsid = "{" + UUID.randomUUID().toString() + "}";

  private String extclsid;

  private List<String> relationshipClassNames = new ArrayList<String>();

  private String aliasName;

  private String modelName;

  private boolean hasGlobalID;

  private String globalIDFieldName;

  private String rasterFieldName;

  private List<PropertySetProperty> extensionProperties = new ArrayList<PropertySetProperty>();

  private String subtypeFieldName;

  private String DefaultSubtypeCode;

  private List<Subtype> subtypes = new ArrayList<Subtype>();

  private List<ControllerMembership> controllerMemberships = new ArrayList<ControllerMembership>();

  public DETable() {
    setDatasetType(EsriGeodatabaseXmlConstants.DATASET_TYPE_TABLE);
  }

  public boolean isHasOID() {
    return hasOID;
  }

  public void setHasOID(boolean hasOID) {
    this.hasOID = hasOID;
  }

  public String getOIDFieldName() {
    return oidFieldName;
  }

  public void setOIDFieldName(String oidFieldName) {
    this.oidFieldName = oidFieldName;
  }

  public List<Field> getFields() {
    return fields;
  }

  public void setFields(List<Field> fields) {
    this.fields = fields;
  }

  public List<Index> getIndexes() {
    return indexes;
  }

  public void setIndexes(List<Index> indexes) {
    this.indexes = indexes;
  }

  public String getCLSID() {
    return clsid;
  }

  public void setCLSID(String clsid) {
    this.clsid = clsid;
  }

  public String getEXTCLSID() {
    return extclsid;
  }

  public void setEXTCLSID(String extclsid) {
    this.extclsid = extclsid;
  }

  public List<String> getRelationshipClassNames() {
    return relationshipClassNames;
  }

  public void setRelationshipClassNames(List<String> relationshipClassNames) {
    this.relationshipClassNames = relationshipClassNames;
  }

  public String getAliasName() {
    return aliasName;
  }

  public void setAliasName(String aliasName) {
    this.aliasName = aliasName;
  }

  public String getModelName() {
    return modelName;
  }

  public void setModelName(String modelName) {
    this.modelName = modelName;
  }

  public boolean isHasGlobalID() {
    return hasGlobalID;
  }

  public void setHasGlobalID(boolean hasGlobalID) {
    this.hasGlobalID = hasGlobalID;
  }

  public String getGlobalIDFieldName() {
    return globalIDFieldName;
  }

  public void setGlobalIDFieldName(String globalIDFieldName) {
    this.globalIDFieldName = globalIDFieldName;
  }

  public String getRasterFieldName() {
    return rasterFieldName;
  }

  public void setRasterFieldName(String rasterFieldName) {
    this.rasterFieldName = rasterFieldName;
  }

  public List<PropertySetProperty> getExtensionProperties() {
    return extensionProperties;
  }

  public void setExtensionProperties(
    List<PropertySetProperty> extensionProperties) {
    this.extensionProperties = extensionProperties;
  }

  public String getSubtypeFieldName() {
    return subtypeFieldName;
  }

  public void setSubtypeFieldName(String subtypeFieldName) {
    this.subtypeFieldName = subtypeFieldName;
  }

  public String getDefaultSubtypeCode() {
    return DefaultSubtypeCode;
  }

  public void setDefaultSubtypeCode(String defaultSubtypeCode) {
    DefaultSubtypeCode = defaultSubtypeCode;
  }

  public List<Subtype> getSubtypes() {
    return subtypes;
  }

  public void setSubtypes(List<Subtype> subtypes) {
    this.subtypes = subtypes;
  }

  public List<ControllerMembership> getControllerMemberships() {
    return controllerMemberships;
  }

  public void setControllerMemberships(
    List<ControllerMembership> controllerMemberships) {
    this.controllerMemberships = controllerMemberships;
  }

  public void addField(Field field) {
    fields.add(field);
  }

}
