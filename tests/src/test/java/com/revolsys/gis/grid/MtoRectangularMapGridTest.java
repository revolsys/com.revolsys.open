package com.revolsys.gis.grid;

public class MtoRectangularMapGridTest extends Nts50000RectangularMapGridTest {
  private static final RectangularMapGrid GRID = new MtoRectangularMapGrid();

  private static final double TILE_HEIGHT = MtoConstants.HEIGHT_QUARTER;

  private static final double TILE_WIDTH = MtoConstants.WIDTH_QUARTER;

  protected void doTestMtoByName(final String parentTileName, final double parentLon,
    final double parentLat) {
    checkTileByName(GRID, parentTileName, parentLon, parentLat, TILE_WIDTH, TILE_HEIGHT);
  }

  @Override
  protected void doTestNts50000(final String parentTileName, final double parentLon,
    final double parentLat) {
    String baseTileName = parentTileName.replaceAll("/", "");
    if (!baseTileName.startsWith("1")) {
      baseTileName = "0" + baseTileName;
    }
    if (baseTileName.length() == 5) {
      baseTileName = baseTileName.substring(0, 4) + "0" + baseTileName.substring(4);
    }
    for (char letter = 'A'; letter <= 'L'; letter++) {
      for (int number = 1; number <= 100; number++) {
        for (char letterQuarter = 'a'; letterQuarter <= 'd'; letterQuarter++) {
          double lon = parentLon;
          lon -= GridUtil.getLetter16Col(letter) * MtoConstants.WIDTH_TWELTH;
          lon -= GridUtil.getNumberCol100(number) * MtoConstants.WIDTH_HUNDRETH;
          lon -= GridUtil.getLetter4Col(letterQuarter) * MtoConstants.WIDTH_QUARTER;

          double lat = parentLat;
          lat += GridUtil.getLetter16Row(letter) * MtoConstants.HEIGHT_TWELTH;
          lat += GridUtil.getNumberRow100(number) * MtoConstants.HEIGHT_HUNDRETH;
          lat += GridUtil.getLetter4Row(letterQuarter) * MtoConstants.HEIGHT_QUARTER;

          final String tileName = baseTileName + letter + GridUtil.formatSheetNumber100(number)
            + letterQuarter;

          doTestMtoByName(tileName, lon, lat);
        }
      }
    }

  }
}
