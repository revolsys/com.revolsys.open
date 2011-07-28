package com.revolsys.gis.esri.gdb.xml.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

public class DataElement implements Cloneable {
  private String catalogPath;

  private String name;

  private Boolean childrenExpanded;

  private Boolean fullPropsRetrieved;

  private Boolean metadataRetrieved;

  private String metadata;

  private List<DataElement> children;

  @Override
  public DataElement clone() {
    try {
      final DataElement clone = (DataElement)super.clone();
      if (children != null) {
        clone.children = new ArrayList<DataElement>();
        for (DataElement child : children) {
          clone.children.add(child.clone());
        }
      }
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }
  public String getParentCatalogPath() {
    String path = getCatalogPath();
    if (path == null) {
      return null;
    } else {
      int index = path.lastIndexOf('\\');
      if (index == -1 || index == 0) {
        return "\\";
      } else {
        return path.substring(0, index);
      }
    }
  }

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

  public Boolean getChildrenExpanded() {
    return childrenExpanded;
  }

  public Boolean getFullPropsRetrieved() {
    return fullPropsRetrieved;
  }

  public Boolean getMetadataRetrieved() {
    return metadataRetrieved;
  }

  public void setCatalogPath(final String catalogPath) {
    this.catalogPath = catalogPath;
  }

  public void setChildren(final List<DataElement> children) {
    this.children = children;
  }

  public void setChildrenExpanded(final Boolean childrenExpanded) {
    this.childrenExpanded = childrenExpanded;
  }

  public void setFullPropsRetrieved(final Boolean fullPropsRetrieved) {
    this.fullPropsRetrieved = fullPropsRetrieved;
  }

  public void setMetadata(final String metadata) {
    this.metadata = metadata;
  }

  public void setMetadataRetrieved(final Boolean metadataRetrieved) {
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

  @Override
  public String toString() {
    return name;
  }
}
