package com.revolsys.format.openstreetmap.pbf;

import java.util.Collections;
import java.util.Set;

import com.revolsys.spring.resource.Resource;

import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.io.AbstractRecordIoFactory;
import com.revolsys.data.record.io.RecordReader;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;

public class OsmPbf extends AbstractRecordIoFactory {
  public OsmPbf() {
    super("Open Street Map PBF");
    addMediaTypeAndFileExtension("application/x-pbf+osm", "osm.pbf");
  }

  @Override
  public RecordReader createRecordReader(final Resource resource,
    final RecordFactory recordFactory) {
    return new OsmPbfRecordIterator(resource);
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
}
