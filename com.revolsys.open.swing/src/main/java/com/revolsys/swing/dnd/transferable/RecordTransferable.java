package com.revolsys.swing.dnd.transferable;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;

import com.revolsys.record.Record;
import com.revolsys.record.io.format.csv.Csv;

public class RecordTransferable implements Transferable {
  public static final DataFlavor DATA_OBJECT_FLAVOR = new DataFlavor(Record.class, "Data Object");

  private static final DataFlavor[] DATA_FLAVORS = {
    DATA_OBJECT_FLAVOR, MapTransferable.MAP_FLAVOR, DataFlavor.stringFlavor
  };

  private final Record object;

  public RecordTransferable(final Record object) {
    this.object = object;
  }

  @Override
  public Object getTransferData(final DataFlavor flavor)
    throws UnsupportedFlavorException, IOException {
    if (this.object == null) {
      return null;
    } else if (DATA_OBJECT_FLAVOR.equals(flavor) || MapTransferable.MAP_FLAVOR.equals(flavor)) {
      return this.object;
    } else if (DataFlavor.stringFlavor.equals(flavor)) {
      final StringWriter out = new StringWriter();
      final Collection<String> fieldNames = this.object.getRecordDefinition().getFieldNames();
      Csv.writeColumns(out, fieldNames, '\t', '\n');
      final Collection<Object> values = this.object.values();
      Csv.writeColumns(out, values, '\t', '\n');
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
