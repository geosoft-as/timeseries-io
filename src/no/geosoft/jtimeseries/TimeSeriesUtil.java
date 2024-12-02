package no.geosoft.jtimeseries;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import no.geosoft.jtimeseries.util.Util;

/**
 * Utility methods for working with time series.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class TimeSeriesUtil
{
  /**
   * Private constructor to prevent client instantiation.
   */
  private TimeSeriesUtil()
  {
    assert false : "This constructor should never be called";
  }

  /**
   * Return the line length of the specified log, being the number
   * of bytes of the <em>binary representation</em> of a single line
   * of data of the log.
   *
   * @param log  Log to get line length from. Non-null.
   * @return     The requested line length. [0,&gt;.
   * @throws IllegalArgumentException  If log is null.
   */
  public static int getLineLength(TimeSeries timeSeries)
  {
    if (timeSeries == null)
      throw new IllegalArgumentException("timeSeries cannot be null");

    int lineLength = 0;
    for (int signalNo = 0; signalNo < timeSeries.getNSignals(); signalNo++)
      lineLength += timeSeries.getNDimensions(signalNo) * timeSeries.getSize(signalNo);

    return lineLength;
  }

  /**
   * Return a logging friendly text string of the specified time series.
   * <p>
   * We write 4 data lines of a time series at most:
   * <pre>
   *   [index0, ...]
   *   [index1, ...]
   *   :
   *   [indexn, ...]
   * </pre>
   *
   * @param timeSeries  Time series to create logging friendly string for. Non-null.
   * @return            The requested string. Never null.
   */
  public static String toLoggingString(TimeSeries timeSeries)
  {
    int nSignals = timeSeries.getNSignals();
    int nValues = timeSeries.getNValues();

    String nSignalsText = nSignals > 1 ? ", ... (" + (nSignals - 1) + " more signals)" : "";

    StringBuilder s = new StringBuilder();
    s.append("TimeSeries: " + timeSeries.getName() + "\n");

    for (int i = 0; i < nValues; i++) {
      s.append("[" + timeSeries.getValue(0, i) + nSignalsText + "]\n");
      if (i > 1 && nValues > 4) {
        s.append(": " + (nValues - 4) + " more values\n");
        s.append("[" + timeSeries.getValue(0, nValues - 1) + nSignalsText + "]\n");
        break;
      }
    }

    return s.toString();
  }

  /**
   * A simple way to keep track of latency within a system or a pipeline
   * is to add time stamp or latency signal to the time series. This method will
   * add the specified latency signal to the given time series and compute latency
   * from similar signals added earlier.
   * <p>
   * The signal should have a numeric suffix, like TIME_T8 etc. or
   * such a suffix will be added.
   * <p>
   * The first signal of this pattern added will contain a timestamp (long
   * values of milliseconds since Epoch) while later signals added will contain
   * the latency (in milliseconds) since the <em>previous</em> signal was added.
   * <p>
   * Signals may not be a consecutive sequence. TIME_T0 can be followed
   * by TIME_T4 and so on.
   *
   * @param signalName         Name of signal to add. A numeric suffix is added if
   *                           the name doesn't contain one already.
   * @param signalDescription  Signal description, typically describing the
   *                           performed task responsible for the latency. May be null.
   * @param isTotalLatency     True to make a grand total of latency signals added
   *                           earlier, false to make it a regular latency signal.
   * @return                   Actual name of the curve added. Never null.
   * @throws IllegalArgumentException  If timeSeries or curveName is null.
   */
  public static String addLatencySignal(TimeSeries timeSeries, String signalName,
                                        String signalDescription,
                                        boolean isTotalLatency)
  {
    if (signalName == null)
      throw new IllegalArgumentException("signalName cannot be null");

    //
    // Split signalName into base name and numeric suffix.
    // If it doesn't end in a number, suffix will be null.
    //
    String baseName = signalName;
    String suffixString = null;

    Pattern pattern = Pattern.compile("([a-zA-Z_\\s]*)(.*)$");
    Matcher matcher = pattern.matcher(signalName);
    if (matcher.find()) {
      baseName = matcher.group(1);
      suffixString = matcher.group(2);
    }

    //
    // Determine suffix. Start with the one provided (or 0), but check
    // if this exists and pick the next available one.
    //
    int suffix = 0;
    try {
      suffix = Integer.parseInt(suffixString);
    }
    catch (NumberFormatException exception) {
      suffix = 0;
    }
    while (true) {
      String name = baseName + suffix;
      if (timeSeries.findSignal(name) == -1)
        break;
      suffix++;
    }

    //
    // Create the new signal
    //
    String newSignalName = isTotalLatency ? baseName : baseName + suffix;
    Signal newLatencySignal = new Signal(newSignalName, signalDescription, "Time", "ms", Long.class, 1);

    //
    // Find all existing latency signals. Since latency signals
    // may not be consecutive we search a wide range.
    //
    List<Integer> latencySignals = new ArrayList<>();
    suffix = 0;
    while (suffix < 9999) {
      String name = baseName + suffix;
      int signalNo = timeSeries.findSignal(name);
      if (signalNo != -1)
        latencySignals.add(signalNo);
      suffix++;
    }

    //
    // Time right now.
    //
    long now = System.currentTimeMillis();

    //
    // If this is the first latency signal, we populate with this number,
    // otherwise we subtract all numbers proir to this one.
    //
    for (int i = 0; i < timeSeries.getNValues(); i++) {

      // Pick the time now and subtract value from the other latency curves
      Long totalLatency = now;

      for (int signalNo : latencySignals) {
        Object value = timeSeries.getValue(signalNo, i);
        Long latency = (Long) Util.getAsType(value, Long.class);

        // In the total latency case we only want to subtract the
        // initial time stamp.
        if (isTotalLatency && latency != null && latency < 10000000L)
          latency = 0L;

        totalLatency = latency != null && totalLatency != null ? totalLatency - latency : null;
      }

      newLatencySignal.addValue(totalLatency);
    }

    timeSeries.addSignal(newLatencySignal);
    return newSignalName;
  }
}
