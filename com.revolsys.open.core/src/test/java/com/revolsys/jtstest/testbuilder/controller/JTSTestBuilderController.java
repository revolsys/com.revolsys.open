package com.revolsys.jtstest.testbuilder.controller;

import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jtstest.testbuilder.GeometryEditPanel;
import com.revolsys.jtstest.testbuilder.JTSTestBuilder;
import com.revolsys.jtstest.testbuilder.JTSTestBuilderFrame;
import com.revolsys.jtstest.testbuilder.model.LayerList;
import com.revolsys.jtstest.testbuilder.ui.SwingUtil;

public class JTSTestBuilderController {
  /*
   * private static boolean autoZoomOnNextChange = false; public static void
   * requestAutoZoom() { autoZoomOnNextChange = true; }
   */

  public static void copyComponentToClipboard(final Coordinates pt) {
    final double toleranceInModel = getGeometryEditPanel().getToleranceInModel();
    final LayerList lyrList = JTSTestBuilderFrame.instance()
      .getModel()
      .getLayers();
    final Geometry comp = lyrList.getComponent(pt, toleranceInModel);
    if (comp == null) {
      return;
    }
    SwingUtil.copyToClipboard(comp, false);
  }

  public static void extractComponentsToTestCase(final Coordinates pt) {
    final double toleranceInModel = getGeometryEditPanel().getToleranceInModel();
    final LayerList lyrList = JTSTestBuilderFrame.instance()
      .getModel()
      .getLayers();
    final Geometry comp = lyrList.getComponent(pt, toleranceInModel);
    if (comp == null) {
      return;
    }
    JTSTestBuilderFrame.instance().getModel().addCase(new Geometry[] {
      comp, null
    });
    JTSTestBuilderFrame.instance().updateTestCases();
  }

  public static void extractComponentsToTestCase(final Geometry aoi) {
    // double toleranceInModel =
    // JTSTestBuilderFrame.getGeometryEditPanel().getToleranceInModel();
    final LayerList lyrList = JTSTestBuilderFrame.instance()
      .getModel()
      .getLayers();
    final Geometry[] comp = lyrList.getComponents(aoi);
    if (comp == null) {
      return;
    }
    JTSTestBuilderFrame.instance().getModel().addCase(comp);
    JTSTestBuilderFrame.instance().updateTestCases();
    JTSTestBuilderFrame.instance().getToolbar().clearToolButtons();
    JTSTestBuilderFrame.instance()
      .getToolbar()
      .unselectExtractComponentButton();
    JTSTestBuilderFrame.instance().getGeometryEditPanel().setCurrentTool(null);
  }

  public static void geometryViewChanged() {
    getGeometryEditPanel().updateView();
    // TODO: provide autoZoom checkbox on Edit tab to control autozooming
    // (default = on)
  }

  public static Geometry getGeometryA() {
    return JTSTestBuilder.model().getGeometryEditModel().getGeometry(0);
  }

  public static Geometry getGeometryB() {
    return JTSTestBuilder.model().getGeometryEditModel().getGeometry(1);
  }

  public static GeometryEditPanel getGeometryEditPanel() {
    return JTSTestBuilderFrame.getGeometryEditPanel();
  }

  public static void zoomToFullExtent() {
    getGeometryEditPanel().zoomToFullExtent();
  }

  public static void zoomToInput() {
    getGeometryEditPanel().zoomToInput();
  }
}
