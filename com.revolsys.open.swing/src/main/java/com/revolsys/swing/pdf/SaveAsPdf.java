package com.revolsys.swing.pdf;

import java.io.File;
import java.io.InputStream;

import org.apache.jempbox.xmp.XMPMetadata;
import org.apache.jempbox.xmp.XMPSchemaDublinCore;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.Project;

public class SaveAsPdf {

  public static void save() {
    try {
      final PDDocument document = new PDDocument();

      final Project project = Project.get();
      final Viewport2D viewport = MapPanel.get(project).getViewport();
      final BoundingBox boundingBox = viewport.getBoundingBox();
      final int width = viewport.getViewWidthPixels();
      final int height = viewport.getViewHeightPixels();
      final PDRectangle pageSize = new PDRectangle(width, height);
      final PDPage page = new PDPage(pageSize);
      try (
        PdfViewport pdfViewport = new PdfViewport(document, page, project,
          width, height, boundingBox)) {
        final LayerRenderer<? extends Layer> renderer = project.getRenderer();
        renderer.render(pdfViewport);
      }
      document.addPage(page);

      // load the font from pdfbox.jar
      final InputStream fontStream = PDDocument.class.getResourceAsStream("/org/apache/pdfbox/resources/ttf/ArialMT.ttf");
      final PDFont font = PDTrueTypeFont.loadTTF(document, fontStream);

      // // create a page with the message where needed
      // PDPageContentStream contentStream = new PDPageContentStream(document,
      // page);
      // contentStream.beginText();
      // contentStream.setFont( font, 12 );
      // contentStream.moveTextPositionByAmount( 100, 700 );
      // contentStream.drawString( message );
      // contentStream.endText();
      // contentStream.saveGraphicsState();
      // contentStream.close();

      final PDDocumentCatalog catalog = document.getDocumentCatalog();
      final PDMetadata metadata = new PDMetadata(document);
      catalog.setMetadata(metadata);

      // jempbox version
      final XMPMetadata xmp = new XMPMetadata();
      final XMPSchemaDublinCore xmpSchema = new XMPSchemaDublinCore(xmp);
      xmp.addSchema(xmpSchema);
      xmpSchema.setAbout("");
      metadata.importXMPMetadata(xmp);

      // final InputStream colorProfile =
      // PDDocument.class.getResourceAsStream("/com/revolsys/pdf/colorprofile/sRGB.icm");
      // // create output intent
      // final PDOutputIntent oi = new PDOutputIntent(document, colorProfile);
      // oi.setInfo("sRGB IEC61966-2.1");
      // oi.setOutputCondition("sRGB IEC61966-2.1");
      // oi.setOutputConditionIdentifier("sRGB IEC61966-2.1");
      // oi.setRegistryName("http://www.color.org");
      // catalog.addOutputIntent(oi);

      document.save(new File("/Users/paustin/Downloads/map.pdf"));
    } catch (final Throwable e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
