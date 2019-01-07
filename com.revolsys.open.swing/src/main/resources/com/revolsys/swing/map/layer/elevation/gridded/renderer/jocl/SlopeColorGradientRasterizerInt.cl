 #pragma OPENCL EXTENSION cl_khr_fp64 : enable
 __kernel void slopeColorGradientRasterizerInt(
   const __global int *cells,
   const int width, 
   const int height,
   const float offsetZ, 
   const float scaleZ, 
   const int rangeCount,
   const float oneDivCellSizeTimes8,
   const __global float *slopes,
   const __global int *rRange,
   const __global int *gRange,
   const __global int *bRange, 
    __global uchar4 *output) {
  int imageX = get_global_id(0);
  int imageY = get_global_id(1);

  int gridX = imageX;
  int gridY = height - imageY - 1;
  int aInt = -2147483648;
  int bInt = -2147483648;
  int cInt = -2147483648;
  int dInt = -2147483648;
  int eInt = -2147483648;
  int fInt = -2147483648;
  int gInt = -2147483648;
  int hInt = -2147483648;
  int iInt = -2147483648;
  if (0 <= gridX && gridX <= width && 0 <= gridY && gridY <= height) {
    eInt = cells[gridY * width + gridX];
  }
  if (eInt == -2147483648) {
    output[imageY * width + imageX] = (uchar4)(0);
  } else {
    int gridX0 = gridX - 1;
    int gridX2 = gridX + 1;
    if (gridY != height - 1) {
      int gridY2 = gridY + 1;
      if (gridX != 0) {
        aInt = cells[gridY2 * width + gridX0];
      }
      bInt = cells[gridY2 * width + gridX];
      if (gridX != width - 1) {
        cInt = cells[gridY2 * width + gridX2];
      }
    }
    if (gridX != 0) {
      dInt = cells[gridY * width + gridX0];
    }
    if (gridX != width - 1) {
      fInt = cells[gridY * width + gridX2];
    }
    if (gridY != 0) {
      int gridY0 = gridY - 1;
      if (gridX != 0) {
        gInt = cells[gridY0 * width + gridX0];
      }
      hInt = cells[gridY0 * width + gridX];
      if (gridX != width - 1) {
        iInt = cells[gridY0 * width + gridX2];
      }
    }

    if (dInt == -2147483648) {
      if (fInt == -2147483648) {
        dInt = eInt;
        fInt = eInt;
      } else {
        dInt = eInt - (fInt - eInt);
      }
    } else if (fInt == -2147483648) {
      fInt = eInt;
    }
    if (aInt == -2147483648) {
      if (gInt == -2147483648) {
        aInt = dInt;
      } else {
        aInt = dInt - (gInt - dInt);
      }
    }
    if (bInt == -2147483648) {
      if (hInt == -2147483648) {
        bInt = eInt;
      } else {
        bInt = eInt - (hInt - eInt);
      }
    }
    if (cInt == -2147483648) {
      if (iInt == -2147483648) {
        cInt = fInt;
      } else {
        cInt = fInt - (iInt - fInt);
      }
    }
    if (gInt == -2147483648) {
      gInt = dInt - (aInt - dInt);
    }
    if (hInt == -2147483648) {
      hInt = eInt - (bInt - eInt);
    }
    if (iInt == -2147483648) {
      iInt = fInt - (cInt - fInt);
    }
    double a = offsetZ + aInt / scaleZ;
    double b = offsetZ + bInt / scaleZ;
    double c = offsetZ + cInt / scaleZ;
    double d = offsetZ + dInt / scaleZ;
    //double e = offsetZ + eInt / scaleZ;
    double f = offsetZ + fInt / scaleZ;
    double g = offsetZ + gInt / scaleZ;
    double h = offsetZ + hInt / scaleZ;
    double i = offsetZ + iInt / scaleZ;
    double dzDivDx = (c + 2 * f + i - (a + 2 * d + g)) * oneDivCellSizeTimes8;
    double dzDivDy = (g + 2 * h + i - (a + 2 * b + c)) * oneDivCellSizeTimes8;
    float slope = atan(sqrt(dzDivDx * dzDivDx + dzDivDy * dzDivDy)) * 57.29577951308232;

  
    int red = rRange[rangeCount - 1];
    int green = gRange[rangeCount - 1];
    int blue = bRange[rangeCount - 1];
    if (slope <= slopes[0]) {
      red = rRange[0];
      green = gRange[0];
      blue = bRange[0];
    } else {
      for (int i = 1; i < rangeCount; i++) {
        float slopeCurrent = slopes[i];
        if (slope <= slopeCurrent) {
          int previousI = i - 1;
          float slopePrevious = slopes[previousI];
          float percent = (float)(slope - slopePrevious) / (slopeCurrent - slopePrevious);
          int previousR = rRange[previousI];
          red = round((previousR + (rRange[i] - previousR) * percent));
          int previousG = gRange[previousI];
          green = round((previousG + (gRange[i] - previousG) * percent));
          int previousB = bRange[previousI];
          blue = round((previousB + (bRange[i] - previousB) * percent));
          break;
        }
      }
    }
    output[imageY * width + imageX] = (uchar4)(blue, green , red, 255);
  }
}
