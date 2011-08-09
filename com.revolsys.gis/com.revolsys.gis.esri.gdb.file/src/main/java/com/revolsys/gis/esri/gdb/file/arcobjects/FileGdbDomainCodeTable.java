package com.revolsys.gis.esri.gdb.file.arcobjects;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esri.arcgis.geodatabase.CodedValueDomain;
import com.esri.arcgis.geodatabase.Workspace;
import com.revolsys.gis.data.model.codes.AbstractCodeTable;

public class FileGdbDomainCodeTable extends AbstractCodeTable {
  private final CodedValueDomain domain;

  private final Workspace workspace;

  private String name;

  private static final Logger LOG = LoggerFactory.getLogger(FileGdbDomainCodeTable.class);

  FileGdbDomainCodeTable(final Workspace workspace,
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
    Object id = getNextId();
    id = ArcObjectsFileGdbDataObjectStore.invoke(ArcObjectsUtil.class,
      "createDomainValue", workspace, domain, id, value);
    addValue(id, value);
    LOG.info(this.name + " created code " + id + "=" + value);
    return id;
  }
  @Override
  public <T> T getId(final Map<String, ? extends Object> values) {
    final Object id = super.getId(values);
    if (id == null) {
      return (T)createValue((String)values.get("NAME"));
    }
    return (T)id;
  }

  @Override
  public <T> T getId(final Object... values) {
    final Object id = super.getId(values);
    if (id == null) {
      return (T)createValue((String)values[0]);
    }
    return (T)id;
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
