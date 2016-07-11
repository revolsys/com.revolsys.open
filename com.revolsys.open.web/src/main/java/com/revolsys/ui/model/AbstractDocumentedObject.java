package com.revolsys.ui.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class AbstractDocumentedObject {

  private final Map<String, Map<String, DocInfo>> documentationByLanguageAndCountry = new LinkedHashMap<>();

  private final Map<Locale, DocInfo> documentationByLocale = new LinkedHashMap<>();

  public AbstractDocumentedObject() {
  }

  public AbstractDocumentedObject(final AbstractDocumentedObject docObject) {
    for (final DocInfo docInfo : docObject.getDocumentation()) {
      addDocumentation(docInfo.clone());
    }
  }

  public void addDocumentation(final DocInfo documentation) {
    final Locale locale = documentation.getLocale();
    this.documentationByLocale.put(locale, documentation);
    if (locale != null) {
      final String language = locale.getLanguage();
      final Map<String, DocInfo> documentationByCountry = getDocumentationByLanguage(language);
      final String country = locale.getCountry();
      documentationByCountry.put(country, documentation);
    }
  }

  public DocInfo getDefaultDocumentation() {
    final DocInfo docInfo = this.documentationByLocale.get(null);
    if (docInfo == null) {
      return new DocInfo();
    } else {
      return docInfo;
    }
  }

  public String getDescription() {
    final DocInfo docInfo = this.documentationByLocale.get(null);
    if (docInfo == null) {
      return null;
    } else {
      return docInfo.getDescription();
    }
  }

  public Collection<DocInfo> getDocumentation() {
    return this.documentationByLocale.values();
  }

  public Map<String, DocInfo> getDocumentationByLanguage(final String language) {
    Map<String, DocInfo> documentationByCountry = this.documentationByLanguageAndCountry
      .get(language);
    if (documentationByCountry == null) {
      documentationByCountry = new LinkedHashMap<>();
      this.documentationByLanguageAndCountry.put(language, documentationByCountry);
    }
    return documentationByCountry;
  }

  public String getTitle() {
    final DocInfo docInfo = this.documentationByLocale.get(null);
    if (docInfo == null) {
      return null;
    } else {
      return docInfo.getTitle();
    }
  }

  public void setDescription(final String description) {
    DocInfo docInfo = this.documentationByLocale.get(null);
    if (docInfo == null) {
      docInfo = new DocInfo(null, description);
      addDocumentation(docInfo);
    } else {
      docInfo.setDescription(description);
    }
  }

  public void setHtmlDescription(final String description) {
    DocInfo docInfo = this.documentationByLocale.get(null);
    if (docInfo == null) {
      docInfo = new DocInfo();
      addDocumentation(docInfo);
    }
    docInfo.setDescription(description);
    docInfo.setHtml(true);
  }

  protected void setTitle(final String title) {
    DocInfo docInfo = this.documentationByLocale.get(null);
    if (docInfo == null) {
      docInfo = new DocInfo(title);
      addDocumentation(docInfo);
    } else {
      docInfo.setTitle(title);
    }
  }
}
