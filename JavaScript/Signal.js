"use strict"

class Signal
{
  #name_;

  #description_;

  #quantity_;

  #unit_;

  #valueType_;

  #nDimensions_;

  #values_;

  #range_;

  #size_;

  constructor(name, description, quantity, unit, valueType, nDimensions)
  {
    this.#name_ = name;
    this.#description_ = description;
    this.#quantity_ = quantity;
    this.#unit_ = unit;
    this.#valueType_ = valueType;
    this.#nDimensions_ = nDimensions != null ? nDimensions : 1;

    this.#values_ = [];
    for (let i = 0; i < this.#nDimensions_; i++)
      this.#values_.push([]);

    switch (valueType) {
      case "float" : this.#size_ = 8; break;
      case "integer" : this.#size_ = 8; break;
      case "boolean" : this.#size_ = 1; break;
      case "datetime" : this.#size_ = 30; break;
      case "string" : this.#size_ = 0; break;
      default :
        throw new TypeError("Invalid valueType: " + valueType);
    }
  }

  static fromJson(json)
  {
    return new Signal(json.name, json.description, json.quantity, json.unit, json.valueType, json.nDimensions);
  }

  getName()
  {
    return this.#name_;
  }

  getQuantity()
  {
    return this.#quantity_;
  }

  getUnit()
  {
    return this.#unit_;
  }

  getDescription()
  {
    return this.#description_;
  }

  getValueType()
  {
    return this.#valueType_;
  }

  getNDimensions()
  {
    return this.#nDimensions_;
  }

  getSize()
  {
    return this.#size_;
  }

  setSize(size)
  {
    this.#size_ = size;
  }

  addValue(dimension, value)
  {
    if (dimension >= this.#nDimensions_)
      throw new TypeError("Invalid dimension: " + dimension);

    this.#values_[dimension].push(value);
  }

  setValue(index, dimension, value)
  {
    // Pad with nulls if necessary
    for (let i = this.getNValues() - 1; i < index; i++) {
      for (let dim = 0; dim < this.getNDimensions(); dim++) {
        addValue(dim, null);
      }
    }

    this.#values_[dimension][index] = value;

    if (this.#valueType_ == "string" && value != null) {
      let s = value.toString();
      let size = TextEncoder().encode(s).length;
      if (size > this.#size_)
        this.#size_ = size;
    }

    this.#range_ = null;
  }

  getNValues(dimension)
  {
    return this.#values_[dimension != null ? dimension : 0].length;
  }

  getValue(index, dimension)
  {
    return this.#values_[dimension != null ? dimension : 0][index];
  }

  getRange()
  {
    return [null, null];
  }

  toString()
  {
    let s = "Signal " + "\n";
    s += "Name..........: " + this.#name_ + "\n";

    return s;
  }
}

//let s = new Signal("latitude", "Position", "angle", "dega", "float", 1);
//console.log(s.toString());

module.exports = Signal

