package com.revolsys.gis.parallel;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.ArrayDataObject;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataFactory;
import com.revolsys.gis.data.model.DataObjectUtil;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;

public class CopyProcess extends BaseInOutProcess<DataObject, DataObject> {

  private QName typeName;

  public QName getTypeName() {
    return typeName;
  }

  public void setTypeName(QName typeName) {
    this.typeName = typeName;
  }

  private DataObjectMetaDataFactory metaDataFactory;

  private DataObjectMetaData metaData;

  public DataObjectMetaData getMetaData() {
    return metaData;
  }

  public void setMetaData(DataObjectMetaData metaData) {
    this.metaData = metaData;
  }

  public CopyProcess() {
  }

  @PostConstruct
  protected void init() {
    super.init();
    if (metaData == null) {
      metaData = metaDataFactory.getMetaData(typeName);
    }
  }

  @Override
  protected void process(Channel<DataObject> in, Channel<DataObject> out,
    DataObject object) {
    if (metaData == null) {
      out.write(object);
    } else {
      DataObject newObject = new ArrayDataObject(metaData);
      for (String attributeName : metaData.getAttributeNames()) {
        DataObjectUtil.copyValue(object, attributeName, newObject,
          attributeName);
      }
      out.write(newObject);
    }
  }

  public DataObjectMetaDataFactory getMetaDataFactory() {
    return metaDataFactory;
  }

  public void setMetaDataFactory(DataObjectMetaDataFactory metaDataFactory) {
    this.metaDataFactory = metaDataFactory;
  }

}
