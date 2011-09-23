package com.revolsys.gis.data.model;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.parallel.channel.ChannelOutput;

public class DataObjectLog {

  private ChannelOutput<DataObject> out;

  private final Map<DataObjectMetaData, DataObjectMetaDataImpl> logMetaDataMap = new HashMap<DataObjectMetaData, DataObjectMetaDataImpl>();

  public void connect() {
    if (out != null) {
      out.writeConnect();
    }
  }

  public void disconnect() {
    if (out != null) {
      out.writeDisconnect();
    }
  }

  public DataObjectMetaData getLogMetaData(final DataObject object) {
    final DataObjectMetaData metaData = object.getMetaData();
    final DataObjectMetaData logMetaData = getLogMetaData(metaData);
    return logMetaData;
  }

  public DataObjectMetaData getLogMetaData(final DataObjectMetaData metaData) {
    DataObjectMetaDataImpl logMetaData = logMetaDataMap.get(metaData);
    if (logMetaData == null) {
      final QName typeName = metaData.getName();
      final String namespaceURI = typeName.getNamespaceURI();
      final String tableName = typeName.getLocalPart();
      final String logTableName;
      if (tableName.toUpperCase().equals(tableName)) {
        logTableName = tableName + "_LOG";
      } else {
        logTableName = tableName + "_log";
      }
      final QName logTypeName = new QName(namespaceURI, logTableName);
      logMetaData = new DataObjectMetaDataImpl(logTypeName);
      logMetaData.addAttribute("LOGMESSAGE", DataTypes.STRING, 255, true);
      for(Attribute attribute: metaData.getAttributes()) {
        Attribute logAttribute = new Attribute(attribute);
        logMetaData.addAttribute(logAttribute);
         
      }
    }
    return logMetaData;
  }

  public ChannelOutput<DataObject> getOut() {
    return out;
  }

  public void log(final Object message, final DataObject object) {
    if (out != null) {
      final DataObjectMetaData logMetaData = getLogMetaData(object);
      final DataObject logObject = new ArrayDataObject(logMetaData, object);
      logObject.setValue("LOGMESSAGE", message);
      out.write(logObject);
    }
  }

  public void setOut(final ChannelOutput<DataObject> out) {
    this.out = out;
  }
}
