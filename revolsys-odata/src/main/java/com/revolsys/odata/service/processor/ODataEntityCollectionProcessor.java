package com.revolsys.odata.service.processor;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.olingo.commons.api.data.ContextURL;
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
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerStreamResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.jeometry.common.logging.Logs;

import com.revolsys.odata.model.ODataEdmProvider;
import com.revolsys.odata.model.ODataEntityIterator;
import com.revolsys.odata.model.ODataEntityType;

public class ODataEntityCollectionProcessor extends AbstractProcessor
  implements EntityCollectionProcessor {

  private final Map<ContentType, ODataSerializer> serializerByContentType = new HashMap<>();

  public ODataEntityCollectionProcessor(final ODataEdmProvider provider) {
    super(provider);
  }

  ODataSerializer getSerializer(final ContentType contentType) throws SerializerException {
    ODataSerializer serializer = this.serializerByContentType.get(contentType);
    if (serializer == null) {
      synchronized (this.serializerByContentType) {
        serializer = this.serializerByContentType.get(contentType);
        if (serializer == null) {
          serializer = this.odata.createSerializer(contentType);
          this.serializerByContentType.put(contentType, serializer);
        }
      }
    }
    return serializer;
  }

  @Override
  public void readEntityCollection(final ODataRequest request, final ODataResponse response,
    final UriInfo uriInfo, final ContentType responseFormat)
    throws ODataApplicationException, ODataLibraryException {
    ODataEntityIterator entityIterator = null;

    final List<UriResource> resourceParts = uriInfo.getUriResourceParts();
    final int segmentCount = resourceParts.size();

    final UriResource uriResource = resourceParts.get(0);
    if (uriResource instanceof UriResourceEntitySet) {
      final UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet)uriResource;
      final EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
      if (segmentCount == 1) {
        final ODataEntityType entityType = getEntityType(edmEntitySet);
        entityIterator = entityType.readEntityIterator(request, uriInfo, edmEntitySet);
      } else if (segmentCount == 2) {
        final UriResource lastSegment = resourceParts.get(1);
        if (lastSegment instanceof UriResourceNavigation) {
          final UriResourceNavigation uriResourceNavigation = (UriResourceNavigation)lastSegment;
          final EdmNavigationProperty edmNavigationProperty = uriResourceNavigation.getProperty();
          final EdmEntityType targetEntityType = edmNavigationProperty.getType();
          // responseEdmEntitySet =
          // Util.getNavigationTargetEntitySet(edmEntitySet,
          // edmNavigationProperty);
          //
          // // 2nd: fetch the data from backend
          // // first fetch the entity where the first segment of the URI points
          // to
          // // e.g. Categories(3)/Products first find the single entity:
          // Category(3)
          // List<UriParameter> keyPredicates =
          // uriResourceEntitySet.getKeyPredicates();
          // Entity sourceEntity = storage.readEntityData(edmEntitySet,
          // keyPredicates);
          // // error handling for e.g. DemoService.svc/Categories(99)/Products
          // if(sourceEntity == null) {
          // throw new ODataApplicationException("Entity not found.",
          // HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
          // }
          // // then fetch the entity collection where the entity navigates to
          // entityCollection = storage.getRelatedEntityCollection(sourceEntity,
          // targetEntityType);
        }
      } else { // this would be the case for e.g. Products(1)/Category/Products
        throw new ODataApplicationException("Not supported",
          HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
      }
      final ODataSerializer serializer = getSerializer(responseFormat);

      final EdmEntityType edmEntityType = edmEntitySet.getEntityType();
      String selectList = null;
      final SelectOption selectOption = uriInfo.getSelectOption();
      if (selectOption != null) {
        selectList = this.odata.createUriHelper()
          .buildContextURLSelectList(edmEntityType, null, selectOption);
      }

      final ContextURL contextUrl = newContextUrl().selectList(selectList)
        .entitySet(edmEntitySet)
        .build();

      final String id = request.getRawBaseUri() + "/" + edmEntitySet.getName();
      final EntityCollectionSerializerOptions opts = EntityCollectionSerializerOptions//
        .with()
        .id(id)
        .count(uriInfo.getCountOption())
        .contextURL(contextUrl)
        .select(selectOption)
        .writeContentErrorCallback((context, channel) -> {
          final String message = request.getRawRequestUri();
          final Exception exception = context.getException();
          Throwable cause = exception.getCause();
          while (cause != null) {
            if (cause.getMessage().equals("Broken pipe")) {
              return;
            }
            cause = cause.getCause();
          }
          Logs.error(this, message, exception);
        })
        .build();

      if (entityIterator != null) {
        final SerializerStreamResult serializerResult = serializer
          .entityCollectionStreamed(this.serviceMetadata, edmEntityType, entityIterator, opts);

        final ODataEntityInteratorDataContent dataContent = new ODataEntityInteratorDataContent(
          serializerResult, entityIterator);
        response.setODataContent(dataContent);
      }
      response.setStatusCode(HttpStatusCode.OK.getStatusCode());
      response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    } else {
      throw new ODataApplicationException("Only EntitySet is supported",
        HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
    }
  }
}
