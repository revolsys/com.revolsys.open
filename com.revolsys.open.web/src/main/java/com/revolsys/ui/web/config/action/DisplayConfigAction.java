package com.revolsys.ui.web.config.action;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.ui.html.layout.DefinitionListLayout;
import com.revolsys.ui.html.layout.UnorderedListLayout;
import com.revolsys.ui.html.view.DivElementContainer;
import com.revolsys.ui.html.view.ElementContainer;
import com.revolsys.ui.html.view.XmlTagElement;
import com.revolsys.ui.model.Menu;
import com.revolsys.ui.web.config.Action;
import com.revolsys.ui.web.config.PageController;
import com.revolsys.ui.web.config.Site;
import com.revolsys.ui.web.config.SiteNode;
import com.revolsys.ui.web.config.SiteNodeController;
import com.revolsys.util.HtmlElem;

public class DisplayConfigAction implements Action {

  private static final Logger log = LoggerFactory.getLogger(DisplayConfigAction.class);

  private void addMenu(final ElementContainer menusView, final String name, final Menu menu) {
    // TODO Auto-generated method stub

  }

  private void addSite(final ElementContainer view, final Site site) {
    view.add(new XmlTagElement(HtmlElem.H3, site.getName()));
    addSiteNode(view, site.getRootNode());

  }

  private void addSiteNode(final ElementContainer view, final SiteNode siteNode) {
    if (siteNode != null) {
      final ElementContainer nodeView = new ElementContainer();
      view.add(nodeView);
      final String path = siteNode.getPath();
      if (path == null) {
        nodeView.add(new XmlTagElement(HtmlElem.H4, "/"));
      } else {
        nodeView.add(new XmlTagElement(HtmlElem.H4, path));
      }
      final SiteNodeController controller = siteNode.getController();
      if (controller instanceof PageController) {
        final PageController pageController = (PageController)controller;
        final ElementContainer pageView = new ElementContainer(new DefinitionListLayout());
        nodeView.add(pageView);
        pageView.add("Actions");
        final ElementContainer actionsView = new ElementContainer(new UnorderedListLayout());
        pageView.add(actionsView);
        for (final Iterator actions = pageController.getActions().iterator(); actions.hasNext();) {
          final Action action = (Action)actions.next();
          actionsView.add(action.getClass().getName());
        }

        pageView.add("Menus");
        final ElementContainer menusView = new ElementContainer(new UnorderedListLayout());
        pageView.add(menusView);
        for (final Iterator menus = pageController.getMenus().entrySet().iterator(); menus
          .hasNext();) {
          final Map.Entry entry = (Entry)menus.next();
          final String name = (String)entry.getKey();
          final Menu menu = (Menu)entry.getValue();

          addMenu(menusView, name, menu);
        }

      }
      final Collection nodes = siteNode.getNodes();
      if (!nodes.isEmpty()) {
        final ElementContainer childNodesView = new ElementContainer(new UnorderedListLayout());
        nodeView.add(childNodesView);
        for (final Iterator nodeIter = nodes.iterator(); nodeIter.hasNext();) {
          final SiteNode childNode = (SiteNode)nodeIter.next();
          addSiteNode(childNodesView, childNode);

        }
      }
    }
  }

  @Override
  public void init(final ServletContext context) throws ServletException {
  }

  @Override
  public void process(final HttpServletRequest request, final HttpServletResponse response)
    throws IOException, ServletException {
    final DivElementContainer view = new DivElementContainer();
    addSite(view, (Site)request.getAttribute("site"));
    request.setAttribute("view", view);
    response.setContentType("text/html");
    view.serialize(response.getWriter());
  }

}
