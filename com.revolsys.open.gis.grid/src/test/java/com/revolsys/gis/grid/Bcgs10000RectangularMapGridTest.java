package com.revolsys.gis.grid;

public class Bcgs10000RectangularMapGridTest extends
  Bcgs20000RectangularMapGridTest {
  private static final RectangularMapGrid GRID = new Bcgs10000RectangularMapGrid();

  private static final double TILE_HEIGHT = BcgsConstants.HEIGHT_10000;

  private static final double TILE_WIDTH = BcgsConstants.WIDTH_10000;

  protected void doTestBcgs10000ByName(
    final String parentTileName,
    final double parentLon,
    final double parentLat) {
    checkTileByName(GRID, parentTileName, parentLon, parentLat, TILE_WIDTH,
      TILE_HEIGHT);
  }

  @Override
  protected void doTestBcgs20000ByName(
    final String parentTileName,
    final double parentLon,
    final double parentLat) {
    for (int number = 1; number <= 4; number++) {
      final double lon = parentLon - GridUtil.getNumberCol4(number)
        * TILE_WIDTH;
      final double lat = parentLat + GridUtil.getNumberRow4(number)
        * TILE_HEIGHT;

      final String tileName = parentTileName + "." + number;

      doTestBcgs10000ByName(tileName, lon, lat);
    }
  }
}
