package com.revolsys.gis.esri.gdb.xml;

import java.util.Arrays;
import java.util.Collection;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;

public interface EsriGeodatabaseXmlConstants {

  String _NAMESPACE_URI = "http://www.esri.com/schemas/ArcGIS/10.1";

  QName ALIAS_NAME = new QName(_NAMESPACE_URI, "AliasName", "esri");

  QName AREA_FIELD_NAME = new QName(_NAMESPACE_URI, "AreaFieldName", "esri");

  QName ARRAY_OF_FIELD = new QName(_NAMESPACE_URI, "ArrayOfField", "esri");

  QName ARRAY_OF_INDEX = new QName(_NAMESPACE_URI, "ArrayOfIndex", "esri");

  QName ARRAY_OF_PROPERTY_SET_PROPERTY = new QName(_NAMESPACE_URI,
    "ArrayOfPropertySetProperty", "esri");

  QName AVG_NUM_POINTS = new QName(_NAMESPACE_URI, "AvgNumPoints", "esri");

  QName CAN_VERSION = new QName(_NAMESPACE_URI, "CanVersion", "esri");

  QName CATALOG_PATH = new QName(_NAMESPACE_URI, "CatalogPath", "esri");

  QName CHILDREN = new QName(_NAMESPACE_URI, "Children", "esri");

  QName CHILDREN_EXPANDED = new QName(_NAMESPACE_URI, "ChildrenExpanded",
    "esri");

  QName CLSID = new QName(_NAMESPACE_URI, "CLSID", "esri");

  QName CONFIGURATION_KEYWORD = new QName(_NAMESPACE_URI,
    "ConfigurationKeyword", "esri");

  QName CONTROLLER_MEMBERSHIP = new QName(_NAMESPACE_URI,
    "ControllerMembership", "esri");

  QName CONTROLLER_MEMBERSHIPS = new QName(_NAMESPACE_URI,
    "ControllerMemberships", "esri");

  String CONTROLLER_MEMBERSHIPS_TYPE = "esri:ArrayOfControllerMembership";

  QName Data = new QName(_NAMESPACE_URI, "Data", "esri");

  QName DATA = new QName(_NAMESPACE_URI, "Data", "esri");

  QName DATA_ELEMENT = new QName(_NAMESPACE_URI, "DataElement", "esri");

  String DATA_ELEMENT_FEATURE_CLASS = "esri:DEFeatureClass";

  String DATA_ELEMENT_TABLE = "esri:DETable";

  String DATA_RECORD_SET = "esri:RecordSet";

  QName DATASET_DATA = new QName(_NAMESPACE_URI, "DatasetData", "esri");

  String DATASET_DATA_TABLE_DATA = "esri:TableData";

  QName DATASET_DEFINITIONS = new QName(_NAMESPACE_URI, "DatasetDefinitions",
    "esri");

  String DATASET_DEFINITIONS_TYPE = "esri:ArrayOfDataElement";

  QName DATASET_NAME = new QName(_NAMESPACE_URI, "DatasetName", "esri");

  QName DATASET_TYPE = new QName(_NAMESPACE_URI, "DatasetType", "esri");

  String DATASET_TYPE_FEATURE_CLASS = "esriDTFeatureClass";

  String DATASET_TYPE_TABLE = "esriDTTable";

  QName DE_DATASET = new QName(_NAMESPACE_URI, "DEDataset", "esri");

  QName DE_FEATURE_CLASS = new QName(_NAMESPACE_URI, "DEFeatureClass", "esri");

  QName DE_TABLE = new QName(_NAMESPACE_URI, "DETable", "esri");

  QName DEFAULT_SUBTYPE_CODE = new QName(_NAMESPACE_URI, "DefaultSubtypeCode",
    "esri");

  QName DEFAULT_VALUE = new QName(_NAMESPACE_URI, "DefaultValue", "esri");

  QName DOMAIN_NAME = new QName(_NAMESPACE_URI, "DomainName", "esri");

  QName DOMAINS = new QName(_NAMESPACE_URI, "Domains", "esri");

  String DOMAINS_TYPE = "esri:ArrayOfDomain";

  QName DSID = new QName(_NAMESPACE_URI, "DSID", "esri");

  QName EDIATBLE = new QName(_NAMESPACE_URI, "Editable", "esri");

  QName ENVELOPE = new QName(_NAMESPACE_URI, "Envelope", "esri");

  QName ENVELOPE_N = new QName(_NAMESPACE_URI, "EnvelopeN", "esri");

  String ENVELOPE_N_TYPE = "esri:EnvelopeN";

  QName EXTCLSID = new QName(_NAMESPACE_URI, "EXTCLSID", "esri");

  QName EXTENSION_PROPERTIES = new QName(_NAMESPACE_URI, "ExtensionProperties",
    "esri");

  QName EXTENT = new QName(_NAMESPACE_URI, "Extent", "esri");

  QName FEATURE_TYPE = new QName(_NAMESPACE_URI, "FeatureType", "esri");

  String FEATURE_TYPE_SIMPLE = "esriFTSimple";

  QName FIELD = new QName(_NAMESPACE_URI, "Field", "esri");

  QName FIELD_ARRAY = new QName(_NAMESPACE_URI, "FieldArray", "esri");

  String FIELD_ARRAY_TYPE = "esri:ArrayOfField";

  QName FIELD_INFOS = new QName(_NAMESPACE_URI, "FieldInfos", "esri");

  QName FIELD_NAME = new QName(_NAMESPACE_URI, "FieldName", "esri");

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

  QName FIELDS = new QName(_NAMESPACE_URI, "Fields", "esri");

  String FIELDS_TYPE = "esri:Fields";

  String FILE_EXTENSION = "gdbx";

  String FORMAT_DESCRIPTION = "ESRI Geodatabase (XML)";

  QName FULL_PROPS_RETRIEVED = new QName(_NAMESPACE_URI, "FullPropsRetrieved",
    "esri");

  QName GEOGRAPHIC_COORDINATE_SYSTEM = new QName(_NAMESPACE_URI,
    "GeographicCoordinateSystem", "esri");

  String GEOGRAPHIC_COORDINATE_SYSTEM_TYPE = "esri:GeographicCoordinateSystem";

  QName GEOMETRY_DEF = new QName(_NAMESPACE_URI, "GeometryDef", "esri");

  String GEOMETRY_DEF_TYPE = "esri:GeometryDef";

  QName GEOMETRY_TYPE = new QName(_NAMESPACE_URI, "GeometryType", "esri");

  String GEOMETRY_TYPE_MULTI_PATCH = "esriGeometryMultiPatch";

  String GEOMETRY_TYPE_MULTI_POINT = "esriGeometryMultipoint";

  String GEOMETRY_TYPE_POINT = "esriGeometryPoint";

  String GEOMETRY_TYPE_POLYGON = "esriGeometryPolygon";

  String GEOMETRY_TYPE_POLYLINE = "esriGeometryPolyline";

  QName GLOBAL_ID_FIELD_NAME = new QName(_NAMESPACE_URI, "GlobalIDFieldName",
    "esri");

  QName GRID_SIZE_0 = new QName(_NAMESPACE_URI, "GridSize0", "esri");

  QName GRID_SIZE_1 = new QName(_NAMESPACE_URI, "GridSize1", "esri");

  QName GRID_SIZE_2 = new QName(_NAMESPACE_URI, "GridSize2", "esri");

  QName HAS_GLOBAL_ID = new QName(_NAMESPACE_URI, "HasGlobalID", "esri");

  QName HAS_ID = new QName(_NAMESPACE_URI, "HasID", "esri");

  QName HAS_M = new QName(_NAMESPACE_URI, "HasM", "esri");

  QName HAS_OID = new QName(_NAMESPACE_URI, "HasOID", "esri");

  QName HAS_SPATIAL_INDEX = new QName(_NAMESPACE_URI, "HasSpatialIndex", "esri");

  QName HAS_Z = new QName(_NAMESPACE_URI, "HasZ", "esri");

  QName HIGH_PRECISION = new QName(_NAMESPACE_URI, "HighPrecision", "esri");

  QName INDEX = new QName(_NAMESPACE_URI, "Index", "esri");

  QName INDEX_ARRAY = new QName(_NAMESPACE_URI, "IndexArray", "esri");

  String INDEX_ARRAY_TYPE = "esri:ArrayOfIndex";

  String INDEX_TYPE = "esri:Index";

  QName INDEXES = new QName(_NAMESPACE_URI, "Indexes", "esri");

  String INDEXES_TYPE = "esri:Indexes";

  QName IS_ASCENDING = new QName(_NAMESPACE_URI, "IsAscending", "esri");

  QName IS_NULLABLE = new QName(_NAMESPACE_URI, "IsNullable", "esri");

  QName IS_UNIQUE = new QName(_NAMESPACE_URI, "IsUnique", "esri");

  QName KEY = new QName(_NAMESPACE_URI, "Key", "esri");

  QName LENGTH = new QName(_NAMESPACE_URI, "Length", "esri");

  QName LENGTH_FIELD_NAME = new QName(_NAMESPACE_URI, "LengthFieldName", "esri");

  QName M = new QName(_NAMESPACE_URI, "M", "esri");

  QName M_MAX = new QName(_NAMESPACE_URI, "MMax", "esri");

  QName M_MIN = new QName(_NAMESPACE_URI, "MMin", "esri");

  QName M_ORIGIN = new QName(_NAMESPACE_URI, "MOrigin", "esri");

  QName M_SCALE = new QName(_NAMESPACE_URI, "MScale", "esri");

  QName M_TOLERANCE = new QName(_NAMESPACE_URI, "MTolerance", "esri");

  String MEDIA_TYPE = "text/xml";

  QName METADATA = new QName(_NAMESPACE_URI, "Metadata", "esri");

  QName METADATA_RETRIEVED = new QName(_NAMESPACE_URI, "MetadataRetrieved",
    "esri");

  QName MODEL_NAME = new QName(_NAMESPACE_URI, "ModelName", "esri");

  QName NAME = new QName(_NAMESPACE_URI, "Name", "esri");

  QName NAMES = new QName(_NAMESPACE_URI, "Names", "esri");

  String NAMES_TYPE = "esri:Names";

  String NAMESPACE_URI_93 = "http://www.esri.com/schemas/ArcGIS/9.3";

  QName OBJECT_ID_FIELD_NAME = new QName(_NAMESPACE_URI, "OIDFieldName", "esri");

  QName PATH = new QName(_NAMESPACE_URI, "Path", "esri");

  QName PATH_ARRAY = new QName(_NAMESPACE_URI, "PathArray", "esri");

  String PATH_ARRAY_TYPE = "esri:ArrayOfPath";

  String PATH_TYPE = "esri:Path";

  QName POINT = new QName(_NAMESPACE_URI, "Point", "esri");

  QName POINT_ARRAY = new QName(_NAMESPACE_URI, "PointArray", "esri");

  String POINT_ARRAY_TYPE = "esri:ArrayOfPoint";

  String POINT_N_TYPE = "esri:PointN";

  String POLYGON_N_TYPE = "esri:PolygonN";

  String POLYLINE_N_TYPE = "esri:PolylineN";

  QName PRECISION = new QName(_NAMESPACE_URI, "Precision", "esri");

  QName PROJECTED_COORDINATE_SYSTEM = new QName(_NAMESPACE_URI,
    "ProjectedCoordinateSystem", "esri");

  String PROJECTED_COORDINATE_SYSTEM_TYPE = "esri:ProjectedCoordinateSystem";

  QName PROPERTY_ARRAY = new QName(_NAMESPACE_URI, "PropertyArray", "esri");

  String PROPERTY_ARRAY_TYPE = "esri:ArrayOfPropertySetProperty";

  QName PROPERTY_SET = new QName(_NAMESPACE_URI, "PropertySet", "esri");

  QName PROPERTY_SET_PROPERTY = new QName(_NAMESPACE_URI,
    "PropertySetProperty", "esri");

  String PROPERTY_SET_TYPE = "esri:PropertySet";

  QName RASTER_FIELD_NAME = new QName(_NAMESPACE_URI, "RasterFieldName", "esri");

  QName RECORD = new QName(_NAMESPACE_URI, "Record", "esri");

  String RECORD_TYPE = "esri:Record";

  QName RECORDS = new QName(_NAMESPACE_URI, "Records", "esri");

  String RECORDS_TYPE = "esri:ArrayOfRecord";

  QName RELATIONSHIP_CLASS_NAMES = new QName(_NAMESPACE_URI,
    "RelationshipClassNames", "esri");

  QName REQUIRED = new QName(_NAMESPACE_URI, "Required", "esri");

  QName RING = new QName(_NAMESPACE_URI, "Ring", "esri");

  QName RING_ARRAY = new QName(_NAMESPACE_URI, "RingArray", "esri");

  String RING_ARRAY_TYPE = "esri:ArrayOfRing";

  String RING_TYPE = "esri:Ring";

  QName SCALE = new QName(_NAMESPACE_URI, "Scale", "esri");

  QName SHAPE_FIELD_NAME = new QName(_NAMESPACE_URI, "ShapeFieldName", "esri");

  QName SHAPE_TYPE = new QName(_NAMESPACE_URI, "ShapeType", "esri");

  QName SPATIAL_REFERENCE = new QName(_NAMESPACE_URI, "SpatialReference",
    "esri");

  QName SUBTYPE = new QName(_NAMESPACE_URI, "Subtype", "esri");

  QName SUBTYPE_CODE = new QName(_NAMESPACE_URI, "SubtypeCode", "esri");

  QName SUBTYPE_FIELD_INFO = new QName(_NAMESPACE_URI, "SubtypeFieldInfo",
    "esri");

  QName SUBTYPE_FIELD_NAME = new QName(_NAMESPACE_URI, "SubtypeFieldName",
    "esri");

  QName SUBTYPE_NAME = new QName(_NAMESPACE_URI, "SubtypeName", "esri");

  QName SUBTYPES = new QName(_NAMESPACE_URI, "Subtypes", "esri");

  QName TYPE = new QName(_NAMESPACE_URI, "Type", "esri");

  QName VALUE = new QName(_NAMESPACE_URI, "Value", "esri");

  QName VALUES = new QName(_NAMESPACE_URI, "Values", "esri");

  String VALUES_TYPE = "esri:ArrayOfValue";

  QName VERSION = new QName(_NAMESPACE_URI, "Version", "esri");

  QName VERSIONED = new QName(_NAMESPACE_URI, "Versioned", "esri");

  QName WKID = new QName(_NAMESPACE_URI, "WKID", "esri");

  QName WKT = new QName(_NAMESPACE_URI, "WKT", "esri");

  String WORKSPACE = "Workspace";

  QName WORKSPACE_DATA = new QName(_NAMESPACE_URI, "WorkspaceData", "esri");

  String WORKSPACE_DATA_TYPE = "esri:WorkspaceData";

  QName WORKSPACE_DEFINITION = new QName(_NAMESPACE_URI, "WorkspaceDefinition",
    "esri");

  String WORKSPACE_DEFINITION_TYPE = "esri:WorkspaceDefinition";

  QName WORKSPACE_TYPE = new QName(_NAMESPACE_URI, "WorkspaceType", "esri");

  QName X = new QName(_NAMESPACE_URI, "X", "esri");

  QName X_MAX = new QName(_NAMESPACE_URI, "XMax", "esri");

  QName X_MIN = new QName(_NAMESPACE_URI, "XMin", "esri");

  QName X_ORIGIN = new QName(_NAMESPACE_URI, "XOrigin", "esri");

  QName XML_DOC = new QName(_NAMESPACE_URI, "XmlDoc", "esri");

  QName XML_PROPERTY_SET = new QName(_NAMESPACE_URI, "XmlPropertySet", "esri");

  String XML_PROPERTY_SET_TYPE = "esri:XmlPropertySet";

  Collection<DataType> XML_SCHEMA_DATA_TYPES = Arrays.asList(DataTypes.ANY_URI,
    DataTypes.BASE64_BINARY, DataTypes.BOOLEAN, DataTypes.BYTE, DataTypes.DATE,
    DataTypes.DATE_TIME, DataTypes.DECIMAL, DataTypes.DOUBLE,
    DataTypes.DURATION, DataTypes.FLOAT, DataTypes.INT, DataTypes.INTEGER,
    DataTypes.LONG, DataTypes.QNAME, DataTypes.SHORT, DataTypes.STRING);

  QName XY_SCALE = new QName(_NAMESPACE_URI, "XYScale", "esri");

  QName XY_TOLERANCE = new QName(_NAMESPACE_URI, "XYTolerance", "esri");

  QName Y = new QName(_NAMESPACE_URI, "Y", "esri");

  QName Y_MAX = new QName(_NAMESPACE_URI, "YMax", "esri");

  QName Y_MIN = new QName(_NAMESPACE_URI, "YMin", "esri");

  QName Y_ORIGIN = new QName(_NAMESPACE_URI, "YOrigin", "esri");

  QName Z = new QName(_NAMESPACE_URI, "Z", "esri");

  QName Z_MAX = new QName(_NAMESPACE_URI, "ZMax", "esri");

  QName Z_MIN = new QName(_NAMESPACE_URI, "ZMin", "esri");

  QName Z_ORIGIN = new QName(_NAMESPACE_URI, "ZOrigin", "esri");

  QName Z_SCALE = new QName(_NAMESPACE_URI, "ZScale", "esri");

  QName Z_TOLERANCE = new QName(_NAMESPACE_URI, "ZTolerance", "esri");

}
