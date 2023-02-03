package com.revolsys.rest;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;

import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.AbstractTableRecordStore;
import com.revolsys.record.schema.TableRecordStoreConnection;

public class BaseTableRest extends AbstractTableRecordRestController {

  public BaseTableRest() {
    super();
  }

  @GetMapping("/app/api/{tableName}({id:[0-9]+})")
  public void getRecordIntegral(
    @RequestAttribute("tableConnection") final TableRecordStoreConnection connection,
    final HttpServletRequest request, final HttpServletResponse response,
    @PathVariable final String tableName, @PathVariable() final String id) throws IOException {
    getRecordString(connection, request, response, tableName, id);
  }

  @GetMapping("/app/api/{tableName}('{id}')")
  public void getRecordString(
    @RequestAttribute("tableConnection") final TableRecordStoreConnection connection,
    final HttpServletRequest request, final HttpServletResponse response,
    @PathVariable final String tableName, @PathVariable() final String id) throws IOException {
    final Query query = getTableRecordStore(connection, tableName).newQuery(connection)//
      .andEqualId(id);
    handleGetRecord(connection, request, response, query);
  }

  @GetMapping("/app/api/{tableName:[A-Za-z0-9_\\.]+}/$schema")
  public void getSchema(
    @RequestAttribute("tableConnection") final TableRecordStoreConnection connection,
    final HttpServletRequest request, final HttpServletResponse response,
    @PathVariable final String tableName) throws IOException {
    final AbstractTableRecordStore recordStore = getTableRecordStore(connection, tableName);
    responseSchema(response, recordStore);
  }

  @GetMapping("/app/api/{tableName:[A-Za-z0-9_\\.]+}")
  public void listRecords(
    @RequestAttribute("tableConnection") final TableRecordStoreConnection connection,
    final HttpServletRequest request, final HttpServletResponse response,
    @PathVariable final String tableName) throws IOException {
    final Query query = newQuery(connection, request, tableName);
    handleGetRecords(connection, request, response, query);
  }

  public void responseSchema(final HttpServletResponse response,
    final AbstractTableRecordStore recordStore) throws IOException {
    final JsonObject jsonSchema = recordStore.schemaToJson();
    responseJson(response, jsonSchema);
  }

}
