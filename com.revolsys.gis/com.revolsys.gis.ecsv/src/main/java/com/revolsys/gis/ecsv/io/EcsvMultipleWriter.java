package com.revolsys.gis.ecsv.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.io.AbstractMultipleWriter;
import com.revolsys.io.Writer;

public class EcsvMultipleWriter extends AbstractMultipleWriter {

  private File directory;

  public EcsvMultipleWriter() {
  }

  public EcsvMultipleWriter(
    final File baseDirectory) {
    setDirectory(baseDirectory);
  }

  @Override
  protected Writer<DataObject> createWriter(
    final DataObjectMetaData metaData) {
    try {
      EcsvWriter writer;
      final File file = new File(directory, metaData.getName().getLocalPart()
        + "." + EcsvConstants.FILE_EXTENSION);
      final FileOutputStream out = new FileOutputStream(file);
      writer = new EcsvWriter(metaData, out);
      return writer;
    } catch (final IOException e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
  }

  public File getDirectory() {
    return directory;
  }

  public String toString() {
    return directory.getAbsolutePath();
  }

  public void setDirectory(
    final File baseDirectory) {
    this.directory = baseDirectory;
    baseDirectory.mkdirs();
  }
}
