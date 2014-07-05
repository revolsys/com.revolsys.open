package com.revolsys.gis.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.core.convert.converter.Converter;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.gis.converter.process.SourceToTargetProcess;
import com.revolsys.gis.jts.GeometryProperties;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.util.CollectionUtil;

public class SimpleRecordConveter implements
  Converter<Record, Record> {
  private RecordDefinition recordDefinition;

  private RecordFactory factory;

  private List<SourceToTargetProcess<Record, Record>> processors = new ArrayList<SourceToTargetProcess<Record, Record>>();

  public SimpleRecordConveter() {
  }

  public SimpleRecordConveter(final RecordDefinition recordDefinition) {
    setRecordDefinition(recordDefinition);
  }

  public SimpleRecordConveter(final RecordDefinition recordDefinition,
    final List<SourceToTargetProcess<Record, Record>> processors) {
    setRecordDefinition(recordDefinition);
    this.processors = processors;
  }

  public SimpleRecordConveter(final RecordDefinition recordDefinition,
    final SourceToTargetProcess<Record, Record>... processors) {
    this(recordDefinition, Arrays.asList(processors));
  }

  public void addProcessor(
    final SourceToTargetProcess<Record, Record> processor) {
    processors.add(processor);
  }

  @Override
  public Record convert(final Record sourceObject) {
    final Record targetObject = factory.createRecord(recordDefinition);
    final Geometry sourceGeometry = sourceObject.getGeometryValue();
    final GeometryFactory geometryFactory = sourceGeometry.getGeometryFactory();
    final Geometry targetGeometry = geometryFactory.geometry(sourceGeometry);
    GeometryProperties.copyUserData(sourceGeometry, targetGeometry);
    targetObject.setGeometryValue(targetGeometry);
    for (final SourceToTargetProcess<Record, Record> processor : processors) {
      processor.process(sourceObject, targetObject);
    }
    return targetObject;
  }

  public RecordDefinition getRecordDefinition() {
    return recordDefinition;
  }

  public List<SourceToTargetProcess<Record, Record>> getProcessors() {
    return processors;
  }

  public void setRecordDefinition(final RecordDefinition recordDefinition) {
    this.recordDefinition = recordDefinition;
    this.factory = recordDefinition.getRecordFactory();
  }

  public void setProcessors(
    final List<SourceToTargetProcess<Record, Record>> processors) {
    this.processors = processors;
  }

  @Override
  public String toString() {
    return recordDefinition.getPath() + "\n  "
      + CollectionUtil.toString("\n  ", processors);
  }
}
