package com.revolsys.gis.jdbc.io;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import javax.xml.namespace.QName;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.ShortNameProperty;

public abstract class JdbcDdlWriter {
  private static final NumberFormat FORMAT = new DecimalFormat(
    "#.#########################");

  private PrintWriter out;

  public JdbcDdlWriter() {
  }

  public JdbcDdlWriter(final PrintWriter out) {
    this.out = out;
  }

  public PrintWriter getOut() {
    return out;
  }

  public String getTableAlias(final DataObjectMetaData metaData) {
    String shortName = ShortNameProperty.getShortName(metaData);
    if (shortName == null) {
      QName typeName = metaData.getName();
      return typeName.getLocalPart();
    } else {
      return shortName;
    }
  }

  public void println() {
    out.println();
  }

  public void setOut(final PrintWriter out) {
    this.out = out;
  }

  public void writeAddForeignKeyConstraint(final DataObjectMetaData metaData,
    final String attributeName, final DataObjectMetaData referencedMetaData) {
    final QName typeName = metaData.getName();
    final QName referencedTypeName = referencedMetaData.getName();
    final String referencedAttributeName = referencedMetaData.getIdAttributeName();
    final String constraintName = getTableAlias(metaData) + "_"
      + getTableAlias(referencedMetaData) + "_FK";
    writeAddForeignKeyConstraint(typeName, constraintName, attributeName,
      referencedTypeName, referencedAttributeName);
  }

  public void writeAddForeignKeyConstraint(final DataObjectMetaData metaData,
    final String attributeName, final String referenceTablePrefix,
    final DataObjectMetaData referencedMetaData) {
    final QName typeName = metaData.getName();
    final QName referencedTypeName = referencedMetaData.getName();
    final String referencedAttributeName = referencedMetaData.getIdAttributeName();
    final String constraintName = getTableAlias(metaData) + "_"
      + referenceTablePrefix + "_" + getTableAlias(referencedMetaData) + "_FK";
    writeAddForeignKeyConstraint(typeName, constraintName, attributeName,
      referencedTypeName, referencedAttributeName);
  }

  public void writeAddForeignKeyConstraint(final QName typeName,
    final String constraintName, final String attributeName,
    final QName referencedTypeName, final String referencedAttributeName) {
    out.print("ALTER TABLE ");
    writeTableName(typeName);
    out.print(" ADD CONSTRAINT ");
    out.print(constraintName);
    out.print(" FOREIGN KEY (");
    out.print(attributeName);
    out.print(") REFERENCES ");
    writeTableName(referencedTypeName);
    out.print(" (");
    out.print(referencedAttributeName);
    out.println(");");
  }

  public void writeAddPrimaryKeyConstraint(final DataObjectMetaData metaData) {
    final String idAttributeName = metaData.getIdAttributeName();
    if (idAttributeName != null) {
      final QName typeName = metaData.getName();
      final String constraintName = getTableAlias(metaData) + "_PK";
      writeAddPrimaryKeyConstraint(typeName, constraintName, idAttributeName);
    }
  }

  public void writeAddPrimaryKeyConstraint(final QName typeName,
    final String constraintName, final String columnName) {
    out.print("ALTER TABLE ");
    writeTableName(typeName);
    out.print(" ADD CONSTRAINT ");
    out.print(constraintName);
    out.print(" PRIMARY KEY (");
    out.print(columnName);
    out.println(");");
  }

  public abstract void writeColumnDataType(final Attribute attribute);

  public void writeCreateSchema(final String schemaName) {
  }

  public String writeCreateSequence(final DataObjectMetaData metaData) {
    final String sequenceName = getSequenceName(metaData);
    writeCreateSequence(sequenceName);
    return sequenceName;
  }

  public String getSequenceName(final DataObjectMetaData metaData) {
    final QName typeName = metaData.getName();
    final String schema = typeName.getNamespaceURI().toLowerCase();
    final String tableName = typeName.getLocalPart().toLowerCase();
    final String idAttributeName = metaData.getIdAttributeName().toLowerCase();
    final String sequenceName = schema + "." + tableName + "_"
      + idAttributeName + "_seq";
    return sequenceName;
  }

  public void writeCreateSequence(final String sequenceName) {
    out.print("CREATE SEQUENCE ");
    out.print(sequenceName);
    out.println(";");
  }

  public void writeCreateTable(final DataObjectMetaData metaData) {
    final QName typeName = metaData.getName();
    out.println();
    out.print("CREATE TABLE ");
    writeTableName(typeName);
    out.println(" (");
    for (int i = 0; i < metaData.getAttributeCount(); i++) {
      final Attribute attribute = metaData.getAttribute(i);
      if (i > 0) {
        out.println(",");
      }
      final String name = attribute.getName();
      out.print("  ");
      out.print(name);
      for (int j = name.length(); j < 32; j++) {
        out.print(' ');
      }
      writeColumnDataType(attribute);
      if (attribute.isRequired()) {
        out.print(" NOT NULL");
      }
    }
    out.println();
    out.println(");");

    writeAddPrimaryKeyConstraint(metaData);

    writeGeometryMetaData(metaData);

    final Attribute idAttribute = metaData.getIdAttribute();
    if (idAttribute != null) {
      if (Number.class.isAssignableFrom(idAttribute.getType().getJavaClass())) {
        writeCreateSequence(metaData);
      }
    }
  }

  public abstract void writeGeometryMetaData(final DataObjectMetaData metaData);

  public void writeInsert(final DataObject row) {
    final DataObjectMetaData metaData = row.getMetaData();
    final QName typeName = metaData.getName();
    out.print("INSERT INTO ");
    writeTableName(typeName);
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

  public void writeInserts(final List<DataObject> rows) {
    for (final DataObject row : rows) {
      writeInsert(row);
    }

  }

  public void writeTableName(final QName typeName) {
    final String schemaName = typeName.getNamespaceURI();
    final String tableName = typeName.getLocalPart();
    writeTableName(schemaName, tableName);
  }

  public void writeTableName(final String schemaName, final String tableName) {
    if (StringUtils.hasText(schemaName)) {
      out.print(schemaName);
      out.print('.');
    }
    out.print(tableName);
  }

  public void writeResetSequence(DataObjectMetaData metaData,
    List<DataObject> values) {
    throw new UnsupportedOperationException();
  }
}
