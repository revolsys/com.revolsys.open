package com.revolsys.gis.ecsv.service.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.codes.AbstractCodeTable;
import com.revolsys.io.Reader;

public class EcsvServiceCodeTable extends AbstractCodeTable {
  private EcsvDataObjectStore client;

  private String idColumn;

  private String typePath;

  private List<String> valueNames;

  public EcsvServiceCodeTable() {
  }

  public EcsvServiceCodeTable(final EcsvDataObjectStore client,
    final String path, final String idColumnName, final String... valueNames) {
    this.client = client;
    this.typePath = typePath;
    this.idColumn = idColumnName;
    this.valueNames = Arrays.asList(valueNames);
  }

  @Override
  public AbstractCodeTable clone() {
    final EcsvServiceCodeTable clone = (EcsvServiceCodeTable)super.clone();
    return clone;
  }

  public String getIdAttributeName() {
    return idColumn;
  }

  @Override
  public List<String> getValueAttributeNames() {
    return valueNames;
  }

  @Override
  protected Object loadId(final List<Object> values, final boolean createId) {
    loadValues();
    return getIdByValue(values);
  }

  private void loadValues() {
    final Reader<? extends DataObject> reader = client.query(typePath);
    for (final DataObject code : reader) {
      final Number id = (Number)code.getValue(idColumn);
      final List<Object> values = new ArrayList<Object>();
      for (final String valueName : getValueAttributeNames()) {
        values.add(code.getValue(valueName));
      }
      addValue(id, values);
    }
  }

  @Override
  protected List<Object> loadValues(final Object id) {
    loadValues();
    return getValueById(id);
  }

  public String toString(final List<String> values) {
    final StringBuffer string = new StringBuffer(values.get(0));
    for (int i = 1; i < values.size(); i++) {
      final String value = values.get(i);
      string.append(",");
      string.append(value);
    }
    return string.toString();
  }
}
