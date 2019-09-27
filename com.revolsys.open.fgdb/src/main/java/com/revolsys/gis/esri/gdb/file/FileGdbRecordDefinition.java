package com.revolsys.gis.esri.gdb.file;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;

import com.revolsys.geometry.model.ClockDirection;
import com.revolsys.gis.esri.gdb.file.capi.FileGdbDomainCodeTable;
import com.revolsys.gis.esri.gdb.file.capi.type.AbstractFileGdbFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.AreaFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.BinaryFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.DateFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.DoubleFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.FloatFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.GeometryFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.GlobalIdFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.GuidFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.IntegerFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.LengthFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.OidFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.ShortFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.StringFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.XmlFieldDefinition;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.io.format.esri.gdb.xml.EsriGeodatabaseXmlConstants;
import com.revolsys.record.io.format.esri.gdb.xml.model.DEFeatureClass;
import com.revolsys.record.io.format.esri.gdb.xml.model.DETable;
import com.revolsys.record.io.format.esri.gdb.xml.model.Domain;
import com.revolsys.record.io.format.esri.gdb.xml.model.EsriGdbXmlParser;
import com.revolsys.record.io.format.esri.gdb.xml.model.Field;
import com.revolsys.record.io.format.esri.gdb.xml.model.Index;
import com.revolsys.record.io.format.esri.gdb.xml.model.enums.FieldType;
import com.revolsys.record.io.format.xml.XmlProcessor;
import com.revolsys.record.property.LengthFieldName;
import com.revolsys.record.schema.RecordDefinitionImpl;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.util.JavaBeanUtil;

public class FileGdbRecordDefinition extends RecordDefinitionImpl {
  private static final Map<FieldType, Constructor<? extends AbstractFileGdbFieldDefinition>> ESRI_FIELD_TYPE_FIELD_DEFINITION_MAP = new HashMap<>();

  static {
    addFieldTypeConstructor(FieldType.esriFieldTypeInteger, IntegerFieldDefinition.class);
    addFieldTypeConstructor(FieldType.esriFieldTypeSmallInteger, ShortFieldDefinition.class);
    addFieldTypeConstructor(FieldType.esriFieldTypeDouble, DoubleFieldDefinition.class);
    addFieldTypeConstructor(FieldType.esriFieldTypeSingle, FloatFieldDefinition.class);
    addFieldTypeConstructor(FieldType.esriFieldTypeString, StringFieldDefinition.class);
    addFieldTypeConstructor(FieldType.esriFieldTypeDate, DateFieldDefinition.class);
    addFieldTypeConstructor(FieldType.esriFieldTypeGeometry, GeometryFieldDefinition.class);
    addFieldTypeConstructor(FieldType.esriFieldTypeOID, OidFieldDefinition.class);
    addFieldTypeConstructor(FieldType.esriFieldTypeBlob, BinaryFieldDefinition.class);
    addFieldTypeConstructor(FieldType.esriFieldTypeGlobalID, GlobalIdFieldDefinition.class);
    addFieldTypeConstructor(FieldType.esriFieldTypeGUID, GuidFieldDefinition.class);
    addFieldTypeConstructor(FieldType.esriFieldTypeXML, XmlFieldDefinition.class);
  }

  private static void addFieldTypeConstructor(final FieldType fieldType,
    final Class<? extends AbstractFileGdbFieldDefinition> fieldClass) {
    try {
      final Constructor<? extends AbstractFileGdbFieldDefinition> constructor = fieldClass
        .getConstructor(int.class, Field.class);
      ESRI_FIELD_TYPE_FIELD_DEFINITION_MAP.put(fieldType, constructor);
    } catch (final SecurityException e) {
      Logs.error(FileGdbRecordStore.class, "No public constructor for ESRI type " + fieldType, e);
    } catch (final NoSuchMethodException e) {
      Logs.error(FileGdbRecordStore.class, "No public constructor for ESRI type " + fieldType, e);
    }

  }

  private final DETable deTable;

  private final String catalogPath;

  private final RecordStore recordStore;

  private final TableReference tableReference;

  public FileGdbRecordDefinition(final FileGdbRecordStoreSchema schema,
    final String tableDefinition) {
    super(schema);
    this.recordStore = schema.getRecordStore();
    setPolygonRingDirection(ClockDirection.CLOCKWISE);
    final FileGdbRecordStore recordStore = getRecordStore();
    final XmlProcessor parser = new EsriGdbXmlParser();
    final DETable deTable = parser.process(tableDefinition);
    this.deTable = deTable;
    final String tableName = deTable.getName();
    final PathName schemaName = schema.getPathName();
    final PathName typePath = PathName.newPathName(schemaName.newChild(tableName));
    setPathName(typePath);
    String lengthFieldName = null;
    String areaFieldName = null;
    if (deTable instanceof DEFeatureClass) {
      final DEFeatureClass featureClass = (DEFeatureClass)deTable;

      lengthFieldName = featureClass.getLengthFieldName();
      final LengthFieldName lengthFieldNameProperty = new LengthFieldName(lengthFieldName);
      lengthFieldNameProperty.setRecordDefinition(this);

      areaFieldName = featureClass.getAreaFieldName();
      final LengthFieldName areaFieldNameProperty = new LengthFieldName(areaFieldName);
      areaFieldNameProperty.setRecordDefinition(this);

    }
    int fieldNumber = 0;
    for (final Field field : deTable.getFields()) {
      final String fieldName = field.getName();
      AbstractFileGdbFieldDefinition fieldDefinition = null;
      if (fieldName.equals(lengthFieldName)) {
        fieldDefinition = new LengthFieldDefinition(fieldNumber, field);
      } else if (fieldName.equals(areaFieldName)) {
        fieldDefinition = new AreaFieldDefinition(fieldNumber, field);
      } else {
        final FieldType type = field.getType();
        final Constructor<? extends AbstractFileGdbFieldDefinition> fieldConstructor = ESRI_FIELD_TYPE_FIELD_DEFINITION_MAP
          .get(type);
        if (fieldConstructor != null) {
          try {
            fieldDefinition = JavaBeanUtil.invokeConstructor(fieldConstructor, fieldNumber, field);
          } catch (final Throwable e) {
            Logs.error(this, tableDefinition);
            throw new RuntimeException("Error creating field for " + typePath + "."
              + field.getName() + " : " + field.getType(), e);
          }
        } else {
          Logs.error(this, "Unsupported field type " + fieldName + ":" + type);
        }
      }
      if (fieldDefinition != null) {
        final Domain domain = field.getDomain();
        if (domain != null) {
          CodeTable codeTable = recordStore.getCodeTable(domain.getDomainName() + "_ID");
          if (codeTable == null) {
            codeTable = new FileGdbDomainCodeTable(recordStore, domain);
            recordStore.addCodeTable(codeTable);
          }
          fieldDefinition.setCodeTable(codeTable);
        }
        fieldDefinition.setRecordStore(recordStore);
        addField(fieldDefinition);
        if (fieldDefinition instanceof GlobalIdFieldDefinition) {
          setIdFieldName(fieldName);
        }
      }
      fieldNumber++;
    }
    final String oidFieldName = deTable.getOIDFieldName();
    setProperty(EsriGeodatabaseXmlConstants.ESRI_OBJECT_ID_FIELD_NAME, oidFieldName);
    if (deTable instanceof DEFeatureClass) {
      final DEFeatureClass featureClass = (DEFeatureClass)deTable;
      final String shapeFieldName = featureClass.getShapeFieldName();
      setGeometryFieldName(shapeFieldName);
    }
    for (final Index index : deTable.getIndexes()) {
      if (index.getName().endsWith("_PK")) {
        for (final Field field : index.getFields()) {
          final String fieldName = field.getName();
          setIdFieldName(fieldName);
        }
      }
    }
    recordStore.addRecordDefinitionProperties(this);
    if (getIdFieldIndex() == -1) {
      setIdFieldName(deTable.getOIDFieldName());
    }
    this.catalogPath = deTable.getCatalogPath();
    this.tableReference = recordStore.getTableReference(typePath, this.catalogPath);
  }

  public String getCatalogPath() {
    return this.catalogPath;
  }

  @SuppressWarnings("unchecked")
  public <T extends DETable> T getDeTable() {
    return (T)this.deTable;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends RecordStore> V getRecordStore() {
    return (V)this.recordStore;
  }

  public TableWrapper lockTable(final boolean loadOnlyMode) {
    return this.tableReference.writeLock(loadOnlyMode);
  }

  TableReference getTableReference() {
    return this.tableReference;
  }

  @Override
  public boolean equalsRecordStore(final RecordStore recordStore) {
    return recordStore == this.recordStore;
  }
}
