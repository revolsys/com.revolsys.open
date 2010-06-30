package com.revolsys.jump.gpx.io;

import com.revolsys.gis.gpx.io.GpxConstants;
import com.revolsys.gis.gpx.io.GpxReaderFactory;
import com.revolsys.jump.io.DataObjectFeatureIsJumpReader;
import com.vividsolutions.jump.io.datasource.StandardReaderWriterFileDataSource;

public class GpxReaderWriterDataSource extends
  StandardReaderWriterFileDataSource {

  public static final DataObjectFeatureIsJumpReader READER = new DataObjectFeatureIsJumpReader(
    GpxReaderFactory.INSTANCE);

  public static final GpxJumpWriter WRITER = new GpxJumpWriter();

  public GpxReaderWriterDataSource() {
    super(READER, WRITER, new String[] {
      GpxConstants.FILE_EXTENSION
    });
  }

}
