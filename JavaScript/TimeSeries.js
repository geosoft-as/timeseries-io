"use strict"

const Signal = require("./Signal.js");

class TimeSeries
{
  #header_;

  #signals_ = [];

  #hasSignalData_;

  constructor(hasSignalData)
  {
    this.#hasSignalData_ = hasSignalData == null || hasSignalData;
    this.#header_ = {};
  }

  hasSignalData()
  {
    return this.#hasSignalData_;
  }

  getHeader()
  {
    return this.#header_;
  }

  setProperty(key, value)
  {
  }

  getProperties()
  {
  }

  getProperty(key)
  {
  }

  addSignal(signal)
  {
    this.#signals_.push(signal);
  }

  getSignals()
  {
    return this.#signals_;
  }

  getSignal(index)
  {
    return this.#signals_[index];
  }

  findSignal(name)
  {
    return this.#signals_.forEach(signal => signal.getName() == name);
  }

  getNSignals()
  {
    return this.#signals_.length;
  }

  getNValues()
  {
    return this.#signals_.length > 0 ? this.#signals_.getNValues() : 0;
  }

  toString()
  {
    return "TimeSeries";
  }
}

module.exports = TimeSeries



let timeSignal = new Signal("time", "Time", "time", "s", "datetime", 1);
let latitudeSignal = new Signal("latitude",  "Position", "angle", "dega", "float", 1);
let longitudeSignal = new Signal("longitude", "Position", "angle", "dega", "float", 1);

let t = new TimeSeries();
t.addSignal(latitudeSignal)
t.addSignal(longitudeSignal);

console.log(t.toString());



