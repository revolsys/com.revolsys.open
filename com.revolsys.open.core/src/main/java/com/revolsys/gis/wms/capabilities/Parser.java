package com.revolsys.gis.wms.capabilities;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;

import com.revolsys.io.xml.StaxUtils;
import com.revolsys.io.xml.XmlProcessor;
import com.revolsys.io.xml.XmlProcessorContext;
import com.revolsys.jts.geom.Envelope;

public class Parser extends XmlProcessor {
  private static final Logger log = Logger.getLogger(Parser.class);

  private static final Class[] PROCESS_METHOD_ARGS = new Class[] {
    XMLStreamReader.class
  };

  private WmsCapabilities capabilities;

  public Parser(final XmlProcessorContext processorContext) {
    super("urn:x-revolsys-com:iaf:core:config");
    setContext(processorContext);
  }

  public Attribution processAttribution(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    final Attribution attribution = new Attribution();
    while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {

      final String tagName = parser.getName().getLocalPart();
      if (tagName.equals("Title")) {
        final String title = StaxUtils.getElementText(parser);
        attribution.setTitle(title);
      } else if (tagName.equals("LogoURL")) {
        final ImageUrl logoUrl = processImageUrl(parser);
        attribution.setLogoUrl(logoUrl);
      } else {
        final Object object = process(parser);
        if (object instanceof URL) {
          final URL onlineResource = (URL)object;
          attribution.setOnlineResource(onlineResource);
        } else if (object instanceof ImageUrl) {
          final ImageUrl logoUrl = (ImageUrl)object;
          attribution.setLogoUrl(logoUrl);
        }
      }
    }
    return attribution;

  }

  public AuthorityUrl processAuthorityURL(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    final AuthorityUrl authorityUrl = new AuthorityUrl();
    final String name = parser.getAttributeValue(null, "name");
    authorityUrl.setName(name);
    while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {
      final Object object = process(parser);
      if (object instanceof URL) {
        authorityUrl.setOnlineResource((URL)object);
      }
    }
    return authorityUrl;
  }

  public BoundingBox processBoundingBox(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    final BoundingBox boundingBox = new BoundingBox();
    final double minX = StaxUtils.getDoubleAttribute(parser, null, "minx");
    final double maxX = StaxUtils.getDoubleAttribute(parser, null, "maxx");
    final double minY = StaxUtils.getDoubleAttribute(parser, null, "miny");
    final double maxY = StaxUtils.getDoubleAttribute(parser, null, "maxy");
    final com.revolsys.jts.geom.BoundingBox envelope = new Envelope(minX, minY,
      maxX, maxY);
    boundingBox.setEnvelope(envelope);
    final double resX = StaxUtils.getDoubleAttribute(parser, null, "resx");
    boundingBox.setResX(resX);
    final double resY = StaxUtils.getDoubleAttribute(parser, null, "resy");
    boundingBox.setResY(resY);
    final String srs = parser.getAttributeValue(null, "SRS");
    boundingBox.setSrs(srs);
    StaxUtils.skipSubTree(parser);
    return boundingBox;

  }

  public Capability processCapability(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    final Capability capability = new Capability();
    while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {
      if (parser.getName().getLocalPart().equals("Request")) {
        final List<Request> requests = processRequest(parser);
        capability.setRequests(requests);
      } else if (parser.getName().getLocalPart().equals("Exception")) {
        while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {
          if (parser.getName().getLocalPart().equals("Format")) {
            final String format = StaxUtils.getElementText(parser);
            capability.addExceptionFormat(format);
          } else {
            process(parser);
          }
        }
      } else {
        final Object object = process(parser);
        if (object instanceof WmsLayer) {
          final WmsLayer layer = (WmsLayer)object;
          capability.setLayer(layer);
        }
      }
    }
    return capability;
  }

  public ContactAddress processContactAddress(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    final ContactAddress contactAddress = new ContactAddress();
    while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {

      final String tagName = parser.getName().getLocalPart();
      if (tagName.equals("AddressType")) {
        final String addressType = StaxUtils.getElementText(parser);
        contactAddress.setAddressType(addressType);
      } else if (tagName.equals("Address")) {
        final String address = StaxUtils.getElementText(parser);
        contactAddress.setAddress(address);
      } else if (tagName.equals("City")) {
        final String city = StaxUtils.getElementText(parser);
        contactAddress.setCity(city);
      } else if (tagName.equals("StateOrProvince")) {
        final String stateOrProvince = StaxUtils.getElementText(parser);
        contactAddress.setStateOrProvince(stateOrProvince);
      } else if (tagName.equals("PostCode")) {
        final String postCode = StaxUtils.getElementText(parser);
        contactAddress.setPostCode(postCode);
      } else if (tagName.equals("Country")) {
        final String country = StaxUtils.getElementText(parser);
        contactAddress.setCountry(country);
      } else {
        process(parser);
      }

    }
    return contactAddress;
  }

  public ContactInformation processContactInformation(
    final XMLStreamReader parser) throws XMLStreamException, IOException {
    final ContactInformation conact = new ContactInformation();
    while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {

      final String tagName = parser.getName().getLocalPart();
      if (tagName.equals("ContactPosition")) {
        final String contactPosition = StaxUtils.getElementText(parser);
        conact.setContactPosition(contactPosition);
      } else if (tagName.equals("ContactVoiceTelephone")) {
        final String contactVoiceTelephone = StaxUtils.getElementText(parser);
        conact.setContactVoiceTelephone(contactVoiceTelephone);
      } else if (tagName.equals("ContactFacsimileTelephone")) {
        final String contactFacsimileTelephone = StaxUtils.getElementText(parser);
        conact.setContactFacsimileTelephone(contactFacsimileTelephone);
      } else if (tagName.equals("ContactElectronicMailAddress")) {
        final String contactElectronicMailAddress = StaxUtils.getElementText(parser);
        conact.setContactElectronicMailAddress(contactElectronicMailAddress);
      } else {
        final Object object = process(parser);
        if (object instanceof ContactPersonPrimary) {
          conact.setContactPersonPrimary((ContactPersonPrimary)object);
        } else if (object instanceof ContactAddress) {
          conact.setContactAddress((ContactAddress)object);
        }
      }

    }
    return conact;
  }

  public ContactPersonPrimary processContactPersonPrimary(
    final XMLStreamReader parser) throws XMLStreamException, IOException {
    final ContactPersonPrimary conactPerson = new ContactPersonPrimary();
    while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {

      final String tagName = parser.getName().getLocalPart();
      if (tagName.equals("ContactPerson")) {
        final String contactPerson = StaxUtils.getElementText(parser);
        conactPerson.setContactPerson(contactPerson);
      } else if (tagName.equals("ContactOrganization")) {
        final String contactOrganization = StaxUtils.getElementText(parser);
        conactPerson.setContactOrganization(contactOrganization);
      } else {
        process(parser);
      }

    }
    return conactPerson;
  }

  public DcpType processDCPType(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    DcpType type = null;
    while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {
      final Object object = process(parser);
      if (object instanceof DcpType) {
        type = (DcpType)object;
      }
    }
    return type;
  }

  public Dimension processDimension(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    final Dimension dimension = new Dimension();
    final String name = parser.getAttributeValue(null, "name");
    dimension.setName(name);
    final String units = parser.getAttributeValue(null, "units");
    dimension.setUnits(units);
    final String unitSymbol = parser.getAttributeValue(null, "unitSymbol");
    dimension.setUnitSymbol(unitSymbol);
    StaxUtils.skipSubTree(parser);
    return dimension;

  }

  public Extent processExtent(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    final Extent extent = new Extent();
    final String name = parser.getAttributeValue(null, "name");
    extent.setName(name);
    StaxUtils.skipSubTree(parser);
    final String defaultValue = parser.getAttributeValue(null, "default");
    extent.setDefaultValue(defaultValue);
    StaxUtils.skipSubTree(parser);
    final String nearestValue = parser.getAttributeValue(null, "nearestValue");
    extent.setNearestValue("1".equals(nearestValue));
    StaxUtils.skipSubTree(parser);
    final String multipleValues = parser.getAttributeValue(null,
      "multipleValues");
    extent.setMultipleValues("1".equals(multipleValues));
    StaxUtils.skipSubTree(parser);
    final String current = parser.getAttributeValue(null, "current");
    extent.setCurrent("1".equals(current));
    StaxUtils.skipSubTree(parser);
    return extent;

  }

  public FormatUrl processFormatUrl(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    final FormatUrl formatUrl = new FormatUrl();
    while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {
      final String tagName = parser.getName().getLocalPart();
      if (tagName.equals("Format")) {
        final String format = StaxUtils.getElementText(parser);
        formatUrl.setFormat(format);
      } else {
        final Object object = process(parser);
        if (object instanceof URL) {
          formatUrl.setOnlineResource((URL)object);
        }
      }
    }
    return formatUrl;
  }

  public HttpDcpType processHTTP(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    final HttpDcpType type = new HttpDcpType();
    while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {
      final String name = parser.getName().getLocalPart();
      final HttpMethod method = new HttpMethod();
      type.addMethod(method);
      method.setName(name);
      while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {
        final Object object = process(parser);
        if (object instanceof URL) {
          method.setOnlineResource((URL)object);
        }
      }
    }
    return type;
  }

  public Identifier processIdentifier(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    final Identifier identifier = new Identifier();
    final String authority = parser.getAttributeValue(null, "authority");
    identifier.setAuthority(authority);
    final String value = StaxUtils.getElementText(parser);
    identifier.setValue(value);
    return identifier;
  }

  public ImageUrl processImageUrl(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    final ImageUrl imageUrl = new ImageUrl();
    final int width = StaxUtils.getIntAttribute(parser, null, "width");
    imageUrl.setWidth(width);
    final int height = StaxUtils.getIntAttribute(parser, null, "height");
    imageUrl.setHeight(height);
    while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {
      final String tagName = parser.getName().getLocalPart();
      if (tagName.equals("Format")) {
        final String format = StaxUtils.getElementText(parser);
        imageUrl.setFormat(format);
      } else {
        final Object object = process(parser);
        if (object instanceof URL) {
          imageUrl.setOnlineResource((URL)object);
        }
      }
    }
    return imageUrl;
  }

  public List<String> processKeywordList(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    final List<String> keywords = new ArrayList<String>();
    while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {

      final String tagName = parser.getName().getLocalPart();
      if (tagName.equals("Keyword")) {
        final String keyword = StaxUtils.getElementText(parser);
        keywords.add(keyword);
      } else {
        process(parser);
      }
    }
    return keywords;
  }

  public com.revolsys.jts.geom.BoundingBox processLatLonBoundingBox(
    final XMLStreamReader parser) throws XMLStreamException, IOException {
    final double minX = StaxUtils.getDoubleAttribute(parser, null, "minx");
    final double maxX = StaxUtils.getDoubleAttribute(parser, null, "maxx");
    final double minY = StaxUtils.getDoubleAttribute(parser, null, "miny");
    final double maxY = StaxUtils.getDoubleAttribute(parser, null, "maxy");
    final com.revolsys.jts.geom.BoundingBox envelope = new Envelope(minX, minY,
      maxX, maxY);
    StaxUtils.skipSubTree(parser);
    return envelope;

  }

  public WmsLayer processLayer(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    final WmsLayer layer = new WmsLayer();
    final String queryable = parser.getAttributeValue(null, "queryable");
    layer.setQueryable("1".equals(queryable));
    final String opaque = parser.getAttributeValue(null, "opaque");
    layer.setOpaque("1".equals(opaque));
    final String noSubsets = parser.getAttributeValue(null, "noSubsets");
    layer.setNoSubsets("1".equals(noSubsets));
    final int cascaded = StaxUtils.getIntAttribute(parser, null, "cascaded");
    layer.setCascaded(cascaded);
    final int fixedWidth = StaxUtils.getIntAttribute(parser, null, "fixedWidth");
    layer.setFixedWidth(fixedWidth);
    final int fixedHeight = StaxUtils.getIntAttribute(parser, null,
      "fixedHeight");
    layer.setFixedHeight(fixedHeight);
    while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {
      final String tagName = parser.getName().getLocalPart();
      if (tagName.equals("Column")) {
        final String name = StaxUtils.getElementText(parser);
        layer.setName(name);
      } else if (tagName.equals("Title")) {
        final String title = StaxUtils.getElementText(parser);
        layer.setTitle(title);
      } else if (tagName.equals("Abstract")) {
        final String abstractDescription = StaxUtils.getElementText(parser);
        layer.setAbstractDescription(abstractDescription);
      } else if (tagName.equals("KeywordList")) {
        final List<String> keywords = processKeywordList(parser);
        layer.setKeywords(keywords);
      } else if (tagName.equals("SRS")) {
        final String srs = StaxUtils.getElementText(parser);
        layer.addSrs(srs);
      } else if (tagName.equals("DataURL")) {
        layer.addDataUrl(processFormatUrl(parser));
      } else if (tagName.equals("FeatureListURL")) {
        layer.addFeatureListUrl(processFormatUrl(parser));
      } else {
        final Object object = process(parser);
        if (object instanceof com.revolsys.jts.geom.BoundingBox) {
          layer.setLatLonBoundingBox((com.revolsys.jts.geom.BoundingBox)object);
        } else if (object instanceof BoundingBox) {
          layer.addBoundingBox((BoundingBox)object);
        } else if (object instanceof Dimension) {
          layer.addDimension((Dimension)object);
        } else if (object instanceof Extent) {
          layer.addExtent((Extent)object);
        } else if (object instanceof Attribution) {
          layer.setAttribution((Attribution)object);
        } else if (object instanceof AuthorityUrl) {
          layer.addAuthorityUrl((AuthorityUrl)object);
        } else if (object instanceof Identifier) {
          layer.addIdentifier((Identifier)object);
        } else if (object instanceof MetadataUrl) {
          layer.addMetaDataUrl((MetadataUrl)object);
        } else if (object instanceof Style) {
          layer.addStyle((Style)object);
        } else if (object instanceof ScaleHint) {
          layer.setScaleHint((ScaleHint)object);
        } else if (object instanceof WmsLayer) {
          layer.addLayer((WmsLayer)object);
        }
      }

    }
    return layer;
  }

  public MetadataUrl processMetadataURL(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    final MetadataUrl metaDataUrl = new MetadataUrl();
    final String type = parser.getAttributeValue(null, "type");
    metaDataUrl.setType(type);
    while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {
      final String tagName = parser.getName().getLocalPart();
      if (tagName.equals("Format")) {
        final String format = StaxUtils.getElementText(parser);
        metaDataUrl.setFormat(format);
      } else {
        final Object object = process(parser);
        if (object instanceof URL) {
          metaDataUrl.setOnlineResource((URL)object);
        }
      }
    }
    return metaDataUrl;
  }

  public URL processOnlineResource(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    final String url = parser.getAttributeValue("http://www.w3.org/1999/xlink",
      "href");
    StaxUtils.skipSubTree(parser);
    return new URL(url);
  }

  public List<Request> processRequest(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    final List<Request> requests = new ArrayList<Request>();
    while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {
      final Request request = new Request();
      final String name = parser.getName().getLocalPart();
      request.setName(name);
      while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {
        if (parser.getName().getLocalPart().equals("Format")) {
          final String format = StaxUtils.getElementText(parser);
          request.addFormat(format);
        } else {
          final Object object = process(parser);
          if (object instanceof DcpType) {
            request.addDcpType((DcpType)object);
          }
        }
      }
      requests.add(request);
    }
    return requests;
  }

  public ScaleHint processScaleHint(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    final double min = StaxUtils.getDoubleAttribute(parser, null, "min");
    final double max = StaxUtils.getDoubleAttribute(parser, null, "max");
    final ScaleHint scaleHint = new ScaleHint();
    scaleHint.setMax(max);
    scaleHint.setMin(min);
    StaxUtils.skipSubTree(parser);
    return scaleHint;

  }

  public Service processService(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    final Service service = new Service();
    while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {

      final String tagName = parser.getName().getLocalPart();
      if (tagName.equals("Column")) {
        final String name = StaxUtils.getElementText(parser);
        service.setName(name);
      } else if (tagName.equals("Title")) {
        final String title = StaxUtils.getElementText(parser);
        service.setTitle(title);
      } else if (tagName.equals("Abstract")) {
        final String abstractDescription = StaxUtils.getElementText(parser);
        service.setAbstractDescription(abstractDescription);
      } else if (tagName.equals("KeywordList")) {
        final List<String> keywords = processKeywordList(parser);
        service.setKeywords(keywords);
      } else if (tagName.equals("OnlineResource")) {
        final URL onlineResource = (URL)process(parser);
        service.setOnlineResource(onlineResource);
      } else if (tagName.equals("Fees")) {
        final String fees = StaxUtils.getElementText(parser);
        service.setFees(fees);
      } else if (tagName.equals("AccessConstraints")) {
        final String accessConstraints = StaxUtils.getElementText(parser);
        service.setAccessConstraints(accessConstraints);
      } else {
        final Object object = process(parser);
        if (object instanceof ContactInformation) {
          service.setContactInformation((ContactInformation)object);
        }
      }

    }
    return service;
  }

  public Style processStyle(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    final Style style = new Style();
    while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {

      final String tagName = parser.getName().getLocalPart();
      if (tagName.equals("Column")) {
        final String name = StaxUtils.getElementText(parser);
        style.setName(name);
      } else if (tagName.equals("Title")) {
        final String title = StaxUtils.getElementText(parser);
        style.setTitle(title);
      } else if (tagName.equals("Abstract")) {
        final String abstractDescription = StaxUtils.getElementText(parser);
        style.setAbstractDescription(abstractDescription);
      } else if (tagName.equals("LegendURL")) {
        final ImageUrl legendUrl = processImageUrl(parser);
        style.addLegendUrl(legendUrl);
      } else {
        final Object object = process(parser);
        if (object instanceof FormatUrl) {
          if (tagName.equals("StyleSheetURL")) {
            style.setStyleSheetUrl((FormatUrl)object);
          } else if (tagName.equals("StyleURL")) {
            style.setStyleUrl((FormatUrl)object);
          }
        }
      }

    }
    return style;
  }

  public WmsCapabilities processWMS_Capabilities(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    capabilities = new WmsCapabilities();
    final String version = parser.getAttributeValue(null, "version");
    final String updateSequence = parser.getAttributeValue(null,
      "updateSequence");
    capabilities.setVersion(version);
    capabilities.setUpdateSequence(updateSequence);
    while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {
      final Object object = process(parser);
      if (object instanceof Service) {
        capabilities.setService((Service)object);
      } else if (object instanceof Capability) {
        capabilities.setCapability((Capability)object);
      }
    }
    return capabilities;
  }

  public WmsCapabilities processWMT_MS_Capabilities(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    capabilities = new WmsCapabilities();
    final String version = parser.getAttributeValue(null, "version");
    final String updateSequence = parser.getAttributeValue(null,
      "updateSequence");
    capabilities.setVersion(version);
    capabilities.setUpdateSequence(updateSequence);
    while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {
      final Object object = process(parser);
      if (object instanceof Service) {
        capabilities.setService((Service)object);
      } else if (object instanceof Capability) {
        capabilities.setCapability((Capability)object);
      }
    }
    return capabilities;
  }
}
