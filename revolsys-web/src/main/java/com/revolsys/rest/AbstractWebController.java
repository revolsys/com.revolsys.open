package com.revolsys.rest;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.revolsys.io.IoConstants;
import com.revolsys.io.IoFactory;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.RecordWriterFactory;
import com.revolsys.record.io.format.csv.Csv;
import com.revolsys.record.io.format.csv.CsvRecordWriter;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.record.io.format.json.JsonList;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.json.JsonParser;
import com.revolsys.record.io.format.json.JsonRecordWriter;
import com.revolsys.record.io.format.json.JsonWriter;
import com.revolsys.record.schema.RecordDefinition;

public class AbstractWebController {

  private static final String UTF_8 = StandardCharsets.UTF_8.toString();

  public static void responseJson(final HttpServletResponse response, final int statusCode,
    final JsonObject jsonObject) throws IOException {
    setContentTypeJson(response);
    response.setStatus(statusCode);
    try (
      PrintWriter writer = response.getWriter();
      JsonWriter jsonWriter = new JsonWriter(writer);) {
      jsonWriter.write(jsonObject);
    }
  }

  public static void responseJson(final HttpServletResponse response, final JsonObject jsonObject)
    throws IOException {
    responseJson(response, 200, jsonObject);
  }

  public static void responseRecordJson(final HttpServletResponse response, final Record record)
    throws IOException {
    if (record == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    } else {
      setContentTypeJson(response);
      response.setStatus(200);
      final RecordDefinition recordDefinition = record.getRecordDefinition();
      try (
        PrintWriter writer = response.getWriter();
        JsonRecordWriter jsonWriter = new JsonRecordWriter(recordDefinition, writer);) {
        jsonWriter.setProperty(IoConstants.SINGLE_OBJECT_PROPERTY, true);
        jsonWriter.write(record);
      }
    }
  }

  public static void setContentTypeJson(final HttpServletResponse response) {
    setContentTypeText(response, Json.MIME_TYPE_UTF8);
  }

  public static void setContentTypeText(final HttpServletResponse response,
    final String contentType) {
    response.setCharacterEncoding(UTF_8);
    response.setContentType(contentType);
  }

  public JsonObject readJsonBody(final HttpServletRequest request) throws IOException {
    final JsonObject json;
    try (
      Reader reader = request.getReader()) {
      json = JsonParser.read(reader);
    }
    return json;
  }

  protected void responseRecordsCsv(final HttpServletResponse response, final RecordReader reader)
    throws IOException {
    response.setHeader("Content-Disposition", "attachment; filename=Export.csv");
    setContentTypeText(response, Csv.MIME_TYPE);
    response.setStatus(200);
    final Csv csv = (Csv)IoFactory.factoryByFileExtension(RecordWriterFactory.class, "csv");
    try (
      PrintWriter writer = response.getWriter();
      CsvRecordWriter recordWriter = csv.newRecordWriter(reader, writer)) {
      recordWriter.setMaxFieldLength(32000);
      recordWriter.writeAll(reader);
    }
  }

  protected void responseRecordsJson(final HttpServletResponse response, final JsonList records)
    throws IOException {
    setContentTypeJson(response);
    response.setStatus(200);
    try (
      PrintWriter writer = response.getWriter()) {
      final JsonObject result = JsonObject.hash("@odata.count", records.size())
        .addValue("value", records);
      writer.write(result.toJsonString(true));
    }
  }

}
