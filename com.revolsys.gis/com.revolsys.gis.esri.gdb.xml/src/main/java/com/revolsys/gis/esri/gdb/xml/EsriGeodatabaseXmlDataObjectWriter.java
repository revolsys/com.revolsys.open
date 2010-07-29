package com.revolsys.gis.esri.gdb.xml;

import java.io.Writer;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.esri.EsriCsWktWriter;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.esri.gdb.xml.type.EsriGeodatabaseXmlFieldType;
import com.revolsys.gis.esri.gdb.xml.type.EsriGeodatabaseXmlFieldTypeRegistry;
import com.revolsys.io.AbstractWriter;
import com.revolsys.xml.XmlContants;
import com.revolsys.xml.XsiConstants;
import com.revolsys.xml.io.XmlWriter;

public class EsriGeodatabaseXmlDataObjectWriter extends
  AbstractWriter<DataObject> implements EsriGeodatabaseXmlConstants {
  private static final Logger LOG = LoggerFactory.getLogger(EsriGeodatabaseXmlDataObjectWriter.class);

  private final XmlWriter out;

  private String namespaceUri = EsriGeodatabaseXmlConstants.NAMESPACE_URI_93;

  private boolean opened;

  private DataObjectMetaData metaData;

  private String datasetType;

  private EsriGeodatabaseXmlFieldTypeRegistry fieldTypes = EsriGeodatabaseXmlFieldTypeRegistry.INSTANCE;

  public EsriGeodatabaseXmlDataObjectWriter(
    DataObjectMetaData metaData,
    final Writer out) {
    this.metaData = metaData;
    this.out = new XmlWriter(out);
  }

  private void writeHeader() {
    opened = true;
    out.startDocument();
    out.startTag(new QName(namespaceUri, WORKSPACE, "esri"));
    out.setPrefix(XsiConstants.PREFIX, XsiConstants.NAMESPACE_URI);
    out.setPrefix(XmlContants.XML_SCHEMA_NAMESPACE_PREFIX,
      XmlContants.XML_SCHEMA_NAMESPACE_URI);

    out.startTag(WORKSPACE_DEFINITION);
    out.attribute(XsiConstants.TYPE, WORKSPACE_DEFINITION_TYPE);

    out.element(WORKSPACE_TYPE, "esriLocalDatabaseWorkspace");
    out.element(VERSION, "");

    out.startTag(DATASET_DEFINITIONS);
    out.attribute(XsiConstants.TYPE, DATASET_DEFINITIONS_TYPE);
    writeDataElement(metaData);
    out.endTag(DATASET_DEFINITIONS);

    out.endTag(WORKSPACE_DEFINITION);
  }

  private void writeDataElement(
    DataObjectMetaData metaData) {
    final String dataElementType;
    Attribute geometryAttribute = metaData.getGeometryAttribute();
    boolean hasGeometry;
    if (geometryAttribute != null
      && fieldTypes.getFieldType(geometryAttribute.getType()) != null) {
      hasGeometry = true;
    } else {
      hasGeometry = false;
    }

    if (hasGeometry) {
      dataElementType = DATA_ELEMENT_FEATURE_CLASS;
      datasetType = DATASET_TYPE_FEATURE_CLASS;
    } else {
      dataElementType = DATA_ELEMENT_TABLE;
      datasetType = DATASET_TYPE_TABLE;
    }

    out.startTag(DATA_ELEMENT);
    out.attribute(XsiConstants.TYPE, dataElementType);

    final QName typeName = metaData.getName();
    final String localName = typeName.getLocalPart();
    out.element(CATALOG_PATH, "/FC=" + localName);
    out.element(NAME, localName);
    out.element(DATASET_TYPE, datasetType);
    Attribute idAttribute = metaData.getIdAttribute();
    final boolean hasId = idAttribute != null;
    out.element(HAS_OBJECT_ID, hasId);
    if (hasId) {
      out.element(OBJECT_ID_FIELD_NAME, idAttribute.getName());
    }
    writeFields(metaData);

    if (hasGeometry) {
      out.element(FEATURE_TYPE, FEATURE_TYPE_SIMPLE);
      // TODO add geometry type
      out.element(SHAPE_TYPE, GEOMETRY_TYPE_POINT);
      out.element(SHAPE_FIELD_NAME, geometryAttribute.getName());
      writeSpatialReference(geometryAttribute);
    }

    out.endTag(DATA_ELEMENT);
  }

  private void writeSpatialReference(
    Attribute geometryAttribute) {
    GeometryFactory geometryFactory = geometryAttribute.getProperty(AttributeProperties.GEOMETRY_FACTORY);
    writeSpatialReference(geometryFactory);
  }

  public void writeSpatialReference(
    GeometryFactory geometryFactory) {
    if (geometryFactory != null) {
      CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
      if (coordinateSystem != null) {
        out.startTag(SPATIAL_REFERENCE);
        out.element(WKT, EsriCsWktWriter.toWkt(coordinateSystem));
        out.endTag(SPATIAL_REFERENCE);
      }
    }
  }

  private void writeFields(
    DataObjectMetaData metaData) {
    out.startTag(FIELDS);
    out.attribute(XsiConstants.TYPE, FIELDS_TYPE);

    out.startTag(FIELD_ARRAY);
    out.attribute(XsiConstants.TYPE, FIELD_ARRAY_TYPE);

    for (Attribute attribute : metaData.getAttributes()) {
      writeField(attribute);
    }
    out.endTag(FIELD_ARRAY);

    out.endTag(FIELDS);
  }

  private void writeField(
    Attribute attribute) {
    final DataType dataType = attribute.getType();
    final EsriGeodatabaseXmlFieldType fieldType = fieldTypes.getFieldType(dataType);
    if (fieldType == null) {
      LOG.error("Data type not supported " + dataType);
    } else {
      out.startTag(FIELD);
      out.attribute(XsiConstants.TYPE, FIELD_TYPE);
      out.element(NAME, attribute.getName());
      out.element(TYPE, fieldType.getEsriFieldTypeName());
      out.element(IS_NULLABLE, !attribute.isRequired());
      out.element(LENGTH, attribute.getLength());
      out.element(PRECISION, attribute.getLength());
      out.element(SCALE, attribute.getScale());
      out.element(REQUIRED, attribute.isRequired());

      GeometryFactory geometryFactory = attribute.getProperty(AttributeProperties.GEOMETRY_FACTORY);
      if (geometryFactory != null) {
        out.startTag(GEOMETRY_DEF);
        out.attribute(XsiConstants.TYPE, GEOMETRY_DEF_TYPE);

        writeSpatialReference(geometryFactory);

        out.endTag();
      }
      out.endTag(FIELD);
    }
  }

  public void close() {
    if (!opened) {
      writeHeader();
    }

    writeFooter();
    out.close();
  }

  public void writeFooter() {
    out.endDocument();
  }

  public void flush() {
    out.flush();
  }

  public void write(
    final DataObject object) {
    if (!opened) {
      writeHeader();
      writeWorkspaceDataHeader();
    }
    out.startTag(RECORD);
    out.attribute(XsiConstants.TYPE, RECORD_TYPE);

    out.startTag(VALUES);
    out.attribute(XsiConstants.TYPE, VALUES_TYPE);

    for (Attribute attribute : metaData.getAttributes()) {
      final String attributeName = attribute.getName();
      final Object value = object.getValue(attributeName);
      if (value != null) {
        final DataType type = attribute.getType();
        final EsriGeodatabaseXmlFieldType fieldType = fieldTypes.getFieldType(type);
        if (fieldType != null) {
          fieldType.writeValue(out, value);
        }
      }
    }

    out.endTag(VALUES);

    out.endTag(RECORD);
  }

  public void writeWorkspaceDataHeader() {
    out.startTag(WORKSPACE_DATA);
    out.attribute(XsiConstants.TYPE, WORKSPACE_DATA_TYPE);

    out.startTag(DATASET_DATA);
    out.attribute(XsiConstants.TYPE, DATASET_DATA_TABLE_DATA);

    out.element(DATASET_NAME, metaData.getName().getLocalPart());
    out.element(DATASET_TYPE, datasetType);

    out.startTag(DATA);
    out.attribute(XsiConstants.TYPE, DATA_RECORD_SET);

    writeFields(metaData);

    out.startTag(RECORDS);
    out.attribute(XsiConstants.TYPE, RECORDS_TYPE);

  }

}
