/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI
 * for visualizing and manipulating spatial features with geometry and attributes.
 *
 * Copyright (C) 2003 Vivid Solutions
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * For more information, contact:
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */
package com.revolsys.jump.ui.plugin.layer;

import java.util.Collection;

import com.revolsys.jump.ui.model.ThemedLayer;
import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.MenuNames;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.EditingPlugIn;

public class AddNewLayerPlugIn extends AbstractPlugIn {
  public AddNewLayerPlugIn() {
  }

  public void initialize(final PlugInContext context) throws Exception {
    EnableCheckFactory enableCheckFactory = new EnableCheckFactory(
      context.getWorkbenchContext());
    context.getFeatureInstaller().addMainMenuItemWithJava14Fix(this,
      new String[] {
        MenuNames.FILE, "New"
      }, "Layer", false, null,
      enableCheckFactory.createWindowWithLayerViewPanelMustBeActiveCheck());
  }

  public static FeatureCollection createBlankFeatureCollection() {
    FeatureSchema featureSchema = new FeatureSchema();
    featureSchema.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
    return new FeatureDataset(featureSchema);
  }

  @SuppressWarnings("unchecked")
  public boolean execute(final PlugInContext context) throws Exception {
    Collection<Category> selectedCategories = context.getLayerNamePanel()
      .getSelectedCategories();
    WorkbenchContext workbenchContext = context.getWorkbenchContext();
    LayerManager layerManager = workbenchContext.getLayerManager();
    String layerName = I18N.get("ui.plugin.AddNewFLayerPlugIn.new");
    String categoryName;
    if (selectedCategories.isEmpty()) {
      categoryName = StandardCategoryNames.WORKING;
    } else {
      categoryName = selectedCategories.iterator().next().toString();
    }
    Layer layer = new ThemedLayer(layerName,
      layerManager.generateLayerFillColor(), createBlankFeatureCollection(),
      layerManager);
    layerManager.addLayerable(categoryName, layer);
    layer.setFeatureCollectionModified(false);
    layer.setEditable(true);

    EditingPlugIn editingPlugIn = (EditingPlugIn)workbenchContext.getBlackboard()
      .get(EditingPlugIn.KEY);
    editingPlugIn.getToolbox(workbenchContext).setVisible(true);

    return true;
  }
}
