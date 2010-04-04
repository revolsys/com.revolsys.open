package com.revolsys.jump.ui.info;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.CategoryEvent;
import com.vividsolutions.jump.workbench.model.FeatureEvent;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEvent;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.model.LayerListener;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.ui.InfoModel;
import com.vividsolutions.jump.workbench.ui.InfoModelListener;
import com.vividsolutions.jump.workbench.ui.LayerTableModel;

public class CurrentFeatureInfoModel extends InfoModel implements LayerListener {
  private Layer currentLayer;

  private Feature currentFeature;

  private int layerFeatureIndex = 0;

  private int featureIndex = 0;

  private int featureCount = 0;

  private List<CurrentFeatureListener> listeners = new ArrayList<CurrentFeatureListener>();

  public CurrentFeatureInfoModel(final LayerManager layerManager) {
    layerManager.addLayerListener(this);
  }

  public Feature getCurrentFeature() {
    return currentFeature;
  }

  public Layer getCurrentLayer() {
    return currentLayer;
  }

  public int getFeatureCount() {
    return featureCount;
  }

  public int getFeatureIndex() {
    return featureIndex;
  }

  public boolean isAtFirst() {
    return featureIndex == 0;
  }

  public boolean isAtLast() {
    return featureIndex >= featureCount - 1;
  }

  public void firstFeature() {
    setSelectedFeature(0);
  }

  public void previousFeature() {
    setSelectedFeature(featureIndex - 1);
  }

  public void nextFeature() {
    setSelectedFeature(featureIndex + 1);
  }

  public void lastFeature() {
    setSelectedFeature(featureCount - 1);
  }

  @SuppressWarnings("unchecked")
  public void setSelectedFeature(final int i) {
    boolean found = false;

    if (featureCount > 0) {
      if (i >= featureCount) {
        if (featureCount == 0) {
          featureIndex = 0;
        } else {
          featureIndex = featureCount - 1;
        }
      } else if (i <= 0) {
        featureIndex = 0;
      } else {
        featureIndex = i;
      }
      int layerIndex = 0;
      Iterator<Layer> layers = getLayers().iterator();
      while (layers.hasNext() && !found) {
        Layer layer = layers.next();
        LayerTableModel layerModel = getTableModel(layer);

        int layerRows = layerModel.getRowCount();
        if (featureIndex < layerIndex + layerRows) {
          layerFeatureIndex = featureIndex - layerIndex;
          currentFeature = layerModel.getFeature(layerFeatureIndex);
          currentLayer = layer;
          found = true;
        } else {
          layerIndex += layerRows;
        }
      }
    } else {
      featureCount = 0;
    }
    if (!found) {
      currentFeature = null;
      currentLayer = null;
    }
    fireFeatureSelected();
  }

  public void addCurrentFeatureListener(final CurrentFeatureListener listener) {
    listeners.add(listener);
    listener.featureSelected(currentLayer, currentFeature);
  }

  public void fireFeatureSelected() {
    for (CurrentFeatureListener listener : listeners) {
      listener.featureSelected(currentLayer, currentFeature);
    }
  }

  @SuppressWarnings("unchecked")
  public void add(final Layer layer, final Collection features) {
    super.add(layer, features);
    updateFeatureCount();
  }

  public void remove(final Layer layer) {
    if (getLayers().contains(layer)) {
      super.remove(layer);
      updateFeatureCount();
    }
  }

  private void updateFeatureCount() {
    featureCount = 0;
    for (Layer layer : getLayers()) {
      LayerTableModel layerModel = getTableModel(layer);
      featureCount += layerModel.getRowCount();
    }
    firstFeature();
  }

  @SuppressWarnings("unchecked")
  public List<Layer> getLayers() {
    return super.getLayers();
  }

  public void addListener(final InfoModelListener listener) {
    List<Layer> layers = getLayers();
    for (Layer layer : layers) {
      LayerTableModel layerTableModel = getTableModel(layer);
      listener.layerAdded(layerTableModel);
    }
    super.addListener(listener);
  }

  public void categoryChanged(final CategoryEvent e) {
  }

  public void featuresChanged(final FeatureEvent e) {
  }

  public void layerChanged(final LayerEvent e) {
    Layerable layerable = e.getLayerable();
    if (layerable instanceof Layer) {
      Layer layer = (Layer)e.getLayerable();
      LayerEventType eventType = e.getType();
      if (eventType == LayerEventType.REMOVED) {
        remove(layer);
      }
    }
  }

}
