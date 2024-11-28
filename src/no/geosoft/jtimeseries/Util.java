package no.geosoft.jtimeseries;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

/**
 * A collection of useful utilities for time series.
 *
 * @author <a href="mailto:info@petroware.no">Petroware AS</a>
 */
public final class Util
{
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
   * @param timeSeries         Log to add latency curve to. Non-null.
   * @param signalName         Name of signal to add. A numeric suffix is added if
   *                           the name doesn't contain one already.
   * @param signalDescription  Signal description, typically describing the
   *                           performed task responsible for the latency. May be null.
   * @param isTotalLatency     True to make a grand total of latency signals added
   *                           earlier, false to make it a regular latency signal.
   * @return                   Actual name of the curve added. Never null.
   * @throws IllegalArgumentException  If timeSeries or curveName is null.
   */
  public static String addLatencyCurve(TimeSeries timeSeries, String signalName,
                                       String signalDescription,
                                       boolean isTotalLatency)
  {
    if (timeSeries == null)
      throw new IllegalArgumentException("jsonLog cannot be null");

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
        Long latency = (Long) getAsType(value, Long.class);

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

  /**
   * Convenience method to return the specified value as a double.
   *
   * @param value  Value to represent as a double value.
   *               May be null if this is a no-value.
   * @return       The requested double value.
   */
  public static double getAsDouble(Object value)
  {
    //
    // No-value
    //
    if (value == null)
      return Double.NaN;

    //
    // Number
    //
    if (value instanceof Number) {
      Number v = (Number) value;
      return v.doubleValue();
    }

    //
    // Date
    //
    if (value instanceof Date) {
      Date date = (Date) value;
      return (double) date.getTime();
    }

    //
    // Boolean
    //
    if (value instanceof Boolean) {
      boolean b = (Boolean) value;
      return b ? 1.0 : 0.0;
    }

    //
    // String
    //
    if (value instanceof String) {
      String v = ((String) value).trim();
      if (v.isEmpty())
        return Double.NaN;

      try {
        return Double.parseDouble(v);
      }
      catch (NumberFormatException exception) {
        // Ignore. It was possibly not meant to be
        // converted like this.
      }
    }

    //
    // Others
    //
    return (double) value.hashCode();
  }

  /**
   * Return the specified double value as an equivalent
   * object of the specified type.
   *
   * @param value      Value to convert. May be null.
   * @param valueType  Value type to convert to. Non-null.
   * @return           Object of type dataType. May be null.
   * @throws IllegalArgumentException  If valueType is null.
   */
  public static Object getAsType(double value, Class<?> valueType)
  {
    if (valueType == null)
      throw new IllegalArgumentException("valueType cannot be null");

    if (Double.isNaN(value))
      return null;

    if (valueType == Double.class)
      return value;

    if (valueType == Float.class)
      return (float) value;

    if (valueType == Long.class)
      return Math.round(value);

    if (valueType == Integer.class)
      return (int) Math.round(value);

    if (valueType == Date.class)
      return new Date((long) value);

    if (valueType == String.class)
      return "" + value;

    if (valueType == Boolean.class)
      return value != 0.0;

    if (valueType == Short.class)
      return (short) value;

    if (valueType == Byte.class)
      return (byte) value;

    // Others
    return null;
  }

  /**
   * Return the specified value as an object of the given type.
   *
   * @param value      Value to consider. Null if no-value.
   * @param valueType  Type to convert to. Non-null.
   * @return           The requested object. Null if no-value.
   * @throws IllegalArgumentException  If valueType is null.
   */
  public static Object getAsType(Object value, Class<?> valueType)
  {
    if (valueType == null)
      throw new IllegalArgumentException("valueType cannot be null");

    if (value == null)
      return null;

    // Not sure why we return emty string as null, but don't change until we know
    if (valueType == String.class)
      return value.toString().length() == 0 ? null : value.toString();

    if (valueType == Date.class && value instanceof String) {
      String dateString = value.toString();
      try {
        return ISO8601DateParser.parse(dateString);
      }
      catch (Exception exception) {
        return null;
      }
    }

    if (value.getClass() == valueType)
      return value;

    return getAsType(getAsDouble(value), valueType);
  }

  /**
   * Find actual step value of the specified JSON log, being the distance between
   * values in the index curve. Three values are returned: the <em>minimum step</em>,
   * the <em>maximum step</em> and the <em>average step</em>. It is left to the client
   * to decide if these numbers represents a <em>regular</em> or an <em>irregular</em>
   * log set.
   *
   * @param signal  Signal to get step from. Non-null.
   * @return        The (minimum, maximum and average) step value of the log.
   */
  private static double[] findStep(TimeSeries timeSeries)
  {
    assert timeSeries != null : "timeSeries cannot be null";

    int nValues = timeSeries.getNValues();

    if (nValues < 2)
      return new double[] {0.0, 0.0, 0.0};

    double minStep = +Double.MAX_VALUE;
    double maxStep = -Double.MAX_VALUE;
    double averageStep = 0.0;

    int nSteps = 0;
    double indexValue0 = Util.getAsDouble(timeSeries.getValue(0, 0));
    for (int index = 1; index < nValues; index++) {
      double indexValue1 = Util.getAsDouble(timeSeries.getValue(0, index));
      double step = indexValue1 - indexValue0;

      nSteps++;

      if (step < minStep)
        minStep = step;

      if (step > maxStep)
        maxStep = step;

      averageStep += (step - averageStep) / nSteps;

      indexValue0 = indexValue1;
    }

    return new double[] {minStep, maxStep, averageStep};
  }

  /**
   * Based on the index curve, compute the step value of the specified log
   * as it will be reported in the <em>step</em> metadata.
   * <p>
   * The method uses the {@link JsonUtil#findStep} method to compute min, max and
   * average step, and then compare the largest deviation from the average
   * (min or max) to the average itself.
   * If this is within some limit (0.5% currently) the step is considered
   * regular.
   *
   * @param log  Log to compute step of. Non-null.
   * @return     The log step value. null if irregular.
   */
  static Double computeStep(TimeSeries timeSeries)
  {
    assert timeSeries != null : "timeSeries cannot be null";

    double[] step = findStep(timeSeries);

    double minStep = step[0];
    double maxStep = step[1];
    double averageStep = step[2];

    // Find largest deviation from average of the two
    double d = Math.max(Math.abs(minStep - averageStep), Math.abs(maxStep - averageStep));

    // Figure out if this is close enough to regard as equal
    // NOTE: If this number causes apparently regular log sets to appear irregular
    // we might consider adjusting it further, probably as high as 0.01 would be OK.
    boolean isEqual = d <= Math.abs(averageStep) * 0.005;

    return isEqual ? averageStep : null;
  }
}
