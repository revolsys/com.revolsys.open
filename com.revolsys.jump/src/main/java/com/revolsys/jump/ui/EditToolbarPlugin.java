package com.revolsys.jump.ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.openjump.core.ui.plugin.edittoolbox.cursortools.ScaleSelectedItemsTool;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.WorkbenchToolBar;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.NodeLineStringsTool;
import com.vividsolutions.jump.workbench.ui.cursortool.QuasimodeTool;
import com.vividsolutions.jump.workbench.ui.cursortool.SelectFeaturesTool;
import com.vividsolutions.jump.workbench.ui.cursortool.SelectLineStringsTool;
import com.vividsolutions.jump.workbench.ui.cursortool.SelectPartsTool;
import com.vividsolutions.jump.workbench.ui.cursortool.SplitLineStringTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.DeleteVertexTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.DrawLineStringTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.DrawPointTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.DrawPolygonTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.DrawRectangleTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.InsertVertexTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.MoveSelectedItemsTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.MoveVertexTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.SnapVerticesToSelectedVertexTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.SnapVerticesTool;

public class EditToolbarPlugin extends AbstractPlugIn {
  private WorkbenchToolBar toolBar;

  @Override
  public void initialize(final PlugInContext context) throws Exception {
    WorkbenchContext workbenchContext = context.getWorkbenchContext();
    toolBar = new WorkbenchToolBar(workbenchContext);
    EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
    // Null out the quasimodes for [Ctrl] because the Select tools will handle
    // that case. [Jon Aquino]
    add(new QuasimodeTool(new SelectFeaturesTool()).add(
      new QuasimodeTool.ModifierKeySpec(true, false, false), null));
    add(new QuasimodeTool(new SelectPartsTool()).add(
      new QuasimodeTool.ModifierKeySpec(true, false, false), null));
    add(new QuasimodeTool(new SelectLineStringsTool()).add(
      new QuasimodeTool.ModifierKeySpec(true, false, false), null));
    add(new MoveSelectedItemsTool(checkFactory));
    toolBar.addSeparator();
    add(DrawRectangleTool.create(workbenchContext));
    add(DrawPolygonTool.create(workbenchContext));
    add(DrawLineStringTool.create(workbenchContext));
    add(DrawPointTool.create(workbenchContext));

    toolBar.addSeparator();
    add(new InsertVertexTool(checkFactory));
    add(new DeleteVertexTool(checkFactory));
    add(new MoveVertexTool(checkFactory));
    // -- [sstein: 11.12.2006] added here to fill toolBar
    add(new ScaleSelectedItemsTool(checkFactory));

    toolBar.addSeparator();
    add(new SnapVerticesTool(checkFactory));
    add(new SnapVerticesToSelectedVertexTool(checkFactory));
    add(new SplitLineStringTool());
    add(new NodeLineStringsTool());

    JToolBar mainToolBar = context.getWorkbenchFrame().getToolBar();
    JPanel tools = new JPanel(new BorderLayout());
    tools.add(mainToolBar, BorderLayout.NORTH);
    tools.add(toolBar, BorderLayout.SOUTH);
    context.getWorkbenchFrame().getContentPane().add(tools, BorderLayout.NORTH);
  }

  public WorkbenchToolBar.ToolConfig add(final CursorTool tool) {
    return add(tool, null);
  }

  /**
   * @param enableCheck null to leave unspecified
   */
  public WorkbenchToolBar.ToolConfig add(final CursorTool tool,
    final EnableCheck enableCheck) {
    WorkbenchToolBar.ToolConfig config = toolBar.addCursorTool(tool);
    JToggleButton button = config.getButton();
    if (enableCheck != null) {
      toolBar.setEnableCheck(button, enableCheck);
    } else {
      toolBar.setEnableCheck(button, new MultiEnableCheck());
    }
    return config;
  }

}
