package com.revolsys.gis.esri.gdb.file;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.xml.namespace.QName;

import com.revolsys.gis.data.io.AbstractDataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.io.Reader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.esri.gdb.file.swig.EsriFileGdb;
import com.revolsys.gis.esri.gdb.file.swig.Geodatabase;
import com.revolsys.io.Writer;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class EsriFileGeodatabaseDataObjectStore extends AbstractDataObjectStore {

  private Geodatabase geodatabase;

  private String fileName;

  @PostConstruct
  public void initialize() {
    System.loadLibrary("EsriFileGdb");
    geodatabase = new Geodatabase();
    EsriFileGeodatabaseUtil.check(EsriFileGdb.OpenGeodatabase(fileName,
      geodatabase));
  }

  @PreDestroy
  public void destory() {
    EsriFileGeodatabaseUtil.check(EsriFileGdb.CloseGeodatabase(geodatabase));
    geodatabase = null;
  }

  public Writer<DataObject> createWriter() {
    // TODO Auto-generated method stub
    return null;
  }

  public Reader<DataObject> query(QName typeName) {
    // TODO Auto-generated method stub
    return null;
  }

  public Reader<DataObject> query(QName typeName, Envelope envelope) {
    // TODO Auto-generated method stub
    return null;
  }

  public Reader<DataObject> query(QName typeName, Geometry geometry) {
    // TODO Auto-generated method stub
    return null;
  }

  public Reader<DataObject> query(QName typeName, String where,
    Object... arguments) {
    // TODO Auto-generated method stub
    return null;
  }

  public DataObject queryFirst(QName typeName, String where,
    Object... arguments) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void loadSchemaDataObjectMetaData(DataObjectStoreSchema schema,
    Map<QName, DataObjectMetaData> metaDataMap) {
    // TODO Auto-generated method stub

  }

  @Override
  protected void loadSchemas(Map<String, DataObjectStoreSchema> schemaMap) {
  }

}
