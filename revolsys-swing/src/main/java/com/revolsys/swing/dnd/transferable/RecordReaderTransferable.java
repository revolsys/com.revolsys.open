package com.revolsys.swing.dnd.transferable;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.io.ListRecordReader;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.format.csv.Csv;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.map.layer.record.LayerRecord;

public class RecordReaderTransferable implements Transferable {
  public static final DataFlavor RECORD_READER_FLAVOR = new DataFlavor(RecordReader.class,
    "Record Reader");

  private static final DataFlavor[] DATA_FLAVORS = {
    RECORD_READER_FLAVOR, DataFlavor.stringFlavor
  };

  private final RecordDefinition recordDefinition;

  private final List<Record> records;

  public RecordReaderTransferable(final RecordDefinition recordDefinition,
    final List<LayerRecord> records) {
    this.recordDefinition = recordDefinition;
    this.records = new ArrayList<>();
    for (final LayerRecord record : records) {
      final ArrayRecord recordCopy = new ArrayRecord(recordDefinition, record);
      this.records.add(recordCopy);
    }

  }

  @Override
  public Object getTransferData(final DataFlavor flavor)
    throws UnsupportedFlavorException, IOException {
    if (this.records.isEmpty()) {
      return null;
    } else if (RECORD_READER_FLAVOR.equals(flavor) || MapTransferable.MAP_FLAVOR.equals(flavor)) {
      return new ListRecordReader(this.recordDefinition, this.records);
    } else if (DataFlavor.stringFlavor.equals(flavor)) {
      final StringWriter out = new StringWriter();
      final RecordDefinition recordDefinition = this.recordDefinition;
      if (recordDefinition != null) {
        final Collection<String> fieldNames = recordDefinition.getFieldNames();
        Csv.writeColumns(out, fieldNames, '\t', '\n');
        for (final Record record : this.records) {
          if (record != null) {
            final Collection<Object> values = record.values();
            Csv.writeColumns(out, values, '\t', '\n');
          }
        }
      }
      final String text = out.toString();
      return text;
    } else {
      throw new UnsupportedFlavorException(flavor);
    }
  }

  @Override
  public DataFlavor[] getTransferDataFlavors() {
    return DATA_FLAVORS;
  }

  @Override
  public boolean isDataFlavorSupported(final DataFlavor dataFlavor) {
    return Arrays.asList(DATA_FLAVORS).contains(dataFlavor);
  }

}
