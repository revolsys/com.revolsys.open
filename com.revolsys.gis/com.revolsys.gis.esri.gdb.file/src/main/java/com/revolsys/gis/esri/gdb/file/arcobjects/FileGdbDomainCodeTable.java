package com.revolsys.gis.esri.gdb.file.arcobjects;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esri.arcgis.geodatabase.CodedValueDomain;
import com.esri.arcgis.geodatabase.Workspace;
import com.esri.arcgis.geodatabase.esriFieldType;
import com.revolsys.gis.data.model.codes.AbstractCodeTable;

public class FileGdbDomainCodeTable extends AbstractCodeTable<Object> {
  private final CodedValueDomain domain;

  private final Workspace workspace;

  private String name;

  private static final Logger LOG = LoggerFactory.getLogger(FileGdbDomainCodeTable.class);

  public FileGdbDomainCodeTable(final Workspace workspace,
    final CodedValueDomain domain) {
    this.workspace = workspace;
    this.domain = domain;
    try {
      this.name = domain.getName();
      for (int i = 0; i < domain.getCodeCount(); i++) {
        final Object id = domain.getValue(i);
        final String name = domain.getName(i);
        addValue(id, name);
      }
    } catch (final Exception e) {
      throw new RuntimeException("Unable to create code table", e);
    }
  }

  @Override
  public FileGdbDomainCodeTable clone() {
    return (FileGdbDomainCodeTable)super.clone();
  }

  private Object createValue(final String value) {
    try {
      Object id = getNextId();
      if (domain.getFieldType() == esriFieldType.esriFieldTypeInteger) {
        id = ((Number)id).intValue();
      } else if (domain.getFieldType() == esriFieldType.esriFieldTypeSmallInteger) {
        id = ((Number)id).shortValue();
      }
      addValue(id, value);
      domain.addCode(id, value);
      workspace.alterDomain(domain);
      LOG.info(this.name + " created code " + id + "=" + value);
      return id;
    } catch (final Exception e) {
      throw new RuntimeException("Unable to create value " + this.name + " "
        + value, e);
    }
  }

  @Override
  public Object getId(final Map<String, ? extends Object> values) {
    final Object id = super.getId(values);
    if (id == null) {
      return createValue((String)values.get("NAME"));
    }
    return id;
  }

  @Override
  public Object getId(final Object... values) {
    final Object id = super.getId(values);
    if (id == null) {
      return createValue((String)values[0]);
    }
    return id;
  }

  public String getIdAttributeName() {
    return name + "_ID";
  }

  public String getName() {
    return name;
  }

  @Override
  public List<String> getValueAttributeNames() {
    return Arrays.asList("NAME");
  }

  @Override
  public String toString() {
    return name;
  }
}
