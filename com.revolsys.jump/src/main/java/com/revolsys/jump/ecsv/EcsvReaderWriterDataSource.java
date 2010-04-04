package com.revolsys.jump.ecsv;

import com.revolsys.gis.ecsv.io.EcsvConstants;
import com.revolsys.gis.ecsv.io.EcsvDataObjectReaderFactory;
import com.revolsys.jump.io.DataObjectFeatureIsJumpReader;
import com.vividsolutions.jump.io.datasource.StandardReaderWriterFileDataSource;

public class EcsvReaderWriterDataSource extends
  StandardReaderWriterFileDataSource {

  public static final DataObjectFeatureIsJumpReader READER = new DataObjectFeatureIsJumpReader(
    EcsvDataObjectReaderFactory.INSTANCE);

  public static final EcsvJumpWriter WRITER = new EcsvJumpWriter();

  public EcsvReaderWriterDataSource() {
    super(READER, WRITER, new String[] {
      EcsvConstants.FILE_EXTENSION
    });
  }

}
