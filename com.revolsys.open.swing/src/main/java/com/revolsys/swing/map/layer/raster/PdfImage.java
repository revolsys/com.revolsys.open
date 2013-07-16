package com.revolsys.swing.map.layer.raster;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.spring.SpringUtil;
import com.revolsys.swing.PdfUtil;

public class PdfImage extends GeoReferencedImage {

  public PdfImage(final Resource imageResource) {
    super(imageResource);
  }

  @Override
  protected BufferedImage createBufferedImage() {
    final Resource imageResource = getImageResource();
    try {
      final File file = SpringUtil.getOrDownloadFile(imageResource);
      // TODO password support
      final PDDocument document = PDDocument.loadNonSeq(file, null, null);

      @SuppressWarnings("unchecked")
      final List<PDPage> pages = document.getDocumentCatalog().getAllPages();
      if (pages.isEmpty()) {
        throw new RuntimeException("PDF file " + imageResource
          + " doesn't contain any pages");
      } else {
        if (pages.size() > 1) {
          LoggerFactory.getLogger(getClass()).warn(
            "PDF file " + imageResource + " doesn't contain any pages");
        }
        final PDPage page = pages.get(0);
        final COSDictionary pageDictionary = page.getCOSDictionary();
        final Rectangle2D mediaBox = PdfUtil.getPageMediaBox(pageDictionary);
        final int resolution = 144;
        final double scaleFactor = resolution / 72;
        BufferedImage image = page.convertToImage(BufferedImage.TYPE_INT_ARGB,
          resolution);

        final COSDictionary viewport = PdfUtil.getPageViewport(pageDictionary);
        if (viewport != null) {
          final Rectangle2D bbox = PdfUtil.getBBox(viewport);
          if (bbox != null) {
            final int width = (int)(bbox.getWidth() * scaleFactor);
            final int height = (int)(bbox.getHeight() * scaleFactor);
            final BufferedImage viewportImage = new BufferedImage(width,
              height, BufferedImage.TYPE_INT_ARGB);
            final Graphics2D graphics = (Graphics2D)viewportImage.getGraphics();
            graphics.translate(-bbox.getX() * scaleFactor,
              -(mediaBox.getHeight() - (bbox.getHeight() + bbox.getY()))
                * scaleFactor);
            graphics.scale(1, 1);
            graphics.drawImage(image, 0, 0, null);
            image = viewportImage;
          }
          final BoundingBox boundingBox = PdfUtil.getViewportBoundingBox(
            mediaBox, viewport);
          setBoundingBox(boundingBox);
        }
        return image;
      }
    } catch (final IOException e) {
      throw new RuntimeException("Error loading PDF file " + imageResource, e);
    }
  }

  @Override
  protected void loadImageMetaData() {
    final Resource resource = getImageResource();
    final BoundingBox boundingBox = getBoundingBox();
    if (boundingBox == null || boundingBox.isNull()) {
      loadProjectionFile(resource);
      loadWorldFile(resource, "pfw");
    }
  }
}
