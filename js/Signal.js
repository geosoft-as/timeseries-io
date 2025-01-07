"use strict"

import { Util } from "./Util.js";

/**
 * Class for modelling a time series signal. This can be the time values
 * themselves, or a list of associated measurement data.
 *
 * Signals are composed into TimeSeries objects where each measurement
 * is mapped with their associated time.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
export class Signal
{
  /** Name of this signal. Never null. */
  #name_;

  /** Description of this signal. May be null if not provided. */
  #description_;

  /** Quantity of this signal data. Null if unknown or N/A. */
  #quantity_;

  /** Unit of this signal. May be null indicating unitless. */
  #unit_;

  /**
   * Value type of the data of this signal.
   * One of "float", "integer", "boolean", "datetime" or "string"
   */
  #valueType_;

  /** The signal values. Array of nDimensions. */
  #values_;

  /**
   * Signal range. Array of two: smallest and largest value across dimensions.
   * Null means it needs to be recomputed.
   */
  #range_;

  /** The binary size of each element in the signal. Used with binary storage only. */
  #size_;

  /**
   * Create a TimeSeries.JSON signal instance.
   *
   * @param {string} name         Name (mnemonic) of signal. Non-null.
   * @param {string} description  Signal long name or description. May be null if not provided.
   * @param {string} quantity     Quantity of the signal data. Null if unknown or N/A.
   * @param {string} unit         Unit of measure for the signal data. Null if unitless.
   * @param {string} valueType    Value type of signal data.
   *                              One of "float", "integer", "boolean", "datetime" or "string".
   *                              Undefined converets to "float".
   * @param {number} nDimensions  Dimension of signal. &lt;0,&gt;. Undefined converts tro 1.
   * @throws TypeError  If name is null, valueType is unknown or nDimensions is out of bounds.
   */
  constructor(name, description, quantity, unit, valueType, nDimensions)
  {
    if (name == null)
      throw new TypeError("name cannot be null");

    if (!valueType)
      valueType = "float";

    if (!nDimensions)
      nDimensions = 1;

    if (nDimensions < 0)
      throw new TypeError("Invalid nDimension: " + nDimensions);

    if (valueType != "float" &&
        valueType != "integer" &&
        valueType != "boolean" &&
        valueType != "datetime" &&
        valueType != "string")
      throw new TypeError("Invalid valueType: " + valueType);

    this.#name_ = name;
    this.#description_ = description;
    this.#quantity_ = quantity;
    this.#unit_ = unit;
    this.#valueType_ = valueType != null ? valueType : "float";

    let dim = nDimensions != null ? nDimensions : 1;
    this.#values_ = [];
    for (let i = 0; i < dim; i++)
      this.#values_.push([]);

    switch (this.#valueType_) {
      case "float" : this.#size_ = 8; break;
      case "integer" : this.#size_ = 8; break;
      case "boolean" : this.#size_ = 1; break;
      case "datetime" : this.#size_ = 30; break;
      case "string" : this.#size_ = 0; break;
    }
  }

  /**
   * Create a signal from the specified signal object.
   *
   * @param {object} json  The signal as a JSON object.
   * @return {Signal}      The equivalent signal instance. Never null.
   */
  static fromJson(json)
  {
    return new Signal(json.name, json.description, json.quantity, json.unit, json.valueType, json.nDimensions);
  }

  /**
   * Return name of this signal.
   *
   * @return {string}  Name of this signal. Never null.
   */
  getName()
  {
    return this.#name_;
  }

  /**
   * Return quantity of the data of this signal.
   *
   * @return {string}  Quantity of the signal data. Null if unitless.
   */
  getQuantity()
  {
    return this.#quantity_;
  }

  /**
   * Return unit of measure of the data of this signal.
   *
   * @return {string}  Unit of measure of the signal data. Null if unitless.
   */
  getUnit()
  {
    return this.#unit_;
  }

  /**
   * Return description of this signal.
   *
   * @return {string}  Description of this signal. Null if not provided.
   */
  getDescription()
  {
    return this.#description_;
  }

  /**
   * Return the value type for the data of this signal,
   * typically Double.class, Integer.class, String.class, etc.
   *
   * @return {string}  Value type for the data of this signal. Never null.
   */
  getValueType()
  {
    return this.#valueType_;
  }

  /**
   * Return the number of dimensions of this signal.
   *
   * @return {number}  Number of dimensions of this signal. [1,&gt;.
   */
  getNDimensions()
  {
    return this.#values_.length;
  }

  /**
   * Return the size (number of bytes) of the values of this signal when stored
   * in binary format.
   * <p>
   * <b>Note:</b> For string values the <em>number of bytes</em> needed to
   * represent the longest text UTF-8 string of the signal may differ from the
   * number of <em>characters</em> in the string, as each UTF-8 character may
   * take up 1, 2 or 3 bytes.
   *
   * @return {number}  Size of a binary representation of the values of this signal. [0,&gt;.
   */
  getSize()
  {
    return this.#size_;
  }

  /**
   * Specify the (maximum) size (number of bytes) of string values of this signal.
   * <p>
   * <b>Note:</b> For string values the <em>number of bytes</em> needed to
   * represent the longest text UTF-8 string of the signal may differ from the
   * number of <em>characters</em> in the string, as each UTF-8 character may
   * take up 1, 2 or 3 bytes.
   * <p>
   * <b>Note:</b> Called by reader only.
   *
   * @param {number} size  Size to set. [0,&gt;.
   * @throws TypeError  If size &lt; 0;
   * @throws TypeError  If the method is being called for other than string signals.
   */
  setSize(size)
  {
    if (size < 0)
      throw new TypeError("Inavlid size: " + size);

    if (this.#valueType_ != "string")
      throw new TypeError("Method can only be called for string signals"); // TODO: Check ex

    this.#size_ = size;
  }

  /**
   * Add a value to this signal.
   *
   * @param {object} value - Value to add. Null to indicate absent.
   * @param {number} dimension - Dimension index. [0,nDimensions&gt; or undefined for 0.
   * @throws TypeError  If dimension is out of bounds.
   */
  addValue(value, dimension)
  {
    if (!dimension)
      dimension = 0;

    if (dimension < 0 || dimension >= this.#values_.length)
      throw new TypeError("Invalid dimension: " + dimension);

    this.#values_[dimension].push(value);

    // Flag range as invalid
    this.#range_ = null;
  }

  /**
   * Set a specific value in this signal.
   *
   * @param {object} value - Value to set. Null for absent.
   * @param {number} index - Index of signal to set. [0&gt;. If index is beyond current maximum,
   *                         the signal is padded with nulls.
   * @param {number} dimension  Dimension entry to set. [0,nDimensions&gt; or undefined for 0.
   */
  setValue(value, index, dimension)
  {
    if (!dimension)
      dimension = 0;

    if (index < 0)
      throw new TypeError("Invalid index: " + index);

    if (dimension < 0 || dimension >= this.#values_.length)
      throw new TypeError("Invalid dimension: " + dimension);

    // Pad with nulls if necessary
    for (let i = this.getNValues() - 1; i < index; i++) {
      for (let dim = 0; dim < this.getNDimensions(); dim++) {
        addValue(dim, null);
      }
    }

    // Set value
    this.#values_[dimension][index] = value;

    // Extend text size if necessary
    if (this.#valueType_ == "string" && value != null) {
      let s = value.toString();
      let size = TextEncoder().encode(s).length;
      if (size > this.#size_)
        this.#size_ = size;
    }

    // Flag range as invalid
    this.#range_ = null;
  }

  /**
   * Return number of values in the speified dimension.
   *
   * @param {number} dimension - Dimension to get number of values of. [0,nDimensions&gt;
   *                             or undefined for 0.
   * @return {number}            Number of values in the specified dimension. [0,&gt;.
   */
  getNValues(dimension)
  {
    if (dimension < 0 || dimension >= this.#values_.length)
      throw new TypeError("Invalid dimension: " + dimension);

    return this.#values_[dimension != null ? dimension : 0].length;
  }

  /**
   * Return a specific value from the given dimension of this signal.
   *
   * @param {number} index - Position index. [0,nValues&gt;.
   * @param {number} dimension - Dimension index. [0,nDimensions&gt;, or undefined for 0.
   * @return {object}  The requested value. Null if absent.
   */
  getValue(index, dimension)
  {
    if (!dimension)
      dimension = 0;

    if (dimension < 0 || dimension >= this.#values_.length)
      throw new TypeError("Invalid dimension: " + dimension);

    if (index < 0 || index >= this.#values_[dimension].length)
      throw new TypeError("Invalid index: " + index);

    return this.#values_[dimension][index];
  }

  /**
   * Remove all values from this signal.
   */
  clear()
  {
    let nDimensions = this.getNDimensions();

    this.#values_ = [];
    for (let i = 0; i < nDimensions; i++)
      this.#values_.push([]);

    if (this.#valueType_ == "string")
      this.#size_ = 0;

    this.#range_ = null;
  }

  /**
   * Return the range (i.e.&nbsp;the min and max value) of this signal.
   * The returned array is never null. The two entries may
   * be null if min/max does not exist.
   * <p>
   * If the signal is multi-dimensional, the range is reported
   * across all dimensions.
   *
   * @return  The range of this signal as an array of two (min/max).
   *          Never null. The entries may be null if no range exists.
   */
  getRange()
  {
    if (this.#range_ == null) {
      let minValue = NaN;
      let maxValue = NaN;

      let nValues = this.getNValues();
      for (let index = 0; index < this.getNValues(); index++) {
        for (let dimension = 0; dimension < this.getNDimensions(); dimension++) {
          let value = Util.getAsNumber(this.getValue(index, dimension));
          if (isNaN(minValue) || value < minValue)
            minValue = value;
          if (isNaN(maxValue) || value > maxValue)
            maxValue = value;
        }
      }

      console.log(Util.getAsType(minValue, this.#valueType_));

      this.#range_ = [Util.getAsType(minValue, this.#valueType_),
                      Util.getAsType(maxValue, this.#valueType_)];
    }

    return this.#range_;
  }

  /**
   * Return a string representation of this instance.
   */
  toString()
  {
    let s = "Signal " + "\n";
    s += "Name..........: " + this.#name_ + "\n";
    s += "Description...: " + this.#description_ + "\n";
    s += "Quantity......: " + this.#quantity_ + "\n";
    s += "Unit..........: " + this.#unit_ + "\n";
    s += "Value type....: " + this.#valueType_ + "\n";
    s += "Range.........: " + this.getRange() + "\n";

    for (let index = 0; index < this.getNValues(); index++) {
      for (let dimension = 0; dimension < this.getNDimensions(); dimension++)
        s += this.#values_[dimension][index] + ", ";
      s += "\n";
    }

    return s;
  }
}

