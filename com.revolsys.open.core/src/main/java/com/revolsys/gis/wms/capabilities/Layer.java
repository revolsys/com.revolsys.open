package com.revolsys.gis.wms.capabilities;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;

public class Layer {
  private String name;

  private String title;

  private String abstractDescription;

  private List<String> keywords = new ArrayList<String>();

  private final List<String> srs = new ArrayList<String>();

  private Envelope latLonBoundingBox;

  private List<BoundingBox> boundingBoxes = new ArrayList<BoundingBox>();

  private List<Dimension> dimensions = new ArrayList<Dimension>();

  private List<Extent> extents = new ArrayList<Extent>();

  private Attribution attribution;

  private List<AuthorityUrl> authorityUrls = new ArrayList<AuthorityUrl>();

  private List<Identifier> identifiers = new ArrayList<Identifier>();

  private List<MetadataUrl> metaDataUrls = new ArrayList<MetadataUrl>();

  private List<FormatUrl> dataUrls = new ArrayList<FormatUrl>();

  private List<FormatUrl> featureListUrls = new ArrayList<FormatUrl>();

  private List<Style> styles = new ArrayList<Style>();

  private ScaleHint scaleHint;

  private List<Layer> layers = new ArrayList<Layer>();

  private boolean queryable;

  private int cascaded;

  private boolean opaque;

  private boolean noSubsets;

  private int fixedWidth;

  private int fixedHeight;

  private Layer parent;

  public void addAuthorityUrl(final AuthorityUrl authorityUrl) {
    authorityUrls.add(authorityUrl);

  }

  public void addBoundingBox(final BoundingBox boundingBox) {
    boundingBoxes.add(boundingBox);
  }

  public void addDataUrl(final FormatUrl dataUrl) {
    dataUrls.add(dataUrl);

  }

  public void addDimension(final Dimension dimension) {
    dimensions.add(dimension);

  }

  public void addExtent(final Extent extent) {
    extents.add(extent);

  }

  public void addFeatureListUrl(final FormatUrl featureListUrl) {
    featureListUrls.add(featureListUrl);

  }

  public void addIdentifier(final Identifier identifier) {
    identifiers.add(identifier);
  }

  public void addLayer(final Layer layer) {
    layers.add(layer);
    layer.setParent(this);

  }

  public void addMetaDataUrl(final MetadataUrl metaDataUrl) {
    metaDataUrls.add(metaDataUrl);

  }

  public void addSrs(final String srs) {
    this.srs.add(srs);
  }

  public void addStyle(final Style style) {
    styles.add(style);
  }

  public String getAbstractDescription() {
    return abstractDescription;
  }

  public Attribution getAttribution() {
    return attribution;
  }

  public List<AuthorityUrl> getAuthorityUrls() {
    return authorityUrls;
  }

  public List<BoundingBox> getBoundingBoxes() {
    return boundingBoxes;
  }

  public int getCascaded() {
    return cascaded;
  }

  public List<FormatUrl> getDataUrls() {
    return dataUrls;
  }

  public List<Dimension> getDimensions() {
    return dimensions;
  }

  public List<Extent> getExtents() {
    return extents;
  }

  public List<FormatUrl> getFeatureListUrls() {
    return featureListUrls;
  }

  public int getFixedHeight() {
    return fixedHeight;
  }

  public int getFixedWidth() {
    return fixedWidth;
  }

  public List<Identifier> getIdentifiers() {
    return identifiers;
  }

  public List<String> getKeywords() {
    return keywords;
  }

  public Envelope getLatLonBoundingBox() {
    return latLonBoundingBox;
  }

  public List<Layer> getLayers() {
    return layers;
  }

  public List<MetadataUrl> getMetaDataUrls() {
    return metaDataUrls;
  }

  public String getName() {
    return name;
  }

  public Layer getParent() {
    return parent;
  }

  public ScaleHint getScaleHint() {
    return scaleHint;
  }

  public List<String> getSrs() {
    return srs;
  }

  public List<Style> getStyles() {
    return styles;
  }

  public String getTitle() {
    return title;
  }

  public boolean isNoSubsets() {
    return noSubsets;
  }

  public boolean isOpaque() {
    return opaque;
  }

  public boolean isQueryable() {
    return queryable;
  }

  public void setAbstractDescription(final String abstractDescription) {
    this.abstractDescription = abstractDescription;
  }

  public void setAttribution(final Attribution attribution) {
    this.attribution = attribution;
  }

  public void setAuthorityUrls(final List<AuthorityUrl> authorityUrls) {
    this.authorityUrls = authorityUrls;
  }

  public void setBoundingBoxes(final List<BoundingBox> boundingBoxes) {
    this.boundingBoxes = boundingBoxes;
  }

  public void setCascaded(final int cascaded) {
    this.cascaded = cascaded;
  }

  public void setDataUrls(final List<FormatUrl> dataUrls) {
    this.dataUrls = dataUrls;
  }

  public void setDimensions(final List<Dimension> dimensions) {
    this.dimensions = dimensions;
  }

  public void setExtents(final List<Extent> extents) {
    this.extents = extents;
  }

  public void setFeatureListUrls(final List<FormatUrl> featureListUrls) {
    this.featureListUrls = featureListUrls;
  }

  public void setFixedHeight(final int fixedHeight) {
    this.fixedHeight = fixedHeight;
  }

  public void setFixedWidth(final int fixedWidth) {
    this.fixedWidth = fixedWidth;
  }

  public void setIdentifiers(final List<Identifier> identifiers) {
    this.identifiers = identifiers;
  }

  public void setKeywords(final List<String> keywords) {
    this.keywords = keywords;
  }

  public void setLatLonBoundingBox(final Envelope latLonBoundingBox) {
    this.latLonBoundingBox = latLonBoundingBox;
  }

  public void setLayers(final List<Layer> layers) {
    this.layers = layers;
  }

  public void setMetaDataUrls(final List<MetadataUrl> metaDataUrls) {
    this.metaDataUrls = metaDataUrls;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setNoSubsets(final boolean noSubsets) {
    this.noSubsets = noSubsets;
  }

  public void setOpaque(final boolean opaque) {
    this.opaque = opaque;
  }

  public void setParent(final Layer parent) {
    this.parent = parent;
  }

  public void setQueryable(final boolean queryable) {
    this.queryable = queryable;
  }

  public void setScaleHint(final ScaleHint scaleHint) {
    this.scaleHint = scaleHint;
  }

  public void setStyles(final List<Style> styles) {
    this.styles = styles;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  @Override
  public String toString() {
    if (title != null) {
      return title;
    } else {
      return name;
    }
  }
}
