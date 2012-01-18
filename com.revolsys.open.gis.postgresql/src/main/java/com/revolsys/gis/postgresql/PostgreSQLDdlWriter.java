package com.revolsys.gis.postgresql;

import java.io.PrintWriter;
import java.util.List;

import javax.xml.namespace.QName;

import org.springframework.util.StringUtils;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.ShortNameProperty;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.jdbc.io.JdbcDdlWriter;
import com.vividsolutions.jts.geom.Geometry;

public class PostgreSQLDdlWriter extends JdbcDdlWriter {
  public PostgreSQLDdlWriter() {
  }

  public PostgreSQLDdlWriter(PrintWriter out) {
    super(out);
  }

  public void writeCreateSchema(final String schemaName) {
    PrintWriter out = getOut();
    out.print("CREATE SCHEMA ");
    out.print(schemaName);
    out.println(";");
  }

  public void writeColumnDataType(final Attribute attribute) {
    PrintWriter out = getOut();
    final DataType dataType = attribute.getType();
    if (dataType == DataTypes.BOOLEAN) {
      out.print("boolean");
    } else if (dataType == DataTypes.BYTE) {
      out.print("NUMBER(3)");
    } else if (dataType == DataTypes.SHORT) {
      out.print("smallint");
    } else if (dataType == DataTypes.INT) {
      out.print("integer");
    } else if (dataType == DataTypes.LONG) {
      out.print("bigint");
    } else if (dataType == DataTypes.FLOAT) {
      out.print("real");
    } else if (dataType == DataTypes.DOUBLE) {
      out.print("double precision");
    } else if (dataType == DataTypes.DATE) {
      out.print("date");
    } else if (dataType == DataTypes.DATE_TIME) {
      out.print("timestamp");
    } else if (dataType == DataTypes.INTEGER) {
      out.print("NUMERIC(");
      out.print(attribute.getLength());
      out.print(')');
    } else if (dataType == DataTypes.DECIMAL) {
      out.print("NUMERIC(");
      out.print(attribute.getLength());
      int scale = attribute.getScale();
      if (scale >= 0) {
        out.print(',');
        out.print(scale);
      }
      out.print(')');
    } else if (dataType == DataTypes.STRING) {
      out.print("varchar(");
      out.print(attribute.getLength());
      out.print(")");
    } else if (Geometry.class.isAssignableFrom(dataType.getJavaClass())) {
      out.print("geometry");
    } else {
      throw new IllegalArgumentException("Unknown data type " + dataType);
    }
  }

  public String getSequenceName(final DataObjectMetaData metaData) {
    final QName typeName = metaData.getName();
    final String schema = typeName.getNamespaceURI().toLowerCase();
    ShortNameProperty shortNameProperty = ShortNameProperty.getProperty(metaData);
    String shortName = null;
    if (shortNameProperty != null) {
      shortName = shortNameProperty.getShortName();
    }
    if (StringUtils.hasText(shortName) && shortNameProperty.isUseForSequence()) {
      final String sequenceName = schema + "." + shortName.toLowerCase()
        + "_seq";
      return sequenceName;
    } else {
      final String tableName = typeName.getLocalPart().toLowerCase();
      final String idAttributeName = metaData.getIdAttributeName()
        .toLowerCase();
      return schema + "." + tableName + "_" + idAttributeName + "_seq";
    }
  }

  public void writeResetSequence(
    DataObjectMetaData metaData,
    List<DataObject> values) {
    PrintWriter out = getOut();
    Long nextValue = 0L;
    for (DataObject object : values) {
      Object id = object.getIdValue();
      if (id instanceof Number) {
        Number number = (Number)id;
        final long longValue = number.longValue();
        if (longValue > nextValue) {
          nextValue = longValue;
        }
      }
    }
    nextValue++;
    String sequeneName = getSequenceName(metaData);
    out.print("ALTER SEQUENCE ");
    out.print(sequeneName);
    out.print(" RESTART WITH ");
    out.print(nextValue);
    out.println(";");
  }

  public void writeGeometryMetaData(final DataObjectMetaData metaData) {
    PrintWriter out = getOut();
    QName typeName = metaData.getName();
    String schemaName = typeName.getNamespaceURI();
    if (schemaName.length() == 0) {
      schemaName = "public";
    }
    final String tableName = typeName.getLocalPart();
    final Attribute geometryAttribute = metaData.getGeometryAttribute();
    if (geometryAttribute != null) {
      final GeometryFactory geometryFactory = geometryAttribute.getProperty(AttributeProperties.GEOMETRY_FACTORY);
      final String name = geometryAttribute.getName();
      String geometryType = "GEOMETRY";
      final DataType dataType = geometryAttribute.getType();
      if (dataType == DataTypes.POINT) {
        geometryType = "POINT";
      } else if (dataType == DataTypes.LINE_STRING) {
        geometryType = "LINESTRING";
      } else if (dataType == DataTypes.POLYGON) {
        geometryType = "POLYGON";
      } else if (dataType == DataTypes.MULTI_POINT) {
        geometryType = "MULTIPOINT";
      } else if (dataType == DataTypes.MULTI_LINE_STRING) {
        geometryType = "MULTILINESTRING";
      } else if (dataType == DataTypes.MULTI_POLYGON) {
        geometryType = "MULTIPOLYGON";
      }
      out.print("INSERT INTO geometry_columns(f_table_catalog, f_table_schema, f_table_name, f_geometry_column, coord_dimension, srid, \"type\") VALUES ('','");
      out.print(schemaName.toLowerCase());
      out.print("', '");
      out.print(tableName.toLowerCase());
      out.print("','");
      out.print(name.toLowerCase());
      out.print("', ");
      out.print(geometryFactory.getNumAxis());
      out.print(",");
      final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
      out.print(coordinateSystem.getId());
      out.print(",'");
      out.print(geometryType);
      out.println("');");
    }
  }

  public void writeAddGeometryColumn(final DataObjectMetaData metaData) {
    PrintWriter out = getOut();
    QName typeName = metaData.getName();
    String schemaName = typeName.getNamespaceURI();
    if (schemaName.length() == 0) {
      schemaName = "public";
    }
    final String tableName = typeName.getLocalPart();
    final Attribute geometryAttribute = metaData.getGeometryAttribute();
    if (geometryAttribute != null) {
      final GeometryFactory geometryFactory = geometryAttribute.getProperty(AttributeProperties.GEOMETRY_FACTORY);
      final String name = geometryAttribute.getName();
      String geometryType = "GEOMETRY";
      final DataType dataType = geometryAttribute.getType();
      if (dataType == DataTypes.POINT) {
        geometryType = "POINT";
      } else if (dataType == DataTypes.LINE_STRING) {
        geometryType = "LINESTRING";
      } else if (dataType == DataTypes.POLYGON) {
        geometryType = "POLYGON";
      } else if (dataType == DataTypes.MULTI_POINT) {
        geometryType = "MULTIPOINT";
      } else if (dataType == DataTypes.MULTI_LINE_STRING) {
        geometryType = "MULTILINESTRING";
      } else if (dataType == DataTypes.MULTI_POLYGON) {
        geometryType = "MULTIPOLYGON";
      }
      out.print("select addgeometrycolumn('");
      out.print(schemaName.toLowerCase());
      out.print("', '");
      out.print(tableName.toLowerCase());
      out.print("','");
      out.print(name.toLowerCase());
      out.print("',");
      final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
      out.print(coordinateSystem.getId());
      out.print(",'");
      out.print(geometryType);
      out.print("', ");
      out.print(geometryFactory.getNumAxis());
      out.println(");");

    }
  }

  public void writeAlterTableOwner(final QName typeName, final String owner) {
    PrintWriter out = getOut();
    out.print("ALTER ");
    final String objectType = "TABLE";
    out.print(objectType);
    out.print(" ");
    writeTableName(typeName);
    out.print(" OWNER TO ");
    out.print(owner);
    out.println(";");
  }

  public void writeAlterOwner(
    final String objectType,
    final String objectName,
    final String owner) {
    PrintWriter out = getOut();
    out.print("ALTER ");
    out.print(objectType);
    out.print(" ");
    out.print(objectName);
    out.print(" OWNER TO ");
    out.print(owner);
    out.println(";");
  }
}
