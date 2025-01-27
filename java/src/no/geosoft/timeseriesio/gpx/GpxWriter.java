package no.geosoft.timeseriesio.gpx;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import no.geosoft.timeseriesio.util.Formatter;
import no.geosoft.timeseriesio.util.ISO8601DateParser;
import no.geosoft.timeseriesio.util.JsonUtil;
import no.geosoft.timeseriesio.util.XmlUtil;

/**
 * Class for writing Gpx entries to disk.
 * <p>
 * Typical usage:
 * <blockquote>
 *   <pre>
 *   GpxWriter writer = new GpxWriter(new File("path/to/file.gpx"), true, 2);
 *   writer.write(gpx);
 *   writer.close();
 *   </pre>
 * </blockquote>
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class GpxWriter
  implements Closeable
{
  /** The logger instance. */
  private static final Logger logger_ = Logger.getLogger(GpxWriter.class.getName());

  /** Platform independent new-line string. */
  private static String NEWLINE = System.getProperty("line.separator");

  /** The physical disk file to write. Null if writing directly to a stream. */
  private final File file_;

  /** The output stream to write. Null if writing to a file. */
  private final OutputStream outputStream_;

  /** True to write in human readable pretty format, false to write dense. */
  private final boolean isPretty_;

  /**
   * The new line token according to pretty print mode. Either NEWLINE or "".
   * Cached for efficiency.
   */
  private final String newline_;

  /**
   * Spacing between tokens according to pretty print mode. Either " " or "".
   * Cached for efficiency.
   */
  private final String spacing_;

  /** Current indentation according to pretty print mode. */
  private final int indentation_;

  /** The writer instance. */
  private Writer writer_;

  /** Indicate if the last written JSON file contains data or not. */
  private boolean hasData_;

  /**
   * Create a time series writer for the specified stream.
   *
   * @param outputStream  Stream to write. Non-null.
   * @param isPretty      True to write in human readable pretty format, false
   *                      to write as dense as possible.
   * @param indentation   The white space indentation used in pretty print mode. [0,&gt;.
   *                      If isPretty is false, this setting has no effect.
   * @throws IllegalArgumentException  If outputStream is null or indentation is out of bounds.
   */
  public GpxWriter(OutputStream outputStream, boolean isPretty, int indentation)
  {
    if (outputStream == null)
      throw new IllegalArgumentException("outputStream cannot be null");

    if (isPretty && indentation < 0)
      throw new IllegalArgumentException("Invalid indentation: " + indentation);

    file_ = null;
    outputStream_ = outputStream;
    isPretty_ = isPretty;
    newline_ = isPretty_ ? NEWLINE : "";
    spacing_ = isPretty_ ? " " : "";
    indentation_ = indentation;
  }

  /**
   * Create a time series writer for the specified disk file.
   *
   * @param file         Disk file to write. Non-null.
   * @param isPretty     True to write in human readable pretty format, false
   *                     to write as dense as possible.
   * @param indentation  The white space indentation used in pretty print mode. [0,&gt;.
   *                     If isPretty is false, this setting has no effect.
   * @throws IllegalArgumentException  If file is null or indentation is out of bounds.
   */
  public GpxWriter(File file, boolean isPretty, int indentation)
  {
    if (file == null)
      throw new IllegalArgumentException("file cannot be null");

    if (isPretty && indentation < 0)
      throw new IllegalArgumentException("Invalid indentation: " + indentation);

    file_ = file;
    outputStream_ = null;
    isPretty_ = isPretty;
    newline_ = isPretty_ ? NEWLINE : "";
    spacing_ = isPretty_ ? " " : "";
    indentation_ = indentation;
  }

  /**
   * Create a time series writer for the specified file.
   * Writing is done in pretty print mode with an indentation of 2.
   *
   * @param file  Disk file to write. Non-null.
   * @throws IllegalArgumentException  If file is null.
   */
  public GpxWriter(File file)
  {
    this(file, true, 2);
  }

  /**
   * Write the specified Gpx instance to this writer.
   *
   * @param gpx  Gpx instanceto write. Non-null.
   * @throws IllegalArgumentException  If gpx is null.
   * @throws IOException               If the write operation fails for some reason.
   */
  public void write(Gpx gpx)
    throws IOException
  {
    if (gpx == null)
      throw new IllegalArgumentException("gpx cannot be null");

    // TODO
  }

  @Override
  public void close()
    throws IOException
  {
    // Nothing to do if the writer was never opened
    if (writer_ == null)
      return;

    writer_.close();
    writer_ = null;
  }

  /**
   * Convenience method for returning a string representation of the specified Gpx instance.
   *
   * @param gpx          Gpx entry to write. Non-null.
   * @param isPretty     True to write in human readable pretty format, false
   *                     to write as dense as possible.
   * @param indentation  The white space indentation used in pretty print mode. [0,&gt;.
   *                     If isPretty is false, this setting has no effect.
   * @return             The requested string. Never null.
   * @throws IllegalArgumentException  If gpx is null or indentation is out of bounds.
   */
  public static String toString(Gpx gpx, boolean isPretty, int indentation)
  {
    if (gpx == null)
      throw new IllegalArgumentException("gpx cannot be null");

    if (indentation < 0)
      throw new IllegalArgumentException("invalid indentation: " + indentation);

    ByteArrayOutputStream stringStream = new ByteArrayOutputStream();
    GpxWriter writer = new GpxWriter(stringStream, isPretty, indentation);

    String string = "";

    try {
      writer.write(gpx);
    }
    catch (IOException exception) {
      // Since we are writing to memory (ByteArrayOutputStream) we don't really
      // expect an IOException so if we get one anyway, we are in serious trouble
      throw new RuntimeException("Unable to write", exception);
    }
    finally {
      try {
        writer.close();
        string = new String(stringStream.toByteArray(), StandardCharsets.UTF_8);
      }
      catch (IOException exception) {
        // Again: This will never happen.
        throw new RuntimeException("Unable to write", exception);
      }
    }

    return string;
  }

  /**
   * Convenience method for returning a pretty printed string representation
   * of the specified Gpx instance.
   *
   * @param gpx  Gpx entry to write. Non-null.
   * @return     The requested string. Never null.
   * @throws IllegalArgumentException  If gpx is null.
   */
  public static String toString(Gpx gpx)
  {
    if (gpx == null)
      throw new IllegalArgumentException("gpx cannot be null");

    return toString(gpx, true, 2);
  }
}
