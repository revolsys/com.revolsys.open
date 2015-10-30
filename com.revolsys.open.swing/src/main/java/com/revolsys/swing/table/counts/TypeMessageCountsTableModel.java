package com.revolsys.swing.table.counts;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.revolsys.collection.map.Maps;
import com.revolsys.record.io.format.tsv.Tsv;
import com.revolsys.record.io.format.tsv.TsvWriter;
import com.revolsys.swing.table.AbstractTableModel;
import com.revolsys.util.Counter;
import com.revolsys.util.LongCounter;

public class TypeMessageCountsTableModel extends AbstractTableModel {
  private static final long serialVersionUID = 1L;

  private static final String[] COLUMN_NAMES = {
    "Type", "Message", "Count"
  };

  private final List<Counter> counters = new ArrayList<>();

  private final List<String> types = new ArrayList<>();

  private final Map<String, Map<String, Integer>> indexByTypeAndMessage = new TreeMap<>();

  public synchronized long addCount(final CharSequence type, final CharSequence message) {
    return addCount(type, message, 1);
  }

  public synchronized long addCount(final CharSequence type, final CharSequence message,
    final long count) {
    final String typeName = type.toString();
    final String messageName = message.toString();
    final Map<String, Integer> indexesByMessage = Maps.get(this.indexByTypeAndMessage, typeName,
      Maps.<String, Integer> treeFactory());
    Integer index = indexesByMessage.get(messageName);
    if (index == null) {
      index = this.counters.size();
      indexesByMessage.put(messageName, index);
      this.types.add(typeName);
      final LongCounter counter = new LongCounter(messageName, count);
      this.counters.add(counter);
      fireTableRowsInserted(index, index);
      return count;
    } else {
      final Counter counter = this.counters.get(index);
      final long newCount = counter.add(count);
      fireTableCellUpdated(index, 2);
      return newCount;
    }
  }

  @Override
  public Class<?> getColumnClass(final int columnIndex) {
    switch (columnIndex) {
      case 0:
        return String.class;
      case 1:
        return String.class;
      case 2:
        return Long.class;
      default:
        return String.class;
    }
  }

  @Override
  public int getColumnCount() {
    return 3;
  }

  @Override
  public String getColumnName(final int columnIndex) {
    return COLUMN_NAMES[columnIndex];
  }

  @Override
  public int getRowCount() {
    return this.counters.size();
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    final int rowCount = getRowCount();
    if (rowIndex >= 0 && rowIndex < rowCount) {
      final Counter counter = this.counters.get(rowIndex);
      switch (columnIndex) {
        case 0:
          return this.types.get(rowIndex);
        case 1:
          return counter.getName();
        case 2:
          return counter.get();
        default:
          return null;
      }
    }
    return null;
  }

  @Override
  public void toTsv(final Writer out) {
    try (
      TsvWriter tsv = Tsv.plainWriter(out)) {
      tsv.write((Object[])COLUMN_NAMES);
      long total = 0;
      for (final Entry<String, Map<String, Integer>> typeEntry : this.indexByTypeAndMessage
        .entrySet()) {
        final String type = typeEntry.getKey();
        final Map<String, Integer> indexByMessage = typeEntry.getValue();
        for (final Entry<String, Integer> messageEntry : indexByMessage.entrySet()) {
          final String message = messageEntry.getKey();
          final Integer index = messageEntry.getValue();
          final Counter counter = this.counters.get(index);
          final long count = counter.get();
          total += count;
          tsv.write(type, message, count);
        }
      }
      tsv.write(null, "Total", total);
    }
  }

}
