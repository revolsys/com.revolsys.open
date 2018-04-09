package com.revolsys.geometry.cs.projection;

public class HelmertOperation {

  class PJ {
    pj_opaque_helmert opaque;
  };

  class pj_opaque_helmert {
    CoordinatesOperationPoint xyz;

    CoordinatesOperationPoint xyz_0;

    CoordinatesOperationPoint dxyz;

    PJ_OPK opk;

    PJ_OPK dopk;

    double scale;

    double scale_0;

    double dscale;

    double theta;

    double theta_0;

    double dtheta;

    double t_epoch;

    double t_obs;

    boolean no_rotation;

    boolean fourparam;

    boolean exact;

    boolean transpose;
  };

  class PJ_OPK {
    double o;

    double p;

    double k;
  };

  private double r00;

  private double r01;

  private double r02;

  private double r10;

  private double r11;

  private double r12;

  private double r20;

  private double r21;

  private double r22;

  private final pj_opaque_helmert Q = new pj_opaque_helmert();

  public HelmertOperation(final PJ P) {
    buildRotMatrix(P);
  }

  private void buildRotMatrix(final PJ P) {

    /* rename (omega, phi, kappa) to (fi, theta, psi) */
    final double f = this.Q.opk.o; // phi/fi
    final double t = this.Q.opk.p; // theta
    final double p = this.Q.opk.k; // psi

    if (this.Q.exact) {
      final double cf = Math.cos(f);
      final double sf = Math.sin(f);
      final double ct = Math.cos(t);
      final double st = Math.sin(t);
      final double cp = Math.cos(p);
      final double sp = Math.sin(p);

      this.r00 = ct * cp;
      this.r01 = cf * sp + sf * st * cp;
      this.r02 = sf * sp - cf * st * cp;

      this.r10 = -ct * sp;
      this.r11 = cf * cp - sf * st * sp;
      this.r12 = sf * cp + cf * st * sp;

      this.r20 = st;
      this.r21 = -sf * ct;
      this.r22 = cf * ct;
    } else {
      this.r00 = 1;
      this.r01 = p;
      this.r02 = -t;

      this.r10 = -p;
      this.r11 = 1;
      this.r12 = f;

      this.r20 = t;
      this.r21 = -f;
      this.r22 = 1;
    }

    if (this.Q.transpose) {
      double r;
      r = this.r01;
      this.r01 = this.r10;
      this.r10 = r;
      r = this.r02;
      this.r02 = this.r20;
      this.r20 = r;
      r = this.r12;
      this.r12 = this.r21;
      this.r21 = r;
    }

    return;
  }

  public void helmertForward(final CoordinatesOperationPoint point) {
    final double cr = Math.cos(this.Q.theta) * this.Q.scale;
    final double sr = Math.sin(this.Q.theta) * this.Q.scale;
    final double x = point.x;
    final double y = point.y;

    point.x = cr * x + sr * y + this.Q.xyz_0.x;
    point.y = -sr * x + cr * y + this.Q.xyz_0.y;
  }

  public void helmertForward3d(final CoordinatesOperationPoint point) {
    if (this.Q.fourparam) {
      helmertForward(point);
      return;
    } else if (this.Q.no_rotation) {
      point.x = point.x + this.Q.xyz.x;
      point.y = point.y + this.Q.xyz.y;
      point.z = point.z + this.Q.xyz.z;
      return;
    } else {
      final double scale = 1 + this.Q.scale * 1e-6;

      final double X = point.x;
      final double Y = point.y;
      final double Z = point.z;

      point.x = scale * (this.r00 * X + this.r01 * Y + this.r02 * Z);
      point.y = scale * (this.r10 * X + this.r11 * Y + this.r12 * Z);
      point.z = scale * (this.r20 * X + this.r21 * Y + this.r22 * Z);

      point.x += this.Q.xyz.x;
      point.y += this.Q.xyz.y;
      point.z += this.Q.xyz.z;
    }
  }

  public void helmertReverse(final CoordinatesOperationPoint point) {
    final double cr = Math.cos(this.Q.theta) / this.Q.scale;
    final double sr = Math.sin(this.Q.theta) / this.Q.scale;
    final double x = point.x - this.Q.xyz_0.x;
    final double y = point.y - this.Q.xyz_0.y;

    point.x = x * cr - y * sr;
    point.y = x * sr + y * cr;
  }

  public void helmertReverse3d(final CoordinatesOperationPoint point) {
    if (this.Q.fourparam) {
      helmertReverse(point);
    } else if (this.Q.no_rotation) {
      point.x = point.x - this.Q.xyz.x;
      point.y = point.y - this.Q.xyz.y;
      point.z = point.z - this.Q.xyz.z;
    } else {

      final double scale = 1 + this.Q.scale * 1e-6;

      final double X = (point.x - this.Q.xyz.x) / scale;
      final double Y = (point.y - this.Q.xyz.y) / scale;
      final double Z = (point.z - this.Q.xyz.z) / scale;

      point.x = this.r00 * X + this.r10 * Y + this.r20 * Z;
      point.y = this.r01 * X + this.r11 * Y + this.r21 * Z;
      point.z = this.r02 * X + this.r12 * Y + this.r22 * Z;

    }
  }

  public void updateParameters(final PJ P) {

    final pj_opaque_helmert Q = P.opaque;
    final double dt = Q.t_obs - Q.t_epoch;

    Q.xyz.x = Q.xyz_0.x + Q.dxyz.x * dt;
    Q.xyz.y = Q.xyz_0.y + Q.dxyz.y * dt;
    Q.xyz.z = Q.xyz_0.z + Q.dxyz.z * dt;

    Q.opk.o = Q.dopk.o * dt;
    Q.opk.p = Q.dopk.p * dt;
    Q.opk.k = Q.dopk.k * dt;

    Q.scale = Q.scale_0 + Q.dscale * dt;

    Q.theta = Q.theta_0 + Q.dtheta * dt;

    return;
  }

}
