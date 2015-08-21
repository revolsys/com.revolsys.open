package com.revolsys.format.pdf;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.slf4j.LoggerFactory;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.raster.JaiGeoreferencedImage;
import com.revolsys.spring.resource.Resource;
import com.revolsys.spring.resource.SpringUtil;

public class PdfImage extends JaiGeoreferencedImage {

  public PdfImage(final Resource imageResource) {
    setImageResource(imageResource);
    setRenderedImage(createBufferedImage());
    if (!hasGeometryFactory()) {
      loadProjectionFile();
    }
    if (!hasBoundingBox()) {
      loadWorldFile();
    }
  }

  protected BufferedImage createBufferedImage() {
    final Resource imageResource = getImageResource();
    try {
      final File file = SpringUtil.getOrDownloadFile(imageResource);
      // TODO password support
      final PDDocument document = PDDocument.loadNonSeq(file, null, null);

      @SuppressWarnings("unchecked")
      final List<PDPage> pages = document.getDocumentCatalog().getAllPages();
      if (pages.isEmpty()) {
        throw new RuntimeException("PDF file " + imageResource + " doesn't contain any pages");
      } else {
        if (pages.size() > 1) {
          LoggerFactory.getLogger(getClass())
            .warn("PDF file " + imageResource + " doesn't contais more than 1 page");
        }
        final PDPage page = pages.get(0);
        final COSDictionary pageDictionary = page.getCOSDictionary();
        final Rectangle2D mediaBox = PdfUtil.findRectangle(pageDictionary, COSName.MEDIA_BOX);
        final int resolution = 72;
        BufferedImage image = page.convertToImage(BufferedImage.TYPE_INT_ARGB, resolution);
        final COSDictionary viewport = PdfUtil.getPageViewport(pageDictionary);
        if (viewport != null) {
          final Rectangle2D bbox = PdfUtil.findRectangle(viewport, COSName.BBOX);
          if (bbox != null) {
            final double boxX = bbox.getX();
            final double boxY = bbox.getY();
            final int boxWidth = (int)bbox.getWidth();
            final int boxHeight = (int)bbox.getHeight();
            final BufferedImage viewportImage = new BufferedImage(boxWidth, boxHeight,
              BufferedImage.TYPE_INT_ARGB);
            final Graphics2D graphics = (Graphics2D)viewportImage.getGraphics();
            final double translateY = -(mediaBox.getHeight() - (boxHeight + boxY));
            graphics.translate(-boxX, translateY);
            graphics.scale(1, 1);
            graphics.drawImage(image, 0, 0, null);
            graphics.dispose();
            image = viewportImage;
          }
          final BoundingBox boundingBox = PdfUtil.getViewportBoundingBox(viewport);
          setBoundingBox(boundingBox);
          setResolution(boundingBox.getWidth() / image.getWidth());
        }
        return image;
      }
    } catch (final IOException e) {
      throw new RuntimeException("Error loading PDF file " + imageResource, e);
    }
  }

  @Override
  public String getWorldFileExtension() {
    return "pfw";
  }
}
