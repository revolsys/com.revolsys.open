package com.revolsys.jdbc.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.property.ShortNameProperty;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.PathUtil;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.MathUtil;

public abstract class JdbcDdlWriter implements Cloneable {
  private PrintWriter out;

  public JdbcDdlWriter() {
  }

  public JdbcDdlWriter(final PrintWriter out) {
    this.out = out;
  }

  @Override
  public JdbcDdlWriter clone() {
    try {
      return (JdbcDdlWriter)super.clone();
    } catch (final CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  public JdbcDdlWriter clone(final File file) {
    final JdbcDdlWriter clone = clone();
    clone.setOut(file);
    return clone;
  }

  public void close() {
    out.flush();
    out.close();
  }

  public PrintWriter getOut() {
    return out;
  }

  public String getSequenceName(final RecordDefinition recordDefinition) {
    throw new UnsupportedOperationException();
  }

  public String getTableAlias(final RecordDefinition recordDefinition) {
    final String shortName = ShortNameProperty.getShortName(recordDefinition);
    if (shortName == null) {
      final String path = recordDefinition.getPath();
      return PathUtil.getName(path);
    } else {
      return shortName;
    }
  }

  public void println() {
    out.println();
  }

  public void setOut(final File file) {
    try {
      final FileWriter writer = new FileWriter(file);
      out = new PrintWriter(writer);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void setOut(final PrintWriter out) {
    this.out = out;
  }

  public void writeAddForeignKeyConstraint(final RecordDefinition recordDefinition,
    final String attributeName, final RecordDefinition referencedMetaData) {
    final String typePath = recordDefinition.getPath();
    final String referencedTypeName = referencedMetaData.getPath();
    final String referencedAttributeName = referencedMetaData.getIdAttributeName();
    final String constraintName = getTableAlias(recordDefinition) + "_"
      + getTableAlias(referencedMetaData) + "_FK";
    writeAddForeignKeyConstraint(typePath, constraintName, attributeName,
      referencedTypeName, referencedAttributeName);
  }

  public void writeAddForeignKeyConstraint(final RecordDefinition recordDefinition,
    final String attributeName, final String referenceTablePrefix,
    final RecordDefinition referencedMetaData) {
    final String typePath = recordDefinition.getPath();
    final String referencedTypeName = referencedMetaData.getPath();
    final String referencedAttributeName = referencedMetaData.getIdAttributeName();
    final String constraintName = getTableAlias(recordDefinition) + "_"
      + referenceTablePrefix + "_" + getTableAlias(referencedMetaData) + "_FK";
    writeAddForeignKeyConstraint(typePath, constraintName, attributeName,
      referencedTypeName, referencedAttributeName);
  }

  public void writeAddForeignKeyConstraint(final String typePath,
    final String constraintName, final String attributeName,
    final String referencedTypeName, final String referencedAttributeName) {
    out.print("ALTER TABLE ");
    writeTableName(typePath);
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

  public void writeAddPrimaryKeyConstraint(final RecordDefinition recordDefinition) {
    final String idAttributeName = recordDefinition.getIdAttributeName();
    if (idAttributeName != null) {
      final String typePath = recordDefinition.getPath();
      final String constraintName = getTableAlias(recordDefinition) + "_PK";
      writeAddPrimaryKeyConstraint(typePath, constraintName, idAttributeName);
    }
  }

  public void writeAddPrimaryKeyConstraint(final String typePath,
    final String constraintName, final String columnName) {
    out.print("ALTER TABLE ");
    writeTableName(typePath);
    out.print(" ADD CONSTRAINT ");
    out.print(constraintName);
    out.print(" PRIMARY KEY (");
    out.print(columnName);
    out.println(");");
  }

  public abstract void writeColumnDataType(final Attribute attribute);

  public void writeCreateSchema(final String schemaName) {
  }

  public String writeCreateSequence(final RecordDefinition recordDefinition) {
    final String sequenceName = getSequenceName(recordDefinition);
    writeCreateSequence(sequenceName);
    return sequenceName;
  }

  public void writeCreateSequence(final String sequenceName) {
    out.print("CREATE SEQUENCE ");
    out.print(sequenceName);
    out.println(";");
  }

  public void writeCreateTable(final RecordDefinition recordDefinition) {
    final String typePath = recordDefinition.getPath();
    out.println();
    out.print("CREATE TABLE ");
    writeTableName(typePath);
    out.println(" (");
    for (int i = 0; i < recordDefinition.getAttributeCount(); i++) {
      final Attribute attribute = recordDefinition.getAttribute(i);
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

    writeAddPrimaryKeyConstraint(recordDefinition);

    writeGeometryMetaData(recordDefinition);

    final Attribute idAttribute = recordDefinition.getIdAttribute();
    if (idAttribute != null) {
      if (Number.class.isAssignableFrom(idAttribute.getType().getJavaClass())) {
        writeCreateSequence(recordDefinition);
      }
    }
  }

  public void writeCreateView(final String typePath,
    final String queryTypeName, final List<String> columnNames) {
    out.println();
    out.print("CREATE VIEW ");
    writeTableName(typePath);
    out.println(" AS ( ");
    out.println("  SELECT ");
    out.print("  ");
    out.println(CollectionUtil.toString(",\n  ", columnNames));
    out.print("  FROM ");
    writeTableName(queryTypeName);
    out.println();
    out.println(");");
  }

  public abstract void writeGeometryMetaData(final RecordDefinition recordDefinition);

  public void writeGrant(final String typePath, final String username,
    final boolean select, final boolean insert, final boolean update,
    final boolean delete) {

    out.print("GRANT ");
    final List<String> perms = new ArrayList<String>();
    if (select) {
      perms.add("SELECT");
    }
    if (insert) {
      perms.add("INSERT");
    }
    if (update) {
      perms.add("UPDATE");
    }
    if (delete) {
      perms.add("DELETE");
    }
    out.print(CollectionUtil.toString(", ", perms));
    out.print(" ON ");
    writeTableName(typePath);
    out.print(" TO ");
    out.print(username);
    out.println(";");

  }

  public void writeInsert(final Record row) {
    final RecordDefinition recordDefinition = row.getRecordDefinition();
    final String typePath = recordDefinition.getPath();
    out.print("INSERT INTO ");
    writeTableName(typePath);
    out.print(" (");
    for (int i = 0; i < recordDefinition.getAttributeCount(); i++) {
      if (i > 0) {
        out.print(", ");
      }
      out.print(recordDefinition.getAttributeName(i));
    }
    out.print(" ) VALUES (");
    for (int i = 0; i < recordDefinition.getAttributeCount(); i++) {
      if (i > 0) {
        out.print(", ");
      }
      final Object value = row.getValue(i);
      if (value == null) {
        out.print("NULL");
      } else if (value instanceof Number) {
        final Number number = (Number)value;
        out.print(MathUtil.toString(number));
      } else {
        out.print("'");
        out.print(value.toString().replaceAll("'", "''"));
        out.print("'");
      }
    }
    out.println(");");

  }

  public void writeInserts(final List<Record> rows) {
    for (final Record row : rows) {
      writeInsert(row);
    }

  }

  public void writeResetSequence(final RecordDefinition recordDefinition,
    final List<Record> values) {
    throw new UnsupportedOperationException();
  }

  public void writeTableName(final String typePath) {
    final String schemaName = PathUtil.getPath(typePath).substring(1);
    final String tableName = PathUtil.getName(typePath);
    writeTableName(schemaName, tableName);
  }

  public void writeTableName(final String schemaName, final String tableName) {
    if (StringUtils.hasText(schemaName)) {
      out.print(schemaName);
      out.print('.');
    }
    out.print(tableName);
  }
}
