package com.revolsys.odata.service.processor;

import java.util.List;
import java.util.Locale;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.ContextURL.Suffix;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceNavigation;

import com.revolsys.odata.model.ODataEdmProvider;
import com.revolsys.odata.model.ODataEntityType;
import com.revolsys.odata.model.ODataNavigationProperty;

public class ODataEntityProcessor extends AbstractProcessor implements EntityProcessor {

  public ODataEntityProcessor(final ODataEdmProvider provider) {
    super(provider);
  }

  @Override
  public void createEntity(final ODataRequest request, final ODataResponse response,
    final UriInfo uriInfo, final ContentType requestFormat, final ContentType responseFormat)
    throws ODataApplicationException, ODataLibraryException {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteEntity(final ODataRequest request, final ODataResponse response,
    final UriInfo uriInfo) throws ODataApplicationException, ODataLibraryException {
    // TODO Auto-generated method stub

  }

  private Entity readEntity(final EdmEntitySet entitySet, final List<UriParameter> keyPredicates)
    throws ODataApplicationException {
    final ODataEntityType entityType = getEntityType(entitySet);
    return entityType.readEntity(entitySet, keyPredicates, null);
  }

  @Override
  public void readEntity(final ODataRequest request, final ODataResponse response,
    final UriInfo uriInfo, final ContentType responseFormat)
    throws ODataApplicationException, ODataLibraryException {
    EdmEntityType responseEdmEntityType = null;
    Entity responseEntity = null;
    EdmEntitySet responseEdmEntitySet = null;

    final List<UriResource> resourceParts = uriInfo.getUriResourceParts();
    final int segmentCount = resourceParts.size();

    final UriResource uriResource = resourceParts.get(0);
    if (!(uriResource instanceof UriResourceEntitySet)) {
      throw new ODataApplicationException("Only EntitySet is supported",
        HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
    }

    final UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet)uriResource;
    final EdmEntitySet startEdmEntitySet = uriResourceEntitySet.getEntitySet();

    if (segmentCount == 1) {
      responseEdmEntityType = startEdmEntitySet.getEntityType();
      responseEdmEntitySet = startEdmEntitySet;
      final List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
      responseEntity = readEntity(startEdmEntitySet, keyPredicates);
    } else if (segmentCount == 2) { // navigation
      final UriResource navSegment = resourceParts.get(1);
      if (navSegment instanceof UriResourceNavigation) {
        final UriResourceNavigation uriResourceNavigation = (UriResourceNavigation)navSegment;
        final EdmNavigationProperty edmNavigationProperty = uriResourceNavigation.getProperty();
        responseEdmEntityType = edmNavigationProperty.getType();
        responseEdmEntitySet = getNavigationTargetEntitySet(startEdmEntitySet,
          edmNavigationProperty);
        final ODataEntityType targetEntityType = getEntityType(responseEdmEntitySet);

        final List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
        final Entity sourceEntity = readEntity(startEdmEntitySet, keyPredicates);
        final List<UriParameter> navKeyPredicates = uriResourceNavigation.getKeyPredicates();
        final String navigationPropertyName = edmNavigationProperty.getName();
        final ODataEntityType sourceEntityType = getEntityType(startEdmEntitySet);
        final ODataNavigationProperty navigationProperty = sourceEntityType
          .getNavigationProperty(navigationPropertyName);
        if (navKeyPredicates.isEmpty()) { // /Products(1)/Category
          responseEntity = targetEntityType.getRelatedEntity(sourceEntity, navigationProperty);
        } else { // e.g. /Categories(3)/Products(5)
          // responseEntity = targetEntityType.getRelatedEntity(sourceEntity,
          // responseEdmEntityType,
          // navKeyPredicates);
        }
      }
    } else {
      // this would be the case for e.g.
      // Products(1)/Category/Products(1)/Category
      throw new ODataApplicationException("Not supported",
        HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
    }

    if (responseEntity == null) {
      // this is the case for e.g. DemoService.svc/Categories(4) or
      // DemoService.svc/Categories(3)/Products(999)
      throw new ODataApplicationException("Nothing found.",
        HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
    }

    // 3. serialize
    final ContextURL contextUrl = newContextUrl()//
      .entitySet(responseEdmEntitySet)
      .suffix(Suffix.ENTITY)
      .build();
    final EntitySerializerOptions opts = EntitySerializerOptions.with()
      .contextURL(contextUrl)
      .build();

    final ODataSerializer serializer = this.odata.createSerializer(responseFormat);
    final SerializerResult serializerResult = serializer.entity(this.serviceMetadata,
      responseEdmEntityType, responseEntity, opts);

    // 4. configure the response object
    response.setContent(serializerResult.getContent());
    response.setStatusCode(HttpStatusCode.OK.getStatusCode());
    response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
  }

  @Override
  public void updateEntity(final ODataRequest request, final ODataResponse response,
    final UriInfo uriInfo, final ContentType requestFormat, final ContentType responseFormat)
    throws ODataApplicationException, ODataLibraryException {
    // TODO Auto-generated method stub

  }

}
