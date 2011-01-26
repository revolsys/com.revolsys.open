package com.revolsys.ui.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class AbstractDocumentedObject {

  private Map<Locale, DocInfo> documentationByLocale = new LinkedHashMap<Locale, DocInfo>();

  private Map<String, Map<String, DocInfo>> documentationByLanguageAndCountry = new LinkedHashMap<String, Map<String, DocInfo>>();

  public AbstractDocumentedObject() {
  }

  public AbstractDocumentedObject(AbstractDocumentedObject docObject) {
    for (DocInfo docInfo : docObject.getDocumentation()) {
      addDocumentation(docInfo.clone());
    }
  }

  public Collection<DocInfo> getDocumentation() {
    return documentationByLocale.values();
  }

  public void addDocumentation(DocInfo documentation) {
    Locale locale = documentation.getLocale();
    documentationByLocale.put(locale, documentation);
    if (locale != null) {
      String language = locale.getLanguage();
      Map<String, DocInfo> documentationByCountry = getDocumentationByLanguage(language);
      String country = locale.getCountry();
      documentationByCountry.put(country, documentation);
    }
  }

  public Map<String, DocInfo> getDocumentationByLanguage(String language) {
    Map<String, DocInfo> documentationByCountry = documentationByLanguageAndCountry.get(language);
    if (documentationByCountry == null) {
      documentationByCountry = new LinkedHashMap<String, DocInfo>();
      documentationByLanguageAndCountry.put(language, documentationByCountry);
    }
    return documentationByCountry;
  }

  protected void setTitle(String title) {
    DocInfo docInfo = documentationByLocale.get(null);
    if (docInfo == null) {
      docInfo = new DocInfo(title);
      addDocumentation(docInfo);
    } else {
      docInfo.setTitle(title);
    }
  }

  public String getTitle() {
    DocInfo docInfo = documentationByLocale.get(null);
    if (docInfo == null) {
      return null;
    } else {
      return docInfo.getTitle();
    }
  }

  protected void setDescription(String description) {
    DocInfo docInfo = documentationByLocale.get(null);
    if (docInfo == null) {
      docInfo = new DocInfo(null, description);
      addDocumentation(docInfo);
    } else {
      docInfo.setDescription(description);
    }
  }

  public String getDescription() {
    DocInfo docInfo = documentationByLocale.get(null);
    if (docInfo == null) {
      return null;
    } else {
      return docInfo.getDescription();
    }
  }
}
