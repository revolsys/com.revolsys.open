package com.revolsys.gis.esri.gdb.file.capi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JComponent;

import org.jeometry.common.compare.CompareUtil;
import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.logging.Logs;

import com.revolsys.gis.esri.gdb.file.FileGdbRecordStore;
import com.revolsys.record.code.AbstractCodeTable;
import com.revolsys.record.code.CodeTableEntry;
import com.revolsys.record.io.format.esri.gdb.xml.model.Domain;
import com.revolsys.record.io.format.json.JsonObject;

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
  public FileGdbDomainCodeTable clone() {
    return this;
  }

  @Override
  public void close() {
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
  public CodeTableEntry getEntry(Consumer<CodeTableEntry> callback, Object idOrValue) {
    return this.domain.getEntry(callback, idOrValue);
  }

  @Override
  public List<String> getFieldNameAliases() {
    return this.domain.getFieldNameAliases();
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
  public JsonObject getMap(final Identifier id) {
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
  public int getValueFieldLength() {
    return this.domain.getValueFieldLength();
  }

  @Override
  public List<String> getValueFieldNames() {
    return this.domain.getValueFieldNames();
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
