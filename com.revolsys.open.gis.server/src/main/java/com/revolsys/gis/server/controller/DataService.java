package com.revolsys.gis.server.controller;

import javax.xml.namespace.QName;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreRegistry;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.io.Reader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;

@Controller
public class DataService {
  private DataObjectStoreRegistry dataStores;

  @RequestMapping("/dataStores/{dataStoreName}")
  @ResponseBody
  public DataObjectStore getDataStore(
    @PathVariable("dataStoreName")
    final String dataStoreName) {
    return dataStores.getDataObjectStore(dataStoreName);
  }

  @RequestMapping("/dataStores/{dataStoreName}/schemas/{schemaName}")
  @ResponseBody
  public DataObjectStoreSchema getSchema(
    @PathVariable("dataStoreName")
    final String dataStoreName,
    @PathVariable("schemaName")
    final String schemaName) {
    final DataObjectStore dataStore = dataStores.getDataObjectStore(dataStoreName);
    if (dataStore == null) {
      return null;
    } else {
      return dataStore.getSchema(schemaName);
    }
  }

  @RequestMapping("/dataStores/{dataStoreName}/schemas/{schemaName}/types/{typeName}")
  @ResponseBody
  public DataObjectMetaData getType(
    @PathVariable("dataStoreName")
    final String dataStoreName,
    @PathVariable("schemaName")
    final String schemaName,
    @PathVariable("typeName")
    final String typeName) {
    final DataObjectStore dataStore = dataStores.getDataObjectStore(dataStoreName);
    if (dataStore == null) {
      return null;
    } else {
      final DataObjectStoreSchema schema = dataStore.getSchema(schemaName);
      if (schema == null) {
        return null;
      } else {
        return schema.getMetaData(new QName(schemaName, typeName));
      }
    }
  }

  @RequestMapping("/dataStores/{dataStoreName}/schemas/{schemaName}/types/{typeName}/records")
  @ResponseBody
  public Reader<DataObject> getRecords(
    @PathVariable("dataStoreName")
    final String dataStoreName,
    @PathVariable("schemaName")
    final String schemaName,
    @PathVariable("typeName")
    final String typeName) {
    final DataObjectStore dataStore = dataStores.getDataObjectStore(dataStoreName);
    if (dataStore == null) {
      return null;
    } else {
      final DataObjectStoreSchema schema = dataStore.getSchema(schemaName);
      if (schema == null) {
        return null;
      } else {
        final QName name = new QName(schemaName, typeName);
        final DataObjectMetaData metaData = schema.getMetaData(name);
        if (metaData == null) {
          return null;
        } else {
          return dataStore.query(name);
        }
      }
    }
  }

  public DataObjectStoreRegistry getDataStores() {
    return dataStores;
  }

  public void setDataStores(
    DataObjectStoreRegistry dataStores) {
    this.dataStores = dataStores;
  }
}
