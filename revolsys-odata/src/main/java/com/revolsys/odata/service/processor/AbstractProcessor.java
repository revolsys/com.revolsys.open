package com.revolsys.odata.service.processor;

import java.net.URI;
import java.util.Locale;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.ContextURL.Builder;
import org.apache.olingo.commons.api.edm.EdmBindingTarget;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.Processor;

import com.revolsys.odata.model.ODataEdmProvider;
import com.revolsys.odata.model.ODataEntityType;

public abstract class AbstractProcessor implements Processor {
  protected OData odata;

  protected final String serviceRoot;

  protected final URI serviceRootUri;

  protected ServiceMetadata serviceMetadata;

  protected final ODataEdmProvider provider;

  public AbstractProcessor(final ODataEdmProvider provider) {
    this.provider = provider;
    this.serviceRoot = provider.getServiceRoot();
    this.serviceRootUri = URI.create(this.serviceRoot);
  }

  public ODataEntityType getEntityType(final EdmEntitySet edmEntitySet)
    throws ODataApplicationException {
    final EdmEntityType edmEntityType = edmEntitySet.getEntityType();
    final FullQualifiedName entityTypeName = edmEntityType.getFullQualifiedName();
    final ODataEntityType entityType = this.provider.getEntityType(entityTypeName);
    if (entityType == null) {
      throw new ODataApplicationException(
        "Entity type " + entityTypeName + " for requested key doesn't exist",
        HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
    }
    return entityType;
  }

  public EdmEntitySet getNavigationTargetEntitySet(final EdmEntitySet startEdmEntitySet,
    final EdmNavigationProperty edmNavigationProperty) throws ODataApplicationException {
    final String navigationPropertyName = edmNavigationProperty.getName();
    final EdmBindingTarget edmBindingTarget = startEdmEntitySet
      .getRelatedBindingTarget(navigationPropertyName);
    if (edmBindingTarget == null) {
      throw new ODataApplicationException("Not supported.",
        HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
    } else if (edmBindingTarget instanceof EdmEntitySet) {
      return (EdmEntitySet)edmBindingTarget;
    } else {
      throw new ODataApplicationException("Not supported.",
        HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
    }

  }

  @Override
  public void init(final OData odata, final ServiceMetadata serviceMetadata) {
    this.odata = odata;
    this.serviceMetadata = serviceMetadata;
  }

  protected Builder newContextUrl() {
    return ContextURL//
      .with()
      .serviceRoot(this.serviceRootUri);
  }

}
