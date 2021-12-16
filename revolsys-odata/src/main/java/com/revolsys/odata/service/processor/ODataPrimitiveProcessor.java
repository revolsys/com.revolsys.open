package com.revolsys.odata.service.processor;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.processor.PrimitiveProcessor;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.PrimitiveSerializerOptions;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceProperty;

import com.revolsys.odata.model.ODataEdmProvider;
import com.revolsys.odata.model.ODataEntityType;

public class ODataPrimitiveProcessor extends AbstractProcessor implements PrimitiveProcessor {

  public ODataPrimitiveProcessor(final ODataEdmProvider provider) {
    super(provider);
  }

  @Override
  public void deletePrimitive(final ODataRequest request, final ODataResponse response,
    final UriInfo uriInfo) throws ODataApplicationException, ODataLibraryException {
    // TODO Auto-generated method stub

  }

  @Override
  public void readPrimitive(final ODataRequest request, final ODataResponse response,
    final UriInfo uriInfo, final ContentType responseFormat)
    throws ODataApplicationException, ODataLibraryException {
    final List<UriResource> resourceParts = uriInfo.getUriResourceParts();
    final UriResourceEntitySet uriEntityset = (UriResourceEntitySet)resourceParts.get(0);
    final EdmEntitySet edmEntitySet = uriEntityset.getEntitySet();
    final List<UriParameter> keyPredicates = uriEntityset.getKeyPredicates();

    final UriResourceProperty uriProperty = (UriResourceProperty)resourceParts
      .get(resourceParts.size() - 1);
    final EdmProperty edmProperty = uriProperty.getProperty();
    final String edmPropertyName = edmProperty.getName();
    final EdmPrimitiveType edmPropertyType = (EdmPrimitiveType)edmProperty.getType();

    final ODataEntityType entityType = getEntityType(edmEntitySet);
    final Property property = entityType.readPrimitive(edmEntitySet, keyPredicates,
      edmPropertyName);

    if (property == null) {
      throw new ODataApplicationException("Property not found",
        HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
    }

    final Object value = property.getValue();
    if (value != null) {
      final ODataSerializer serializer = this.odata.createSerializer(responseFormat);

      final ContextURL contextUrl = newContextUrl()//
        .entitySet(edmEntitySet)
        .navOrPropertyPath(edmPropertyName)
        .build();
      final PrimitiveSerializerOptions options = PrimitiveSerializerOptions.with()
        .contextURL(contextUrl)
        .build();
      final SerializerResult serializerResult = serializer.primitive(this.serviceMetadata,
        edmPropertyType, property, options);
      final InputStream propertyStream = serializerResult.getContent();

      response.setContent(propertyStream);
      response.setStatusCode(HttpStatusCode.OK.getStatusCode());
      response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    } else {
      response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
    }
  }

  @Override
  public void updatePrimitive(final ODataRequest request, final ODataResponse response,
    final UriInfo uriInfo, final ContentType requestFormat, final ContentType responseFormat)
    throws ODataApplicationException, ODataLibraryException {
    // TODO Auto-generated method stub

  }

}
