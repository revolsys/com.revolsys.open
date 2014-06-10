package com.revolsys.swing.map.layer.raster;

import it.geosolutions.imageio.core.GCP;
import it.geosolutions.imageio.gdalframework.GDALCommonIIOImageMetadata;
import it.geosolutions.imageio.gdalframework.GDALUtilities;
import it.geosolutions.imageio.plugins.ecw.ECWImageReader;
import it.geosolutions.imageio.plugins.ecw.ECWImageReaderSpi;

import java.io.File;
import java.io.IOException;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PlanarImage;

import org.gdal.gdal.Dataset;
import org.springframework.core.io.Resource;

import com.revolsys.gdal.Gdal;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.cs.esri.EsriCoordinateSystems;
import com.revolsys.jts.geom.GeometryFactory;

public class EcwImage extends JaiGeoReferencedImage {

  private Dataset dataset;

  // @Override
  // public String getWorldFileExtension() {
  // return "eww";
  // }

  public EcwImage(final Resource imageResource) {

    try {
      setImageResource(imageResource);
      final ECWImageReader reader = (ECWImageReader)new ECWImageReaderSpi().createReaderInstance();
      final Dataset dataset = getDataset();
      final File file = getFile();
      reader.setInput(dataset);
      final ParameterBlockJAI parameters = new ParameterBlockJAI("ImageRead");
      parameters.setParameter("Input", file);
      parameters.setParameter("Reader", reader);
      final PlanarImage jaiImage = JAI.create("ImageRead", parameters);
      setJaiImage(jaiImage);

      final GDALCommonIIOImageMetadata metaData = reader.getDatasetMetadata(0);
      final String projection = dataset.GetProjection();
      System.out.println(projection);
      final double[] geoTransform = dataset.GetGeoTransform();
      if (projection != null) {
        final CoordinateSystem esriCoordinateSystem = EsriCoordinateSystems.getCoordinateSystem(projection);
        final CoordinateSystem epsgCoordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(esriCoordinateSystem);
        final int srid = epsgCoordinateSystem.getId();
        if (srid > 0 && srid < 2000000) {
          setGeometryFactory(GeometryFactory.floating(srid, 2));
        } else {
          setGeometryFactory(GeometryFactory.fixed(3005, 2, -1));
        }
      }
      setBoundingBox(geoTransform[0], geoTransform[3], geoTransform[1],
        geoTransform[5]);
      for (final GCP gcp : metaData.getGCPs()) {
        System.out.println(gcp);
      }
      postConstruct();
    } catch (final IOException e) {
      throw new RuntimeException("Unable to read: " + imageResource, e);
    }
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    if (dataset != null) {
      GDALUtilities.closeDataSet(dataset);
    }
    dataset = null;
  }

  public synchronized Dataset getDataset() {
    if (dataset == null) {
      final File file = getFile();
      dataset = Gdal.getDataset(file);
    }
    return dataset;
  }
}
