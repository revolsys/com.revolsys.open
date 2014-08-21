package com.revolsys.gis.postgresql;

import java.io.PrintWriter;
import java.util.List;

import com.revolsys.data.identifier.Identifier;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.property.AttributeProperties;
import com.revolsys.data.record.property.ShortNameProperty;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.io.Path;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jdbc.io.JdbcDdlWriter;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.util.Property;

public class PostgreSQLDdlWriter extends JdbcDdlWriter {
  public PostgreSQLDdlWriter() {
  }

  public PostgreSQLDdlWriter(final PrintWriter out) {
    super(out);
  }

  @Override
  public String getSequenceName(final RecordDefinition recordDefinition) {
    final String typePath = recordDefinition.getPath();
    final String schema = JdbcUtils.getSchemaName(typePath);
    final ShortNameProperty shortNameProperty = ShortNameProperty.getProperty(recordDefinition);
    String shortName = null;
    if (shortNameProperty != null) {
      shortName = shortNameProperty.getShortName();
    }
    if (Property.hasValue(shortName) && shortNameProperty.isUseForSequence()) {
      final String sequenceName = schema + "." + shortName.toLowerCase()
          + "_seq";
      return sequenceName;
    } else {
      final String tableName = Path.getName(typePath).toLowerCase();
      final String idAttributeName = recordDefinition.getIdAttributeName()
          .toLowerCase();
      return schema + "." + tableName + "_" + idAttributeName + "_seq";
    }
  }

  public void writeAddGeometryColumn(final RecordDefinition recordDefinition) {
    final PrintWriter out = getOut();
    final String typePath = recordDefinition.getPath();
    String schemaName = JdbcUtils.getSchemaName(typePath);
    if (schemaName.length() == 0) {
      schemaName = "public";
    }
    final String tableName = Path.getName(typePath);
    final Attribute geometryAttribute = recordDefinition.getGeometryAttribute();
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
      out.print(geometryFactory.getAxisCount());
      out.println(");");

    }
  }

  public void writeAlterOwner(final String objectType, final String objectName,
    final String owner) {
    final PrintWriter out = getOut();
    out.print("ALTER ");
    out.print(objectType);
    out.print(" ");
    out.print(objectName);
    out.print(" OWNER TO ");
    out.print(owner);
    out.println(";");
  }

  public void writeAlterTableOwner(final String typePath, final String owner) {
    final PrintWriter out = getOut();
    out.print("ALTER ");
    final String objectType = "TABLE";
    out.print(objectType);
    out.print(" ");
    writeTableName(typePath);
    out.print(" OWNER TO ");
    out.print(owner);
    out.println(";");
  }

  @Override
  public void writeColumnDataType(final Attribute attribute) {
    final PrintWriter out = getOut();
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
      final int scale = attribute.getScale();
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

  @Override
  public void writeCreateSchema(final String schemaName) {
    final PrintWriter out = getOut();
    out.print("CREATE SCHEMA ");
    out.print(schemaName);
    out.println(";");
  }

  @Override
  public void writeGeometryRecordDefinition(final RecordDefinition recordDefinition) {
    final PrintWriter out = getOut();
    final String typePath = recordDefinition.getPath();
    String schemaName = JdbcUtils.getSchemaName(typePath);
    if (schemaName.length() == 0) {
      schemaName = "public";
    }
    final String tableName = Path.getName(typePath);
    final Attribute geometryAttribute = recordDefinition.getGeometryAttribute();
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
      out.print(geometryFactory.getAxisCount());
      out.print(",");
      final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
      out.print(coordinateSystem.getId());
      out.print(",'");
      out.print(geometryType);
      out.println("');");
    }
  }

  @Override
  public void writeResetSequence(final RecordDefinition recordDefinition,
    final List<Record> values) {
    final PrintWriter out = getOut();
    Long nextValue = 0L;
    for (final Record object : values) {
      final Identifier id = object.getIdentifier();
      for (final Object value : id.getValues()) {
        if (value instanceof Number) {
          final Number number = (Number)value;
          final long longValue = number.longValue();
          if (longValue > nextValue) {
            nextValue = longValue;
          }
        }
      }
    }
    nextValue++;
    final String sequeneName = getSequenceName(recordDefinition);
    out.print("ALTER SEQUENCE ");
    out.print(sequeneName);
    out.print(" RESTART WITH ");
    out.print(nextValue);
    out.println(";");
  }
}
