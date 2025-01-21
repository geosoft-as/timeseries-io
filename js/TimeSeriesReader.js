"use strict"

import { Signal } from "./Signal.js";
import { TimeSeries } from "./TimeSeries.js";

//import fs from "fs"; // Node.js
//import * as fs from "fs"; // Node.js
//const fs = require("fs"); // Browser

/**
 * Class for reading TimeSeries.JSON files.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
export class TimeSeriesReader
{
  static async readJson(url)
  {
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error("Unable to access URL: " + url);
    }
    return await response.json();
  }

  /**
   * Convert the specified JSON string containing TimeSeries.JSON data
   * into a list of TimeSeries instances.
   *
   * @param {string} json  JSON string with TimeSeries.JSON instance.
   * @return {TimeSeries[]}  List of TimeSeries instances.
   */
  static readString(json)
  {
    const jsonArray = JSON.parse(json);

    let timeSeriesList = [];

    for (let i = 0; i < jsonArray.length; i++) {
      let timeSeries = new TimeSeries();
      timeSeriesList.push(timeSeries);

      const ts = jsonArray[i];
      for (let j = 0; j < ts.signals.length; j++) {
        let signal = Signal.fromJson(ts.signals[j]);

        timeSeries.addSignal(signal);
      }

      let signals = timeSeries.getSignals();

      for (let index = 0; index < ts.data.length; index++) {
        let row = ts.data[index];
        for (let signalNo = 0; signalNo < row.length; signalNo++) {
          let signal = signals[signalNo];
          let nDimensions = signal.getNDimensions();
          let value = row[signalNo];
          for (let dimension = 0; dimension < nDimensions; dimension++)
            signal.addValue(nDimensions > 1 ? value[dimension] : value, dimension);
        }
      }
    }

    return timeSeriesList;
  }

  /**
   * Read TimeSeries.JSON from the specified URL.
   *
   * @param {string} url     URL of content to read. Non-null.
   * @return {TimeSeries[]}  List of TimeSeries instances. Never null.
   */
  static readUrl(url)
  {
    if (url == null)
      throw new TypeError("url cannot be null");

    const json = readJson(url, "utf8");
    return readString(json);
  }
}
