package com.revolsys.record.code;

import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JComponent;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.number.Numbers;

import com.revolsys.io.BaseCloseable;
import com.revolsys.properties.BaseObjectWithPropertiesAndChange;
import com.revolsys.record.schema.FieldDefinition;

import reactor.core.publisher.Sinks;

public abstract class AbstractCodeTable extends BaseObjectWithPropertiesAndChange
  implements BaseCloseable, CodeTable, Cloneable {

  protected boolean capitalizeWords = false;

  protected boolean caseSensitive = false;

  private String name;

  protected AtomicReference<CodeTableData> data = new AtomicReference<>();

  private final Sinks.Many<CodeTableData> dataSubject = Sinks.many().replay().latest();

  protected int valueFieldLength = -1;

  private JComponent swingEditor;

  private FieldDefinition valueFieldDefinition = new FieldDefinition("value", DataTypes.STRING,
    true);

  public AbstractCodeTable() {
  }

  protected int calculateValueFieldLength() {
    final CodeTableData data = getData();
    if (data == null) {
      return 20;
    } else {
      return data.calculateValueFieldLength();
    }
  }

  public void clear() {
    setData(null);
  }

  @Override
  public AbstractCodeTable clone() {
    final AbstractCodeTable clone = (AbstractCodeTable)super.clone();
    final CodeTableData oldData = getData();
    clone.data = new AtomicReference<>();
    if (oldData != null) {
      clone.setData(oldData.clone());
    }
    return clone;
  }

  @Override
  public void close() {
    clear();
    this.swingEditor = null;
  }

  protected CodeTableData getData() {
    return this.data.get();
  }

  @Override
  public String getName() {
    return this.name;
  }

  protected Object getNormalizedValue(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return Numbers.toString(number);
    } else if (this.caseSensitive) {
      return value;
    } else {
      return value.toString().toLowerCase();
    }
  }

  @Override
  public JComponent getSwingEditor() {
    return this.swingEditor;
  }

  @Override
  public FieldDefinition getValueFieldDefinition() {
    return this.valueFieldDefinition;
  }

  @Override
  public int getValueFieldLength() {
    if (this.valueFieldLength == -1) {
      final int length = calculateValueFieldLength();
      this.valueFieldLength = length;
    }
    return this.valueFieldLength;
  }

  public boolean isCapitalizeWords() {
    return this.capitalizeWords;
  }

  public boolean isCaseSensitive() {
    return this.caseSensitive;
  }

  @Override
  public boolean isEmpty() {
    final CodeTableData data = getData();
    return data.isEmpty();
  }

  protected CodeTableData newData() {
    return new CodeTableData(this);
  }

  @Override
  public synchronized void refresh() {
    clear();
  }

  public void setCapitalizeWords(final boolean capitalizedWords) {
    this.capitalizeWords = capitalizedWords;
  }

  public void setCaseSensitive(final boolean caseSensitive) {
    this.caseSensitive = caseSensitive;
  }

  public void setData(CodeTableData data) {
    if (data != null) {
      this.data.set(data);
      this.dataSubject.tryEmitNext(data);
    }
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setSwingEditor(final JComponent swingEditor) {
    this.swingEditor = swingEditor;
  }

  public void setValueFieldDefinition(final FieldDefinition valueFieldDefinition) {
    this.valueFieldDefinition = valueFieldDefinition;
  }

}
