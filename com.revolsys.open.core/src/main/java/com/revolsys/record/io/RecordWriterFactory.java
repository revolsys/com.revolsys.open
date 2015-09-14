package com.revolsys.record.io;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import com.revolsys.geometry.io.GeometryWriter;
import com.revolsys.geometry.io.GeometryWriterFactory;
import com.revolsys.io.FileIoFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactoryWithCoordinateSystem;
import com.revolsys.io.Writer;
import com.revolsys.record.Record;
import com.revolsys.record.Records;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.PathResource;
import com.revolsys.spring.resource.Resource;

public interface RecordWriterFactory
  extends FileIoFactory, GeometryWriterFactory, IoFactoryWithCoordinateSystem {

  @Override
  default GeometryWriter createGeometryWriter(final String baseName, final OutputStream out,
    final Charset charset) {
    final RecordDefinition recordDefinition = Records.createGeometryRecordDefinition();
    final Writer<Record> recordWriter = createRecordWriter(baseName, recordDefinition, out,
      charset);
    return new RecordWriterGeometryWriter(recordWriter);
  }

  default RecordWriter createRecordWriter(final RecordDefinition recordDefinition,
    final Path path) {
    final PathResource resource = new PathResource(path);
    return createRecordWriter(recordDefinition, resource);
  }

  /**
   * Create a writer to write to the specified resource.
   *
   * @param recordDefinition The recordDefinition for the type of data to write.
   * @param resource The resource to write to.
   * @return The writer.
   */
  default RecordWriter createRecordWriter(final RecordDefinition recordDefinition,
    final Resource resource) {
    final OutputStream out = resource.newBufferedOutputStream();
    final String fileName = resource.getFilename();
    final String baseName = FileUtil.getBaseName(fileName);
    return createRecordWriter(baseName, recordDefinition, out);
  }

  default RecordWriter createRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream) {
    return createRecordWriter(baseName, recordDefinition, outputStream, StandardCharsets.UTF_8);
  }

  RecordWriter createRecordWriter(String baseName, RecordDefinition recordDefinition,
    OutputStream outputStream, Charset charset);
}
