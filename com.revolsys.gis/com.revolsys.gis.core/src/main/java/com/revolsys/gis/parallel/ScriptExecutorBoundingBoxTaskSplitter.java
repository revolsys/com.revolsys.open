package com.revolsys.gis.parallel;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.io.OutsideBoundaryWriter;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.parallel.channel.ChannelInput;
import com.revolsys.parallel.channel.ChannelOutput;
import com.revolsys.parallel.tools.ScriptExecutorRunnable;

public class ScriptExecutorBoundingBoxTaskSplitter extends
  BoundingBoxTaskSplitter {

  private String scriptName;

  private Map<String, Object> attributes = new LinkedHashMap<String, Object>();

  private Map<String, Object> beans = new LinkedHashMap<String, Object>();

  private Map<String, ChannelInput<?>> inChannels = new LinkedHashMap<String, ChannelInput<?>>();

  private Map<String, ChannelOutput<?>> outChannels = new LinkedHashMap<String, ChannelOutput<?>>();

  private OutsideBoundaryWriter outsideBoundaryWriter;

  @Override
  public void execute(final BoundingBox boundingBox) {
    outsideBoundaryWriter.expandBoundary(boundingBox.toGeometry());
    final ScriptExecutorRunnable executor = new ScriptExecutorRunnable(
      scriptName, attributes);
    executor.setLogScriptInfo(isLogScriptInfo());
    executor.addBean("boundingBox", boundingBox);
    final Set<DataObject> outsideBoundaryObjects = outsideBoundaryWriter.getAndClearOutsideBoundaryObjects();
    executor.addBean("outsideBoundaryObjects", outsideBoundaryObjects);
    executor.addBeans(beans);
    executor.addBeans(inChannels);
    executor.addBeans(outChannels);
    executor.run();
  }

  public Map<String, Object> getAttributes() {
    return attributes;
  }

  public Map<String, Object> getBeans() {
    return beans;
  }

  public Map<String, ChannelInput<?>> getInChannels() {
    return inChannels;
  }

  public Map<String, ChannelOutput<?>> getOutChannels() {
    return outChannels;
  }

  public OutsideBoundaryWriter getOutsideBoundaryWriter() {
    return outsideBoundaryWriter;
  }

  public String getScriptName() {
    return scriptName;
  }

  @Override
  protected void postRun() {
    for (final ChannelInput<?> in : inChannels.values()) {
      in.readDisconnect();
    }
    for (final ChannelOutput<?> out : outChannels.values()) {
      out.writeDisconnect();
    }
    
  }

  @Override
  protected void preRun() {
    for (final ChannelInput<?> in : inChannels.values()) {
      in.readConnect();
    }
    for (final ChannelOutput<?> out : outChannels.values()) {
      out.writeConnect();
    }
  }

  public void setAttributes(final Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  public void setBeans(final Map<String, Object> beans) {
    this.beans = beans;
  }

  public void setInChannels(final Map<String, ChannelInput<?>> inChannels) {
    this.inChannels = inChannels;
  }

  public void setOutChannels(final Map<String, ChannelOutput<?>> outChannels) {
    this.outChannels = outChannels;
  }

  public void setOutsideBoundaryWriter(
    final OutsideBoundaryWriter outsideBoundaryWriter) {
    this.outsideBoundaryWriter = outsideBoundaryWriter;
  }

  public void setScriptName(final String scriptName) {
    this.scriptName = scriptName;
  }

}
