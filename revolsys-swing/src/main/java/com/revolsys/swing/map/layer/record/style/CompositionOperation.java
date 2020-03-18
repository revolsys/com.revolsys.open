package com.revolsys.swing.map.layer.record.style;

/**
 * Composite operation. This defines how this symbolizer should behave relative
 * to symbolizers atop or below it. Not sure how this would work.
 */
public enum CompositionOperation {
  clear("clear"), //
  color("color"), //
  color_burn("color-burn"), //
  color_dodge("color-dodge"), //
  contrast("contrast"), //
  darken("darken"), //
  difference("difference"), //
  dst("dst"), //
  dst_atop("dst-atop"), //
  dst_in("dst-in"), //
  dst_out("dst-out"), //
  dst_over("dst-over"), //
  exclusion("exclusion"), //
  grain_extract("grain-extract"), //
  grain_merge("grain-merge"), //
  hard_light("hard-light"), //
  hue("hue"), //
  invert("invert"), //
  invert_rgb("invert-rgb"), //
  lighten("lighten"), //
  minus("minus"), //
  multiply("multiply"), //
  overlay("overlay"), //
  plus("plus"), //
  saturation("saturation"), //
  screen("screen"), //
  soft_light("soft-light"), //
  src("src"), //
  src_atop("src-atop"), //
  src_in("src-in"), //
  src_out("src-out"), //
  src_over("src-over"), //
  value("value"), //
  xor("xor");

  private String label;

  private CompositionOperation(final String label) {
    this.label = label;
  }

  @Override
  public String toString() {
    return this.label;
  }
}
