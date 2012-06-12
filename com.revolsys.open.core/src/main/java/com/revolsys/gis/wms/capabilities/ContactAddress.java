package com.revolsys.gis.wms.capabilities;

public class ContactAddress {
  private String addressType;

  private String address;

  private String city;

  private String stateOrProvince;

  private String postCode;

  private String country;

  public String getAddress() {
    return address;
  }

  public String getAddressType() {
    return addressType;
  }

  public String getCity() {
    return city;
  }

  public String getCountry() {
    return country;
  }

  public String getPostCode() {
    return postCode;
  }

  public String getStateOrProvince() {
    return stateOrProvince;
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
