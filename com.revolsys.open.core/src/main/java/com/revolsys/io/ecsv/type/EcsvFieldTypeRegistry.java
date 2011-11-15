package com.revolsys.io.ecsv.type;

import java.util.HashMap;
import java.util.Map;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.io.ecsv.EcsvConstants;

public class EcsvFieldTypeRegistry implements EcsvConstants {

  public static final EcsvFieldTypeRegistry INSTANCE = new EcsvFieldTypeRegistry();

  private GeometryFactory geometryFactory;

  private final Map<DataType, EcsvFieldType> typeMapping = new HashMap<DataType, EcsvFieldType>();

  public EcsvFieldTypeRegistry() {
    this(null);
  }

  public EcsvFieldTypeRegistry(final GeometryFactory geometryFactory) {
    if (geometryFactory == null) {
      this.geometryFactory = new GeometryFactory();
    } else {
      this.geometryFactory = geometryFactory;
    }
    addFieldType(new UriFieldType());
    addFieldType(new Base64BinaryFieldType());
    addFieldType(new BooleanFieldType());
    addFieldType(new ByteFieldType());
    addFieldType(new DateFieldType(DataTypes.DATE, "yyyy-MM-dd"));
    addFieldType(new DateFieldType(DataTypes.DATE_TIME, "yyyy-MM-dd'T'HH:mm:ss"));
    addFieldType(new DecimalFieldType());
    addFieldType(new DoubleFieldType());
    addFieldType(new FloatFieldType());
    addFieldType(new IntFieldType());
    addFieldType(new IntegerFieldType());
    addFieldType(new LongFieldType());
    addFieldType(new QNameFieldType());
    addFieldType(new ShortFieldType());
    addFieldType(new StringFieldType());
    addFieldType(new CollectionFieldType(DataTypes.COLLECTION));
    addFieldType(new CollectionFieldType(DataTypes.LIST));
    addFieldType(new CollectionFieldType(DataTypes.SET));
    addFieldType(new EcsvGeometryFieldType(DataTypes.GEOMETRY,
      this.geometryFactory));
    addFieldType(new EcsvGeometryFieldType(DataTypes.POINT,
      this.geometryFactory));
    addFieldType(new EcsvGeometryFieldType(DataTypes.LINE_STRING,
      this.geometryFactory));
    addFieldType(new EcsvGeometryFieldType(DataTypes.POLYGON,
      this.geometryFactory));
    addFieldType(new EcsvGeometryFieldType(DataTypes.MULTI_POINT,
      this.geometryFactory));
    addFieldType(new EcsvGeometryFieldType(DataTypes.MULTI_LINE_STRING,
      this.geometryFactory));
    addFieldType(new EcsvGeometryFieldType(DataTypes.MULTI_POLYGON,
      this.geometryFactory));
    addFieldType(new GeometryFactoryFieldType());
  }

  public void addFieldType(final DataType dataType,
    final EcsvFieldType fieldType) {
    typeMapping.put(dataType, fieldType);
  }

  public void addFieldType(final EcsvFieldType fieldType) {
    final DataType dataType = fieldType.getDataType();
    addFieldType(dataType, fieldType);
  }

  public EcsvFieldType getFieldType(final DataType dataType) {
    final EcsvFieldType fieldType = typeMapping.get(dataType);
    if (fieldType == null) {
      return new StringFieldType();
    }
    return fieldType;
  }
}
