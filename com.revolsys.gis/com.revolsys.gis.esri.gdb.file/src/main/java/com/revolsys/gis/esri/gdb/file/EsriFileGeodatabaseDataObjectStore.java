package com.revolsys.gis.esri.gdb.file;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.xml.namespace.QName;

import com.revolsys.gis.data.io.AbstractDataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.io.IteratorReader;
import com.revolsys.gis.data.io.Reader;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.esri.gdb.file.swig.EsriFileGdb;
import com.revolsys.gis.esri.gdb.file.swig.FieldInfo;
import com.revolsys.gis.esri.gdb.file.swig.FieldType;
import com.revolsys.gis.esri.gdb.file.swig.Geodatabase;
import com.revolsys.gis.esri.gdb.file.swig.Table;
import com.revolsys.gis.esri.gdb.file.swig.VectorOfWString;
import com.revolsys.gis.esri.gdb.file.type.BinaryAttribute;
import com.revolsys.gis.esri.gdb.file.type.DateAttribute;
import com.revolsys.gis.esri.gdb.file.type.DoubleAttribute;
import com.revolsys.gis.esri.gdb.file.type.FloatAttribute;
import com.revolsys.gis.esri.gdb.file.type.GeometryAttribute;
import com.revolsys.gis.esri.gdb.file.type.GuidAttribute;
import com.revolsys.gis.esri.gdb.file.type.IntegerAttribute;
import com.revolsys.gis.esri.gdb.file.type.OidAttribute;
import com.revolsys.gis.esri.gdb.file.type.ShortAttribute;
import com.revolsys.gis.esri.gdb.file.type.StringAttribute;
import com.revolsys.gis.esri.gdb.file.type.XmlAttribute;
import com.revolsys.gis.esri.gdb.xml.parser.DataElement;
import com.revolsys.gis.esri.gdb.xml.parser.EsriGdbXmlParser;
import com.revolsys.io.Writer;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class EsriFileGeodatabaseDataObjectStore extends AbstractDataObjectStore {

  private Geodatabase geodatabase;

  private String fileName;

  public EsriFileGeodatabaseDataObjectStore(final String fileName) {
    this.fileName = fileName;
  }

  public Writer<DataObject> createWriter() {
    // TODO Auto-generated method stub
    return null;
  }

  @PreDestroy
  public void destory() {
    EsriFileGeodatabaseUtil.check(EsriFileGdb.CloseGeodatabase(geodatabase));
    geodatabase.delete();
    geodatabase = null;
  }

  public String getFileName() {
    return fileName;
  }

  @PostConstruct
  public void initialize() {
    System.loadLibrary("EsriFileGdb");
    geodatabase = new Geodatabase();
    EsriFileGeodatabaseUtil.check(EsriFileGdb.OpenGeodatabase(fileName,
      geodatabase));
  }

  @Override
  protected void loadSchemaDataObjectMetaData(
    final DataObjectStoreSchema schema,
    final Map<QName, DataObjectMetaData> metaDataMap) {
    final String schemaName = schema.getName();
    String path = "\\" + schemaName;
    loadSchemaDataObjectMetaData(metaDataMap, schemaName, path, "Feature Class");
    loadSchemaDataObjectMetaData(metaDataMap, schemaName, path, "Table");
  }

  public void loadSchemaDataObjectMetaData(
    final Map<QName, DataObjectMetaData> metaDataMap, final String schemaName,
    String path, final String datasetType) {
    final VectorOfWString childFeatureClasses = new VectorOfWString();
    try {
      geodatabase.GetChildDatasets(path, datasetType, childFeatureClasses);
      for (int i = 0; i < childFeatureClasses.size(); i++) {
        final String childPath = childFeatureClasses.get(i);
        final String tableName = childPath.substring(schemaName.length() + 2);
        final QName typeName = new QName(schemaName, tableName);
        final DataObjectMetaDataImpl metaData = new DataObjectMetaDataImpl(
          typeName);
        Table table = geodatabase.openTable(childPath);
         try {
           final EsriGdbXmlParser parser = new EsriGdbXmlParser();
          final String tableDefinition = table.getDefinition();
          System.out.println(tableDefinition);
           final DataElement dataElement = parser.process(tableDefinition);
           System.out.println(dataElement.getSpatialReference().getGeometryFactory());
          FieldInfo fieldInfo = new FieldInfo();
          table.GetFieldInformation(fieldInfo);
          for (int j = 0; j < fieldInfo.getFieldCount(); j++) {
            String fieldName = fieldInfo.getFieldName(j);
            FieldType fieldType = fieldInfo.getFieldType(j);
            int length = fieldInfo.getFieldLength(j);
            boolean required = !fieldInfo.isNullable(j);
            Attribute attribute = null;
            switch (fieldType) {
              case fieldTypeBlob:
                attribute = new BinaryAttribute(fieldName, length, required);
              break;
              case fieldTypeDate:
                attribute = new DateAttribute(fieldName, length, required);
              break;
              case fieldTypeSingle:
                attribute = new FloatAttribute(fieldName, length, required);
              break;
              case fieldTypeDouble:
                attribute = new DoubleAttribute(fieldName, length, required);
              break;
              case fieldTypeGeometry:
                attribute = new GeometryAttribute(fieldName, length, required);
              break;
              case fieldTypeInteger:
                attribute = new IntegerAttribute(fieldName, length, required);
              break;
              case fieldTypeSmallInteger:
                attribute = new ShortAttribute(fieldName, length, required);
              break;
              case fieldTypeString:
                attribute = new StringAttribute(fieldName, length, required);
              break;
              case fieldTypeXML:
                attribute = new XmlAttribute(fieldName, length, required);
              break;
              case fieldTypeOID:
                attribute = new OidAttribute(fieldName, length, required);
              break;
              case fieldTypeGUID:
                attribute = new GuidAttribute(fieldName, length, required);
              break;

              default:
                System.err.println(fieldName + ":" + fieldType);
              break;
            }
            metaData.addAttribute(attribute);
          }
        } finally {
          geodatabase.CloseTable(table);
        }
        metaDataMap.put(typeName, metaData);
      }
    } finally {
      childFeatureClasses.delete();
    }
  }

  @Override
  protected void loadSchemas(final Map<String, DataObjectStoreSchema> schemaMap) {
    schemaMap.put("", new DataObjectStoreSchema(this, ""));

    addChildSchema(schemaMap, "\\");
  }

  public void addChildSchema(
    final Map<String, DataObjectStoreSchema> schemaMap, final String path) {
    final VectorOfWString childDatasets = new VectorOfWString();
    try {
      geodatabase.GetChildDatasets(path, "Feature Dataset", childDatasets);
      for (int i = 0; i < childDatasets.size(); i++) {
        final String childPath = childDatasets.get(i);
        final String schemaName = childPath.substring(1);
        final DataObjectStoreSchema schema = new DataObjectStoreSchema(this,
          schemaName);
        schemaMap.put(schemaName, schema);
        addChildSchema(schemaMap, childPath);
      }
    } finally {
      childDatasets.delete();
    }
  }

  private Table getTable(QName typeName) {
    String path = "\\\\" + typeName.getNamespaceURI() + "\\"
      + typeName.getLocalPart();
    return geodatabase.openTable(path);
  }

  public Reader<DataObject> query(final QName typeName) {
    Table table = getTable(typeName);
    DataObjectMetaData metaData = getMetaData(typeName);
    final EsriFileGeodatabaseQueryIterator iterator = new EsriFileGeodatabaseQueryIterator(
      metaData, geodatabase, table);
    final IteratorReader<DataObject> reader = new IteratorReader<DataObject>(
      iterator);
    return reader;
  }

  public Reader<DataObject> query(final QName typeName, final Envelope envelope) {
    DataObjectMetaData metaData = getMetaData(typeName);
    Table table = getTable(typeName);
    final EsriFileGeodatabaseQueryIterator iterator = new EsriFileGeodatabaseQueryIterator(
      metaData, geodatabase, table, envelope);
    final IteratorReader<DataObject> reader = new IteratorReader<DataObject>(
      iterator);
    return reader;
  }

  public Reader<DataObject> query(final QName typeName, final Geometry geometry) {
    final Envelope envelope = geometry.getEnvelopeInternal();
    return query(typeName, envelope);
  }

  private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\?");

  public Reader<DataObject> query(final QName typeName, final String where,
    final Object... arguments) {
    DataObjectMetaData metaData = getMetaData(typeName);
    Table table = getTable(typeName);
    StringBuffer whereClause = new StringBuffer();
    if (arguments.length == 0) {
      if (where.indexOf('?') > -1) {
        throw new IllegalArgumentException(
          "No arguments specified for a where clause with placeholders: "
            + where);
      } else {
        whereClause.append(where);
      }
    } else {
      Matcher matcher = PLACEHOLDER_PATTERN.matcher(where);
      int i = 0;
      while (matcher.find()) {
        if (i >= arguments.length) {
          throw new IllegalArgumentException(
            "Not enough arguments for where clause with placeholders: " + where);
        }
        final Object argument = arguments[i];
        matcher.appendReplacement(whereClause, "");
        whereClause.append(argument);
        i++;
      }
      matcher.appendTail(whereClause);
    }

    final EsriFileGeodatabaseQueryIterator iterator = new EsriFileGeodatabaseQueryIterator(
      metaData, geodatabase, table, whereClause.toString());
    final IteratorReader<DataObject> reader = new IteratorReader<DataObject>(
      iterator);
    return reader;
  }

  public void setFileName(final String fileName) {
    this.fileName = fileName;
  }

}
