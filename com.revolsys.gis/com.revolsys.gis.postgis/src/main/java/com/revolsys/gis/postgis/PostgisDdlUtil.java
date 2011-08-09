package com.revolsys.gis.postgis;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import javax.xml.namespace.QName;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.vividsolutions.jts.geom.Geometry;

public class PostgisDdlUtil {
  private static final NumberFormat FORMAT = new DecimalFormat(
    "#.#########################");

  public static void createTable(final PrintWriter out,
    final DataObjectMetaData metaData) {
    final QName typeName = metaData.getName();
    out.println();
    out.print("CREATE TABLE ");
    String schemaName = typeName.getNamespaceURI();
    if (schemaName.length() == 0) {
      schemaName = "public";
    }
    final String tableName = typeName.getLocalPart();
    writeTableName(out, schemaName, tableName);
    out.println(" (");
    for (int i = 0; i < metaData.getAttributeCount(); i++) {
      final Attribute attribute = metaData.getAttribute(i);
      final DataType dataType = attribute.getType();
        if (i > 0) {
          out.println(",");
        }
        final String name = attribute.getName();
        out.print("  ");
        out.print(name);
        for (int j = name.length(); j < 32; j++) {
          out.print(' ');
        }
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
        } else if (dataType == DataTypes.STRING) {
          out.print("varchar(");
          out.print(attribute.getLength());
          out.print(")");
        } else if (Geometry.class.isAssignableFrom(dataType.getJavaClass())) {
          out.print("geometry");
        } else {
          throw new IllegalArgumentException("Unknown data type" + dataType);
        }
        if (attribute.isRequired()) {
          out.print(" NOT NULL");
        }
    }
    out.println();
    out.println(");");

    writeGeometryMetaData(out, metaData);
    final String idAttributeName = metaData.getIdAttributeName();
    if (idAttributeName != null) {
      writeAddPrimaryKeyConstraint(out, schemaName, tableName, idAttributeName);
    }
  }
 
  public static void writeGeometryMetaData(final PrintWriter out,
    final DataObjectMetaData metaData) {
    QName typeName = metaData.getName();
    String schemaName = typeName.getNamespaceURI();
    if (schemaName.length() == 0) {
      schemaName = "public";
    }
    final String tableName = typeName.getLocalPart(); final Attribute geometryAttribute = metaData.getGeometryAttribute();
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
  public static void writeAddGeometryColumn(final PrintWriter out,
    final DataObjectMetaData metaData) {
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
  public static void writeAddPrimaryKeyConstraint(final PrintWriter out,
    final String schemaName, final String tableName, final String columnName) {
    out.print("ALTER TABLE ");
    writeTableName(out, schemaName, tableName);
    out.print(" ADD CONSTRAINT ");
    out.print(tableName);
    out.print("_PK PRIMARY KEY (");
    out.print(columnName);
    out.println(");");
  }

  public static void writeAlterTableOwner(final PrintWriter out,
    final QName typeName, final String owner) {
    out.print("ALTER ");
    final String objectType = "TABLE";
    out.print(objectType);
    out.print(" ");
    writeTableName(out, typeName);
    out.print(" OWNER TO ");
    out.print(owner);
    out.println(";");
  }

  public static void writeAlterOwner(final PrintWriter out,
    final String objectType, final String objectName, final String owner) {
    out.print("ALTER ");
    out.print(objectType);
    out.print(" ");
    out.print(objectName);
    out.print(" OWNER TO ");
    out.print(owner);
    out.println(";");
  }

  public static String writeCreateSequence(final PrintWriter out,
    final DataObjectMetaData metaData) {
    final QName typeName = metaData.getName();
    final String schema = typeName.getNamespaceURI().toLowerCase();
    final String tableName = typeName.getLocalPart().toLowerCase();
    final String idAttributeName = metaData.getIdAttributeName().toLowerCase();
    final String sequenceName = schema + "." + tableName + "_"
      + idAttributeName + "_seq";
    writeCreateSequence(out, sequenceName);
    return sequenceName;
  }

  public static void writeCreateSequence(final PrintWriter out,
    final String sequenceName) {
    out.print("CREATE SEQUENCE ");
    out.print(sequenceName);
    out.println(";");
  }

  public static void writeInsert(final PrintWriter out, final DataObject row) {
    final DataObjectMetaData metaData = row.getMetaData();
    final QName typeName = metaData.getName();
    out.print("INSERT INTO ");
    writeTableName(out, typeName);
    out.print(" (");
    for (int i = 0; i < metaData.getAttributeCount(); i++) {
      if (i > 0) {
        out.print(", ");
      }
      out.print(metaData.getAttributeName(i));
    }
    out.print(" ) VALUES (");
    for (int i = 0; i < metaData.getAttributeCount(); i++) {
      if (i > 0) {
        out.print(", ");
      }
      final Object value = row.getValue(i);
      if (value == null) {
        out.print("NULL");
      } else if (value instanceof Number) {
        final Number number = (Number)value;
        out.print(FORMAT.format(number));
      } else {
        out.print("'");
        out.print(value.toString().replaceAll("'", "''"));
        out.print("'");
      }
    }
    out.println(");");

  }

  public static void writeInserts(final PrintWriter out,
    final List<DataObject> rows) {
    for (final DataObject row : rows) {
      writeInsert(out, row);
    }

  }

  public static void writeTableName(final PrintWriter out, final QName typeName) {
    String schemaName = typeName.getNamespaceURI();
    if (schemaName.length() == 0) {
      schemaName = "public";
    }
    final String tableName = typeName.getLocalPart();
    writeTableName(out, schemaName, tableName);
  }

  public static void writeTableName(final PrintWriter out,
    final String schemaName, final String tableName) {
    out.print(schemaName);
    out.print('.');
    out.print(tableName);
  }
}
