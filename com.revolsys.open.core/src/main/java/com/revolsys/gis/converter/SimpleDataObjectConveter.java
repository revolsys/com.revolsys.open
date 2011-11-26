package com.revolsys.gis.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.core.convert.converter.Converter;

import com.revolsys.gis.converter.process.SourceToTargetProcess;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.jts.JtsGeometryUtil;
import com.revolsys.util.CollectionUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.revolsys.gis.cs.GeometryFactory;

public class SimpleDataObjectConveter implements
  Converter<DataObject, DataObject> {
  private DataObjectMetaData dataObjectMetaData;

  private DataObjectFactory factory;

  private List<SourceToTargetProcess<DataObject, DataObject>> processors = new ArrayList<SourceToTargetProcess<DataObject,DataObject>>();

  public SimpleDataObjectConveter() {
  }

  public SimpleDataObjectConveter(final DataObjectMetaData dataObjectMetaData) {
    setDataObjectMetaData(dataObjectMetaData);
  }

  public SimpleDataObjectConveter(final DataObjectMetaData dataObjectMetaData,
    final List<SourceToTargetProcess<DataObject, DataObject>> processors) {
    setDataObjectMetaData(dataObjectMetaData);
    this.processors = processors;
  }

  public SimpleDataObjectConveter(final DataObjectMetaData dataObjectMetaData,
    final SourceToTargetProcess<DataObject, DataObject>... processors) {
    this(dataObjectMetaData, Arrays.asList(processors));
  }

  public void addProcessor(
    final SourceToTargetProcess<DataObject, DataObject> processor) {
    processors.add(processor);
  }

  public DataObject convert(final DataObject sourceObject) {
    final DataObject targetObject = factory.createDataObject(dataObjectMetaData);
    final Geometry sourceGeometry = sourceObject.getGeometryValue();
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(sourceGeometry);
    final Geometry targetGeometry = geometryFactory.createGeometry(sourceGeometry);
    JtsGeometryUtil.copyUserData(sourceGeometry, targetGeometry);
    targetObject.setGeometryValue(targetGeometry);
    for (final SourceToTargetProcess<DataObject, DataObject> processor : processors) {
      processor.process(sourceObject, targetObject);
    }
    return targetObject;
  }

  public DataObjectMetaData getDataObjectMetaData() {
    return dataObjectMetaData;
  }

  public List<SourceToTargetProcess<DataObject, DataObject>> getProcessors() {
    return processors;
  }

  public void setDataObjectMetaData(final DataObjectMetaData dataObjectMetaData) {
    this.dataObjectMetaData = dataObjectMetaData;
    this.factory = dataObjectMetaData.getDataObjectFactory();
  }

  public void setProcessors(
    final List<SourceToTargetProcess<DataObject, DataObject>> processors) {
    this.processors = processors;
  }

  @Override
  public String toString() {
    return dataObjectMetaData.getName() + "\n  "
      + CollectionUtil.toString(processors, "\n  ");
  }
}
