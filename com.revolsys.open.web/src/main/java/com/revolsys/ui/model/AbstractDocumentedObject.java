package com.revolsys.ui.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class AbstractDocumentedObject {

  private final Map<Locale, DocInfo> documentationByLocale = new LinkedHashMap<Locale, DocInfo>();

  private final Map<String, Map<String, DocInfo>> documentationByLanguageAndCountry = new LinkedHashMap<String, Map<String, DocInfo>>();

  public AbstractDocumentedObject() {
  }

  public AbstractDocumentedObject(final AbstractDocumentedObject docObject) {
    for (final DocInfo docInfo : docObject.getDocumentation()) {
      addDocumentation(docInfo.clone());
    }
  }

  public void addDocumentation(final DocInfo documentation) {
    final Locale locale = documentation.getLocale();
    documentationByLocale.put(locale, documentation);
    if (locale != null) {
      final String language = locale.getLanguage();
      final Map<String, DocInfo> documentationByCountry = getDocumentationByLanguage(language);
      final String country = locale.getCountry();
      documentationByCountry.put(country, documentation);
    }
  }

  public String getDescription() {
    final DocInfo docInfo = documentationByLocale.get(null);
    if (docInfo == null) {
      return null;
    } else {
      return docInfo.getDescription();
    }
  }

  public DocInfo getDefaultDocumentation() {
    final DocInfo docInfo = documentationByLocale.get(null);
    if (docInfo == null) {
      return new DocInfo();
    } else {
      return docInfo;
    }
  }

  public Collection<DocInfo> getDocumentation() {
    return documentationByLocale.values();
  }

  public Map<String, DocInfo> getDocumentationByLanguage(final String language) {
    Map<String, DocInfo> documentationByCountry = documentationByLanguageAndCountry.get(language);
    if (documentationByCountry == null) {
      documentationByCountry = new LinkedHashMap<String, DocInfo>();
      documentationByLanguageAndCountry.put(language, documentationByCountry);
    }
    return documentationByCountry;
  }

  public String getTitle() {
    final DocInfo docInfo = documentationByLocale.get(null);
    if (docInfo == null) {
      return null;
    } else {
      return docInfo.getTitle();
    }
  }

  public void setDescription(final String description) {
    DocInfo docInfo = documentationByLocale.get(null);
    if (docInfo == null) {
      docInfo = new DocInfo(null, description);
      addDocumentation(docInfo);
    } else {
      docInfo.setDescription(description);
    }
  }

  public void setHtmlDescription(final String description) {
    DocInfo docInfo = documentationByLocale.get(null);
    if (docInfo == null) {
      docInfo = new DocInfo();
      addDocumentation(docInfo);
    }
    docInfo.setDescription(description);
    docInfo.setHtml(true);
  }

  protected void setTitle(final String title) {
    DocInfo docInfo = documentationByLocale.get(null);
    if (docInfo == null) {
      docInfo = new DocInfo(title);
      addDocumentation(docInfo);
    } else {
      docInfo.setTitle(title);
    }
  }
}
