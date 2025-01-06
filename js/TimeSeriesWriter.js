"use strict"

import { Signal } from "./Signal.js";
import { TimeSeries } from "./TimeSeries.js";
import { TimeSeriesReader } from "./TimeSeriesReader.js";

export class TimeSeriesWriter
{
  static toJsonArray(timeSeriesList)
  {
    if (!Array.isArray(timeSeriesList))
      throw new TypeError("Invalid array: " + timeSeriesList);

    let timeSeriesArray = [];
    for (let timeSeries of timeSeriesList)
      timeSeriesArray.push(this.toJsonObject(timeSeries));

    return timeSeriesArray;
  }

  static toJsonObject(timeSeries)
  {
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

  static toString(timeSeries)
  {
    let timeSeriesList = Array.isArray(timeSeries) ? timeSeries : [timeSeries];
    let jsonArray = this.toJsonArray(timeSeriesList);
    return JSON.stringify(jsonArray, null, 2);
  }
}

/*
let timeSignal = new Signal("time", "Time", "time", "s", "datetime", 1);
let positionSignal = new Signal("position",  "Position", "latlong", "dega", "float", 2);

let t = new TimeSeries();
t.addSignal(timeSignal)
t.addSignal(positionSignal);

timeSignal.addValue(0, "12:03");
timeSignal.addValue(0, "12:04");
positionSignal.addValue(0, 10.0);
positionSignal.addValue(1, 11.0);
positionSignal.addValue(0, 20.0);
positionSignal.addValue(1, 21.0);

let timeSeriesList = [];
timeSeriesList.push(t);


let timeSeriesWriter = new TimeSeriesWriter();
console.log(timeSeriesWriter.toString(t));


*/