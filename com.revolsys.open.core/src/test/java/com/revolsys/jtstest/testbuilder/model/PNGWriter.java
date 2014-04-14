
/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.jtstest.testbuilder.model;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.PrecisionModel;
import com.revolsys.jts.util.Assert;
import com.revolsys.jtstest.testbuilder.GeometryEditPanel;

/**
 *  Creates an .PNG file for a test case.
 *
 * @version 1.7
 */
public class PNGWriter {
  private final static int IMAGE_WIDTH = 200;

  private final static int IMAGE_HEIGHT = 200;

  private final static int STACK_TRACE_DEPTH = 1;

  private final GeometryEditPanel geometryEditPanel = new GeometryEditPanel();

  private final JFrame frame = new JFrame();

  private File outputDirectory;

  public PNGWriter() {
    geometryEditPanel.setSize(IMAGE_WIDTH, IMAGE_HEIGHT);
    geometryEditPanel.setGridEnabled(false);
    geometryEditPanel.setBorder(BorderFactory.createEmptyBorder());
    frame.getContentPane().add(geometryEditPanel);
  }

  private void createPNGFile(final String filenameNoPath, final Geometry a,
    final Geometry b, final Geometry result, final int imageWidth,
    final int imageHeight) throws FileNotFoundException, IOException {
    final TestBuilderModel tbModel = new TestBuilderModel();
    final TestCaseEdit tc = new TestCaseEdit(new Geometry[] {
      a, b
    });
    tc.setResult(result);
    tbModel.getGeometryEditModel().setTestCase(tc);
    geometryEditPanel.setModel(tbModel);
    geometryEditPanel.zoomToFullExtent();
    geometryEditPanel.setShowingResult(result != null);
    geometryEditPanel.setShowingGeometryA(a != null);
    geometryEditPanel.setShowingGeometryB(b != null);
    final String filenameWithPath = outputDirectory.getPath() + "\\"
      + filenameNoPath;
    final Image image = new BufferedImage(imageWidth, imageHeight,
      BufferedImage.TYPE_4BYTE_ABGR);
    geometryEditPanel.paint(image.getGraphics());

    ImageIO.write((RenderedImage)image, "png", new File(filenameWithPath
      + ".png"));
  }

  public void write(final File outputDirectory, final TestCaseEdit testCase,
    final PrecisionModel precisionModel) throws IOException {
    Assert.isTrue(outputDirectory.isDirectory());
    this.outputDirectory = outputDirectory;
    createPNGFile("geoms", testCase.getGeometry(0), testCase.getGeometry(1),
      testCase.getResult(), IMAGE_WIDTH, IMAGE_HEIGHT);
  }

}
