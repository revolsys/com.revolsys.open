package com.revolsys.gis.google.fusiontables;

import java.io.IOException;
import java.sql.SQLException;

import javax.xml.namespace.QName;

import com.google.api.client.http.HttpResponse;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.google.fusiontables.attribute.FusionTablesAttribute;
import com.revolsys.gis.io.Statistics;
import com.revolsys.io.AbstractWriter;

public class FusionTablesDataObjectWriter extends AbstractWriter<DataObject> {
 
  private final FusionTablesDataObjectStore dataStore;

  private Statistics deleteStatistics;

  private Statistics insertStatistics;

  private Statistics updateStatistics;

  private String label;

  public FusionTablesDataObjectWriter(
    final FusionTablesDataObjectStore dataStore) {
    this.dataStore = dataStore;
  }

  @Override
  public void close() {
    if (insertStatistics != null) {
      insertStatistics.disconnect();
      insertStatistics = null;
    }
    if (updateStatistics != null) {
      updateStatistics.disconnect();
      updateStatistics = null;
    }
    if (deleteStatistics != null) {
      deleteStatistics.disconnect();
      deleteStatistics = null;
    }
  }

  private void delete(final DataObject object) throws SQLException {
    final DataObjectMetaData objectType = object.getMetaData();
    final QName typeName = objectType.getName();
    final StringBuffer sqlBuffer = new StringBuffer();

    sqlBuffer.append("DELETE  FROM ");
    sqlBuffer.append(dataStore.getTableId(typeName));
    sqlBuffer.append(" WHERE rowid = ");
    final Object rowId = object.getValue("rowid");
    FusionTablesDataObjectStore.appendString(sqlBuffer, rowId.toString());
    final String sql = sqlBuffer.toString();

    execute(typeName, sql, getDeleteStatistics());

  }

  private void execute(final QName typeName, final String sql,
    final Statistics statistics) {
    statistics.add(typeName.toString(), 1);
    HttpResponse response = dataStore.executePostQuery(sql);
    try {
      response.ignore();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void flush() {

  }

  private DataObjectMetaData getDataObjectMetaData(final QName typeName) {
    QName localTypeName = dataStore.getTypeName(typeName);
    final DataObjectMetaData metaData = dataStore.getMetaData(localTypeName);
    return metaData;
  }

  public Statistics getDeleteStatistics() {
    if (deleteStatistics == null) {
      if (label == null) {
        deleteStatistics = new Statistics("Delete");
      } else {
        deleteStatistics = new Statistics(label + " Delete");
      }
      deleteStatistics.connect();
    }
    return deleteStatistics;
  }

  public Statistics getInsertStatistics() {
    if (insertStatistics == null) {
      if (label == null) {
        insertStatistics = new Statistics("Insert");
      } else {
        insertStatistics = new Statistics(label + " Insert");
      }
      insertStatistics.connect();
    }
    return insertStatistics;
  }

  public String getLabel() {
    return label;
  }

  public Statistics getUpdateStatistics() {
    if (updateStatistics == null) {
      if (label == null) {
        updateStatistics = new Statistics("Update");
      } else {
        updateStatistics = new Statistics(label + " Update");
      }
      updateStatistics.connect();
    }
    return updateStatistics;
  }

  private void insert(final DataObject object) throws SQLException {
    final DataObjectMetaData objectType = object.getMetaData();
    final QName typeName = objectType.getName();
    final DataObjectMetaData metaData = getDataObjectMetaData(typeName);

    final StringBuffer sql = new StringBuffer();
    sql.append("INSERT ");
    sql.append(" INTO ");
    final String tableId = dataStore.getTableId(typeName);
    sql.append(tableId);
    sql.append(" (");

    for (int i = 1; i < metaData.getAttributeCount(); i++) {
      if (i > 1) {
        sql.append(", ");
      }
      final String attributeName = metaData.getAttributeName(i);
      FusionTablesDataObjectStore.appendString(sql, attributeName);
    }
    sql.append(") VALUES (");
    for (int i = 1; i < metaData.getAttributeCount(); i++) {
      if (i > 1) {
        sql.append(", ");
      }
      final FusionTablesAttribute attribute = (FusionTablesAttribute)metaData.getAttribute(i);
      final String attributeName = metaData.getAttributeName(i);
      final Object value = object.getValue(attributeName);
      attribute.appendValue(sql, value);
    }
    sql.append(")");
    execute(typeName, sql.toString(), getInsertStatistics());
  }

  private void update(final DataObject object) throws SQLException {
    final DataObjectMetaData objectType = object.getMetaData();
    final QName typeName = objectType.getName();
    final DataObjectMetaData metaData = getDataObjectMetaData(typeName);
    final StringBuffer sql = new StringBuffer();
    sql.append("UPDATE ");
    final String tableId = dataStore.getTableId(typeName);
    sql.append(tableId);
    sql.append(" SET ");
    for (int i = 1; i < metaData.getAttributeCount(); i++) {
      if (i > 1) {
        sql.append(", ");
      }
      final FusionTablesAttribute attribute = (FusionTablesAttribute)metaData.getAttribute(i);
      final String attributeName = attribute.getName();
      Object value = object.getValue(attributeName);
      FusionTablesDataObjectStore.appendString(sql, attributeName);
      sql.append(" = ");
      attribute.appendValue(sql, value);
    }
    sql.append(" WHERE rowid = ");
    final FusionTablesAttribute rowidAttribute = (FusionTablesAttribute)metaData.getAttribute("rowid");
    Object rowId = object.getValue("rowid");
    rowidAttribute.appendValue(sql, rowId);

    execute(typeName, sql.toString(), getUpdateStatistics());
  }

  public synchronized void write(final DataObject object) {
    try {
      switch (object.getState()) {
        case New:
          insert(object);
        break;
        case Modified:
          update(object);
        break;
        case Persisted:
          // No action required
        break;
        case Deleted:
          delete(object);
        break;
        default:
          throw new IllegalStateException("State not known");
      }
    } catch (final RuntimeException e) {
      throw e;
    } catch (final Error e) {
      throw e;
    } catch (final Exception e) {
      throw new RuntimeException("Unable to write", e);
    }
  }
}
