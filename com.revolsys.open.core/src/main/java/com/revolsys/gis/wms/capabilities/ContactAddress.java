package com.revolsys.gis.wms.capabilities;

public class ContactAddress {
  private String address;

  private String addressType;

  private String city;

  private String country;

  private String postCode;

  private String stateOrProvince;

  public String getAddress() {
    return this.address;
  }

  public String getAddressType() {
    return this.addressType;
  }

  public String getCity() {
    return this.city;
  }

  public String getCountry() {
    return this.country;
  }

  public String getPostCode() {
    return this.postCode;
  }

  public String getStateOrProvince() {
    return this.stateOrProvince;
  }

  public void setAddress(final String address) {
    this.address = address;
  }

  public void setAddressType(final String addressType) {
    this.addressType = addressType;
  }

  public void setCity(final String city) {
    this.city = city;
  }

  public void setCountry(final String country) {
    this.country = country;
  }

  public void setPostCode(final String postCode) {
    this.postCode = postCode;
  }

  public void setStateOrProvince(final String stateOrProvince) {
    this.stateOrProvince = stateOrProvince;
  }

}
