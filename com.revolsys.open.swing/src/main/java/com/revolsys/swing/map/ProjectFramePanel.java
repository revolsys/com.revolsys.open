package com.revolsys.swing.map;

import java.awt.Component;
import java.util.Map;

import javax.swing.Icon;

import com.revolsys.io.ObjectWithProperties;

public interface ProjectFramePanel extends ObjectWithProperties {
  void activatePanelComponent(Component component, Map<String, Object> config);

  Component createPanelComponent(Map<String, Object> config);

  Icon getIcon();

  String getName();
}
