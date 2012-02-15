package com.revolsys.io.moep;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Date;
import java.text.ParseException;

import javax.xml.namespace.QName;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.DataObjectDirectoryReader;
import com.revolsys.gis.data.model.ArrayDataObjectFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataFactory;
import com.revolsys.io.Reader;
import com.revolsys.io.filter.ExtensionFilenameFilter;

public class MoepDirectoryReader extends DataObjectDirectoryReader implements
  DataObjectMetaDataFactory {
  private static final FilenameFilter BIN_FILTER = new ExtensionFilenameFilter(
    "bin");

  private Date integrationDate;

  private String revisionKey;

  private String specificationsRelease;

  private Date submissionDate;

  public MoepDirectoryReader() {
    setFileExtensions("bin");
  }

  public MoepDirectoryReader(final File directory) throws IOException {
    setFileExtensions("bin");
    setDirectory(directory);
  }

  /**
   * Create a new {@link MoepBinaryReader} to read the file.
   * 
   * @param file The file to read.
   * @return The reader for the file.
   * @throws IOException If an I/O error occurs.
   */
  @Override
  protected Reader<DataObject> createReader(final Resource resource) {
    return new MoepBinaryReader(this, resource, new ArrayDataObjectFactory());
  }

  public Date getIntegrationDate() {
    return integrationDate;
  }

  @Override
  public DataObjectMetaData getMetaData(final QName typeName) {
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
  public void setDirectory(final File directory) {
    super.setDirectory(directory);
    final String name = directory.getName();
    final File file = new File(directory, name + "s.bin");
    final Reader<DataObject> supDataReader = createReader(new FileSystemResource(
      file));
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
