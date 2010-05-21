package com.revolsys.jump.ui.io;

import java.util.Map;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.DataObjectReaderFactory;
import com.revolsys.gis.data.io.Reader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.vividsolutions.jump.workbench.WorkbenchContext;

public class InputStreamReaderFileLoader extends AbstractDataObjectFileLoader {

  private DataObjectReaderFactory readerFactory;
  private DataObjectFactory dataObjectFactory;

  public InputStreamReaderFileLoader(final WorkbenchContext workbenchContext,
    final DataObjectReaderFactory readerFactory,
    final DataObjectFactory dataObjectFactory,
    final String description, final String... extensions) {
    super(workbenchContext, description, extensions);
    this.readerFactory = readerFactory;
    this.dataObjectFactory = dataObjectFactory;
  }

  protected Reader<DataObject> createReader(final Resource resource,
    final Map<String, Object> options) {
    return readerFactory.createDataObjectReader(resource, dataObjectFactory);
  }
}
