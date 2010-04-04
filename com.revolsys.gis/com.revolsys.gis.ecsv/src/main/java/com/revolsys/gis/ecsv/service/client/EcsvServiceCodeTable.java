package com.revolsys.gis.ecsv.service.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.io.Reader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.codes.AbstractCodeTable;
import com.revolsys.gis.data.model.codes.CodeTable;

public class EcsvServiceCodeTable extends AbstractCodeTable {
  private EcsvDataObjectStore client;

  private String idColumn;

  private QName typeName;

  private List<String> valueNames;

  public EcsvServiceCodeTable() {
  }

  public EcsvServiceCodeTable(
    final EcsvDataObjectStore client,
    final QName typeName,
    final String idColumn,
    final String... valueNames) {
    this.client = client;
    this.typeName = typeName;
    this.idColumn = idColumn;
    this.valueNames = Arrays.asList(valueNames);
    loadValues();
  }

  @Override
  public CodeTable clone() {
    return new EcsvServiceCodeTable(client, typeName, idColumn,
      valueNames.toArray(new String[0]));
  }

  public String getIdColumn() {
    return idColumn;
  }

  @Override
  protected Number loadId(
    final List<Object> values,
    final boolean createId) {
    loadValues();
    return getIdByValue(values);
  }

  private void loadValues() {
    final Reader<? extends DataObject> reader = client.query(typeName);
    for (final DataObject code : reader) {
      final Number id = (Number)code.getValue(idColumn);
      final List<Object> values = new ArrayList<Object>();
      for (final String valueName : valueNames) {
        values.add(code.getValue(valueName));
      }
      addValue(id, values);
    }
  }

  @Override
  protected List<Object> loadValues(
    final Number id) {
    loadValues();
    return getValueById(id);
  }

  public String toString(
    final List<String> values) {
    final StringBuffer string = new StringBuffer(values.get(0));
    for (int i = 1; i < values.size(); i++) {
      final String value = values.get(i);
      string.append(",");
      string.append(value);
    }
    return string.toString();
  }
}
