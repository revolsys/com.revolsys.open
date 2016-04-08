package com.revolsys.ui.model;

import java.util.ArrayList;
import java.util.List;

public class Navbar extends Menu {
  private List<Brand> brands = new ArrayList<>();

  private String navbarAlign = "left";

  private String navbarCssClass = "";

  public List<Brand> getBrands() {
    return this.brands;
  }

  public String getNavbarAlign() {
    return this.navbarAlign;
  }

  public String getNavbarCssClass() {
    return this.navbarCssClass;
  }

  public void setBrands(final List<Brand> brands) {
    this.brands = brands;
  }

  public void setNavbarAlign(final String navbarAlign) {
    this.navbarAlign = navbarAlign;
  }

  public void setNavbarCssClass(final String navbarCssClass) {
    this.navbarCssClass = navbarCssClass;
  }
}
