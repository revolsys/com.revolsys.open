package com.revolsys.jtstest.testbuilder.model;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import com.revolsys.jts.geom.Geometry;

public class GeometryTransferable implements Transferable {
  public static final DataFlavor GEOMETRY_FLAVOR = new DataFlavor(
    Geometry.class, "Geometry");

  private final Geometry geom;

  private boolean isFormatted;

  private static final DataFlavor[] flavors = {
    DataFlavor.stringFlavor, GEOMETRY_FLAVOR
  };

  public GeometryTransferable(final Geometry geom) {
    this.geom = geom;
  }

  public GeometryTransferable(final Geometry geom, final boolean isFormatted) {
    this.geom = geom;
    this.isFormatted = isFormatted;
  }

  @Override
  public Object getTransferData(final DataFlavor flavor)
    throws UnsupportedFlavorException, IOException {
    if (flavor.equals(GEOMETRY_FLAVOR)) {
      return geom;
    }
    if (flavor.equals(DataFlavor.stringFlavor)) {
      if (isFormatted) {
        final String wkt = geom.toWkt();
        return wkt;
      }
      return geom.toString();
    }
    throw new UnsupportedFlavorException(flavor);

  }

  @Override
  public DataFlavor[] getTransferDataFlavors() {
    return flavors;
  }

  @Override
  public boolean isDataFlavorSupported(final DataFlavor flavor) {
    for (int i = 0; i < flavors.length; i++) {
      if (flavor.equals(flavors[i])) {
        return true;
      }
    }
    return false;
  }
}
