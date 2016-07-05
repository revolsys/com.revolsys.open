package com.revolsys.record.io;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.revolsys.geometry.io.GeometryWriter;
import com.revolsys.geometry.io.GeometryWriterFactory;
import com.revolsys.io.FileIoFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactoryWithCoordinateSystem;
import com.revolsys.record.Records;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;

public interface RecordWriterFactory
  extends FileIoFactory, GeometryWriterFactory, IoFactoryWithCoordinateSystem {

  @Override
  default GeometryWriter newGeometryWriter(final String baseName, final OutputStream out,
    final Charset charset) {
    final RecordDefinition recordDefinition = Records.newGeometryRecordDefinition();
    final RecordWriter recordWriter = newRecordWriter(baseName, recordDefinition, out, charset);
    return new RecordWriterGeometryWriter(recordWriter);
  }

  /**
   * Construct a new writer to write to the specified resource.
   *
   * @param recordDefinition The recordDefinition for the type of data to write.
   * @param resource The resource to write to.
   * @return The writer.
   */
  default RecordWriter newRecordWriter(final RecordDefinition recordDefinition,
    final Resource resource) {
    final OutputStream out = resource.newBufferedOutputStream();
    final String fileName = resource.getFilename();
    final String baseName = FileUtil.getBaseName(fileName);
    return newRecordWriter(baseName, recordDefinition, out);
  }

  default RecordWriter newRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream) {
    return newRecordWriter(baseName, recordDefinition, outputStream, StandardCharsets.UTF_8);
  }

  RecordWriter newRecordWriter(String baseName, RecordDefinition recordDefinition,
    OutputStream outputStream, Charset charset);
}
