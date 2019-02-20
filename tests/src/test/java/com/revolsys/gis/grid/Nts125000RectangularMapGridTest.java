package com.revolsys.gis.grid;

public class Nts125000RectangularMapGridTest extends Nts250000RectangularMapGridTest {
  private static final RectangularMapGrid GRID = new Nts125000RectangularMapGrid();

  private static final double TILE_HEIGHT = NtsConstants.HEIGHT_125000;

  private static final double TILE_WIDTH = NtsConstants.WIDTH_125000;

  @Override
  protected void doTestNts250000ByName(final String parentTileName, final double parentLon,
    final double parentLat) {
    for (int v = 0; v <= 1; v++) {
      double lat = parentLat;
      char northSouth;
      if (v == 0) {
        northSouth = 'S';
      } else {
        lat += TILE_HEIGHT;
        northSouth = 'N';
      }
      for (int h = 0; h <= 1; h++) {
        double lon = parentLon;
        char eastWest;
        if (h == 1) {
          eastWest = 'W';
          lon -= TILE_WIDTH;
        } else {
          eastWest = 'E';
        }
        final String tileName = parentTileName + "/" + northSouth + "." + eastWest + ".";

        checkTileByName(GRID, tileName, lon, lat, TILE_WIDTH, TILE_HEIGHT);
      }
    }

  }
}
