package com.revolsys.swing.map.view.pdf;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.xml.XmpSerializer;

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
        boundingBox = boundingBox.bboxEdit(editor -> editor
          .setGeometryFactory(viewport.getGeometryFactory().getGeographicGeometryFactory()));
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
      final XMPMetadata xmpMetadata = XMPMetadata.createXMPMetadata();
      final DublinCoreSchema dcSchema = xmpMetadata.createAndAddDublinCoreSchema();

      dcSchema.setAboutAsSimple("");

      final XmpSerializer serializer = new XmpSerializer();
      final ByteArrayOutputStream baos = new ByteArrayOutputStream();
      serializer.serialize(xmpMetadata, baos, false);
      metadata.importXMPMetadata(baos.toByteArray());

      document.save(file);
    } catch (final Throwable e) {
      Logs.error(SaveAsPdf.class, "Unable to create PDF " + file, e);
    }
  }
}
