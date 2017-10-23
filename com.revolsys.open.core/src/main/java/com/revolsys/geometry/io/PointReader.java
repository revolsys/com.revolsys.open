package com.revolsys.geometry.io;

import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.io.IoFactory;
import com.revolsys.io.Reader;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionBuilder;
import com.revolsys.spring.resource.Resource;

public interface PointReader extends Reader<Point> {
  static boolean isReadable(final Object source) {
    final PointReaderFactory readerFactory = IoFactory.factory(PointReaderFactory.class, source);
    if (readerFactory == null) {
      return false;
    } else {
      return true;
    }
  }

  static PointReader newPointReader(final Object source) {
    final PointReaderFactory readerFactory = IoFactory.factory(PointReaderFactory.class, source);
    if (readerFactory == null) {
      return null;
    } else {
      final Resource resource = readerFactory.getZipResource(source);
      return readerFactory.newPointReader(resource);
    }
  }

  GeometryFactory getGeometryFactory();

  default RecordDefinition newRecordDefinition(final String name) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final RecordDefinition recordDefinition = new RecordDefinitionBuilder(name) //
      .addField("POINT", DataTypes.POINT) //
      .setGeometryFactory(geometryFactory) //
      .getRecordDefinition();
    return recordDefinition;
  }
}
