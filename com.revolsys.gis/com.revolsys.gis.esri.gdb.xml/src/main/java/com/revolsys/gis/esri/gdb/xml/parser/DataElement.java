package com.revolsys.gis.esri.gdb.xml.parser;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.revolsys.gis.cs.BoundingBox;

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

  private List<Field> fields = new ArrayList<Field>();

  private List<Index> indexes = new ArrayList<Index>();

  private SpatialReference spatialReference;

  private String clsid;

  private String extClsid;

  private String relationshipClassNames;

  private String aliasName;

  private String modelName;

  private boolean hasGlobalID;

  private String globalIDFieldName;

  private String rasterFieldName;

  private Set<PropertySet> extensionProperties = new LinkedHashSet<PropertySet>();;

  private List<ControllerMembership> controllerMemberships = new ArrayList<ControllerMembership>();

  private boolean editorTrackingEnabled;

  private String creatorFieldName;

  private String createdAtFieldName;

  private String editorFieldName;

  private String editedAtFieldName;

  private boolean isTimeInUTC;

  private String featureType;

  private String shapeType;

  private String shapeFieldName;

  private boolean hasM;

  private boolean hasZ;

  private boolean hasSpatialIndex;

  private String areaFieldName;

  private String LengthFieldName;

  private Extent extent;

  public String getAliasName() {
    return aliasName;
  }

  public String getAreaFieldName() {
    return areaFieldName;
  }

  public String getCatalogPath() {
    return catalogPath;
  }

  public String getClsid() {
    return clsid;
  }

  public String getCLSID() {
    return clsid;
  }

  public String getConfigurationKeyword() {
    return configurationKeyword;
  }

  public List<ControllerMembership> getControllerMemberships() {
    return controllerMemberships;
  }

  public String getCreatedAtFieldName() {
    return createdAtFieldName;
  }

  public String getCreatorFieldName() {
    return creatorFieldName;
  }

  public String getDatasetType() {
    return datasetType;
  }

  public int getDsid() {
    return dsid;
  }

  public String getEditedAtFieldName() {
    return editedAtFieldName;
  }

  public String getEditorFieldName() {
    return editorFieldName;
  }

  public String getExtClsid() {
    return extClsid;
  }

  public String getEXTCLSID() {
    return extClsid;
  }

  public Set<PropertySet> getExtensionProperties() {
    return extensionProperties;
  }

  public Extent getExtent() {
    return extent;
  }

  public String getFeatureType() {
    return featureType;
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

  public String getLengthFieldName() {
    return LengthFieldName;
  }

  public String getModelName() {
    return modelName;
  }

  public String getName() {
    return name;
  }

  public String getOidFieldName() {
    return oidFieldName;
  }

  public String getRasterFieldName() {
    return rasterFieldName;
  }

  public String getRelationshipClassNames() {
    return relationshipClassNames;
  }

  public String getShapeFieldName() {
    return shapeFieldName;
  }

  public String getShapeType() {
    return shapeType;
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

  public boolean isEditorTrackingEnabled() {
    return editorTrackingEnabled;
  }

  public boolean isHasGlobalID() {
    return hasGlobalID;
  }

  public boolean isHasM() {
    return hasM;
  }

  public boolean isHasOID() {
    return hasOID;
  }

  public boolean isHasSpatialIndex() {
    return hasSpatialIndex;
  }

  public boolean isHasZ() {
    return hasZ;
  }

  public boolean isTimeInUTC() {
    return isTimeInUTC;
  }

  public boolean isVersioned() {
    return versioned;
  }

  public void setAliasName(final String aliasName) {
    this.aliasName = aliasName;
  }

  public void setAreaFieldName(final String areaFieldName) {
    this.areaFieldName = areaFieldName;
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

  public void setClsid(final String clsid) {
    this.clsid = clsid;
  }

  public void setCLSID(final String clsid) {
    this.clsid = clsid;
  }

  public void setConfigurationKeyword(final String configurationKeyword) {
    this.configurationKeyword = configurationKeyword;
  }

  public void setControllerMemberships(
    final List<ControllerMembership> controllerMemberships) {
    this.controllerMemberships = controllerMemberships;
  }

  public void setCreatedAtFieldName(final String createdAtFieldName) {
    this.createdAtFieldName = createdAtFieldName;
  }

  public void setCreatorFieldName(final String creatorFieldName) {
    this.creatorFieldName = creatorFieldName;
  }

  public void setDatasetType(final String datasetType) {
    this.datasetType = datasetType;
  }

  public void setDsid(final int dsid) {
    this.dsid = dsid;
  }

  public void setEditedAtFieldName(final String editedAtFieldName) {
    this.editedAtFieldName = editedAtFieldName;
  }

  public void setEditorFieldName(final String editorFieldName) {
    this.editorFieldName = editorFieldName;
  }

  public void setEditorTrackingEnabled(final boolean editorTrackingEnabled) {
    this.editorTrackingEnabled = editorTrackingEnabled;
  }

  public void setExtClsid(final String extClsid) {
    this.extClsid = extClsid;
  }

  public void setEXTCLSID(final String extClsid) {
    this.extClsid = extClsid;
  }

  public void setExtensionProperties(
    final Set<PropertySet> extensionProperties) {
    this.extensionProperties = extensionProperties;
  }

  public void setExtent(final Extent extent) {
    this.extent = extent;
  }

  public void setFeatureType(final String featureType) {
    this.featureType = featureType;
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

  public void setHasM(final boolean hasM) {
    this.hasM = hasM;
  }

  public void setHasOID(final boolean hasOID) {
    this.hasOID = hasOID;
  }

  public void setHasSpatialIndex(final boolean hasSpatialIndex) {
    this.hasSpatialIndex = hasSpatialIndex;
  }

  public void setHasZ(final boolean hasZ) {
    this.hasZ = hasZ;
  }

  public void setIndexes(final List<Index> indexes) {
    this.indexes = indexes;
  }

  public void setLengthFieldName(final String lengthFieldName) {
    LengthFieldName = lengthFieldName;
  }

  public void setModelName(final String modelName) {
    this.modelName = modelName;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setOidFieldName(final String oidFieldName) {
    this.oidFieldName = oidFieldName;
  }

  public void setRasterFieldName(final String rasterFieldName) {
    this.rasterFieldName = rasterFieldName;
  }

  public void setRelationshipClassNames(final String relationshipClassNames) {
    this.relationshipClassNames = relationshipClassNames;
  }

  public void setShapeFieldName(final String shapeFieldName) {
    this.shapeFieldName = shapeFieldName;
  }

  // public void setIndexes(final List<Index> indexes) {
  // this.indexes = indexes;
  // }

  public void setShapeType(final String shapeType) {
    this.shapeType = shapeType;
  }

  public void setSpatialReference(final SpatialReference spatialReference) {
    this.spatialReference = spatialReference;
  }

  public void setTimeInUTC(final boolean isTimeInUTC) {
    this.isTimeInUTC = isTimeInUTC;
  }

  public void setVersioned(final boolean versioned) {
    this.versioned = versioned;
  }
}
