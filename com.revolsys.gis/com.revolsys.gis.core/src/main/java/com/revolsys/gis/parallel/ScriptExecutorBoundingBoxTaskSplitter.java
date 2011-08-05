package com.revolsys.gis.parallel;

import java.util.LinkedHashMap;
import java.util.Map;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.parallel.tools.ScriptExecutorRunnable;

public class ScriptExecutorBoundingBoxTaskSplitter extends
  BoundingBoxTaskSplitter {

  private String scriptName;

  private Map<String, Object> attributes = new LinkedHashMap<String, Object>();

  private Map<String, Object> beans = new LinkedHashMap<String, Object>();

  @Override
  public void execute(final BoundingBox boundingBox) {
    final ScriptExecutorRunnable executor = new ScriptExecutorRunnable(
      scriptName, attributes);
    executor.addBean("boundingBox", boundingBox);
    executor.addBeans(beans);
    executor.run();
  }

  public Map<String, Object> getAttributes() {
    return attributes;
  }

  public String getScriptName() {
    return scriptName;
  }

  public void setAttributes(Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  public void setScriptName(final String scriptName) {
    this.scriptName = scriptName;
  }

  public Map<String, Object> getBeans() {
    return beans;
  }

  public void setBeans(Map<String, Object> beans) {
    this.beans = beans;
  }

}
