"use strict"

const Signal = require("./Signal.js");
const TimeSeries = require("./TimeSeries.js");
const TimeSeriesReader = require("./TimeSeriesReader.js");

class TimeSeriesWriter
{
  toJson(timeSeries)
  {
    let ts = [];

    ts.push({
      header: timeSeries.getHeader(),
      signals: [],
      data: []
    });

    for (let signalNo = 0; signalNo < timeSeries.getNSignals(); signalNo++) {
      let signal = timeSeries.getSignal(signalNo);
      ts[0].signals.push({
          name: signal.getName(),
          description: signal.getDescription(),
          quantity: signal.getQuantity(),
          unit: signal.getUnit(),
          valueType: signal.getValueType(),
          nDimensions: signal.getNDimensions()
      });
    }

    let nValues = timeSeries.getNValues();
    for (let index = 0; index < nValues; index++) {
      ts[0].data.push([]);
      let rowNo = ts[0].data[ts[0].length];

      for (let signalNo = 0; signalNo < timeSeries.getNSignals(); signalNo++) {
        let signal = timeSeries.getSignal(signalNo);
        let nDimensions = signal.getNDimensions();

        if (nDimensions == 1)


        let array = nDimension > 1 ? [] : ts[0].data.push([]);

        for (let dimension = 0; dimension < signal.getNDimensions(); dimension++) {


        }
      }
    }

    return JSON.stringify(ts, null, 2);
  }
}


module.exports = TimeSeriesWriter

let timeSeriesList = TimeSeriesReader.read("C:/Users/jd/logdata/timeseries/NY.json");
let timeSeriesWriter = new TimeSeriesWriter();

console.log(timeSeriesWriter.toJson(timeSeriesList[0]));
