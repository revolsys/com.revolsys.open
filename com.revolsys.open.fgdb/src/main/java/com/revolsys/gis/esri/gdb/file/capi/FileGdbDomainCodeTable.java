package com.revolsys.gis.esri.gdb.file.capi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.data.codes.CodeTable;
import com.revolsys.data.identifier.Identifier;
import com.revolsys.format.esri.gdb.xml.model.CodedValueDomain;
import com.revolsys.format.esri.gdb.xml.model.Domain;
import com.revolsys.gis.esri.gdb.file.FileGdbRecordStore;
import com.revolsys.util.CompareUtil;

public class FileGdbDomainCodeTable implements CodeTable {
  private static final Logger LOG = LoggerFactory.getLogger(FileGdbDomainCodeTable.class);

  private final CodedValueDomain domain;

  private final String name;

  private final FileGdbRecordStore recordStore;

  private JComponent swingEditor;

  public FileGdbDomainCodeTable(final FileGdbRecordStore recordStore,
    final CodedValueDomain domain) {
    this.recordStore = recordStore;
    this.domain = domain;
    this.name = domain.getDomainName();
  }

  @Override
  public FileGdbDomainCodeTable clone() {
    try {
      return (FileGdbDomainCodeTable)super.clone();
    } catch (final CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public int compare(final Object value1, final Object value2) {
    if (value1 == null) {
      if (value2 == null) {
        return 0;
      } else {
        return 1;
      }
    } else if (value2 == null) {
      return -1;
    } else {
      final Object codeValue1 = getValue(Identifier.create(value1));
      final Object codeValue2 = getValue(Identifier.create(value2));
      return CompareUtil.compare(codeValue1, codeValue2);
    }
  }

  private Identifier createValue(final String name) {
    synchronized (this.recordStore) {
      final Identifier id = this.domain.addCodedValue(name);
      this.recordStore.alterDomain(this.domain);
      LOG.info(this.domain.getDomainName() + " created code " + id + "=" + name);
      return id;
    }
  }

  @Override
  public Map<Identifier, List<Object>> getCodes() {
    return this.domain.getCodes();
  }

  public Domain getDomain() {
    return this.domain;
  }

  @Override
  public List<String> getFieldAliases() {
    return this.domain.getFieldAliases();
  }

  @Override
  public Identifier getId(final Map<String, ? extends Object> values) {
    final Identifier id = this.domain.getId(values);
    if (id == null) {
      return createValue(this.domain.getName(values));
    }
    return id;
  }

  @Override
  public Identifier getId(final Object... values) {
    final Identifier id = this.domain.getId(values);
    if (id == null) {
      return createValue((String)values[0]);
    }
    return id;
  }

  @Override
  public List<Identifier> getIdentifiers() {
    return new ArrayList<>(getCodes().keySet());
  }

  @Override
  public Identifier getIdExact(final Object... values) {
    return getId(values);
  }

  @Override
  public String getIdFieldName() {
    return this.domain.getIdFieldName();
  }

  @Override
  public Map<String, ? extends Object> getMap(final Identifier id) {
    return this.domain.getMap(id);
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public JComponent getSwingEditor() {
    return this.swingEditor;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V> V getValue(final Identifier id) {
    return (V)this.domain.getValue(id);
  }

  @Override
  public <V> V getValue(final Object id) {
    return getValue(Identifier.create(id));
  }

  @Override
  public List<String> getValueFieldNames() {
    return this.domain.getValueFieldNames();
  }

  @Override
  public List<Object> getValues(final Identifier id) {
    return this.domain.getValues(id);
  }

  @Override
  public boolean isLoaded() {
    return true;
  }

  @Override
  public boolean isLoading() {
    return false;
  }

  @Override
  public void refresh() {
  }

  public void setSwingEditor(final JComponent swingEditor) {
    this.swingEditor = swingEditor;
  }

  @Override
  public String toString() {
    return this.domain.toString();
  }
}
