package com.revolsys.swing.map.layer.dataobject.style;

/**
 * Composite operation. This defines how this symbolizer should behave relative
 * to symbolizers atop or below it. Not sure how this would work.
 */
public enum CompositionOperation {
  clear("clear"), src("src"), dst("dst"), src_over("src-over"), dst_over(
    "dst-over"), src_in("src-in"), dst_in("dst-in"), src_out("src-out"), dst_out(
    "dst-out"), src_atop("src-atop"), dst_atop("dst-atop"), xor("xor"), plus(
    "plus"), minus("minus"), multiply("multiply"), screen("screen"), overlay(
    "overlay"), darken("darken"), lighten("lighten"), color_dodge("color-dodge"), color_burn(
    "color-burn"), hard_light("hard-light"), soft_light("soft-light"), difference(
    "difference"), exclusion("exclusion"), contrast("contrast"), invert(
    "invert"), invert_rgb("invert-rgb"), grain_merge("grain-merge"), grain_extract(
    "grain-extract"), hue("hue"), saturation("saturation"), color("color"), value(
    "value");

  private String label;

  private CompositionOperation(final String label) {
    this.label = label;
  }

  @Override
  public String toString() {
    return label;
  }
}
