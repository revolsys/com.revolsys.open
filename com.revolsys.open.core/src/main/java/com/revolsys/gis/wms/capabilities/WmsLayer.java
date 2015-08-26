package com.revolsys.gis.wms.capabilities;

import java.util.ArrayList;
import java.util.List;

public class WmsLayer {
  private String abstractDescription;

  private Attribution attribution;

  private List<AuthorityUrl> authorityUrls = new ArrayList<AuthorityUrl>();

  private List<BoundingBox> boundingBoxes = new ArrayList<BoundingBox>();

  private int cascaded;

  private List<FormatUrl> dataUrls = new ArrayList<FormatUrl>();

  private List<Dimension> dimensions = new ArrayList<Dimension>();

  private List<Extent> extents = new ArrayList<Extent>();

  private List<FormatUrl> featureListUrls = new ArrayList<FormatUrl>();

  private int fixedHeight;

  private int fixedWidth;

  private List<Identifier> identifiers = new ArrayList<Identifier>();

  private List<String> keywords = new ArrayList<String>();

  private com.revolsys.geometry.model.BoundingBox latLonBoundingBox;

  private List<WmsLayer> layers = new ArrayList<WmsLayer>();

  private String name;

  private boolean noSubsets;

  private boolean opaque;

  private WmsLayer parent;

  private boolean queryable;

  private List<MetadataUrl> recordDefinitionUrls = new ArrayList<MetadataUrl>();

  private ScaleHint scaleHint;

  private final List<String> srs = new ArrayList<String>();

  private List<Style> styles = new ArrayList<Style>();

  private String title;

  public void addAuthorityUrl(final AuthorityUrl authorityUrl) {
    this.authorityUrls.add(authorityUrl);

  }

  public void addBoundingBox(final BoundingBox boundingBox) {
    this.boundingBoxes.add(boundingBox);
  }

  public void addDataUrl(final FormatUrl dataUrl) {
    this.dataUrls.add(dataUrl);

  }

  public void addDimension(final Dimension dimension) {
    this.dimensions.add(dimension);

  }

  public void addExtent(final Extent extent) {
    this.extents.add(extent);

  }

  public void addFeatureListUrl(final FormatUrl featureListUrl) {
    this.featureListUrls.add(featureListUrl);

  }

  public void addIdentifier(final Identifier identifier) {
    this.identifiers.add(identifier);
  }

  public void addLayer(final WmsLayer layer) {
    this.layers.add(layer);
    layer.setParent(this);

  }

  public void addMetaDataUrl(final MetadataUrl recordDefinitionUrl) {
    this.recordDefinitionUrls.add(recordDefinitionUrl);

  }

  public void addSrs(final String srs) {
    this.srs.add(srs);
  }

  public void addStyle(final Style style) {
    this.styles.add(style);
  }

  public String getAbstractDescription() {
    return this.abstractDescription;
  }

  public Attribution getAttribution() {
    return this.attribution;
  }

  public List<AuthorityUrl> getAuthorityUrls() {
    return this.authorityUrls;
  }

  public List<BoundingBox> getBoundingBoxes() {
    return this.boundingBoxes;
  }

  public int getCascaded() {
    return this.cascaded;
  }

  public List<FormatUrl> getDataUrls() {
    return this.dataUrls;
  }

  public List<Dimension> getDimensions() {
    return this.dimensions;
  }

  public List<Extent> getExtents() {
    return this.extents;
  }

  public List<FormatUrl> getFeatureListUrls() {
    return this.featureListUrls;
  }

  public int getFixedHeight() {
    return this.fixedHeight;
  }

  public int getFixedWidth() {
    return this.fixedWidth;
  }

  public List<Identifier> getIdentifiers() {
    return this.identifiers;
  }

  public List<String> getKeywords() {
    return this.keywords;
  }

  public com.revolsys.geometry.model.BoundingBox getLatLonBoundingBox() {
    return this.latLonBoundingBox;
  }

  public List<WmsLayer> getLayers() {
    return this.layers;
  }

  public String getName() {
    return this.name;
  }

  public WmsLayer getParent() {
    return this.parent;
  }

  public List<MetadataUrl> getRecordDefinitionUrls() {
    return this.recordDefinitionUrls;
  }

  public ScaleHint getScaleHint() {
    return this.scaleHint;
  }

  public List<String> getSrs() {
    return this.srs;
  }

  public List<Style> getStyles() {
    return this.styles;
  }

  public String getTitle() {
    return this.title;
  }

  public boolean isNoSubsets() {
    return this.noSubsets;
  }

  public boolean isOpaque() {
    return this.opaque;
  }

  public boolean isQueryable() {
    return this.queryable;
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

  public void setLatLonBoundingBox(final com.revolsys.geometry.model.BoundingBox latLonBoundingBox) {
    this.latLonBoundingBox = latLonBoundingBox;
  }

  public void setLayers(final List<WmsLayer> layers) {
    this.layers = layers;
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

  public void setParent(final WmsLayer parent) {
    this.parent = parent;
  }

  public void setQueryable(final boolean queryable) {
    this.queryable = queryable;
  }

  public void setRecordDefinitionUrls(final List<MetadataUrl> recordDefinitionUrls) {
    this.recordDefinitionUrls = recordDefinitionUrls;
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
    if (this.title != null) {
      return this.title;
    } else {
      return this.name;
    }
  }
}
