package com.revolsys.ui.html.view;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.revolsys.collection.ResultPager;
import com.revolsys.format.xml.XmlWriter;
import com.revolsys.util.HtmlUtil;
import com.revolsys.util.UrlUtil;

/**
 * @author Paul Austin
 * @version 1.0
 */
public class ResultPagerView extends Element {
  /** The base URL. */
  private final String baseUrl;

  /** The result pager. */
  private final ResultPager<?> pager;

  /** The parameters to include in the URLs. */
  private final Map<String, Object> parameters = new HashMap<String, Object>();

  /**
   * Construct a new ResultPagerView.
   *
   * @param resultPager The result pager.
   * @param newBaseUrl The base URL.
   * @param newParameters The parameters to include in the URLs.
   */
  public ResultPagerView(final ResultPager<?> resultPager, final String newBaseUrl,
    final Map<String, Object> newParameters) {
    this.pager = resultPager;
    this.baseUrl = newBaseUrl;
    this.parameters.putAll(newParameters);
  }

  /**
   * Serialize a link to the specified page number title and contents.
   *
   * @param out The XML Writer.
   * @param pageNumber The page number.
   * @param title The title of the link.
   * @param contents The contents of the link.
   * @throws IOException If there was an exception serializing.
   */
  private void pageLink(final XmlWriter out, final int pageNumber, final String title,
    final String contents) {
    this.parameters.put("page", String.valueOf(pageNumber));
    final String url = UrlUtil.getUrl(this.baseUrl, this.parameters);
    out.startTag(HtmlUtil.A);
    out.attribute(HtmlUtil.ATTR_HREF, url);
    out.attribute(HtmlUtil.ATTR_TITLE, title);
    out.text(contents);
    out.endTag(HtmlUtil.A);
  }

  /**
   * Serialize the view.
   *
   * @param out The XML Writer.
   */
  @Override
  public final void serializeElement(final XmlWriter out) {
    final int numPages = this.pager.getNumPages();

    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, "pager");

    out.startTag(HtmlUtil.TABLE);
    out.attribute(HtmlUtil.ATTR_CELL_SPACING, "0");
    out.attribute(HtmlUtil.ATTR_CELL_PADDING, "0");

    out.startTag(HtmlUtil.TR);

    out.startTag(HtmlUtil.TD);
    out.attribute(HtmlUtil.ATTR_CLASS, "records");
    out.text("records ");
    out.text(this.pager.getStartIndex());
    out.text(" - ");
    out.text(this.pager.getEndIndex());
    if (this.pager.getNumResults() > this.pager.getEndIndex()) {
      out.text(" of ");
      out.text(this.pager.getNumResults());
    }
    out.endTag(HtmlUtil.TD);

    if (numPages > 1) {

      if (this.pager.isFirstPage()) {
        out.startTag(HtmlUtil.TD);
        out.attribute(HtmlUtil.ATTR_CLASS, "first");
        out.entityRef("nbsp");
        out.endTag(HtmlUtil.TD);
      } else {
        out.startTag(HtmlUtil.TD);
        out.attribute(HtmlUtil.ATTR_CLASS, "first");
        pageLink(out, 1, "First Page", "<<");
        out.endTag(HtmlUtil.TD);
      }

      if (this.pager.hasPreviousPage()) {
        out.startTag(HtmlUtil.TD);
        out.attribute(HtmlUtil.ATTR_CLASS, "previous");
        pageLink(out, this.pager.getPreviousPageNumber(), "Previous Page", "<");
        out.endTag(HtmlUtil.TD);
      } else {
        out.startTag(HtmlUtil.TD);
        out.attribute(HtmlUtil.ATTR_CLASS, "previous");
        out.entityRef("nbsp");
        out.endTag(HtmlUtil.TD);
      }

      out.startTag(HtmlUtil.TD);
      out.attribute(HtmlUtil.ATTR_CLASS, "pages");
      if (numPages < 7) {
        for (int pageNumber = 1; pageNumber <= numPages; pageNumber++) {
          serializePageLink(out, pageNumber);
        }
      } else {
        final int currentPageNumber = this.pager.getPageNumber();
        final int fromPage = Math.max(1, Math.min(currentPageNumber - 3, numPages - 6));
        final int toPage = Math.min(numPages, fromPage + 6);
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

      if (this.pager.hasNextPage()) {
        out.startTag(HtmlUtil.TD);
        out.attribute(HtmlUtil.ATTR_CLASS, "next");
        pageLink(out, this.pager.getNextPageNumber(), "Next Page", ">");
        out.endTag(HtmlUtil.TD);
      } else {
        out.startTag(HtmlUtil.TD);
        out.attribute(HtmlUtil.ATTR_CLASS, "next");
        out.entityRef("nbsp");
        out.endTag(HtmlUtil.TD);
      }

      if (this.pager.isLastPage()) {
        out.startTag(HtmlUtil.TD);
        out.attribute(HtmlUtil.ATTR_CLASS, "last");
        out.entityRef("nbsp");
        out.endTag(HtmlUtil.TD);
      } else {
        out.startTag(HtmlUtil.TD);
        out.attribute(HtmlUtil.ATTR_CLASS, "last");
        pageLink(out, numPages, "Last Page", ">>");
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
  private void serializePageLink(final XmlWriter out, final int pageNumber) {
    final String contents = String.valueOf(pageNumber);
    final String title = "Page " + pageNumber;
    if (pageNumber == this.pager.getPageNumber()) {
      HtmlUtil.serializeSpan(out, "pageNum selected", contents);
    } else {
      pageLink(out, pageNumber, title, contents);
    }
  }
}
