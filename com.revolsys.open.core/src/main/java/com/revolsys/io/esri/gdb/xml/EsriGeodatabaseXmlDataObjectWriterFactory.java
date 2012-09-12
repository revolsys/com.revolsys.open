package com.revolsys.io.esri.gdb.xml;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.data.io.AbstractDataObjectAndGeometryWriterFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.Writer;

public class EsriGeodatabaseXmlDataObjectWriterFactory extends
  AbstractDataObjectAndGeometryWriterFactory {
  public EsriGeodatabaseXmlDataObjectWriterFactory() {
    super(EsriGeodatabaseXmlConstants.FORMAT_DESCRIPTION, true, true);
    addMediaTypeAndFileExtension(EsriGeodatabaseXmlConstants.MEDIA_TYPE,
      EsriGeodatabaseXmlConstants.FILE_EXTENSION);
    setCoordinateSystems(EpsgCoordinateSystems.getCoordinateSystem(4326));
  }

  @Override
  public Writer<DataObject> createDataObjectWriter(final String baseName,
    final DataObjectMetaData metaData, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = new OutputStreamWriter(outputStream,
      charset);
    return new EsriGeodatabaseXmlDataObjectWriter(metaData, writer);
  }
}
