package com.revolsys.gis.desktop.print;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.parallel.Invoke;

public class SinglePage extends Viewport2D implements Pageable, Printable {

  public static void print() {
    final Project project = Project.get();
    final MapPanel mapPanel = MapPanel.get(project);
    final Viewport2D viewport = mapPanel.getViewport();
    final int viewWidth = viewport.getViewWidthPixels();
    final int viewHeight = viewport.getViewHeightPixels();
    final BoundingBox boundingBox = viewport.getBoundingBox();

    final PrinterJob job = PrinterJob.getPrinterJob();
    job.setJobName(project.getName());
    final PageFormat format = job.defaultPage();
    if (boundingBox.getAspectRatio() > 1) {
      format.setOrientation(PageFormat.LANDSCAPE);
    } else {
      format.setOrientation(PageFormat.PORTRAIT);
    }

    final SinglePage pageable = new SinglePage(project, boundingBox, viewWidth,
      viewHeight);
    job.setPageable(pageable);
    final boolean doPrint = job.printDialog();
    if (doPrint) {
      Invoke.background("Print", job, "print");
    }
  }

  private Graphics2D graphics;

  public SinglePage(final Project project, final BoundingBox boundingBox,
    final int viewWidth, final int viewHeight) {
    super(project, viewWidth, viewHeight, boundingBox);
  }

  @Override
  public Graphics2D getGraphics() {
    return this.graphics;
  }

  @Override
  public int getNumberOfPages() {
    return 1;
  }

  @Override
  public PageFormat getPageFormat(final int pageIndex) {
    final PageFormat pageFormat = new PageFormat();
    final Paper paper = new Paper();
    final int viewWidth = getViewWidthPixels();
    final int viewHeight = getViewHeightPixels();
    double width;
    double height;
    if (viewWidth > viewHeight) {
      width = viewHeight;
      height = viewWidth;
      pageFormat.setOrientation(PageFormat.LANDSCAPE);
    } else {
      width = viewWidth;
      height = viewHeight;
    }
    paper.setSize(width, height);
    paper.setImageableArea(0, 0, width, height);
    pageFormat.setPaper(paper);
    return pageFormat;
  }

  @Override
  protected double getPixelsPerYUnit(final double viewHeight,
    final double mapHeight) {
    return -viewHeight / mapHeight;
  }

  @Override
  public Printable getPrintable(final int pageIndex) {
    if (pageIndex == 0) {
      return this;
    } else {
      return null;
    }
  }

  @Override
  public int getScreenResolution() {
    return 72;
  }

  @Override
  public int print(final Graphics graphics, final PageFormat pageFormat,
    final int pageIndex) throws PrinterException {
    if (pageIndex == 0) {
      this.graphics = (Graphics2D)graphics;
      final int translateX = (int)pageFormat.getImageableX();
      final int translateY = (int)pageFormat.getImageableY();
      graphics.translate(translateX - 1, translateY - 1);
      final Project project = getProject();
      final MapPanel mapPanel = MapPanel.get(project);
      final Layer baseMapLayer = mapPanel.getBaseMapLayer();
      render(baseMapLayer);

      render(project);
      return PAGE_EXISTS;
    } else {
      return NO_SUCH_PAGE;
    }
  }

}
