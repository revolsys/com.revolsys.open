package com.revolsys.gis.esri.gdb.file.capi;

import java.util.NoSuchElementException;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.esri.gdb.file.capi.swig.EnumRows;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.gis.esri.gdb.file.capi.swig.Table;
import com.revolsys.gis.esri.gdb.file.capi.type.AbstractFileGdbAttribute;
import com.revolsys.gis.esri.gdb.file.convert.GeometryConverter;

public class FileGdbQueryIterator extends AbstractIterator<DataObject> {

  private final DataObjectFactory dataObjectFactory;

  private Table table;

  private String fields;

  private String whereClause;

  private BoundingBox boundingBox;

  private DataObjectMetaData metaData;

  private CapiFileGdbDataObjectStore dataStore;

  private EnumRows rows;

  private final String typePath;

  FileGdbQueryIterator(final CapiFileGdbDataObjectStore dataStore,
    final String typePath) {
    this(dataStore, typePath, "*", "", null);
  }

  FileGdbQueryIterator(final CapiFileGdbDataObjectStore dataStore,
    final String typePath, final BoundingBox boundingBox) {
    this(dataStore, typePath, "*", "", boundingBox);
  }

  FileGdbQueryIterator(final CapiFileGdbDataObjectStore dataStore,
    final String typePath, final String whereClause) {
    this(dataStore, typePath, "*", whereClause, null);
  }

  FileGdbQueryIterator(final CapiFileGdbDataObjectStore dataStore,
    final String typePath, final String whereClause,
    final BoundingBox boundingBox) {
    this(dataStore, typePath, "*", whereClause, boundingBox);
  }

  FileGdbQueryIterator(final CapiFileGdbDataObjectStore dataStore,
    final String typePath, final String fields, final String whereClause) {
    this(dataStore, typePath, fields, whereClause, null);
  }

  FileGdbQueryIterator(final CapiFileGdbDataObjectStore dataStore,
    final String typePath, final String fields, final String whereClause,
    final BoundingBox boundingBox) {
    this.dataStore = dataStore;
    this.typePath = typePath;
    this.metaData = dataStore.getMetaData(typePath);
    if (metaData == null) {
      throw new IllegalArgumentException("Unknown type " + typePath);
    }
    this.table = dataStore.getTable(typePath);
    this.fields = fields;
    this.whereClause = whereClause;
    setBoundingBox(boundingBox);
    this.dataObjectFactory = dataStore.getDataObjectFactory();
  }

  @Override
  protected void doClose() {
    if (dataStore != null) {
      synchronized (dataStore) {
        try {
          if (rows != null) {
            try {
              rows.Close();
            } catch (final NullPointerException e) {
            }
            rows = null;
          }
          if (table != null) {
            dataStore.closeTable(table);
            table = null;
          }
          dataStore = null;
          metaData = null;
          fields = null;
          whereClause = null;
          boundingBox = null;
        } catch (final Throwable t) {
          t.printStackTrace();
        }
      }
    }
  }

  @Override
  protected void doInit() {
    synchronized (dataStore) {
      if (boundingBox == null) {
        rows = table.search(fields, whereClause, true);
      } else {
        final com.revolsys.gis.esri.gdb.file.capi.swig.Envelope boundingBox = GeometryConverter.toEsri(this.boundingBox);
        rows = table.search(fields, whereClause, boundingBox, true);
      }
    }
  }

  protected DataObjectMetaData getMetaData() {
    if (metaData == null) {
      hasNext();
    }
    return metaData;
  }

  @Override
  protected DataObject getNext() throws NoSuchElementException {
    synchronized (dataStore) {
      final Row row = rows.next();
      if (row == null) {
        throw new NoSuchElementException();
      } else {
        try {
          final DataObject object = dataObjectFactory.createDataObject(metaData);
          for (final Attribute attribute : metaData.getAttributes()) {
            final String name = attribute.getName();
            final AbstractFileGdbAttribute esriAttribute = (AbstractFileGdbAttribute)attribute;
            final Object value = esriAttribute.getValue(row);
            object.setValue(name, value);
          }
          object.setState(DataObjectState.Persisted);
          return object;
        } finally {
          row.delete();
        }
      }
    }
  }

  public void setBoundingBox(final BoundingBox boundingBox) {
    this.boundingBox = boundingBox;
    if (boundingBox != null) {
      final Attribute geometryAttribute = metaData.getGeometryAttribute();
      if (geometryAttribute != null) {
        final GeometryFactory geometryFactory = geometryAttribute.getProperty(AttributeProperties.GEOMETRY_FACTORY);
        if (geometryFactory != null) {
          this.boundingBox = boundingBox.convert(geometryFactory);
        }
      }
    }
  }

  public void setWhereClause(final String whereClause) {
    this.whereClause = whereClause;
  }

  @Override
  public String toString() {
    return typePath.toString();
  }

}
