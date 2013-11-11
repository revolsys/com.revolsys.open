package com.revolsys.io.moep;

import java.io.File;
import java.io.IOException;
import java.sql.Date;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.DataObjectDirectoryReader;
import com.revolsys.gis.data.model.ArrayDataObjectFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Reader;
import com.revolsys.util.DateUtil;

public class MoepDirectoryReader extends DataObjectDirectoryReader implements
  DataObjectMetaDataFactory {

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
  public DataObjectMetaData getMetaData(final String typePath) {
    if (typePath.equals(MoepConstants.TYPE_NAME)) {
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
    final String name = FileUtil.getFileName(directory);
    final File file = new File(directory, name + "s.bin");
    final Reader<DataObject> supDataReader = createReader(new FileSystemResource(
      file));
    for (final DataObject supData : supDataReader) {
      final String featureCode = supData.getValue(MoepConstants.FEATURE_CODE);
      if (featureCode.equals("KN00020000")) {
        final String text = supData.getValue(MoepConstants.TEXT);
        final String[] versionFields = text.split(" ");

        final String dateString = versionFields[2];
        submissionDate = new Date(DateUtil.parse("yyyyMMdd", dateString)
          .getTime());
        revisionKey = versionFields[3];
        integrationDate = new Date(DateUtil.parse("yyyyMMdd", versionFields[4])
          .getTime());
        specificationsRelease = versionFields[5];
      }
    }

  }
}
