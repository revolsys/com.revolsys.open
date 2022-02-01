package com.revolsys.rest;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.io.PathName;
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
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.AbstractTableRecordStore;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.TableRecordStoreConnection;
import com.revolsys.transaction.Transaction;
import com.revolsys.transaction.TransactionOptions;
import com.revolsys.ui.web.utils.HttpServletUtils;

public class AbstractTableRecordRestController {

  private static final String UTF_8 = StandardCharsets.UTF_8.toString();

  public static void responseJson(final HttpServletResponse response, final JsonObject jsonObject)
    throws IOException {
    setContentTypeJson(response);
    response.setStatus(200);
    try (
      PrintWriter writer = response.getWriter();
      JsonWriter jsonWriter = new JsonWriter(writer);) {
      jsonWriter.write(jsonObject);
    }
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

  protected int maxPageSize = Integer.MAX_VALUE;

  public AbstractTableRecordRestController() {
  }

  protected <RS extends AbstractTableRecordStore> RS getTableRecordStore(
    final TableRecordStoreConnection connection, final CharSequence tablePath) {
    final RS tableRecordStore = connection.getTableRecordStore(tablePath);
    if (tableRecordStore == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    return tableRecordStore;
  }

  protected void handleGetRecord(final TableRecordStoreConnection connection,
    final HttpServletRequest request, final HttpServletResponse response, final Query query)
    throws IOException {
    responseRecordJson(connection, request, response, query);
  }

  protected void handleGetRecords(final TableRecordStoreConnection connection,
    final HttpServletRequest request, final HttpServletResponse response, final Query query)
    throws IOException {
    try (
      Transaction transaction = connection.newTransaction(TransactionOptions.REQUIRES_NEW_READONLY);
      final RecordReader records = query.getRecordReader(transaction)) {
      Long count = null;
      if (HttpServletUtils.getBooleanParameter(request, "$count")) {
        count = query.getRecordCount();
      }
      responseRecords(connection, request, response, query, records, count);
    }
  }

  protected void handleInsertRecord(final TableRecordStoreConnection connection,
    final HttpServletRequest request, final HttpServletResponse response,
    final CharSequence tablePath) throws IOException {
    final JsonObject json = readJsonBody(request);
    Record record = connection.newRecord(tablePath, json);
    try (
      Transaction transaction = connection.newTransaction()) {
      try {
        record = connection.insertRecord(record);
      } catch (final Exception e) {
        // TODO return error
        transaction.setRollbackOnly(e);
      }
    }
    responseRecordJson(response, record);
  }

  protected Record handleUpdateRecordDo(final TableRecordStoreConnection connection,
    final HttpServletResponse response, final CharSequence tablePath, final Identifier id,
    final Consumer<Record> updateAction) throws IOException {
    return handleUpdateRecordDo(connection, response,
      () -> connection.updateRecord(tablePath, id, updateAction));
  }

  protected Record handleUpdateRecordDo(final TableRecordStoreConnection connection,
    final HttpServletResponse response, final CharSequence tablePath, final Identifier id,
    final JsonObject values) throws IOException {
    return handleUpdateRecordDo(connection, response,
      () -> connection.updateRecord(tablePath, id, values));
  }

  protected Record handleUpdateRecordDo(final TableRecordStoreConnection connection,
    final HttpServletResponse response, final Supplier<Record> action) throws IOException {
    final Record record;
    try (
      Transaction transaction = connection.newTransaction()) {
      try {
        record = action.get();
      } catch (final Exception e) {
        // TODO display error
        throw transaction.setRollbackOnly(e);
      }
    }
    responseRecordJson(response, record);
    return record;
  }

  protected Record insertRecord(final TableRecordStoreConnection connection,
    final PathName tablePath, final JsonObject values) {
    final Record record = connection.newRecord(tablePath, values);
    return connection.insertRecord(record);
  }

  protected boolean isUpdateable(final TableRecordStoreConnection connection, final Identifier id) {
    return true;
  }

  protected Query newQuery(final TableRecordStoreConnection connection,
    final HttpServletRequest request, final CharSequence tablePath) {
    final AbstractTableRecordStore recordStore = getTableRecordStore(connection, tablePath);
    return recordStore.newQuery(connection, request, Integer.MAX_VALUE);
  }

  public JsonObject readJsonBody(final HttpServletRequest request) throws IOException {
    final JsonObject json;
    try (
      Reader reader = request.getReader()) {
      json = JsonParser.read(reader);
    }
    return json;
  }

  protected void responseRecordJson(final TableRecordStoreConnection connection,
    final HttpServletRequest request, final HttpServletResponse response, final Query query)
    throws IOException {
    final Record record = query.getRecord();
    responseRecordJson(response, record);
  }

  protected void responseRecords(final TableRecordStoreConnection connection,
    final HttpServletRequest request, final HttpServletResponse response, final Query query,
    final RecordReader reader, final Long count) throws IOException {
    if ("csv".equals(request.getParameter("format"))) {
      responseRecordsCsv(response, reader);
    } else {
      responseRecordsJson(connection, request, response, query, reader, count);
    }
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

  public void responseRecordsJson(final TableRecordStoreConnection connection,
    final HttpServletRequest request, final HttpServletResponse response, final Query query,
    final Long count) throws IOException {
    try (
      Transaction transaction = connection.newTransaction(TransactionOptions.REQUIRES_NEW_READONLY);
      final RecordReader records = query.getRecordReader(transaction)) {
      responseRecordsJson(connection, request, response, query, records, count);
    }
  }

  protected void responseRecordsJson(final TableRecordStoreConnection connection,
    final HttpServletRequest request, final HttpServletResponse response, final Query query,
    final RecordReader reader, final Long count) throws IOException {
    reader.open();
    setContentTypeJson(response);
    response.setStatus(200);
    try (
      PrintWriter writer = response.getWriter();
      JsonRecordWriter jsonWriter = new JsonRecordWriter(reader, writer);) {
      if (count != null) {
        jsonWriter.setHeader(JsonObject.hash("@odata.count", count));
      }
      jsonWriter.setItemsPropertyName("value");
      final int writeCount = jsonWriter.writeAll(reader);
      final int nextSkip = query.getOffset() + writeCount;
      boolean writeNext = false;
      if (writeCount != 0) {
        if (count == null) {
          if (writeCount >= query.getLimit()) {
            writeNext = true;
          }
        } else if (query.getOffset() + writeCount < count) {
          writeNext = true;
        }
      }

      if (writeNext) {
        final String nextLink = HttpServletUtils.getFullRequestUriBuilder(request)
          .setParameter("$skip", nextSkip)
          .buildString();
        jsonWriter.setFooter(JsonObject.hash("@odata.nextLink", nextLink));
      }
    }
  }
}
