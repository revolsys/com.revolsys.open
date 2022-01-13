package com.revolsys.odata.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;

import com.revolsys.odata.service.processor.ODataEntityCollectionProcessor;
import com.revolsys.odata.service.processor.ODataEntityProcessor;
import com.revolsys.odata.service.processor.ODataPrimitiveProcessor;
import com.revolsys.odata.service.processor.ODataServiceDocumentMetadataProcessor;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.record.schema.TableRecordStoreConnection;

public abstract class ODataEdmProvider extends CsdlAbstractEdmProvider {

  private final CsdlEntityContainer allEntityContainer = new CsdlEntityContainer()
    .setName("Container");

  private ODataSchema defaultSchema;

  private RecordStore recordStore;

  private final Map<String, ODataSchema> schemaByNamespace = new TreeMap<>();

  private final List<CsdlSchema> schemas = new ArrayList<>();

  private ODataHttpHandler handler;

  private String serviceRoot;

  private final Map<FullQualifiedName, CsdlTerm> termByName = new HashMap<>();

  public ODataEdmProvider() {
    addTerm("Geometry", "axisCount", "Int");
    addTerm("Geometry", "scaleX", "Float");
    addTerm("Geometry", "scaleY", "Float");
    addTerm("Geometry", "scaleZ", "Float");
  }

  protected ODataSchema addSchema(final String namespace) {
    final ODataSchema schema = new ODataSchema(this, namespace);
    this.schemaByNamespace.put(namespace, schema);
    this.schemas.add(schema);
    if (this.defaultSchema == null) {
      this.defaultSchema = schema;
    }
    return schema;
  }

  private CsdlTerm addTerm(final String namespace, final String name, final String type) {
    final CsdlTerm term = new CsdlTerm().setName(name).setType(type);
    this.termByName.put(new FullQualifiedName(namespace, name), term);
    return term;
  }

  public void close() {
    this.recordStore.close();
  }

  @Override
  public CsdlEntityContainer getEntityContainer() throws ODataException {
    return this.allEntityContainer;
  }

  private ODataEntityContainer getEntityContainer(final FullQualifiedName entityContainerName) {
    final ODataSchema schema = getSchema(entityContainerName);
    if (schema == null) {
      return null;
    } else {
      return schema.getEntityContainer(entityContainerName);
    }
  }

  @Override
  public CsdlEntityContainerInfo getEntityContainerInfo(final FullQualifiedName entityContainerName)
    throws ODataException {
    if (entityContainerName == null) {
      if (this.defaultSchema != null) {
        return this.defaultSchema.getEntityContainerInfo();
      }
    } else {
      final ODataEntityContainer entityContainer = getEntityContainer(entityContainerName);
      if (entityContainer != null) {
        return entityContainer.getEntityContainerInfo();
      }
    }
    return null;
  }

  @Override
  public CsdlEntitySet getEntitySet(final FullQualifiedName entityContainerName,
    final String entitySetName) throws ODataException {
    final ODataEntityContainer entityContainer = getEntityContainer(entityContainerName);
    if (entityContainer != null) {
      return entityContainer.getEntitySet(entitySetName);
    }
    return null;
  }

  @Override
  public ODataEntityType getEntityType(final FullQualifiedName entityTypeName) {
    final ODataSchema schema = getSchema(entityTypeName);
    if (schema != null) {
      return schema.getEntityType(entityTypeName);
    }
    return null;
  }

  public ODataHttpHandler getHandler() {
    return this.handler;
  }

  public RecordStore getRecordStore() {
    return this.recordStore;
  }

  private ODataSchema getSchema(final FullQualifiedName qualifiedName) {
    final String namespace = qualifiedName.getNamespace();
    return this.schemaByNamespace.get(namespace);
  }

  @Override
  public List<CsdlSchema> getSchemas() throws ODataException {
    return this.schemas;
  }

  public String getServiceRoot() {
    return this.serviceRoot;
  }

  public abstract TableRecordStoreConnection getTableRecordStoreConnection();

  @Override
  public CsdlTerm getTerm(final FullQualifiedName termName) throws ODataException {
    return this.termByName.get(termName);
  }

  public void init() {
    for (final CsdlSchema schema : this.schemas) {
      for (final CsdlEntitySet entitySet : schema.getEntityContainer().getEntitySets()) {
        this.allEntityContainer.getEntitySets().add(entitySet);
      }
    }
    final OData odata = OData.newInstance();
    final ServiceMetadata edm = odata.createServiceMetadata(this, new ArrayList<>());
    final ODataHttpHandler handler = odata.createHandler(edm);
    handler.register(new ODataEntityCollectionProcessor(this));
    handler.register(new ODataEntityProcessor(this));
    handler.register(new ODataPrimitiveProcessor(this));
    handler.register(new ODataServiceDocumentMetadataProcessor(this));
    this.handler = handler;
  }

  protected void setRecordStore(final RecordStore recordStore) {
    this.recordStore = recordStore;
  }

  public void setServiceRoot(final String serviceRoot) {
    this.serviceRoot = serviceRoot;
  }

}
