package com.revolsys.io.esri.gdb.xml.model;

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
        for (final DataElement child : children) {
          clone.children.add(child.clone());
        }
      }
      return clone;
    } catch (final CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  public String getCatalogPath() {
    return catalogPath;
  }

  public List<DataElement> getChildren() {
    return children;
  }

  public Boolean getChildrenExpanded() {
    return childrenExpanded;
  }

  public Boolean getFullPropsRetrieved() {
    return fullPropsRetrieved;
  }

  public String getMetadata() {
    return metadata;
  }

  public Boolean getMetadataRetrieved() {
    return metadataRetrieved;
  }

  public String getName() {
    return name;
  }

  public String getParentCatalogPath() {
    final String path = getCatalogPath();
    if (path == null) {
      return null;
    } else {
      final int index = path.lastIndexOf('\\');
      if (index == -1 || index == 0) {
        return "\\";
      } else {
        return path.substring(0, index);
      }
    }
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
