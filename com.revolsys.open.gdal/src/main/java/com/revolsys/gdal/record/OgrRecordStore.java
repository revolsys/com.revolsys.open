package com.revolsys.gdal.record;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PreDestroy;

import org.gdal.ogr.DataSource;
import org.gdal.ogr.FeatureDefn;
import org.gdal.ogr.FieldDefn;
import org.gdal.ogr.GeomFieldDefn;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;
import org.gdal.osr.SpatialReference;
import org.slf4j.LoggerFactory;

import com.revolsys.data.query.Query;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.property.AttributeProperties;
import com.revolsys.data.record.schema.AbstractRecordStore;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.data.record.schema.RecordStoreSchema;
import com.revolsys.data.record.schema.RecordStoreSchemaElement;
import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.gdal.Gdal;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Path;
import com.revolsys.io.Writer;
import com.revolsys.jts.geom.GeometryFactory;

public class OgrRecordStore extends AbstractRecordStore {
  static {
    Gdal.init();
  }

  private final File file;

  private DataSource dataSource;

  private boolean closed;

  private final Map<String, String> layerNameToPathMap = new HashMap<>();

  private final Map<String, String> pathToLayerNameMap = new HashMap<>();

  public OgrRecordStore(final File file) {
    this.file = file;
  }

  @Override
  @PreDestroy
  public void close() {
    // TODO if (!FileGdbRecordStoreFactory.release(this.fileName)) {
    doClose();
    // }
  }

  protected RecordDefinitionImpl createRecordDefinition(
    final RecordStoreSchema schema, final Layer layer) {
    final String layerName = layer.GetName();
    final String typePath = Path.clean(layerName);

    final RecordDefinitionImpl recordDefinition = new RecordDefinitionImpl(
      schema, typePath);
    final FeatureDefn layerDefinition = layer.GetLayerDefn();

    for (int fieldIndex = 0; fieldIndex < layerDefinition.GetFieldCount(); fieldIndex++) {
      final FieldDefn fieldDefinition = layerDefinition.GetFieldDefn(fieldIndex);
      final String fieldName = fieldDefinition.GetName();
      final String fieldTypeName = fieldDefinition.GetFieldTypeName(fieldDefinition.GetFieldType());
      final int fieldWidth = fieldDefinition.GetWidth();
      final int fieldPrecision = fieldDefinition.GetPrecision();
      DataType fieldDataType = DataTypes.STRING;
      if ("String".equals(fieldTypeName)) {
        fieldDataType = DataTypes.STRING;
      } else if ("Integer".equals(fieldTypeName)) {
        fieldDataType = DataTypes.INT;
      } else if ("Real".equals(fieldTypeName)) {
        fieldDataType = DataTypes.DOUBLE;
      } else if ("Date".equals(fieldTypeName)) {
        fieldDataType = DataTypes.DATE;
      } else if ("DateTime".equals(fieldTypeName)) {
        fieldDataType = DataTypes.DATE_TIME;
      } else {
        LoggerFactory.getLogger(getClass()).error(
          "Unsupported field type " + this.file + " " + fieldName + ": "
              + fieldTypeName);
      }
      final Attribute field = new Attribute(fieldName, fieldDataType,
        fieldWidth, fieldPrecision, false);
      recordDefinition.addAttribute(field);
    }
    for (int fieldIndex = 0; fieldIndex < layerDefinition.GetGeomFieldCount(); fieldIndex++) {
      final GeomFieldDefn fieldDefinition = layerDefinition.GetGeomFieldDefn(fieldIndex);
      final String fieldName = fieldDefinition.GetName();
      final int fieldType = fieldDefinition.GetFieldType();
      DataType fieldDataType;
      int axisCount = 2;
      switch (fieldType) {
        case 1:
          fieldDataType = DataTypes.POINT;
          break;
        case 2:
          fieldDataType = DataTypes.LINE_STRING;
          break;
        case 3:
          fieldDataType = DataTypes.POLYGON;
          break;
        case 4:
          fieldDataType = DataTypes.MULTI_POINT;
          break;
        case 5:
          fieldDataType = DataTypes.MULTI_LINE_STRING;
          break;
        case 6:
          fieldDataType = DataTypes.MULTI_POLYGON;
          break;
        case 7:
          fieldDataType = DataTypes.GEOMETRY_COLLECTION;
          break;
        case 101:
          fieldDataType = DataTypes.LINEAR_RING;
          break;
        case 0x80000000 + 1:
          fieldDataType = DataTypes.POINT;
        axisCount = 3;
        break;
        case 0x80000000 + 2:
          fieldDataType = DataTypes.LINE_STRING;
        axisCount = 3;
        break;
        case 0x80000000 + 3:
          fieldDataType = DataTypes.POLYGON;
        axisCount = 3;
        break;
        case 0x80000000 + 4:
          fieldDataType = DataTypes.MULTI_POINT;
        axisCount = 3;
        break;
        case 0x80000000 + 5:
          fieldDataType = DataTypes.MULTI_LINE_STRING;
        axisCount = 3;
        break;
        case 0x80000000 + 6:
          fieldDataType = DataTypes.MULTI_POLYGON;
        axisCount = 3;
        break;
        case 0x80000000 + 7:
          fieldDataType = DataTypes.GEOMETRY_COLLECTION;
        axisCount = 3;
        break;

        default:
          fieldDataType = DataTypes.GEOMETRY;
          break;
      }
      final SpatialReference spatialReference = fieldDefinition.GetSpatialRef();
      final CoordinateSystem coordinateSystem = Gdal.getCoordinateSystem(spatialReference);
      final GeometryFactory geometryFactory = GeometryFactory.floating(
        coordinateSystem, axisCount);
      final Attribute field = new Attribute(fieldName, fieldDataType, false);
      field.setProperty(AttributeProperties.GEOMETRY_FACTORY, geometryFactory);
      recordDefinition.addAttribute(field);
    }
    return recordDefinition;
  }

  @Override
  public Writer<Record> createWriter() {
    // TODO Auto-generated method stub
    return null;
  }

  public void doClose() {
    synchronized (this) {

      if (!isClosed()) {
        if (this.dataSource != null) {
          try {
            this.dataSource.delete();
          } finally {
            this.dataSource = null;
            this.closed = true;
            super.close();
          }
        }
      }
    }
  }

  private DataSource getDataSource() {
    if (isClosed()) {
      return null;
    } else {
      if (this.dataSource == null) {
        this.dataSource = ogr.Open(FileUtil.getCanonicalPath(this.file), true);
      }
      return this.dataSource;
    }
  }

  @Override
  public int getRowCount(final Query query) {
    // TODO Auto-generated method stub
    return 0;
  }

  public boolean isClosed() {
    return this.closed;
  }

  @Override
  protected Map<String, ? extends RecordStoreSchemaElement> refreshSchemaElements(
    final RecordStoreSchema schema) {
    final Map<String, RecordStoreSchemaElement> elementsByPath = new TreeMap<>();
    if (!isClosed()) {
      final DataSource dataSource = getDataSource();
      if (dataSource != null) {
        for (int layerIndex = 0; layerIndex < dataSource.GetLayerCount(); layerIndex++) {
          final Layer layer = dataSource.GetLayer(layerIndex);
          final RecordDefinitionImpl recordDefinition = createRecordDefinition(
            schema, layer);
          final String typePath = recordDefinition.getPath();
          final String layerName = layer.GetName();
          this.layerNameToPathMap.put(layerName, typePath);
          this.pathToLayerNameMap.put(typePath, layerName);
          elementsByPath.put(typePath, recordDefinition);
        }
      }
    }
    return elementsByPath;
  }
}
