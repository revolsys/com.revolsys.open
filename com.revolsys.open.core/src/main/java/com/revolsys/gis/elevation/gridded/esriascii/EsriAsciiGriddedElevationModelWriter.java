package com.revolsys.gis.elevation.gridded.esriascii;

import java.io.IOException;
import java.io.Writer;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.number.Doubles;
import org.jeometry.common.number.Floats;
import org.jeometry.common.number.Integers;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.gis.elevation.gridded.GriddedElevationModel;
import com.revolsys.gis.elevation.gridded.GriddedElevationModelWriter;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.spring.resource.Resource;

public class EsriAsciiGriddedElevationModelWriter extends AbstractWriter<GriddedElevationModel>
  implements GriddedElevationModelWriter {

  private Resource resource;

  private Writer writer;

  public EsriAsciiGriddedElevationModelWriter(final Resource resource) {
    this.resource = resource;
  }

  @Override
  public void close() {
    super.close();
    flush();
    if (this.writer != null) {
      FileUtil.closeSilent(this.writer);
      this.writer = null;
    }
    this.resource = null;
  }

  @Override
  public void flush() {
    if (this.writer != null) {
      try {
        this.writer.flush();
      } catch (final IOException e) {
      }
    }
  }

  @Override
  public void write(final GriddedElevationModel model) {
    final String nodataValue = DataTypes.toString(model.getProperty("nodataValue", "-9999"));
    if (this.resource == null) {
      throw new IllegalStateException("Writer is closed");
    } else {
      this.writer = this.resource.newWriter();
      try {
        final BoundingBox boundingBox = model.getBoundingBox();
        final int width = model.getWidth();
        final int height = model.getHeight();
        final int cellSize = model.getCellSize();

        this.writer.write("NCOLS ");
        this.writer.write(Integers.toString(width));
        this.writer.write('\n');

        this.writer.write("NROWS ");
        this.writer.write(Integers.toString(height));
        this.writer.write('\n');

        this.writer.write("XLLCORNER ");
        this.writer.write(Doubles.toString(boundingBox.getMinX()));
        this.writer.write('\n');

        this.writer.write("YLLCORNER ");
        this.writer.write(Doubles.toString(boundingBox.getMinY()));
        this.writer.write('\n');

        this.writer.write("CELLSIZE ");
        this.writer.write(Integers.toString(cellSize));
        this.writer.write('\n');

        this.writer.write("NODATA_VALUE ");
        this.writer.write(nodataValue);
        this.writer.write('\n');

        for (int j = 0; j < height; j++) {
          for (int i = 0; i < width; i++) {
            if (model.isNull(i, j)) {
              this.writer.write(nodataValue);
            } else {
              final float elevation = model.getElevationFloat(i, j);
              if (elevation == 0) {
                model.getElevationFloat(i, j);
              }
              final String elevationString = Floats.toString(elevation);
              this.writer.write(elevationString);
            }
            this.writer.write(' ');
          }
          this.writer.write('\n');
        }
        this.writer.write('\n');
      } catch (final Throwable e) {
        throw Exceptions.wrap("Unable to write to: " + this.resource, e);
      } finally {
        close();
      }
    }
  }
}
