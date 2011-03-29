package com.revolsys.ui.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;

public class PageInfo extends AbstractDocumentedObject {

  private Map<String, PageInfo> pages = new LinkedHashMap<String, PageInfo>();

  private Map<String, ParameterInfo> parameters = new LinkedHashMap<String, ParameterInfo>();

  private final List<String> methods = new ArrayList<String>();

  private List<MediaType> mediaTypes = new ArrayList<MediaType>();

  private List<MediaType> inputContentTypes = new ArrayList<MediaType>();

  private String url;

  public PageInfo() {
  }

  public PageInfo(final PageInfo pageInfo) {
    super(pageInfo);
    this.methods.addAll(pageInfo.getMethods());
    this.pages.putAll(pageInfo.getPages());
    this.parameters.putAll(pageInfo.getParametersMap());
    this.mediaTypes.addAll(pageInfo.getMediaTypes());
    this.inputContentTypes.addAll(pageInfo.getInputContentTypes());
  }

  public PageInfo(final String title, final String description) {
    this(title, description, "get");
  }

  public PageInfo(final String title, final String description,
    final String... methods) {
    setTitle(title);
    setDescription(description);
    this.methods.addAll(Arrays.asList(methods));
  }

  public void addInputContentType(final MediaType mediaType) {
    inputContentTypes.add(mediaType);
  }

  public void addPage(final Object path, final PageInfo page) {
    addPage(path.toString(), page);
  }

  public void addPage(final String path, final PageInfo page) {
    pages.put(path, page);
  }

  public void addParameter(final ParameterInfo parameter) {
    parameters.put(parameter.getName(), parameter);
  }

  @Override
  public PageInfo clone() {
    return new PageInfo(this);
  }

  public List<MediaType> getInputContentTypes() {
    return inputContentTypes;
  }

  public List<MediaType> getMediaTypes() {
    return mediaTypes;
  }

  public List<String> getMethods() {
    return methods;
  }

  public Map<String, PageInfo> getPages() {
    return pages;
  }

  public Collection<ParameterInfo> getParameters() {
    return parameters.values();
  }

  public Map<String, ParameterInfo> getParametersMap() {
    return parameters;
  }

  public String getUrl() {
    return url;
  }

  @Override
  public void setDescription(final String description) {
    super.setDescription(description);
  }

  public void setInputContentTypes(final List<MediaType> inputContentTypes) {
    this.inputContentTypes = inputContentTypes;
  }

  public void setInputContentTypes(final MediaType... inputContentTypes) {
    final ArrayList<MediaType> mediaTypes = new ArrayList<MediaType>();
    for (final MediaType mediaType : inputContentTypes) {
      mediaTypes.add(mediaType);
    }
    setInputContentTypes(mediaTypes);
  }

  public void setMediaTypes(final List<MediaType> mediaTypes) {
    this.mediaTypes = mediaTypes;
  }

  public void setPages(final Map<String, PageInfo> pages) {
    this.pages = pages;
  }

  public void setParameters(final Map<String, ParameterInfo> parameters) {
    this.parameters = parameters;
  }

  @Override
  public void setTitle(final String title) {
    super.setTitle(title);
  }

  public void setUrl(final String url) {
    this.url = url;
  }
}
