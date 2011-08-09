package com.revolsys.gis.esri.gdb.file.arcobjects;

import java.util.NoSuchElementException;

import javax.xml.namespace.QName;

import com.esri.arcgis.geodatabase.IFeatureCursor;
import com.revolsys.collection.AbstractIterator;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;

public class FileGdbFeatureClassQueryIterator extends
  AbstractIterator<DataObject> {

  private final DataObjectFactory dataObjectFactory;

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
    this.fields = fields;
    this.whereClause = whereClause;
    setBoundingBox(boundingBox);
    this.dataObjectFactory = dataStore.getDataObjectFactory();
  }

  @Override
  protected void doClose() {
    rows = null;
    metaData = null;
    fields = null;
    whereClause = null;
    boundingBox = null;
  }

  @Override
  protected void doInit() {
    rows = ArcObjectsFileGdbDataObjectStore.invoke(ArcObjectsUtil.class,
      "search", metaData, fields, whereClause, boundingBox);
  }

  protected DataObjectMetaData getMetaData() {
    if (metaData == null) {
      hasNext();
    }
    return metaData;
  }

  @Override
  protected DataObject getNext() throws NoSuchElementException {
    return ArcObjectsFileGdbDataObjectStore.invoke(ArcObjectsUtil.class,
      "getNext", rows, metaData, dataObjectFactory);
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
