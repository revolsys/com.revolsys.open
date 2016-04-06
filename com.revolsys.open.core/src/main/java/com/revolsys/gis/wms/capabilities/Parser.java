package com.revolsys.gis.wms.capabilities;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.record.io.format.xml.StaxReader;
import com.revolsys.record.io.format.xml.XmlProcessor;
import com.revolsys.record.io.format.xml.XmlProcessorContext;

public class Parser extends XmlProcessor {
  private WmsCapabilities capabilities;

  public Parser(final XmlProcessorContext processorContext) {
    super("urn:x-revolsys-com:iaf:core:config");
    setContext(processorContext);
  }

  public Attribution processAttribution(final StaxReader in)
    throws XMLStreamException, IOException {
    final Attribution attribution = new Attribution();
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {

      final String tagName = in.getName().getLocalPart();
      if (tagName.equals("Title")) {
        final String title = in.getElementText();
        attribution.setTitle(title);
      } else if (tagName.equals("LogoURL")) {
        final ImageUrl logoUrl = processImageUrl(in);
        attribution.setLogoUrl(logoUrl);
      } else {
        final Object object = process(in);
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

  public AuthorityUrl processAuthorityURL(final StaxReader in)
    throws XMLStreamException, IOException {
    final AuthorityUrl authorityUrl = new AuthorityUrl();
    final String name = in.getAttributeValue(null, "name");
    authorityUrl.setName(name);
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {
      final Object object = process(in);
      if (object instanceof URL) {
        authorityUrl.setOnlineResource((URL)object);
      }
    }
    return authorityUrl;
  }

  public WmsBoundingBox processBoundingBox(final StaxReader in)
    throws XMLStreamException, IOException {
    final WmsBoundingBox wmsBoundingBox = new WmsBoundingBox();
    final double minX = in.getDoubleAttribute(null, "minx");
    final double maxX = in.getDoubleAttribute(null, "maxx");
    final double minY = in.getDoubleAttribute(null, "miny");
    final double maxY = in.getDoubleAttribute(null, "maxy");
    final BoundingBox envelope = new BoundingBoxDoubleGf(2, minX, minY, maxX, maxY);
    wmsBoundingBox.setEnvelope(envelope);
    final double resX = in.getDoubleAttribute(null, "resx");
    wmsBoundingBox.setResX(resX);
    final double resY = in.getDoubleAttribute(null, "resy");
    wmsBoundingBox.setResY(resY);
    final String srs = in.getAttributeValue(null, "SRS");
    wmsBoundingBox.setSrs(srs);
    in.skipSubTree();
    return wmsBoundingBox;

  }

  public Capability processCapability(final StaxReader in) throws XMLStreamException, IOException {
    final Capability capability = new Capability();
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {
      if (in.getName().getLocalPart().equals("Request")) {
        final List<Request> requests = processRequest(in);
        capability.setRequests(requests);
      } else if (in.getName().getLocalPart().equals("Exception")) {
        while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {
          if (in.getName().getLocalPart().equals("Format")) {
            final String format = in.getElementText();
            capability.addExceptionFormat(format);
          } else {
            process(in);
          }
        }
      } else {
        final Object object = process(in);
        if (object instanceof WmsLayer) {
          final WmsLayer layer = (WmsLayer)object;
          capability.setLayer(layer);
        }
      }
    }
    return capability;
  }

  public ContactAddress processContactAddress(final StaxReader in)
    throws XMLStreamException, IOException {
    final ContactAddress contactAddress = new ContactAddress();
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {

      final String tagName = in.getName().getLocalPart();
      if (tagName.equals("AddressType")) {
        final String addressType = in.getElementText();
        contactAddress.setAddressType(addressType);
      } else if (tagName.equals("Address")) {
        final String address = in.getElementText();
        contactAddress.setAddress(address);
      } else if (tagName.equals("City")) {
        final String city = in.getElementText();
        contactAddress.setCity(city);
      } else if (tagName.equals("StateOrProvince")) {
        final String stateOrProvince = in.getElementText();
        contactAddress.setStateOrProvince(stateOrProvince);
      } else if (tagName.equals("PostCode")) {
        final String postCode = in.getElementText();
        contactAddress.setPostCode(postCode);
      } else if (tagName.equals("Country")) {
        final String country = in.getElementText();
        contactAddress.setCountry(country);
      } else {
        process(in);
      }

    }
    return contactAddress;
  }

  public ContactInformation processContactInformation(final StaxReader in)
    throws XMLStreamException, IOException {
    final ContactInformation conact = new ContactInformation();
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {

      final String tagName = in.getName().getLocalPart();
      if (tagName.equals("ContactPosition")) {
        final String contactPosition = in.getElementText();
        conact.setContactPosition(contactPosition);
      } else if (tagName.equals("ContactVoiceTelephone")) {
        final String contactVoiceTelephone = in.getElementText();
        conact.setContactVoiceTelephone(contactVoiceTelephone);
      } else if (tagName.equals("ContactFacsimileTelephone")) {
        final String contactFacsimileTelephone = in.getElementText();
        conact.setContactFacsimileTelephone(contactFacsimileTelephone);
      } else if (tagName.equals("ContactElectronicMailAddress")) {
        final String contactElectronicMailAddress = in.getElementText();
        conact.setContactElectronicMailAddress(contactElectronicMailAddress);
      } else {
        final Object object = process(in);
        if (object instanceof ContactPersonPrimary) {
          conact.setContactPersonPrimary((ContactPersonPrimary)object);
        } else if (object instanceof ContactAddress) {
          conact.setContactAddress((ContactAddress)object);
        }
      }

    }
    return conact;
  }

  public ContactPersonPrimary processContactPersonPrimary(final StaxReader in)
    throws XMLStreamException, IOException {
    final ContactPersonPrimary conactPerson = new ContactPersonPrimary();
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {

      final String tagName = in.getName().getLocalPart();
      if (tagName.equals("ContactPerson")) {
        final String contactPerson = in.getElementText();
        conactPerson.setContactPerson(contactPerson);
      } else if (tagName.equals("ContactOrganization")) {
        final String contactOrganization = in.getElementText();
        conactPerson.setContactOrganization(contactOrganization);
      } else {
        process(in);
      }

    }
    return conactPerson;
  }

  public DcpType processDCPType(final StaxReader in) throws XMLStreamException, IOException {
    DcpType type = null;
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {
      final Object object = process(in);
      if (object instanceof DcpType) {
        type = (DcpType)object;
      }
    }
    return type;
  }

  public Dimension processDimension(final StaxReader in) throws XMLStreamException, IOException {
    final Dimension dimension = new Dimension();
    final String name = in.getAttributeValue(null, "name");
    dimension.setName(name);
    final String units = in.getAttributeValue(null, "units");
    dimension.setUnits(units);
    final String unitSymbol = in.getAttributeValue(null, "unitSymbol");
    dimension.setUnitSymbol(unitSymbol);
    in.skipSubTree();
    return dimension;

  }

  public Extent processExtent(final StaxReader in) throws XMLStreamException, IOException {
    final Extent extent = new Extent();
    final String name = in.getAttributeValue(null, "name");
    extent.setName(name);
    in.skipSubTree();
    final String defaultValue = in.getAttributeValue(null, "default");
    extent.setDefaultValue(defaultValue);
    in.skipSubTree();
    final String nearestValue = in.getAttributeValue(null, "nearestValue");
    extent.setNearestValue("1".equals(nearestValue));
    in.skipSubTree();
    final String multipleValues = in.getAttributeValue(null, "multipleValues");
    extent.setMultipleValues("1".equals(multipleValues));
    in.skipSubTree();
    final String current = in.getAttributeValue(null, "current");
    extent.setCurrent("1".equals(current));
    in.skipSubTree();
    return extent;

  }

  public FormatUrl processFormatUrl(final StaxReader in) throws XMLStreamException, IOException {
    final FormatUrl formatUrl = new FormatUrl();
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {
      final String tagName = in.getName().getLocalPart();
      if (tagName.equals("Format")) {
        final String format = in.getElementText();
        formatUrl.setFormat(format);
      } else {
        final Object object = process(in);
        if (object instanceof URL) {
          formatUrl.setOnlineResource((URL)object);
        }
      }
    }
    return formatUrl;
  }

  public HttpDcpType processHTTP(final StaxReader in) throws XMLStreamException, IOException {
    final HttpDcpType type = new HttpDcpType();
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {
      final String name = in.getName().getLocalPart();
      final HttpMethod method = new HttpMethod();
      type.addMethod(method);
      method.setName(name);
      while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {
        final Object object = process(in);
        if (object instanceof URL) {
          method.setOnlineResource((URL)object);
        }
      }
    }
    return type;
  }

  public WmsIdentifier processIdentifier(final StaxReader in)
    throws XMLStreamException, IOException {
    final WmsIdentifier wmsIdentifier = new WmsIdentifier();
    final String authority = in.getAttributeValue(null, "authority");
    wmsIdentifier.setAuthority(authority);
    final String value = in.getElementText();
    wmsIdentifier.setValue(value);
    return wmsIdentifier;
  }

  public ImageUrl processImageUrl(final StaxReader in) throws XMLStreamException, IOException {
    final ImageUrl imageUrl = new ImageUrl();
    final int width = in.getIntAttribute(null, "width");
    imageUrl.setWidth(width);
    final int height = in.getIntAttribute(null, "height");
    imageUrl.setHeight(height);
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {
      final String tagName = in.getName().getLocalPart();
      if (tagName.equals("Format")) {
        final String format = in.getElementText();
        imageUrl.setFormat(format);
      } else {
        final Object object = process(in);
        if (object instanceof URL) {
          imageUrl.setOnlineResource((URL)object);
        }
      }
    }
    return imageUrl;
  }

  public List<String> processKeywordList(final StaxReader in)
    throws XMLStreamException, IOException {
    final List<String> keywords = new ArrayList<String>();
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {

      final String tagName = in.getName().getLocalPart();
      if (tagName.equals("Keyword")) {
        final String keyword = in.getElementText();
        keywords.add(keyword);
      } else {
        process(in);
      }
    }
    return keywords;
  }

  public BoundingBox processLatLonBoundingBox(final StaxReader in)
    throws XMLStreamException, IOException {
    final double minX = in.getDoubleAttribute(null, "minx");
    final double maxX = in.getDoubleAttribute(null, "maxx");
    final double minY = in.getDoubleAttribute(null, "miny");
    final double maxY = in.getDoubleAttribute(null, "maxy");
    final BoundingBox envelope = new BoundingBoxDoubleGf(2, minX, minY, maxX, maxY);
    in.skipSubTree();
    return envelope;

  }

  public WmsLayer processLayer(final StaxReader in) throws XMLStreamException, IOException {
    final WmsLayer layer = new WmsLayer();
    final String queryable = in.getAttributeValue(null, "queryable");
    layer.setQueryable("1".equals(queryable));
    final String opaque = in.getAttributeValue(null, "opaque");
    layer.setOpaque("1".equals(opaque));
    final String noSubsets = in.getAttributeValue(null, "noSubsets");
    layer.setNoSubsets("1".equals(noSubsets));
    final int cascaded = in.getIntAttribute(null, "cascaded");
    layer.setCascaded(cascaded);
    final int fixedWidth = in.getIntAttribute(null, "fixedWidth");
    layer.setFixedWidth(fixedWidth);
    final int fixedHeight = in.getIntAttribute(null, "fixedHeight");
    layer.setFixedHeight(fixedHeight);
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {
      final String tagName = in.getName().getLocalPart();
      if (tagName.equals("Name")) {
        final String name = in.getElementText();
        layer.setName(name);
      } else if (tagName.equals("Title")) {
        final String title = in.getElementText();
        layer.setTitle(title);
      } else if (tagName.equals("Abstract")) {
        final String abstractDescription = in.getElementText();
        layer.setAbstractDescription(abstractDescription);
      } else if (tagName.equals("KeywordList")) {
        final List<String> keywords = processKeywordList(in);
        layer.setKeywords(keywords);
      } else if (tagName.equals("SRS")) {
        final String srs = in.getElementText();
        layer.addSrs(srs);
      } else if (tagName.equals("DataURL")) {
        layer.addDataUrl(processFormatUrl(in));
      } else if (tagName.equals("FeatureListURL")) {
        layer.addFeatureListUrl(processFormatUrl(in));
      } else {
        final Object object = process(in);
        if (object instanceof BoundingBox) {
          layer.setLatLonBoundingBox((BoundingBox)object);
        } else if (object instanceof WmsBoundingBox) {
          layer.addBoundingBox((WmsBoundingBox)object);
        } else if (object instanceof Dimension) {
          layer.addDimension((Dimension)object);
        } else if (object instanceof Extent) {
          layer.addExtent((Extent)object);
        } else if (object instanceof Attribution) {
          layer.setAttribution((Attribution)object);
        } else if (object instanceof AuthorityUrl) {
          layer.addAuthorityUrl((AuthorityUrl)object);
        } else if (object instanceof WmsIdentifier) {
          layer.addIdentifier((WmsIdentifier)object);
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

  public MetadataUrl processMetadataURL(final StaxReader in)
    throws XMLStreamException, IOException {
    final MetadataUrl recordDefinitionUrl = new MetadataUrl();
    final String type = in.getAttributeValue(null, "type");
    recordDefinitionUrl.setType(type);
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {
      final String tagName = in.getName().getLocalPart();
      if (tagName.equals("Format")) {
        final String format = in.getElementText();
        recordDefinitionUrl.setFormat(format);
      } else {
        final Object object = process(in);
        if (object instanceof URL) {
          recordDefinitionUrl.setOnlineResource((URL)object);
        }
      }
    }
    return recordDefinitionUrl;
  }

  public URL processOnlineResource(final StaxReader in) throws XMLStreamException, IOException {
    final String url = in.getAttributeValue("http://www.w3.org/1999/xlink", "href");
    in.skipSubTree();
    return new URL(url);
  }

  public List<Request> processRequest(final StaxReader in) throws XMLStreamException, IOException {
    final List<Request> requests = new ArrayList<Request>();
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {
      final Request request = new Request();
      final String name = in.getName().getLocalPart();
      request.setName(name);
      while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {
        if (in.getName().getLocalPart().equals("Format")) {
          final String format = in.getElementText();
          request.addFormat(format);
        } else {
          final Object object = process(in);
          if (object instanceof DcpType) {
            request.addDcpType((DcpType)object);
          }
        }
      }
      requests.add(request);
    }
    return requests;
  }

  public ScaleHint processScaleHint(final StaxReader in) throws XMLStreamException, IOException {
    final double min = in.getDoubleAttribute(null, "min");
    final double max = in.getDoubleAttribute(null, "max");
    final ScaleHint scaleHint = new ScaleHint();
    scaleHint.setMax(max);
    scaleHint.setMin(min);
    in.skipSubTree();
    return scaleHint;

  }

  public Service processService(final StaxReader in) throws XMLStreamException, IOException {
    final Service service = new Service();
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {

      final String tagName = in.getName().getLocalPart();
      if (tagName.equals("Name")) {
        final String name = in.getElementText();
        service.setName(name);
      } else if (tagName.equals("Title")) {
        final String title = in.getElementText();
        service.setTitle(title);
      } else if (tagName.equals("Abstract")) {
        final String abstractDescription = in.getElementText();
        service.setAbstractDescription(abstractDescription);
      } else if (tagName.equals("KeywordList")) {
        final List<String> keywords = processKeywordList(in);
        service.setKeywords(keywords);
      } else if (tagName.equals("OnlineResource")) {
        final URL onlineResource = (URL)process(in);
        service.setOnlineResource(onlineResource);
      } else if (tagName.equals("Fees")) {
        final String fees = in.getElementText();
        service.setFees(fees);
      } else if (tagName.equals("AccessConstraints")) {
        final String accessConstraints = in.getElementText();
        service.setAccessConstraints(accessConstraints);
      } else {
        final Object object = process(in);
        if (object instanceof ContactInformation) {
          service.setContactInformation((ContactInformation)object);
        }
      }

    }
    return service;
  }

  public Style processStyle(final StaxReader in) throws XMLStreamException, IOException {
    final Style style = new Style();
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {

      final String tagName = in.getName().getLocalPart();
      if (tagName.equals("Name")) {
        final String name = in.getElementText();
        style.setName(name);
      } else if (tagName.equals("Title")) {
        final String title = in.getElementText();
        style.setTitle(title);
      } else if (tagName.equals("Abstract")) {
        final String abstractDescription = in.getElementText();
        style.setAbstractDescription(abstractDescription);
      } else if (tagName.equals("LegendURL")) {
        final ImageUrl legendUrl = processImageUrl(in);
        style.addLegendUrl(legendUrl);
      } else {
        final Object object = process(in);
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

  public WmsCapabilities processWMS_Capabilities(final StaxReader in)
    throws XMLStreamException, IOException {
    this.capabilities = new WmsCapabilities();
    final String version = in.getAttributeValue(null, "version");
    final String updateSequence = in.getAttributeValue(null, "updateSequence");
    this.capabilities.setVersion(version);
    this.capabilities.setUpdateSequence(updateSequence);
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {
      final Object object = process(in);
      if (object instanceof Service) {
        this.capabilities.setService((Service)object);
      } else if (object instanceof Capability) {
        this.capabilities.setCapability((Capability)object);
      }
    }
    return this.capabilities;
  }

  public WmsCapabilities processWMT_MS_Capabilities(final StaxReader in)
    throws XMLStreamException, IOException {
    this.capabilities = new WmsCapabilities();
    final String version = in.getAttributeValue(null, "version");
    final String updateSequence = in.getAttributeValue(null, "updateSequence");
    this.capabilities.setVersion(version);
    this.capabilities.setUpdateSequence(updateSequence);
    while (in.nextTag() == XMLStreamConstants.START_ELEMENT) {
      final Object object = process(in);
      if (object instanceof Service) {
        this.capabilities.setService((Service)object);
      } else if (object instanceof Capability) {
        this.capabilities.setCapability((Capability)object);
      }
    }
    return this.capabilities;
  }
}
