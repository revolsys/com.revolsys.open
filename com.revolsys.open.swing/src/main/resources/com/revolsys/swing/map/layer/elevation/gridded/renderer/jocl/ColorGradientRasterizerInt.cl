 #pragma OPENCL EXTENSION cl_khr_fp64 : enable
 __kernel void colorGradientRasterizerInt(
   const __global int *cells,
   const int width, 
   const int height,
   const int rangeCount,
   const __global int *zRange,
   const __global int *rRange,
   const __global int *gRange,
   const __global int *bRange, 
    __global uchar4 *output) {
  int imageX = get_global_id(0);
  int imageY = get_global_id(1);

  int gridX = imageX;
  int gridY = height - imageY - 1;
  int elevationInt = cells[gridY * width + gridX];

  if (elevationInt == -2147483648) {
    output[imageY * width + imageX] = (uchar4)(0);
  } else {
    int r = rRange[rangeCount - 1];
    int g = gRange[rangeCount - 1];
    int b = bRange[rangeCount - 1];
    if (elevationInt <= zRange[0]) {
      r = rRange[0];
      g = gRange[0];
      b = bRange[0];
    } else {
      for (int i = 1; i < rangeCount; i++) {
        int zInt = zRange[i];
        if (elevationInt <= zInt) {
          int previousI = i - 1;
          int zIntPrevious = zRange[previousI];
          float percent = (float)(elevationInt - zIntPrevious) / (zInt - zIntPrevious);
          int previousR = rRange[previousI];
          r = round((previousR + (rRange[i] - previousR) * percent));
          int previousG = gRange[previousI];
          g = round((previousG + (gRange[i] - previousG) * percent));
          int previousB = bRange[previousI];
          b = round((previousB + (bRange[i] - previousB) * percent));
          break;
        }
      }
    }
    output[imageY * width + imageX] = (uchar4)(b, g , r, 255);
  }
}
