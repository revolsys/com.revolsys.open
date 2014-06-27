package com.revolsys.gis.esri.gdb.file.capi;

import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.gis.data.model.RecordIdentifier;
import com.revolsys.gis.data.model.SingleRecordIdentifier;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.esri.gdb.file.CapiFileGdbDataObjectStore;
import com.revolsys.io.esri.gdb.xml.model.CodedValueDomain;
import com.revolsys.io.esri.gdb.xml.model.Domain;

public class FileGdbDomainCodeTable implements CodeTable {
  private final CodedValueDomain domain;

  private static final Logger LOG = LoggerFactory.getLogger(FileGdbDomainCodeTable.class);

  private final String name;

  private final CapiFileGdbDataObjectStore dataStore;

  private JComponent swingEditor;

  public FileGdbDomainCodeTable(final CapiFileGdbDataObjectStore dataStore,
    final CodedValueDomain domain) {
    this.dataStore = dataStore;
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

  private RecordIdentifier createValue(final String name) {
    synchronized (this.dataStore) {
      final RecordIdentifier id = this.domain.addCodedValue(name);
      this.dataStore.alterDomain(this.domain);
      LOG.info(this.domain.getDomainName() + " created code " + id + "=" + name);
      return id;
    }
  }

  @Override
  public List<String> getAttributeAliases() {
    return this.domain.getAttributeAliases();
  }

  @Override
  public Map<RecordIdentifier, List<Object>> getCodes() {
    return this.domain.getCodes();
  }

  public Domain getDomain() {
    return this.domain;
  }

  @Override
  public RecordIdentifier getId(final Map<String, ? extends Object> values) {
    final RecordIdentifier id = this.domain.getId(values);
    if (id == null) {
      return createValue(this.domain.getName(values));
    }
    return id;
  }

  @Override
  public RecordIdentifier getId(final Object... values) {
    final RecordIdentifier id = this.domain.getId(values);
    if (id == null) {
      return createValue((String)values[0]);
    }
    return id;
  }

  @Override
  public String getIdAttributeName() {
    return this.domain.getIdAttributeName();
  }

  @Override
  public Map<String, ? extends Object> getMap(final RecordIdentifier id) {
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
  public <V> V getValue(final Object id) {
    return getValue(SingleRecordIdentifier.create(id));
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V> V getValue(final RecordIdentifier id) {
    return (V)this.domain.getValue(id);
  }

  @Override
  public List<String> getValueAttributeNames() {
    return this.domain.getValueAttributeNames();
  }

  @Override
  public List<Object> getValues(final RecordIdentifier id) {
    return this.domain.getValues(id);
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
