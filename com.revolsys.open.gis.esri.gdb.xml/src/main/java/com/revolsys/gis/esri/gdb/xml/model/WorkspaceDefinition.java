package com.revolsys.gis.esri.gdb.xml.model;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.gis.esri.gdb.xml.model.enums.WorkspaceType;

public class WorkspaceDefinition implements Cloneable {
  private WorkspaceType workspaceType = WorkspaceType.esriLocalDatabaseWorkspace;

  private String version = "";

  private List<Domain> domains = new ArrayList<Domain>();

  private List<DataElement> datasetDefinitions = new ArrayList<DataElement>();

  private String metadata;

  public void addDatasetDefinition(final DataElement datasetDefinition) {
    this.datasetDefinitions.add(datasetDefinition);
  }

  public void addDomain(final Domain domain) {
    domains.add(domain);
  }

  @Override
  public WorkspaceDefinition clone() {
    try {
      final WorkspaceDefinition clone = (WorkspaceDefinition)super.clone();
      clone.domains = new ArrayList<Domain>(domains.size());
      for (final Domain domain : domains) {
        clone.domains.add(domain.clone());
      }
      clone.datasetDefinitions = new ArrayList<DataElement>();
      for (final DataElement dataElement : datasetDefinitions) {
        clone.datasetDefinitions.add(dataElement.clone());
      }
      return clone;
    } catch (final CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  public List<DataElement> getDatasetDefinitions() {
    return datasetDefinitions;
  }

  public List<Domain> getDomains() {
    return domains;
  }

  public String getMetadata() {
    return metadata;
  }

  public String getVersion() {
    return version;
  }

  public WorkspaceType getWorkspaceType() {
    return workspaceType;
  }

  public void setDatasetDefinitions(final List<DataElement> datasetDefinitions) {
    this.datasetDefinitions = datasetDefinitions;
  }

  public void setDomains(final List<Domain> domains) {
    this.domains = domains;
  }

  public void setMetadata(final String metadata) {
    this.metadata = metadata;
  }

  public void setVersion(final String version) {
    this.version = version;
  }

  public void setWorkspaceType(final WorkspaceType workspaceType) {
    this.workspaceType = workspaceType;
  }

}
