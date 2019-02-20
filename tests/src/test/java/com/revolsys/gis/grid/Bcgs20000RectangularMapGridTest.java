package com.revolsys.gis.grid;

public class Bcgs20000RectangularMapGridTest extends Nts250000RectangularMapGridTest {
  private static final RectangularMapGrid GRID = new Bcgs20000RectangularMapGrid();

  private static final double TILE_HEIGHT = BcgsConstants.HEIGHT_20000;

  private static final double TILE_WIDTH = BcgsConstants.WIDTH_20000;

  protected void doTestBcgs20000ByName(final String parentTileName, final double parentLon,
    final double parentLat) {
    checkTileByName(GRID, parentTileName, parentLon, parentLat, TILE_WIDTH, TILE_HEIGHT);
  }

  @Override
  protected void doTestNts250000ByName(final String parentTileName, final double parentLon,
    final double parentLat) {
    for (int number = 1; number <= 100; number++) {
      final double lon = parentLon - GridUtil.getNumberCol100(number) * TILE_WIDTH;
      final double lat = parentLat + GridUtil.getNumberRow100(number) * TILE_HEIGHT;

      final String tileName = parentTileName + "." + GridUtil.formatSheetNumber100(number);

      doTestBcgs20000ByName(tileName, lon, lat);
    }
  }
}
