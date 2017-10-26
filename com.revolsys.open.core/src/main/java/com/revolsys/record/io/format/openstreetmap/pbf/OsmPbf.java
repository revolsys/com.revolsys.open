package com.revolsys.record.io.format.openstreetmap.pbf;

import java.util.Collections;
import java.util.Set;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.io.AbstractRecordIoFactory;
import com.revolsys.record.io.RecordReader;
import com.revolsys.spring.resource.Resource;

public class OsmPbf extends AbstractRecordIoFactory {
  public OsmPbf() {
    super("Open Street Map PBF");
    addMediaTypeAndFileExtension("application/x-pbf+osm", "osm.pbf");
  }

  @Override
  public Set<CoordinateSystem> getCoordinateSystems() {
    return Collections.singleton(EpsgCoordinateSystems.wgs84());
  }

  @Override
  public boolean isBinary() {
    return true;
  }

  @Override
  public boolean isCoordinateSystemSupported(final CoordinateSystem coordinateSystem) {
    return coordinateSystem instanceof GeographicCoordinateSystem;
  }

  @Override
  public RecordReader newRecordReader(final Resource resource,
    final RecordFactory<? extends Record> recordFactory, MapEx properties) {
    return new OsmPbfRecordIterator(resource);
  }
}
