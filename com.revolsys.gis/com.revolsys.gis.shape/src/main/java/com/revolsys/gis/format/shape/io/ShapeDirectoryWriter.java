package com.revolsys.gis.format.shape.io;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.FileSystemResource;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.io.AbstractDataObjectWriterFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.IoConstants;
import com.revolsys.io.Writer;
import com.vividsolutions.jts.geom.Geometry;

public class ShapeDirectoryWriter extends AbstractWriter<DataObject> {
  private File directory;

  private boolean useZeroForNull = true;

  private final Map<DataObjectMetaData, Writer<DataObject>> writers = new HashMap<DataObjectMetaData, Writer<DataObject>>();

  public ShapeDirectoryWriter() {
  }

  public ShapeDirectoryWriter(
    final File baseDirectory) {
    setDirectory(baseDirectory);
  }

  public void close() {
    for (final Writer<DataObject> writer : writers.values()) {
      try {
        writer.close();
      } catch (final RuntimeException e) {
        e.printStackTrace();
      }
    }
  }

  public File getDirectory() {
    return directory;
  }

  public void flush() {
    // TODO Auto-generated method stub

  }

  public String toString() {
    return directory.getAbsolutePath();
  }

  private Writer<DataObject> getWriter(
    final DataObject object) {
    final DataObjectMetaData metaData = object.getMetaData();
    Writer<DataObject> writer = writers.get(metaData);
    if (writer == null) {
      File file = new File(directory, getFileName(metaData)
        + ".shp");
      writer = AbstractDataObjectWriterFactory.dataObjectWriter(metaData,
        new FileSystemResource(file));
      final Geometry geometry = object.getGeometryValue();
      if (geometry != null) {
        setProperty(IoConstants.GEOMETRY_FACTORY,
          GeometryFactory.getFactory(geometry));
      }
      writers.put(metaData, writer);
    }
    return writer;
  }

  protected String getFileName(final DataObjectMetaData metaData) {
    return metaData.getName().getLocalPart();
  }

  public boolean isUseZeroForNull() {
    return useZeroForNull;
  }

  public void setDirectory(
    final File baseDirectory) {
    this.directory = baseDirectory;
    baseDirectory.mkdirs();
  }

  public void setUseZeroForNull(
    final boolean useZeroForNull) {
    this.useZeroForNull = useZeroForNull;
  }

  public void write(
    final DataObject object) {

    final Writer<DataObject> writer = getWriter(object);
    writer.write(object);
  }

}
