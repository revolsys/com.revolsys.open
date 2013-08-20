package com.revolsys.gis.wms.capabilities;

public class ContactInformation {
  private ContactPersonPrimary contactPersonPrimary;

  private String contactPosition;

  private ContactAddress contactAddress;

  private String contactVoiceTelephone;

  private String contactFacsimileTelephone;

  private String contactElectronicMailAddress;

  public ContactAddress getContactAddress() {
    return contactAddress;
  }

  public String getContactElectronicMailAddress() {
    return contactElectronicMailAddress;
  }

  public String getContactFacsimileTelephone() {
    return contactFacsimileTelephone;
  }

  public ContactPersonPrimary getContactPersonPrimary() {
    return contactPersonPrimary;
  }

  public String getContactPosition() {
    return contactPosition;
  }

  public String getContactVoiceTelephone() {
    return contactVoiceTelephone;
  }

  public void setContactAddress(final ContactAddress contactAddress) {
    this.contactAddress = contactAddress;
  }

  public void setContactElectronicMailAddress(
    final String contactElectronicMailAddress) {
    this.contactElectronicMailAddress = contactElectronicMailAddress;
  }

  public void setContactFacsimileTelephone(
    final String contactFacsimileTelephone) {
    this.contactFacsimileTelephone = contactFacsimileTelephone;
  }

  public void setContactPersonPrimary(
    final ContactPersonPrimary contactPersonPrimary) {
    this.contactPersonPrimary = contactPersonPrimary;
  }

  public void setContactPosition(final String contactPosition) {
    this.contactPosition = contactPosition;
  }

  public void setContactVoiceTelephone(final String contactVoiceTelephone) {
    this.contactVoiceTelephone = contactVoiceTelephone;
  }

}
