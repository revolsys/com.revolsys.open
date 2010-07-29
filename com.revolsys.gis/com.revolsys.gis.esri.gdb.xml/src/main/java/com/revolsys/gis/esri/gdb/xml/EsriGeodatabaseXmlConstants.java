package com.revolsys.gis.esri.gdb.xml;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;

public interface EsriGeodatabaseXmlConstants {
  QName CATALOG_PATH = new QName("CatalogPath");

  QName Data = new QName("Data");

  QName DATA = new QName("Data");

  QName DATA_ELEMENT = new QName("DataElement");

  String DATA_ELEMENT_FEATURE_CLASS = "esri:DEFeatureClass";

  String DATA_ELEMENT_TABLE = "esri:DETable";

  String DATA_RECORD_SET = "esri:RecordSet";

  QName DATASET_DATA = new QName("DatasetData");

  String DATASET_DATA_TABLE_DATA = "esri:TableData";

  QName DATASET_DEFINITIONS = new QName("DatasetDefinitions");

  String DATASET_DEFINITIONS_TYPE = "esri:ArrayOfDataElement";

  QName DATASET_NAME = new QName("DatasetName");

  QName DATASET_TYPE = new QName("DatasetType");

  String DATASET_TYPE_FEATURE_CLASS = "esriDTFeatureClass";

  String DATASET_TYPE_TABLE = "esriDTTable";

  QName EDIATBLE = new QName("Editable");

  QName FEATURE_TYPE = new QName("FeatureType");

  String FEATURE_TYPE_SIMPLE = "esriFTSimple";

  QName FIELD = new QName("Field");

  QName FIELD_ARRAY = new QName("FieldArray");

  String FIELD_ARRAY_TYPE = "esri:ArrayOfField";

  String FIELD_TYPE = "esri:Field";

  QName FIELDS = new QName("Fields");

  String FIELDS_TYPE = "esri:Fields";

  String FILE_EXTENSION = "gdbx";

  String FORMAT_DESCRIPTION = "ESRI Geodatabase (XML)";

  QName GEOMETRY_DEF = new QName("GeometryDef");

  String GEOMETRY_DEF_TYPE = "esri:GeometryDef";

  String GEOMETRY_TYPE__MULTI_POINT = "esriGeometryMultipoint";

  String GEOMETRY_TYPE_MULTI_PATCH = "esriGeometryMultiPatch";

  String GEOMETRY_TYPE_POINT = "esriGeometryPoint";

  String GEOMETRY_TYPE_POLYGON = "esriGeometryPolygon";

  String GEOMETRY_TYPE_POLYLINE = "esriGeometryPolyline";

  QName HAS_OBJECT_ID = new QName("HasOID");

  QName IS_NULLABLE = new QName("IsNullable");

  QName LENGTH = new QName("Length");

  String MEDIA_TYPE = "text/xml";

  QName NAME = new QName("Name");

  String NAMESPACE_URI_93 = "http://www.esri.com/schemas/ArcGIS/9.3";

  QName OBJECT_ID_FIELD_NAME = new QName("OIDFieldName");

  QName PRECISION = new QName("Precision");

  QName RECORD = new QName("Record");

  String RECORD_TYPE = "esri:Record";

  QName RECORDS = new QName("Records");

  String RECORDS_TYPE = "esri:ArrayOfRecord";

  QName REQUIRED = new QName("Required");

  QName SCALE = new QName("Scale");

  QName SHAPE_FIELD_NAME = new QName("ShapeFieldName");

  QName SHAPE_TYPE = new QName("ShapeType");

  QName SPATIAL_REFERENCE = new QName("SpatialReference");

  QName TYPE = new QName("Type");

  QName VALUE = new QName("Value");

  QName VALUES = new QName("Values");

  String VALUES_TYPE = "esri:ArrayOfValue";

  QName VERSION = new QName("Version");

  QName WKT = new QName("WKT");

  String WORKSPACE = "Workspace";

  QName WORKSPACE_DATA = new QName("WorkspaceData");

  String WORKSPACE_DATA_TYPE = "esri:WorkspaceData";

  QName WORKSPACE_DEFINITION = new QName("WorkspaceDefinition");

  String WORKSPACE_DEFINITION_TYPE = "esri:WorkspaceDefinition";

  QName WORKSPACE_TYPE = new QName("WorkspaceType");

  String FIELD_TYPE_INTEGER = "esriFieldTypeInteger";

  String FIELD_TYPE_SMALL_INTEGER = "esriFieldTypeSmallInteger";

  String FIELD_TYPE_DOUBLE = "esriFieldTypeDouble";

  String FIELD_TYPE_SINGLE = "esriFieldTypeSingle";

  String FIELD_TYPE_STRING = "esriFieldTypeString";

  String FIELD_TYPE_DATE = "esriFieldTypeDate";

  String FIELD_TYPE_GEOMETRY = "esriFieldTypeGeometry";

  String FIELD_TYPE_OBJECT_ID = "esriFieldTypeOID";

  String FIELD_TYPE_BLOB = "esriFieldTypeBlob";

  String FIELD_TYPE_GLOBAL_ID = "esriFieldTypeGlobalID";

  String FIELD_TYPE_RASTER = "esriFieldTypeRaster";

  String FIELD_TYPE_GUID = "esriFieldTypeGUID";

  Collection<DataType> XML_SCHEMA_DATA_TYPES = Arrays.asList(DataTypes.ANY_URI,
    DataTypes.BASE64_BINARY, DataTypes.BOOLEAN, DataTypes.BYTE, DataTypes.DATE,
    DataTypes.DATE_TIME, DataTypes.DECIMAL, DataTypes.DOUBLE,
    DataTypes.DURATION, DataTypes.FLOAT, DataTypes.INT, DataTypes.INTEGER,
    DataTypes.LONG, DataTypes.QNAME, DataTypes.SHORT, DataTypes.STRING);

}
