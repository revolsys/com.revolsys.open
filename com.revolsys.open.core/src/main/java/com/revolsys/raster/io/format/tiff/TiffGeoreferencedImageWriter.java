package com.revolsys.raster.io.format.tiff;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffImageWriterLossy;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import org.jeometry.common.exception.Exceptions;

import com.revolsys.collection.map.MapEx;
import com.revolsys.io.AbstractWriter;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.raster.GeoreferencedImageWriter;
import com.revolsys.spring.resource.Resource;

public class TiffGeoreferencedImageWriter extends AbstractWriter<GeoreferencedImage>
  implements GeoreferencedImageWriter {

  private final Resource resource;

  public TiffGeoreferencedImageWriter(final Resource resource) {
    this.resource = resource;
  }

  @Override
  public void write(final GeoreferencedImage image) {
    final BufferedImage bufferedImage = image.getBufferedImage();
    if (bufferedImage != null) {
      try (
        OutputStream out = this.resource.newBufferedOutputStream()) {
        final MapEx params = getProperties();
        final TiffOutputSet set = new TiffOutputSet();
        final TiffOutputDirectory directory = set.getOrCreateRootDirectory();

        final int width = bufferedImage.getWidth();
        final int height = bufferedImage.getHeight();
        directory.add(TiffTagConstants.TIFF_TAG_IMAGE_WIDTH, width);
        directory.add(TiffTagConstants.TIFF_TAG_IMAGE_LENGTH, height);
        directory.add(TiffTagConstants.TIFF_TAG_XRESOLUTION, RationalNumber.valueOf(72));
        directory.add(TiffTagConstants.TIFF_TAG_YRESOLUTION, RationalNumber.valueOf(72));

        // dir.add(TiffTagConstants.TIFF_TAG_IMAGE_DESCRIPTION, description);
        // dir.add(TiffTagConstants.TIFF_TAG_PAGE_NUMBER, page, page);
        // dir.add(TiffTagConstants.TIFF_TAG_YRESOLUTION, twoThirds);
        // dir.add(TiffTagConstants.TIFF_TAG_T4_OPTIONS, t4Options);
        // dir.add(GpsTagConstants.GPS_TAG_GPS_AREA_INFORMATION, area);
        // dir.add(MicrosoftHdPhotoTagConstants.EXIF_TAG_WIDTH_RESOLUTION,
        // widthRes);
        // dir.add(GeoTiffTagConstants.EXIF_TAG_GEO_DOUBLE_PARAMS_TAG,
        // geoDoubleParams);

        // final TiffElement.DataElement[] imageData = new
        // TiffElement.DataElement[strips.length];
        // for (int i = 0; i < strips.length; i++) {
        // imageData[i] = new TiffImageData.Data(0, strips[i].length,
        // strips[i]);
        // }

        // final TiffImageData tiffImageData = new
        // TiffImageData.Strips(imageData, rowsPerStrip);
        // directory.setTiffImageData(tiffImageData);

        final TiffImageWriterLossy writer = new TiffImageWriterLossy() {
          @Override
          public void write(final OutputStream os, final TiffOutputSet outputSet)
            throws IOException, ImageWriteException {
            // TODO Auto-generated method stub
            super.write(os, outputSet);
          }
        };
        // writer.write(out, set);
        writer.writeImage(bufferedImage, out, params);
      } catch (final ImageWriteException | IOException e) {
        throw Exceptions.wrap("Unable to write: " + this.resource, e);
      }
    }
  }
}
