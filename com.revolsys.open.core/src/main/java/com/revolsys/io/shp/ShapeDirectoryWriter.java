package com.revolsys.io.shp;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.xml.namespace.QName;

import org.springframework.core.io.FileSystemResource;
import org.springframework.util.StringUtils;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.io.AbstractDataObjectWriterFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.io.Statistics;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.IoConstants;
import com.revolsys.io.Writer;
import com.vividsolutions.jts.geom.Geometry;

public class ShapeDirectoryWriter extends AbstractWriter<DataObject> {
  private File directory;

  private boolean useZeroForNull = true;

  private Map<DataObjectMetaData, Writer<DataObject>> writers = new HashMap<DataObjectMetaData, Writer<DataObject>>();

  private boolean useNamespaceAsSubDirectory;

  private Statistics statistics;

  public ShapeDirectoryWriter() {
  }

  public ShapeDirectoryWriter(final File baseDirectory) {
    setDirectory(baseDirectory);
  }

  @Override
  @PreDestroy
  public void close() {
    if (writers != null) {
      for (final Writer<DataObject> writer : writers.values()) {
        try {
          writer.close();
        } catch (final RuntimeException e) {
          e.printStackTrace();
        }
      }
      writers = null;
    }
    if (statistics != null) {
      statistics.disconnect();
      statistics = null;
    }
  }

  @Override
  public void flush() {
    for (final Writer<DataObject> writer : writers.values()) {
      try {
        writer.flush();
      } catch (final RuntimeException e) {
        e.printStackTrace();
      }
    }
  }

  public File getDirectory() {
    return directory;
  }

  private File getDirectory(final DataObjectMetaData metaData) {
    if (useNamespaceAsSubDirectory) {
      final QName typeName = metaData.getName();
      final String schemaName = typeName.getNamespaceURI();
      if (StringUtils.hasText(schemaName)) {
        final File childDirectory = new File(directory, schemaName);
        if (!childDirectory.mkdirs()) {
          if (!childDirectory.isDirectory()) {
            throw new IllegalArgumentException("Unable to create directory "
              + childDirectory);
          }
        }
        return childDirectory;
      }
    }
    return directory;
  }

  private String getFileName(final DataObjectMetaData metaData) {
    return metaData.getName().getLocalPart();
  }

  private Writer<DataObject> getWriter(final DataObject object) {
    final DataObjectMetaData metaData = object.getMetaData();
    Writer<DataObject> writer = writers.get(metaData);
    if (writer == null) {
      final File directory = getDirectory(metaData);
      final File file = new File(directory, getFileName(metaData) + ".shp");
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

  public boolean isUseNamespaceAsSubDirectory() {
    return useNamespaceAsSubDirectory;
  }

  public boolean isUseZeroForNull() {
    return useZeroForNull;
  }

  public void setDirectory(final File baseDirectory) {
    this.directory = baseDirectory;
    baseDirectory.mkdirs();
    statistics = new Statistics("Write Shape "
      + baseDirectory.getAbsolutePath());
    statistics.connect();
  }

  public void setUseNamespaceAsSubDirectory(
    final boolean useNamespaceAsSubDirectory) {
    this.useNamespaceAsSubDirectory = useNamespaceAsSubDirectory;
  }

  public void setUseZeroForNull(final boolean useZeroForNull) {
    this.useZeroForNull = useZeroForNull;
  }

  @Override
  public String toString() {
    return directory.getAbsolutePath();
  }

  public void write(final DataObject object) {
    final Writer<DataObject> writer = getWriter(object);
    writer.write(object);
    statistics.add(object);
  }

}
