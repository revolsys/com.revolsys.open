package com.revolsys.swing.map.print;

import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.swing.map.layer.Project;

public class MapPageable implements Pageable {

  private final int numPages;

  private final PageFormat pageFormat;

  private BoundingBox mapBoundingBox;

  private final Project map;

  private double modelGridSizeMetres = 200;

  private final int numXPages;

  private final int numYPages;

  private double coreCellsPerWidth;

  private double coreCellsPerHeight;

  private final double modelPageWidth;

  private final double modelPageHeight;

  private final double rulerSizePixels;

  private final Rectangle2D.Double contentRect;

  private final int dpi;

  private final double scale;

  public MapPageable(final Project map, final BoundingBox boundingBox,
    final PageFormat pageFormat, final double scale, final int dpi,
    final double gridSizeMetres) {
    this.map = map;
    this.pageFormat = pageFormat;
    this.scale = scale;
    this.dpi = dpi;
    this.modelGridSizeMetres = gridSizeMetres;
    final Unit<Length> pixelSize = NonSI.INCH.divide(dpi);

    mapBoundingBox = boundingBox;

    rulerSizePixels = Measure.valueOf(0.5, SI.CENTIMETRE)
      .doubleValue(pixelSize);

    final double modelWidthMetres = mapBoundingBox.getWidthLength()
      .doubleValue(SI.METRE);
    final double modelHeightMetres = mapBoundingBox.getWidthLength()
      .doubleValue(SI.METRE);

    final double printWidthPixels = pageFormat.getImageableWidth() * dpi / 72;
    final double printHeightPixels = pageFormat.getImageableHeight() * dpi / 72;

    final double widthPixels = printWidthPixels - 2 * rulerSizePixels;
    final double heightPixels = printHeightPixels - 3 * rulerSizePixels;

    final double pageWidthMetres = Measure.valueOf(widthPixels, pixelSize)
      .doubleValue(SI.METRE);
    final double pageHeightMetres = Measure.valueOf(heightPixels, pixelSize)
      .doubleValue(SI.METRE);

    modelPageWidth = pageWidthMetres * scale;
    modelPageHeight = pageHeightMetres * scale;
    final double cellsPerWidth = Math.ceil(pageWidthMetres * scale
      / modelGridSizeMetres);
    final double cellsPerHeight = Math.ceil(pageHeightMetres * scale
      / modelGridSizeMetres);

    final double numXCells = Math.ceil(modelWidthMetres / modelGridSizeMetres);
    final double numYCells = Math.ceil(modelHeightMetres / modelGridSizeMetres);

    if (numXCells <= cellsPerWidth - 1) {
      coreCellsPerWidth = cellsPerWidth - 1;
    } else {
      coreCellsPerWidth = cellsPerWidth - 2;
    }
    if (numYCells <= cellsPerHeight - 1) {
      coreCellsPerHeight = cellsPerHeight - 1;
    } else {
      coreCellsPerHeight = cellsPerHeight - 2;
    }
    this.numXPages = (int)Math.ceil(numXCells / coreCellsPerWidth);
    this.numYPages = (int)Math.ceil(numYCells / coreCellsPerHeight);

    final double totalModelWidth = numXPages * coreCellsPerWidth
      * modelGridSizeMetres;
    if (mapBoundingBox.getWidth() < totalModelWidth) {
      final double expandDistance = (totalModelWidth - mapBoundingBox.getWidth()) / 2;
      mapBoundingBox = mapBoundingBox.expand(expandDistance, 0);
    }
    final double totalModelHeight = numYPages * coreCellsPerHeight
      * modelGridSizeMetres;
    if (mapBoundingBox.getHeight() < totalModelHeight) {
      final double expandDistance = (totalModelHeight - mapBoundingBox.getHeight()) / 2;
      mapBoundingBox = mapBoundingBox.expand(0, expandDistance);
    }

    numPages = numXPages * numYPages;
    contentRect = new Rectangle2D.Double(rulerSizePixels, rulerSizePixels,
      widthPixels, heightPixels);
  }

  @Override
  public int getNumberOfPages() {
    return numPages;
  }

  @Override
  public PageFormat getPageFormat(final int pageIndex) {
    return pageFormat;
  }

  @Override
  public Printable getPrintable(final int pageIndex) {
    final double pageXOffset = (modelPageWidth - coreCellsPerWidth
      * modelGridSizeMetres) / 2;
    final double pageYOffset = (modelPageHeight - coreCellsPerHeight
      * modelGridSizeMetres) / 2;
    final double startX = Math.floor(mapBoundingBox.getMinX()
      / modelGridSizeMetres)
      * modelGridSizeMetres;
    final double startY = Math.floor(mapBoundingBox.getMinY()
      / modelGridSizeMetres)
      * modelGridSizeMetres;
    final int column = pageIndex % numXPages;
    final int row = numYPages - 1
      - (int)Math.floor((double)pageIndex / numXPages);
    final double x = startX + column * modelGridSizeMetres * coreCellsPerWidth
      - pageXOffset;
    final double y = startY + row * modelGridSizeMetres * coreCellsPerHeight
      - pageYOffset;
    final BoundingBox pageBoundingBox = new BoundingBoxDoubleGf(map.getGeometryFactory(),
      2, x, y, x + modelPageWidth, y + modelPageHeight);
    return new MapPrintable(map, column, numYPages - row, pageBoundingBox,
      contentRect, dpi, rulerSizePixels, modelGridSizeMetres, scale);
  }

}
