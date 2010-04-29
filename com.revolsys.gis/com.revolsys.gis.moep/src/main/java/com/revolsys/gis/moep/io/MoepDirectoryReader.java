package com.revolsys.gis.moep.io;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.sql.Date;
import java.util.List;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.io.AbstractFileDatasetDataObjectReader;
import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.gis.data.model.ArrayDataObjectFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataFactory;
import com.revolsys.io.filter.ExtensionFilenameFilter;

public class MoepDirectoryReader extends AbstractFileDatasetDataObjectReader
  implements DataObjectMetaDataFactory {
  private static final FilenameFilter BIN_FILTER = new ExtensionFilenameFilter(
    "bin");

  private Date integrationDate;

  private String revisionKey;

  private String specificationsRelease;

  private Date submissionDate;

  public MoepDirectoryReader() {
  }

  public MoepDirectoryReader(
    final File file)
    throws IOException {
    super(file);
  }

  /**
   * Create a new {@link MoepBinaryReader} to read the file.
   * 
   * @param file The file to read.
   * @return The reader for the file.
   * @throws IOException If an I/O error occurs.
   */
  @Override
  protected DataObjectReader createFileDataObjectReader(
    final File file)
    throws IOException {
    return new MoepBinaryReader(this, file, new ArrayDataObjectFactory());
  }

  /**
   * Get the list of .dbf files in the dataset.
   * 
   * @return The list of files.
   */
  @Override
  protected List<File> getFiles() {
    return Arrays.asList(getWorkingDirectory().listFiles(BIN_FILTER));
  }

  public Date getIntegrationDate() {
    return integrationDate;
  }

  public DataObjectMetaData getMetaData(
    final QName typeName) {
    if (typeName.equals(MoepConstants.TYPE_NAME)) {
      return MoepConstants.META_DATA;
    } else {
      return null;
    }
  }

  public String getRevisionKey() {
    return revisionKey;
  }

  public String getSpecificationsRelease() {
    return specificationsRelease;
  }

  public Date getSubmissionDate() {
    return submissionDate;
  }

  @Override
  protected void init()
    throws IOException {
    final File workingDirectory = getWorkingDirectory();
    final String name = workingDirectory.getName();
    final DataObjectReader supDataReader = createFileDataObjectReader(new File(
      workingDirectory, name + "s.bin"));
    for (final DataObject supData : supDataReader) {
      final String featureCode = supData.getValue(MoepConstants.FEATURE_CODE);
      if (featureCode.equals("KN00020000")) {
        final String text = supData.getValue(MoepConstants.TEXT);
        final String[] versionFields = text.split(" ");

        try {
          final String dateString = versionFields[2];
          submissionDate = new Date(MoepConstants.FULL_DATE_FORMAT.parse(
            dateString).getTime());
        } catch (final ParseException e) {
          throw new IllegalArgumentException("Invalid KN submission date", e);
        }
        revisionKey = versionFields[3];
        try {
          integrationDate = new Date(MoepConstants.FULL_DATE_FORMAT.parse(
            versionFields[4]).getTime());
        } catch (final ParseException e) {
          throw new IllegalArgumentException("Invalid KN integration date", e);
        }
        specificationsRelease = versionFields[5];
      }
    }

  }
}
