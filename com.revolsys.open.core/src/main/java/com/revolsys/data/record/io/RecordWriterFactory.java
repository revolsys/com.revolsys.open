package com.revolsys.data.record.io;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.springframework.core.io.Resource;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.Records;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.gis.geometry.io.GeometryWriter;
import com.revolsys.gis.geometry.io.GeometryWriterFactory;
import com.revolsys.io.FileIoFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactoryWithCoordinateSystem;
import com.revolsys.io.Writer;
import com.revolsys.spring.resource.PathResource;
import com.revolsys.spring.resource.SpringUtil;

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
    final OutputStream out = SpringUtil.getOutputStream(resource);
    final String fileName = SpringUtil.getFileName(resource);
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
