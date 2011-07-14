package com.revolsys.gis.esri.gdb.xml.model;

import java.util.List;

import javax.xml.namespace.QName;

public class DataElement {
  private String catalogPath;

  private String name;

  private boolean childrenExpanded;

  private boolean fullPropsRetrieved;

  private boolean metadataRetrieved;

  private String metadata;

  private List<DataElement> children;

  public String getCatalogPath() {
    return catalogPath;
  }

  public List<DataElement> getChildren() {
    return children;
  }

  public String getMetadata() {
    return metadata;
  }

  public String getName() {
    return name;
  }

  public QName getTypeName() {
    final int slashIndex = catalogPath.lastIndexOf('\\');
    if (slashIndex == -1) {
      return new QName(catalogPath);
    } else if (slashIndex == 0) {
      return new QName(catalogPath.substring(1));
    } else {
      final String namespaceUri = catalogPath.substring(1, slashIndex);
      final String localPart = catalogPath.substring(slashIndex + 1);
      return new QName(namespaceUri, localPart);
    }
  }

  public boolean isChildrenExpanded() {
    return childrenExpanded;
  }

  public boolean isFullPropsRetrieved() {
    return fullPropsRetrieved;
  }

  public boolean isMetadataRetrieved() {
    return metadataRetrieved;
  }

  public void setCatalogPath(final String catalogPath) {
    this.catalogPath = catalogPath;
  }

  public void setChildren(final List<DataElement> children) {
    this.children = children;
  }

  public void setChildrenExpanded(final boolean childrenExpanded) {
    this.childrenExpanded = childrenExpanded;
  }

  public void setFullPropsRetrieved(final boolean fullPropsRetrieved) {
    this.fullPropsRetrieved = fullPropsRetrieved;
  }

  public void setMetadata(final String metadata) {
    this.metadata = metadata;
  }

  public void setMetadataRetrieved(final boolean metadataRetrieved) {
    this.metadataRetrieved = metadataRetrieved;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setTypeName(final QName catalogPath) {
    final String namespaceUri = catalogPath.getNamespaceURI();
    this.name = catalogPath.getLocalPart();
    if (namespaceUri.length() == 0) {
      this.catalogPath = "\\" + name;
    } else {
      this.catalogPath = "\\" + namespaceUri + "\\" + name;
    }
  }

}
