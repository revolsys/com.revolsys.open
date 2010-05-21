package com.revolsys.gis.data.io;

import java.util.Iterator;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.model.ArrayDataObjectFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.geometry.io.GeometryReaderFactory;
import com.vividsolutions.jts.geom.Geometry;

public abstract class AbstractDataObjectAndGeometryReaderFactory extends
  AbstractDataObjectReaderFactory implements GeometryReaderFactory {
  /** The default data object dataObjectFactory instance. */
  private static final DataObjectFactory DEFAULT_DATA_OBJECT_FACTORY = new ArrayDataObjectFactory();

  public AbstractDataObjectAndGeometryReaderFactory(
    final String name) {
    super(name);
  }

  public Reader<Geometry> createGeometryReader(
    Resource resource) {
    final Reader<DataObject> dataObjectReader = createDataObjectReader(resource);
    final Iterator<DataObject> dataObjectIterator = dataObjectReader.iterator();
    final DataObjectGeometryIterator iterator = new DataObjectGeometryIterator(
      dataObjectIterator);
    final IteratorReader<Geometry> geometryReader = new IteratorReader<Geometry>(
      iterator);
    return geometryReader;
  }
}
