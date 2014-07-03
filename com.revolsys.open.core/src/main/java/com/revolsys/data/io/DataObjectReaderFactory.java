package com.revolsys.data.io;

import java.io.File;
import java.util.Set;

import org.springframework.core.io.Resource;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.io.IoFactory;
import com.revolsys.io.Reader;

public interface DataObjectReaderFactory extends IoFactory {

  DataObjectReader createDataObjectReader(Resource resource);

  DataObjectReader createDataObjectReader(Resource resource,
    RecordFactory factory);

  Reader<Record> createDirectoryDataObjectReader();

  Reader<Record> createDirectoryDataObjectReader(File file);

  Reader<Record> createDirectoryDataObjectReader(File file,
    RecordFactory factory);

  Set<CoordinateSystem> getCoordinateSystems();

  boolean isBinary();

  boolean isCoordinateSystemSupported(CoordinateSystem coordinateSystem);
}
