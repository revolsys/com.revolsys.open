package com.revolsys.data.record.io;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.core.io.Resource;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.Records;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.gis.geometry.io.GeometryWriterFactory;
import com.revolsys.io.FileIoFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactoryWithCoordinateSystem;
import com.revolsys.io.Paths;
import com.revolsys.io.Writer;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.spring.SpringUtil;

public interface RecordWriterFactory
  extends FileIoFactory, GeometryWriterFactory, IoFactoryWithCoordinateSystem {

  @Override
  default Writer<Geometry> createGeometryWriter(final String baseName, final OutputStream out,
    final Charset charset) {
    final RecordDefinition recordDefinition = Records.createGeometryRecordDefinition();
    final Writer<Record> recordWriter = createRecordWriter(baseName, recordDefinition, out,
      charset);
    return new RecordWriterGeometryWriter(recordWriter);
  }

  default Writer<Record> createRecordWriter(final RecordDefinition recordDefinition,
    final Path path) {
    final OutputStream out = Paths.outputStream(path);
    final String baseName = Paths.getBaseName(path);
    return createRecordWriter(baseName, recordDefinition, out);
  }

  /**
   * Create a writer to write to the specified resource.
   *
   * @param recordDefinition The recordDefinition for the type of data to write.
   * @param resource The resource to write to.
   * @return The writer.
   */
  default Writer<Record> createRecordWriter(final RecordDefinition recordDefinition,
    final Resource resource) {
    final OutputStream out = SpringUtil.getOutputStream(resource);
    final String fileName = resource.getFilename();
    final String baseName = FileUtil.getBaseName(fileName);
    return createRecordWriter(baseName, recordDefinition, out);
  }

  default Writer<Record> createRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream) {
    return createRecordWriter(baseName, recordDefinition, outputStream, StandardCharsets.UTF_8);
  }

  Writer<Record> createRecordWriter(String baseName, RecordDefinition recordDefinition,
    OutputStream outputStream, Charset charset);
}
