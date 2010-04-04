package com.revolsys.jump.ui.io;

import java.io.InputStream;
import java.util.Map;

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

  protected Reader<DataObject> createReader(final InputStream in,
    final Map<String, Object> options) {
    return readerFactory.createDataObjectReader(in, dataObjectFactory);
  }
}
