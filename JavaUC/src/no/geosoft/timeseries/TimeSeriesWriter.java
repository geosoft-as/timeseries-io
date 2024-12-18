package no.geosoft.timeseries;

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

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import no.geosoft.timeseries.util.Formatter;
import no.geosoft.timeseries.util.ISO8601DateParser;
import no.geosoft.timeseries.util.Indentation;
import no.geosoft.timeseries.util.JsonUtil;
import no.geosoft.timeseries.util.Util;

/**
 * Class for writing TimeSeries.JSON entries to disk.
 * <p>
 * Typical usage:
 * <blockquote>
 *   <pre>
 *   TimeSeriesWriter writer = new TimeSeriesWriter(new File("path/to/file.json"), true, 2);
 *   writer.write(timeSeries);
 *   writer.close();
 *   </pre>
 * </blockquote>
 *
 * If there is to much data to keep in memory, or the writing is based on a
 * streaming source, it is possible to append chunks of data to the last TimeSeries
 * instance written, like:
 * <blockquote>
 *   <pre>
 *   TimeSeriesWriter writer = new TimeSeriesWriter(new File("path/to/file.json"), true, 2);
 *   writer.write(timeSeries);
 *   writer.append(timeSeries);
 *   writer.append(timeSeries);
 *   :
 *   writer.close();
 *   </pre>
 * </blockquote>
 *
 * Note that the pretty print mode of this writer will behave different than
 * a standard JSON writer in that it always writes signal data arrays horizontally,
 * with each signal vertically aligned.
 * <p>
 * If the time series header contains a valid <em>dataUri</em> property, the time
 * series data will be written in binary form to this location.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class TimeSeriesWriter
  implements Closeable
{
  /** The logger instance. */
  private static final Logger logger_ = Logger.getLogger(TimeSeriesWriter.class.getName());

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
  private final Indentation indentation_;

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
  public TimeSeriesWriter(OutputStream outputStream, boolean isPretty, int indentation)
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
    indentation_ = new Indentation(isPretty ? indentation : 0);
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
  public TimeSeriesWriter(File file, boolean isPretty, int indentation)
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
    indentation_ = new Indentation(isPretty ? indentation : 0);
  }

  /**
   * Create a time series writer for the specified file.
   * Writing is done in pretty print mode with an indentation of 2.
   *
   * @param file  Disk file to write. Non-null.
   * @throws IllegalArgumentException  If file is null.
   */
  public TimeSeriesWriter(File file)
  {
    this(file, true, 2);
  }

  /**
   * Get the specified string token as a quoted text suitable for writing to
   * a JSON disk file, i.e. "null" if null, or properly escaped if non-null.
   *
   * @param value  Value to get as text. May be null.
   * @return       The value as a JSON text. Never null.
   */
  private static String getQuotedText(String value)
  {
    return value != null ? JsonUtil.encode(value) : "null";
  }

  /**
   * Compute the width of the widest element of the column of the specified signal.
   *
   * @param signal     Signal to compute column width of. Non-null.
   * @param formatter  Signal data formatter. Null if N/A for the specified signal.
   * @return           Width of widest element of the signal. [0,&gt;.
   */
  private static int computeColumnWidth(Signal signal, Formatter formatter)
  {
    assert signal != null :  "signal cannot be null";

    int columnWidth = 0;
    Class<?> valueType = signal.getValueType();

    for (int index = 0; index < signal.getNValues(); index++) {
      for (int dimension = 0; dimension < signal.getNDimensions(); dimension++) {
        Object value = signal.getValue(index, dimension);

        String text;

        if (value == null)
          text = "null";

        else if (valueType == Date.class)
          text = "2018-10-10T12:20:00Z"; // Template

        else if (formatter != null)
          text = formatter.format(Util.getAsDouble(value));

        else if (valueType == String.class)
          text = getQuotedText(value.toString());

        else // Boolean and Integers
          text = value.toString();

        if (text.length() > columnWidth)
          columnWidth = text.length();
      }
    }

    return columnWidth;
  }

  /**
   * Get the specified data value as text, according to the specified value type,
   * the signal formatter, the signal width and the general rules for the TimeSeries.JSON format.
   *
   * @param value      Signal value to get as text. May be null, in case "null" is returned.
   * @param valueType  Java value type of the signal value. Non-null.
   * @param formatter  Signal formatter. Specified for floating point values only, null otherwise,
   * @param width      Total width set aside for the values of this column. [0,&gt;.
   * @return           The JSON token to be written to file. Never null.
   */
  private String getText(Object value, Class<?> valueType, Formatter formatter, int width)
  {
    assert valueType != null : "valueType cannot be null";
    assert width >= 0 : "Invalid width: " + width;

    String text = null;

    if (value == null)
      text = "null";
    else if (valueType == Date.class)
      text = '\"' + ISO8601DateParser.toString((Date) value) + '\"';
    else if (valueType == Boolean.class)
      text = value.toString();
    else if (formatter != null)
      text = formatter.format(Util.getAsDouble(value));
    else if (Number.class.isAssignableFrom(valueType))
      text = value.toString();
    else if (valueType == String.class)
      text = getQuotedText(value.toString());
    else
      assert false : "Unrecognized valueType: " + valueType;

    String padding = isPretty_ ? Util.getSpaces(width - text.length()) : "";
    return padding + text;
  }

  /**
   * Write the specified JSON value to the current writer.
   *
   * @param jsonValue    Value to write. Non-null.
   * @param indentation  The current indentation level. Non-null.
   */
  private void writeValue(JsonValue jsonValue, Indentation indentation)
    throws IOException
  {
    assert jsonValue != null : "jsonValue cannot be null";
    assert indentation != null : "indentation cannot b3 null";

    switch (jsonValue.getValueType()) {
      case ARRAY :
        writeArray((JsonArray) jsonValue, indentation);
        break;

      case OBJECT :
        writeObject((JsonObject) jsonValue, indentation);
        break;

      case NUMBER :
        writer_.write(jsonValue.toString());
        break;

      case STRING :
        writer_.write(getQuotedText(((JsonString) jsonValue).getString()));
        break;

      case FALSE :
        writer_.write("false");
        break;

      case TRUE :
        writer_.write("true");
        break;

      case NULL :
        writer_.write("null");
        break;

      default :
        assert false : "Unrecognized value type: " + jsonValue.getValueType();
    }
  }

  /**
   * Write the specified JSON object to the current writer.
   *
   * @param jsonObject   Object to write. Non-null.
   * @param indentation  The current indentation level. Non-null.
   */
  private void writeObject(JsonObject jsonObject, Indentation indentation)
    throws IOException
  {
    assert jsonObject != null : "jsonObject cannot be null";
    assert indentation != null : "indentation cannot be null";

    writer_.write('{');

    boolean isFirst = true;

    for (Map.Entry<String,JsonValue> entry : jsonObject.entrySet()) {
      String key = entry.getKey();
      JsonValue value = entry.getValue();

      if (!isFirst)
        writer_.write(',');

      writer_.write(newline_);
      writer_.write(indentation.push().toString());
      writer_.write(getQuotedText(key));
      writer_.write(':');
      writer_.write(spacing_);

      writeValue(value, indentation.push());

      isFirst = false;
    }

    if (!jsonObject.isEmpty()) {
      writer_.write(newline_);
      writer_.write(indentation.toString());
    }

    writer_.write("}");
  }

  /**
   * Write the specified JSON array to the current writer.
   *
   * @param jsonArray    Array to write. Non-null.
   * @param indentation  The current indentation level. Non-null.
   */
  private void writeArray(JsonArray jsonArray, Indentation indentation)
    throws IOException
  {
    assert jsonArray != null : "jsonArray cannot be null";
    assert indentation != null : "indentation cannot be null";

    boolean isHorizontal = !JsonUtil.containsObjects(jsonArray);

    writer_.write('[');

    boolean isFirst = true;

    for (JsonValue jsonValue : jsonArray) {
      if (!isFirst) {
        writer_.write(",");
        if (isHorizontal)
          writer_.write(spacing_);
      }

      if (!isHorizontal) {
        writer_.write(newline_);
        writer_.write(indentation.push().toString());
      }

      writeValue(jsonValue, indentation.push());

      isFirst = false;
    }

    if (!jsonArray.isEmpty() && !isHorizontal) {
      writer_.write(newline_);
      writer_.write(indentation.toString());
    }

    writer_.write(']');
  }

  /**
   * Write the specified time series header object to the current writer.
   * <p>
   * This method is equal the writeHeader method apart from its special handling
   * of the specific keys startIndex, endIndex and step so that these gets identical
   * formatting as the index of the time series.
   *
   * @param header       The time series header. Non-null.
   * @param indentation  The current indentation level. Non-null.
   * @param timeSeries   The time series of the header. Non-null.
   */
  private void writeHeaderObject(JsonObject header, Indentation indentation, TimeSeries timeSeries)
    throws IOException
  {
    assert header != null : "header cannot be null";
    assert indentation != null : "indentation cannot be null";
    assert timeSeries != null : "timeSeries cannot be null";

    Signal indexSignal = timeSeries.getIndexSignal();
    Formatter indexFormatter = indexSignal != null ? timeSeries.createFormatter(indexSignal, true) : null;

    writer_.write('{');

    boolean isFirst = true;

    for (Map.Entry<String,JsonValue> entry : header.entrySet()) {
      String key = entry.getKey();
      JsonValue value = entry.getValue();

      if (!isFirst)
        writer_.write(',');

      writer_.write(newline_);
      writer_.write(indentation.push().toString());
      writer_.write(getQuotedText(key));
      writer_.write(':');
      writer_.write(spacing_);

      //
      // Special handling of startIndex, endIndex and step so that
      // they get the same formatting as the index data.
      //
      if (indexFormatter != null && (key.equals("startIndex") || key.equals("endIndex") || key.equals("step"))) {
        double v = Util.getAsDouble(JsonUtil.getValue(value));
        String text = Double.isFinite(v) ? indexFormatter.format(v) : "null";
        writer_.write(text);
      }
      else {
        writeValue(value, indentation.push());
      }

      isFirst = false;
    }

    if (!header.isEmpty()) {
      writer_.write(newline_);
      writer_.write(indentation.toString());
    }

    writer_.write("}");
  }

  /**
   * Write the signal data of the specified time series to the given file
   * in binary form.
   *
   * @param timeSeries  Time series to write. Non-null.
   * @param file        File to write to. Non-null.
   */
  private void writeDataAsBinary(TimeSeries timeSeries, File file)
    throws IOException
  {
    FileOutputStream fileOutputStream = new FileOutputStream(file);

    DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(fileOutputStream));

    try {
      for (int index = 0; index < timeSeries.getNValues(); index++) {
        for (Signal signal : timeSeries.getSignals()) {
          Class<?> valueType = signal.getValueType();

          int size = signal.getSize();

          for (int dimension = 0; dimension < signal.getNDimensions(); dimension++) {
            Object value = signal.getValue(index, dimension);

            //
            // Double
            //
            if (valueType == Double.class) {
              double v = value != null ? (Double) value : Double.NaN;
              outputStream.writeDouble(v);
            }

            //
            // Long
            //
            if (valueType == Long.class) {
              long v = value != null ? (Long) value : Long.MAX_VALUE;
              outputStream.writeLong(v);
            }

            //
            // String
            //
            if (valueType == String.class) {
              byte[] bytes = Util.toUtf8(value, size);
              outputStream.write(bytes, 0, bytes.length);
            }

            //
            // Boolean
            //
            if (valueType == Boolean.class) {
              int v = value != null ? ((Boolean) value) ? 1 : 0 : 255;
              outputStream.writeByte(v);
            }

            //
            // Date
            //
            if (valueType == Date.class) {
              size = 30;
              Date date = value != null ? (Date) value : null;
              String v = date != null ? ISO8601DateParser.toString(date) : "";
              String s = Util.toString(v, size);
              byte[] bytes = s.getBytes(StandardCharsets.US_ASCII);
              outputStream.write(bytes, 0, bytes.length);
            }
          }
        }
      }
    }
    catch (IOException exception) {
      throw exception;
    }
    finally {
      outputStream.close();
    }
  }

  /**
   * Write the signal data of the specified time series.
   *
   * @param timeSeries    Time series of data to write. Non-null.
   * @throws IOException  If the write operation fails for some reason.
   */
  private void writeDataAsText(TimeSeries timeSeries)
    throws IOException
  {
    assert timeSeries != null : "timeSeries cannot be null";

    Indentation indentation = indentation_.push().push().push();

    // Create formatters for each signal
    Map<Signal,Formatter> formatters = new HashMap<>();
    for (Signal signal : timeSeries.getSignals()) {
      Formatter formatter = timeSeries.createFormatter(signal, signal == timeSeries.getIndexSignal());
      formatters.put(signal, formatter);
    }

    // Compute column width for each data column
    Map<Signal,Integer> columnWidths = new HashMap<>();
    for (Signal signal : timeSeries.getSignals())
      columnWidths.put(signal, computeColumnWidth(signal, formatters.get(signal)));

    for (int index = 0; index < timeSeries.getNValues(); index++) {
      for (int signalNo = 0; signalNo < timeSeries.getNSignals(); signalNo++) {
        Signal signal = timeSeries.getSignals().get(signalNo);

        int nValues = signal.getNValues();

        Class<?> valueType = signal.getValueType();
        int nDimensions = signal.getNDimensions();
        int width = columnWidths.get(signal);
        Formatter formatter = formatters.get(signal);

        if (signalNo == 0) {
          writer_.write(indentation.toString());
          writer_.write('[');
        }

        // Multi-dimensional
        if (nDimensions > 1) {
          if (signalNo > 0) {
            writer_.write(',');
            writer_.write(spacing_);
          }

          writer_.write('[');
          for (int dimension = 0; dimension < nDimensions; dimension ++) {
            Object value = index < nValues ? signal.getValue(index, dimension) : null;
            String text = getText(value, valueType, formatter, width);

            if (dimension > 0) {
              writer_.write(',');
              writer_.write(spacing_);
            }

            writer_.write(text);
          }
          writer_.write(']');
        }

        // Single dimensional
        else {
          Object value = index < nValues ? signal.getValue(index, 0) : null;
          String text = getText(value, valueType, formatter, width);

          if (signalNo > 0) {
            writer_.write(',');
            writer_.write(spacing_);
          }

          writer_.write(text);
        }
      }

      writer_.write(']');
      if (index < timeSeries.getNValues() - 1) {
        writer_.write(',');
        writer_.write(newline_);
      }
    }
  }

  /**
   * Write the signal data of the specified time series instance.
   *
   * @param timSeries     Time series to data of. Non-null.
   * @throws IOException  If the write operation fails for some reason.
   */
  private void writeData(TimeSeries timeSeries)
    throws IOException
  {
    String dataUri = timeSeries.getDataUri();

    //
    // Case 1: Write data as JSON text in same stream
    //
    if (dataUri == null) {
      writeDataAsText(timeSeries);
    }

    //
    // Case 2: Write data as binary in separate file
    //
    if (dataUri != null) {
      try {
        URI uri = new URI(dataUri);

        // Can only refer to a relative URI if source is a file
        if (!uri.isAbsolute() && file_ != null)
          uri = file_.toURI().resolve(uri);

        File dataFile = new File(uri);

        writeDataAsBinary(timeSeries, dataFile);
      }
      catch (URISyntaxException exception) {
        logger_.log(Level.SEVERE, "Unable to write binary data to " + dataUri, exception);
      }
    }
  }

  /**
   * Write the specified time series.
   * <p>
   * Multiple time series can be written in sequence to the same stream.
   * Additional data can be appended to the last one by {@link #append}.
   * When writing is done, close the writer with {@link #close}.
   * <p>
   * If the header contains a valid <em>dataUri</em> property, the signal
   * data will be written in binary form to this location.
   *
   * @param timeSeries                 Time series to write. Non-null.
   * @throws IllegalArgumentException  If timeSeries is null.
   * @throws IOException               If the write operation fails for some reason.
   */
  public void write(TimeSeries timeSeries)
    throws IOException
  {
    if (timeSeries == null)
      throw new IllegalArgumentException("timeSeries cannot be null");

    boolean isFirstTimeSeries = writer_ == null;

    // Create the writer on first write operation
    if (isFirstTimeSeries) {
      OutputStream outputStream = file_ != null ? new FileOutputStream(file_) : outputStream_;
      writer_ = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
      writer_.write('[');
      writer_.write(newline_);
    }

    // If this is an additional time series, close the previous and make ready for a new
    else {
      writer_.write(newline_);
      writer_.write(indentation_.push().push().toString());
      writer_.write(']');
      writer_.write(newline_);

      writer_.write(indentation_.push().toString());
      writer_.write("},");
      writer_.write(newline_);
    }

    Indentation indentation = indentation_.push();

    writer_.write(indentation.toString());
    writer_.write('{');
    writer_.write(newline_);

    indentation = indentation.push();

    //
    // "header"
    //
    writer_.write(indentation.toString());
    writer_.write("\"header\":");
    writer_.write(spacing_);

    writeHeaderObject(timeSeries.getHeader(), indentation, timeSeries);

    writer_.write(',');

    //
    // "signals"
    //
    writer_.write(newline_);
    writer_.write(indentation.toString());
    writer_.write("\"signals\": [");

    boolean isFirstSignal = true;

    for (Signal signal : timeSeries.getSignals()) {

      if (!isFirstSignal)
        writer_.write(',');

      writer_.write(newline_);
      indentation = indentation.push();
      writer_.write(indentation.toString());
      writer_.write('{');
      writer_.write(newline_);
      indentation = indentation.push();

      // Name
      writer_.write(indentation.toString());
      writer_.write("\"name\":");
      writer_.write(spacing_);
      writer_.write(getQuotedText(signal.getName()));
      writer_.write(',');
      writer_.write(newline_);

      // Description
      writer_.write(indentation.toString());
      writer_.write("\"description\":");
      writer_.write(spacing_);
      writer_.write(getQuotedText(signal.getDescription()));
      writer_.write(',');
      writer_.write(newline_);

      // Quantity
      writer_.write(indentation.toString());
      writer_.write("\"quantity\":");
      writer_.write(spacing_);
      writer_.write(getQuotedText(signal.getQuantity()));
      writer_.write(',');
      writer_.write(newline_);

      // Unit
      writer_.write(indentation.toString());
      writer_.write("\"unit\":");
      writer_.write(spacing_);
      writer_.write(getQuotedText(signal.getUnit()));
      writer_.write(',');
      writer_.write(newline_);

      // Value type
      writer_.write(indentation.toString());
      writer_.write("\"valueType\":");
      writer_.write(spacing_);
      writer_.write(getQuotedText(ValueType.get(signal.getValueType()).toString()));
      writer_.write(',');
      writer_.write(newline_);

      // Max size
      if (signal.getValueType() == String.class) {
        writer_.write(indentation.toString());
        writer_.write("\"maxSize\":");
        writer_.write(spacing_);
        writer_.write("" + signal.getSize());
        writer_.write(',');
        writer_.write(newline_);
      }

      // Dimension
      writer_.write(indentation.toString());
      writer_.write("\"dimensions\":");
      writer_.write(spacing_);
      writer_.write("" + signal.getNDimensions());
      writer_.write(newline_);

      indentation = indentation.pop();
      writer_.write(indentation.toString());
      writer_.write('}');
      indentation = indentation.pop();

      isFirstSignal = false;
    }

    writer_.write(newline_);
    writer_.write(indentation.toString());
    writer_.write(']');

    //
    // "data"
    //
    writer_.write(',');
    writer_.write(newline_);
    writer_.write(indentation.toString());
    writer_.write("\"data\": [");
    writer_.write(newline_);

    writeData(timeSeries);

    hasData_ = timeSeries.getNValues() > 0;
  }

  /**
   * Append the signal data of the specified time series.
   * <p>
   * This feature can be used to <em>stream</em> data to a time series destination.
   * By repeatedly clearing and populating the signals with new data there is no need
   * for the client to keep the full volume in memory at any point in time.
   * <p>
   * If the time series header contains a valid <em>dataUri</em> property, the signal
   * data will be written in binary form to this location.
   * <p>
   * <b>NOTE:</b> This method should be called after the
   * time series metadata has been written (see {@link #write}),
   * and the time series instance must be compatible with this.
   * <p>
   * When writing is done, close the stream with {@link #close}.
   *
   * @param timeSeries  Time series to append to stream. Non-null.
   * @throws IllegalArgumentException  If timeSeries is null.
   * @throws IllegalStateException     If the writer is not open for writing.
   * @throws IOException  If the write operation fails for some reason.
   */
  public void append(TimeSeries timeSeries)
    throws IOException
  {
    if (timeSeries == null)
      throw new IllegalArgumentException("timeSeries cannot be null");

    if (writer_ == null)
      throw new IllegalStateException("Writer is not open");

    if (hasData_) {
      writer_.write(',');
      writer_.write(newline_);
    }

    writer_.write(indentation_.toString());
    writeData(timeSeries);

    if (!hasData_ && timeSeries.getNValues() > 0)
      hasData_ = true;
  }

  /**
   * Append closing brackets and close the back-end stream.
   */
  @Override
  public void close()
    throws IOException
  {
    // Nothing to do if the writer was never opened
    if (writer_ == null)
      return;

    // Complete the data array
    writer_.write(newline_);
    writer_.write(indentation_.push().push().toString());
    writer_.write(']');
    writer_.write(newline_);

    // Complete the time series object
    writer_.write(indentation_.push().toString());
    writer_.write('}');
    writer_.write(newline_);

    // Complete the time series array
    writer_.write(']');
    writer_.write(newline_);

    writer_.close();
    writer_ = null;
  }

  /**
   * Convenience method for returning a string representation of the specified time series instances.
   * <p>
   * <b>Note: </b>If a time series header contains the <em>dataUri</em> property, this
   * will be masked for the present operation so that signal data always appears
   * in the returned string.
   *
   * @param timeSeriesList  Time series instances  to write. Non-null.
   * @param isPretty        True to write in human readable pretty format, false
   *                        to write as dense as possible.
   * @param indentation     The white space indentation used in pretty print mode. [0,&gt;.
   *                        If isPretty is false, this setting has no effect.
   * @return                The requested string. Never null.
   * @throws IllegalArgumentException  If timeSeriesList is null or indentation is out of bounds.
   */
  public static String toString(List<TimeSeries> timeSeriesList, boolean isPretty, int indentation)
  {
    if (timeSeriesList == null)
      throw new IllegalArgumentException("timeSeriesList cannot be null");

    if (indentation < 0)
      throw new IllegalArgumentException("invalid indentation: " + indentation);

    ByteArrayOutputStream stringStream = new ByteArrayOutputStream();
    TimeSeriesWriter writer = new TimeSeriesWriter(stringStream, isPretty, indentation);

    String string = "";

    try {
      for (TimeSeries timeSeries : timeSeriesList) {

        // Temporarily hide the dataUri property or the data will be written
        // to binary file
        String dataUri = timeSeries.getDataUri();
        if (dataUri != null)
          timeSeries.setDataUri(null);

        writer.write(timeSeries);

        // Restore dataUri
        if (dataUri != null)
          timeSeries.setDataUri(dataUri);
      }
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
   * Convenience method for returning a string representation of the specified time series.
   * <p>
   * <b>Note: </b>If a header contains the <em>dataUri</em> property, this
   * will be masked for the present operation so that signal data always appears
   * in the returned JSON string.
   *
   * @param timeSeries   Time series to write. Non-null.
   * @param isPretty     True to write in human readable pretty format, false
   *                     to write as dense as possible.
   * @param indentation  The white space indentation used in pretty print mode. [0,&gt;.
   *                     If isPretty is false, this setting has no effect.
   * @return             The requested string. Never null.
   * @throws IllegalArgumentException  If timeSeries is null or indentation is out of bounds.
   */
  public static String toString(TimeSeries timeSeries, boolean isPretty, int indentation)
  {
    if (timeSeries == null)
      throw new IllegalArgumentException("timeSeries cannot be null");

    if (indentation < 0)
      throw new IllegalArgumentException("invalid indentation: " + indentation);

    List<TimeSeries> timeSeriesList = new ArrayList<>();
    timeSeriesList.add(timeSeries);

    return toString(timeSeriesList, isPretty, indentation);
  }

  /**
   * Convenience method for returning a pretty printed string representation
   * of the specified time series.
   * <p>
   * <b>Note: </b>If the time series header contains the <em>dataUri</em> property, this
   * will be masked for the present operation so that signal data always appears
   * in the returned string.
   *
   * @param timeSeries  Time series to write. Non-null.
   * @return            The requested string. Never null.
   * @throws IllegalArgumentException  If timeSeries is null.
   */
  public static String toString(TimeSeries timeSeries)
  {
    if (timeSeries == null)
      throw new IllegalArgumentException("timeSeries cannot be null");

    return toString(timeSeries, true, 2);
  }
}
