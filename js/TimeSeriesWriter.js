"use strict"

import { Signal } from "./Signal.js";
import { TimeSeries } from "./TimeSeries.js";

/**
 * Class for converting TimeSeries instances to JSON strings.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
export class TimeSeriesWriter
{
  /**
   * Create a JSON array from the specified list of TimeSeries instances.
   *
   * @param {array} - Array of TimeSeries instances. Non-null.
   * @return {array}  A JSON array of the instances. Never null.
   */
  static toJsonArray(timeSeriesList)
  {
    if (!Array.isArray(timeSeriesList))
      throw new TypeError("Invalid array: " + timeSeriesList);

    let timeSeriesArray = [];
    for (let timeSeries of timeSeriesList)
      timeSeriesArray.push(this.toJsonObject(timeSeries));

    return timeSeriesArray;
  }

  /**
   * Create a JSON object from the specified TimeSeries instances.
   *
   * @param {TimeSeries} - TimeSeries instance to convert. Non-null.
   * @return {array}  A JSON array of the instances. Never null.
   */
  static toJsonObject(timeSeries)
  {
    if (timeSeries == null)
      throw new TypeError("timeSeries cannot be null");

    // Time series JavaScript object
    let ts = {
      header: timeSeries.getHeader(),
      signals: [],
      data: []
    }

    // Signals
    for (let signalNo = 0; signalNo < timeSeries.getNSignals(); signalNo++) {
      let signal = timeSeries.getSignal(signalNo);
      ts.signals.push({
          name: signal.getName(),
          description: signal.getDescription(),
          quantity: signal.getQuantity(),
          unit: signal.getUnit(),
          valueType: signal.getValueType(),
          nDimensions: signal.getNDimensions()
      });
    }

    // Data
    let nValues = timeSeries.getNValues();
    for (let index = 0; index < nValues; index++) {
      ts.data.push([]);
      for (let signalNo = 0; signalNo < timeSeries.getNSignals(); signalNo++) {
        let signal = timeSeries.getSignal(signalNo);
        let nDimensions = signal.getNDimensions();

        if (nDimensions == 1)
          ts.data[index].push(signal.getValue(index));

        else {
          let array = [];
          for (let dimension = 0; dimension < signal.getNDimensions(); dimension++) {
            array.push(signal.getValue(index, dimension));
          }
          ts.data[index].push(array);
        }
      }
    }

    return ts;
  }

  /**
   * Create a string representation of the argument, being either a
   * TimeSeries instance or an array of such.
   *
   * @return  A TimeSeries.JSON string representation of the given argument. Never null.
   */
  static toString(timeSeries)
  {
    let timeSeriesList = Array.isArray(timeSeries) ? timeSeries : [timeSeries];
    let jsonArray = this.toJsonArray(timeSeriesList);
    return JSON.stringify(jsonArray, null, 2);
  }
}
