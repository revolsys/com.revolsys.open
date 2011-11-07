package com.revolsys.gis.grid;

public class Nts50000RectangularMapGridTest extends
  Nts250000RectangularMapGridTest {
  private static final RectangularMapGrid GRID = new Nts50000RectangularMapGrid();

  private static final double TILE_HEIGHT = NtsConstants.HEIGHT_50000;

  private static final double TILE_WIDTH = NtsConstants.WIDTH_50000;

  @Override
  protected void doTestNts250000ByName(
    final String parentTileName,
    final double parentLon,
    final double parentLat) {
    for (int number = 1; number <= 16; number++) {
      final double lat = parentLat + GridUtil.getNumberRow16(number)
        * TILE_HEIGHT;
      final double lon = parentLon - GridUtil.getNumberCol16(number)
        * TILE_WIDTH;

      final String tileName = parentTileName + "/" + number;

      doTestNts50000(tileName, lon, lat);
    }
  }

  protected void doTestNts50000(
    final String parentTileName,
    final double parentLon,
    final double parentLat) {
    checkTileByName(GRID, parentTileName, parentLon, parentLat, TILE_WIDTH,
      TILE_HEIGHT);
  }
}
