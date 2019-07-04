package com.revolsys.record.io.format.esri.gdb.xml;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.jeometry.coordinatesystem.model.systems.EpsgCoordinateSystems;

import com.revolsys.io.AbstractIoFactoryWithCoordinateSystem;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.RecordWriterFactory;
import com.revolsys.record.schema.RecordDefinition;

public class EsriGeodatabaseXmlRecordWriterFactory extends AbstractIoFactoryWithCoordinateSystem
  implements RecordWriterFactory {
  public EsriGeodatabaseXmlRecordWriterFactory() {
    super(EsriGeodatabaseXmlConstants.FORMAT_DESCRIPTION);
    addMediaTypeAndFileExtension(EsriGeodatabaseXmlConstants.MEDIA_TYPE,
      EsriGeodatabaseXmlConstants.FILE_EXTENSION);
    setCoordinateSystems(EpsgCoordinateSystems.getCoordinateSystem(4326));
  }

  @Override
  public RecordWriter newRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = new OutputStreamWriter(outputStream, charset);
    return new EsriGeodatabaseXmlRecordWriter(recordDefinition, writer);
  }
}
