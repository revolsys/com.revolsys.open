package com.revolsys.gis.oracle.io;

import java.io.PrintWriter;
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
import com.revolsys.jdbc.io.JdbcDdlWriter;
import com.vividsolutions.jts.geom.Geometry;

public class OracleDdlWriter extends JdbcDdlWriter {
  public OracleDdlWriter() {
  }

  public OracleDdlWriter(PrintWriter out) {
    super(out);
  }

  public void writeColumnDataType(final Attribute attribute) {
    PrintWriter out = getOut();
    final DataType dataType = attribute.getType();
    if (dataType == DataTypes.BOOLEAN) {
      out.print("NUMBER(1,0)");
    } else if (dataType == DataTypes.BYTE) {
      out.print("NUMBER(3)");
    } else if (dataType == DataTypes.SHORT) {
      out.print("NUMBER(5)");
    } else if (dataType == DataTypes.INT) {
      out.print("NUMBER(10)");
    } else if (dataType == DataTypes.LONG) {
      out.print("NUMBER(19)");
    } else if (dataType == DataTypes.FLOAT) {
      out.print("float");
    } else if (dataType == DataTypes.DOUBLE) {
      out.print("double precision");
    } else if (dataType == DataTypes.DATE) {
      out.print("DATE");
    } else if (dataType == DataTypes.DATE_TIME) {
      out.print("TIMESTAMP");
    } else if (dataType == DataTypes.STRING) {
      out.print("VARCHAR2(");
      out.print(attribute.getLength());
      out.print(")");
    } else if (dataType == DataTypes.INTEGER) {
      out.print("NUMBER(");
      out.print(attribute.getLength());
      out.print(')');
    } else if (dataType == DataTypes.DECIMAL) {
      out.print("NUMBER(");
      out.print(attribute.getLength());
      int scale = attribute.getScale();
      if (scale >= 0) {
        out.print(',');
        out.print(scale);
      }
      out.print(')');
    } else if (Geometry.class.isAssignableFrom(dataType.getJavaClass())) {
      out.print("MDSYS.SDO_GEOMETRY");
    } else {
      throw new IllegalArgumentException("Unknown data type" + dataType);
    }
  }

  public void writeResetSequence(DataObjectMetaData metaData,
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
    out.println("DECLARE");
    out.println("  cur_val NUMBER;");
    out.println("BEGIN");

    out.print("  SELECT ");
    out.print(sequeneName);
    out.println(".NEXTVAL INTO cur_val FROM DUAL;");

    out.print("  IF cur_val + 1 <> ");
    out.print(nextValue);
    out.println(" THEN");

    out.print("    EXECUTE IMMEDIATE 'ALTER SEQUENCE ");
    out.print(sequeneName);
    out.print(" INCREMENT BY ' || (");
    out.print(nextValue);
    out.println(" -  cur_val -1) || ' MINVALUE 1';");

    out.print("    SELECT ");
    out.print(sequeneName);
    out.println(".NEXTVAL INTO cur_val FROM DUAL;");

    out.print("    EXECUTE IMMEDIATE 'ALTER SEQUENCE ");
    out.print(sequeneName);
    out.println(" INCREMENT BY 1';");
    out.println("  END IF;");
    out.println("END;");
    out.println("/");
  }

  public String writeCreateSequence(final DataObjectMetaData metaData) {
    final String sequenceName = getSequenceName(metaData);
    writeCreateSequence(sequenceName);
    return sequenceName;
  }

  public String getSequenceName(final DataObjectMetaData metaData) {
    final QName typeName = metaData.getName();
    final String schemaName = typeName.getNamespaceURI().toUpperCase();
    final String tableName = typeName.getLocalPart().toUpperCase();
    final String sequenceName = schemaName + "." + tableName + "_SEQ";
    return sequenceName;
  }

  public void writeGeometryMetaData(final DataObjectMetaData metaData) {
    PrintWriter out = getOut();
    QName typeName = metaData.getName();
    String schemaName = typeName.getNamespaceURI();
    final String tableName = typeName.getLocalPart();
    final Attribute geometryAttribute = metaData.getGeometryAttribute();
    if (geometryAttribute != null) {
      final GeometryFactory geometryFactory = geometryAttribute.getProperty(AttributeProperties.GEOMETRY_FACTORY);
      final String name = geometryAttribute.getName();
      final int numAxis = geometryFactory.getNumAxis();
      final DataType dataType = geometryAttribute.getType();
      final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
      final int srid = coordinateSystem.getId();

      out.print("INSERT INTO USER_SDO_GEOM_METADATA(TABLE_NAME, COLUMN_NAME, DIMINFO, SRID) VALUES('");
      out.print(tableName.toUpperCase());
      out.print("','");
      out.print(name.toUpperCase());
      // TODO get from geometry factory
      out.print("',MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 263000, 1876000, 0.001),MDSYS.SDO_DIM_ELEMENT('Y', 356000, 1738000, 0.001)");
      if (numAxis > 2) {
        out.print(",MDSYS.SDO_DIM_ELEMENT('Z',-2500, 5000, 0.001)");
      }
      out.print("),");
      out.println("3005);");

      int geometryType = OracleSdoGeometryAttributeAdder.getGeometryTypeId(
        dataType, numAxis);
      out.print("INSERT INTO OGIS_GEOMETRY_COLUMNS(F_TABLE_SCHEMA,F_TABLE_NAME,F_GEOMETRY_COLUMN,G_TABLE_SCHEMA,G_TABLE_NAME,GEOMETRY_TYPE,COORD_DIMENSION,SRID) VALUES ('");
      out.print(schemaName.toUpperCase());
      out.print("', '");
      out.print(tableName.toUpperCase());
      out.print("','");
      out.print(name.toUpperCase());
      out.print("', '");
      out.print(schemaName.toUpperCase());
      out.print("', '");
      out.print(tableName.toUpperCase());
      out.print("',");
      out.print(geometryType);
      out.print(",");
      out.print(numAxis);
      out.print(",");
      out.print("100");
      out.print(srid);
      out.println(");");
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

  public void writeAlterOwner(final String objectType, final String objectName,
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
