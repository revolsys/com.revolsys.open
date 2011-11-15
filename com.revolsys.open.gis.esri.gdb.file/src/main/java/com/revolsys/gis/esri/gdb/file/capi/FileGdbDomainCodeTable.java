package com.revolsys.gis.esri.gdb.file.capi;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.esri.gdb.file.capi.swig.Geodatabase;
import com.revolsys.io.esri.gdb.xml.model.CodedValueDomain;
import com.revolsys.io.esri.gdb.xml.model.Domain;
import com.revolsys.io.esri.gdb.xml.model.EsriGdbXmlSerializer;

public class FileGdbDomainCodeTable implements CodeTable {
  private final CodedValueDomain domain;

  private final Geodatabase geodatabase;

  private static final Logger LOG = LoggerFactory.getLogger(FileGdbDomainCodeTable.class);

  private String name;

  public FileGdbDomainCodeTable(final Geodatabase geodatabase,
    final CodedValueDomain domain) {
    this.geodatabase = geodatabase;
    this.domain = domain;
    this.name = domain.getDomainName();
  }

  public String getName() {
    return name;
  }

  @Override
  public FileGdbDomainCodeTable clone() {
    try {
      return (FileGdbDomainCodeTable)super.clone();
    } catch (final CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  private Object createValue(final String name) {
    final Object id = domain.addCodedValue(name);
    final String domainDefinition = EsriGdbXmlSerializer.toString(domain);
    geodatabase.alterDomain(domainDefinition);
    LOG.info(domain.getDomainName() + " created code " + id + "=" + name);
    return id;
  }

  public List<String> getAttributeAliases() {
    return domain.getAttributeAliases();
  }

  public Map<Object, List<Object>> getCodes() {
    return domain.getCodes();
  }

  public Domain getDomain() {
    return domain;
  }

  public <T> T getId(final Map<String, ? extends Object> values) {
    final Object id = domain.getId(values);
    if (id == null) {
      return (T)createValue(domain.getName(values));
    }
    return (T)id;
  }

  public <T> T getId(final Object... values) {
    final Object id = domain.getId(values);
    if (id == null) {
      return (T)createValue((String)values[0]);
    }
    return (T)id;
  }

  public String getIdAttributeName() {
    return domain.getIdAttributeName();
  }

  public Map<String, ? extends Object> getMap(final Object id) {
    return domain.getMap(id);
  }

  @SuppressWarnings("unchecked")
  public <V> V getValue(final Object id) {
    return (V)domain.getValue(id);
  }

  public List<String> getValueAttributeNames() {
    return domain.getValueAttributeNames();
  }

  public List<Object> getValues(final Object id) {
    return domain.getValues(id);
  }

  @Override
  public String toString() {
    return domain.toString();
  }
}
