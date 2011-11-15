package com.revolsys.io.esri.gdb.xml.model;

import java.util.ArrayList;
import java.util.List;

public class Workspace implements Cloneable {
  private WorkspaceDefinition workspaceDefinition = new WorkspaceDefinition();

  private List<AnyDatasetData> workspaceData = new ArrayList<AnyDatasetData>();

  public Workspace() {
  }

  @Override
  public Workspace clone() {
    try {
      final Workspace clone = (Workspace)super.clone();
      workspaceDefinition = workspaceDefinition.clone();
      clone.workspaceData = new ArrayList<AnyDatasetData>(workspaceData.size());
      for (final AnyDatasetData data : workspaceData) {
        clone.workspaceData.add(data.clone());
      }
      return clone;
    } catch (final CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  public List<AnyDatasetData> getWorkspaceData() {
    return workspaceData;
  }

  public WorkspaceDefinition getWorkspaceDefinition() {
    return workspaceDefinition;
  }

  public void setWorkspaceData(final List<AnyDatasetData> workspaceData) {
    this.workspaceData = workspaceData;
  }

  public void setWorkspaceDefinition(
    final WorkspaceDefinition workspaceDefinition) {
    this.workspaceDefinition = workspaceDefinition;
  }

}
