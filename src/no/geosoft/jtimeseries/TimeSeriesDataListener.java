package no.geosoft.jtimeseries;

/**
 * Provides a mechanism for the client to monitor and process data
 * <em>during</em> a time series read operation, and also to abort the
 * process in case that is requested by user or for other reasons.
 *
 * Convenient for handling time series content that are larger than physical
 * memory. In this case the client should <em>clear</em> the time series
 * instance at fixed intervals:
 *
 * <blockquote>
 *   <pre>
 *   class DataListener implements TimeSeriesDataListener
 *   {
 *     &#64;Override
 *     public void dataRead(TimeSeries timeSeries)
 *     {
 *       // Process time series data
 *       :
 *
 *       // Clear signal data to save memory
 *       timeSeries.clearSignals();
 *
 *       // Continue the process
 *       return true;
 *     }
 *   }
 *   </pre>
 * </blockquote>
 *
 * @author <a href="mailto:info@petroware.no">Petroware AS</a>
 */
public interface TimeSeriesDataListener
{
  /**
   * A notification from {@link TimeSeriesReader} indicating that a new
   * portion of data has been read into the specified TimeSeries instance.
   *
   * After the client has processed the data, it may clean the signal data
   * in order to save memory storage. See {@link TimeSeries#clearSignals}.
   *
   * It is also possible for the client to <em>abort</em> the reading
   * process at this point, by returning <code>false</code> from the method.
   * This will close all resources and throw an InterruptedException
   * back to the client.
   *
   * @see TimeSeriesReader#read(boolean,boolean,TimeSeriesDataListener)
   *
   * @param timeSeries  Time series that has been populated with new data. Never null.
   * @return            True to continue reading, false to abort the process.
   */
  public boolean dataRead(TimeSeries timeSeries);
}
