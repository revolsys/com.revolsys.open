package com.revolsys.gis.wms.capabilities;

public class ContactPersonPrimary {
  private String contactPerson;

  private String contactOrganization;

  public String getContactOrganization() {
    return contactOrganization;
  }

  public String getContactPerson() {
    return contactPerson;
  }

  public void setContactOrganization(final String contactOrganization) {
    this.contactOrganization = contactOrganization;
  }

  public void setContactPerson(final String contactPerson) {
    this.contactPerson = contactPerson;
  }

}
