package com.revolsys.gis.esri.gdb.xml;

import java.util.Arrays;
import java.util.Collection;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;

public interface EsriGeodatabaseXmlConstants {
  QName ALIAS_NAME = new QName("AliasName");

  QName AREA_FIELD_NAME = new QName("AreaFieldName");

  QName AVG_NUM_POINTS = new QName("AvgNumPoints");

  QName CAN_VERSION = new QName("CanVersion");

  QName CATALOG_PATH = new QName("CatalogPath");

  String POLYLINE_N_TYPE = "esri:PolylineN";

  QName CLSID = new QName("CLSID");

  QName CONTROLLER_MEMBERSHIPS = new QName("ControllerMemberships");

  String CONTROLLER_MEMBERSHIPS_TYPE = "esri:ArrayOfControllerMembership";

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

  QName DOMAINS = new QName("Domains");

  String DOMAINS_TYPE = "esri:ArrayOfDomain";

  QName DSID = new QName("DSID");

  QName EDIATBLE = new QName("Editable");

  String ENVELOPE_N_TYPE = "esri:EnvelopeN";

  QName EXTCLSID = new QName("EXTCLSID");

  QName EXTENSION_PROPERTIES = new QName("ExtensionProperties");

  QName EXTENT = new QName("Extent");

  QName FEATURE_TYPE = new QName("FeatureType");

  String FEATURE_TYPE_SIMPLE = "esriFTSimple";

  QName FIELD = new QName("Field");

  QName FIELD_ARRAY = new QName("FieldArray");

  String FIELD_ARRAY_TYPE = "esri:ArrayOfField";

  String FIELD_TYPE = "esri:Field";

  String FIELD_TYPE_BLOB = "esriFieldTypeBlob";

  String FIELD_TYPE_DATE = "esriFieldTypeDate";

  String FIELD_TYPE_DOUBLE = "esriFieldTypeDouble";

  String FIELD_TYPE_GEOMETRY = "esriFieldTypeGeometry";

  String FIELD_TYPE_GLOBAL_ID = "esriFieldTypeGlobalID";

  String FIELD_TYPE_GUID = "esriFieldTypeGUID";

  String FIELD_TYPE_INTEGER = "esriFieldTypeInteger";

  String FIELD_TYPE_OBJECT_ID = "esriFieldTypeOID";

  String FIELD_TYPE_RASTER = "esriFieldTypeRaster";

  String FIELD_TYPE_SINGLE = "esriFieldTypeSingle";

  String FIELD_TYPE_SMALL_INTEGER = "esriFieldTypeSmallInteger";

  String FIELD_TYPE_STRING = "esriFieldTypeString";

  QName FIELDS = new QName("Fields");

  String FIELDS_TYPE = "esri:Fields";

  String FILE_EXTENSION = "gdbx";

  String FORMAT_DESCRIPTION = "ESRI Geodatabase (XML)";

  String GEOGRAPHIC_COORDINATE_SYSTEM_TYPE = "esri:GeographicCoordinateSystem";

  QName GEOMETRY_DEF = new QName("GeometryDef");

  String GEOMETRY_DEF_TYPE = "esri:GeometryDef";

  QName GEOMETRY_TYPE = new QName("GeometryType");

  String GEOMETRY_TYPE_MULTI_PATCH = "esriGeometryMultiPatch";

  String GEOMETRY_TYPE_MULTI_POINT = "esriGeometryMultipoint";

  String GEOMETRY_TYPE_POINT = "esriGeometryPoint";

  String GEOMETRY_TYPE_POLYGON = "esriGeometryPolygon";

  String GEOMETRY_TYPE_POLYLINE = "esriGeometryPolyline";

  QName GLOBAL_ID_FIELD_NAME = new QName("GlobalIDFieldName");

  QName HAS_GLOBAL_ID = new QName("HasGlobalID");

  QName HAS_ID = new QName("HasID");

  QName HAS_M = new QName("HasM");

  QName HAS_OID = new QName("HasOID");

  QName HAS_SPATIAL_INDEX = new QName("HasSpatialIndex");

  QName HAS_Z = new QName("HasZ");

  QName HIGH_PRECISION = new QName("HighPrecision");

  QName IS_NULLABLE = new QName("IsNullable");

  QName LENGTH = new QName("Length");

  QName LENGTH_FIELD_NAME = new QName("LengthFieldName");

  QName M = new QName("M");

  QName M_ORIGIN = new QName("MOrigin");

  QName M_SCALE = new QName("MScale");

  QName M_TOLERANCE = new QName("MTolerance");

  String MEDIA_TYPE = "text/xml";

  QName METADATA_RETRIEVED = new QName("MetadataRetrieved");

  QName METADATA = new QName("Metadata");

  QName MODEL_NAME = new QName("ModelName");

  QName XML_DOC = new QName("XmlDoc");

  String XML_PROPERTY_SET_TYPE = "esri:XmlPropertySet";

  QName NAME = new QName("Name");

  String NAMES_TYPE = "esri:Names";

  String NAMESPACE_URI_93 = "http://www.esri.com/schemas/ArcGIS/9.3";

  QName OBJECT_ID_FIELD_NAME = new QName("OIDFieldName");

  QName PATH_ARRAY = new QName("PathArray");

  String PATH_ARRAY_TYPE = "esri:ArrayOfPath";

  QName PATH = new QName("Path");

  String PATH_TYPE = "esri:Path";

  QName POINT = new QName("Point");

  QName POINT_ARRAY = new QName("PointArray");

  String POINT_ARRAY_TYPE = "esri:ArrayOfPoint";

  QName RING_ARRAY = new QName("RingArray");

  String RING_ARRAY_TYPE = "esri:ArrayOfRing";

  QName RING = new QName("Ring");

  String RING_TYPE = "esri:Ring";

  String POINT_N_TYPE = "esri:PointN";

  String POLYGON_N_TYPE = "esri:PolygonN";

  QName PRECISION = new QName("Precision");

  String PROJECTED_COORDINATE_SYSTEM_TYPE = "esri:ProjectedCoordinateSystem";

  QName PROPERTY_ARRAY = new QName("PropertyArray");

  String PROPERTY_ARRAY_TYPE = "esri:ArrayOfPropertySetProperty";

  String PROPERTY_SET_TYPE = "esri:PropertySet";

  QName RASTER_FIELD_NAME = new QName("RasterFieldName");

  QName RECORD = new QName("Record");

  String RECORD_TYPE = "esri:Record";

  QName RECORDS = new QName("Records");

  String RECORDS_TYPE = "esri:ArrayOfRecord";

  QName RELATIONSHIP_CLASS_NAMES = new QName("RelationshipClassNames");

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

  QName VERSIONED = new QName("Versioned");

  QName WKID = new QName("WKID");

  QName WKT = new QName("WKT");

  String WORKSPACE = "Workspace";

  QName WORKSPACE_DATA = new QName("WorkspaceData");

  String WORKSPACE_DATA_TYPE = "esri:WorkspaceData";

  QName WORKSPACE_DEFINITION = new QName("WorkspaceDefinition");

  String WORKSPACE_DEFINITION_TYPE = "esri:WorkspaceDefinition";

  QName WORKSPACE_TYPE = new QName("WorkspaceType");

  QName X = new QName("X");

  QName X_MAX = new QName("XMax");

  QName X_MIN = new QName("XMin");

  QName X_ORIGIN = new QName("XOrigin");

  Collection<DataType> XML_SCHEMA_DATA_TYPES = Arrays.asList(DataTypes.ANY_URI,
    DataTypes.BASE64_BINARY, DataTypes.BOOLEAN, DataTypes.BYTE, DataTypes.DATE,
    DataTypes.DATE_TIME, DataTypes.DECIMAL, DataTypes.DOUBLE,
    DataTypes.DURATION, DataTypes.FLOAT, DataTypes.INT, DataTypes.INTEGER,
    DataTypes.LONG, DataTypes.QNAME, DataTypes.SHORT, DataTypes.STRING);

  QName XY_SCALE = new QName("XYScale");

  QName XY_TOLERANCE = new QName("XYTolerance");

  QName Y = new QName("Y");

  QName Y_MAX = new QName("YMax");

  QName Y_MIN = new QName("YMin");

  QName Y_ORIGIN = new QName("YOrigin");

  QName Z = new QName("Z");

  QName Z_MAX = new QName("ZMax");

  QName Z_MIN = new QName("ZMin");

  QName Z_ORIGIN = new QName("ZOrigin");

  QName Z_SCALE = new QName("ZScale");

  QName Z_TOLERANCE = new QName("ZTolerance");

}
