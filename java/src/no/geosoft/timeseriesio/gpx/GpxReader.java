package no.geosoft.timeseriesio.gpx;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import no.geosoft.timeseriesio.util.XmlUtil;

/**
 * Reader for reading GPX files into TimeSeries.JSON.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class GpxReader
{
  /** The logger instance. */
  private static final Logger logger_ = Logger.getLogger(GpxReader.class.getName());

  /** The file to read. Null if reading from stream or JSON array. */
  private final File file_;

  /** The stream to be read. Null if reading from file or JSON array. */
  private InputStream inputStream_;

  /**
   * Create a GPX reader for the specified stream.
   *
   * @param inputStream  Stream to read. Non-null.
   * @throws IllegalArgumentException  If inputStream is null.
   */
  public GpxReader(InputStream inputStream)
  {
    if (inputStream == null)
      throw new IllegalArgumentException("inputStream cannot be null");

    file_ = null;
    inputStream_ = inputStream;
  }

  /**
   * Create a GPX reader for the specified disk file.
   *
   * @param file  Disk file to read. Non-null.
   * @throws IllegalArgumentException  If file is null.
   */
  public GpxReader(File file)
  {
    if (file == null)
      throw new IllegalArgumentException("file cannot be null");

    file_ = file;
    inputStream_ = null;
  }

  /**
   * Create a GPX reader for the specified text.
   *
   * @param text  Text to read. Non-null.
   * @throws IllegalArgumentException  If text is null.
   */
  public GpxReader(String text)
  {
    if (text == null)
      throw new IllegalArgumentException("text cannot be null");

    file_ = null;

    // NOTE: This stream will never be closed.
    // This is not a problem as it is all in memory and close() is anyway empty.
    inputStream_ = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Get the probability that the specified sequence of bytes is
   * from a GPX file.
   *
   * @param content  A number of bytes from the start of a file,
   *                 typically 2-3000. May be null, in case 0.0 is returned.
   * @return  Probability that the sequence is from a GPX file [0.0,1.0].
   * @see #isJsonFile
   */
  private static double isGpx(byte[] content)
  {
    if (content == null)
      return 0.0;

    String s = (new String(content)).toLowerCase();

    if (!s.contains("xml"))
      return 0.1;

    if (s.contains("<gpx"))
      return 0.95;

    return 0.1;
  }

  /**
   * Get the probability that the specified file is a GPX file.
   * <p>
   * The check can be done with or without considering the
   * <em>content</em> of the file. In the latter case, only
   * the file name (typically its extension) is considered.
   * In the former case a portion from the start of the file
   * is used to match for known patterns. By passing the
   * portion as an argument, the client code can read this
   * <em>once</em> and then pass it to different classifiers
   * in order to determine its most likely type.
   * <p>
   * Getting a portion of a file can be done by:
   * <pre>
   *   File file = ...;
   *   BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file));
   *   byte[] content = new byte[2000];
   *   stream.read(content, 0, content.length);
   *   stream.close();
   * </pre>
   *
   * @param file     File to check. Null to classify on content only.
   * @param content  A number of bytes from the start of the file.
   *                 Null to classify on file name only.
   * @return  Probability that the file is a GPX file. [0.0,1.0].
   */
  public static double isGpx(File file, byte[] content)
  {
    if (file == null)
      return isGpx(content);

    if (file.isDirectory())
      return 0.0;

    if (!file.exists())
      return 0.0;

    boolean isFileNameMatching = file.getName().toLowerCase(Locale.US).endsWith(".gpx");
    double contentMatch = isGpx(content);

    if (isFileNameMatching && content == null)
      return 0.75; // File name is matching, content is not considered
    else if (content != null)
      return contentMatch;
    else
      return 0.02; // File name is not matching, content is not considered
  }

  /**
   * Read the back-end of this class and return as a list of time series instances.
   *
   * @return  List of time series instances. Never null.
   * @throws IOException  If the reading operation fails for some reason.
   */
  public Gpx read()
    throws IOException
  {
    Gpx gpx = new Gpx();

    InputStream stream = inputStream_;
    if (file_ != null) {
      try {
        stream = new FileInputStream(file_);
      }
      catch (FileNotFoundException exception) {
        throw new IOException("Unable to open file: " + file_, exception);
      }
    }

    // TODO: Rewrite to SAX

    // Initialize DOM parser
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(stream);

      Element root = document.getDocumentElement();

      List<Element> trackElements = XmlUtil.findChildren(root, "trk");
      for (Element trackElement : trackElements) {
        String name = XmlUtil.getChildValue(trackElement, "name", (String) null);
        Track track = new Track(name);

        int index = 0;

        List<Element> trackSegmentElements = XmlUtil.findChildren(trackElement, "trkseg");
        for (Element trackSegmentElement : trackSegmentElements) {
          List<Element> trackPointElements = XmlUtil.findChildren(trackSegmentElement, "trkpt");

          for (Element trackPointElement : trackPointElements) {
            Double latitude = XmlUtil.getAttribute(trackPointElement, "lat", (Double) null);
            Double longitude = XmlUtil.getAttribute(trackPointElement, "lon", (Double) null);
            Double elevation = XmlUtil.getChildValue(trackPointElement, "ele", (Double) null);
            Date time = XmlUtil.getChildValue(trackPointElement, "time", (Date) null);

            TrackPoint trackPoint = new TrackPoint(time, latitude, longitude, elevation);

            //
            // Extensions (like Garmin etc.)
            //
            Element extensionElement = XmlUtil.getChild(trackPointElement, "extensions");
            if (extensionElement != null) {

              // Heart rate
              Element heartRateElement = XmlUtil.findChild(extensionElement, "gpxtpx:hr");
              if (heartRateElement != null) {
                Double heartRate = XmlUtil.getValue(heartRateElement, (Double) null);
                trackPoint.setHeartRate(heartRate);
              }

              // Cadence
              Element cadenceElement = XmlUtil.findChild(extensionElement, "gpxtpx:cad");
              if (cadenceElement != null) {
                Double cadence = XmlUtil.getValue(cadenceElement, (Double) null);
                trackPoint.setCadence(cadence);
              }
            }

            track.addTrackPoint(trackPoint);
          }
        }
      }
    }
    catch (ParserConfigurationException exception) {
      throw new IOException("Unable to read", exception);
    }
    catch (SAXException exception) {
      throw new IOException("Unable to read", exception);
    }
    finally {
      // We only close in the file input case.
      // Otherwise the client manage the stream.
      if (file_ != null)
        stream.close();
    }

    return gpx;
  }

  /**
   * Testing this class.
   *
   * @param arguments  Application arguments. Not used.
   */
  public static void main(String[] arguments)
  {
    try {
      GpxReader gpxReader = new GpxReader(new File("C:/Users/jd/logdata/timeseries/Gramstad.gpx"));

      Gpx gpx = gpxReader.read();
      System.out.println(gpx);

      File file = new File("C:/Users/jd/logdata/timeseries/Gramstad.gpx");
      GpxWriter writer = new GpxWriter(file);
      writer.write(gpx);
      writer.close();
    }
    catch (Exception exception) {
      exception.printStackTrace();
    }
  }
}
