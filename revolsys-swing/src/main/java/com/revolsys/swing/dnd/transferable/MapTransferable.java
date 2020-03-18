package com.revolsys.swing.dnd.transferable;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import com.revolsys.record.io.format.csv.Csv;

public class MapTransferable implements Transferable {
  public static final DataFlavor MAP_FLAVOR = new DataFlavor(Map.class, "Java Map");

  private static final DataFlavor[] DATA_FLAVORS = {
    MAP_FLAVOR, DataFlavor.stringFlavor
  };

  private final Map<String, Object> map;

  public MapTransferable(final Map<String, Object> map) {
    this.map = map;
  }

  public Map<String, Object> getMap() {
    return this.map;
  }

  @Override
  public Object getTransferData(final DataFlavor flavor)
    throws UnsupportedFlavorException, IOException {
    if (MAP_FLAVOR.equals(flavor)) {
      return this.map;
    } else if (DataFlavor.stringFlavor.equals(flavor)) {
      final StringWriter out = new StringWriter();
      final Collection<String> fieldNames = this.map.keySet();
      Csv.writeColumns(out, fieldNames, '\t', '\n');
      final Collection<Object> values = this.map.values();
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
