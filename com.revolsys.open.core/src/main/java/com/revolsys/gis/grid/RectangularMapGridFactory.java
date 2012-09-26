package com.revolsys.gis.grid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class RectangularMapGridFactory {
  public static final String[] GRID_NAMES;

  private static final Map<String, RectangularMapGrid> GRIDS_BY_NAME = new LinkedHashMap<String, RectangularMapGrid>();

  static {
    GRIDS_BY_NAME.put("NTS 1:1 000 000", new Nts1000000RectangularMapGrid());
    GRIDS_BY_NAME.put("NTS 1:500 000", new Nts500000RectangularMapGrid());
    GRIDS_BY_NAME.put("NTS 1:250 000", new Nts250000RectangularMapGrid());
    GRIDS_BY_NAME.put("NTS 1:125 000", new Nts125000RectangularMapGrid());
    GRIDS_BY_NAME.put("NTS 1:50 000", new Nts50000RectangularMapGrid());
    GRIDS_BY_NAME.put("NTS 1:25 000", new Nts25000RectangularMapGrid());
    GRIDS_BY_NAME.put("BCGS 1:20 000", new Bcgs20000RectangularMapGrid());
    GRIDS_BY_NAME.put("BCGS 1:10 000", new Bcgs10000RectangularMapGrid());
    GRIDS_BY_NAME.put("BCGS 1:5 000", new Bcgs5000RectangularMapGrid());
    GRIDS_BY_NAME.put("BCGS 1:2 500", new Bcgs2500RectangularMapGrid());
    GRIDS_BY_NAME.put("BCGS 1:2 000", new Bcgs2000RectangularMapGrid());
    GRIDS_BY_NAME.put("BCGS 1:1 250", new Bcgs1250RectangularMapGrid());
    GRIDS_BY_NAME.put("BCGS 1:1 000", new Bcgs1000RectangularMapGrid());
    GRIDS_BY_NAME.put("BCGS 1:500", new Bcgs500RectangularMapGrid());
    GRIDS_BY_NAME.put("MTO", new MtoRectangularMapGrid());
    GRID_NAMES = new ArrayList<String>(GRIDS_BY_NAME.keySet()).toArray(new String[0]);
  }

  public static RectangularMapGrid getGrid(final String name) {
    return GRIDS_BY_NAME.get(name);
  }

  public static RectangularMapGrid getGrid(final String name,
    final int inverseScale) {
    if (name.equals("NTS")) {
      switch (inverseScale) {
        case 1000000:
          return new Nts1000000RectangularMapGrid();
        case 500000:
          return new Nts500000RectangularMapGrid();
        case 250000:
          return new Nts250000RectangularMapGrid();
        case 125000:
          return new Nts125000RectangularMapGrid();
        case 50000:
          return new Nts50000RectangularMapGrid();
        case 25000:
          return new Nts25000RectangularMapGrid();
        default:
          return null;
      }
    } else if (name.equals("BCGS")) {
      switch (inverseScale) {
        case 20000:
          return new Bcgs20000RectangularMapGrid();
        case 10000:
          return new Bcgs10000RectangularMapGrid();
        case 5000:
          return new Bcgs5000RectangularMapGrid();
        case 2500:
          return new Bcgs2500RectangularMapGrid();
        case 2000:
          return new Bcgs2000RectangularMapGrid();
        case 1250:
          return new Bcgs1250RectangularMapGrid();
        case 1000:
          return new Bcgs1000RectangularMapGrid();
        case 500:
          return new Bcgs500RectangularMapGrid();
        default:
          return null;
      }
    } else if (name.equals("MTO")) {
      switch (inverseScale) {
        case 0:
          return new MtoRectangularMapGrid();
        default:
          return null;
      }
    }
    return null;
  }

  public static Collection<String> getGridNames() {
    return GRIDS_BY_NAME.keySet();
  }
}
