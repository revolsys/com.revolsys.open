package com.revolsys.swing.map.view.pdf;

import java.io.File;

import org.apache.jempbox.xmp.XMPMetadata;
import org.apache.jempbox.xmp.XMPSchemaDublinCore;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.logging.Logs;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.Project;

public class SaveAsPdf {

  public static void save() {
    final Project project = Project.get();
    final String directory = "/Users/paustin/Downloads/";
    final File file = new File(directory, project.getName() + ".pdf");
    try {
      final PDDocument document = new PDDocument();

      final Viewport2D viewport = project.getViewport();
      BoundingBox boundingBox = viewport.getBoundingBox();
      final int width = viewport.getViewWidthPixels();
      final int height = viewport.getViewHeightPixels();

      final int srid = boundingBox.getHorizontalCoordinateSystemId();
      if (srid == 3857) {
        boundingBox = boundingBox
          .convert(boundingBox.getGeometryFactory().getGeographicGeometryFactory());
      }
      final PDRectangle pageSize = new PDRectangle(width, height);
      final PDPage page = new PDPage(pageSize);
      try (
        PdfViewport pdfViewport = new PdfViewport(document, page, project, width, height,
          boundingBox)) {
        final LayerRenderer<? extends Layer> renderer = project.getRenderer();
        renderer.render(pdfViewport.newViewRenderer());
      }
      document.addPage(page);

      final PDDocumentCatalog catalog = document.getDocumentCatalog();
      final PDMetadata metadata = new PDMetadata(document);
      catalog.setMetadata(metadata);

      // jempbox version
      final XMPMetadata xmp = new XMPMetadata();
      final XMPSchemaDublinCore xmpSchema = new XMPSchemaDublinCore(xmp);
      xmp.addSchema(xmpSchema);
      xmpSchema.setAbout("");
      metadata.importXMPMetadata(xmp);

      document.save(file);
    } catch (final Throwable e) {
      Logs.error(SaveAsPdf.class, "Unable to create PDF " + file, e);
    }
  }
}
