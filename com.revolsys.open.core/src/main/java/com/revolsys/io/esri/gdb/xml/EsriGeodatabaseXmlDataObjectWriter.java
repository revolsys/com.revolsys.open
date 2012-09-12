package com.revolsys.io.esri.gdb.xml;

import java.io.Writer;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.ProjectedCoordinateSystem;
import com.revolsys.gis.cs.esri.EsriCoordinateSystems;
import com.revolsys.gis.cs.esri.EsriCsWktWriter;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.PathUtil;
import com.revolsys.io.esri.gdb.xml.type.EsriGeodatabaseXmlFieldType;
import com.revolsys.io.esri.gdb.xml.type.EsriGeodatabaseXmlFieldTypeRegistry;
import com.revolsys.io.xml.XmlConstants;
import com.revolsys.io.xml.XmlWriter;
import com.revolsys.io.xml.XsiConstants;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class EsriGeodatabaseXmlDataObjectWriter extends
  AbstractWriter<DataObject> implements EsriGeodatabaseXmlConstants {
  private static final Logger LOG = LoggerFactory.getLogger(EsriGeodatabaseXmlDataObjectWriter.class);

  private int datasetId = 1;

  private int objectId = 1;

  private final XmlWriter out;

  private boolean opened;

  private final DataObjectMetaData metaData;

  private String datasetType;

  private final EsriGeodatabaseXmlFieldTypeRegistry fieldTypes = EsriGeodatabaseXmlFieldTypeRegistry.INSTANCE;

  private String geometryType;

  public EsriGeodatabaseXmlDataObjectWriter(final DataObjectMetaData metaData,
    final Writer out) {
    this.metaData = metaData;
    this.out = new XmlWriter(out);
  }

  @Override
  public void close() {
    if (!opened) {
      writeHeader(null);
    }

    writeFooter();
    out.close();
  }

  @Override
  public void flush() {
    out.flush();
  }

  @Override
  public void write(final DataObject object) {
    if (!opened) {
      writeHeader(object.getGeometryValue());
      writeWorkspaceDataHeader();
    }
    out.startTag(RECORD);
    out.attribute(XsiConstants.TYPE, RECORD_TYPE);

    out.startTag(VALUES);
    out.attribute(XsiConstants.TYPE, VALUES_TYPE);

    for (final Attribute attribute : metaData.getAttributes()) {
      final String attributeName = attribute.getName();
      final Object value = object.getValue(attributeName);
      final DataType type = attribute.getType();
      final EsriGeodatabaseXmlFieldType fieldType = fieldTypes.getFieldType(type);
      if (fieldType != null) {
        fieldType.writeValue(out, value);
      }
    }
    if (metaData.getAttribute("OBJECTID") == null) {
      final EsriGeodatabaseXmlFieldType fieldType = fieldTypes.getFieldType(DataTypes.INTEGER);
      fieldType.writeValue(out, objectId++);
    }

    out.endTag(VALUES);

    out.endTag(RECORD);
  }

  private void writeDataElement(final DataObjectMetaData metaData,
    final Geometry geometry) {
    final String dataElementType;
    final Attribute geometryAttribute = metaData.getGeometryAttribute();
    boolean hasGeometry = false;
    DataType geometryDataType = null;
    if (geometryAttribute != null) {
      geometryDataType = geometryAttribute.getType();
      if (fieldTypes.getFieldType(geometryDataType) != null) {
        hasGeometry = true;

        if (geometryDataType.equals(DataTypes.POINT)) {
          geometryType = GEOMETRY_TYPE_POINT;
        } else if (geometryDataType.equals(DataTypes.MULTI_POINT)) {
          geometryType = GEOMETRY_TYPE_MULTI_POINT;
        } else if (geometryDataType.equals(DataTypes.LINE_STRING)) {
          geometryType = GEOMETRY_TYPE_POLYLINE;
        } else if (geometryDataType.equals(DataTypes.MULTI_LINE_STRING)) {
          geometryType = GEOMETRY_TYPE_POLYLINE;
        } else if (geometryDataType.equals(DataTypes.POLYGON)) {
          geometryType = GEOMETRY_TYPE_POLYGON;
        } else {
          if (geometry instanceof Point) {
            geometryType = GEOMETRY_TYPE_POINT;
          } else if (geometry instanceof MultiPoint) {
            geometryType = GEOMETRY_TYPE_MULTI_POINT;
          } else if (geometry instanceof LineString) {
            geometryType = GEOMETRY_TYPE_POLYLINE;
          } else if (geometry instanceof MultiLineString) {
            geometryType = GEOMETRY_TYPE_POLYLINE;
          } else if (geometry instanceof Polygon) {
            geometryType = GEOMETRY_TYPE_POLYGON;
          } else {
            hasGeometry = false;
          }
        }
      }
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

    final String path = metaData.getPath();
    final String localName = PathUtil.getName(path);
    out.element(CATALOG_PATH, "/FC=" + localName);
    out.element(NAME, localName);
    out.element(METADATA_RETRIEVED, true);

    out.startTag(METADATA);
    out.attribute(XsiConstants.TYPE, XML_PROPERTY_SET_TYPE);
    out.startTag(XML_DOC);
    out.text("<?xml version=\"1.0\"?>");
    out.text("<metadata xml:lang=\"en\">");
    out.text("<Esri>");
    out.text("<MetaID>{");
    out.text(UUID.randomUUID().toString().toUpperCase());
    out.text("}</MetaID>");
    out.text("<CreaDate>");
    final Timestamp date = new Timestamp(System.currentTimeMillis());
    out.text(new SimpleDateFormat("yyyyMMdd").format(date));
    out.text("</CreaDate>");
    out.text("<CreaTime>");
    out.text(new SimpleDateFormat("HHmmssSS").format(date));
    out.text("</CreaTime>");
    out.text("<SyncOnce>TRUE</SyncOnce>");
    out.text("</Esri>");
    out.text("</metadata>");
    out.endTag(XML_DOC);
    out.endTag(METADATA);

    out.element(DATASET_TYPE, datasetType);
    out.element(DSID, datasetId++);
    out.element(VERSIONED, false);
    out.element(CAN_VERSION, false);
    out.element(HAS_OID, true);
    out.element(OBJECT_ID_FIELD_NAME, "OBJECTID");
    writeFields(metaData);

    out.element(CLSID, "{52353152-891A-11D0-BEC6-00805F7C4268}");
    out.emptyTag(EXTCLSID);
    out.startTag(RELATIONSHIP_CLASS_NAMES);
    out.attribute(XsiConstants.TYPE, NAMES_TYPE);
    out.endTag(RELATIONSHIP_CLASS_NAMES);
    out.element(ALIAS_NAME, localName);
    out.emptyTag(MODEL_NAME);
    out.element(HAS_GLOBAL_ID, false);
    out.emptyTag(GLOBAL_ID_FIELD_NAME);
    out.emptyTag(RASTER_FIELD_NAME);
    out.startTag(EXTENSION_PROPERTIES);
    out.attribute(XsiConstants.TYPE, PROPERTY_SET_TYPE);
    out.startTag(PROPERTY_ARRAY);
    out.attribute(XsiConstants.TYPE, PROPERTY_ARRAY_TYPE);
    out.endTag(PROPERTY_ARRAY);
    out.endTag(EXTENSION_PROPERTIES);
    out.startTag(CONTROLLER_MEMBERSHIPS);
    out.attribute(XsiConstants.TYPE, CONTROLLER_MEMBERSHIPS_TYPE);
    out.endTag(CONTROLLER_MEMBERSHIPS);
    if (hasGeometry) {
      out.element(FEATURE_TYPE, FEATURE_TYPE_SIMPLE);
      out.element(SHAPE_TYPE, geometryType);
      out.element(SHAPE_FIELD_NAME, geometryAttribute.getName());
      final GeometryFactory geometryFactory = geometryAttribute.getProperty(AttributeProperties.GEOMETRY_FACTORY);
      out.element(HAS_M, false);
      out.element(HAS_Z, geometryFactory.hasZ());
      out.element(HAS_SPATIAL_INDEX, false);
      out.emptyTag(AREA_FIELD_NAME);
      out.emptyTag(LENGTH_FIELD_NAME);

      writeExtent(geometryFactory);
      writeSpatialReference(geometryFactory);
    }

    out.endTag(DATA_ELEMENT);
  }

  public void writeExtent(final GeometryFactory geometryFactory) {
    if (geometryFactory != null) {
      final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
      if (coordinateSystem != null) {
        final BoundingBox boundingBox = coordinateSystem.getAreaBoundingBox();
        out.startTag(EXTENT);
        out.attribute(XsiConstants.TYPE, ENVELOPE_N_TYPE);
        out.element(X_MIN, boundingBox.getMinX());
        out.element(Y_MIN, boundingBox.getMinY());
        out.element(X_MAX, boundingBox.getMaxX());
        out.element(Y_MAX, boundingBox.getMaxY());
        out.element(Z_MIN, boundingBox.getMinZ());
        out.element(Z_MAX, boundingBox.getMaxZ());
        writeSpatialReference(geometryFactory);
        out.endTag(EXTENT);
      }
    }
  }

  private void writeField(final Attribute attribute) {
    final String fieldName = attribute.getName();
    if (fieldName.equals("OBJECTID")) {
      writeOidField();
    } else {
      final DataType dataType = attribute.getType();
      final EsriGeodatabaseXmlFieldType fieldType = fieldTypes.getFieldType(dataType);
      if (fieldType == null) {
        LOG.error("Data type not supported " + dataType);
      } else {
        out.startTag(FIELD);
        out.attribute(XsiConstants.TYPE, FIELD_TYPE);
        out.element(NAME, fieldName);
        out.element(TYPE, fieldType.getEsriFieldType());
        out.element(IS_NULLABLE, !attribute.isRequired());
        int length = fieldType.getFixedLength();
        if (length < 0) {
          length = attribute.getLength();
        }
        out.element(LENGTH, length);
        final int precision;
        if (fieldType.isUsePrecision()) {
          precision = attribute.getLength();
        } else {
          precision = 0;
        }
        out.element(PRECISION, precision);
        out.element(SCALE, attribute.getScale());

        final GeometryFactory geometryFactory = attribute.getProperty(AttributeProperties.GEOMETRY_FACTORY);
        if (geometryFactory != null) {
          out.startTag(GEOMETRY_DEF);
          out.attribute(XsiConstants.TYPE, GEOMETRY_DEF_TYPE);

          out.element(AVG_NUM_POINTS, 0);

          out.element(GEOMETRY_TYPE, geometryType);
          out.element(HAS_M, false);
          out.element(HAS_Z, geometryFactory.hasZ());

          writeSpatialReference(geometryFactory);

          out.endTag();
        }
        out.endTag(FIELD);
      }
    }
  }

  private void writeFields(final DataObjectMetaData metaData) {
    out.startTag(FIELDS);
    out.attribute(XsiConstants.TYPE, FIELDS_TYPE);

    out.startTag(FIELD_ARRAY);
    out.attribute(XsiConstants.TYPE, FIELD_ARRAY_TYPE);

    for (final Attribute attribute : metaData.getAttributes()) {
      writeField(attribute);
    }
    if (metaData.getAttribute("OBJECTID") == null) {
      writeOidField();
    }
    out.endTag(FIELD_ARRAY);

    out.endTag(FIELDS);
  }

  public void writeFooter() {
    out.endDocument();
  }

  private void writeHeader(final Geometry geometry) {
    opened = true;
    out.startDocument();
    out.startTag(WORKSPACE);
    out.setPrefix(XsiConstants.PREFIX, XsiConstants.NAMESPACE_URI);
    out.setPrefix(XmlConstants.XML_SCHEMA_NAMESPACE_PREFIX,
      XmlConstants.XML_SCHEMA_NAMESPACE_URI);

    out.startTag(WORKSPACE_DEFINITION);
    out.attribute(XsiConstants.TYPE, WORKSPACE_DEFINITION_TYPE);

    out.element(WORKSPACE_TYPE, "esriLocalDatabaseWorkspace");
    out.element(VERSION, "");

    out.startTag(DOMAINS);
    out.attribute(XsiConstants.TYPE, DOMAINS_TYPE);
    out.endTag(DOMAINS);

    out.startTag(DATASET_DEFINITIONS);
    out.attribute(XsiConstants.TYPE, DATASET_DEFINITIONS_TYPE);
    writeDataElement(metaData, geometry);
    out.endTag(DATASET_DEFINITIONS);

    out.endTag(WORKSPACE_DEFINITION);
  }

  private void writeOidField() {
    out.startTag(FIELD);
    out.attribute(XsiConstants.TYPE, FIELD_TYPE);
    out.element(NAME, "OBJECTID");
    out.element(TYPE, FIELD_TYPE_OBJECT_ID);
    out.element(IS_NULLABLE, false);
    out.element(LENGTH, 4);
    out.element(PRECISION, 10);
    out.element(SCALE, 0);

    out.element(REQUIRED, true);
    out.element(EDIATBLE, false);
    out.endTag(FIELD);
  }

  public void writeSpatialReference(final GeometryFactory geometryFactory) {
    if (geometryFactory != null) {
      final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
      if (coordinateSystem != null) {
        final CoordinateSystem esriCoordinateSystem = EsriCoordinateSystems.getCoordinateSystem(coordinateSystem);
        if (esriCoordinateSystem != null) {
          out.startTag(SPATIAL_REFERENCE);
          if (esriCoordinateSystem instanceof ProjectedCoordinateSystem) {
            out.attribute(XsiConstants.TYPE, PROJECTED_COORDINATE_SYSTEM_TYPE);
          } else {
            out.attribute(XsiConstants.TYPE, GEOGRAPHIC_COORDINATE_SYSTEM_TYPE);
          }
          out.element(WKT, EsriCsWktWriter.toWkt(esriCoordinateSystem));
          out.element(X_ORIGIN, 0);
          out.element(Y_ORIGIN, 0);
          final double scaleXy = geometryFactory.getScaleXY();
          out.element(XY_SCALE, (int)scaleXy);
          out.element(Z_ORIGIN, 0);
          final double scaleZ = geometryFactory.getScaleZ();
          out.element(Z_SCALE, (int)scaleZ);
          out.element(M_ORIGIN, 0);
          out.element(M_SCALE, 1);
          out.element(XY_TOLERANCE, 1.0 / scaleXy * 2.0);
          out.element(Z_TOLERANCE, 1.0 / scaleZ * 2.0);
          out.element(M_TOLERANCE, 1);
          out.element(HIGH_PRECISION, true);
          out.element(WKID, coordinateSystem.getId());
          out.endTag(SPATIAL_REFERENCE);
        }
      }
    }
  }

  public void writeWorkspaceDataHeader() {
    out.startTag(WORKSPACE_DATA);
    out.attribute(XsiConstants.TYPE, WORKSPACE_DATA_TYPE);

    out.startTag(DATASET_DATA);
    out.attribute(XsiConstants.TYPE, DATASET_DATA_TABLE_DATA);

    out.element(DATASET_NAME, metaData.getTypeName());
    out.element(DATASET_TYPE, datasetType);

    out.startTag(DATA);
    out.attribute(XsiConstants.TYPE, DATA_RECORD_SET);

    writeFields(metaData);

    out.startTag(RECORDS);
    out.attribute(XsiConstants.TYPE, RECORDS_TYPE);

  }

}
