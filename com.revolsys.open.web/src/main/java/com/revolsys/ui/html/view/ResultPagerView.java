package com.revolsys.ui.html.view;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.revolsys.io.xml.io.XmlWriter;
import com.revolsys.orm.core.ResultPager;
import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.util.UrlUtil;

/**
 * @author Paul Austin
 * @version 1.0
 */
public class ResultPagerView extends Element {
  /** The result pager. */
  private ResultPager pager;

  /** The base URL. */
  private String baseUrl;

  /** The parameters to include in the URLs. */
  private Map<String, Object> parameters = new HashMap<String, Object>();

  /**
   * Construct a new ResultPagerView.
   * 
   * @param resultPager The result pager.
   * @param newBaseUrl The base URL.
   * @param newParameters The parameters to include in the URLs.
   */
  public ResultPagerView(final ResultPager resultPager,
    final String newBaseUrl, final Map<String, Object> newParameters) {
    this.pager = resultPager;
    this.baseUrl = newBaseUrl;
    this.parameters.putAll(newParameters);
  }

  /**
   * Serialize the view.
   * 
   * @param out The XML Writer.
   */
  public final void serializeElement(final XmlWriter out) {
    int numPages = pager.getNumPages();

    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, "pager");

    out.startTag(HtmlUtil.TABLE);
    out.attribute(HtmlUtil.ATTR_CELL_SPACING, "0");
    out.attribute(HtmlUtil.ATTR_CELL_PADDING, "0");

    out.startTag(HtmlUtil.TR);

    out.startTag(HtmlUtil.TD);
    out.attribute(HtmlUtil.ATTR_CLASS, "records");
    out.text("records ");
    out.text(pager.getStartIndex());
    out.text(" - ");
    out.text(pager.getEndIndex());
    if (pager.getNumResults() > pager.getEndIndex()) {
      out.text(" of ");
      out.text(pager.getNumResults());
    }
    out.endTag(HtmlUtil.TD);

    if (numPages > 1) {

      if (!pager.isFirstPage()) {
        out.startTag(HtmlUtil.TD);
        out.attribute(HtmlUtil.ATTR_CLASS, "first");
        pageLinkSpan(out, 1, "First Page", "<<");
        out.endTag(HtmlUtil.TD);
      }

      if (pager.hasPreviousPage()) {
        out.startTag(HtmlUtil.TD);
        out.attribute(HtmlUtil.ATTR_CLASS, "previous");
        pageLinkSpan(out, pager.getPreviousPageNumber(), "Previous Page", "<");
        out.endTag(HtmlUtil.TD);
      }

      out.startTag(HtmlUtil.TD);
      out.attribute(HtmlUtil.ATTR_CLASS, "pages");
      if (numPages < 7) {
        for (int pageNumber = 1; pageNumber <= numPages; pageNumber++) {
          serializePageLink(out, pageNumber);
        }
      } else {
        int currentPageNumber = pager.getPageNumber();
        int fromPage = Math.max(1,
          Math.min(currentPageNumber - 3, numPages - 6));
        int toPage = Math.min(numPages, fromPage + 6);
        if (fromPage > 1) {
          HtmlUtil.serializeSpan(out, "pageGap", "...");
        }
        for (int pageNumber = fromPage; pageNumber <= toPage; pageNumber++) {
          serializePageLink(out, pageNumber);
        }
        if (toPage < numPages) {
          HtmlUtil.serializeSpan(out, "pageGap", "...");
        }
      }
      out.endTag(HtmlUtil.TD);

      if (pager.hasNextPage()) {
        out.startTag(HtmlUtil.TD);
        out.attribute(HtmlUtil.ATTR_CLASS, "next");
        pageLinkSpan(out, pager.getNextPageNumber(), "Next Page", ">");
        out.endTag(HtmlUtil.TD);
      }

      if (!pager.isLastPage()) {
        out.startTag(HtmlUtil.TD);
        out.attribute(HtmlUtil.ATTR_CLASS, "last");
        pageLinkSpan(out, numPages, "Last Page", ">>");
        out.endTag(HtmlUtil.TD);
      }

    }

    out.endTag(HtmlUtil.TR);
    out.endTag(HtmlUtil.TABLE);

    out.endTag(HtmlUtil.DIV);
  }

  /**
   * Serialize a link to the specified page number.
   * 
   * @param out The XML Writer.
   * @param pageNumber The page number.
   * @throws IOException If there was an exception serializing.
   */
  private void serializePageLink(final XmlWriter out, final int pageNumber)
    {
    String contents = String.valueOf(pageNumber);
    String title = "Page " + pageNumber;
    if (pageNumber == pager.getPageNumber()) {
      HtmlUtil.serializeSpan(out, "pageNum selected", contents);
    } else {
      pageLink(out, pageNumber, title, contents);
    }
  }

  /**
   * Serialize a link to the specified page numberm title and contents.
   * 
   * @param out The XML Writer.
   * @param pageNumber The page number.
   * @param title The title of the link.
   * @param contents The contents of the link.
   * @throws IOException If there was an exception serializing.
   */
  private void pageLink(final XmlWriter out, final int pageNumber,
    final String title, final String contents) {
    parameters.put("page", String.valueOf(pageNumber));
    String url = UrlUtil.getUrl(baseUrl, parameters);
    out.startTag(HtmlUtil.A);
    out.attribute(HtmlUtil.ATTR_HREF, url);
    out.attribute(HtmlUtil.ATTR_TITLE, title);
    out.text(contents);
    out.endTag(HtmlUtil.A);
  }

  /**
   * Serialize a link to the specified page numberm title and contents.
   * 
   * @param out The XML Writer.
   * @param pageNumber The page number.
   * @param title The title of the link.
   * @param contents The contents of the link.
   * @throws IOException If there was an exception serializing.
   */
  private void pageLinkSpan(final XmlWriter out, final int pageNumber,
    final String title, final String contents) {
    parameters.put("page", String.valueOf(pageNumber));
    String url = UrlUtil.getUrl(baseUrl, parameters);
    out.startTag(HtmlUtil.A);
    out.attribute(HtmlUtil.ATTR_HREF, url);
    out.attribute(HtmlUtil.ATTR_TITLE, title);
    out.element(HtmlUtil.SPAN, contents);
    out.endTag(HtmlUtil.A);
  }
}
