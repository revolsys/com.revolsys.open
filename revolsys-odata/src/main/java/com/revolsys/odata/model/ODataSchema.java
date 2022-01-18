package com.revolsys.odata.model;

import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;

import com.revolsys.record.schema.RecordStore;

public class ODataSchema extends CsdlSchema {
  private final ODataEntityContainer odataEntityContainer;

  private final ODataEdmProvider provider;

  public ODataSchema(final ODataEdmProvider provider, final String namespace) {
    this.provider = provider;
    setNamespace(namespace);
    this.odataEntityContainer = new ODataEntityContainer(namespace);
    setEntityContainer(this.odataEntityContainer);

  }

  public void addEntitySet(final AbstractODataEntitySet entitySet) {
    this.odataEntityContainer.addEntitySet(entitySet);
    final ODataEntityType entityType = entitySet.getEntityType();
    final List<CsdlEntityType> entityTypes = getEntityTypes();
    entityTypes.sort((a, b) -> {
      return a.getName().compareToIgnoreCase(b.getName());
    });
    entityTypes.add(entityType);
  }

  private boolean equalsNamespace(final FullQualifiedName qualifiedName) {
    final String namespaceThis = getNamespace();
    final String namespaceOther = qualifiedName.getNamespace();
    return namespaceThis.equals(namespaceOther);
  }

  public ODataEntityContainer getEntityContainer(final FullQualifiedName entityContainerName) {
    if (this.odataEntityContainer.getQualifiedName().equals(entityContainerName)) {
      return this.odataEntityContainer;
    }
    return null;
  }

  public CsdlEntityContainerInfo getEntityContainerInfo() {
    return this.odataEntityContainer.getEntityContainerInfo();
  }

  public ODataEntityType getEntityType(final FullQualifiedName entityTypeName) {
    if (equalsNamespace(entityTypeName)) {
      final String name = entityTypeName.getName();
      return getEntityType(name);
    }
    return null;
  }

  @Override
  public ODataEntityType getEntityType(final String name) {
    return (ODataEntityType)super.getEntityType(name);
  }

  public ODataEdmProvider getProvider() {
    return this.provider;
  }

  public FullQualifiedName getQualifiedName(final String name) {
    final String namespace = getNamespace();
    return new FullQualifiedName(namespace, name);
  }

  public RecordStore getRecordStore() {
    return this.provider.getRecordStore();
  }
}
