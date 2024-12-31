"use strict"

import { Signal } from "./Signal.js";
import { TimeSeriesWriter } from "./TimeSeriesWriter.js";
import { WellKnownProperty } from "./WellKnownProperty.js";

/**
 * Class representing the content of one TimeSeries.JSON entry.
 * A TimeSeries consists of a header, signal definitions, and signal data.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
export class TimeSeries
{
  /** The time series header data as a single JSON object. */
  #header_;

  /** The signals of this time series instance. */
  #signals_ = [];

  /** Indicate if this instance includes signal data or not. */
  #hasSignalData_;

  /**
   * Create a new time series instance.
   *
   * @param {boolean} hasSignalData  Indicate if the time series includes signal data.
   *                                 May be undefined to indicate true.
   */
  constructor(hasSignalData)
  {
    this.#hasSignalData_ = hasSignalData == null || hasSignalData;
    this.#header_ = {};

    this.setProperty(WellKnownProperty.VERSION.key, "1.0");
  }

  /**
   * Return whether this time series includes curve data
   * or not, i.e.&nbsp;if only header data was read or created.
   *
   * @return {boiolean}  True if bulk (signal) data is present, false otherwise.
   */
  hasSignalData()
  {
    return this.#hasSignalData_;
  }

  /**
   * Return the header of this time series as a single JSON object.
   *
   * @return {object}  Header of this time series. Never null.
   */
  getHeader()
  {
    return this.#header_;
  }

  /**
   * Set a string header property of this time series.
   *
   * @param key    Key of property to set. Non-null.
   * @param value  Associated value. Null to unset. Must be of type
   *               BigDecimal, BigInteger, Boolean, Double, Integer,
   *               Long, String, Date or JsonValue.
   * @throws IllegalArgumentException  If key is null or value is not of a
   *               legal primitive type.
   */
  setProperty(key, value)
  {
    this.#header_[key] = value;
  }

  /**
   * Return header property for the specified key.
   * <p>
   * This is a generic method for clients that knows custom content
   * of the time series. It is up to the client program to parse the returned
   * content into the appropriate type.
   *
   * @param {string} key  Key of property to get. Non-null.
   * @return {object}     The associated value, or undefined if not found.
   * @throws TypeError    If key is null.
   */
  getProperty(key)
  {
    if (key == null)
      throw new TypeError("key cannot be null");

    return this.#header_[key];
  }

  /**
   * Add the specified signal to this time series. The first signal added to a
   * time series is by convention the index signal.
   *
   * @param {Signal} signal  Signal to add. Non-null.
   * @throws TypeError  If signal is null.
   */
  addSignal(signal)
  {
    if (signal == null)
      throw new TypeError("signal cannot be null");

    this.#signals_.push(signal);
  }

  /**
   * Return the signals of this time series. The first signal
   * is by convention always the index, typically the time values.
   *
   * @return {array}  The signals of this time series. Never null.
   */
  getSignals()
  {
    return this.#signals_;
  }

  /**
   * Return the n'th signal of this time series.
   *
   * @param signalNo  Signal suumber to get. 0 is the index signal. [0,nSignals&gt;.
   * @return          The requested signal. Never null.
   */
  getSignal(signalNo)
  {
    if (signalNo < 0 || signalNo >= this.getNSignals())
      throw new TypeError("Invalid signalNo: " + signalNo);

    return this.#signals_[signalNo];
  }

  /**
   * Find signal of the given name.
   *
   * @param {string} signalName  Name of signal to find. Non-null.
   * @return {Signal}            Requested signal, or null if not found.
   * @throws TypeError  If signalName is null.
   */
  findSignal(signalName)
  {
    if (signalName == null)
      throw new TypeError("signalName cannot be null");

    return this.#signals_.forEach(signal => signal.getName() == signalName);
  }

  /**
   * Return the number of curves in this log.
   *
   * @return  Number of curves in this log. [0,&gt;.
   */
  getNSignals()
  {
    return this.#signals_.length;
  }

  /**
   * Return the number of values in the index signal of this time series,
   * i.e. the number of time values.
   *
   * @return  Number of time values in this time series. [0,&gt;.
   */
  getNValues()
  {
    return this.#signals_.length > 0 ? this.#signals_[0].getNValues() : 0;
  }

  /**
   * Clear signal data from all signals of this time series.
   */
  clearSignals()
  {
    for (let signal of this.#signals_)
      signal.clear();
  }

  /**
   * Return a string representation of this instance.
   *
   * @return {string}  A stgring representation of this instance.
   */
  toString()
  {
    return TimeSeriesWriter.toString(this);
  }
}


/*

let timeSignal = new Signal("time", "Time", "time", "s", "datetime", 1);
let positionSignal = new Signal("position",  "Position", "latlong", "dega", "float", 2);

let t = new TimeSeries();
t.addSignal(timeSignal)
t.addSignal(positionSignal);
t.setProperty(WellKnownProperty.DESCRIPTION.key, "test");

timeSignal.addValue(0, "12:03");
timeSignal.addValue(0, "12:04");
positionSignal.addValue(0, 10.0);
positionSignal.addValue(1, 11.0);
positionSignal.addValue(0, 20.0);
positionSignal.addValue(1, 21.0);

console.log(t.getProperty("TULL"));

*/