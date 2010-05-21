package com.revolsys.jump.gpx.io;

import java.util.Map;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.Reader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.gpx.io.GpxReaderFactory;
import com.revolsys.jump.model.FeatureDataObjectFactory;
import com.revolsys.jump.ui.io.AbstractDataObjectFileLoader;
import com.vividsolutions.jump.workbench.WorkbenchContext;

public class GpxFileLoader extends AbstractDataObjectFileLoader {

  public GpxFileLoader(
    final WorkbenchContext workbenchContext) {
    super(workbenchContext, "GPS Exchange Format", "gpx");
  }

  @Override
  protected Reader<DataObject> createReader(
    final Resource resource,
    final Map<String, Object> options) {
    FeatureDataObjectFactory factory = new FeatureDataObjectFactory();
    Reader<DataObject> reader = GpxReaderFactory.get().createDataObjectReader(resource, factory);
    return reader;
  }

}
