/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.revolsys.gis.esri.gdb.file.capi.swig;

public class Geodatabase {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected Geodatabase(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(Geodatabase obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

   protected void finalize() {
   }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        EsriFileGdbJNI.delete_Geodatabase(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public int GetDatasetTypes(VectorOfWString datasetTypes) {
    return EsriFileGdbJNI.Geodatabase_GetDatasetTypes(swigCPtr, this, VectorOfWString.getCPtr(datasetTypes), datasetTypes);
  }

  public int GetDatasetRelationshipTypes(VectorOfWString relationshipTypes) {
    return EsriFileGdbJNI.Geodatabase_GetDatasetRelationshipTypes(swigCPtr, this, VectorOfWString.getCPtr(relationshipTypes), relationshipTypes);
  }

  public int GetRelatedDatasets(String path, String relType, String datasetType, VectorOfWString relatedDatasets) {
    return EsriFileGdbJNI.Geodatabase_GetRelatedDatasets(swigCPtr, this, path, relType, datasetType, VectorOfWString.getCPtr(relatedDatasets), relatedDatasets);
  }

  public int GetChildDatasetDefinitions(String parentPath, String datasetType, VectorOfString childDatasetDefs) {
    return EsriFileGdbJNI.Geodatabase_GetChildDatasetDefinitions(swigCPtr, this, parentPath, datasetType, VectorOfString.getCPtr(childDatasetDefs), childDatasetDefs);
  }

  public int GetRelatedDatasetDefinitions(String path, String relType, String datasetType, VectorOfString relatedDatasetDefs) {
    return EsriFileGdbJNI.Geodatabase_GetRelatedDatasetDefinitions(swigCPtr, this, path, relType, datasetType, VectorOfString.getCPtr(relatedDatasetDefs), relatedDatasetDefs);
  }

  public int Rename(String path, String datasetType, String newName) {
    return EsriFileGdbJNI.Geodatabase_Rename(swigCPtr, this, path, datasetType, newName);
  }

  public int Move(String path, String newParentPath) {
    return EsriFileGdbJNI.Geodatabase_Move(swigCPtr, this, path, newParentPath);
  }

  public int Delete(String path, String datasetType) {
    return EsriFileGdbJNI.Geodatabase_Delete(swigCPtr, this, path, datasetType);
  }

  public int CompactDatabase() {
    return EsriFileGdbJNI.Geodatabase_CompactDatabase(swigCPtr, this);
  }

  public Geodatabase() {
    this(EsriFileGdbJNI.new_Geodatabase(), true);
  }

  public void createFeatureDataset(String featureDatasetDef) {
    EsriFileGdbJNI.Geodatabase_createFeatureDataset(swigCPtr, this, featureDatasetDef);
  }

  public EnumRows query(String sql, boolean recycling) {
    long cPtr = EsriFileGdbJNI.Geodatabase_query(swigCPtr, this, sql, recycling);
    return (cPtr == 0) ? null : new EnumRows(cPtr, false);
  }

  public VectorOfWString getChildDatasets(String parentPath, String datasetType) {
    return new VectorOfWString(EsriFileGdbJNI.Geodatabase_getChildDatasets(swigCPtr, this, parentPath, datasetType), true);
  }

  public String getDatasetDefinition(String path, String datasetType) {
    return EsriFileGdbJNI.Geodatabase_getDatasetDefinition(swigCPtr, this, path, datasetType);
  }

  public String getDatasetDocumentation(String path, String datasetType) {
    return EsriFileGdbJNI.Geodatabase_getDatasetDocumentation(swigCPtr, this, path, datasetType);
  }

  public VectorOfWString getDomains() {
    return new VectorOfWString(EsriFileGdbJNI.Geodatabase_getDomains(swigCPtr, this), true);
  }

  public String getDomainDefinition(String domainName) {
    return EsriFileGdbJNI.Geodatabase_getDomainDefinition(swigCPtr, this, domainName);
  }

  public void createDomain(String domainDefinition) {
    EsriFileGdbJNI.Geodatabase_createDomain(swigCPtr, this, domainDefinition);
  }

  public void alterDomain(String domainDefinition) {
    EsriFileGdbJNI.Geodatabase_alterDomain(swigCPtr, this, domainDefinition);
  }

  public void deleteDomain(String domainName) {
    EsriFileGdbJNI.Geodatabase_deleteDomain(swigCPtr, this, domainName);
  }

  public String getQueryName(String path) {
    return EsriFileGdbJNI.Geodatabase_getQueryName(swigCPtr, this, path);
  }

  public Table openTable(String path) {
    long cPtr = EsriFileGdbJNI.Geodatabase_openTable(swigCPtr, this, path);
    return (cPtr == 0) ? null : new Table(cPtr, true);
  }

  public void closeTable(Table table) {
    EsriFileGdbJNI.Geodatabase_closeTable(swigCPtr, this, Table.getCPtr(table), table);
  }

  public String getTableDefinition(String path) {
    return EsriFileGdbJNI.Geodatabase_getTableDefinition(swigCPtr, this, path);
  }

  public Table createTable(String tableDefinition, String parent) {
    long cPtr = EsriFileGdbJNI.Geodatabase_createTable(swigCPtr, this, tableDefinition, parent);
    return (cPtr == 0) ? null : new Table(cPtr, true);
  }

}
