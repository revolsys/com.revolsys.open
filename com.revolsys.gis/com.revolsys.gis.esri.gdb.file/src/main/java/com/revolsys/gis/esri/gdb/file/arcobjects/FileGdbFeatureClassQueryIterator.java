package com.revolsys.gis.esri.gdb.file.arcobjects;

import java.util.NoSuchElementException;

import javax.xml.namespace.QName;

import com.esri.arcgis.geodatabase.IFeatureClass;
import com.esri.arcgis.geodatabase.IFeatureCursor;
import com.esri.arcgis.geodatabase.IRow;
import com.esri.arcgis.geodatabase.SpatialFilter;
import com.esri.arcgis.geometry.Envelope;
import com.revolsys.collection.AbstractIterator;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.esri.gdb.file.arcobjects.type.AbstractFileGdbAttribute;

public class FileGdbFeatureClassQueryIterator extends
  AbstractIterator<DataObject> {

  private final DataObjectFactory dataObjectFactory;

  private IFeatureClass featureClass;

  private String fields;

  private String whereClause;

  private BoundingBox boundingBox;

  private DataObjectMetaData metaData;

  private IFeatureCursor rows;

  private final QName typeName;

  FileGdbFeatureClassQueryIterator(
    final ArcObjectsFileGdbDataObjectStore dataStore, final QName typeName,
    final BoundingBox boundingBox) {
    this(dataStore, typeName, "", "", boundingBox);
  }

  FileGdbFeatureClassQueryIterator(
    final ArcObjectsFileGdbDataObjectStore dataStore, final QName typeName,
    final String fields, final String whereClause, final BoundingBox boundingBox) {
    this.typeName = typeName;
    this.metaData = dataStore.getMetaData(typeName);
    if (metaData == null) {
      throw new IllegalArgumentException("Unknown type " + typeName);
    }
    this.featureClass = (IFeatureClass)dataStore.getITable(typeName);
    this.fields = fields;
    this.whereClause = whereClause;
    setBoundingBox(boundingBox);
    this.dataObjectFactory = dataStore.getDataObjectFactory();
  }

  @Override
  protected void doClose() {
    rows = null;
    featureClass = null;
    metaData = null;
    fields = null;
    whereClause = null;
    boundingBox = null;
  }

  @Override
  protected void doInit() {
    try {
      final Envelope envelope = new Envelope();

      final double x1 = boundingBox.getMinX();
      final double y1 = boundingBox.getMinY();
      final double x2 = boundingBox.getMaxX();
      final double y2 = boundingBox.getMaxY();
      envelope.setXMin(x1);
      envelope.setYMin(y1);
      envelope.setXMax(x2);
      envelope.setYMax(y2);
      final SpatialFilter query = new SpatialFilter();
      query.setGeometryByRef(envelope);
      query.setSubFields(fields);
      query.setWhereClause(whereClause);
      rows = featureClass.search(query, true);
    } catch (final Exception e) {
      throw new RuntimeException("Unable to perform search", e);
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
    try {
      final IRow row = rows.nextFeature();
      if (row == null) {
        throw new NoSuchElementException();
      } else {
        final DataObject object = dataObjectFactory.createDataObject(metaData);
        for (final Attribute attribute : metaData.getAttributes()) {
          final String name = attribute.getName();
          final AbstractFileGdbAttribute esriAttribute = (AbstractFileGdbAttribute)attribute;
          final Object value = esriAttribute.getValue(row);
          object.setValue(name, value);
        }
        object.setState(DataObjectState.Persisted);
        return object;
      }
    } catch (final NoSuchElementException e) {
      throw e;
    } catch (final Exception e) {
      throw new RuntimeException("Unable to get next row", e);
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
    return typeName.toString();
  }

}
