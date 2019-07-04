package com.revolsys.geometry.io;

import com.revolsys.geometry.model.ClockDirection;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.IoFactory;
import com.revolsys.io.Reader;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionBuilder;

public interface GeometryReader extends Reader<Geometry> {
  static boolean isReadable(final Object source) {
    final GeometryReaderFactory factory = IoFactory.factory(GeometryReaderFactory.class, source);
    if (factory == null || !factory.isGeometrySupported()) {
      return false;
    } else {
      return true;
    }
  }

  static GeometryReader newGeometryReader(final Object source) {
    final GeometryReaderFactory factory = IoFactory.factory(GeometryReaderFactory.class, source);
    if (factory == null || !factory.isGeometrySupported()) {
      return null;
    } else {
      return factory.newGeometryReader(source);
    }
  }

  GeometryFactory getGeometryFactory();

  default ClockDirection getPolygonRingDirection() {
    return ClockDirection.NONE;
  }

  default RecordDefinition newRecordDefinition(final String name) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final RecordDefinition recordDefinition = new RecordDefinitionBuilder(name) //
      .addField("GEOMETRY", GeometryDataTypes.GEOMETRY) //
      .setGeometryFactory(geometryFactory) //
      .getRecordDefinition();
    return recordDefinition;
  }
}
