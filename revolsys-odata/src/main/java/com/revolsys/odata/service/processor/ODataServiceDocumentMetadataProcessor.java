package com.revolsys.odata.service.processor;

import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.etag.ETagHelper;
import org.apache.olingo.server.api.etag.ServiceMetadataETagSupport;
import org.apache.olingo.server.api.processor.MetadataProcessor;
import org.apache.olingo.server.api.processor.ServiceDocumentProcessor;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.uri.UriInfo;

import com.revolsys.odata.model.ODataEdmProvider;

public class ODataServiceDocumentMetadataProcessor extends AbstractProcessor
  implements ServiceDocumentProcessor, MetadataProcessor {

  public ODataServiceDocumentMetadataProcessor(final ODataEdmProvider provider) {
    super(provider);

  }

  @Override
  public void readMetadata(final ODataRequest request, final ODataResponse response,
    final UriInfo uriInfo, final ContentType requestedContentType)
    throws ODataApplicationException, ODataLibraryException {
    boolean isNotModified = false;
    final ServiceMetadataETagSupport eTagSupport = this.serviceMetadata
      .getServiceMetadataETagSupport();
    if (eTagSupport != null && eTagSupport.getMetadataETag() != null) {
      response.setHeader(HttpHeader.ETAG, eTagSupport.getMetadataETag());
      final ETagHelper eTagHelper = this.odata.createETagHelper();
      isNotModified = eTagHelper.checkReadPreconditions(eTagSupport.getMetadataETag(),
        request.getHeaders(HttpHeader.IF_MATCH), request.getHeaders(HttpHeader.IF_NONE_MATCH));
    }

    // Send the correct response
    if (isNotModified) {
      response.setStatusCode(HttpStatusCode.NOT_MODIFIED.getStatusCode());
    } else {
      // HTTP HEAD requires no payload but a 200 OK response
      if (HttpMethod.HEAD == request.getMethod()) {
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
      } else {
        final ODataSerializer serializer = this.odata.createSerializer(requestedContentType);
        response.setContent(serializer.metadataDocument(this.serviceMetadata).getContent());
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, requestedContentType.toContentTypeString());
      }
    }
  }

  @Override
  public void readServiceDocument(final ODataRequest request, final ODataResponse response,
    final UriInfo uriInfo, final ContentType requestedContentType)
    throws ODataApplicationException, ODataLibraryException {
    boolean isNotModified = false;
    final ServiceMetadataETagSupport eTagSupport = this.serviceMetadata
      .getServiceMetadataETagSupport();
    if (eTagSupport != null && eTagSupport.getServiceDocumentETag() != null) {
      // Set application etag at response
      response.setHeader(HttpHeader.ETAG, eTagSupport.getServiceDocumentETag());
      final ETagHelper eTagHelper = this.odata.createETagHelper();
      isNotModified = eTagHelper.checkReadPreconditions(eTagSupport.getServiceDocumentETag(),
        request.getHeaders(HttpHeader.IF_MATCH), request.getHeaders(HttpHeader.IF_NONE_MATCH));
    }

    // Send the correct response
    if (isNotModified) {
      response.setStatusCode(HttpStatusCode.NOT_MODIFIED.getStatusCode());
    } else {
      // HTTP HEAD requires no payload but a 200 OK response
      if (HttpMethod.HEAD == request.getMethod()) {
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
      } else {
        final ODataSerializer serializer = this.odata.createSerializer(requestedContentType);
        response.setContent(
          serializer.serviceDocument(this.serviceMetadata, this.serviceRoot).getContent());
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, requestedContentType.toContentTypeString());
      }
    }
  }
}
