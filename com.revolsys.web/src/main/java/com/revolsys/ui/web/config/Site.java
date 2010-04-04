package com.revolsys.ui.web.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Site implements Cloneable {
  /** The list of pages defined as direct children of the site. */
  private Map pages = new HashMap();

  /** The cache of pages that can be found from the decendents of this site. */
  private Map pageCache = new HashMap();

  private String name;

  private SiteNode rootNode = new SiteNode();

  public Site() {
  }

  public Site(final String name) {
    this.name = name;
  }

  protected Object clone() throws CloneNotSupportedException {
    Site site = new Site();

    return site;
  }

  /**
   * Get the site node controller for the specified path.
   * 
   * @param path The path
   * @return The contoller or null if not found.
   */
  public SiteNodeController getController(final String path) {
    if (path.equals("/")) {
      return rootNode.getController();
    } else {
      return rootNode.getController(path);
    }
  }

  /**
   * Get the nane of the site.
   * 
   * @return The nane of the site.
   */
  public String getName() {
    return name;
  }

  /**
   * Set the nane of the site.
   * 
   * @param name The name of the site.
   */
  public void setName(final String name) {
    this.name = name;
  }

  /**
   * @return Returns the rootNode.
   */
  public SiteNode getRootNode() {
    return rootNode;
  }

  /**
   * @param rootNode The rootNode to set.
   */
  public void setRootNode(final SiteNode rootNode) {
    this.rootNode = rootNode;
  }

  public Collection getNodes() {
    return rootNode.getNodes();
  }

  public void setNodes(final Collection nodes) {
    rootNode.setNodes(nodes);
  }
}
