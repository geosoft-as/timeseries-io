"use strict"

const Signal = require("./Signal.js");
const TimeSeries = require("./TimeSeries.js");


//import * as fs from "fs"; // Node.js

const fs = require("fs");


class TimeSeriesReader
{
  static read(url)
  {
    const data = fs.readFileSync(url, "utf8");
    const json = JSON.parse(data);

    let timeSeriesList = [];

    for (let i = 0; i < json.length; i++) {
      let timeSeries = new TimeSeries();
      timeSeriesList.push(timeSeries);

      const ts = json[i];
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

module.exports = TimeSeriesReader

let t = TimeSeriesReader.read("C:/Users/jd/logdata/timeseries/NY.json");
console.log(t);

