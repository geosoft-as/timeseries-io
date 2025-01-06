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
   * Read TimeSeries.JSON from the specified URL.
   *
   * @param {string} url  URL of content to read. Non-null.
   * @return {array}      List of TimeSeries instances. Never null.
   */
  static read(url)
  {
    if (url == null)
      throw new TypeError("url cannot be null");

    const json = readJson(url, "utf8");
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
            signal.addValue(dimension, nDimensions > 1 ? value[dimension] : value);
        }
      }
    }

    return timeSeriesList;
  }
}

