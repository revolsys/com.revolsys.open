package com.revolsys.rest;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.function.Consumer;
import java.util.function.Supplier;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.io.PathName;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.json.JsonRecordWriter;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.AbstractTableRecordStore;
import com.revolsys.record.schema.TableRecordStoreConnection;
import com.revolsys.transaction.Transaction;
import com.revolsys.transaction.TransactionOptions;
import com.revolsys.web.HttpServletUtils;

public class AbstractTableRecordRestController extends AbstractWebController {

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
