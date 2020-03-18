package com.revolsys.webservice;

import java.util.Collections;
import java.util.List;

import org.jeometry.common.io.PathName;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.spring.resource.UrlResource;

public interface WebServiceFeatureLayer extends RecordDefinitionProxy, WebServiceResource {
  default BoundingBox getBoundingBox() {
    return BoundingBox.empty();
  }

  @Override
  default String getIconName() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (recordDefinition == null) {
      return "table";
    } else {
      return recordDefinition.getIconName();
    }
  }

  default int getMaxRecordCount() {
    return Integer.MAX_VALUE;
  }

  @Override
  default String getName() {
    final PathName pathName = getPathName();
    return pathName.getName();
  }

  @Override
  default PathName getPathName() {
    return WebServiceResource.super.getPathName();
  }

  default int getRecordCount(final BoundingBox boundingBox) {
    return 0;
  }

  default int getRecordCount(final Query query) {
    return 0;
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  default <V extends Record> List<V> getRecords(final RecordFactory<V> recordFactory,
    final BoundingBox boundingBox) {
    try (
      RecordReader reader = newRecordReader(recordFactory, boundingBox)) {
      if (reader == null) {
        return Collections.emptyList();
      } else {
        return (List)reader.toList();
      }
    }
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  default <V extends Record> List<V> getRecords(final RecordFactory<V> recordFactory,
    final Query query) {
    try (
      RecordReader reader = newRecordReader(recordFactory, query)) {
      if (reader == null) {
        return Collections.emptyList();
      } else {
        return (List)reader.toList();
      }
    }
  }

  @Override
  default UrlResource getServiceUrl() {
    return null;
  }

  default RecordReader newRecordReader(final BoundingBox boundingBox) {
    return newRecordReader(ArrayRecord.FACTORY, boundingBox);
  }

  default RecordReader newRecordReader(final Query query) {
    return newRecordReader(ArrayRecord.FACTORY, query);
  }

  <V extends Record> RecordReader newRecordReader(final RecordFactory<V> recordFactory,
    final BoundingBox boundingBox);

  <V extends Record> RecordReader newRecordReader(final RecordFactory<V> recordFactory,
    final Query query);
}
