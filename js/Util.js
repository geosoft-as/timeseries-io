"use strict"

/**
 * Time series common utilities.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
export class Util
{
  /**
   * Return the specified argument as a number if possible.
   *
   * @param value - Primitive or object of any type. May be null, in case NaN will be returned.
   * @return  The specified value as a number, or null if no conversion is possible.
   */
  static getAsNumber(value)
  {
    // No-value
    if (value == null)
      return NaN;

    // Number
    if (typeof value == "number")
      return value;

    // Date
    if (value instanceof Date)
      return value.getTime();

    // Boolean
    if (typeof value == "boolean")
      return value ? 1.0 : 0.0;

    // String
    if (typeof value == "string") {
      if (value.length == 0)
        return NaN;

      return Number(value);
    }

    // Others
    return null;
  }

  /**
   * Return the specified number as an object of the given type.
   *
   * @param {number} value - Number to convert.
   * @param {string} valueType - A valid TimeSeries.JSON value type. Non-null.
   * @return  The specified number converted to the given value type, or null if no conversion is possible.
   * @throws TypeError  If valueType is null.
   */
  static getNumberAsType(value, valueType)
  {
    if (valueType == null)
      throw new TypeError("valueType cannot be null");

    // NaN reports as null for all types
    if (isNaN(value))
      return null;

    // float
    if (valueType == "float")
      return value;

    // integer
    if (valueType == "integer")
      return Math.round(value);

    // datetime
    if (valueType == "datetime")
      return new Date(Math.round(value));

    // string
    if (valueType == "string")
      return "" + value;

    // boolean
    if (valueType == "boolean")
      return value != 0.0;

    return null;
  }

  /**
   * Return the specified value as an object of the given type.
   *
   * @param {number} value - Value to convert. May be null, in case null will be returned.
   * @param {string} valueType - A valid TimeSeries.JSON value type. Non-null.
   * @return  The specified value converted to the given value type, or null if no conversion is possible.
   * @throws TypeError  If valueType is null.
   */
  static getAsType(value, valueType)
  {
    if (valueType == null)
      throw new TypeError("valueType cannot be null");

    if (value == null)
      return null;

    if (valueType == "string")
      return value.toString();

    if (valueType == "datetime" && typeof value == "string")
      return new Date(value);

    return this.getNumberAsType(this.getAsNumber(value), valueType);
  }
}
