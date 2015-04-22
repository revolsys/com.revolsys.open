package com.revolsys.format.moep;

import java.io.File;
import java.io.IOException;
import java.sql.Date;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.data.io.RecordDirectoryReader;
import com.revolsys.data.record.ArrayRecordFactory;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Reader;
import com.revolsys.util.DateUtil;

public class MoepDirectoryReader extends RecordDirectoryReader implements
RecordDefinitionFactory {

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
  protected Reader<Record> createReader(final Resource resource) {
    return new MoepBinaryReader(this, resource, new ArrayRecordFactory());
  }

  public Date getIntegrationDate() {
    return this.integrationDate;
  }

  @Override
  public RecordDefinition getRecordDefinition(final String typePath) {
    if (typePath.equals(MoepConstants.TYPE_NAME)) {
      return MoepConstants.RECORD_DEFINITION;
    } else {
      return null;
    }
  }

  public String getRevisionKey() {
    return this.revisionKey;
  }

  public String getSpecificationsRelease() {
    return this.specificationsRelease;
  }

  public Date getSubmissionDate() {
    return this.submissionDate;
  }

  @Override
  public void setDirectory(final File directory) {
    super.setDirectory(directory);
    final String name = FileUtil.getFileName(directory);
    final File file = new File(directory, name + "s.bin");
    final Reader<Record> supDataReader = createReader(new FileSystemResource(
      file));
    for (final Record supData : supDataReader) {
      final String featureCode = supData.getValue(MoepConstants.FEATURE_CODE);
      if (featureCode.equals("KN00020000")) {
        final String text = supData.getValue(MoepConstants.TEXT);
        final String[] versionFields = text.split(" ");

        final String dateString = versionFields[2];
        this.submissionDate = new Date(DateUtil.getDate("yyyyMMdd", dateString)
          .getTime());
        this.revisionKey = versionFields[3];
        this.integrationDate = new Date(DateUtil.getDate("yyyyMMdd", versionFields[4])
          .getTime());
        this.specificationsRelease = versionFields[5];
      }
    }

  }
}
