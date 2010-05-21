package com.revolsys.gis.data.io;

import java.io.File;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.io.IoFactory;

public interface DataObjectReaderFactory extends IoFactory {
  Reader<DataObject> createDataObjectReader(
    Resource resource);

  Reader<DataObject> createDataObjectReader(
    Resource resource,
    DataObjectFactory factory);

  Reader<DataObject> createDirectoryDataObjectReader();

  Reader<DataObject> createDirectoryDataObjectReader(
    File file);

  Reader<DataObject> createDirectoryDataObjectReader(
    File file,
    DataObjectFactory factory);

}
