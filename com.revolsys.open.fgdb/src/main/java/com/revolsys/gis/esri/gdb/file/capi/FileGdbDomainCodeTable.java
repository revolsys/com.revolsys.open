package com.revolsys.gis.esri.gdb.file.capi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import org.jeometry.common.compare.CompareUtil;
import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.logging.Logs;

import com.revolsys.gis.esri.gdb.file.FileGdbRecordStore;
import com.revolsys.record.code.AbstractCodeTable;
import com.revolsys.record.io.format.esri.gdb.xml.model.CodedValue;
import com.revolsys.record.io.format.esri.gdb.xml.model.Domain;

public class FileGdbDomainCodeTable extends AbstractCodeTable {
  private final Domain domain;

  private final String name;

  private final FileGdbRecordStore recordStore;

  private JComponent swingEditor;

  public FileGdbDomainCodeTable(final FileGdbRecordStore recordStore, final Domain domain) {
    this.recordStore = recordStore;
    this.domain = domain;
    this.name = domain.getDomainName();
  }

  @Override
  protected int calculateValueFieldLength() {
    int length = 0;
    for (final CodedValue codedValue : this.domain.getCodedValues()) {
      final String name = codedValue.getName();
      final int valueLength = name.length();
      if (valueLength > length) {
        length = valueLength;
      }
    }
    return length;
  }

  @Override
  public FileGdbDomainCodeTable clone() {
    return (FileGdbDomainCodeTable)super.clone();
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
      final Object codeValue1 = getValue(Identifier.newIdentifier(value1));
      final Object codeValue2 = getValue(Identifier.newIdentifier(value2));
      return CompareUtil.compare(codeValue1, codeValue2);
    }
  }

  public Domain getDomain() {
    return this.domain;
  }

  @Override
  public List<String> getFieldNameAliases() {
    return this.domain.getFieldNameAliases();
  }

  @Override
  public Identifier getIdentifier(final List<Object> values) {
    final Identifier id = this.domain.getIdentifier(values);
    if (id == null) {
      return newIdentifier((String)values.get(0));
    }
    return id;
  }

  @Override
  public Identifier getIdentifier(final Map<String, ? extends Object> values) {
    final Identifier id = this.domain.getIdentifier(values);
    if (id == null) {
      return newIdentifier(this.domain.getName(values));
    }
    return id;
  }

  @Override
  public List<Identifier> getIdentifiers() {
    return new ArrayList<>(this.domain.getIdentifiers());
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
    return getValue(Identifier.newIdentifier(id));
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
  public boolean isEmpty() {
    return this.domain.isEmpty();
  }

  @Override
  public boolean isLoaded() {
    return true;
  }

  @Override
  public boolean isLoading() {
    return false;
  }

  private Identifier newIdentifier(final String name) {
    synchronized (this.domain) {
      final Identifier id = this.domain.newCodedValue(name);
      this.recordStore.alterDomain(this.domain);
      Logs.info(this, this.domain.getDomainName() + " created code " + id + "=" + name);
      return id;
    }
  }

  @Override
  public void refresh() {
  }

  @Override
  public void setSwingEditor(final JComponent swingEditor) {
    this.swingEditor = swingEditor;
  }

  @Override
  public String toString() {
    return this.domain.toString();
  }
}
