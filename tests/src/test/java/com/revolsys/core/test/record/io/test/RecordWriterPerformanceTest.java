package com.revolsys.core.test.record.io.test;

import java.nio.file.Path;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.io.IoFactory;
import com.revolsys.io.file.Paths;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.RecordWriterFactory;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionBuilder;
import com.revolsys.spring.resource.PathResource;
import com.revolsys.spring.resource.Resource;

/*
Comma-Separated Values  0:5.749 150000105
GeoJSON 0:10.890  332000043
Geography Markup Language 0:10.104  474000136
GPS Exchange Format 0:3.90  67000147
XHMTL 0:7.864 257000499
JSON  0:7.464 274000014
KMZ - Google Earth  0:14.74 4194226
KML - Google Earth  0:10.580  865000128
ESRI Shapefile  0:9.408 36000100
ESRI Shapefile inside a ZIP archive 0:11.662  7191631
Tab-Separated Values  0:3.238 151000105
Well-Known Text Geometry  0:0.196 11000000
D-Base  0:11.887  606000418
Excel Workbook  0:34.742  10511807
XML 0:9.136 373000065
*/
public class RecordWriterPerformanceTest {

  public static void main(final String[] args) {
    final Path basePath = Paths.getPath("target/test/performance/recordWriter");
    Paths.createDirectories(basePath);
    final RecordDefinition recordDefinition = newRecordDefinition(GeometryDataTypes.POINT);

    // writeRecords(basePath, recordDefinition, new Json());

    for (final RecordWriterFactory writerFactory : IoFactory.factories(RecordWriterFactory.class)) {
      writeRecords(basePath, recordDefinition, writerFactory);
    }
    // writeRecords(basePath, recordDefinition, new Kml());
  }

  private static Record newRecord(final RecordDefinition recordDefinition, final int index) {
    final Record record = new ArrayRecord(recordDefinition);

    record.setValue("boolean", index % 2 == 0);

    record.setValue("byte", index % 256 + Byte.MIN_VALUE);

    record.setValue("short", index % 65536 + Short.MIN_VALUE);

    record.setValue("int", index);

    record.setValue("long", index);

    record.setValue("float", index + index % 1000 / 1000.0);

    record.setValue("double", index + index % 1000 / 1000.0);

    record.setValue("string", "String with some special characters " + index + "\\/\"'\t\n\r");

    final Calendar calendar = new GregorianCalendar();
    calendar.set(2016, 11, index % 28 + 1, 0, 0);
    final Date date = new Date(calendar.getTimeInMillis());
    record.setValue("date", date);

    calendar.set(Calendar.MINUTE, index % 60);
    final java.util.Date dateTime = new java.util.Date(calendar.getTimeInMillis());
    record.setValue("dateTime", dateTime);

    calendar.set(Calendar.MILLISECOND, index % 1000);
    final Instant timestamp = Instant.now();
    record.setValue("timestamp", timestamp);

    record.setValue("geometry", new PointDoubleXY(index, index * 2));
    return record;
  }

  private static RecordDefinition newRecordDefinition(final DataType geometryType) {
    return new RecordDefinitionBuilder("PerformanceTest") //
      .addField("boolean", DataTypes.BOOLEAN) //
      .addField("byte", DataTypes.BYTE) //
      .addField("short", DataTypes.SHORT) //
      .addField("int", DataTypes.INT) //
      .addField("long", DataTypes.LONG) //
      .addField("float", DataTypes.FLOAT) //
      .addField("double", DataTypes.DOUBLE) //
      .addField("string", DataTypes.STRING) //
      .addField("date", DataTypes.SQL_DATE) //
      .addField("dateTime", DataTypes.UTIL_DATE) //
      .addField("timestamp", DataTypes.TIMESTAMP) //
      .addField("geometry", geometryType) //
      .getRecordDefinition();
  }

  private static void writeRecords(final Path basePath, final RecordDefinition recordDefinition,
    final RecordWriterFactory writerFactory) {
    final String fileExtension = writerFactory.getFileExtensions().get(0);
    final Resource resource = new PathResource(basePath.resolve("records." + fileExtension));
    final Record record = newRecord(recordDefinition, 0);
    // Prime the code to avoid initialization in timings
    try {
      writeRecords(writerFactory, recordDefinition, resource, 1, record);
    } finally {
      resource.delete();
    }

    try {
      final long time = System.currentTimeMillis();
      final int numIterations = 1000000;
      writeRecords(writerFactory, recordDefinition, resource, numIterations, record);
      final long ellapsedTime = System.currentTimeMillis() - time;
      final long millis = ellapsedTime % 1000;
      final long seconds = ellapsedTime / 1000 % 60;
      final long minutes = ellapsedTime / 60000 % 60;
      System.out.println(writerFactory.getName() + "\t" + minutes + ":" + seconds + "." + millis
        + "\t" + resource.getFile().length());
    } finally {
      resource.delete();
    }
    for (int i = 0; i < 10; i++) {
      System.gc();
      synchronized (record) {
        try {
          record.wait(1000);
        } catch (final InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
  }

  private static void writeRecords(final RecordWriterFactory writerFactory,
    final RecordDefinition recordDefinition, final Resource resource, final int numIterations,
    final Record record) {
    try (
      RecordWriter writer = writerFactory.newRecordWriter(recordDefinition, resource)) {
      writer.open();
      for (int i = 0; i < numIterations; i++) {
        writer.write(record);
      }
    }
  }
}
