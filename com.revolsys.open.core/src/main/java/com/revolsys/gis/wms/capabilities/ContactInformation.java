package com.revolsys.gis.wms.capabilities;

public class ContactInformation {
  private ContactAddress contactAddress;

  private String contactElectronicMailAddress;

  private String contactFacsimileTelephone;

  private ContactPersonPrimary contactPersonPrimary;

  private String contactPosition;

  private String contactVoiceTelephone;

  public ContactAddress getContactAddress() {
    return this.contactAddress;
  }

  public String getContactElectronicMailAddress() {
    return this.contactElectronicMailAddress;
  }

  public String getContactFacsimileTelephone() {
    return this.contactFacsimileTelephone;
  }

  public ContactPersonPrimary getContactPersonPrimary() {
    return this.contactPersonPrimary;
  }

  public String getContactPosition() {
    return this.contactPosition;
  }

  public String getContactVoiceTelephone() {
    return this.contactVoiceTelephone;
  }

  public void setContactAddress(final ContactAddress contactAddress) {
    this.contactAddress = contactAddress;
  }

  public void setContactElectronicMailAddress(final String contactElectronicMailAddress) {
    this.contactElectronicMailAddress = contactElectronicMailAddress;
  }

  public void setContactFacsimileTelephone(final String contactFacsimileTelephone) {
    this.contactFacsimileTelephone = contactFacsimileTelephone;
  }

  public void setContactPersonPrimary(final ContactPersonPrimary contactPersonPrimary) {
    this.contactPersonPrimary = contactPersonPrimary;
  }

  public void setContactPosition(final String contactPosition) {
    this.contactPosition = contactPosition;
  }

  public void setContactVoiceTelephone(final String contactVoiceTelephone) {
    this.contactVoiceTelephone = contactVoiceTelephone;
  }

}
