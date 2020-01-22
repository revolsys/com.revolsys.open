package com.revolsys.swing.map.print;

import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;

import javax.measure.Unit;
import javax.measure.quantity.Length;

import org.jeometry.coordinatesystem.model.unit.CustomUnits;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.util.QuantityType;

import tech.units.indriya.quantity.Quantities;
import tech.units.indriya.unit.Units;

public class MapPageable implements Pageable {
  private final Rectangle2D.Double contentRect;

  private double coreCellsPerHeight;

  private double coreCellsPerWidth;

  private final int dpi;

  private final Project map;

  private BoundingBox mapBoundingBox;

  private double modelGridSizeMetres = 200;

  private final double modelPageHeight;

  private final double modelPageWidth;

  private final int numPages;

  private final int numXPages;

  private final int numYPages;

  private final PageFormat pageFormat;

  private final double rulerSizePixels;

  private final double scale;

  public MapPageable(final Project map, final BoundingBox boundingBox, final PageFormat pageFormat,
    final double scale, final int dpi, final double gridSizeMetres) {
    this.map = map;
    this.pageFormat = pageFormat;
    this.scale = scale;
    this.dpi = dpi;
    this.modelGridSizeMetres = gridSizeMetres;
    final Unit<Length> pixelSize = CustomUnits.INCH.divide(dpi);

    this.mapBoundingBox = boundingBox;

    this.rulerSizePixels = QuantityType
      .doubleValue(Quantities.getQuantity(0.5, CustomUnits.CENTIMETRE), pixelSize);

    final double modelWidthMetres = QuantityType.doubleValue(this.mapBoundingBox.getWidthLength(),
      Units.METRE);
    final double modelHeightMetres = QuantityType.doubleValue(this.mapBoundingBox.getWidthLength(),
      Units.METRE);

    final double printWidthPixels = pageFormat.getImageableWidth() * dpi / 72;
    final double printHeightPixels = pageFormat.getImageableHeight() * dpi / 72;

    final double widthPixels = printWidthPixels - 2 * this.rulerSizePixels;
    final double heightPixels = printHeightPixels - 3 * this.rulerSizePixels;

    final double pageWidthMetres = QuantityType
      .doubleValue(Quantities.getQuantity(widthPixels, pixelSize), Units.METRE);
    final double pageHeightMetres = QuantityType
      .doubleValue(Quantities.getQuantity(heightPixels, pixelSize), Units.METRE);

    this.modelPageWidth = pageWidthMetres * scale;
    this.modelPageHeight = pageHeightMetres * scale;
    final double cellsPerWidth = Math.ceil(pageWidthMetres * scale / this.modelGridSizeMetres);
    final double cellsPerHeight = Math.ceil(pageHeightMetres * scale / this.modelGridSizeMetres);

    final double numXCells = Math.ceil(modelWidthMetres / this.modelGridSizeMetres);
    final double numYCells = Math.ceil(modelHeightMetres / this.modelGridSizeMetres);

    if (numXCells <= cellsPerWidth - 1) {
      this.coreCellsPerWidth = cellsPerWidth - 1;
    } else {
      this.coreCellsPerWidth = cellsPerWidth - 2;
    }
    if (numYCells <= cellsPerHeight - 1) {
      this.coreCellsPerHeight = cellsPerHeight - 1;
    } else {
      this.coreCellsPerHeight = cellsPerHeight - 2;
    }
    this.numXPages = (int)Math.ceil(numXCells / this.coreCellsPerWidth);
    this.numYPages = (int)Math.ceil(numYCells / this.coreCellsPerHeight);

    final double totalModelWidth = this.numXPages * this.coreCellsPerWidth
      * this.modelGridSizeMetres;
    if (this.mapBoundingBox.getWidth() < totalModelWidth) {
      final double deltaX = (totalModelWidth - this.mapBoundingBox.getWidth()) / 2;
      this.mapBoundingBox = this.mapBoundingBox.bboxEdit(editor -> editor.expandDeltaX(deltaX));
    }
    final double totalModelHeight = this.numYPages * this.coreCellsPerHeight
      * this.modelGridSizeMetres;
    if (this.mapBoundingBox.getHeight() < totalModelHeight) {
      final double deltaY = (totalModelHeight - this.mapBoundingBox.getHeight()) / 2;
      this.mapBoundingBox = this.mapBoundingBox.bboxEdit(editor -> editor.expandDeltaY(deltaY));
    }

    this.numPages = this.numXPages * this.numYPages;
    this.contentRect = new Rectangle2D.Double(this.rulerSizePixels, this.rulerSizePixels,
      widthPixels, heightPixels);
  }

  @Override
  public int getNumberOfPages() {
    return this.numPages;
  }

  @Override
  public PageFormat getPageFormat(final int pageIndex) {
    return this.pageFormat;
  }

  @Override
  public Printable getPrintable(final int pageIndex) {
    final double pageXOffset = (this.modelPageWidth
      - this.coreCellsPerWidth * this.modelGridSizeMetres) / 2;
    final double pageYOffset = (this.modelPageHeight
      - this.coreCellsPerHeight * this.modelGridSizeMetres) / 2;
    final double startX = Math.floor(this.mapBoundingBox.getMinX() / this.modelGridSizeMetres)
      * this.modelGridSizeMetres;
    final double startY = Math.floor(this.mapBoundingBox.getMinY() / this.modelGridSizeMetres)
      * this.modelGridSizeMetres;
    final int column = pageIndex % this.numXPages;
    final int row = this.numYPages - 1 - (int)Math.floor((double)pageIndex / this.numXPages);
    final double x = startX + column * this.modelGridSizeMetres * this.coreCellsPerWidth
      - pageXOffset;
    final double y = startY + row * this.modelGridSizeMetres * this.coreCellsPerHeight
      - pageYOffset;
    final BoundingBox pageBoundingBox = this.map.getGeometryFactory()
      .newBoundingBox(x, y, x + this.modelPageWidth, y + this.modelPageHeight);
    return new MapPrintable(this.map, column, this.numYPages - row, pageBoundingBox,
      this.contentRect, this.dpi, this.rulerSizePixels, this.modelGridSizeMetres, this.scale);
  }

}
