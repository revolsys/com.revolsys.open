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

import org.apache.log4j.Logger;

import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.layout.DefinitionListLayout;
import com.revolsys.ui.html.layout.UnorderedListLayout;
import com.revolsys.ui.html.view.DivElementContainer;
import com.revolsys.ui.html.view.ElementContainer;
import com.revolsys.ui.html.view.XmlTagElement;
import com.revolsys.ui.model.Menu;
import com.revolsys.ui.web.config.Action;
import com.revolsys.ui.web.config.Config;
import com.revolsys.ui.web.config.PageController;
import com.revolsys.ui.web.config.Site;
import com.revolsys.ui.web.config.SiteNode;
import com.revolsys.ui.web.config.SiteNodeController;

public class DisplayConfigAction implements Action {

  private static final Logger log = Logger.getLogger(DisplayConfigAction.class);

  public void process(final HttpServletRequest request,
    final HttpServletResponse response)
    throws IOException, ServletException {
    DivElementContainer view = new DivElementContainer();
    addSite(view, (Site)request.getAttribute("site"));
    request.setAttribute("view", view);
    response.setContentType("text/html");
    view.serialize(response.getWriter());
  }

  private void addSites(final Config config, final ElementContainer view) {
    view.add(new XmlTagElement(HtmlUtil.H2, "Sites"));
//    for (Iterator sites = config.getSites().iterator(); sites.hasNext();) {
//      Site site = (Site)sites.next();
//      addSite(view, site);
//    }
  }

  private void addSite(final ElementContainer view, final Site site) {
    view.add(new XmlTagElement(HtmlUtil.H3, site.getName()));
    addSiteNode(view, site.getRootNode());

  }

  private void addSiteNode(final ElementContainer view, final SiteNode siteNode) {
     if (siteNode != null) {
       ElementContainer nodeView = new ElementContainer();
       view.add(nodeView);
     String path = siteNode.getPath();
      if (path == null) {
        nodeView.add(new XmlTagElement(HtmlUtil.H4, "/"));
      } else {
        nodeView.add(new XmlTagElement(HtmlUtil.H4, path));
      }
      SiteNodeController controller = siteNode.getController();
      if (controller instanceof PageController) {
        PageController pageController = (PageController)controller;
        ElementContainer pageView = new ElementContainer(
          new DefinitionListLayout());
        nodeView.add(pageView);
        pageView.add("Actions");
        ElementContainer actionsView = new ElementContainer(
          new UnorderedListLayout());
        pageView.add(actionsView);
        for (Iterator actions = pageController.getActions().iterator(); actions.hasNext();) {
          Action action = (Action)actions.next();
          actionsView.add(action.getClass().getName());
        }

        pageView.add("Menus");
        ElementContainer menusView = new ElementContainer(
          new UnorderedListLayout());
        pageView.add(menusView);
        for (Iterator menus = pageController.getMenus().entrySet().iterator(); menus.hasNext();) {
          Map.Entry entry = (Entry)menus.next();
          String name = (String)entry.getKey();
          Menu menu = (Menu)entry.getValue();
          
          addMenu(menusView, name, menu);
        }

      }
      Collection nodes = siteNode.getNodes();
      if (!nodes.isEmpty()) {
        ElementContainer childNodesView = new ElementContainer(
          new UnorderedListLayout());
        nodeView.add(childNodesView);
        for (Iterator nodeIter = nodes.iterator(); nodeIter.hasNext();) {
          SiteNode childNode = (SiteNode)nodeIter.next();
          addSiteNode(childNodesView, childNode);

        }
      }
    }
  }

  private void addMenu(ElementContainer menusView, String name, Menu menu) {
    // TODO Auto-generated method stub
    
  }

  public void init(ServletContext context) throws ServletException {
  }

}
