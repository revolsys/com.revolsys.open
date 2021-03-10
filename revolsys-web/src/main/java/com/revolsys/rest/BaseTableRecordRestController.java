package com.revolsys.rest;

import java.io.IOException;
import java.util.function.Consumer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.io.PathName;

import com.revolsys.record.Record;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.query.Q;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.TableRecordStoreConnection;
import com.revolsys.transaction.Transaction;

public class BaseTableRecordRestController extends AbstractTableRecordRestController {

  protected final PathName tablePath;

  protected final String typeName;

  public BaseTableRecordRestController(final PathName tablePath) {
    this.tablePath = tablePath;
    this.typeName = tablePath.getName();
  }

  protected void handleGetRecord(final TableRecordStoreConnection connection,
    final HttpServletRequest request, final HttpServletResponse response, final String fieldName,
    final Object value) throws IOException {
    final Query query = newQuery(connection, request)//
      .and(fieldName, Q.EQUAL, value);
    handleGetRecord(connection, request, response, query);
  }

  protected void handleInsertRecord(final TableRecordStoreConnection connection,
    final HttpServletRequest request, final HttpServletResponse response) throws IOException {
    handleInsertRecord(connection, request, response, this.tablePath);
  }

  protected void handleUpdateRecordDo(final TableRecordStoreConnection connection,
    final HttpServletResponse response, final Identifier id, final Consumer<Record> updateAction)
    throws IOException {
    handleUpdateRecordDo(connection, response, this.tablePath, id, updateAction);
  }

  protected void handleUpdateRecordDo(final TableRecordStoreConnection connection,
    final HttpServletResponse response, final Identifier id, final JsonObject values)
    throws IOException {
    final Record record;
    try (
      Transaction transaction = connection.newTransaction()) {
      record = connection.updateRecord(this.tablePath, id, values);
    }
    responseRecordJson(response, record);
  }

  protected Query newQuery(final TableRecordStoreConnection connection,
    final HttpServletRequest request) {
    return newQuery(connection, request, this.maxPageSize);
  }

  protected Query newQuery(final TableRecordStoreConnection connection,
    final HttpServletRequest request, final int maxRecords) {
    return super.newQuery(connection, request, this.tablePath, maxRecords);
  }

}
